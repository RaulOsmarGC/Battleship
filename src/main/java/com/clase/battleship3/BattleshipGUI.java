package com.clase.battleship3;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

public class BattleshipGUI extends Application implements VistaJuego {

    private TextArea logArea;
    private GridPane gridPropio;
    private GridPane gridEnemigo;
    private Label estadoLabel;

    private int[] coordenadasDisparo = null;
    private final Object disparoLock = new Object();
    private Stage primaryStage;

    private Button[][] botonesEnemigos = new Button[10][10];
    private Rectangle[][] celdasPropias = new Rectangle[10][10];

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.setTitle("Battleship - JavaFX");

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        estadoLabel = new Label("BATTLESHIP");
        estadoLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        root.setTop(estadoLabel);
        BorderPane.setAlignment(estadoLabel, Pos.CENTER);

        HBox tablerosBox = new HBox(20);
        tablerosBox.setAlignment(Pos.CENTER);

        VBox boxPropio = new VBox(5);
        boxPropio.getChildren().add(new Label("MAPA ALIADO"));
        gridPropio = new GridPane();
        inicializarTableroPropio();
        boxPropio.getChildren().add(gridPropio);

        VBox boxEnemigo = new VBox(5);
        boxEnemigo.getChildren().add(new Label("MAPA ENEMIGO"));
        gridEnemigo = new GridPane();
        inicializarTableroEnemigo();
        boxEnemigo.getChildren().add(gridEnemigo);

        tablerosBox.getChildren().addAll(boxPropio, boxEnemigo);
        root.setCenter(tablerosBox);

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(100);
        root.setBottom(logArea);

        Scene scene = new Scene(root, 900, 600);
        stage.setScene(scene);
        stage.show();

        new Thread(this::iniciarLogicaJuego).start();
    }

    private void iniciarLogicaJuego() {
        JuegoBattleship modelo = new JuegoBattleship();
        GestorRed red = new GestorRed();
        ControladorJuego controlador = new ControladorJuego(modelo, red, this);
        controlador.iniciar();
    }

    private void inicializarTableroPropio() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Rectangle rect = new Rectangle(30, 30);
                rect.setFill(Color.LIGHTBLUE);
                rect.setStroke(Color.BLACK);
                celdasPropias[i][j] = rect;
                gridPropio.add(rect, j, i);
            }
        }
    }

    private void inicializarTableroEnemigo() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                Button btn = new Button();
                btn.setPrefSize(30, 30);
                int finalI = i;
                int finalJ = j;
                btn.setOnAction(e -> registrarClickEnemigo(finalI, finalJ));
                btn.setDisable(true);
                botonesEnemigos[i][j] = btn;
                gridEnemigo.add(btn, j, i);
            }
        }
    }

    private void registrarClickEnemigo(int x, int y) {
        synchronized (disparoLock) {
            coordenadasDisparo = new int[]{x, y};
            disparoLock.notifyAll();
        }
    }

    @Override
    public void mostrarMensaje(String mensaje) {
        Platform.runLater(() -> {
            logArea.appendText(mensaje + "\n");
            estadoLabel.setText(mensaje);
        });
    }

    @Override
    public void mostrarError(String error) {
        Platform.runLater(() -> logArea.appendText("ERROR: " + error + "\n"));
    }

    @Override
    public void actualizarTableroPropio(char[][] tablero) {
        Platform.runLater(() -> {
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    Color color;
                    switch (tablero[i][j]) {
                        case '~': color = Color.LIGHTBLUE; break;
                        case 'X': color = Color.RED; break;
                        case 'O': color = Color.WHITE; break;
                        default: color = Color.GRAY; break;
                    }
                    celdasPropias[i][j].setFill(color);
                }
            }
        });
    }

    @Override
    public void actualizarTableroEnemigo(char[][] tablero) {
        Platform.runLater(() -> {
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    String texto = "";
                    String estilo = "";
                    switch (tablero[i][j]) {
                        case 'X': texto = "X"; estilo = "-fx-base: #ff4444;"; break;
                        case 'O': texto = "O"; estilo = "-fx-base: #ffffff;"; break;
                        default: texto = ""; estilo = ""; break;
                    }
                    botonesEnemigos[i][j].setText(texto);
                    if (!estilo.isEmpty()) botonesEnemigos[i][j].setStyle(estilo);
                }
            }
        });
    }

    @Override
    public void actualizarEstadoBarcos(Map<String, String> estadoBarcos) {

    }

    @Override
    public boolean elegirModo() {
        final boolean[] esServidor = {false};
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Modo de Juego");
            alert.setHeaderText("Selecciona tu modo de conexión");
            ButtonType btnServer = new ButtonType("Crear Partida (Servidor)");
            ButtonType btnClient = new ButtonType("Unirse (Cliente)");
            alert.getButtonTypes().setAll(btnServer, btnClient);

            Optional<ButtonType> result = alert.showAndWait();
            esServidor[0] = result.isPresent() && result.get() == btnServer;
            latch.countDown();
        });

        try { latch.await(); } catch (InterruptedException e) { e.printStackTrace(); }
        return esServidor[0];
    }

    @Override
    public String pedirIPServidor() {
        final String[] ip = {""};
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            TextInputDialog dialog = new TextInputDialog("localhost");
            dialog.setTitle("Conexión");
            dialog.setHeaderText("Ingresa la IP del servidor:");
            dialog.showAndWait().ifPresent(result -> ip[0] = result);
            latch.countDown();
        });

        try { latch.await(); } catch (InterruptedException e) { e.printStackTrace(); }
        return ip[0];
    }

    @Override
    public String pedirNombreJugador() {
        final String[] nombre = {"Jugador"};
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            TextInputDialog dialog = new TextInputDialog("Capitan");
            dialog.setTitle("Registro");
            dialog.setHeaderText("Ingresa tu nombre:");
            dialog.showAndWait().ifPresent(result -> nombre[0] = result);
            latch.countDown();
        });

        try { latch.await(); } catch (InterruptedException e) { e.printStackTrace(); }
        return nombre[0];
    }

    @Override
    public int[] pedirDisparo() {
        Platform.runLater(() -> {
            estadoLabel.setText("Selecciona una coordenada.");
            for (Button[] fila : botonesEnemigos) {
                for (Button btn : fila) btn.setDisable(false);
            }
        });

        int[] disparo;
        synchronized (disparoLock) {
            try {
                while (coordenadasDisparo == null) {
                    disparoLock.wait();
                }
            } catch (InterruptedException e) {
                return new int[]{0,0};
            }
            disparo = coordenadasDisparo;
            coordenadasDisparo = null;
        }

        Platform.runLater(() -> {
            for (Button[] fila : botonesEnemigos) {
                for (Button btn : fila) btn.setDisable(true);
            }
        });

        return disparo;
    }

    @Override
    public void reportarFinJuego(boolean ganaste) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Fin del Juego");
            alert.setHeaderText(ganaste ? "¡VICTORIA!" : "DERROTA");
            alert.setContentText(ganaste ? "Has hundido la flota enemiga." : "Tu flota ha sido destruida.");
            alert.showAndWait();
            Platform.exit();
            System.exit(0);
        });
    }
}