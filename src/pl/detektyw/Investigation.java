package pl.detektyw;

import java.util.EnumSet;
import java.util.Set;

/** Stan sprawy i zasady wspólne dla wersji okienkowej i konsolowej. */
final class Investigation {
    private final Set<CaseFile.Place> visited = EnumSet.noneOf(CaseFile.Place.class);
    private final Set<CaseFile.Suspect> questioned = EnumSet.noneOf(CaseFile.Suspect.class);

    void discoverClue(CaseFile.Place place) {
        visited.add(place);
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
                && questioned.size() == CaseFile.Suspect.values().length;
    }

    boolean isCulprit(CaseFile.Suspect suspect) {
        return canAccuse() && suspect == CaseFile.Suspect.OLA;
    }

    private boolean deducedHiddenEntry;

    boolean deduceHiddenEntry() {
        if (hasVisited(CaseFile.Place.LABORATORIUM)
                && hasVisited(CaseFile.Place.KORYTARZ)) {
            deducedHiddenEntry = true;
        }
        return deducedHiddenEntry;
    }

    boolean hasDeductedHiddenEntry() {
        return deducedHiddenEntry;
    }
}
