package vista;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.*;


public class botonFactory {

    public static botonSonido crearBoton(String ruta, String nombreCancion, int idBoton,
                                          double duracion, EventHandler<ActionEvent> handler,
                                          TilePane panelBotones) {
        Button botonFx = new Button();
        botonFx.setPrefSize(80, 80);
        botonFx.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-border-width: 0;");
        botonFx.setOnAction(handler);

        StackPane contenedorConBorde = new StackPane(botonFx);
        contenedorConBorde.setPrefSize(80, 80);
        contenedorConBorde.setStyle("-fx-border-color: black; -fx-border-width: 2; -fx-background-color: transparent;");

        Label nombreLabel = crearLabel(nombreCancion);
        TextField textField = crearTextField(nombreCancion);

        botonSonido boton = new botonSonido(ruta, nombreCancion, idBoton, duracion, botonFx, nombreLabel);
        boton.setBordeBoton(contenedorConBorde);

        configurarListenersEstilo(boton);

        StackPane pilaNombre = new StackPane(nombreLabel, textField);
        pilaNombre.setPrefSize(80, 18);

        VBox contenedor = new VBox(5, contenedorConBorde, pilaNombre);
        contenedor.setAlignment(Pos.CENTER);
        contenedor.setPrefWidth(80);
        boton.setContenedor(contenedor);

        panelBotones.getChildren().add(contenedor);
        
        alternarEdicion(nombreLabel, textField,boton);
        
        return boton;
    }

    private static Label crearLabel(String texto) {
        Label label = new Label(texto);
        label.setPrefSize(80, 18);
        label.setWrapText(false);
        label.setTextOverrun(OverrunStyle.ELLIPSIS);
        label.setTooltip(new Tooltip(texto));
        label.setStyle("-fx-font-size: 10px; -fx-text-fill: black; -fx-alignment: center;");
        return label;
    }

    private static TextField crearTextField(String texto) {
        TextField field = new TextField(texto);
        field.setPrefSize(80, 18);
        field.setVisible(false);
        field.setManaged(false);
        field.setStyle("-fx-font-size: 10px; -fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 0; -fx-text-fill: black; -fx-alignment: center;");
        return field;
    }

    private static void alternarEdicion(Label label, TextField field,botonSonido boton) {
        label.setOnMouseClicked(e -> {
            field.setText(label.getText());
            label.setVisible(false);
            label.setManaged(false);
            field.setVisible(true);
            field.setManaged(true);
            field.requestFocus();
            field.selectAll();
        });

        Runnable confirmarCambio = () -> {
            String nuevoTexto = field.getText().trim();
            if (!nuevoTexto.isEmpty()) {
                label.setText(nuevoTexto);
                label.setTooltip(new Tooltip(nuevoTexto));
                boton.setNombreArchivo(nuevoTexto);
            }
            field.setVisible(false);
            field.setManaged(false);
            label.setVisible(true);
            label.setManaged(true);
        };

        field.setOnAction(e -> confirmarCambio.run());
        field.focusedProperty().addListener((obs, oldV, newV) -> {
            if (!newV) confirmarCambio.run();
        });
    }

    private static void configurarListenersEstilo(botonSonido boton) {
        Button botonFx = boton.getBotonAsociado();
        StackPane borde = boton.getBordeBoton();

        Runnable aplicarEstilo = () -> {
            if (boton.getBotonApretado()) {
                borde.setStyle("-fx-border-color: yellow; -fx-border-width: 2;");
            } else if (botonFx.isFocused()) {
                borde.setStyle("-fx-border-color: green; -fx-border-width: 2;");
            } else if (botonFx.isHover()) {
                borde.setStyle("-fx-border-color: dodgerblue; -fx-border-width: 2;");
            } else {
                borde.setStyle("-fx-border-color: black; -fx-border-width: 2;");
            }
        };

        botonFx.hoverProperty().addListener((obs, o, n) -> aplicarEstilo.run());
        botonFx.focusedProperty().addListener((obs, o, n) -> aplicarEstilo.run());
        aplicarEstilo.run();
    }
}

