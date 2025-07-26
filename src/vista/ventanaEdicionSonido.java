package vista;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import controlador.controlador;
import interfacesObserver.interfaceReproductorListener;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter; 

public class ventanaEdicionSonido extends Stage implements interfaceReproductorListener{

    private botonSonido botonAsociado;
    private controlador controlador;
    private boolean usuarioMovioSlider,dragPermitido;
    
    // parametros sonido
    private CheckBox checkBoxLoop;  
    private Button botonReproducir;
    private Slider sliderVolumen,sliderProgreso;
    private double duracionSegundos;
    private BorderPane root;
    private Label labelTiempoSonido;
    private TextField textFieldNombreArchivo;

    public ventanaEdicionSonido(botonSonido botonAsociado, controlador controlador) {
        this.botonAsociado = botonAsociado;
        this.controlador = controlador;
        controlador.setObserver(this);
        inicializarComponentes();
    }

    public void inicializarComponentes() {
        
    	setTitle(botonAsociado.getNombreArchivo());
    	duracionSegundos =  botonAsociado.getDuracion();
    	
    	crearComponentesUI();
    	configurarLayout();  
    	configurarEscena();
    	configurarCierreVentana();
     }
    
    private void crearComponentesUI() {
    	crearBotonPlay();
        crearSliderVolumen();
        crearSliderProgreso();
        crearBotonLoop();
        crearCampoNombreEditable();
    }
    
    private void configurarLayout() {
    
    	DecimalFormat df = new DecimalFormat("0.##",DecimalFormatSymbols.getInstance(Locale.US));
    	TextField textFieldFadeIn = new TextField(df.format(botonAsociado.getFadeIn()));
    	TextField textFieldFadeOut = new TextField(df.format(botonAsociado.getFadeOut()));
    	textFieldFadeIn.setPrefWidth(40);
        textFieldFadeOut.setPrefWidth(40);

        agregarListenersTextField(textFieldFadeIn, textFieldFadeOut);

        Label labelFadeIn = new Label("Fade In (s):");
        Label labelFadeOut = new Label("Fade Out (s):");
        labelTiempoSonido = new Label("00:00:00 / "+ formatearDuracion(duracionSegundos));

        HBox panelProgreso = new HBox(10, sliderProgreso, labelTiempoSonido);
        panelProgreso.setPadding(new Insets(10));

        HBox panelFadeIn = new HBox(10, labelFadeIn, textFieldFadeIn);
        panelFadeIn.setPadding(new Insets(10));

        HBox panelFadeOut = new HBox(10, labelFadeOut, textFieldFadeOut);
        panelFadeOut.setPadding(new Insets(10));

        HBox panelNorte = new HBox(10, botonReproducir, checkBoxLoop);
        panelNorte.setPadding(new Insets(10));

        HBox panelVolumen = new HBox(10, new Label("Volumen:"), sliderVolumen);
        panelVolumen.setPadding(new Insets(10));

        VBox panelCentro = new VBox(10, panelVolumen, panelProgreso, panelFadeIn, panelFadeOut);
        panelCentro.setPadding(new Insets(10));

        
        root = new BorderPane();
        
        VBox panelSuperior = new VBox(10, textFieldNombreArchivo, panelNorte);
        panelSuperior.setPadding(new Insets(10));
        root.setTop(panelSuperior);
        root.setCenter(panelCentro);
    }
    
    private void configurarEscena() {
    	Scene scene = new Scene(root, 500, 400);
        setScene(scene);
        show();
        Platform.runLater(() -> textFieldNombreArchivo.getParent().requestFocus());
    }
    
    private void configurarCierreVentana() {
    	 setOnCloseRequest(e ->{ 
    		 // Actualizar nombre si fue editado
    	        String nuevoNombre = textFieldNombreArchivo.getText().trim();
    	        if (!nuevoNombre.isEmpty()) {
    	            botonAsociado.setNombreArchivo(nuevoNombre);
    	            botonAsociado.getBotonAsociado().setText(nuevoNombre); // actualiza el texto del botón
    	        }
    		 botonAsociado.setVentanaEdicion(null);
    	 });
    }
    
    private void crearBotonPlay() {
    	
    	botonReproducir = new Button("Play");
        agregarListenerBotonReproducir(botonAsociado.getBotonAsociado());
    }
    
    private void crearBotonLoop() {
    	checkBoxLoop = new CheckBox("Loop");
        checkBoxLoop.setSelected(botonAsociado.getLoop());
        checkBoxLoop.setOnAction(e -> listenerLoop());
    }
    
