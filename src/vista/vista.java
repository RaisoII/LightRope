package vista;

import controlador.controlador;
import interfacesObserver.interfaceReproductorListener;
import archivosSoloLectura.datosSonidoLectura;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class vista implements interfaceReproductorListener{
    
    private controlador controlador;
    private Map<Integer,botonSonido> mapaBotonesSonido;
    private botonSonido botonActualSeleccionado;
    
    // Elementos de la interfaz
	private VBox root;
    private FileChooser buscadorArchivos;
    private MenuItem openItem,saveItem,loadItem;
    private TilePane  panelBotones;
    ScrollPane scrollBotones;
    
    private MenuBar menuBar = new MenuBar();
    
    public vista() {
    
    	mapaBotonesSonido = new HashMap<Integer,botonSonido>();
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
        panelBotones = new TilePane();
        panelBotones.setHgap(10);
        panelBotones.setVgap(10);
        panelBotones.setPrefColumns(5); // o el número de columnas que quieras
        panelBotones.setAlignment(Pos.TOP_LEFT);
        panelBotones.setPadding(new Insets(10, 10, 10, 10));
        panelBotones.setStyle("-fx-background: transparent; -fx-border-color: transparent;");
        // Inicializar ScrollPane con FlowPane
        ScrollPane scrollBotones = new ScrollPane(panelBotones);
        scrollBotones.setFitToWidth(true);  // Ajustar el contenido al ancho del ScrollPane
 
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
    
public void seleccionarImagenParaBoton(int idBoton) {
          	
    	FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar imagen");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        File archivo = fileChooser.showOpenDialog(null);
        if (archivo != null)
        	setearImagenBoton(idBoton,archivo.toURI().toString());
        
    }

	private void setearImagenBoton(int idBoton,String ruta) 
	{
         Image imagen = new Image(ruta);
         ImageView imageView = new ImageView(imagen);

         botonSonido boton = mapaBotonesSonido.get(idBoton);
         Button botonFx = boton.getBotonAsociado();

         imageView.setPreserveRatio(false);
         imageView.setFitWidth(botonFx.getPrefWidth());
         imageView.setFitHeight(botonFx.getPrefHeight());

         StackPane contenedor = boton.getBordeBoton();
         contenedor.getChildren().setAll(imageView, botonFx); // Imagen de fondo, botón encima
         botonFx.toFront(); // Asegura que el botón sea interactivo

         boton.setRutaImagen(ruta);

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
    
    public void agregarBoton(String ruta, String nombre, int id, double duracion, EventHandler<ActionEvent> handler) {
        botonSonido boton = botonFactory.crearBoton(ruta, nombre, id, duracion, handler, panelBotones);
        mapaBotonesSonido.put(id, boton);
        agregarListenerBotonDerecho(boton);
        saveItem.setDisable(false);
    }


    private void agregarListenerBotonDerecho(botonSonido botonSonido) 
    {
    	Button boton = botonSonido.getBotonAsociado();
    	  // Crear el menú contextual
        ContextMenu menuContextual = crearMenuContextual.menuContextual(this,botonSonido.getIdBoton());

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

    public boolean getEstadoBoton(int idBoton) 
    {
    	System.out.print("id: "+idBoton);
    	botonSonido boton = mapaBotonesSonido.get(idBoton);
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
	
	public void colorearBotonReproduccion(int idBoton, boolean reproduciendo)
	{
	 
		botonSonido botonSonido = mapaBotonesSonido.get(idBoton);
	    // Button boton = botonSonido.getBotonAsociado(); // Ya no necesitamos interactuar directamente con el botón para el estilo amarillo

	    botonSonido.setBotonApretado(reproduciendo); // Actualiza el estado de reproducción

	    if (botonSonido.getBotonApretado()) 
            botonSonido.getBordeBoton().setStyle("-fx-border-color: yellow; -fx-border-width: 2;");
         else if (botonSonido.getBotonAsociado().isFocused()) 
            botonSonido.getBordeBoton().setStyle("-fx-border-color: green; -fx-border-width: 2;");
         else if (botonSonido.getBotonAsociado().isHover()) 
            botonSonido.getBordeBoton().setStyle("-fx-border-color: dodgerblue; -fx-border-width: 2;");
         else 
            botonSonido.getBordeBoton().setStyle("-fx-border-color: black; -fx-border-width: 2;");
        
	}
	
	 public datosSonidoLectura getDatosSonido(int idBoton) 
	 {
    	botonSonido boton =  mapaBotonesSonido.get(idBoton);
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
	public void onReproduccionTerminada(int idBoton) {
	    botonSonido boton = mapaBotonesSonido.get(idBoton); 
	    Button botonFx = boton.getBotonAsociado();

	    if (botonFx.isFocused()) {
	        boton.getBordeBoton().setStyle("-fx-border-color: green; -fx-border-width: 2;");
	    } else {
	        boton.getBordeBoton().setStyle("-fx-border-color: black; -fx-border-width: 2;");
	    }

	    boton.setBotonApretado(false);
	}

	
    public VBox getRoot()
    {
        return root;
    }
    
    public void borrarTodosLosBotones()
    {
        mapaBotonesSonido.clear();
        panelBotones.getChildren().clear(); // limpia todos los nodos visibles
    }
    
    public void borrarBoton(int idBoton)
    {
        botonSonido boton = mapaBotonesSonido.get(idBoton);
        
        if(boton.getBotonApretado()) 
        {
        	controlador.borrarReproduccion(idBoton);
        	onReproduccionTerminada(idBoton);
        }
        
        mapaBotonesSonido.remove(idBoton);
        panelBotones.getChildren().remove(boton.getContenedor());    
    }
    
    // seters para cargar datos
    public void setVolumen(int idBoton,double volumen) 
    {
    	botonSonido boton =  mapaBotonesSonido.get(idBoton);
    	boton.setVolumen(volumen);
    }
    
    public void setFadeIn(int idBoton,double fadeIn) 
    {
    	botonSonido boton =  mapaBotonesSonido.get(idBoton);
    	boton.setFadeIn(fadeIn);
    }
    
    public void setFadeOut(int idBoton,double fadeOut) 
    {
    	botonSonido boton =  mapaBotonesSonido.get(idBoton);
    	boton.setFadeOut(fadeOut);
    }
    
    public void setLoop(int idBoton,boolean loop) 
    {
    	botonSonido boton =  mapaBotonesSonido.get(idBoton);
    	boton.setLoop(loop);
    }
    
    public void setImagen(int idBoton,String rutaImagen) 
    {
    	if(rutaImagen == null || rutaImagen.isEmpty())
    		return;
    	
    	setearImagenBoton(idBoton,rutaImagen);
    }

	@Override
	public void avanceReproduccion(int idBoton, double avance) {
		// TODO Auto-generated method stub
		
	}
}
