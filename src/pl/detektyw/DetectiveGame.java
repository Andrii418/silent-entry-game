package pl.detektyw;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.ImageCursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.util.Duration;

/** Ekrany i interakcje pierwszej sprawy. Stan śledztwa przechowuje Investigation. */
public final class DetectiveGame extends Application {
    private static final String CARD = "-fx-background-color: rgba(12, 24, 36, 0.94);"
            + "-fx-background-radius: 18; -fx-border-color: #9bb8c8;"
            + "-fx-border-radius: 18; -fx-border-width: 1;";
    private static final String BUTTON = "-fx-background-color: #27465b;"
            + "-fx-background-radius: 12; -fx-text-fill: white;"
            + "-fx-font-size: 16px; -fx-padding: 12 18;";
    private static final String GOLD_BUTTON = "-fx-background-color: #b6843d;"
            + "-fx-background-radius: 12; -fx-text-fill: #111d28;"
            + "-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 12 18;";

    private Stage stage;
    private ImageCursor defaultCursor;
    private ImageCursor actionCursor;
    private Investigation investigation = new Investigation();
    private Pane currentRoom;
    private Pane activeOverlay;
    private Label message;
    private Label objective;
    private boolean solved;
    private int hintLevel;
    private String hintedObjective = "";

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        defaultCursor = arrowCursor(false);
        actionCursor = arrowCursor(true);
        stage.setTitle("Casefile 21:14");
        Scene scene = new Scene(new Pane(), 1100, 680);
        scene.setCursor(defaultCursor);
        stage.setScene(scene);
        stage.setMinWidth(850);
        stage.setMinHeight(550);
        showMenu();
        stage.show();
    }

    private ImageView background(Pane room, String file) {
        ImageView image = new ImageView(new Image(
                getClass().getResource("/images/" + file).toExternalForm()));
        image.fitWidthProperty().bind(room.widthProperty());
        image.fitHeightProperty().bind(room.heightProperty());
        room.getChildren().add(image);
        return image;
    }

    private void showMenu() {
        Pane menu = new Pane();
        background(menu, "menu-akt.png");

        Polygon firstFolder = new Polygon(420, 450, 760, 448, 785, 807, 320, 813);
        Polygon secondFolder = new Polygon(875, 462, 1230, 460, 1300, 809, 855, 815);
        Label notice = text("", 19, "#ffe2a1");
        notice.setPadding(new Insets(8, 16, 8, 16));
        notice.setStyle(notice.getStyle() + "-fx-background-color: rgba(9,21,32,0.84);"
                + "-fx-background-radius: 12;");
        notice.setVisible(false);
        notice.layoutXProperty().bind(menu.widthProperty().subtract(notice.widthProperty()).divide(2));
        notice.layoutYProperty().bind(menu.heightProperty().multiply(0.87));

        Label title = text("CASEFILE", 48, "#f5e8cd");
        title.layoutXProperty().bind(menu.widthProperty().subtract(title.widthProperty()).divide(2));
        title.layoutYProperty().bind(menu.heightProperty().multiply(0.12));
        Label subtitle = text("Wybierz sprawę", 24, "#d7e5ed");
        subtitle.layoutXProperty().bind(menu.widthProperty().subtract(subtitle.widthProperty()).divide(2));
        subtitle.layoutYProperty().bind(menu.heightProperty().multiply(0.23));

        Button first = button(solved ? "AKTA 01  •  Zagraj ponownie"
                : "AKTA 01  •  Zniknięcie prototypu", true);
        first.layoutXProperty().bind(menu.widthProperty().multiply(0.33)
                .subtract(first.widthProperty().divide(2)));
        first.layoutYProperty().bind(menu.heightProperty().multiply(0.67)
                .subtract(first.heightProperty().divide(2)));
        first.setOnAction(event -> startCase());
        menuFolder(firstFolder, first, this::startCase);

        Button second = button("AKTA 02  •  W przygotowaniu", false);
        second.layoutXProperty().bind(menu.widthProperty().multiply(0.65)
                .subtract(second.widthProperty().divide(2)));
        second.layoutYProperty().bind(menu.heightProperty().multiply(0.67)
                .subtract(second.heightProperty().divide(2)));
        Runnable unavailable = () -> {
            notice.setText("Akta 02 są w przygotowaniu.");
            notice.setVisible(true);
        };
        second.setOnAction(event -> unavailable.run());
        menuFolder(secondFolder, second, unavailable);

        scaled(menu, firstFolder, secondFolder);
        menu.getChildren().addAll(title, subtitle, first, second, notice);
        transitionTo(menu);
    }

    private void menuFolder(Polygon folder, Button label, Runnable action) {
        folder.setFill(Color.TRANSPARENT);
        folder.setStroke(Color.TRANSPARENT);
        folder.setStrokeWidth(4);
        folder.setStrokeLineJoin(StrokeLineJoin.ROUND);
        folder.setCursor(actionCursor);
        folder.setOnMouseClicked(event -> action.run());
        folder.setOnMouseEntered(event -> highlightFolder(folder, label, true));
        folder.setOnMouseExited(event -> highlightFolder(folder, label, false));
        label.setOnMouseEntered(event -> highlightFolder(folder, label, true));
        label.setOnMouseExited(event -> highlightFolder(folder, label, false));
    }

    private void highlightFolder(Polygon folder, Button label, boolean highlighted) {
        folder.setFill(highlighted ? Color.rgb(255, 208, 104, 0.14) : Color.TRANSPARENT);
        folder.setStroke(highlighted ? Color.web("#ffd16c") : Color.TRANSPARENT);
        folder.setEffect(highlighted ? new DropShadow(22, Color.web("#ffc760")) : null);
        label.setEffect(highlighted ? new DropShadow(20, Color.web("#ffd16c")) : null);
        label.setScaleX(highlighted ? 1.05 : 1);
        label.setScaleY(highlighted ? 1.05 : 1);
    }

    private static ImageCursor arrowCursor(boolean interactive) {
        WritableImage image = new WritableImage(32, 32);
        PixelWriter pixels = image.getPixelWriter();
        Color outline = Color.web("#14212b");
        Color gold = Color.web(interactive ? "#ffe37b" : "#f8c74e");
        int[] xs = {2, 2, 7, 12, 17, 12, 23};
        int[] ys = {1, 25, 20, 29, 27, 18, 18};
        for (int y = 0; y < 32; y++) {
            for (int x = 0; x < 32; x++) {
                if (!insideArrow(x + 0.5, y + 0.5, xs, ys)) continue;
                boolean edge = !insideArrow(x - 0.5, y + 0.5, xs, ys)
                        || !insideArrow(x + 1.5, y + 0.5, xs, ys)
                        || !insideArrow(x + 0.5, y - 0.5, xs, ys)
                        || !insideArrow(x + 0.5, y + 1.5, xs, ys);
                pixels.setColor(x, y, edge ? outline : gold);
            }
        }
        return new ImageCursor(image, 2, 1);
    }

    private static boolean insideArrow(double x, double y, int[] xs, int[] ys) {
        boolean inside = false;
        for (int i = 0, j = xs.length - 1; i < xs.length; j = i++) {
            if ((ys[i] > y) != (ys[j] > y)
                    && x < (double) (xs[j] - xs[i]) * (y - ys[i]) / (ys[j] - ys[i]) + xs[i]) {
                inside = !inside;
            }
        }
        return inside;
    }

    private void startCase() {
        investigation = new Investigation();
        solved = false;
        hintLevel = 0;
        hintedObjective = "";
        showLaboratory(true);
    }

    private void transitionTo(Pane next) {
        transitionTo(next, null);
    }

    private void transitionTo(Pane next, Runnable afterTransition) {
        Pane previous = (Pane) stage.getScene().getRoot();
        previous.setMouseTransparent(true);
        next.setMouseTransparent(true);
        FadeTransition out = new FadeTransition(Duration.millis(160), previous);
        out.setFromValue(previous.getOpacity());
        out.setToValue(0);
        out.setOnFinished(event -> {
            next.setOpacity(0);
            stage.getScene().setRoot(next);
            FadeTransition in = new FadeTransition(Duration.millis(280), next);
            in.setFromValue(0);
            in.setToValue(1);
            in.setOnFinished(done -> {
                next.setMouseTransparent(false);
                if (afterTransition != null) afterTransition.run();
            });
            in.play();
        });
        out.play();
    }

    private Pane room(String image, String opening) {
        Pane room = new Pane();
        background(room, image);
        currentRoom = room;
        activeOverlay = null;

        Button menu = hud(room, "☰  Menu akt", 0.02, 0.03);
        menu.setOnAction(event -> showMenu());
        Button board = hud(room, "◈  Dowody", 0.14, 0.03);
        board.setOnAction(event -> showBoard());
        Button help = hud(room, "?  Podpowiedź", 0.25, 0.03);
        help.setOnAction(event -> showHint());

        objective = text(nextObjective(), 15, "#ffd16c");
        objective.setWrapText(true);
        objective.setPadding(new Insets(9, 12, 9, 12));
        objective.setStyle(objective.getStyle() + "-fx-background-color: rgba(9,21,32,0.86);"
                + "-fx-background-radius: 10;");
        objective.layoutXProperty().bind(room.widthProperty().multiply(0.41));
        objective.layoutYProperty().bind(room.heightProperty().multiply(0.03));
        objective.prefWidthProperty().bind(room.widthProperty().multiply(0.35));

        message = text(opening, 17, "#ffffff");
        message.setWrapText(true);
        message.setPadding(new Insets(13, 17, 13, 17));
        message.setStyle(message.getStyle() + "-fx-background-color: rgba(9,21,32,0.88);"
                + "-fx-background-radius: 12;");
        message.layoutXProperty().bind(room.widthProperty().multiply(0.23));
        message.layoutYProperty().bind(room.heightProperty().subtract(95));
        message.prefWidthProperty().bind(room.widthProperty().multiply(0.54));

        room.getChildren().addAll(menu, board, help, objective, message);
        return room;
    }

    private void updateProgress() {
        if (objective != null) {
            objective.setText(nextObjective());
        }
    }

    private String nextObjective() {
        if (solved) return "Sprawa rozwiązana.";
        if (!investigation.hasVisited(CaseFile.Place.LABORATORIUM))
            return "Cel: znajdź identyfikator w laboratorium.";
        if (!investigation.hasVisited(CaseFile.Place.KORYTARZ))
            return "Cel: zbadaj korytarz i podnieś bezpiecznik.";
        if (!investigation.hasDeductedHiddenEntry())
            return "Cel: porównaj czas wejścia z awarią kamery.";
        if (!investigation.hasQuestioned(CaseFile.Suspect.MAREK))
            return "Cel: skonfrontuj Marka z dowodami.";
        if (!investigation.hasVisited(CaseFile.Place.BIURO))
            return "Cel: sprawdź drukarkę w biurze.";
        if (!investigation.hasQuestioned(CaseFile.Suspect.LENA)
                || !investigation.hasQuestioned(CaseFile.Suspect.OLA))
            return "Cel: porównaj zeznania Leny i Oli.";
        return "Cel: wskaż sprawcę w aktach dowodów.";
    }

    private void showHint() {
        String goal = nextObjective();
        if (!goal.equals(hintedObjective)) {
            hintedObjective = goal;
            hintLevel = 0;
        }
        hintLevel = Math.min(hintLevel + 1, 2);
        boolean direct = hintLevel == 2;
        String hint;
        if (!investigation.hasVisited(CaseFile.Place.LABORATORIUM)) {
            hint = direct ? "Kliknij identyfikator leżący na podłodze laboratorium."
                    : "Rozejrzyj się po podłodze, nie tylko po biurku.";
        } else if (!investigation.hasVisited(CaseFile.Place.KORYTARZ)) {
            hint = direct ? "Przejdź przez drzwi laboratorium i kliknij bezpiecznik na podłodze."
                    : "Brak zasilania kamery może mieć związek z szafką serwisową.";
        } else if (!investigation.hasDeductedHiddenEntry()) {
            hint = direct ? "Otwórz Dowody i kliknij „Połącz ślady”."
                    : "Kamera zgasła o 21:12; zamek otwarto o 21:14. Co z tego wynika?";
        } else if (!investigation.hasQuestioned(CaseFile.Suspect.MAREK)) {
            hint = direct ? "Wróć do korytarza i kliknij Marka stojącego na środku."
                    : "Technik odpowiadał za kamerę. Sprawdź jego wersję wydarzeń.";
        } else if (!investigation.hasVisited(CaseFile.Place.BIURO)) {
            hint = direct ? "Przejdź do biura i kliknij drukarkę po lewej stronie."
                    : "Kto mógł skorzystać na kradzieży? Poszukaj dokumentu.";
        } else if (!investigation.hasQuestioned(CaseFile.Suspect.LENA)
                || !investigation.hasQuestioned(CaseFile.Suspect.OLA)) {
            hint = direct ? "Kliknij obie kobiety w biurze i porównaj ich wypowiedzi."
                    : "Jedna osoba mówi, że Ola wzięła identyfikator; Ola zaprzecza dostępowi.";
        } else {
            hint = direct ? "Umowa wskazuje motyw Oli, a Marek potwierdził, że pożyczył jej identyfikator."
                    : "Sprawca potrzebował motywu i sposobu wejścia bez nagrania.";
        }
        message.setText("Podpowiedź " + hintLevel + "/2: " + hint);
    }

    private Button hud(Pane room, String title, double x, double y) {
        Button button = button(title, false);
        button.setStyle("-fx-background-color: rgba(13,31,45,0.88);"
                + "-fx-background-radius: 16; -fx-border-color: #8db6c9;"
                + "-fx-border-radius: 16; -fx-text-fill: white;"
                + "-fx-font-size: 15px; -fx-padding: 10 15;");
        button.layoutXProperty().bind(room.widthProperty().multiply(x));
        button.layoutYProperty().bind(room.heightProperty().multiply(y));
        return button;
    }

    private Button passage(Pane room, String title, double x, double y, Runnable action) {
        Button button = hud(room, title, x, y);
        button.setStyle("-fx-background-color: rgba(12,34,49,0.92);"
                + "-fx-background-radius: 22; -fx-border-color: #e5b969;"
                + "-fx-border-radius: 22; -fx-border-width: 2;"
                + "-fx-text-fill: #fff0d0; -fx-font-size: 17px;"
                + "-fx-padding: 13 20;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 12, 0.2, 0, 3);");
        button.setOnAction(event -> action.run());
        return button;
    }

    private Button button(String title, boolean important) {
        Button button = new Button(title);
        button.setStyle(important ? GOLD_BUTTON : BUTTON);
        button.setCursor(actionCursor);
        return button;
    }

    private static Label text(String value, int size, String color) {
        Label label = new Label(value);
        label.setStyle("-fx-text-fill: " + color + "; -fx-font-size: " + size + "px;");
        return label;
    }

    private void outline(Shape shape, String hint, Runnable action) {
        shape.setFill(Color.TRANSPARENT);
        shape.setStroke(Color.TRANSPARENT);
        shape.setStrokeWidth(4);
        shape.setStrokeLineCap(StrokeLineCap.ROUND);
        shape.setStrokeLineJoin(StrokeLineJoin.ROUND);
        shape.setCursor(actionCursor);
        Tooltip.install(shape, new Tooltip(hint));
        shape.setOnMouseEntered(event -> {
            shape.setStroke(Color.web("#ffd16c"));
            shape.setEffect(new DropShadow(14, Color.web("#ffc760")));
        });
        shape.setOnMouseExited(event -> {
            shape.setStroke(Color.TRANSPARENT);
            shape.setEffect(null);
        });
        shape.setOnMouseClicked(event -> action.run());
    }

    private void scaled(Pane room, Shape... shapes) {
        javafx.scene.Group group = new javafx.scene.Group(shapes);
        Scale scale = new Scale();
        scale.xProperty().bind(room.widthProperty().divide(1672));
        scale.yProperty().bind(room.heightProperty().divide(941));
        group.getTransforms().add(scale);
        room.getChildren().add(group);
    }

    private ImageView sprite(Pane room, String file, double x, double y, double width) {
        ImageView image = new ImageView(new Image(
                getClass().getResource("/images/" + file).toExternalForm()));
        image.setPreserveRatio(true);
        image.fitWidthProperty().bind(room.widthProperty().multiply(width));
        image.layoutXProperty().bind(room.widthProperty().multiply(x));
        image.layoutYProperty().bind(room.heightProperty().multiply(y));
        image.setPickOnBounds(false);
        image.setCursor(actionCursor);
        room.getChildren().add(image);
        return image;
    }

    private void raiseHud(Pane room) {
        room.getChildren().stream()
                .filter(node -> node instanceof Button || node instanceof Label)
                .toList()
                .forEach(Node::toFront);
    }

    private void showLaboratory() {
        showLaboratory(false);
    }

    private void showLaboratory(boolean introduction) {
        Pane room = room("laboratorium-v2.png", "Laboratorium jest puste. Zbadaj drzwi i podłogę.");

        Rectangle terminal = new Rectangle(818, 353, 190, 120);
        terminal.setArcWidth(14);
        terminal.setArcHeight(14);
        outline(terminal, "Terminal", () -> message.setText(
                "Terminal nie działa. Ważniejszy jest zapis elektronicznego zamka: wejście o 21:14."));

        Polygon cabinet = new Polygon(
                1350, 140, 1665, 140, 1665, 690, 1320, 690, 1320, 162);
        outline(cabinet, "Szafa", () -> message.setText(
                "W szafie nie brakuje sprzętu. Ktoś celował tylko w prototyp."));

        Polygon door = new Polygon(
                100, 160, 340, 160, 389, 200, 389, 690, 62, 690, 62, 200);
        outline(door, "Drzwi do korytarza", () -> {
            if (investigation.hasVisited(CaseFile.Place.LABORATORIUM)) {
                showCorridor();
            } else {
                message.setText("Zanim wyjdziesz, przyjrzyj się podłodze obok drzwi.");
            }
        });
        scaled(room, door, terminal, cabinet);

        ImageView badge = sprite(room, "identyfikator.png", 0.35, 0.76, 0.10);
        Tooltip.install(badge, new Tooltip("Identyfikator Marka"));
        badge.setVisible(!investigation.hasVisited(CaseFile.Place.LABORATORIUM));
        badge.setOnMouseClicked(event -> {
            investigation.discoverClue(CaseFile.Place.LABORATORIUM);
            badge.setVisible(false);
            message.setText("Identyfikator Marka. Rejestr zamka: wejście o 21:14. "
                    + "Sprawdź, co działo się wtedy z kamerą.");
            updateProgress();
        });
        raiseHud(room);
        transitionTo(room, introduction ? () -> card("AKTA 01  /  ZNIKNIĘCIE PROTOTYPU",
                "O 21:20 odkryto kradzież prototypu. W budynku zostały trzy osoby: "
                        + "Lena, Marek i Ola. Zacznij od laboratorium. Klikaj przedmioty "
                        + "oraz postacie; cel u góry podpowiada kolejny krok.",
                "Rozpocznij", this::closeOverlay) : null);
    }

    private void showCorridor() {
        Pane room = room("korytarz-v2.png", "Kamera zgasła o 21:12. Co odcięło jej zasilanie?");

        Polygon labDoor = new Polygon(126, 162, 398, 258, 398, 742, 130, 812);
        outline(labDoor, "Powrót do laboratorium", this::showLaboratory);
        Polygon camera = new Polygon(94, 23, 248, 73, 214, 129, 79, 80);
        outline(camera, "Kamera", () -> message.setText(
                "Kamera przestała nagrywać o 21:12 — dwie minuty przed otwarciem laboratorium."));
        Polygon cabinet = new Polygon(1426, 251, 1608, 194, 1608, 810, 1426, 755);
        outline(cabinet, "Szafka serwisowa", () -> message.setText(
                investigation.hasVisited(CaseFile.Place.KORYTARZ)
                        ? "W szafce brakuje bezpiecznika. Wyjęty element masz już w aktach."
                        : "W szafce brakuje bezpiecznika. Spójrz na podłogę obok niej."));
        scaled(room, labDoor, camera, cabinet);

        ImageView marek = sprite(room, "marek.png", 0.40, 0.32, 0.25);
        marek.fitWidthProperty().unbind();
        marek.fitHeightProperty().bind(room.heightProperty().multiply(0.62));
        Tooltip.install(marek, new Tooltip("Marek — technik"));
        marek.setOnMouseEntered(event -> marek.setEffect(
                new DropShadow(22, Color.web("#ffd16c"))));
        marek.setOnMouseExited(event -> marek.setEffect(null));
        marek.setOnMouseClicked(event -> {
            if (!investigation.hasDeductedHiddenEntry()) {
                message.setText("Marek: Naprawiałem kamerę. O 21:10 poszedłem po narzędzia. "
                        + "Zbierz ślady i sprawdź, czy jego wersja zgadza się z godzinami.");
            } else {
                investigation.question(CaseFile.Suspect.MAREK);
                message.setText("Marek przyznaje, że wyjął bezpiecznik na polecenie Oli "
                        + "i pożyczył jej identyfikator. Sprawdź w biurze, jaki miała motyw.");
                updateProgress();
            }
        });

        ImageView fuse = sprite(room, "bezpiecznik.png", 0.76, 0.76, 0.13);
        Tooltip.install(fuse, new Tooltip("Bezpiecznik"));
        fuse.setVisible(!investigation.hasVisited(CaseFile.Place.KORYTARZ));
        fuse.setOnMouseClicked(event -> {
            investigation.discoverClue(CaseFile.Place.KORYTARZ);
            fuse.setVisible(false);
            message.setText("Wyjęty bezpiecznik potwierdza, że ktoś celowo wyłączył kamerę. "
                    + "Otwórz Dowody i połącz go z identyfikatorem.");
            updateProgress();
        });

        room.getChildren().add(passage(room, "Do biura  →", 0.84, 0.85, this::showOffice));
        raiseHud(room);
        transitionTo(room);
    }

    private void showOffice() {
        Pane room = room("biuro.png", "W biurze są Lena, Ola i dokument pozostawiony w drukarce.");

        Polygon door = new Polygon(0, 89, 232, 95, 230, 730, 0, 751);
        outline(door, "Powrót do korytarza", this::showCorridor);
        Polygon printer = new Polygon(268, 385, 420, 381, 434, 516, 258, 519);
        outline(printer, "Drukarka", () -> {
            if (!investigation.hasVisited(CaseFile.Place.BIURO)) {
                investigation.discoverClue(CaseFile.Place.BIURO);
                message.setText("W drukarce jest umowa sprzedaży prototypu konkurencji "
                        + "z podpisem Oli. To wskazuje motyw, ale sprawdź też dostęp do laboratorium.");
                updateProgress();
            } else {
                message.setText("Umowa z podpisem Oli jest już w aktach dowodów.");
            }
        });
        Polygon lena = new Polygon(
                476, 188, 497, 185, 521, 193, 538, 211,
                543, 233, 535, 250, 548, 272, 564, 294,
                576, 325, 580, 357, 568, 389, 574, 479,
                581, 568, 552, 578, 548, 611, 562, 687,
                567, 720, 555, 746, 523, 751, 501, 740,
                481, 751, 463, 742, 465, 703, 476, 674,
                458, 636, 439, 583, 421, 568, 429, 477,
                435, 387, 427, 359, 431, 324, 444, 289,
                458, 266, 449, 238, 453, 215);
        outline(lena, "Lena — kierowniczka", () -> {
            investigation.question(CaseFile.Suspect.LENA);
            message.setText("Lena: Wyszłam z laboratorium przed 21:00. Widziałam, "
                    + "jak Ola brała od Marka identyfikator.");
            updateProgress();
        });
        Polygon ola = new Polygon(
                1404, 183, 1433, 181, 1457, 190, 1475, 210,
                1478, 235, 1467, 257, 1487, 278, 1501, 308,
                1510, 350, 1510, 409, 1501, 443, 1508, 505,
                1499, 571, 1490, 657, 1481, 751, 1484, 779,
                1501, 800, 1478, 813, 1449, 809, 1436, 790,
                1410, 795, 1384, 812, 1351, 813, 1343, 799,
                1356, 765, 1359, 697, 1353, 625, 1343, 546,
                1332, 498, 1328, 420, 1332, 352, 1344, 308,
                1361, 281, 1380, 265, 1372, 243, 1376, 213);
        outline(ola, "Ola — inwestorka", () -> {
            investigation.question(CaseFile.Suspect.OLA);
            message.setText("Ola: Nie miałam dostępu do laboratorium. Cały wieczór "
                    + "spędziłam z gośćmi. Czy jej słowa pasują do pozostałych dowodów?");
            updateProgress();
        });
        scaled(room, door, printer, lena, ola);
        room.getChildren().add(passage(room, "←  Korytarz", 0.03, 0.85, this::showCorridor));
        raiseHud(room);
        transitionTo(room);
    }

    private void showBoard() {
        Pane overlay = overlay();
        VBox panel = panel(overlay, "AKTA 01  /  DOWODY");
        panel.getChildren().add(paragraph(nextObjective()));
        for (CaseFile.Place place : CaseFile.Place.values()) {
            panel.getChildren().add(paragraph((investigation.hasVisited(place) ? "● " : "○ ")
                    + place.name + " — "
                    + (investigation.hasVisited(place) ? place.clue : "niezbadane")));
        }
        panel.getChildren().add(paragraph("Przesłuchania: " + investigation.questionedCount() + "/3"));
        if (investigation.hasDeductedHiddenEntry()) {
            panel.getChildren().add(paragraph("Dedukcja: kamera zgasła przed wejściem "
                    + "z użyciem identyfikatora Marka."));
        } else {
            Button deduce = button("Połącz identyfikator i bezpiecznik", true);
            deduce.setOnAction(event -> {
                if (investigation.deduceHiddenEntry()) {
                    updateProgress();
                    showBoard();
                } else {
                    message.setText("Do tej dedukcji potrzebujesz identyfikatora i bezpiecznika.");
                    closeOverlay();
                }
            });
            panel.getChildren().add(deduce);
        }
        Button accuse = button("Wskaż sprawcę", false);
        accuse.setOnAction(event -> showAccusation());
        panel.getChildren().add(accuse);
        Button close = button("Wróć do sceny", false);
        close.setOnAction(event -> closeOverlay());
        panel.getChildren().add(close);
    }

    private void showAccusation() {
        if (!investigation.canAccuse()) {
            card("Jeszcze za wcześnie", "Brakuje dowodów lub zeznań. " + nextObjective(),
                    "Wróć do śledztwa", this::closeOverlay);
            return;
        }
        Pane overlay = overlay();
        VBox panel = panel(overlay, "KTO UKRADŁ PROTOTYP?");
        panel.getChildren().add(paragraph(
                "Wybierz osobę, której motyw i sposób wejścia zgadzają się ze śladami."));
        for (CaseFile.Suspect suspect : CaseFile.Suspect.values()) {
            Button choice = button(suspect.name + " — " + suspect.role, false);
            choice.setOnAction(event -> {
                if (investigation.isCulprit(suspect)) {
                    solved = true;
                    updateProgress();
                    card("SPRAWA ROZWIĄZANA", "Ola zleciła wyłączenie kamery, "
                                    + "użyła identyfikatora Marka i weszła o 21:14. "
                                    + "Umowa sprzedaży wyjaśnia jej motyw.",
                            "Wróć do menu akt", this::showMenu);
                } else {
                    card("Ta teoria się nie zgadza", "Sprawdź ponownie, kto miał "
                                    + "identyfikator, kto wyłączył kamerę i czyj podpis jest na umowie.",
                            "Wróć do dowodów", this::showBoard);
                }
            });
            panel.getChildren().add(choice);
        }
        Button close = button("Wróć do dowodów", false);
        close.setOnAction(event -> showBoard());
        panel.getChildren().add(close);
    }

    private void card(String title, String body, String action, Runnable onAction) {
        Pane overlay = overlay();
        VBox panel = panel(overlay, title);
        panel.getChildren().add(paragraph(body));
        Button next = button(action, true);
        next.setOnAction(event -> onAction.run());
        panel.getChildren().add(next);
    }

    private Pane overlay() {
        closeOverlay();
        Pane overlay = new Pane();
        overlay.prefWidthProperty().bind(currentRoom.widthProperty());
        overlay.prefHeightProperty().bind(currentRoom.heightProperty());
        overlay.setStyle("-fx-background-color: rgba(3, 10, 18, 0.68);");
        currentRoom.getChildren().add(overlay);
        activeOverlay = overlay;
        return overlay;
    }

    private VBox panel(Pane overlay, String title) {
        VBox panel = new VBox(14);
        panel.setAlignment(Pos.CENTER_LEFT);
        panel.setPadding(new Insets(28));
        panel.setStyle(CARD);
        Label heading = text(title, 27, "#ffe0a0");
        heading.setWrapText(true);
        panel.getChildren().add(heading);

        ScrollPane viewport = new ScrollPane(panel);
        viewport.setFitToWidth(true);
        viewport.setPannable(true);
        viewport.setStyle("-fx-background: transparent; -fx-background-color: transparent;"
                + "-fx-border-color: transparent;");
        viewport.prefWidthProperty().bind(overlay.widthProperty().multiply(0.56));
        viewport.prefHeightProperty().bind(overlay.heightProperty().multiply(0.76));
        viewport.layoutXProperty().bind(overlay.widthProperty().multiply(0.22));
        viewport.layoutYProperty().bind(overlay.heightProperty().multiply(0.12));
        overlay.getChildren().add(viewport);
        return panel;
    }

    private Label paragraph(String value) {
        Label label = text(value, 17, "#e9eff3");
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    private void closeOverlay() {
        if (activeOverlay != null && currentRoom != null) {
            currentRoom.getChildren().remove(activeOverlay);
            activeOverlay = null;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
