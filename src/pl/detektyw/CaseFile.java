package pl.detektyw;

import java.util.List;

final class CaseFile {
    private CaseFile() {}

    enum Place {
        LABORATORIUM("Laboratorium", "Na podłodze leży identyfikator techniczny Marka. Rejestr zamka pokazuje wejście o 21:14.", "identyfikator Marka"),
        KORYTARZ("Korytarz", "Kamera zgasła o 21:12. W szafce serwisowej leży wyjęty bezpiecznik.", "bezpiecznik"),
        BIURO("Biuro", "W drukarce leży umowa sprzedaży prototypu konkurencji z podpisem Oli.", "umowa z podpisem Oli");

        final String name;
        final String description;
        final String clue;

        Place(String name, String description, String clue) {
            this.name = name;
            this.description = description;
            this.clue = clue;
        }
    }

    enum Suspect {
        LENA("Lena", "kierowniczka projektu"),
        MAREK("Marek", "technik"),
        OLA("Ola", "inwestorka");

        final String name;
        final String role;

        Suspect(String name, String role) {
            this.name = name;
            this.role = role;
        }
    }

    static List<String> testimony(Suspect suspect, boolean hasFuse) {
        return switch (suspect) {
            case LENA -> List.of("Lena: Wyszłam z laboratorium przed 21:00. Widziałam Olę, gdy brała od Marka identyfikator.");
            case MAREK -> hasFuse
                    ? List.of("Marek: Pracowałem przy kamerze, ale bezpiecznika nie dotykałem.",
                              "Pokazujesz bezpiecznik. Marek przyznaje, że wyjął go na polecenie Oli i pożyczył jej identyfikator.")
                    : List.of("Marek: Naprawiałem kamerę. O 21:10 poszedłem po narzędzia.");
            case OLA -> List.of("Ola: Nie miałam dostępu do laboratorium. Cały wieczór spędziłam z gośćmi.");
        };
    }
}
