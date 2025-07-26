package vista;

import controlador.controlador;
import interfacesObserver.interfaceReproductorListener;
import archivosSoloLectura.datosSonidoLectura;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.stage.Stage;

public class vista implements interfaceReproductorListener{
    
    private controlador controlador;
    private Map<String,botonSonido> mapaBotonesSonido;
    private botonSonido botonActualSeleccionado;
    
    // Elementos de la interfaz
	private VBox root;
    private FileChooser buscadorArchivos;
    private MenuItem openItem,saveItem,loadItem;
    private FlowPane panelBotones;
    ScrollPane scrollBotones;
    
    private MenuBar menuBar = new MenuBar();
    
    public vista() {
    
    	mapaBotonesSonido = new HashMap<String,botonSonido>();
        crearInterface();
    }
    
    private void crearInterface() {
        // Crear el menú "File"
        Menu fileMenu = new Menu("File");

        // Crear los elementos del menú
        openItem = new MenuItem("Open");
        saveItem = new MenuItem("Save");
        loadItem = new MenuItem("Load");
        
        saveItem.setDisable(true);

        // Agregar los elementos al menú "File"
        fileMenu.getItems().addAll(openItem,saveItem,loadItem);

        // Agregar el menú "File" a la barra de menú
        menuBar.getMenus().add(fileMenu);

        // Configurar el layout principal
        BorderPane borderPane = new BorderPane();
        borderPane.setTop(menuBar); // Colocar la barra de menú en la parte superior
        
        buscadorArchivos = new FileChooser();
        // Configurar el VBox para que use el BorderPane
        
        panelBotones = new FlowPane();
        panelBotones.setHgap(10);  // Espacio horizontal entre botones
        panelBotones.setVgap(10);  // Espacio vertical entre filas
        panelBotones.setPrefWrapLength(400);  // Ancho preferido del FlowPane antes de ajustar a la siguiente línea
        panelBotones.setStyle("-fx-background: transparent; -fx-border-color: transparent;");
        // Inicializar ScrollPane con FlowPane
        ScrollPane scrollBotones = new ScrollPane(panelBotones);
        scrollBotones.setFitToWidth(true);  // Ajustar el contenido al ancho del ScrollPane
 
        // Remover bordes visibles del ScrollPane
        //scrollBotones.setStyle("-fx-background: transparent; -fx-border-color: transparent;");

        // Configurar el ScrollPane para expandirse y ocupar todo el espacio disponible
        VBox.setVgrow(scrollBotones, Priority.ALWAYS);

        // Crear BorderPane y agregar el ScrollPane al centro
        borderPane.setCenter(scrollBotones);

        // Crear el layout raíz y agregar el BorderPane
        root = new VBox(borderPane);
        VBox.setVgrow(borderPane, Priority.ALWAYS);

    }
    
    //llamados desde el controlador
    public void agregarListenerMenuItemOpen(EventHandler<ActionEvent> handler)
    {
        openItem.setOnAction(handler);
    }
    
    public void agregarListenerMenuItemSave(EventHandler<ActionEvent> handler) 
    {
    	saveItem.setOnAction(handler);
    }
    
    public void agregarListenerMenuItemLoad(EventHandler<ActionEvent> handler) 
    {
    	loadItem.setOnAction(handler);
    }
    
    public String seleccionarArchivoXML() {
	    FileChooser fileChooser = new FileChooser();
	    fileChooser.setTitle("Seleccionar archivo XML");
	    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos XML", "*.xml"));
	    File archivo = fileChooser.showOpenDialog(null);
	    return (archivo != null) ? archivo.getAbsolutePath() : null;
	}
    
