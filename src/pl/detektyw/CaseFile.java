package pl.detektyw;

import java.util.List;

final class CaseFile {
    private CaseFile() {}

    enum Place {
        LABORATORIUM("Laboratorium", "Na podłodze leży identyfikator techniczny Marka. Rejestr zamka pokazuje wejście o 21:14.", "identyfikator Marka"),
        KORYTARZ("Korytarz", "Kamera zgasła o 21:12. W szafce serwisowej brakuje bezpiecznika; wyjęty leżał obok na podłodze.", "bezpiecznik"),
        BIURO("Biuro", "W drukarce leży podpisana przez Olę umowa sprzedaży prototypu P-17. Odbiór z magazynu zaplanowano na 22:00.", "umowa P-17 z podpisem Oli");

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

    enum EntryMethod {
        BADGE("Identyfikator w czasie awarii kamery"),
        FORCED_DOOR("Wyłamane drzwi laboratorium"),
        TECHNICIAN("Marek osobiście otworzył drzwi");

        final String description;

        EntryMethod(String description) {
            this.description = description;
        }
    }

    enum EntryConclusion {
        MAREK_ENTERED("Marek wszedł, bo użyto jego karty"),
        UNRECORDED_BADGE("Ktoś wszedł kartą bez nagrania"),
        MAREK_STOLE("Bezpiecznik wskazuje Marka jako złodzieja");

        final String description;

        EntryConclusion(String description) {
            this.description = description;
        }
    }

    enum Motive {
        SALE("Sprzedaż prototypu konkurencji"),
        REVENGE("Zemsta na zespole"),
        ACCIDENT("Przypadkowe wyniesienie sprzętu");

        final String description;

        Motive(String description) {
            this.description = description;
        }
    }

    static List<String> testimony(Suspect suspect, boolean hasFuse) {
        return switch (suspect) {
            case LENA -> List.of("Lena: O 21:12 byłam sama w biurze. Ola wyszła kilka minut wcześniej.",
                                 "Widziałam u Oli identyfikator Marka, ale nie widziałam jej w laboratorium.");
            case MAREK -> hasFuse
                    ? List.of("Marek: Testowałem zasilanie kamery. Wyszedłem po narzędzia, a przed końcem naprawy ogłoszono kradzież.",
                              "Pokazujesz bezpiecznik. Marek: O 21:12 wyjąłem go do testu. Upadł przy szafce. Identyfikator zostawiłem w biurze.")
                    : List.of("Marek: Testowałem zasilanie kamery. Wyszedłem po narzędzia.");
            case OLA -> List.of("Ola: O 21:12 byłam z Leną w biurze.",
                               "Nie miałam identyfikatora Marka ani dostępu do laboratorium.");
        };
    }
}
