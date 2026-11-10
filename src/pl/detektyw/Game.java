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
        System.out.println("4. Zebrane dowody");
        System.out.println("5. Wskaż sprawcę");
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
        if (investigation.clueCount() == 0) {
            System.out.println("Nie masz jeszcze dowodów.");
            return;
        }
        for (CaseFile.Place place : CaseFile.Place.values()) {
            if (investigation.hasVisited(place)) {
                System.out.println("• " + place.clue + " (" + place.name + ")");
            }
        }
        System.out.println("Przesłuchano: " + investigation.questionedCount() + "/3 osób.");
    }

    private void accuse() {
        if (!investigation.canAccuse()) {
            System.out.println("Najpierw zbadaj wszystkie miejsca i przesłuchaj wszystkie osoby.");
            return;
        }
        System.out.println("Kogo oskarżasz? 1. Lena  2. Marek  3. Ola");
        String choice = read();
        if (choice.equals("3") && investigation.isCulprit(CaseFile.Suspect.OLA)) {
            System.out.println("Trafnie. Ola zleciła Markowi wyłączenie kamery, a sama użyła jego identyfikatora.");
            System.out.println("Umowa pokazuje motyw: chciała sprzedać prototyp konkurencji.");
            finished = true;
        } else if (choice.equals("1") || choice.equals("2")) {
            System.out.println("Dowody nie składają się w spójną historię. Możesz spróbować ponownie.");
        } else {
            System.out.println("Nie ma takiej osoby.");
        }
    }

    private String read() {
        System.out.print("> ");
        return scanner.nextLine().trim();
    }
}
