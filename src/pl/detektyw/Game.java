package pl.detektyw;

import java.util.Scanner;

final class Game {
    private final Scanner scanner;
    private final Investigation investigation = new Investigation();
    private boolean finished;

    Game(Scanner scanner) {
        this.scanner = scanner;
    }

    boolean isFinished() {
        return finished;
    }

    void showMenu() {
        System.out.println("\n--- AKTA SPRAWY ---");
        System.out.println("1. Opis sprawy");
        System.out.println("2. Zbadaj miejsce");
        System.out.println("3. Przesłuchaj osobę");
        System.out.println("4. Dowody i dedukcje");
        System.out.println("5. Wskaż sprawcę");
        System.out.println("6. Sprawdź magazyn prototypów");
        System.out.println("0. Zakończ grę");
        System.out.print("> ");
    }

    void handle(String input) {
        switch (input) {
            case "1" -> describe();
            case "2" -> places();
            case "3" -> suspects();
            case "4" -> evidence();
            case "5" -> accuse();
            case "6" -> storage();
            case "0" -> finished = true;
            default -> System.out.println("Nie ma takiej opcji.");
        }
    }

    private void describe() {
        System.out.println("O 21:20 odkryto kradzież prototypu z zamkniętego laboratorium.");
        System.out.println("W budynku zostały trzy osoby: Lena, Marek i Ola.");
        System.out.println("Ustal, kto zabrał urządzenie i jak ukrył swoje działania.");
    }

    private void places() {
        System.out.println("1. Laboratorium  2. Korytarz  3. Biuro");
        String choice = read();
        CaseFile.Place place = switch (choice) {
            case "1" -> CaseFile.Place.LABORATORIUM;
            case "2" -> CaseFile.Place.KORYTARZ;
            case "3" -> CaseFile.Place.BIURO;
            default -> null;
        };
        if (place == null) {
            System.out.println("Nie ma takiego miejsca.");
            return;
        }
        investigation.discoverClue(place);
        System.out.println(place.name + ": " + place.description);
        if (place == CaseFile.Place.LABORATORIUM) investigation.readLockLog();
        if (place == CaseFile.Place.KORYTARZ) {
            investigation.inspectCabinet();
            if (investigation.installFuse()) {
                System.out.println("Wkładasz znaleziony bezpiecznik do szafki. Kamera znów działa.");
            }
            if (investigation.readCameraLog()) {
                System.out.println("Odczyt archiwum: o 21:12 zanikło zasilanie, o 21:14 nie ma nagrania.");
            }
        }
    }

    private void suspects() {
        System.out.println("1. Lena  2. Marek  3. Ola");
        String choice = read();
        CaseFile.Suspect suspect = switch (choice) {
            case "1" -> CaseFile.Suspect.LENA;
            case "2" -> CaseFile.Suspect.MAREK;
            case "3" -> CaseFile.Suspect.OLA;
            default -> null;
        };
        if (suspect == null) {
            System.out.println("Nie ma takiej osoby.");
            return;
        }
        investigation.question(suspect);
        System.out.println(suspect.name + " — " + suspect.role);
        CaseFile.testimony(suspect, investigation.hasVisited(CaseFile.Place.KORYTARZ))
                .forEach(System.out::println);
    }

    private void evidence() {
        if (investigation.clueCount() == 0 && investigation.questionedCount() == 0) {
            System.out.println("Nie masz jeszcze dowodów ani zeznań.");
            return;
        }
        for (CaseFile.Place place : CaseFile.Place.values()) {
            if (investigation.hasVisited(place)) {
                System.out.println("• " + place.clue + " (" + place.name + ")");
            }
        }
        System.out.println("Przesłuchano: " + investigation.questionedCount() + "/3 osób.");
        System.out.println("1. Porównaj zapis zamka i awarię kamery");
        System.out.println("2. Porównaj zeznania Leny i Oli");
        System.out.println("0. Wróć do akt");
        switch (read()) {
            case "1" -> {
                System.out.println("Jaki wniosek wynika z godzin 21:12 i 21:14?");
                System.out.println("1. " + CaseFile.EntryConclusion.MAREK_ENTERED.description);
                System.out.println("2. " + CaseFile.EntryConclusion.UNRECORDED_BADGE.description);
                System.out.println("3. " + CaseFile.EntryConclusion.MAREK_STOLE.description);
                CaseFile.EntryConclusion conclusion = switch (read()) {
                    case "1" -> CaseFile.EntryConclusion.MAREK_ENTERED;
                    case "2" -> CaseFile.EntryConclusion.UNRECORDED_BADGE;
                    case "3" -> CaseFile.EntryConclusion.MAREK_STOLE;
                    default -> null;
                };
                System.out.println(investigation.deduceHiddenEntry(conclusion).feedback());
            }
            case "2" -> System.out.println(investigation.deduceAlibiContradiction()
                    ? "Lena była sama w biurze o 21:12, choć Ola twierdzi, że była wtedy z nią."
                    : "Najpierw przesłuchaj Lenę i Olę.");
            case "0" -> { }
            default -> System.out.println("Nie ma takiej opcji.");
        }
    }

    private void accuse() {
        if (!investigation.canAccuse()) {
            System.out.println("Zbierz ślady, odnajdź prototyp, przesłuchaj osoby i wykonaj obie dedukcje.");
            return;
        }
        System.out.println("Kogo oskarżasz? 1. Lena  2. Marek  3. Ola");
        CaseFile.Suspect suspect = switch (read()) {
            case "1" -> CaseFile.Suspect.LENA;
            case "2" -> CaseFile.Suspect.MAREK;
            case "3" -> CaseFile.Suspect.OLA;
            default -> null;
        };
        System.out.println("Jak wszedł sprawca? 1. Identyfikator  2. Wyłamane drzwi  3. Pomoc Marka");
        CaseFile.EntryMethod method = switch (read()) {
            case "1" -> CaseFile.EntryMethod.BADGE;
            case "2" -> CaseFile.EntryMethod.FORCED_DOOR;
            case "3" -> CaseFile.EntryMethod.TECHNICIAN;
            default -> null;
        };
        System.out.println("Dlaczego? 1. Sprzedaż  2. Zemsta  3. Przypadek");
        CaseFile.Motive motive = switch (read()) {
            case "1" -> CaseFile.Motive.SALE;
            case "2" -> CaseFile.Motive.REVENGE;
            case "3" -> CaseFile.Motive.ACCIDENT;
            default -> null;
        };
        Investigation.TheoryResult result = investigation.evaluateTheory(suspect, method, motive);
        System.out.println(result.feedback());
        if (result.correct()) finished = true;
    }

    private void storage() {
        if (!investigation.hasVisited(CaseFile.Place.BIURO)) {
            System.out.println("Najpierw odczytaj dokument w biurze, by poznać oznaczenie prototypu.");
            return;
        }
        if (investigation.hasRecoveredPrototype()) {
            System.out.println("Prototyp P-17 jest już zabezpieczony.");
            return;
        }
        System.out.println("Trzy schowki: 1. P-11  2. P-17  3. P-21. Który sprawdzisz?");
        if (read().equals("2")) {
            investigation.recoverPrototype();
            System.out.println("W schowku P-17 odnajdujesz skradziony prototyp.");
        } else {
            System.out.println("Kod tego schowka nie zgadza się z kodem prototypu w umowie.");
        }
    }

    private String read() {
        System.out.print("> ");
        return scanner.nextLine().trim();
    }
}