    public String seleccionarRutaGuardado() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar como archivo XML");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo XML", "*.xml"));
        File archivo = fileChooser.showSaveDialog(null);
        
        if (archivo != null) {
            return archivo.getAbsolutePath();
        }
        return null;
    }

    // llamado desde el controlador
    public String[] seleccionarArchivos() {
        // Abre un diálogo para seleccionar archivos
        List<File> archivos = buscadorArchivos.showOpenMultipleDialog(null);
        if (archivos != null) {
            return archivos.stream().map(File::getAbsolutePath).toArray(String[]::new);
        }
        return null;
    }

    // llamado desde el controlador
    public void agregarBoton(String ruta,String nombreCancion,double duracion,EventHandler<ActionEvent> handler) {
    	saveItem.setDisable(false);
    	Button boton = new Button(nombreCancion);
        boton.setOnAction(handler);
        botonSonido botonSonido = new botonSonido(ruta,nombreCancion,duracion,boton);
        agregarListenerBotonDerecho(botonSonido);
        mapaBotonesSonido.put(nombreCancion, botonSonido);
        panelBotones.getChildren().add(boton);
    }
    
    private void agregarListenerBotonDerecho(botonSonido botonSonido) 
    {
    	Button boton = botonSonido.getBotonAsociado();
    	  // Crear el menú contextual
        ContextMenu menuContextual = crearMenuContextual.menuContextual(this);

        // Configurar el evento de menú contextual
        boton.setOnContextMenuRequested((ContextMenuEvent e) -> {
            botonActualSeleccionado = botonSonido;
            menuContextual.show(boton, e.getScreenX(), e.getScreenY());
        });
    }
    
    public void setControlador(controlador controlador) 
    {
    	this.controlador = controlador;
    }

    public boolean getEstadoBoton(String nombreBoton) 
    {
    	botonSonido boton = mapaBotonesSonido.get(nombreBoton);
    	return boton.getBotonApretado();
    }
	
	public void abrirVentanaEditarSonido() 
    {
    	Stage ventanaBoton = botonActualSeleccionado.getVentanaEdicion();
    	
    	if (ventanaBoton == null) {
            ventanaEdicionSonido nuevaVentana = new ventanaEdicionSonido(botonActualSeleccionado,controlador);
            
            botonActualSeleccionado.setVentanaEdicion(nuevaVentana);
            nuevaVentana.show();
        } else {
        	ventanaBoton.toFront(); // Llevar la ventana existente al frente
        	ventanaBoton.requestFocus(); // Asegurar que la ventana tenga el foco
        }
    }
	
	public void colorearBotonReproduccion(String nombreBoton,boolean reproduciendo) 
    {
		botonSonido botonSonido = mapaBotonesSonido.get(nombreBoton);
		Button boton = botonSonido.getBotonAsociado();
		
		if(reproduciendo)
    		 boton.getStyleClass().add("boton-reproduccion"); // el archivo CSS estilosBoton
    	else
    		boton.getStyleClass().remove("boton-reproduccion");
		
		botonSonido.setBotonApretado(reproduciendo);
    }
	
	 public datosSonidoLectura getDatosSonido(String archivo) 
	 {
    	botonSonido boton =  mapaBotonesSonido.get(archivo);
    	return boton.getDatosLectura();
	 }
	 
	 public List<datosSonidoLectura> getDatosSonidos()
	 {
		 List<datosSonidoLectura> datosGuardar = new ArrayList<>();
		 for(botonSonido boton : mapaBotonesSonido.values())
		 {
			datosGuardar.add(boton.getDatosLectura()); 
		 }
		 
		 return datosGuardar;
	 }
	
    // listener que está escuchando al modelo para no estar preguntandole constantemente por 
    //eventos

	@Override
	 public void onReproduccionTerminada(String nombreBoton) {
    	botonSonido boton = mapaBotonesSonido.get(nombreBoton);
        Button botonInterface = boton.getBotonAsociado(); 
        botonInterface.getStyleClass().remove("boton-reproduccion");
    	boton.setBotonApretado(false);
    }
	
    public VBox getRoot() {
        return root;
    }
    
    public void borrarTodosLosBotones() {
        mapaBotonesSonido.clear();
        panelBotones.getChildren().clear(); // limpia todos los nodos visibles
    }
    
    // seters para cargar datos
    public void setVolumen(String archivo,double volumen) 
    {
    	botonSonido boton =  mapaBotonesSonido.get(archivo);
    	boton.setVolumen(volumen);
    }
    
    public void setFadeIn(String archivo,double fadeIn) 
    {
    	botonSonido boton =  mapaBotonesSonido.get(archivo);
    	boton.setFadeIn(fadeIn);
    }
    
    public void setFadeOut(String archivo,double fadeOut) 
    {
    	botonSonido boton =  mapaBotonesSonido.get(archivo);
    	boton.setFadeOut(fadeOut);
    }
    
    public void setLoop(String archivo,boolean loop) 
    {
    	botonSonido boton =  mapaBotonesSonido.get(archivo);
    	boton.setLoop(loop);
    }

	@Override
	public void avanceReproduccion(String nombreCancion, double avance) {
		// TODO Auto-generated method stub
		
	}
}
