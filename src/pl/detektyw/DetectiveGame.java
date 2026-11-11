package pl.detektyw;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.ImageCursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
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
    private static final String THEORY_OPTION = "-fx-background-color: rgba(255, 244, 218, 0.22);"
            + "-fx-border-color: rgba(96, 61, 25, 0.35); -fx-border-radius: 8;"
            + "-fx-background-radius: 8; -fx-text-fill: #322417;"
            + "-fx-font-family: Georgia; -fx-font-size: 21px; -fx-padding: 9 12;";
    private static final String THEORY_SELECTED = "-fx-background-color: rgba(231, 170, 72, 0.72);"
            + "-fx-border-color: #7c4d1d; -fx-border-width: 2; -fx-border-radius: 8;"
            + "-fx-background-radius: 8; -fx-text-fill: #21170e;"
            + "-fx-font-family: Georgia; -fx-font-size: 21px; -fx-font-weight: bold; -fx-padding: 9 12;";

    private Stage stage;
    private ImageCursor defaultCursor;
    private ImageCursor actionCursor;
    private Investigation investigation = new Investigation();
    private Pane currentRoom;
    private Pane activeOverlay;
    private Label message;
    private Label objective;
    private VBox inventoryPanel;
    private Label inventoryCount;
    private ImageView badgeInventoryIcon;
    private ImageView fuseInventoryIcon;
    private Label badgePlaceholder;
    private Label fusePlaceholder;
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

        inventoryPanel = inventory(room);
        room.getChildren().addAll(menu, board, help, objective, message, inventoryPanel);
        updateInventory();
        return room;
    }

    private void updateProgress() {
        if (objective != null) {
            objective.setText(nextObjective());
        }
        updateInventory();
    }

    private VBox inventory(Pane room) {
        inventoryCount = text("PRZEDMIOTY 0/2", 12, "#ffe0a0");
        badgePlaceholder = text("?", 28, "#91a3aa");
        fusePlaceholder = text("?", 28, "#91a3aa");
        badgeInventoryIcon = inventoryImage("identyfikator.png");
        fuseInventoryIcon = inventoryImage("bezpiecznik.png");
        StackPane badgeSlot = inventorySlot(badgePlaceholder, badgeInventoryIcon,
                "Identyfikator: zamek odnotował jego użycie o 21:14");
        StackPane fuseSlot = inventorySlot(fusePlaceholder, fuseInventoryIcon,
                "Bezpiecznik: kamera zgasła o 21:12");
        HBox slots = new HBox(8, badgeSlot, fuseSlot);
        VBox panel = new VBox(5, inventoryCount, slots);
        panel.setPadding(new Insets(8));
        panel.setStyle("-fx-background-color: rgba(8, 21, 34, 0.91);"
                + "-fx-background-radius: 12; -fx-border-color: #b6843d;"
                + "-fx-border-radius: 12;");
        panel.layoutXProperty().bind(room.widthProperty().multiply(0.79));
        panel.layoutYProperty().bind(room.heightProperty().multiply(0.025));
        return panel;
    }

    private ImageView inventoryImage(String file) {
        ImageView icon = new ImageView(new Image(
                getClass().getResource("/images/" + file).toExternalForm()));
        icon.setPreserveRatio(true);
        icon.setFitWidth(56);
        icon.setFitHeight(43);
        return icon;
    }

    private StackPane inventorySlot(Label placeholder, ImageView icon, String hint) {
        StackPane slot = new StackPane(placeholder, icon);
        slot.setPrefSize(64, 52);
        slot.setMinSize(64, 52);
        slot.setStyle("-fx-background-color: rgba(30, 48, 61, 0.9);"
                + "-fx-background-radius: 8;");
        Tooltip.install(slot, new Tooltip(hint));
        return slot;
    }

    private void updateInventory() {
        if (inventoryPanel == null) return;
        boolean badge = investigation.hasHeldEvidence(CaseFile.Place.LABORATORIUM);
        boolean fuse = investigation.hasHeldEvidence(CaseFile.Place.KORYTARZ);
        badgeInventoryIcon.setVisible(badge);
        fuseInventoryIcon.setVisible(fuse);
        badgePlaceholder.setVisible(!badge);
        fusePlaceholder.setVisible(!fuse);
        inventoryCount.setText("PRZEDMIOTY " + ((badge ? 1 : 0) + (fuse ? 1 : 0)) + "/2");
        inventoryPanel.setVisible(badge || fuse);
    }

    private String nextObjective() {
        if (solved) return "Sprawa rozwiązana.";
        if (!investigation.hasVisited(CaseFile.Place.LABORATORIUM))
            return "Cel: podnieś identyfikator — pokaże, jak otwarto zamek.";
        if (!investigation.hasVisited(CaseFile.Place.KORYTARZ))
            return "Cel: podnieś bezpiecznik — wyjaśni przerwę nagrania.";
        if (!investigation.hasDeductedHiddenEntry())
            return "Cel: połącz oba przedmioty na tablicy dowodów.";
        if (!investigation.hasQuestioned(CaseFile.Suspect.MAREK))
            return "Cel: skonfrontuj Marka z dowodami.";
        if (!investigation.hasVisited(CaseFile.Place.BIURO))
            return "Cel: sprawdź drukarkę w biurze.";
        if (!investigation.hasQuestioned(CaseFile.Suspect.LENA)
                || !investigation.hasQuestioned(CaseFile.Suspect.OLA))
            return "Cel: zapytaj Lenę i Olę, gdzie były o 21:12.";
        if (!investigation.hasDeductedAlibiContradiction())
            return "Cel: porównaj zeznania Leny i Oli w Dowodach.";
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
                    : "Identyfikator wyjaśnia wejście, a bezpiecznik brak nagrania. Zestaw godziny 21:12 i 21:14.";
        } else if (!investigation.hasQuestioned(CaseFile.Suspect.MAREK)) {
            hint = direct ? "Wróć do korytarza i kliknij Marka stojącego na środku."
                    : "Technik odpowiadał za kamerę. Sprawdź jego wersję wydarzeń.";
        } else if (!investigation.hasVisited(CaseFile.Place.BIURO)) {
            hint = direct ? "W korytarzu kliknij przeszklone drzwi biura po prawej, potem drukarkę po lewej."
                    : "Kto mógł skorzystać na kradzieży? Poszukaj dokumentu.";
        } else if (!investigation.hasQuestioned(CaseFile.Suspect.LENA)
                || !investigation.hasQuestioned(CaseFile.Suspect.OLA)) {
            hint = direct ? "W biurze zapytaj Lenę i Olę, gdzie były o 21:12."
                    : "Alibi potwierdza się u drugiej osoby. Sprawdź obie wersje godziny 21:12.";
        } else if (!investigation.hasDeductedAlibiContradiction()) {
            hint = direct ? "Otwórz Dowody, porównaj zeznania i wskaż sprzeczne alibi."
                    : "Czy Ola mogła być z Leną, jeśli Lena twierdzi, że była sama?";
        } else {
            hint = direct ? "Sprawdź podpis na umowie, identyfikator i alibi. Potem wskaż sprawcę w Dowodach."
                    : "Sprawca potrzebował motywu i sposobu wejścia podczas przerwy nagrania.";
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
            message.setText("Identyfikator trafił do Przedmiotów w prawym górnym rogu. "
                    + "Zamek zapisał wejście o 21:14; teraz sprawdź, dlaczego kamera tego nie nagrała.");
            updateProgress();
            pulse(badgeInventoryIcon);
        });
        raiseHud(room);
        transitionTo(room, introduction ? () -> card("AKTA 01  /  ZNIKNIĘCIE PROTOTYPU",
                "O 21:20 odkryto kradzież prototypu. W budynku zostały trzy osoby: "
                        + "Lena, Marek i Ola. Zacznij od laboratorium. Klikaj przedmioty "
                        + "oraz postacie; cel u góry podpowiada kolejny krok.",
                "Rozpocznij", this::closeOverlay) : null);
    }

    private void showCorridor() {
        Pane room = room("korytarz-v3.png", "Kamera zgasła o 21:12. Co odcięło jej zasilanie? "
                + "Biuro jest za przeszklonymi drzwiami.");

        Polygon labDoor = new Polygon(126, 162, 398, 258, 398, 742, 130, 812);
        outline(labDoor, "Powrót do laboratorium", this::showLaboratory);
        Polygon camera = new Polygon(94, 23, 248, 73, 214, 129, 79, 80);
        outline(camera, "Kamera", () -> message.setText(
                "Kamera przestała nagrywać o 21:12 — dwie minuty przed otwarciem laboratorium."));
        Polygon cabinet = new Polygon(1426, 251, 1608, 194, 1608, 810, 1426, 755);
        outline(cabinet, "Szafka serwisowa", () -> message.setText(
                investigation.hasVisited(CaseFile.Place.KORYTARZ)
                        ? "W szafce brakuje bezpiecznika. Sprawdź go w Przedmiotach lub na tablicy dowodów."
                        : "W szafce brakuje bezpiecznika. Spójrz na podłogę obok niej."));
        Polygon officeDoor = new Polygon(1100, 278, 1287, 278, 1287, 633, 1100, 633);
        outline(officeDoor, "Drzwi do biura", this::showOffice);
        scaled(room, labDoor, camera, cabinet, officeDoor);

        ImageView marek = sprite(room, "marek.png", 0.40, 0.32, 0.25);
        marek.fitWidthProperty().unbind();
        marek.fitHeightProperty().bind(room.heightProperty().multiply(0.62));
        Tooltip.install(marek, new Tooltip("Marek — technik"));
        marek.setOnMouseEntered(event -> marek.setEffect(
                new DropShadow(22, Color.web("#ffd16c"))));
        marek.setOnMouseExited(event -> marek.setEffect(null));
        marek.setOnMouseClicked(event -> showMarekDialogue());

        ImageView fuse = sprite(room, "bezpiecznik.png", 0.76, 0.76, 0.13);
        Tooltip.install(fuse, new Tooltip("Bezpiecznik"));
        fuse.setVisible(!investigation.hasVisited(CaseFile.Place.KORYTARZ));
        fuse.setOnMouseClicked(event -> {
            investigation.discoverClue(CaseFile.Place.KORYTARZ);
            fuse.setVisible(false);
            message.setText("Bezpiecznik trafił do Przedmiotów. Bez niego kamera zgasła o 21:12. "
                    + "Połącz go z identyfikatorem na tablicy: czy wejście o 21:14 zostało nagrane?");
            updateProgress();
            pulse(fuseInventoryIcon);
        });

        raiseHud(room);
        transitionTo(room);
    }

    private void showMarekDialogue() {
        Label reply = paragraph("Zapytaj Marka o kamerę lub skonfrontuj go z zebranymi śladami.");

        Button camera = button("Zapytaj o kamerę", false);
        camera.setOnAction(event -> reply.setText(
                "Marek: O 21:10 poszedłem po narzędzia. Potem nie dotykałem kamery."));

        Button evidence = button("Pokaż identyfikator i bezpiecznik", true);
        evidence.setOnAction(event -> {
            if (!investigation.hasDeductedHiddenEntry()) {
                reply.setText("Najpierw znajdź oba ślady i połącz je w Dowodach.");
                return;
            }

            investigation.question(CaseFile.Suspect.MAREK);
            reply.setText("Marek: Wyjąłem bezpiecznik do testu zasilania. "
                    + "Identyfikator zostawiłem w biurze. Ktoś musiał go zabrać.");
            camera.setDisable(true);
            evidence.setDisable(true);
            updateProgress();
        });

        Button close = button("Wróć do korytarza", false);
        close.setOnAction(event -> closeOverlay());

        dialogue("MAREK  /  TECHNIK", reply, camera, evidence, close);
    }

    private void showLenaDialogue() {
        Label reply = paragraph("Zapytaj o godzinę 21:12, aby zapisać zeznanie w Dowodach.");

        Button time = button("Gdzie byłaś o 21:12?", true);
        time.setOnAction(event -> {
            reply.setText("Lena: Byłam sama w biurze. Ola wyszła kilka minut wcześniej.");
            investigation.question(CaseFile.Suspect.LENA);
            updateProgress();
        });

        Button badge = button("Czy widziałaś identyfikator Marka?", false);
        badge.setOnAction(event -> {
            reply.setText("Lena: Widziałam identyfikator w dłoni Oli. "
                    + "Nie widziałam jednak, czy weszła do laboratorium.");
        });

        Button close = button("Zakończ rozmowę", false);
        close.setOnAction(event -> closeOverlay());

        dialogue("LENA  /  KIEROWNICZKA", reply, time, badge, close);
    }

    private void showOlaDialogue() {
        Label reply = paragraph("Zapytaj o godzinę 21:12, aby zapisać zeznanie w Dowodach.");

        Button time = button("Gdzie byłaś o 21:12?", true);
        time.setOnAction(event -> {
            reply.setText("Ola: Byłam wtedy z Leną w biurze. Może to potwierdzić.");
            investigation.question(CaseFile.Suspect.OLA);
            updateProgress();
        });

        Button badge = button("Czy miałaś identyfikator Marka?", false);
        badge.setOnAction(event -> reply.setText(
                "Ola: Nie. Nie miałam dostępu do laboratorium."));

        Button contract = button("Zapytaj o umowę z drukarki", false);
        contract.setOnAction(event -> reply.setText(
                investigation.hasVisited(CaseFile.Place.BIURO)
                        ? "Ola: To tylko propozycja sprzedaży. Podpis nie dowodzi kradzieży."
                        : "Najpierw sprawdź dokument w drukarce."));

        Button close = button("Zakończ rozmowę", false);
        close.setOnAction(event -> closeOverlay());

        dialogue("OLA  /  INWESTORKA", reply, time, badge, contract, close);
    }

    private void dialogue(String name, Label reply, Button... questions) {
        Pane layer = overlay();
        layer.setStyle("-fx-background-color: rgba(3, 10, 18, 0.18);");

        HBox ribbon = new HBox(22);
        ribbon.setAlignment(Pos.CENTER_LEFT);
        ribbon.setPadding(new Insets(18, 28, 18, 28));
        ribbon.setStyle("-fx-background-color: rgba(8, 21, 34, 0.95);"
                + "-fx-border-color: #c59650 transparent transparent transparent;"
                + "-fx-border-width: 3 0 0 0;");
        ribbon.prefWidthProperty().bind(layer.widthProperty());
        ribbon.prefHeightProperty().bind(layer.heightProperty().multiply(0.36));
        ribbon.layoutYProperty().bind(layer.heightProperty().multiply(0.64));

        Label heading = text(name, 23, "#ffe0a0");
        reply.setStyle("-fx-text-fill: #f0e8d8; -fx-font-size: 19px;"
                + "-fx-font-family: Georgia;");
        reply.setWrapText(true);
        reply.textProperty().addListener((observable, oldText, newText) -> reveal(reply));
        VBox testimony = new VBox(16, heading, reply);
        testimony.setAlignment(Pos.CENTER_LEFT);
        testimony.prefWidthProperty().bind(layer.widthProperty().multiply(0.55));

        VBox choices = new VBox(7);
        choices.setAlignment(Pos.CENTER_LEFT);
        choices.prefWidthProperty().bind(layer.widthProperty().multiply(0.33));
        for (Button question : questions) {
            question.setStyle(question.getStyle() + "-fx-font-size: 14px;"
                    + "-fx-padding: 7 12;");
            question.setMaxWidth(Double.MAX_VALUE);
            choices.getChildren().add(question);
        }
        ribbon.getChildren().addAll(testimony, choices);
        layer.getChildren().add(ribbon);
        reveal(ribbon);
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
        outline(lena, "Lena — kierowniczka", this::showLenaDialogue);
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
        outline(ola, "Ola — inwestorka", this::showOlaDialogue);
        scaled(room, door, printer, lena, ola);
        raiseHud(room);
        transitionTo(room);
    }

    private void showBoard() {
        Pane board = illustratedBoard();
        Group content = new Group();
        Label heading = text("AKTA 01  /  TABLICA DOWODÓW", 36, "#ffe0a0");
        heading.setLayoutX(490);
        heading.setLayoutY(38);
        content.getChildren().add(heading);

        content.getChildren().addAll(
                boardText("LABORATORIUM", 27, 318, 215, 310),
                boardText("KORYTARZ", 27, 714, 215, 310),
                boardText("BIURO", 27, 1107, 215, 300));
        content.getChildren().add(boardText(
                investigation.hasVisited(CaseFile.Place.LABORATORIUM)
                        ? "Identyfikator Marka leżał na podłodze. Zamek zapisał wejście o 21:14."
                        : "Nie zbadano podłogi laboratorium.",
                23, 318, 295, 305));
        content.getChildren().add(boardText(
                investigation.hasVisited(CaseFile.Place.KORYTARZ)
                        ? "Kamera zgasła o 21:12. Wyjęty bezpiecznik leżał przy szafce."
                        : "Nie znaleziono przyczyny awarii kamery.",
                23, 714, 295, 305));
        content.getChildren().add(boardText(
                investigation.hasVisited(CaseFile.Place.BIURO)
                        ? "W drukarce znaleziono umowę sprzedaży z podpisem Oli."
                        : "Nie zbadano dokumentów w biurze.",
                23, 1107, 295, 300));
        if (investigation.hasQuestioned(CaseFile.Suspect.LENA)) {
            content.getChildren().add(boardText("Lena: o 21:12 byłam sama.", 19, 1107, 420, 300));
        }
        if (investigation.hasQuestioned(CaseFile.Suspect.OLA)) {
            content.getChildren().add(boardText("Ola: o 21:12 byłam z Leną.", 19, 1107, 475, 300));
        }
        Label feedback = boardText("Ślady " + investigation.clueCount() + "/3  •  Przesłuchania "
                        + investigation.questionedCount() + "/3\n"
                        + "Wejście: " + (investigation.hasDeductedHiddenEntry() ? "ustalone" : "nieustalone")
                        + "  •  Alibi: " + (investigation.hasDeductedAlibiContradiction()
                        ? "sprzeczne" : "nieporównane") + "\n"
                        + (investigation.hasDeductedHiddenEntry()
                        ? "Identyfikator i bezpiecznik przypięto: kamera zgasła przed wejściem."
                        : investigation.hasHeldEvidence(CaseFile.Place.LABORATORIUM)
                        && investigation.hasHeldEvidence(CaseFile.Place.KORYTARZ)
                        ? "Masz oba przedmioty. Połącz je, aby wyjaśnić wejście bez nagrania."
                        : "Zbierz oba przedmioty, aby wyjaśnić wejście bez nagrania."),
                23, 326, 676, 1030);
        content.getChildren().add(feedback);

        Button combine = boardAction(investigation.hasDeductedHiddenEntry()
                ? "Ślady połączone" : "Połącz ślady", true, 302, 813, 265);
        combine.setDisable(investigation.hasDeductedHiddenEntry());
        combine.setOnAction(event -> {
            if (investigation.deduceHiddenEntry()) {
                updateProgress();
                message.setText("Identyfikator i bezpiecznik są teraz na tablicy dowodów. "
                        + "Kamera zgasła przed wejściem o 21:14. Skonfrontuj Marka z tą luką.");
                showBoard();
            } else {
                feedback.setText("Podnieś identyfikator z laboratorium i bezpiecznik z korytarza. "
                        + "Dopiero razem wyjaśniają wejście bez nagrania.");
                shake(feedback);
            }
        });
        Button compare = boardAction(investigation.hasDeductedAlibiContradiction()
                ? "Alibi porównane" : "Porównaj alibi", false, 590, 813, 270);
        compare.setDisable(investigation.hasDeductedAlibiContradiction());
        compare.setOnAction(event -> {
            if (investigation.hasQuestioned(CaseFile.Suspect.LENA)
                    && investigation.hasQuestioned(CaseFile.Suspect.OLA)) {
                showAlibiDeduction();
            } else {
                feedback.setText("Najpierw zapytaj Lenę i Olę, gdzie były o 21:12.");
                shake(feedback);
            }
        });
        Button accuse = boardAction("Ułóż teorię", true, 885, 813, 220);
        accuse.setOnAction(event -> {
            if (investigation.canAccuse()) {
                showAccusation();
            } else {
                feedback.setText(nextObjective());
                shake(feedback);
            }
        });
        Button close = boardAction("Wróć do sceny", false, 1130, 813, 235);
        close.setOnAction(event -> closeOverlay());
        content.getChildren().addAll(combine, compare, accuse, close);
        scaledUi(board, content);
    }

    private void showAlibiDeduction() {
        Pane board = illustratedBoard();
        Group content = new Group();
        Label heading = text("AKTA 01  /  PORÓWNAJ ALIBI", 36, "#ffe0a0");
        heading.setLayoutX(530);
        heading.setLayoutY(38);
        content.getChildren().addAll(heading,
                boardText("LENA", 28, 318, 215, 310),
                boardText("O 21:12 byłam sama w biurze. Ola wyszła kilka minut wcześniej.",
                        23, 318, 315, 305),
                boardText("OLA", 28, 714, 215, 310),
                boardText("O 21:12 byłam razem z Leną w biurze. Może to potwierdzić.",
                        23, 714, 315, 305),
                boardText("TWÓJ WNIOSEK", 28, 1107, 215, 300));

        Label feedback = boardText("Czy obie wypowiedzi mogą być jednocześnie prawdziwe?",
                23, 326, 680, 1030);
        Button contradiction = boardAction("Zeznania się wykluczają", true, 1094, 330, 300);
        Button uncertain = boardAction("Obie wersje są spójne", false, 1094, 440, 300);
        contradiction.setOnAction(event -> {
            if (investigation.deduceAlibiContradiction()) {
                updateProgress();
                feedback.setText("Zapisano dedukcję: alibi Oli przeczy zeznaniu Leny.");
                reveal(feedback);
                contradiction.setDisable(true);
                uncertain.setDisable(true);
            }
        });
        uncertain.setOnAction(event -> {
            feedback.setText("Jeśli Lena była sama, Ola nie mogła być wtedy z nią.");
            shake(feedback);
        });
        Button back = boardAction("Wróć do dowodów", false, 1090, 813, 275);
        back.setOnAction(event -> showBoard());
        content.getChildren().addAll(contradiction, uncertain, feedback, back);
        scaledUi(board, content);
    }

    private void showAccusation() {
        if (!investigation.canAccuse()) {
            showBoard();
            return;
        }
        Pane board = illustratedBoard();
        Group content = new Group();
        Label heading = text("AKTA 01  /  REKONSTRUKCJA", 36, "#ffe0a0");
        heading.setLayoutX(510);
        heading.setLayoutY(38);
        content.getChildren().addAll(heading,
                boardText("KTO?", 29, 318, 215, 310),
                boardText("JAK?", 29, 714, 215, 310),
                boardText("DLACZEGO?", 29, 1107, 215, 300));

        ToggleGroup people = new ToggleGroup();
        ToggleGroup methods = new ToggleGroup();
        ToggleGroup motives = new ToggleGroup();
        CaseFile.EntryMethod[] methodOrder = {CaseFile.EntryMethod.FORCED_DOOR,
                CaseFile.EntryMethod.BADGE, CaseFile.EntryMethod.TECHNICIAN};
        CaseFile.Motive[] motiveOrder = {CaseFile.Motive.REVENGE,
                CaseFile.Motive.SALE, CaseFile.Motive.ACCIDENT};
        for (int i = 0; i < 3; i++) {
            CaseFile.Suspect suspect = CaseFile.Suspect.values()[i];
            content.getChildren().add(theoryChoice(suspect.name + " — " + suspect.role,
                    people, suspect, 310, 300 + i * 91, 320));
            CaseFile.EntryMethod method = methodOrder[i];
            content.getChildren().add(theoryChoice(method.description,
                    methods, method, 700, 300 + i * 91, 320));
            CaseFile.Motive motive = motiveOrder[i];
            content.getChildren().add(theoryChoice(motive.description,
                    motives, motive, 1095, 300 + i * 91, 320));
        }

        Label feedback = boardText("Wybierz osobę, sposób wejścia i motyw. Potem sprawdź teorię.",
                23, 326, 680, 1030);
        Button check = boardAction("Sprawdź teorię", true, 1040, 813, 300);
        check.setOnAction(event -> {
            CaseFile.Suspect suspect = people.getSelectedToggle() == null ? null
                    : (CaseFile.Suspect) ((ToggleButton) people.getSelectedToggle()).getUserData();
            CaseFile.EntryMethod method = methods.getSelectedToggle() == null ? null
                    : (CaseFile.EntryMethod) ((ToggleButton) methods.getSelectedToggle()).getUserData();
            CaseFile.Motive motive = motives.getSelectedToggle() == null ? null
                    : (CaseFile.Motive) ((ToggleButton) motives.getSelectedToggle()).getUserData();
            Investigation.TheoryResult result = investigation.evaluateTheory(suspect, method, motive);
            if (result.correct()) {
                solved = true;
                updateProgress();
                showResolution();
            } else {
                feedback.setText(result.feedback());
                shake(feedback);
            }
        });
        Button back = boardAction("Wróć do dowodów", false, 310, 813, 275);
        back.setOnAction(event -> showBoard());
        content.getChildren().addAll(feedback, check, back);
        scaledUi(board, content);
    }

    private void showResolution() {
        Pane board = illustratedBoard();
        Group content = new Group();
        Label heading = text("SPRAWA ROZWIĄZANA", 40, "#ffe0a0");
        heading.setLayoutX(555);
        heading.setLayoutY(34);
        content.getChildren().addAll(heading,
                boardText("SPRAWCA", 29, 318, 215, 310),
                boardText("Ola. Jej alibi o 21:12 przeczy zeznaniu Leny.", 23, 318, 310, 305),
                boardText("SPOSÓB", 29, 714, 215, 310),
                boardText("Podczas awarii kamery użyła identyfikatora Marka. Zamek otwarto o 21:14.",
                        23, 714, 310, 305),
                boardText("MOTYW", 29, 1107, 215, 300),
                boardText("Umowa sprzedaży prototypu konkurencji nosi jej podpis.",
                        23, 1107, 310, 300),
                boardText("21:12 — kamera gaśnie. 21:14 — wejście. 21:20 — odkrycie kradzieży.",
                        23, 326, 680, 1030));
        Button menu = boardAction("Wróć do menu akt", true, 1030, 813, 320);
        menu.setOnAction(event -> showMenu());
        content.getChildren().add(menu);
        scaledUi(board, content);
    }

    private Pane illustratedBoard() {
        Pane board = overlay();
        ImageView art = background(board, "tablica-dowodow.png");
        art.setMouseTransparent(true);
        board.setOpacity(0);
        FadeTransition appear = new FadeTransition(Duration.millis(260), board);
        appear.setFromValue(0);
        appear.setToValue(1);
        appear.play();
        return board;
    }

    private void scaledUi(Pane board, Group content) {
        Scale scale = new Scale();
        scale.xProperty().bind(board.widthProperty().divide(1672));
        scale.yProperty().bind(board.heightProperty().divide(941));
        content.getTransforms().add(scale);
        board.getChildren().add(content);
    }

    private Label boardText(String value, int size, double x, double y, double width) {
        Label label = text(value, size, "#352719");
        label.setStyle(label.getStyle() + "-fx-font-family: Georgia;");
        label.setWrapText(true);
        label.setPrefWidth(width);
        label.setLayoutX(x);
        label.setLayoutY(y);
        return label;
    }

    private Button boardAction(String title, boolean important,
                               double x, double y, double width) {
        Button action = button(title, important);
        action.setStyle(action.getStyle() + "-fx-font-size: 22px;");
        action.setWrapText(true);
        action.setPrefSize(width, 74);
        action.setLayoutX(x);
        action.setLayoutY(y);
        action.setOnMouseEntered(event -> action.setEffect(
                new DropShadow(18, Color.web("#ffd16c"))));
        action.setOnMouseExited(event -> action.setEffect(null));
        return action;
    }

    private ToggleButton theoryChoice(String title, ToggleGroup group, Object value,
                                      double x, double y, double width) {
        ToggleButton choice = new ToggleButton(title);
        choice.setToggleGroup(group);
        choice.setUserData(value);
        choice.setCursor(actionCursor);
        choice.setWrapText(true);
        choice.setAlignment(Pos.CENTER_LEFT);
        choice.setPrefSize(width, 72);
        choice.setLayoutX(x);
        choice.setLayoutY(y);
        choice.setStyle(THEORY_OPTION);
        choice.selectedProperty().addListener((observable, previous, selected) -> {
            choice.setStyle(selected ? THEORY_SELECTED : THEORY_OPTION);
            if (selected) pulse(choice);
        });
        return choice;
    }

    private void pulse(Node node) {
        ScaleTransition animation = new ScaleTransition(Duration.millis(135), node);
        animation.setFromX(1);
        animation.setFromY(1);
        animation.setToX(1.04);
        animation.setToY(1.04);
        animation.setCycleCount(2);
        animation.setAutoReverse(true);
        animation.play();
    }

    private void reveal(Node node) {
        FadeTransition animation = new FadeTransition(Duration.millis(300), node);
        animation.setFromValue(0);
        animation.setToValue(1);
        animation.play();
    }

    private void shake(Node node) {
        TranslateTransition animation = new TranslateTransition(Duration.millis(60), node);
        animation.setFromX(0);
        animation.setToX(9);
        animation.setCycleCount(6);
        animation.setAutoReverse(true);
        animation.play();
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
