package com.example.tap2025x.vistas;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.image.WritableImage;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Rompecabezas extends Stage {
    private Scene escena;
    private BorderPane raiz;
    private GridPane rejillaRompecabezas; // Grilla vacía para colocar piezas (IZQUIERDA)
    private GridPane rejillaPiezas; // Grilla con piezas desordenadas (DERECHA)
    private ImageView[][] rejillaObjetivo; // Matriz para rastrear posiciones en la grilla
    private List<PiezaRompecabezas> piezas; // Lista de piezas del rompecabezas
    private Image imagenOriginal;
    private int tamanoRejilla = 3;
    private int movimientos = 0;
    private LocalDateTime tiempoInicio;
    private Label etiquetaMovimientos;
    private Label etiquetaTiempo;
    private Timeline lineaTiempo;
    private List<RegistroJuego> historial = new ArrayList<>();
    private final String ARCHIVO_HISTORIAL = "Historial.txt"; // Archivo se guarda en el directorio del proyecto
    private final double TAMANO_FICHA = 80.0; // Tamaño uniforme para todas las piezas

    private final String[] IMAGENES_INTEGRADAS = {
            "/images/camaro.jpeg",
            "/images/deportivo.jpg",
            "/images/supra.jpeg"
    };

    // Imagen de fondo
    private final String IMAGEN_FONDO = "/images/pista.jpg";

    // Audio de fondo
    private final String AUDIO_FONDO = "/audio/Go DJ.mp3";
    private MediaPlayer reproductor;
    private boolean musicaActivada = true;

    public Rompecabezas() {
        crearInterfaz();
        this.setTitle("Rompecabezas Midnight Club 3 🏎️");
        this.setScene(escena);

        // Detener audio al cerrar la ventana
        this.setOnCloseRequest(e -> {
            detenerAudio();
        });

        this.show();
        cargarHistorial();
    }

    private void crearInterfaz() {
        raiz = new BorderPane();
        raiz.setPadding(new Insets(15));

        // Configurar fondo de la ventana
        configurarFondo();

        // Panel superior con controles
        HBox panelSuperior = new HBox(15);
        panelSuperior.setAlignment(Pos.CENTER);
        panelSuperior.getStyleClass().add("panel-superior");

        ComboBox<String> comboImagen = new ComboBox<>();
        comboImagen.getItems().addAll("🚗Camaro", "🏎️Deportivo", "🚙Supra");
        comboImagen.setValue("🚗Camaro");
        comboImagen.setOnAction(e -> cargarImagenIntegrada(comboImagen.getSelectionModel().getSelectedIndex()));
        comboImagen.getStyleClass().add("combo-personalizado");

        ComboBox<String> comboDificultad = new ComboBox<>();
        comboDificultad.getItems().addAll("😇Fácil (3x3)", "😬Medio (4x4)", "😰Difícil (5x5)");
        comboDificultad.setValue("😇Fácil (3x3)");
        comboDificultad.setOnAction(e -> {
            tamanoRejilla = 3 + comboDificultad.getSelectionModel().getSelectedIndex();
            if (imagenOriginal != null) inicializarRompecabezas();
        });
        comboDificultad.getStyleClass().add("combo-personalizado");

        Button botonNuevoJuego = new Button("🎮 Nuevo Juego");
        botonNuevoJuego.setOnAction(e -> inicializarRompecabezas());
        botonNuevoJuego.getStyleClass().add("boton-principal");

        Button botonHistorial = new Button("⏳ Historial");
        botonHistorial.setOnAction(e -> mostrarHistorial());
        botonHistorial.getStyleClass().add("boton-principal");

        Button botonMusica = new Button("🎵 Música: ON");
        botonMusica.setOnAction(e -> alternarMusica(botonMusica));
        botonMusica.getStyleClass().add("boton-principal");

        panelSuperior.getChildren().addAll(comboImagen, comboDificultad, botonNuevoJuego, botonHistorial, botonMusica);
        raiz.setTop(panelSuperior);

        // Panel central con las dos grillas lado a lado
        HBox panelCentral = new HBox(40);
        panelCentral.setAlignment(Pos.CENTER);
        panelCentral.getStyleClass().add("panel-central");

        // LADO IZQUIERDO: Grilla de armado (vacía)
        VBox seccionIzquierda = new VBox(15);
        seccionIzquierda.setAlignment(Pos.CENTER);

        Label etiquetaRejilla = new Label("Arma tu auto");
        etiquetaRejilla.getStyleClass().add("etiqueta-titulo");

        rejillaRompecabezas = new GridPane();
        rejillaRompecabezas.setAlignment(Pos.CENTER);
        rejillaRompecabezas.setHgap(3);
        rejillaRompecabezas.setVgap(3);
        rejillaRompecabezas.getStyleClass().add("rejilla-rompecabezas");

        seccionIzquierda.getChildren().addAll(etiquetaRejilla, rejillaRompecabezas);

        // LADO DERECHO: Grilla con piezas desordenadas
        VBox seccionDerecha = new VBox(15);
        seccionDerecha.setAlignment(Pos.CENTER);

        Label etiquetaPiezas = new Label("Piezas de tu Auto:");
        etiquetaPiezas.getStyleClass().add("etiqueta-titulo");

        rejillaPiezas = new GridPane();
        rejillaPiezas.setAlignment(Pos.CENTER);
        rejillaPiezas.setHgap(8);
        rejillaPiezas.setVgap(8);
        rejillaPiezas.getStyleClass().add("rejilla-piezas");

        seccionDerecha.getChildren().addAll(etiquetaPiezas, rejillaPiezas);

        panelCentral.getChildren().addAll(seccionIzquierda, seccionDerecha);
        raiz.setCenter(panelCentral);

        // Panel inferior con estadísticas
        HBox panelInferior = new HBox(30);
        panelInferior.setAlignment(Pos.CENTER);
        panelInferior.getStyleClass().add("panel-inferior");

        etiquetaMovimientos = new Label("Movimientos: 0");
        etiquetaMovimientos.getStyleClass().add("etiqueta-estadistica");

        etiquetaTiempo = new Label("Tiempo: 00:00");
        etiquetaTiempo.getStyleClass().add("etiqueta-estadistica");

        panelInferior.getChildren().addAll(etiquetaMovimientos, etiquetaTiempo);
        raiz.setBottom(panelInferior);

        escena = new Scene(raiz, 1300, 800);

        // Cargar hoja de estilos CSS
        try {
            String hojaEstilos = getClass().getResource("/styles/styles.css").toExternalForm();
            escena.getStylesheets().add(hojaEstilos);
        } catch (Exception e) {
            System.err.println("No se pudo cargar la hoja de estilos: " + e.getMessage());
        }

        cargarImagenIntegrada(0);
        inicializarAudio();
    }

    private void inicializarAudio() {
        try {
            // Intentar cargar el archivo de audio
            String rutaAudio = getClass().getResource(AUDIO_FONDO).toString();
            Media media = new Media(rutaAudio);
            reproductor = new MediaPlayer(media);

            // Configurar reproducción en bucle
            reproductor.setCycleCount(MediaPlayer.INDEFINITE);

            // Configurar volumen (0.0 a 1.0)
            reproductor.setVolume(0.3);

            // Configurar manejo de errores
            reproductor.setOnError(() -> {
                System.err.println("Error al reproducir audio: " + reproductor.getError());
                musicaActivada = false;
            });

            // Iniciar reproducción automáticamente
            if (musicaActivada) {
                reproductor.play();
            }

        } catch (Exception e) {
            System.err.println("No se pudo cargar el archivo de audio: " + AUDIO_FONDO);
            System.err.println("Error: " + e.getMessage());
            musicaActivada = false;
            reproductor = null;
        }
    }

    private void alternarMusica(Button botonMusica) {
        if (reproductor != null) {
            if (musicaActivada) {
                reproductor.pause();
                botonMusica.setText("🎵 Música: OFF");
                musicaActivada = false;
            } else {
                reproductor.play();
                botonMusica.setText("🎵 Música: ON");
                musicaActivada = true;
            }
        } else {
            botonMusica.setText("🎵 No disponible");
        }
    }

    private void detenerAudio() {
        if (reproductor != null) {
            reproductor.stop();
            reproductor.dispose();
        }
    }

    private void configurarFondo() {
        try {
            InputStream flujoEntrada = getClass().getResourceAsStream(IMAGEN_FONDO);
            if (flujoEntrada != null) {
                Image imagenFondo = new Image(flujoEntrada);
                BackgroundImage imagenFondoPantalla = new BackgroundImage(
                        imagenFondo,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.CENTER,
                        new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
                );
                Background fondo = new Background(imagenFondoPantalla);
                raiz.setBackground(fondo);
            } else {
                System.err.println("No se pudo cargar la imagen de fondo: " + IMAGEN_FONDO);
                // Fondo de color como respaldo
                raiz.getStyleClass().add("fondo-degradado");
            }
        } catch (Exception e) {
            System.err.println("Error al cargar la imagen de fondo: " + e.getMessage());
            // Fondo de color como respaldo
            raiz.getStyleClass().add("fondo-degradado");
        }
    }

    private void cargarImagenIntegrada(int indice) {
        try {
            InputStream flujoEntrada = getClass().getResourceAsStream(IMAGENES_INTEGRADAS[indice]);
            if (flujoEntrada != null) {
                imagenOriginal = new Image(flujoEntrada);
                inicializarRompecabezas();
            } else {
                mostrarAlerta("Error", "No se pudo cargar la imagen: " + IMAGENES_INTEGRADAS[indice]);
            }
        } catch (Exception e) {
            mostrarAlerta("Error", "Error al cargar la imagen: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Image crearImagenFicha(Image fuente, int fila, int columna, int tamanoRejilla) {
        // Crear un canvas para dibujar la pieza perfectamente
        Canvas lienzo = new Canvas(TAMANO_FICHA, TAMANO_FICHA);
        GraphicsContext contextoGrafico = lienzo.getGraphicsContext2D();

        // Calcular las dimensiones para hacer la imagen cuadrada
        double anchoFuente = fuente.getWidth();
        double altoFuente = fuente.getHeight();
        double tamano = Math.min(anchoFuente, altoFuente);

        // Centrar la imagen cuadrada
        double inicioX = (anchoFuente - tamano) / 2.0;
        double inicioY = (altoFuente - tamano) / 2.0;

        // Calcular el tamaño de cada pieza
        double tamanoPieza = tamano / tamanoRejilla;

        // Calcular las coordenadas de la pieza específica
        double origenX = inicioX + (columna * tamanoPieza);
        double origenY = inicioY + (fila * tamanoPieza);

        // Dibujar la pieza en el canvas escalándola al tamaño deseado
        contextoGrafico.drawImage(fuente,
                origenX, origenY, tamanoPieza, tamanoPieza,  // Coordenadas y tamaño de origen
                0, 0, TAMANO_FICHA, TAMANO_FICHA);       // Coordenadas y tamaño de destino

        // Agregar borde a la pieza
        contextoGrafico.setStroke(Color.DARKRED);
        contextoGrafico.setLineWidth(2);
        contextoGrafico.strokeRect(1, 1, TAMANO_FICHA-2, TAMANO_FICHA-2);

        // Convertir el canvas a imagen
        SnapshotParameters parametros = new SnapshotParameters();
        parametros.setFill(Color.TRANSPARENT);
        return lienzo.snapshot(parametros, null);
    }

    private void inicializarRompecabezas() {
        if (imagenOriginal == null) return;

        if (lineaTiempo != null) lineaTiempo.stop();

        movimientos = 0;
        etiquetaMovimientos.setText("👆🏼Movimientos: 0");
        etiquetaTiempo.setText("⏱️ Tiempo: 00:00");
        tiempoInicio = LocalDateTime.now();

        // Limpiar grillas
        rejillaRompecabezas.getChildren().clear();
        rejillaPiezas.getChildren().clear();

        // Inicializar matriz de seguimiento
        rejillaObjetivo = new ImageView[tamanoRejilla][tamanoRejilla];
        piezas = new ArrayList<>();

        // CREAR GRILLA IZQUIERDA (zona de armado) - VACÍA
        for (int i = 0; i < tamanoRejilla; i++) {
            for (int j = 0; j < tamanoRejilla; j++) {
                StackPane celda = new StackPane();
                celda.setPrefSize(TAMANO_FICHA, TAMANO_FICHA);
                celda.setMaxSize(TAMANO_FICHA, TAMANO_FICHA);
                celda.setMinSize(TAMANO_FICHA, TAMANO_FICHA);

                // Aplicar estilo CSS base
                celda.getStyleClass().add("celda-base");

                final int fila = i, columna = j;

                // Configurar drag and drop
                celda.setOnDragOver(e -> {
                    if (e.getDragboard().hasString()) {
                        e.acceptTransferModes(javafx.scene.input.TransferMode.MOVE);
                        celda.getStyleClass().clear();
                        celda.getStyleClass().add("celda-hover");
                    }
                    e.consume();
                });

                celda.setOnDragExited(e -> {
                    celda.getStyleClass().clear();
                    celda.getStyleClass().add("celda-base");
                    e.consume();
                });

                celda.setOnDragDropped(e -> {
                    if (e.getDragboard().hasString()) {
                        int idPieza = Integer.parseInt(e.getDragboard().getString());
                        colocarPieza(idPieza, fila, columna);
                        e.setDropCompleted(true);
                    }
                    celda.getStyleClass().clear();
                    celda.getStyleClass().add("celda-base");
                    e.consume();
                });

                rejillaRompecabezas.add(celda, j, i);
            }
        }

        // CREAR TODAS LAS PIEZAS (tamanoRejilla * tamanoRejilla)
        List<Integer> posiciones = new ArrayList<>();
        for (int i = 0; i < tamanoRejilla * tamanoRejilla; i++) posiciones.add(i);
        Collections.shuffle(posiciones);

        // Crear las piezas y colocarlas en la grilla derecha
        for (int i = 0; i < tamanoRejilla * tamanoRejilla; i++) {
            int posicion = posiciones.get(i);
            int filaOriginal = posicion / tamanoRejilla;
            int columnaOriginal = posicion % tamanoRejilla;

            Image imagenFicha = crearImagenFicha(imagenOriginal, filaOriginal, columnaOriginal, tamanoRejilla);
            PiezaRompecabezas pieza = new PiezaRompecabezas(i, posicion, imagenFicha);
            piezas.add(pieza);
        }

        // Colocar las piezas en la grilla derecha de manera ordenada
        redistribuirPiezasEnRejilla();

        iniciarTemporizador();
    }

    private void redistribuirPiezasAhora() {
        // Limpiar la grilla de piezas
        rejillaPiezas.getChildren().clear();

        // Crear lista de piezas disponibles (no colocadas)
        List<PiezaRompecabezas> piezasDisponibles = new ArrayList<>();
        for (PiezaRompecabezas pieza : piezas) {
            if (!pieza.estaColocada()) {
                piezasDisponibles.add(pieza);
            }
        }

        // Calcular el tamaño de la grilla para mostrar las piezas disponibles
        int tamanoRejillaPiezas = Math.max((int) Math.ceil(Math.sqrt(piezasDisponibles.size())), tamanoRejilla);

        // Llenar la grilla de piezas
        int indicePieza = 0;

        for (int i = 0; i < tamanoRejillaPiezas && indicePieza < piezasDisponibles.size(); i++) {
            for (int j = 0; j < tamanoRejillaPiezas && indicePieza < piezasDisponibles.size(); j++) {
                PiezaRompecabezas pieza = piezasDisponibles.get(indicePieza);
                rejillaPiezas.add(pieza.obtenerVistaImagen(), j, i);
                indicePieza++;
            }
        }

        // Rellenar con algunas celdas vacías si es necesario
        if (piezasDisponibles.size() > 0 && piezasDisponibles.size() < tamanoRejillaPiezas * tamanoRejillaPiezas) {
            int maxCeldasVacias = Math.min(4, tamanoRejillaPiezas * tamanoRejillaPiezas - piezasDisponibles.size());
            for (int k = 0; k < maxCeldasVacias; k++) {
                int posicionTotal = piezasDisponibles.size() + k;
                int fila = posicionTotal / tamanoRejillaPiezas;
                int columna = posicionTotal % tamanoRejillaPiezas;
                if (fila < tamanoRejillaPiezas && columna < tamanoRejillaPiezas) {
                    StackPane celdaVacia = crearCeldaVacia();
                    rejillaPiezas.add(celdaVacia, columna, fila);
                }
            }
        }
    }

    private void redistribuirPiezasEnRejilla() {
        // Evitar operaciones pesadas durante la redistribución
        javafx.application.Platform.runLater(() -> redistribuirPiezasAhora());
    }

    private StackPane crearCeldaVacia() {
        StackPane celdaVacia = new StackPane();
        celdaVacia.setPrefSize(TAMANO_FICHA, TAMANO_FICHA);
        celdaVacia.setMaxSize(TAMANO_FICHA, TAMANO_FICHA);
        celdaVacia.setMinSize(TAMANO_FICHA, TAMANO_FICHA);
        celdaVacia.getStyleClass().add("celda-vacia");
        return celdaVacia;
    }

    private void colocarPieza(int idPieza, int filaObjetivo, int columnaObjetivo) {
        if (idPieza >= piezas.size()) return;

        PiezaRompecabezas pieza = piezas.get(idPieza);

        // Si la pieza ya está colocada, no hacer nada
        if (pieza.estaColocada()) return;

        // Verificar si la celda ya tiene una pieza
        if (rejillaObjetivo[filaObjetivo][columnaObjetivo] != null) {
            // Devolver la pieza anterior a la grilla de piezas
            ImageView piezaAnterior = rejillaObjetivo[filaObjetivo][columnaObjetivo];

            // Encontrar la pieza anterior y marcarla como no colocada
            for (PiezaRompecabezas p : piezas) {
                if (p.obtenerVistaImagen() == piezaAnterior) {
                    p.establecerColocada(false);
                    p.establecerPosicionActual(-1, -1);
                    break;
                }
            }

            // Limpiar la posición anterior
            rejillaObjetivo[filaObjetivo][columnaObjetivo] = null;
        }

        // Colocar la pieza en la grilla de armado
        StackPane celda = obtenerStackPaneEn(rejillaRompecabezas, filaObjetivo, columnaObjetivo);
        if (celda != null) {
            celda.getChildren().clear();
            celda.getChildren().add(pieza.obtenerVistaImagen());

            rejillaObjetivo[filaObjetivo][columnaObjetivo] = pieza.obtenerVistaImagen();
            pieza.establecerColocada(true);
            pieza.establecerPosicionActual(filaObjetivo, columnaObjetivo);

            movimientos++;
            etiquetaMovimientos.setText("👆🏼 Movimientos: " + movimientos);

            // Redistribuir las piezas restantes en la grilla (sin Platform.runLater aquí para evitar anidamiento)
            redistribuirPiezasAhora();

            // Verificar si el puzzle está resuelto de manera asíncrona para evitar bloqueos
            javafx.application.Platform.runLater(() -> {
                if (estaRompecabezasResuelto()) {
                    rompecabezasResuelto();
                }
            });
        }
    }

    private StackPane obtenerStackPaneEn(GridPane rejilla, int fila, int columna) {
        for (javafx.scene.Node nodo : rejilla.getChildren()) {
            if (GridPane.getRowIndex(nodo) != null && GridPane.getColumnIndex(nodo) != null) {
                if (GridPane.getRowIndex(nodo) == fila && GridPane.getColumnIndex(nodo) == columna) {
                    return (StackPane) nodo;
                }
            } else if (GridPane.getRowIndex(nodo) == null && GridPane.getColumnIndex(nodo) == null) {
                if (fila == 0 && columna == 0) {
                    return (StackPane) nodo;
                }
            } else if (GridPane.getRowIndex(nodo) == null && columna == 0 && fila == 0) {
                return (StackPane) nodo;
            } else if (GridPane.getColumnIndex(nodo) == null && fila == 0 && columna == 0) {
                return (StackPane) nodo;
            }
        }
        return null;
    }

    private boolean estaRompecabezasResuelto() {
        // Verificar que todas las piezas estén colocadas
        int piezasColocadas = 0;
        for (PiezaRompecabezas pieza : piezas) {
            if (!pieza.estaColocada()) {
                return false;
            }
            piezasColocadas++;
        }

        // Verificar que tenemos todas las piezas colocadas
        if (piezasColocadas != tamanoRejilla * tamanoRejilla) {
            return false;
        }

        // Verificar que cada pieza está en su posición correcta
        for (PiezaRompecabezas pieza : piezas) {
            int filaEsperada = pieza.obtenerPosicionOriginal() / tamanoRejilla;
            int columnaEsperada = pieza.obtenerPosicionOriginal() % tamanoRejilla;

            if (pieza.obtenerFilaActual() != filaEsperada || pieza.obtenerColumnaActual() != columnaEsperada) {
                return false;
            }
        }

        return true;
    }

    private void rompecabezasResuelto() {
        // Detener el temporizador inmediatamente
        if (lineaTiempo != null) {
            lineaTiempo.stop();
        }

        // Calcular el tiempo transcurrido
        long segundos = java.time.Duration.between(tiempoInicio, LocalDateTime.now()).getSeconds();
        String tiempo = String.format("%02d:%02d", segundos / 60, segundos % 60);

        // Crear y guardar el registro del juego
        RegistroJuego registro = new RegistroJuego(
                tiempoInicio.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                tamanoRejilla + "x" + tamanoRejilla,
                movimientos,
                tiempo
        );

        // Agregar al historial y guardar
        historial.add(registro);

        // Guardar historial de manera asíncrona
        javafx.application.Platform.runLater(() -> {
            try {
                guardarHistorial();
            } catch (Exception e) {
                System.err.println("Error al guardar historial: " + e.getMessage());
            }
        });

        // Mostrar mensaje de victoria
        mostrarAlerta("¡Wow! Pero mira esa maquina 🤤", String.format(
                "¡Terminaste de armar tu auto 🫡!\n\nTamaño 🤯: %dx%d\nMovimientos 👆🏼: %d\nTiempo ⏱️: %s",
                tamanoRejilla, tamanoRejilla, movimientos, tiempo));
    }

    private void iniciarTemporizador() {
        lineaTiempo = new Timeline(
                new KeyFrame(
                        Duration.seconds(1),
                        e -> {
                            long segundos = java.time.Duration.between(tiempoInicio, LocalDateTime.now()).getSeconds();
                            etiquetaTiempo.setText(String.format("Tiempo ⏱️: %02d:%02d", segundos / 60, segundos % 60));
                        }
                )
        );
        lineaTiempo.setCycleCount(Timeline.INDEFINITE);
        lineaTiempo.play();
    }

    // Clase para representar una pieza del rompecabezas
    private class PiezaRompecabezas {
        private int id;
        private int posicionOriginal;
        private ImageView vistaImagen;
        private boolean estaColocada = false;
        private int filaActual = -1, columnaActual = -1;

        public PiezaRompecabezas(int id, int posicionOriginal, Image imagen) {
            this.id = id;
            this.posicionOriginal = posicionOriginal;
            this.vistaImagen = new ImageView(imagen);
            this.vistaImagen.setFitWidth(TAMANO_FICHA);
            this.vistaImagen.setFitHeight(TAMANO_FICHA);
            this.vistaImagen.setPreserveRatio(false);
            this.vistaImagen.setSmooth(true);
            this.vistaImagen.getStyleClass().add("pieza-base");

            configurarArrastrarYSoltar();
            configurarManejadoresClick();
            configurarEfectosVisuales();
        }

        private void configurarArrastrarYSoltar() {
            // Configurar drag and drop
            this.vistaImagen.setOnDragDetected(e -> {
                // Solo permitir drag si la pieza no está colocada
                if (!estaColocada) {
                    javafx.scene.input.Dragboard tableroArrastrar = vistaImagen.startDragAndDrop(javafx.scene.input.TransferMode.MOVE);
                    javafx.scene.input.ClipboardContent contenido = new javafx.scene.input.ClipboardContent();
                    contenido.putString(String.valueOf(id));
                    tableroArrastrar.setContent(contenido);
                    tableroArrastrar.setDragView(vistaImagen.snapshot(null, null));
                }
                e.consume();
            });
        }

        private void configurarManejadoresClick() {
            // Doble clic para devolver a la grilla de piezas
            this.vistaImagen.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && estaColocada) {
                    regresarARejillaPiezas();
                }
            });
        }

        private void configurarEfectosVisuales() {
            // Efectos visuales con CSS
            this.vistaImagen.setOnMouseEntered(e -> {
                vistaImagen.getStyleClass().clear();
                if (!estaColocada) {
                    vistaImagen.getStyleClass().add("pieza-hover-disponible");
                } else {
                    vistaImagen.getStyleClass().add("pieza-hover-colocada");
                }
            });

            this.vistaImagen.setOnMouseExited(e -> {
                vistaImagen.getStyleClass().clear();
                vistaImagen.getStyleClass().add("pieza-base");
            });
        }

        private void regresarARejillaPiezas() {
            if (estaColocada) {
                // Limpiar la celda en la grilla de armado
                StackPane celda = obtenerStackPaneEn(rejillaRompecabezas, filaActual, columnaActual);
                if (celda != null) {
                    celda.getChildren().clear();
                }
                rejillaObjetivo[filaActual][columnaActual] = null;

                // Marcar como no colocada
                estaColocada = false;
                filaActual = -1;
                columnaActual = -1;

                // Redistribuir piezas
                redistribuirPiezasAhora();

                movimientos++;
                etiquetaMovimientos.setText("Movimientos 👆🏼: " + movimientos);
            }
        }

        // Getters y setters
        public int obtenerId() { return id; }
        public int obtenerPosicionOriginal() { return posicionOriginal; }
        public ImageView obtenerVistaImagen() { return vistaImagen; }
        public boolean estaColocada() { return estaColocada; }
        public void establecerColocada(boolean colocada) { this.estaColocada = colocada; }
        public int obtenerFilaActual() { return filaActual; }
        public int obtenerColumnaActual() { return columnaActual; }
        public void establecerPosicionActual(int fila, int columna) {
            this.filaActual = fila;
            this.columnaActual = columna;
        }
    }

    private void cargarHistorial() {
        try (BufferedReader lector = new BufferedReader(new FileReader(ARCHIVO_HISTORIAL))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                String[] partes = linea.split(",");
                if (partes.length == 4) {
                    historial.add(new RegistroJuego(partes[0], partes[1], Integer.parseInt(partes[2]), partes[3]));
                }
            }
        } catch (IOException e) {
            // Archivo no existe, se creará luego
        }
    }

    private void guardarHistorial() {
        try (BufferedWriter escritor = new BufferedWriter(new FileWriter(ARCHIVO_HISTORIAL))) {
            for (RegistroJuego registro : historial) {
                escritor.write(registro.aCSV());
                escritor.newLine();
            }
        } catch (IOException e) {
            mostrarAlerta("Error", "No se pudo guardar el historial.");
        }
    }

    private void mostrarHistorial() {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle("Historial de armado ⏳");
        alerta.setHeaderText("Registro de armados anteriores ⌛️");

        TextArea areaTexto = new TextArea();
        areaTexto.setEditable(false);
        areaTexto.setWrapText(true);

        StringBuilder constructorCadena = new StringBuilder();
        for (RegistroJuego registro : historial) {
            constructorCadena.append(registro).append("\n\n");
        }

        areaTexto.setText(constructorCadena.toString());
        alerta.getDialogPane().setContent(areaTexto);
        alerta.getDialogPane().setPrefSize(400, 300);

        // Aplicar estilo personalizado al diálogo
        DialogPane panelDialogo = alerta.getDialogPane();
        panelDialogo.getStyleClass().add("dialogo-personalizado");

        // Estilizar los botones del diálogo
        panelDialogo.lookupButton(ButtonType.OK).getStyleClass().add("boton-dialogo");

        alerta.showAndWait();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);

        // Aplicar estilo personalizado al diálogo
        DialogPane panelDialogo = alerta.getDialogPane();
        panelDialogo.getStyleClass().add("dialogo-personalizado");

        // Estilizar los botones del diálogo
        panelDialogo.lookupButton(ButtonType.OK).getStyleClass().add("boton-dialogo");

        alerta.showAndWait();
    }

    private static class RegistroJuego {
        String fecha;
        String dificultad;
        int movimientos;
        String tiempo;

        RegistroJuego(String fecha, String dificultad, int movimientos, String tiempo) {
            this.fecha = fecha;
            this.dificultad = dificultad;
            this.movimientos = movimientos;
            this.tiempo = tiempo;
        }

        @Override
        public String toString() {
            return String.format("Fecha 📆: %s\nDificultad 🤯: %s\nMovimientos 👆🏼: %d\nTiempo ⏱️: %s",
                    fecha, dificultad, movimientos, tiempo);
        }

        public String aCSV() {
            return String.format("%s,%s,%d,%s", fecha, dificultad, movimientos, tiempo);
        }
    }

    public static class Principal extends Application {
        @Override
        public void start(Stage escenarioPrincipal) {
            new Rompecabezas();
        }

        public static void main(String[] args) {
            launch(args);
        }
    }
}