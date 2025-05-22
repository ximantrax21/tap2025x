package com.example.tap2025x.vistas;
import com.example.tap2025x.componentes.Hilo;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Celayork extends Stage {
    private GridPane gdpCalles;
    private Button btnIniciar;
    private Label[] lblRutas;
    private ProgressBar[] pgbRutas;
    private Scene escena;
    private VBox vBox;
    private String[] strRutas = {"Rihanna","Chris Brown","Justin Bieber","Akron","Katy Perry"};

    public Celayork(){
        CrearUI();
        this.setTitle("Raperos Run");
        this.setScene(escena);
        this.show();
    }

    private void CrearUI() {
        btnIniciar = new Button("Iniciar");
        pgbRutas = new ProgressBar[5];
        lblRutas = new Label[5];
        gdpCalles = new GridPane();

        gdpCalles.setHgap(10);
        gdpCalles.setVgap(5);
        gdpCalles.setPadding(new Insets(10));

        for (int i = 0; i < pgbRutas.length; i++) {
            lblRutas[i] = new Label(strRutas[i]);
            pgbRutas[i] = new ProgressBar(0);
            pgbRutas[i].setPrefWidth(200);
            gdpCalles.add(lblRutas[i], 0, i);
            gdpCalles.add(pgbRutas[i], 1, i);
        }

        btnIniciar.setOnAction(e -> iniciarHilos());

        vBox = new VBox(10, gdpCalles, btnIniciar);
        vBox.setPadding(new Insets(10));
        escena = new Scene(vBox, 400, 250);
    }

    private void iniciarHilos() {
        for (int i = 0; i < pgbRutas.length; i++) {
            pgbRutas[i].setProgress(0);
        }

        for (int i = 0; i < strRutas.length; i++) {
            final int indice = i;
            HiloConUI hilo = new HiloConUI(strRutas[i], pgbRutas[i]);
            hilo.start();
        }

        btnIniciar.setDisable(true);

        new Thread(() -> {
            try {
                Thread.sleep(15000);
                Platform.runLater(() -> btnIniciar.setDisable(false));
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private class HiloConUI extends Hilo {
        private ProgressBar progressBar;

        public HiloConUI(String nombre, ProgressBar progressBar) {
            super(nombre);
            this.progressBar = progressBar;
        }

        @Override
        public void run() {
            for (int i = 1; i <= 10; i++) {
                try {
                    sleep((long)(Math.random() * 3000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                final int km = i;
                final double progreso = km / 10.0;

                Platform.runLater(() -> {
                    progressBar.setProgress(progreso);
                });

                System.out.println("El corredor " + this.getName() + " llegó al KM " + i);
            }

            Platform.runLater(() -> {
                System.out.println("¡El corredor " + this.getName() + " ha terminado la carrera!");
            });
        }
    }
}