package pl.detektyw;

import java.util.EnumSet;
import java.util.Set;

/** Stan sprawy i zasady wspólne dla wersji okienkowej i konsolowej. */
final class Investigation {
    private final Set<CaseFile.Place> visited = EnumSet.noneOf(CaseFile.Place.class);
    private final Set<CaseFile.Place> heldEvidence = EnumSet.noneOf(CaseFile.Place.class);
    private final Set<CaseFile.Suspect> questioned = EnumSet.noneOf(CaseFile.Suspect.class);
    private boolean deducedHiddenEntry;
    private boolean deducedAlibiContradiction;
    private boolean lockLogRead;
    private boolean cameraLogRead;
    private boolean fuseInstalled;
    private boolean cabinetInspected;

    void inspectCabinet() {
        cabinetInspected = true;
    }

    boolean hasInspectedCabinet() {
        return cabinetInspected;
    }

    void readLockLog() {
        lockLogRead = true;
    }

    boolean hasReadLockLog() {
        return lockLogRead;
    }

    boolean readCameraLog() {
        if (!fuseInstalled) return false;
        cameraLogRead = true;
        return true;
    }

    boolean hasReadCameraLog() {
        return cameraLogRead;
    }

    boolean installFuse() {
        if (fuseInstalled || !heldEvidence.remove(CaseFile.Place.KORYTARZ)) {
            return false;
        }
        fuseInstalled = true;
        return true;
    }

    boolean hasInstalledFuse() {
        return fuseInstalled;
    }

    void discoverClue(CaseFile.Place place) {
        if (visited.add(place) && place != CaseFile.Place.BIURO) {
            heldEvidence.add(place);
        }
    }

    boolean hasHeldEvidence(CaseFile.Place place) {
        return heldEvidence.contains(place);
    }

    void question(CaseFile.Suspect suspect) {
        questioned.add(suspect);
    }

    boolean hasVisited(CaseFile.Place place) {
        return visited.contains(place);
    }

    boolean hasQuestioned(CaseFile.Suspect suspect) {
        return questioned.contains(suspect);
    }

    int clueCount() {
        return visited.size();
    }

    int questionedCount() {
        return questioned.size();
    }

    boolean canAccuse() {
        return visited.size() == CaseFile.Place.values().length
                && questioned.size() == CaseFile.Suspect.values().length
                && deducedHiddenEntry
                && deducedAlibiContradiction;
    }

    record TheoryResult(boolean correct, String feedback) {}

    TheoryResult evaluateTheory(CaseFile.Suspect suspect,
                                CaseFile.EntryMethod method, CaseFile.Motive motive) {
        if (!canAccuse()) {
            return new TheoryResult(false, "Najpierw zbierz wszystkie dowody i wykonaj obie dedukcje.");
        }
        if (suspect == null || method == null || motive == null) {
            return new TheoryResult(false, "Uzupełnij osobę, sposób wejścia i motyw.");
        }
        if (suspect == CaseFile.Suspect.LENA) {
            return new TheoryResult(false, "Lena widziała identyfikator u innej osoby. Sprawdź też podpis na umowie.");
        }
        if (suspect == CaseFile.Suspect.MAREK) {
            return new TheoryResult(false, "Marek wyłączył kamerę, ale podpis na umowie i fałszywe alibi wskazują gdzie indziej.");
        }
        if (method == CaseFile.EntryMethod.FORCED_DOOR) {
            return new TheoryResult(false, "Zamek zapisał użycie identyfikatora o 21:14. Nie ma śladów włamania.");
        }
        if (method == CaseFile.EntryMethod.TECHNICIAN) {
            return new TheoryResult(false, "Marek zostawił identyfikator w biurze. Zamek zanotował użycie karty.");
        }
        if (motive == CaseFile.Motive.REVENGE) {
            return new TheoryResult(false, "Umowa z drukarki wskazuje korzyść finansową, a nie zemstę.");
        }
        if (motive == CaseFile.Motive.ACCIDENT) {
            return new TheoryResult(false, "Umowa sprzedaży i wykorzystanie awarii kamery nie pasują do przypadku.");
        }
        return new TheoryResult(true, "Ola wykorzystała awarię kamery, weszła z identyfikatorem Marka i zamierzała sprzedać prototyp.");
    }

    record DeductionResult(boolean correct, String feedback) {}

    DeductionResult deduceHiddenEntry(CaseFile.EntryConclusion conclusion) {
        if (deducedHiddenEntry) {
            return new DeductionResult(true, "Ten wniosek jest już zapisany na tablicy.");
        }
        if (!heldEvidence.contains(CaseFile.Place.LABORATORIUM)
                || !visited.contains(CaseFile.Place.KORYTARZ)) {
            return new DeductionResult(false, "Najpierw znajdź identyfikator i bezpiecznik.");
        }
        if (!lockLogRead) {
            return new DeductionResult(false,
                    "Odczytaj dziennik zamka w laboratorium.");
        }
        if (!fuseInstalled) {
            return new DeductionResult(false, "Włóż bezpiecznik do pustego gniazda, aby uruchomić kamerę.");
        }
        if (!cameraLogRead) {
            return new DeductionResult(false, "Po naprawie odczytaj zapis awarii kamery w korytarzu.");
        }
        if (conclusion == null) {
            return new DeductionResult(false, "Wybierz wniosek z obu śladów.");
        }
        if (conclusion == CaseFile.EntryConclusion.MAREK_ENTERED) {
            return new DeductionResult(false, "Zamek rozpoznał kartę Marka, ale nie osobę, która jej użyła.");
        }
        if (conclusion == CaseFile.EntryConclusion.MAREK_STOLE) {
            return new DeductionResult(false, "Bezpiecznik wyjaśnia awarię kamery, lecz nie wskazuje złodzieja.");
        }
        heldEvidence.remove(CaseFile.Place.LABORATORIUM);
        deducedHiddenEntry = true;
        return new DeductionResult(true,
                "O 21:12 kamera zgasła. O 21:14 ktoś otworzył drzwi kartą Marka bez nagrania wejścia.");
    }

    boolean hasDeductedHiddenEntry() {
        return deducedHiddenEntry;
    }

    boolean deduceAlibiContradiction() {
        if (hasQuestioned(CaseFile.Suspect.LENA)
                && hasQuestioned(CaseFile.Suspect.OLA)) {
            deducedAlibiContradiction = true;
        }
        return deducedAlibiContradiction;
    }

    boolean hasDeductedAlibiContradiction() {
        return deducedAlibiContradiction;
    }
}
