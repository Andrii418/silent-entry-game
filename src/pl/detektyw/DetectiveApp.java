package pl.detektyw;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.Cursor;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.Group;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Scale;

/** Pierwszy interfejs gry. Reguły rozgrywki są w Investigation. */
public final class DetectiveApp extends Application {
    private static final String BG = "-fx-background-color: #101824;";
    private static final String PANEL = "-fx-background-color: #1b2838; -fx-background-radius: 12;";
    private static final String TEXT = "-fx-text-fill: #e9edf3; -fx-font-size: 15px;";
    private static final String MUTED = "-fx-text-fill: #aebccc; -fx-font-size: 13px;";
    private static final String BUTTON = "-fx-background-color: #29435b; -fx-text-fill: #f4f8fc; "
            + "-fx-background-radius: 8; -fx-padding: 10 14; -fx-font-size: 14px; -fx-cursor: hand;";

    private final Investigation investigation = new Investigation();
    private final Label contentTitle = label("Akta sprawy", 25);
    private Stage stage;
    private BorderPane dashboard;
    private final VBox content = new VBox(14);
    private final VBox evidence = new VBox(10);
    private final Label progress = new Label();
    private boolean solved;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        Label heading = label("AKTA 01  /  ZNIKNIĘCIE PROTOTYPU", 25);
        Label subtitle = new Label("Zbadaj ślady. Sprawdź zeznania. Odkryj prawdę.");
        subtitle.setStyle(MUTED);
        VBox header = new VBox(6, heading, subtitle);
        header.setPadding(new Insets(22, 26, 18, 26));

        VBox navigation = new VBox(12);
        navigation.setPadding(new Insets(20));
        navigation.setStyle(PANEL);
        navigation.setPrefWidth(220);
        navigation.getChildren().add(section("MIEJSCA"));
        for (CaseFile.Place place : CaseFile.Place.values()) {
            navigation.getChildren().add(button(place.name, () -> visit(place)));
        }
        navigation.getChildren().add(section("PODEJRZANI"));
        for (CaseFile.Suspect suspect : CaseFile.Suspect.values()) {
            navigation.getChildren().add(button(suspect.name, () -> question(suspect)));
        }

        navigation.getChildren().add(section("DEDUKCJE"));
        navigation.getChildren().add(
                button("Połącz ślady", this::connectClues)
        );
        navigation.getChildren().add(section("ROZWIĄZANIE"));
        navigation.getChildren().add(button("Wskaż sprawcę", this::showAccusation));

        content.setPadding(new Insets(24));
        content.setStyle(PANEL);
        ScrollPane contentScroll = new ScrollPane(content);
        contentScroll.setFitToWidth(true);
        contentScroll.setStyle("-fx-background: #1b2838; -fx-background-color: transparent;");
        HBox.setHgrow(contentScroll, Priority.ALWAYS);

        evidence.setPadding(new Insets(20));
        evidence.setStyle(PANEL);
        evidence.setPrefWidth(245);

        HBox columns = new HBox(16, navigation, contentScroll, evidence);
        columns.setPadding(new Insets(0, 26, 26, 26));
        HBox.setHgrow(contentScroll, Priority.ALWAYS);

        dashboard = new BorderPane();
        dashboard.setStyle(BG);
        dashboard.setTop(header);
        dashboard.setCenter(columns);
        showIntroduction();
        refreshEvidence();

