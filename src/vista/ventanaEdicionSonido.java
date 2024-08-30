package vista;

import controlador.controlador;

import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class ventanaEdicionSonido extends Stage{

    private botonSonido botonAsociado;
    private controlador controlador;
    private CheckBox checkBoxLoop;  // CheckBox para activar o desactivar loop
    private Button botonReproducir;

    public ventanaEdicionSonido(botonSonido botonAsociado, controlador controlador) {
        this.botonAsociado = botonAsociado;
        this.controlador = controlador;
        inicializarComponentes();
    }

    public void inicializarComponentes() {
        setTitle(botonAsociado.getNombreArchivo());

        // Crear los componentes
        checkBoxLoop = new CheckBox("Loop");
        botonReproducir = new Button("Play");
        checkBoxLoop.setSelected(botonAsociado.getLoop());

        // Configurar eventos
        checkBoxLoop.setOnAction(e -> listenerLoop());
        agregarListenerBotonReproducir(botonAsociado.getBotonAsociado());

        // Crear contenedores
        HBox panelNorte = new HBox(10);
        panelNorte.getChildren().addAll(botonReproducir, checkBoxLoop);

        BorderPane root = new BorderPane();
        root.setTop(panelNorte);

        Scene scene = new Scene(root, 400, 300);
        setScene(scene);
        show();

        // Configurar el cierre de la ventana
        setOnCloseRequest(e -> botonAsociado.setVentanaEdicion(null));
    }

    private void agregarListenerBotonReproducir(Button botonOriginal) {
        botonOriginal.setOnAction(e -> botonReproducir.fire());
    }

    private void listenerLoop() {
        botonAsociado.setLoop(checkBoxLoop.isSelected());
    }
}
