package com.example.tap2025x.vistas;

import com.example.tap2025x.componentes.Hilo;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Celayork extends Stage {
    private VBox vBox;
    private GridPane gdpCalles;
    private Button btnIniciar;
    private Label[] lblRutas;
    private ProgressBar[] pgbRutas;
    private Scene escena;
    private String[] strRutas = {"Rihanna","Drake","Chris Brown","Wiz Khalifa","Snoop Dog"};
    private Hilo[] thrRutas;
    public Celayork(){
        CrearUI();
        this.setTitle("Calles de Celaya");
        this.setScene(escena);
        this.show();

    }

    public void CrearUI(){
        btnIniciar=new Button("Iniciar");
        pgbRutas = new ProgressBar[5];
        lblRutas = new Label[5];
        thrRutas= new Hilo[5];
        gdpCalles =new GridPane();
        for (int i = 0; i < pgbRutas.length; i++) {
            lblRutas[i]= new Label(strRutas[i]);
            pgbRutas[i]= new ProgressBar(0);//inicia en 0 para asegurarnos qu desde el inicio este en 0, se  llena de 0-1
            thrRutas[i]= new Hilo(strRutas[i]);
            gdpCalles.add(lblRutas[i], 0,i);
            gdpCalles.add(pgbRutas[i],1,i);
        }
        btnIniciar.setOnAction(actionEvent -> {
            for (int i = 0; i < pgbRutas.length; i++) {
                thrRutas[i].start();
            }
        });
        vBox= new VBox(gdpCalles, btnIniciar);
        escena= new Scene(vBox, 300, 200);
    }
}
