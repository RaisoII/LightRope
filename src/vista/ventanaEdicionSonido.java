package vista;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import controlador.controlador;
import interfacesObserver.interfaceReproductorListener;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter; 

public class ventanaEdicionSonido extends Stage implements interfaceReproductorListener{

    private botonSonido botonAsociado;
    private controlador controlador;
    
    
    // parametros sonido
    private CheckBox checkBoxLoop;  
    private Button botonReproducir;
    private Slider sliderVolumen,sliderProgreso;
    private double duracionSegundos;
    private BorderPane root;
    private Label labelTiempoSonido;
    private TextField textFieldNombreArchivo;
    
 // Dentro de la clase ventanaEdicionSonido
    private List<String> tagsDisponibles; // La lista completa de tags que se carga desde afuera
    private List<String> tagsSeleccionados; // Los tags que el usuario eligió para este sonido
    private FlowPane flowPaneTagsDisponibles; // El panel que mostrará todos los tags
    private FlowPane flowPaneTagsSeleccionados; // El panel que mostrará solo los tags elegidos

    public ventanaEdicionSonido(botonSonido botonAsociado, controlador controlador, List<String> tagsDisponibles) {
       
    	tagsSeleccionados = new ArrayList<String>();
    	this.tagsDisponibles = tagsDisponibles;
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

        DecimalFormat df = new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.US));
        TextField textFieldFadeIn = new TextField(df.format(botonAsociado.getFadeIn()));
        TextField textFieldFadeOut = new TextField(df.format(botonAsociado.getFadeOut()));
        textFieldFadeIn.setPrefWidth(40);
        textFieldFadeOut.setPrefWidth(40);

        agregarListenersTextField(textFieldFadeIn, textFieldFadeOut);

        Label labelFadeIn = new Label("Fade In (s):");
        Label labelFadeOut = new Label("Fade Out (s):");
        labelTiempoSonido = new Label("00:00:00 / " + formatearDuracion(duracionSegundos));

        HBox panelProgreso = new HBox(10, sliderProgreso, labelTiempoSonido);
        panelProgreso.setPadding(new Insets(10, 0, 10, 0)); // Padding para este panel

        HBox panelFadeIn = new HBox(10, labelFadeIn, textFieldFadeIn);
        panelFadeIn.setPadding(new Insets(0));

        HBox panelFadeOut = new HBox(10, labelFadeOut, textFieldFadeOut);
        panelFadeOut.setPadding(new Insets(0));

        HBox panelNorte = new HBox(10, botonReproducir, checkBoxLoop);
        panelNorte.setPadding(new Insets(10, 0, 0, 10)); // Padding para este panel

        HBox panelVolumen = new HBox(10, new Label("Volumen:"), sliderVolumen);
        panelVolumen.setPadding(new Insets(0));

        // VBox que contiene los controles de volumen, progreso y fades
        VBox panelControlesAudio = new VBox(10, panelVolumen, panelProgreso, panelFadeIn, panelFadeOut);
        panelControlesAudio.setPadding(new Insets(10));
        
        // VBox que contiene el panel de selección de tags.
        // Llama a la función que crea el panel de tags (con CheckBoxes y la visualización)
        VBox panelTags = crearPanelSelectorTags();
        panelTags.setPadding(new Insets(10));

        // Un único VBox para contener todo el contenido central de la ventana
        VBox contenidoCentral = new VBox(20); // Un espacio más grande para separar secciones
        contenidoCentral.getChildren().addAll(panelControlesAudio, panelTags);
        
        // Crear el ScrollPane y asignarle el VBox con todo el contenido central
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(contenidoCentral);
        scrollPane.setFitToWidth(true); // Hace que el contenido se ajuste al ancho del ScrollPane
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Configurar el layout principal de la ventana
        root = new BorderPane();
        
        // Panel superior con el nombre del archivo y controles de reproducción
        VBox panelSuperior = new VBox(10, textFieldNombreArchivo, panelNorte);
        panelSuperior.setPadding(new Insets(10));
        root.setTop(panelSuperior);
        
        // Asignar el ScrollPane (con todo el contenido) al centro
        root.setCenter(scrollPane);
    }
    
    private void configurarEscena() {
    	Scene scene = new Scene(root, 500, 400);
        setScene(scene);
        show();
        Platform.runLater(() -> textFieldNombreArchivo.getParent().requestFocus());
    }
    
    private void configurarCierreVentana() {
    	setOnCloseRequest(e ->{ 
    		 botonAsociado.setListaTagVentana(new ArrayList<>(tagsSeleccionados),tagsDisponibles);
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
    	
    	sliderProgreso = new Slider(0, duracionSegundos, 0);
        sliderProgreso.setShowTickLabels(true);
        sliderProgreso.setShowTickMarks(true);
        sliderProgreso.setMajorTickUnit(10);
        sliderProgreso.setMinorTickCount(0);
        sliderProgreso.setPrefWidth(350);
        sliderProgreso.setDisable(!botonAsociado.getBotonApretado());
        
        
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
        
        // Método para detectar clicks en thumb, ticks o labels
        EventHandler<MouseEvent> bloquearClicksTicks = e -> {
            if (isClickEnThumbOTicks(e)) {
                e.consume();
            }
        };

        // Usamos filtros para interceptar el evento antes que el slider lo procese
        sliderProgreso.addEventFilter(MouseEvent.MOUSE_PRESSED, bloquearClicksTicks);
        sliderProgreso.addEventFilter(MouseEvent.MOUSE_RELEASED, bloquearClicksTicks);
        sliderProgreso.addEventFilter(MouseEvent.MOUSE_CLICKED, bloquearClicksTicks);

        // Opcional: bloquear clicks en la pista para evitar mover slider desde ahí
        Node track = sliderProgreso.lookup(".track");
        if (track != null) {
            track.addEventFilter(MouseEvent.ANY, e -> e.consume());
        }

        // Tu lógica previa, adaptada
        sliderProgreso.setOnMousePressed(e -> {
            if (!isClickEnThumbOTicks(e)) {
                controlador.deleteObserver(this);
            }
        });

        sliderProgreso.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (sliderProgreso.isValueChanging()) {
                controlador.deleteObserver(this);
            }
        });

        sliderProgreso.setOnMouseReleased(e -> {
            boolean fueArrastre = sliderProgreso.isValueChanging();

            if (!fueArrastre)
            {
                double posicion = (e.getX() / sliderProgreso.getWidth()) * duracionSegundos;
                sliderProgreso.setValue(posicion);
            }

            actualizarAudio();
        });
    }

    // El método mejorado para detectar clicks en thumb, tick, label o axis
    private boolean isClickEnThumbOTicks(MouseEvent e) {
        Node target = (Node) e.getTarget();

        while (target != null) {
            var styles = target.getStyleClass();
            if (styles.contains("tick-mark") || styles.contains("tick-label") || styles.contains("axis")) {
                return true;
            }
            target = target.getParent();
        }
        return false;
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
    
    private void crearCampoNombreEditable()
    {
        textFieldNombreArchivo = new TextField(botonAsociado.getNombreArchivo());
        textFieldNombreArchivo.setPrefWidth(300);
        textFieldNombreArchivo.setOnAction(e -> {
            String nuevoNombre = textFieldNombreArchivo.getText().trim();
            if (!nuevoNombre.isEmpty()) {
            	botonAsociado.setNombreLabel(nuevoNombre);
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
	}
	
	private VBox crearPanelSelectorTags() {
	    
		// Panel para los tags seleccionados
	    Label tituloSeleccionados = new Label("Tags seleccionados:");
	    tituloSeleccionados.setStyle("-fx-font-weight: bold;");
	    
	    flowPaneTagsSeleccionados = new FlowPane(5, 5); // Espacio entre tags
	    flowPaneTagsSeleccionados.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-border-radius: 5;");
	    
	    // Panel para todos los tags disponibles (el selector)
	    Label tituloDisponibles = new Label("Tags disponibles:");
	    tituloDisponibles.setStyle("-fx-font-weight: bold;");
	    
	    flowPaneTagsDisponibles = new FlowPane(5, 5);
	    flowPaneTagsDisponibles.setPadding(new Insets(10));
	    flowPaneTagsDisponibles.setStyle("-fx-border-color: lightgray; -fx-border-width: 1; -fx-border-radius: 5;");
	   
	    tagsSeleccionados = botonAsociado.getTags();
	    
	    // Llenar el panel con todos los tags como CheckBoxes
	    for (String tag : tagsDisponibles) {
	        CheckBox checkBoxTag = new CheckBox(tag);

	        checkBoxTag.setSelected(tagsSeleccionados.contains(tag));
	        
	        checkBoxTag.setOnAction(e -> {
	            if (checkBoxTag.isSelected()) {
	                tagsSeleccionados.add(tag);
	            } else {
	                tagsSeleccionados.remove(tag);
	            }
	            // Después de cada cambio, refrescamos la visualización
	            refrescarVisualizacionTags();
	        });
	        
	        flowPaneTagsDisponibles.getChildren().add(checkBoxTag);
	    }
	    
	    // Al iniciar, refrescamos para mostrar los tags iniciales
	    refrescarVisualizacionTags();
	    
	    botonAsociado.setListaTagVentana(tagsSeleccionados,tagsDisponibles);
	    
	    return new VBox(10, tituloSeleccionados, flowPaneTagsSeleccionados, tituloDisponibles, flowPaneTagsDisponibles);
	}
	
	private void refrescarVisualizacionTags() {
	    flowPaneTagsSeleccionados.getChildren().clear(); // Limpiamos para redibujar
	   
	    for (String tag : tagsSeleccionados) {
	    	boolean isTagGlobal = tagsDisponibles.contains(tag);
	        HBox tagBox = crearTagItem(tag, isTagGlobal);
	        flowPaneTagsSeleccionados.getChildren().add(tagBox);
	    }
	}
	
	
	private HBox crearTagItem(String tag, boolean isTagGlobal) {
	    
		HBox tagBox = new HBox(3);
	    tagBox.setAlignment(Pos.CENTER_LEFT);
	    
	    String colorFondo = isTagGlobal ? "#3870b2" : "#bbbbbb";  // azul o gris claro

	    tagBox.setStyle("-fx-background-color: " + colorFondo + 
                "; -fx-background-radius: 3; -fx-padding: 3 5 3 5;");

	    Label tagLabel = new Label(tag);
	    tagLabel.setStyle("-fx-text-fill: white; -fx-font-size: 11px;");
	    
	    tagBox.getChildren().add(tagLabel);
	    
	    if (!isTagGlobal) {
	        Button closeButton = new Button("✕");
	        closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-padding: 0; -fx-font-size: 8px;");
	        
	        closeButton.setOnAction(e -> {
	            // El cambio clave está aquí: eliminamos de la lista de seleccionados
	            this.tagsSeleccionados.remove(tag);
	            botonAsociado.borrarTagBoton(tag);
	            refrescarVisualizacionTags();
	        });
	        
	        tagBox.getChildren().add(closeButton);
	    }
	    
	    return tagBox;
	}
	
	public void checkearTags(List<String> listaTagsNueva) {
	    // Usamos un Set para búsquedas más eficientes (O(1) en lugar de O(n))
	    Set<String> tagsValidos = new HashSet<>(listaTagsNueva);
	    
	    // --- PASO 1: Actualizar los tags seleccionados ---
	    // Eliminar de 'tagsSeleccionados' cualquier tag que ya no esté en la nueva lista de tags válidos
	    tagsSeleccionados.removeIf(tag -> !tagsValidos.contains(tag));
	    
	    // --- PASO 2: Reconstruir el panel de tags disponibles ---
	    // Esto es más robusto que intentar eliminar nodos individualmente
	    flowPaneTagsDisponibles.getChildren().clear();
	    
	    // Recorrer la nueva lista de tags válidos y crear los CheckBoxes
	    for (String tag : listaTagsNueva) {
	        CheckBox checkBoxTag = new CheckBox(tag);

	        // Marcar el CheckBox si el tag estaba previamente seleccionado
	        checkBoxTag.setSelected(tagsSeleccionados.contains(tag));
	        
	        checkBoxTag.setOnAction(e -> {
	            if (checkBoxTag.isSelected()) {
	                tagsSeleccionados.add(tag);
	            } else {
	                tagsSeleccionados.remove(tag);
	            }
	            refrescarVisualizacionTags();
	        });
	        
	        flowPaneTagsDisponibles.getChildren().add(checkBoxTag);
	    }
	    
	    // --- PASO 3: Refrescar la visualización de los tags seleccionados ---
	    // Esto es importante para que el panel de tags seleccionados también se actualice
	    // si se eliminó algún tag del paso 1.
	    refrescarVisualizacionTags();
	}
	
	public void setLoopBox(boolean loop) 
	{
		checkBoxLoop.setSelected(loop);
	}
}
