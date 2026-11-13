package pl.detektyw;

import java.io.IOException;
import java.util.Optional;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
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
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.util.Duration;

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
            + "-fx-font-family: Georgia; -fx-font-size: 19px; -fx-padding: 6 10;";
    private static final String THEORY_SELECTED = "-fx-background-color: rgba(231, 170, 72, 0.72);"
            + "-fx-border-color: #7c4d1d; -fx-border-width: 2; -fx-border-radius: 8;"
            + "-fx-background-radius: 8; -fx-text-fill: #21170e;"
            + "-fx-font-family: Georgia; -fx-font-size: 19px; -fx-font-weight: bold; -fx-padding: 6 10;";

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
    private CaseSave.Room currentLocation = CaseSave.Room.LABORATORIUM;
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
        Optional<CaseSave.Progress> saved = CaseSave.load();

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

        Runnable firstAction = saved.isPresent() ? () -> continueCase(saved.get()) : this::startCase;
        Button first = button(saved.isPresent()
                ? saved.get().solved() ? "AKTA 01  •  Zobacz finał" : "AKTA 01  •  Kontynuuj"
                : "AKTA 01  •  Zniknięcie prototypu", true);
        first.layoutXProperty().bind(menu.widthProperty().multiply(0.33)
                .subtract(first.widthProperty().divide(2)));
        first.layoutYProperty().bind(menu.heightProperty().multiply(0.67)
                .subtract(first.heightProperty().divide(2)));
        first.setOnAction(event -> firstAction.run());
        menuFolder(firstFolder, first, firstAction);

        Button restart = null;
        if (saved.isPresent()) {
            restart = button("Nowa gra", false);
            restart.setStyle(restart.getStyle() + "-fx-font-size: 13px; -fx-padding: 6 12;");
            restart.layoutXProperty().bind(menu.widthProperty().multiply(0.33)
                    .subtract(restart.widthProperty().divide(2)));
            restart.layoutYProperty().bind(menu.heightProperty().multiply(0.77));
            restart.setOnAction(event -> startCase());
        }

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
        if (restart != null) menu.getChildren().add(restart);
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

    private void continueCase(CaseSave.Progress saved) {
        investigation = saved.investigation();
        solved = saved.solved();
        hintLevel = 0;
        hintedObjective = "";
        switch (saved.room()) {
            case LABORATORIUM -> showLaboratory();
            case KORYTARZ -> showCorridor();
            case BIURO -> showOffice();
            case MAGAZYN -> showStorage();
        }
        if (solved) showResolution();
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
        saveProgress();
    }

    private void saveProgress() {
        try {
            CaseSave.save(new CaseSave.Progress(investigation, currentLocation, solved));
        } catch (IOException error) {
            if (message != null) message.setText("Nie udało się zapisać postępu gry.");
        }
    }

    private VBox inventory(Pane room) {
        inventoryCount = text("PRZEDMIOTY 0/2", 12, "#ffe0a0");
        badgePlaceholder = text("?", 28, "#91a3aa");
        fusePlaceholder = text("?", 28, "#91a3aa");
        badgeInventoryIcon = inventoryImage("identyfikator.png");
        fuseInventoryIcon = inventoryImage("bezpiecznik.png");
        StackPane badgeSlot = inventorySlot(badgePlaceholder, badgeInventoryIcon,
                "Identyfikator Marka — sprawdź czytnik przy drzwiach");
        StackPane fuseSlot = inventorySlot(fusePlaceholder, fuseInventoryIcon,
                "Bezpiecznik — sprawdź archiwum kamery");
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
            return "Cel: podnieś identyfikator leżący w laboratorium.";
        if (!investigation.hasReadLockLog())
            return "Cel: odczytaj dziennik zamka przy drzwiach laboratorium.";
        if (!investigation.hasInspectedCabinet())
            return "Cel: otwórz szafkę serwisową po prawej w korytarzu.";
        if (!investigation.hasVisited(CaseFile.Place.KORYTARZ))
            return "Cel: znajdź bezpiecznik na podłodze obok szafki.";
        if (!investigation.hasInstalledFuse())
            return "Cel: włóż bezpiecznik do pustego gniazda w szafce.";
        if (!investigation.hasReadCameraLog())
            return "Cel: sprawdź zapis awarii po uruchomieniu kamery.";
        if (!investigation.hasDeductedHiddenEntry())
            return "Cel: zestaw odczyty zamka i kamery na tablicy dowodów.";
        if (!investigation.hasQuestioned(CaseFile.Suspect.MAREK))
            return "Cel: skonfrontuj Marka z dowodami.";
        if (!investigation.hasVisited(CaseFile.Place.BIURO))
            return "Cel: sprawdź drukarkę w biurze.";
        if (!investigation.hasQuestioned(CaseFile.Suspect.LENA)
                || !investigation.hasQuestioned(CaseFile.Suspect.OLA))
            return "Cel: zapytaj Lenę i Olę, gdzie były o 21:12.";
        if (!investigation.hasDeductedAlibiContradiction())
            return "Cel: porównaj zeznania Leny i Oli w Dowodach.";
        if (!investigation.hasRecoveredPrototype())
            return "Cel: odnajdź prototyp P-17 w magazynie przy biurze.";
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
        } else if (!investigation.hasReadLockLog()) {
            hint = direct ? "Kliknij czytnik kart obok drzwi laboratorium, potem odczytaj dziennik."
                    : "Zamek mógł zapisać kartę używaną przy wejściu.";
        } else if (!investigation.hasInspectedCabinet()) {
            hint = direct ? "Przejdź do korytarza i kliknij dużą szafkę po prawej."
                    : "Sprawdź, skąd kamera bierze zasilanie.";
        } else if (!investigation.hasVisited(CaseFile.Place.KORYTARZ)) {
            hint = direct ? "W korytarzu kliknij bezpiecznik na podłodze przy szafce."
                    : "Puste gniazdo ma odpowiednik na podłodze obok szafki.";
        } else if (!investigation.hasInstalledFuse()) {
            hint = direct ? "Otwórz szafkę w korytarzu i kliknij „Włóż bezpiecznik”."
                    : "Znaleziony bezpiecznik pasuje do pustego gniazda. Bez prądu archiwum jest niedostępne.";
        } else if (!investigation.hasReadCameraLog()) {
            hint = direct ? "Wróć do szafki w korytarzu i kliknij „Sprawdź zapis awarii”."
                    : "Kamera już działa. Teraz odczytaj, od której chwili nie było nagrania.";
        } else if (!investigation.hasDeductedHiddenEntry()) {
            hint = direct ? "Otwórz Dowody, kliknij „Połącz ślady” i wybierz wniosek, który nie zgaduje osoby."
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
        } else if (!investigation.hasRecoveredPrototype()) {
            hint = direct ? "W biurze kliknij znak MAGAZYN po prawej. Dopasuj P-17 z umowy do schowka."
                    : "Porównaj numer prototypu z umowy z oznaczeniami schowków.";
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
        currentLocation = CaseSave.Room.LABORATORIUM;
        Pane room = room("laboratorium-v2.png", "Laboratorium jest puste. Zbadaj drzwi i podłogę.");

        Rectangle terminal = new Rectangle(818, 353, 190, 120);
        terminal.setArcWidth(14);
        terminal.setArcHeight(14);
        outline(terminal, "Terminal", () -> message.setText(
                "Terminal nie działa. Sprawdź zapis elektronicznego zamka przy drzwiach."));

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
        Rectangle reader = new Rectangle(365, 362, 56, 105);
        reader.setArcWidth(8);
        reader.setArcHeight(8);
        outline(reader, "Czytnik kart — dziennik zamka", this::showLockLog);
        scaled(room, door, terminal, cabinet, reader);

        ImageView badge = sprite(room, "identyfikator.png", 0.35, 0.76, 0.10);
        Tooltip.install(badge, new Tooltip("Identyfikator Marka"));
        badge.setVisible(!investigation.hasVisited(CaseFile.Place.LABORATORIUM));
        badge.setOnMouseClicked(event -> {
            investigation.discoverClue(CaseFile.Place.LABORATORIUM);
            badge.setVisible(false);
            message.setText("Identyfikator trafił do Przedmiotów. Sprawdź czytnik przy drzwiach: "
                    + "czy zamek zapisał użycie tej karty?");
            updateProgress();
            pulse(badgeInventoryIcon);
            PauseTransition prompt = new PauseTransition(Duration.millis(550));
            prompt.setOnFinished(done -> {
                if (stage.getScene().getRoot() == room) showLockLog();
            });
            prompt.play();
        });
        raiseHud(room);
        saveProgress();
        transitionTo(room, introduction ? () -> card("AKTA 01  /  ZNIKNIĘCIE PROTOTYPU",
                "O 21:20 odkryto kradzież prototypu. W budynku zostały trzy osoby: "
                        + "Lena, Marek i Ola. Zacznij od laboratorium. Klikaj przedmioty "
                        + "oraz postacie; cel u góry podpowiada kolejny krok.",
                "Rozpocznij", this::closeOverlay) : null);
    }

    private void showCorridor() {
        currentLocation = CaseSave.Room.KORYTARZ;
        Pane room = room("korytarz-v3.png", investigation.hasInstalledFuse()
                ? (investigation.hasReadCameraLog()
                ? "Kamera działa po naprawie. Starej luki w nagraniu to nie wypełni. "
                : "Kamera działa po naprawie. Teraz możesz odczytać archiwum. ")
                + "Biuro jest za przeszklonymi drzwiami."
                : "Kamera nie nagrywa. Co odcięło jej zasilanie? "
                + "Biuro jest za przeszklonymi drzwiami.");

        Polygon labDoor = new Polygon(126, 162, 398, 258, 398, 742, 130, 812);
        outline(labDoor, "Powrót do laboratorium", this::showLaboratory);
        Polygon camera = new Polygon(94, 23, 248, 73, 214, 129, 79, 80);
        outline(camera, "Kamera — zapis nagrania", this::showCameraLog);
        Polygon cabinet = new Polygon(1426, 251, 1608, 194, 1608, 810, 1426, 755);
        outline(cabinet, "Szafka serwisowa — puste gniazdo", this::showCameraLog);
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
            message.setText("Bezpiecznik trafił do Przedmiotów. Puste gniazdo jest w szafce serwisowej. "
                    + "Sprawdź ekran kamery, aby ustalić czas przerwy nagrania.");
            updateProgress();
            pulse(fuseInventoryIcon);
            PauseTransition prompt = new PauseTransition(Duration.millis(550));
            prompt.setOnFinished(done -> {
                if (stage.getScene().getRoot() == room) showCameraLog();
            });
            prompt.play();
        });

        raiseHud(room);
        saveProgress();
        transitionTo(room);
    }

    private void showLockLog() {
        Pane inspection = new Pane();
        background(inspection, "zapis-zamka.png");
        Group content = new Group();
        Label heading = inspectionNote("LABORATORIUM  /  CZYTNIK KART", 50, 48, 620);
        Label display = text("", 27, "#09363b");
        display.setStyle(display.getStyle() + "-fx-font-family: Consolas; -fx-font-weight: bold;");
        display.setWrapText(true);
        display.setPrefWidth(385);
        display.setLayoutX(1002);
        display.setLayoutY(215);
        Label note = inspectionNote("Czytnik zapisuje użycie karty, ale nie rozpoznaje twarzy.",
                50, 750, 900);
        Button read = inspectionButton("Odczytaj dziennik", 1180, 805, 345, true);
        Runnable showEntries = () -> display.setText("DZIENNIK ZAMKA\n\n21:11  ZAMKNIĘTE\n\n21:14  KARTA M-04\n         OTWARTO");
        if (investigation.hasReadLockLog()) {
            showEntries.run();
            read.setDisable(true);
        } else {
            display.setText("DZIENNIK ZAMKA\n\nDotknij przycisku,\naby odczytać pamięć.");
        }
        read.setOnAction(event -> {
            investigation.readLockLog();
            showEntries.run();
            note.setText("21:14: użyto karty Marka. To nie dowodzi, że Marek wszedł osobiście.");
            read.setDisable(true);
            updateProgress();
            reveal(display);
        });
        Button back = inspectionButton("Wróć do laboratorium", 70, 805, 350, false);
        back.setOnAction(event -> showLaboratory());
        content.getChildren().addAll(heading, display, note, read, back);
        scaledUi(inspection, content);
        transitionTo(inspection);
    }

    private void showCameraLog() {
        investigation.inspectCabinet();
        updateProgress();
        Pane inspection = new Pane();
        background(inspection, "awaria-kamery.png");
        Group content = new Group();
        Label heading = inspectionNote("KORYTARZ  /  ZASILANIE KAMERY", 50, 42, 700);
        Rectangle poweredScreen = new Rectangle(266, 281, 674, 414);
        poweredScreen.setArcWidth(16);
        poweredScreen.setArcHeight(16);
        poweredScreen.setFill(Color.web("#168a8d"));
        poweredScreen.setOpacity(investigation.hasInstalledFuse() ? 0.42 : 0);
        Circle poweredLight = new Circle(793, 726, 9, Color.web("#65ffc1"));
        poweredLight.setEffect(new DropShadow(22, Color.web("#65ffc1")));
        poweredLight.setOpacity(investigation.hasInstalledFuse() ? 1 : 0);
        Group fuseInSocket = installedFuseArt();
        fuseInSocket.setOpacity(investigation.hasInstalledFuse() ? 1 : 0);
        Label display = text("", 29, "#d4e9ef");
        display.setStyle(display.getStyle() + "-fx-font-family: Consolas; -fx-font-weight: bold;");
        display.setWrapText(true);
        display.setPrefWidth(635);
        display.setLayoutX(290);
        display.setLayoutY(318);
        Label note = inspectionNote(investigation.hasInstalledFuse()
                ? investigation.hasReadCameraLog()
                ? "Kamera działa teraz. Naprawa nie przywróci nagrania z 21:14."
                : "Kamera działa teraz. Odczytaj archiwum, by ustalić czas przerwy."
                : investigation.hasHeldEvidence(CaseFile.Place.KORYTARZ)
                ? "Masz bezpiecznik. Włóż go do pustego gniazda, by uruchomić ekran."
                : "W szafce jest puste gniazdo. Znajdź bezpiecznik na podłodze korytarza.",
                55, 750, 1070);
        Button read = inspectionButton(investigation.hasInstalledFuse()
                ? "Sprawdź zapis awarii" : "Archiwum bez zasilania", 455, 805, 425, false);
        read.setDisable(!investigation.hasInstalledFuse() || investigation.hasReadCameraLog());
        Button install = inspectionButton(investigation.hasInstalledFuse()
                ? "Kamera naprawiona" : "Włóż bezpiecznik do szafki", 1085, 805, 470, true);
        install.setDisable(investigation.hasInstalledFuse()
                || !investigation.hasHeldEvidence(CaseFile.Place.KORYTARZ));
        Runnable showEntries = () -> display.setText("ARCHIWUM KAMERY\n\n21:11  OBRAZ JEST\n\n21:12  ZASILANIE UTRACONE\n\n21:14  BRAK NAGRANIA"
                + (investigation.hasInstalledFuse() ? "\n\nTERAZ  NAGRYWANIE WZNOWIONE" : ""));
        if (investigation.hasReadCameraLog()) {
            showEntries.run();
        } else {
            display.setText(investigation.hasInstalledFuse()
                    ? "KAMERA AKTYWNA\n\nOBRAZ NA ŻYWO\n\nOdczytaj archiwum."
                    : "BRAK ZASILANIA\n\nArchiwum jest\nniedostępne.");
        }
        read.setOnAction(event -> {
            if (!investigation.readCameraLog()) return;
            showEntries.run();
            note.setText("O 21:12 zasilanie znikło. O 21:14 kamera niczego nie nagrała.");
            read.setDisable(true);
            updateProgress();
            reveal(display);
        });
        Button back = inspectionButton("Wróć do korytarza", 70, 805, 350, false);
        back.setOnAction(event -> showCorridor());
        install.setOnAction(event -> {
            if (!investigation.installFuse()) return;
            install.setDisable(true);
            back.setDisable(true);
            display.setText("URUCHAMIANIE KAMERY...");
            fuseInSocket.setTranslateY(160);
            TranslateTransition slide = new TranslateTransition(Duration.millis(650), fuseInSocket);
            slide.setFromY(160);
            slide.setToY(0);
            FadeTransition appear = new FadeTransition(Duration.millis(650), fuseInSocket);
            appear.setFromValue(0);
            appear.setToValue(1);
            ParallelTransition insert = new ParallelTransition(slide, appear);
            insert.setOnFinished(done -> {
                FadeTransition screenGlow = new FadeTransition(Duration.millis(550), poweredScreen);
                screenGlow.setFromValue(0);
                screenGlow.setToValue(0.42);
                FadeTransition indicatorGlow = new FadeTransition(Duration.millis(550), poweredLight);
                indicatorGlow.setFromValue(0);
                indicatorGlow.setToValue(1);
                ParallelTransition boot = new ParallelTransition(screenGlow, indicatorGlow);
                boot.setOnFinished(powered -> {
                    display.setText("KAMERA AKTYWNA\n\nTERAZ  OBRAZ JEST\n\nOdczytaj archiwum.");
                    note.setText("Kamera działa. Archiwum jest dostępne — sprawdź czas przerwy.");
                    install.setText("Kamera naprawiona");
                    read.setText("Sprawdź zapis awarii");
                    read.setDisable(false);
                    back.setDisable(false);
                    updateProgress();
                    reveal(display);
                });
                boot.play();
            });
            insert.play();
        });
        content.getChildren().addAll(poweredScreen, poweredLight, fuseInSocket,
                heading, display, note, read, install, back);
        scaledUi(inspection, content);
        transitionTo(inspection);
    }

    private Group installedFuseArt() {
        Rectangle glass = new Rectangle(1215, 544, 50, 153);
        glass.setArcWidth(12);
        glass.setArcHeight(12);
        glass.setFill(Color.web("#f7e2a7"));
        glass.setStroke(Color.web("#a96824"));
        glass.setStrokeWidth(3);
        Rectangle band = new Rectangle(1215, 602, 50, 16);
        band.setFill(Color.web("#aa4c35"));
        Rectangle top = new Rectangle(1210, 530, 60, 28);
        Rectangle bottom = new Rectangle(1210, 683, 60, 28);
        top.setArcWidth(10);
        top.setArcHeight(10);
        bottom.setArcWidth(10);
        bottom.setArcHeight(10);
        top.setFill(Color.web("#d99742"));
        bottom.setFill(Color.web("#d99742"));
        Group fuse = new Group(glass, band, top, bottom);
        fuse.setEffect(new DropShadow(12, Color.web("#ffd16c")));
        return fuse;
    }

    private void showContract() {
        Pane inspection = new Pane();
        background(inspection, "umowa.png");
        Group content = new Group();
        Label heading = inspectionNote("BIURO  /  WYDRUK Z DRUKARKI", 50, 48, 625);
        Label document = text("UMOWA SPRZEDAŻY\n\nPrzedmiot: prototyp\nodbiornika P-17\n\nNabywca: konkurencja\n\nOdbiór: magazyn\no 22:00\n\nPodpis: Ola", 26, "#3c2b1b");
        document.setStyle(document.getStyle() + "-fx-font-family: Georgia; -fx-font-weight: bold;");
        document.setWrapText(true);
        document.setPrefWidth(500);
        document.setLayoutX(650);
        document.setLayoutY(380);
        Label note = inspectionNote("Odbiór o 22:00. W magazynie dopasuj oznaczenie P-17.",
                50, 765, 950);
        Button collect = inspectionButton("Dodaj dokument do dowodów", 1125, 805, 430, true);
        collect.setDisable(investigation.hasVisited(CaseFile.Place.BIURO));
        collect.setOnAction(event -> {
            investigation.discoverClue(CaseFile.Place.BIURO);
            note.setText("Umowa trafiła do Dowodów. Sprawdź alibi i magazyn.");
            collect.setDisable(true);
            updateProgress();
            pulse(document);
        });
        Button back = inspectionButton("Wróć do biura", 70, 805, 315, false);
        back.setOnAction(event -> showOffice());
        content.getChildren().addAll(heading, document, note, collect, back);
        scaledUi(inspection, content);
        transitionTo(inspection);
    }

    private Label inspectionNote(String value, double x, double y, double width) {
        Label label = text(value, 25, "#fff0cf");
        label.setWrapText(true);
        label.setPrefWidth(width);
        label.setPadding(new Insets(9, 15, 9, 15));
        label.setStyle(label.getStyle() + "-fx-background-color: rgba(7, 20, 32, 0.88);"
                + "-fx-background-radius: 9;");
        label.setLayoutX(x);
        label.setLayoutY(y);
        return label;
    }

    private Button inspectionButton(String value, double x, double y, double width, boolean important) {
        Button action = button(value, important);
        action.setPrefWidth(width);
        action.setLayoutX(x);
        action.setLayoutY(y);
        return action;
    }

    private void showMarekDialogue() {
        Label reply = paragraph("Zapytaj Marka o kamerę lub skonfrontuj go z zebranymi śladami.");

        Button camera = button("Zapytaj o kamerę", false);
        camera.setOnAction(event -> reply.setText(
                "Marek: Testowałem zasilanie kamery. Wyszedłem po narzędzia, a przed końcem naprawy ogłoszono kradzież."));

        Button evidence = button("Skonfrontuj z osią czasu", true);
        evidence.setOnAction(event -> {
            if (!investigation.hasDeductedHiddenEntry()) {
                reply.setText("Najpierw znajdź oba ślady i połącz je w Dowodach.");
                return;
            }

            investigation.question(CaseFile.Suspect.MAREK);
            reply.setText("Marek: O 21:12 wyjąłem bezpiecznik do testu. Upadł przy szafce. "
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
            reply.setText("Lena: Byłam sama w biurze. Kilka minut wcześniej Ola wyszła "
                    + "z identyfikatorem Marka w dłoni.");
            investigation.question(CaseFile.Suspect.LENA);
            updateProgress();
        });

        Button badge = button("Czy widziałaś identyfikator Marka?", false);
        badge.setOnAction(event -> {
            reply.setText("Lena: Widziałam identyfikator Marka w dłoni Oli, "
                    + "ale nie widziałam jej przy drzwiach laboratorium.");
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
        currentLocation = CaseSave.Room.BIURO;
        Pane room = room("biuro.png", "W biurze są Lena, Ola i dokument pozostawiony w drukarce.");

        Polygon door = new Polygon(0, 89, 232, 95, 230, 730, 0, 751);
        outline(door, "Powrót do korytarza", this::showCorridor);
        Polygon printer = new Polygon(268, 385, 420, 381, 434, 516, 258, 519);
        outline(printer, "Drukarka — dokument", this::showContract);
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

        Button storage = button("MAGAZYN  →", false);
        storage.setStyle("-fx-background-color: rgba(9, 24, 35, 0.90);"
                + "-fx-border-color: #d6a65d; -fx-border-radius: 9;"
                + "-fx-background-radius: 9; -fx-text-fill: #ffe0a0;"
                + "-fx-font-size: 13px; -fx-padding: 8 10;");
        storage.layoutXProperty().bind(room.widthProperty().multiply(0.88));
        storage.layoutYProperty().bind(room.heightProperty().multiply(0.49));
        storage.setOnAction(event -> showStorage());
        room.getChildren().add(storage);
        raiseHud(room);
        saveProgress();
        transitionTo(room);
    }

    private void showStorage() {
        currentLocation = CaseSave.Room.MAGAZYN;
        Pane room = room("magazyn.png", investigation.hasRecoveredPrototype()
                ? "Prototyp P-17 jest zabezpieczony. Dokończ śledztwo."
                : investigation.hasVisited(CaseFile.Place.BIURO)
                ? "Trzy schowki. Który ma kod prototypu z umowy?"
                : "Trzy schowki. Dokument w biurze może wskazać właściwy numer.");

        Rectangle door = new Rectangle(0, 140, 235, 635);
        outline(door, "Powrót do biura", this::showOffice);
        Rectangle left = new Rectangle(465, 262, 245, 468);
        outline(left, "Schowek P-11", () -> message.setText(
                "P-11: próbki starszego projektu. To inny model niż na umowie."));
        Rectangle middle = new Rectangle(867, 260, 235, 470);
        outline(middle, "Schowek P-17", () -> {
            if (investigation.hasVisited(CaseFile.Place.BIURO)) showPrototype();
            else message.setText("Najpierw odczytaj dokument z drukarki w biurze. Jaki numer ma prototyp?");
        });
        Rectangle right = new Rectangle(1270, 260, 250, 470);
        outline(right, "Schowek P-21", () -> message.setText(
                "P-21: urządzenie innego zespołu. Sprawdź kod prototypu w umowie."));
        scaled(room, door, left, middle, right);

        Group plaques = new Group();
        plaques.getChildren().addAll(storagePlaque("P-11", 538),
                storagePlaque("P-17", 930), storagePlaque("P-21", 1340));
        scaledUi(room, plaques);
        raiseHud(room);
        saveProgress();
        transitionTo(room);
    }

    private Label storagePlaque(String code, double x) {
        Label plaque = inspectionNote(code, x, 300, 120);
        plaque.setStyle(plaque.getStyle() + "-fx-text-fill: #ffe0a0; -fx-font-weight: bold;");
        plaque.setMouseTransparent(true);
        return plaque;
    }

    private void showPrototype() {
        Pane inspection = new Pane();
        background(inspection, "prototyp-p17.png");
        Group content = new Group();
        Label heading = inspectionNote("MAGAZYN  /  SCHOWEK P-17", 50, 45, 680);
        Label note = inspectionNote(investigation.hasRecoveredPrototype()
                ? "Prototyp jest już zabezpieczony jako dowód rzeczowy."
                : "Oznaczenie P-17 zgadza się z przedmiotem umowy. W środku jest prototyp.",
                55, 750, 1060);
        Button collect = inspectionButton(investigation.hasRecoveredPrototype()
                ? "Prototyp zabezpieczony" : "Zabezpiecz prototyp", 1100, 805, 455, true);
        collect.setDisable(investigation.hasRecoveredPrototype());
        collect.setOnAction(event -> {
            if (!investigation.recoverPrototype()) return;
            note.setText("Prototyp P-17 trafił do dowodów. " + nextObjective());
            collect.setText("Prototyp zabezpieczony");
            collect.setDisable(true);
            updateProgress();
            reveal(note);
        });
        Button back = inspectionButton("Wróć do magazynu", 70, 805, 345, false);
        back.setOnAction(event -> showStorage());
        content.getChildren().addAll(heading, note, collect, back);
        scaledUi(inspection, content);
        transitionTo(inspection);
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
                        ? "Identyfikator Marka leżał na podłodze. "
                        + (investigation.hasReadLockLog()
                        ? "Dziennik: karta M-04, otwarto o 21:14."
                        : "Nie odczytano dziennika zamka.")
                        : "Nie zbadano podłogi laboratorium.",
                23, 318, 295, 305));
        content.getChildren().add(boardText(
                investigation.hasVisited(CaseFile.Place.KORYTARZ)
                        ? (investigation.hasInstalledFuse()
                        ? "Bezpiecznik włożono; kamera znów działa. "
                        : "Wyjęty bezpiecznik leżał przy szafce. ")
                        + (investigation.hasReadCameraLog()
                        ? "Archiwum: o 21:14 brak nagrania."
                        : "Nie odczytano zapisu kamery.")
                        : investigation.hasInspectedCabinet()
                        ? "W szafce jest puste gniazdo. Poszukaj bezpiecznika obok."
                        : "Nie zbadano przyczyny awarii kamery.",
                23, 714, 295, 305));
        content.getChildren().add(boardText(
                investigation.hasVisited(CaseFile.Place.BIURO)
                        ? "Umowa Oli: sprzedaż P-17, odbiór z magazynu o 22:00."
                        : "Nie zbadano dokumentów w biurze.",
                23, 1107, 295, 300));
        if (investigation.hasQuestioned(CaseFile.Suspect.LENA)) {
            content.getChildren().add(boardText("Lena: o 21:12 sama; karta Marka u Oli.",
                    19, 1107, 420, 300));
        }
        if (investigation.hasQuestioned(CaseFile.Suspect.OLA)) {
            content.getChildren().add(boardText("Ola: o 21:12 byłam z Leną.", 19, 1107, 475, 300));
        }
        String nextStep;
        if (investigation.hasDeductedHiddenEntry()) {
            nextStep = "Karta użyta podczas awarii. Naprawa nie odzyskała starego nagrania.";
        } else if (!investigation.hasHeldEvidence(CaseFile.Place.LABORATORIUM)
                || !investigation.hasVisited(CaseFile.Place.KORYTARZ)) {
            nextStep = "Znajdź identyfikator i bezpiecznik w pomieszczeniach.";
        } else if (!investigation.hasReadLockLog()) {
            nextStep = "Odczytaj dziennik zamka w laboratorium.";
        } else if (!investigation.hasInstalledFuse()) {
            nextStep = "Włóż bezpiecznik do szafki, by uruchomić ekran archiwum.";
        } else if (!investigation.hasReadCameraLog()) {
            nextStep = "Kamera działa. Odczytaj archiwum, by ustalić czas przerwy.";
        } else {
            nextStep = "Masz odczyty i wynik naprawy. Odtwórz kolejność zdarzeń.";
        }
        Label feedback = boardText("Ślady " + investigation.clueCount() + "/3  •  Przesłuchania "
                        + investigation.questionedCount() + "/3\n"
                        + "Wejście: " + (investigation.hasDeductedHiddenEntry() ? "ustalone" : "nieustalone")
                        + "  •  Alibi: " + (investigation.hasDeductedAlibiContradiction()
                        ? "sprzeczne" : "nieporównane")
                        + "  •  P-17: " + (investigation.hasRecoveredPrototype()
                        ? "zabezpieczony" : "nieodnaleziony") + "\n" + nextStep,
                23, 326, 676, 1030);
        content.getChildren().add(feedback);

        Button combine = boardAction(investigation.hasDeductedHiddenEntry()
                ? "Ślady połączone" : "Połącz ślady", true, 302, 813, 265);
        combine.setDisable(investigation.hasDeductedHiddenEntry());
        combine.setOnAction(event -> {
            if (investigation.hasHeldEvidence(CaseFile.Place.LABORATORIUM)
                    && investigation.hasVisited(CaseFile.Place.KORYTARZ)
                    && investigation.hasReadLockLog() && investigation.hasReadCameraLog()
                    && investigation.hasInstalledFuse()) {
                showTimelineDeduction();
            } else {
                feedback.setText(nextObjective());
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

    private void showTimelineDeduction() {
        Pane board = illustratedBoard();
        Group content = new Group();
        Label heading = text("AKTA 01  /  OŚ CZASU", 36, "#ffe0a0");
        heading.setLayoutX(580);
        heading.setLayoutY(38);
        content.getChildren().addAll(heading,
                boardText("KAMERA", 28, 318, 215, 310),
                boardText("21:12", 42, 318, 460, 300),
                boardText("Wyjęty bezpiecznik odciął nagranie.", 23, 318, 530, 305),
                boardText("ZAMEK", 28, 714, 215, 310),
                boardText("21:14", 42, 714, 460, 300),
                boardText("Zamek odnotował identyfikator Marka.", 23, 714, 530, 305),
                boardText("CO WYNIKA ZE ŚLADÓW?", 26, 1107, 215, 300),
                boardEvidenceImage("bezpiecznik.png", 405, 310, 150),
                boardEvidenceImage("identyfikator.png", 790, 310, 155));

        ToggleGroup conclusions = new ToggleGroup();
        CaseFile.EntryConclusion[] options = CaseFile.EntryConclusion.values();
        for (int i = 0; i < options.length; i++) {
            CaseFile.EntryConclusion option = options[i];
            content.getChildren().add(theoryChoice(option.description,
                    conclusions, option, 1095, 305 + i * 94, 320));
        }

        Label feedback = boardText("Karta wskazuje sposób wejścia, a bezpiecznik przerwę nagrania. "
                + "Co możesz ustalić bez zgadywania sprawcy?", 23, 326, 680, 1030);
        Button check = boardAction("Zapisz wniosek", true, 1045, 813, 310);
        check.setOnAction(event -> {
            CaseFile.EntryConclusion selected = conclusions.getSelectedToggle() == null ? null
                    : (CaseFile.EntryConclusion) ((ToggleButton) conclusions.getSelectedToggle()).getUserData();
            Investigation.DeductionResult result = investigation.deduceHiddenEntry(selected);
            feedback.setText(result.feedback());
            if (result.correct()) {
                updateProgress();
                message.setText("Odczyty są teraz na tablicy dowodów. "
                        + "Ktoś użył karty Marka podczas przerwy nagrania. Skonfrontuj go z tą luką.");
                check.setDisable(true);
                conclusions.getToggles().forEach(toggle -> ((ToggleButton) toggle).setDisable(true));
                reveal(feedback);
            } else {
                shake(feedback);
            }
        });
        Button back = boardAction("Wróć do dowodów", false, 310, 813, 285);
        back.setOnAction(event -> showBoard());
        content.getChildren().addAll(feedback, check, back);
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

        Label feedback = boardText("Lena widziała kartę u Oli. Zamek ją zapisał; "
                        + "umowa wyjaśnia motyw. Ułóż teorię z tych śladów.",
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
        Pane board = overlay();
        ImageView art = background(board, "rekonstrukcja.png");
        art.setMouseTransparent(true);
        Group content = new Group();
        Label heading = inspectionNote("AKTA 01  /  SPRAWA ROZWIĄZANA", 50, 42, 860);
        Label motive = inspectionNote("Umowa Oli: odbiór prototypu przez konkurencję o 22:00.", 50, 116, 800);
        Label outage = inspectionNote("21:12  •  Kamera traci zasilanie.", 45, 735, 480);
        Label entry = inspectionNote("21:14  •  Ola używa karty Marka.", 590, 735, 480);
        Label recovery = inspectionNote("P-17  •  Ukryty do odbioru o 22:00.", 1135, 735, 490);
        outage.setOpacity(0);
        entry.setOpacity(0);
        recovery.setOpacity(0);
        Button menu = inspectionButton("Wróć do menu akt", 1160, 850, 430, true);
        menu.setOnAction(event -> showMenu());
        content.getChildren().addAll(heading, motive, outage, entry, recovery, menu);
        scaledUi(board, content);
        revealAfter(outage, 200);
        revealAfter(entry, 850);
        revealAfter(recovery, 1500);
    }

    private void revealAfter(Node node, int milliseconds) {
        PauseTransition delay = new PauseTransition(Duration.millis(milliseconds));
        delay.setOnFinished(event -> reveal(node));
        delay.play();
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

    private ImageView boardEvidenceImage(String file, double x, double y, double width) {
        ImageView image = new ImageView(new Image(
                getClass().getResource("/images/" + file).toExternalForm()));
        image.setPreserveRatio(true);
        image.setFitWidth(width);
        image.setFitHeight(135);
        image.setLayoutX(x);
        image.setLayoutY(y);
        return image;
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
        choice.setPrefSize(width, 84);
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
