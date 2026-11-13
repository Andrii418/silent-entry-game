package pl.detektyw;

import java.util.EnumSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

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
    private boolean prototypeRecovered;

    boolean recoverPrototype() {
        if (!visited.contains(CaseFile.Place.BIURO)) return false;
        prototypeRecovered = true;
        return true;
    }

    boolean hasRecoveredPrototype() {
        return prototypeRecovered;
    }

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
                && deducedAlibiContradiction
                && prototypeRecovered;
    }

    record TheoryResult(boolean correct, String feedback) {}

    TheoryResult evaluateTheory(CaseFile.Suspect suspect,
                                CaseFile.EntryMethod method, CaseFile.Motive motive) {
        if (!canAccuse()) {
            return new TheoryResult(false, "Zbierz dowody, odnajdź prototyp i wykonaj obie dedukcje.");
        }
        if (suspect == null || method == null || motive == null) {
            return new TheoryResult(false, "Uzupełnij osobę, sposób wejścia i motyw.");
        }
        if (suspect == CaseFile.Suspect.LENA) {
            return new TheoryResult(false, "Lena widziała identyfikator u innej osoby. Sprawdź też podpis na umowie.");
        }
        if (suspect == CaseFile.Suspect.MAREK) {
            return new TheoryResult(false, "Marek wyjął bezpiecznik, ale Lena widziała jego kartę u Oli. Sprawdź też podpis i alibi.");
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
        return new TheoryResult(true, "Lena widziała kartę Marka u Oli. Ola skorzystała z awarii kamery, weszła do laboratorium o 21:14 i ukryła prototyp przed odbiorem o 22:00.");
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

    void writeState(Properties data) {
        data.setProperty("visited", names(visited));
        data.setProperty("heldEvidence", names(heldEvidence));
        data.setProperty("questioned", names(questioned));
        data.setProperty("deducedHiddenEntry", Boolean.toString(deducedHiddenEntry));
        data.setProperty("deducedAlibiContradiction", Boolean.toString(deducedAlibiContradiction));
        data.setProperty("lockLogRead", Boolean.toString(lockLogRead));
        data.setProperty("cameraLogRead", Boolean.toString(cameraLogRead));
        data.setProperty("fuseInstalled", Boolean.toString(fuseInstalled));
        data.setProperty("cabinetInspected", Boolean.toString(cabinetInspected));
        data.setProperty("prototypeRecovered", Boolean.toString(prototypeRecovered));
    }

    static Investigation readState(Properties data) {
        Investigation result = new Investigation();
        readPlaces(data.getProperty("visited", ""), result.visited);
        readPlaces(data.getProperty("heldEvidence", ""), result.heldEvidence);
        String people = data.getProperty("questioned", "");
        if (!people.isBlank()) {
            for (String name : people.split(",")) {
                result.questioned.add(CaseFile.Suspect.valueOf(name));
            }
        }
        result.deducedHiddenEntry = flag(data, "deducedHiddenEntry");
        result.deducedAlibiContradiction = flag(data, "deducedAlibiContradiction");
        result.lockLogRead = flag(data, "lockLogRead");
        result.cameraLogRead = flag(data, "cameraLogRead");
        result.fuseInstalled = flag(data, "fuseInstalled");
        result.cabinetInspected = flag(data, "cabinetInspected");
        result.prototypeRecovered = flag(data, "prototypeRecovered");
        if (!result.visited.containsAll(result.heldEvidence)
                || result.cameraLogRead && !result.fuseInstalled
                || result.fuseInstalled && !result.visited.contains(CaseFile.Place.KORYTARZ)
                || result.fuseInstalled && result.heldEvidence.contains(CaseFile.Place.KORYTARZ)
                || result.deducedHiddenEntry && (!result.lockLogRead || !result.cameraLogRead)
                || result.deducedHiddenEntry && result.heldEvidence.contains(CaseFile.Place.LABORATORIUM)
                || result.deducedAlibiContradiction && (!result.questioned.contains(CaseFile.Suspect.LENA)
                    || !result.questioned.contains(CaseFile.Suspect.OLA))
                || result.prototypeRecovered && !result.visited.contains(CaseFile.Place.BIURO)) {
            throw new IllegalArgumentException("Niespójny zapis śledztwa");
        }
        return result;
    }

    private static String names(Set<? extends Enum<?>> values) {
        return values.stream().map(Enum::name).collect(Collectors.joining(","));
    }

    private static void readPlaces(String value, Set<CaseFile.Place> target) {
        if (value.isBlank()) return;
        for (String name : value.split(",")) {
            target.add(CaseFile.Place.valueOf(name));
        }
    }

    private static boolean flag(Properties data, String key) {
        String value = data.getProperty(key, "false");
        if (!value.equals("true") && !value.equals("false")) {
            throw new IllegalArgumentException("Nieprawidłowa wartość: " + key);
        }
        return Boolean.parseBoolean(value);
    }
}