    private void crearSliderProgreso() {
    	
    	usuarioMovioSlider = false;
    	dragPermitido = false;
    	// Slider de progreso de la canción
        sliderProgreso = new Slider(0, duracionSegundos, 0); // ahora el tope es la duración real
        sliderProgreso.setShowTickLabels(true);
        sliderProgreso.setShowTickMarks(true);

        // Elegís un salto cómodo. Por ejemplo, cada 10 segundos:
        sliderProgreso.setMajorTickUnit(10);
        sliderProgreso.setMinorTickCount(0); // o 1 si querés mini-marcas
        sliderProgreso.setBlockIncrement(1);
        sliderProgreso.setPrefWidth(350);
        sliderProgreso.setDisable(!botonAsociado.getBotonApretado()); // hasta que se reproduzca
        
        sliderProgreso.setOnMousePressed(e -> {
            
            controlador.deleteObserver(this);

            double y = e.getY();
            double height = sliderProgreso.getHeight();

            // Solo permitimos drag si el clic fue dentro de la zona de la barra
            dragPermitido = y > 4 && y < height - 26;
        });
       
        sliderProgreso.setOnMouseReleased(e -> {
        	
        	//System.out.println(dragPermitido + " , "+usuarioMovioSlider);
            if (dragPermitido || usuarioMovioSlider) {
            	actualizarAudio();
            }
            
            usuarioMovioSlider = false;
        });
        
     // Este bloque detecta si el usuario hace clic sobre la bolita (thumb) directamente
        Platform.runLater(() -> {
            Node thumb = sliderProgreso.lookup(".thumb");
                thumb.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
                    usuarioMovioSlider = true;
                    dragPermitido = true;
                    //System.out.println("🎯 Clic directo sobre la bolita (lookup)");
                });
        });
        
        
        sliderProgreso.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (dragPermitido && !oldVal.equals(newVal)) {
                usuarioMovioSlider = true;
            }
        });
       
        sliderProgreso.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double valor) {
                int totalSeconds = valor.intValue();
                int minutos = totalSeconds / 60;
                int segundos = totalSeconds % 60;
                return String.format("%02d:%02d", minutos, segundos); // Formato 00:00
            }
            
	        @Override
	        public Double fromString(String string) {
	            return 0.0; // No se necesita convertir de vuelta, solo mostramos el formato
	        }
        });
    }
    
    private void crearSliderVolumen() {
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
    }
   

    private void agregarListenersTextField(TextField textFieldFadeIn, TextField textFieldFadeOut) {
    	 textFieldFadeIn.textProperty().addListener((observable, oldValue, newValue) -> {
    	        try {
    	             double fadeInValue = Double.parseDouble(newValue);
    	             botonAsociado.setFadeIn(fadeInValue);
    	        } catch (NumberFormatException e) {
    	            // Manejar el caso en que el texto no sea un número válido
    	            System.err.println("Error: El valor de Fade In debe ser un número entero.");
    	        }
    	    });
    	 
    	  textFieldFadeOut.textProperty().addListener((observable, oldValue, newValue) -> {
    	        try {
    	             double fadeOutValue = Double.parseDouble(newValue);
    	             botonAsociado.setFadeOut(fadeOutValue);
    	        } catch (NumberFormatException e) {
    	            // Manejar el caso en que el texto no sea un número válido
    	            System.err.println("Error: El valor de Fade Out debe ser un número entero.");
    	        }
    	    });
	}
    
    private void crearCampoNombreEditable() {
        textFieldNombreArchivo = new TextField(botonAsociado.getNombreArchivo());
        textFieldNombreArchivo.setPrefWidth(300);
        textFieldNombreArchivo.setOnAction(e -> {
            String nuevoNombre = textFieldNombreArchivo.getText().trim();
            if (!nuevoNombre.isEmpty()) {
                botonAsociado.setNombreArchivo(nuevoNombre);
                setTitle(nuevoNombre); // también actualiza el título de la ventana
            }
        });
    }
    
    private String formatearDuracion(double segundosTotales) {
    	
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
        controlador.setLoopReproduccion(botonAsociado.getIdBoton(),loop);
    }
    
    private void manejarCambioDeVolumenControlador(double valor) 
    {
    	 controlador.setVolumenReproduccion(botonAsociado.getIdBoton(),valor);	
    }
    
    private void guardarValorFinalVolumen() 
    {
    	botonAsociado.setVolumen(sliderVolumen.getValue() / 100f);
    }

	@Override
	public void onReproduccionTerminada(int idBoton) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void avanceReproduccion(int idBoton,double avance) {
		
		if(idBoton != botonAsociado.getIdBoton())
				return;
		
		sliderProgreso.setValue(avance);
		int horas = (int) (avance / 3600);
		int minutos = (int) ((avance % 3600) / 60);
		int segundos = (int) (avance % 60);
		 
	    String tiempoFormateado = String.format("%02d:%02d:%02d",horas, minutos, segundos) + " / "+ formatearDuracion(duracionSegundos);
	    labelTiempoSonido.setText(tiempoFormateado);
	}
	
	public void setEnabledSliderProgreso(boolean estaReproduciendo) 
	{
		sliderProgreso.setDisable(!estaReproduciendo);
	}
	
	public void actualizarAudio() 
	{
		float segundos = (float) sliderProgreso.getValue();
		controlador.actualizarAudio(botonAsociado.getDatosLectura(), segundos);
		controlador.setObserver(this);
		//PauseTransition delay = new PauseTransition(Duration.millis(100f));
	    //delay.setOnFinished(e -> controlador.setObserver(this));
	    //delay.play();
	}
}
