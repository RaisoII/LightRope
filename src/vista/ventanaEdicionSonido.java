package vista;

import controlador.controlador;

import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ventanaEdicionSonido extends Stage{

    private botonSonido botonAsociado;
    private controlador controlador;
    
    // parametros sonido
    private CheckBox checkBoxLoop;  
    private Button botonReproducir;
    private Slider sliderVolumen;

    public ventanaEdicionSonido(botonSonido botonAsociado, controlador controlador) {
        this.botonAsociado = botonAsociado;
        this.controlador = controlador;
        controlador.entrarModoEdicion(botonAsociado.getDatosLectura());
        inicializarComponentes();
    }

    public void inicializarComponentes() {
        
    	setTitle(botonAsociado.getNombreArchivo());
    	float duracionSegundos =  botonAsociado.getDuracion();
        //crear play
        botonReproducir = new Button("Play");
        agregarListenerBotonReproducir(botonAsociado.getBotonAsociado());
        
        // Crear loop
        checkBoxLoop = new CheckBox("Loop");
        checkBoxLoop.setSelected(botonAsociado.getLoop());
        checkBoxLoop.setOnAction(e -> listenerLoop());
        
        // Slider de volumen
        sliderVolumen = new Slider(0, 100, 100); // Rango de 0 a 100, valor inicial 100
        sliderVolumen.setShowTickLabels(true);
        sliderVolumen.setShowTickMarks(true);
        sliderVolumen.setMajorTickUnit(20.0); // Cada 20 unidades, habrá una marca principal con etiqueta
        sliderVolumen.setMinorTickCount(1);  // Añade 4 marcas menores entre cada marca principal
        sliderVolumen.setBlockIncrement(5.0); // Mueve el slider en incrementos de 5 al usar el teclado o clicks en el área de desplazamiento
        sliderVolumen.setValue(botonAsociado.getVolumen() * 100);
        sliderVolumen.valueProperty().addListener((observable, oldValue, newValue) -> {
            manejarCambioDeVolumenControlador(newValue.doubleValue() / 100f); 
        });// Llama a una función para manejar el cambio constante 
        
        sliderVolumen.setOnMouseReleased(event -> {
            guardarValorFinalVolumen(); //cuando se suelta la "bolita"
        });
        
     // Slider de progreso de la canción
        Slider sliderProgreso = new Slider(0, duracionSegundos, 0); // ahora el tope es la duración real
        sliderProgreso.setShowTickLabels(true);
        sliderProgreso.setShowTickMarks(true);

        // Elegís un salto cómodo. Por ejemplo, cada 10 segundos:
        sliderProgreso.setMajorTickUnit(1);
        sliderProgreso.setMinorTickCount(0); // o 1 si querés mini-marcas
        sliderProgreso.setBlockIncrement(1);
        sliderProgreso.setPrefWidth(300);
        sliderProgreso.setDisable(true); // hasta que se reproduzca
        
     // TextFields para Fade In y Fade Out
        TextField textFieldFadeIn = new TextField("0");
        TextField textFieldFadeOut = new TextField("0");
        
        // Establecer ancho preferido para los campos
        textFieldFadeIn.setPrefWidth(30);
        textFieldFadeOut.setPrefWidth(30);
        agregarListenersTextField(textFieldFadeIn,textFieldFadeOut);

        // Etiquetas para los campos de Fade In y Fade Out
        Label labelFadeIn = new Label("Fade In (s):");
        Label labelFadeOut = new Label("Fade Out (s):");
        Label labelDuracion = new Label(formatearDuracion(duracionSegundos)); // su duración en segundos
        
        HBox panelProgreso = new HBox(10, sliderProgreso, labelDuracion);
        panelProgreso.setPadding(new Insets(10));
      
        //creacion paneles
        
     // Creación de los paneles de Fade In y Fade Out
        HBox panelFadeIn = new HBox(10, labelFadeIn, textFieldFadeIn);
        panelFadeIn.setPadding(new Insets(10));

        HBox panelFadeOut = new HBox(10, labelFadeOut, textFieldFadeOut);
        panelFadeOut.setPadding(new Insets(10));

        
        HBox panelNorte = new HBox(10, botonReproducir, checkBoxLoop);
        panelNorte.setPadding(new Insets(10));

        HBox panelVolumen = new HBox(10, new Label("Volumen:"), sliderVolumen);
        panelVolumen.setPadding(new Insets(10));
        

        VBox panelCentro = new VBox(10, panelVolumen,panelProgreso, panelFadeIn,panelFadeOut);
        panelCentro.setPadding(new Insets(10));
        BorderPane root = new BorderPane();
        root.setTop(panelNorte);
        root.setCenter(panelCentro);
        
        // activar ventana
        Scene scene = new Scene(root, 400, 300);
        setScene(scene);
        show();

        // Configurar el cierre de la ventana
        setOnCloseRequest(e -> botonAsociado.setVentanaEdicion(null));
    }

    private void agregarListenersTextField(TextField textFieldFadeIn, TextField textFieldFadeOut) {
    	 textFieldFadeIn.textProperty().addListener((observable, oldValue, newValue) -> {
    	        try {
    	            int fadeInValue = Integer.parseInt(newValue);
    	            botonAsociado.setFadeIn(fadeInValue);
    	        } catch (NumberFormatException e) {
    	            // Manejar el caso en que el texto no sea un número válido
    	            System.err.println("Error: El valor de Fade In debe ser un número entero.");
    	        }
    	    });
    	 
    	  textFieldFadeOut.textProperty().addListener((observable, oldValue, newValue) -> {
    	        try {
    	            int fadeOutValue = Integer.parseInt(newValue);
    	            botonAsociado.setFadeIn(fadeOutValue);
    	        } catch (NumberFormatException e) {
    	            // Manejar el caso en que el texto no sea un número válido
    	            System.err.println("Error: El valor de Fade Out debe ser un número entero.");
    	        }
    	    });
	}
    
    private String formatearDuracion(float segundosTotales) {
        int horas = (int) (segundosTotales / 3600);
        int minutos = (int) ((segundosTotales % 3600) / 60);
        int segundos = (int) (segundosTotales % 60);
        return String.format("%02d:%02d:%02d", horas, minutos, segundos);
    }
    

	private void agregarListenerBotonReproducir(Button botonOriginal) {
    	//l a funcionalidad del boton que hace abrir esta ventana
    	 EventHandler<ActionEvent> handler = botonOriginal.getOnAction();
    	 botonReproducir.setOnAction(handler);
    }
    
    private void listenerLoop() 
    {
    	boolean loop = checkBoxLoop.isSelected();
        botonAsociado.setLoop(loop);
        controlador.setLoopReproduccion(botonAsociado.getNombreArchivo(),loop);
    }
    
    private void manejarCambioDeVolumenControlador(double valor) 
    {
    	 controlador.setVolumenReproduccion(botonAsociado.getNombreArchivo(),valor);	
    }
    
    private void guardarValorFinalVolumen() 
    {
    	botonAsociado.setVolumen(sliderVolumen.getValue() / 100f);
    }
}
