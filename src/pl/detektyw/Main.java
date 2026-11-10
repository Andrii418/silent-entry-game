package pl.detektyw;

import java.util.Scanner;

public final class Main {
    private Main() {}

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in, "UTF-8")) {
            Game game = new Game(scanner);
            System.out.println("AKTA 01: ZNIKNIĘCIE PROTOTYPU");
            System.out.println("Wpisz numer opcji i naciśnij Enter.\n");

            while (!game.isFinished()) {
                game.showMenu();
                String input = scanner.nextLine().trim();
                game.handle(input);
            }
        }
    }
}