        stage.setTitle("Akta 01: Zniknięcie prototypu");
        stage.setScene(new Scene(dashboard, 1080, 680));
        stage.setMinWidth(840);
        stage.setMinHeight(550);
        stage.show();
    }

    private void showIntroduction() {
        show("Sprawa", "O 21:20 odkryto kradzież prototypu z zamkniętego laboratorium. "
                + "W budynku zostały trzy osoby: Lena, Marek i Ola.\n\n"
                + "Klikaj miejsca po lewej, by znaleźć dowody. Następnie przesłuchaj podejrzanych. "
                + "Gdy zbierzesz komplet informacji, wskaż sprawcę.");
    }

    private void visit(CaseFile.Place place) {
        if (place == CaseFile.Place.LABORATORIUM) {
            showLaboratory();
            return;
        }
        if (place == CaseFile.Place.KORYTARZ) {
            showCorridor();
            return;
        }

        show(place.name, "Rozejrzyj się po pomieszczeniu i znajdź coś podejrzanego.");

        content.getChildren().add(
                button("Zbadaj podejrzany przedmiot", () -> {
                    investigation.discoverClue(place);
                    show(place.name, place.description);
                    refreshEvidence();
                })
        );
    }

    private void showLaboratory() {
        Pane room = new Pane();

        ImageView background = new ImageView(
                new Image(getClass().getResource("/images/laboratorium-v2.png").toExternalForm())
        );
        background.fitWidthProperty().bind(room.widthProperty());
        background.fitHeightProperty().bind(room.heightProperty());

        Label description = new Label("Rozejrzyj się po laboratorium.");
        description.setWrapText(true);
        description.setPadding(new Insets(12));
        description.setStyle(
                "-fx-background-color: rgba(8, 18, 28, 0.85);"
                        + "-fx-text-fill: white; -fx-font-size: 17px;"
        );
        description.layoutXProperty().bind(room.widthProperty().multiply(0.25));
        description.layoutYProperty().bind(room.heightProperty().subtract(80));
        description.prefWidthProperty().bind(room.widthProperty().multiply(0.5));

        Button back = hotspot(room, "← Akta", 0.02, 0.03);
        back.setOnAction(event -> stage.getScene().setRoot(dashboard));

        Rectangle terminal = new Rectangle(818, 353, 190, 120);
        terminal.setArcWidth(14);
        terminal.setArcHeight(14);
        outline(terminal, "Terminal", () ->
                description.setText("Terminal jest wyłączony. Nie ma na nim danych."));

        Polygon locker = new Polygon(
                1350, 140, 1665, 140, 1665, 690, 1320, 690, 1320, 162
        );
        outline(locker, "Szafa", () ->
                description.setText("W szafie są narzędzia laboratoryjne. Niczego nie brakuje."));

        ImageView badge = new ImageView(
                new Image(getClass().getResource("/images/identyfikator.png").toExternalForm())
        );
        badge.setPreserveRatio(true);
        badge.fitWidthProperty().bind(room.widthProperty().multiply(0.10));
        badge.layoutXProperty().bind(room.widthProperty().multiply(0.35));
        badge.layoutYProperty().bind(room.heightProperty().multiply(0.76));
        badge.setCursor(Cursor.HAND);

        Tooltip.install(badge, new Tooltip("Identyfikator Marka"));

        badge.setOnMouseClicked(event -> {
            investigation.discoverClue(CaseFile.Place.LABORATORIUM);
            description.setText(CaseFile.Place.LABORATORIUM.description);
            badge.setVisible(false);
            refreshEvidence();
        });

        badge.setVisible(!investigation.hasVisited(CaseFile.Place.LABORATORIUM));

        Polygon door = new Polygon(
                100, 160, 340, 160, 389, 200, 389, 690, 62, 690, 62, 200
        );
        outline(door, "Drzwi", () -> {
            if (investigation.hasVisited(CaseFile.Place.LABORATORIUM)) {
                stage.getScene().setRoot(dashboard);
                visit(CaseFile.Place.KORYTARZ);
            } else {
                description.setText("Drzwi są zamknięte. Potrzebujesz identyfikatora.");
            }
        });

        Group outlines = new Group(door, terminal, locker);
        Scale scale = new Scale();
        scale.xProperty().bind(room.widthProperty().divide(1672));
        scale.yProperty().bind(room.heightProperty().divide(941));
        outlines.getTransforms().add(scale);

        room.getChildren().addAll(background, outlines, back, badge, description);
        stage.getScene().setRoot(room);
    }

    private void outline(Shape shape, String hint, Runnable action) {
        shape.setFill(Color.TRANSPARENT);
        shape.setStroke(Color.TRANSPARENT);
        shape.setStrokeWidth(4);
        shape.setCursor(Cursor.HAND);
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

    private Button hotspot(Pane room, String text, double x, double y) {
        Button result = new Button(text);

        result.layoutXProperty().bind(room.widthProperty().multiply(x));
        result.layoutYProperty().bind(room.heightProperty().multiply(y));

        result.setStyle(
                "-fx-background-color: rgba(8, 18, 28, 0.75);"
                        + "-fx-border-color: #8bd8ed;"
                        + "-fx-text-fill: white;"
                        + "-fx-padding: 8 12;"
                        + "-fx-cursor: hand;"
        );

        return result;
    }

    private void showCorridor() {
        show("Korytarz", "Kamera nie działa. Rozejrzyj się.");

        Pane room = new Pane();
        room.setPrefSize(520, 310);
        room.setMinSize(520, 310);
        ImageView background = new ImageView(
                new Image(getClass().getResource("/images/korytarz.png").toExternalForm())
        );
        background.setFitWidth(520);
        background.setFitHeight(310);
        room.getChildren().add(background);

        Label description = new Label("Kliknij przedmiot, który chcesz zbadać.");
        description.setWrapText(true);
        description.setStyle(TEXT);

        Button fuse = new Button("Szafka serwisowa");
        fuse.setLayoutX(340);
        fuse.setLayoutY(150);

        fuse.setOnAction(event -> {
            investigation.discoverClue(CaseFile.Place.KORYTARZ);
            description.setText(CaseFile.Place.KORYTARZ.description);
            fuse.setDisable(true);
            refreshEvidence();
        });

        if (investigation.hasVisited(CaseFile.Place.KORYTARZ)) {
            fuse.setDisable(true);
            description.setText("Bezpiecznik jest już w aktach sprawy.");
        }

        room.getChildren().add(fuse);
        content.getChildren().addAll(room, description);
    }

    private void question(CaseFile.Suspect suspect) {
        investigation.question(suspect);
        show(suspect.name + " / " + suspect.role,
                String.join("\n\n", CaseFile.testimony(suspect,
                        investigation.hasVisited(CaseFile.Place.KORYTARZ))));
        refreshEvidence();
    }

    private void connectClues() {
        if (investigation.deduceHiddenEntry()) {
            show(
                    "Ukryte wejście",
                    "Kamera zgasła o 21:12, a identyfikator Marka otworzył "
                            + "laboratorium o 21:14. Ktoś mógł wejść niezauważony, "
                            + "posługując się jego identyfikatorem."
            );
        } else {
            show(
                    "Brakuje śladów",
                    "Zbadaj laboratorium i korytarz, zanim połączysz te informacje."
            );
        }

        refreshEvidence();
    }

    private void showAccusation() {
        if (solved) {
            show("Sprawa zamknięta", "Ola zleciła wyłączenie kamery i użyła identyfikatora Marka. "
                    + "Umowa sprzedaży ujawnia motyw.");
            return;
        }
        if (!investigation.canAccuse()) {
            show("Za mało dowodów", "Zbadaj wszystkie trzy miejsca i przesłuchaj wszystkie trzy osoby. "
                    + "Wróć do zeznań, gdy znajdziesz nowy ślad.");
            return;
        }
        show("Kogo oskarżasz?", "Wybierz osobę, której działania wyjaśniają zniknięcie prototypu.");
        for (CaseFile.Suspect suspect : CaseFile.Suspect.values()) {
            content.getChildren().add(button(suspect.name, () -> accuse(suspect)));
        }
    }

    private void accuse(CaseFile.Suspect suspect) {
        if (investigation.isCulprit(suspect)) {
            solved = true;
            show("Sprawa rozwiązana", "To Ola. Zleciła Markowi wyłączenie kamery, pożyczyła jego "
                    + "identyfikator i weszła do laboratorium o 21:14. Umowa z jej podpisem "
                    + "wyjaśnia, po co ukradła prototyp.");
        } else {
            show("To się nie zgadza", "Ta osoba nie wyjaśnia wszystkich śladów. "
                    + "Porównaj czas wejścia, identyfikator i umowę sprzedaży.");
            content.getChildren().add(button("Spróbuj ponownie", this::showAccusation));
        }
    }

    private void show(String title, String body) {
        contentTitle.setText(title);
        Label paragraph = new Label(body);
        paragraph.setWrapText(true);
        paragraph.setStyle(TEXT);
        paragraph.setMaxWidth(Double.MAX_VALUE);
        content.getChildren().setAll(contentTitle, paragraph);
    }

    private void refreshEvidence() {
        evidence.getChildren().clear();
        evidence.getChildren().add(section("TABLICA DOWODÓW"));
        progress.setText("Ślady: " + investigation.clueCount() + "/3\nPrzesłuchania: "
                + investigation.questionedCount() + "/3");
        progress.setStyle(MUTED);
        evidence.getChildren().add(progress);
        for (CaseFile.Place place : CaseFile.Place.values()) {
            if (investigation.hasVisited(place)) {
                Label clue = new Label("• " + place.clue);
                clue.setWrapText(true);
                clue.setStyle(TEXT);
                evidence.getChildren().add(clue);
            }
        }
        if (investigation.clueCount() == 0) {
            Label empty = new Label("Tu pojawią się znalezione ślady.");
            empty.setWrapText(true);
            empty.setStyle(MUTED);
            evidence.getChildren().add(empty);
        }
    }

    private static Button button(String title, Runnable action) {
        Button button = new Button(title);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setStyle(BUTTON);
        button.setOnAction(event -> action.run());
        return button;
    }

    private static Label section(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #89b9d8; -fx-font-size: 12px; -fx-font-weight: bold;");
        label.setPadding(new Insets(10, 0, 3, 0));
        return label;
    }

    private static Label label(String text, int size) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #f6f1e8; -fx-font-size: " + size + "px; -fx-font-weight: bold;");
        return label;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
