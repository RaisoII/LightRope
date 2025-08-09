package vista;

import controlador.controlador;
import interfacesObserver.interfaceReproductorListener;
import archivosSoloLectura.datosSonidoLectura;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class vista implements interfaceReproductorListener{
    
    private controlador controlador;
    private Map<Integer,botonSonido> mapaBotonesSonido;
    private Set<String> tagsGlobales;
    ListView<String> listaTagsSugeridos;
    private botonSonido botonActualSeleccionado;
    
    // Elementos de la interfaz
	private VBox root;
    private FileChooser buscadorArchivos;
    private MenuItem openItem,saveItem,loadItem,importItem;
    private TilePane  panelBotones;
    private TextField barraBusqueda;
    private Button botonFiltrarTag;
    private Button botonLimpiarTags;
    private Popup popupTags;
    private FlowPane tagsContainer;
    private HBox filtroTagsContainer; // CAMBIO: Usamos HBox en lugar de FlowPane
    
    
    private MenuBar menuBar = new MenuBar();
    
    public vista() {
    
    	mapaBotonesSonido = new HashMap<Integer,botonSonido>();
    	tagsGlobales = new HashSet<>();
        crearInterface();
    }
    
    private void crearInterface() {
        // Crear menú
        Menu fileMenu = new Menu("File");
        openItem = new MenuItem("Open Audio File");
        saveItem = new MenuItem("Save Changes");
        loadItem = new MenuItem("Load XML Project");
        importItem = new MenuItem("Import Resources");
        saveItem.setDisable(true);
        fileMenu.getItems().addAll(openItem, saveItem, loadItem,importItem);
        menuBar.getMenus().add(fileMenu);

        // Layout principal con BorderPane
        BorderPane borderPane = new BorderPane();

        // --- Área de Filtros de Tags (CAMBIO: Usamos HBox para la barra de búsqueda) ---
        filtroTagsContainer = new HBox(5); // Espaciado horizontal
        filtroTagsContainer.setPrefHeight(22);
        filtroTagsContainer.setAlignment(Pos.CENTER_LEFT);
        filtroTagsContainer.setStyle("-fx-border-color: #ccc; -fx-border-radius: 3; -fx-background-color: white;");

        // TextField auxiliar para escribir cuando no hay tags
        barraBusqueda = new TextField();
        barraBusqueda.setPromptText("Find Sound...");
        barraBusqueda.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-padding: 2 6 2 6;");
        
        // CAMBIO CLAVE: Hacemos que el TextField ocupe el espacio restante en el HBox
        HBox.setHgrow(barraBusqueda, Priority.ALWAYS);

        // Escuchador para el TextField auxiliar
        barraBusqueda.textProperty().addListener((obs, oldText, newText) -> {
            filtrarBotones();
        });
        
        filtroTagsContainer.getChildren().add(barraBusqueda);
        // --- FIN NUEVA ÁREA DE FILTROS ---
        
        botonLimpiarTags = new Button("✕");
        botonLimpiarTags.setStyle("-fx-background-color: transparent; -fx-text-fill: gray; -fx-padding: 0; -fx-font-size: 16px;");
        botonLimpiarTags.setTooltip(new Tooltip("Eliminar todos los tags"));
        botonLimpiarTags.setVisible(false); // Inicialmente oculto
        botonLimpiarTags.setManaged(false); // No ocupa espacio
        botonLimpiarTags.setOnAction(e -> limpiarTags());

        // Botón para abrir/cerrar el popup de tags
        botonFiltrarTag = new Button();
        botonFiltrarTag.setGraphic(crearIconoTags()); // Usamos la nueva función
        botonFiltrarTag.setTooltip(new Tooltip("Filtrar por tags"));
        botonFiltrarTag.setPadding(new Insets(5)); // Ajusta el padding para que se vea bien

        botonFiltrarTag.setOnAction(e -> {
            if (popupTags == null || !popupTags.isShowing()) {
                filtrarTags(botonFiltrarTag);
            } else {
                popupTags.hide();
            }
        });

        // Botón "Tags" (si se usa para otra funcionalidad)
        Button botonTags = new Button("Tags");
        botonTags.setOnAction(e -> abrirVentanaTags()); // Asumimos que este método existe

        // Alineación principal
        HBox barraBusquedaBox = new HBox(10, filtroTagsContainer, botonLimpiarTags, botonFiltrarTag, botonTags); 
        barraBusquedaBox.setAlignment(Pos.CENTER_LEFT);
        barraBusquedaBox.setPadding(new Insets(5));
        HBox.setHgrow(filtroTagsContainer, Priority.ALWAYS);

        VBox topContainer = new VBox(menuBar, barraBusquedaBox);
        borderPane.setTop(topContainer);

        // FileChooser
        buscadorArchivos = new FileChooser();

        // Panel botones
        panelBotones = new TilePane();
        panelBotones.setHgap(10);
        panelBotones.setVgap(10);
        panelBotones.setPrefColumns(5);
        panelBotones.setAlignment(Pos.TOP_LEFT);
        panelBotones.setPadding(new Insets(10));
        panelBotones.setStyle("-fx-background: transparent; -fx-border-color: transparent;");

        ScrollPane scrollBotones = new ScrollPane(panelBotones);
        scrollBotones.setFitToWidth(true);
        VBox.setVgrow(scrollBotones, Priority.ALWAYS);
        borderPane.setCenter(scrollBotones);

        // Contenedor raíz
        root = new VBox(borderPane);
        VBox.setVgrow(borderPane, Priority.ALWAYS);
        
        Platform.runLater(() -> root.requestFocus());
    }
    
    private VBox crearIconoTags() {
        VBox iconoHamburguesa = new VBox(2); // Espacio entre las rayas
        iconoHamburguesa.setAlignment(Pos.CENTER);

        Region rayaSuperior = new Region();
        rayaSuperior.setPrefSize(18,2);
        rayaSuperior.setStyle("-fx-background-color: #333; -fx-background-radius: 1;");

        Region rayaMedia = new Region();
        rayaMedia.setPrefSize(18,2);
        rayaMedia.setStyle("-fx-background-color: #333; -fx-background-radius: 1;");

        Region rayaInferior = new Region();
        rayaInferior.setPrefSize(18,2); // Esta es la raya más corta
        rayaInferior.setStyle("-fx-background-color: #333; -fx-background-radius: 1;");

        iconoHamburguesa.getChildren().addAll(rayaSuperior, rayaMedia, rayaInferior);
        return iconoHamburguesa;
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
    
    public void agregarListenerMenuItemImport(EventHandler<ActionEvent> handler) 
    {
    	importItem.setOnAction(handler);
    }
    
    private void filtrarTags(Button botonQueAbre) {
        // 1. Asegurarse de que el Popup y el contenedor de tags existan
        if (popupTags == null) {
            popupTags = new Popup();
            VBox popupLayout = new VBox(5);
            popupLayout.setPadding(new Insets(10));
            popupLayout.setStyle("-fx-background-color: white; -fx-border-color: gray; -fx-border-width: 1; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);");

            tagsContainer = new FlowPane(5, 5);
            tagsContainer.setPrefWrapLength(200);

            popupLayout.getChildren().add(tagsContainer);
            popupTags.getContent().add(popupLayout);

            // --- CAMBIO CLAVE: Desactivar el cierre automático del popup ---
            popupTags.setAutoHide(false);
            
            // --- NUEVA LÓGICA: Añadir un filtro de eventos a la escena para detectar clics fuera del popup ---
            // Esto permite que los clics en otros botones "pasen" y se ejecuten
            root.getScene().addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                Node target = (Node) event.getTarget();
                
                // Verificar si el clic fue en un nodo fuera del popup
                if (popupTags.isShowing() && !popupTags.getContent().get(0).getBoundsInParent().contains(target.getBoundsInParent())) {
                    // Cerramos el popup manualmente
                    popupTags.hide();
                }
            });
            
            popupTags.setOnHidden(e -> barraBusqueda.setEditable(true));
        }

        // 2. Limpiar y recrear los botones de tags CADA VEZ
        tagsContainer.getChildren().clear();
        Set<String> tagsEnBarra = obtenerTagsDeBarra();

        for (String tag : tagsGlobales) {
            Button tagButton = crearBotonPopupTag(tag);
            if (tagsEnBarra.contains(tag.toLowerCase())) {
                tagButton.getStyleClass().add("tag-seleccionado");
            }
            tagButton.setOnAction(e -> {
                if (tagButton.getStyleClass().contains("tag-seleccionado")) {
                    tagButton.getStyleClass().remove("tag-seleccionado");
                    eliminarTagDeBarra(tag);
                } else {
                    tagButton.getStyleClass().add("tag-seleccionado");
                    agregarTagABarra(tag);
                }
            });
            tagsContainer.getChildren().add(tagButton);
        }
        
        // Ocultar si ya está abierto
        if (popupTags.isShowing()) {
            popupTags.hide();
            return;
        }

        // Calcular la posición y centrar el popup
        double buttonScreenX = botonQueAbre.localToScreen(0, 0).getX();
        double buttonScreenY = botonQueAbre.localToScreen(0, 0).getY();
        double buttonWidth = botonQueAbre.getWidth();
        double buttonHeight = botonQueAbre.getHeight();

        popupTags.getContent().get(0).applyCss();
        double popupWidth = popupTags.getContent().get(0).prefWidth(-1);

        double popupX = buttonScreenX + (buttonWidth / 2) - (popupWidth / 2);
        double popupY = buttonScreenY + buttonHeight + 5;

        // Mostrar el popup en la posición calculada
        popupTags.show(botonQueAbre, popupX, popupY);
        barraBusqueda.setEditable(false);
    }    
    
    private Button crearBotonPopupTag(String tag) {
        Button tagButton = new Button(tag);
        // Aplicamos el mismo estilo de los tags de la barra
        tagButton.setStyle("-fx-background-color: #3870b2; -fx-background-radius: 3; -fx-padding: 3 5 3 5; -fx-text-fill: white; -fx-font-size: 11px;");
        tagButton.setAlignment(Pos.CENTER);
        return tagButton;
    }
    
    // --- Métodos de ayuda ---

    private void agregarTagABarra(String tag) {
        // Si el tag ya existe, no hacemos nada
        if (filtroTagsContainer.getChildren().stream().anyMatch(node -> {
            if (node instanceof HBox) {
                return ((Label) ((HBox) node).getChildren().get(0)).getText().equals(tag);
            }
            return false;
        })) {
            return;
        }

        HBox tagNode = crearBotonTag(tag);

        // CAMBIO CLAVE: Agregamos el tag en la posición 0
        filtroTagsContainer.getChildren().add(0, tagNode);
        
        // Llamamos a este método para que la UI se actualice correctamente
        filtrarBotones();
    }
    
    
    private void eliminarTagDeBarra(String tag) {
        filtroTagsContainer.getChildren().removeIf(node -> {
            if (node instanceof HBox) {
                return ((Label) ((HBox) node).getChildren().get(0)).getText().equals(tag);
            }
            return false;
        });

        // ¡Esta línea es la clave!
        // Al llamar a filtrarBotones(), el método se encargará de
        // actualizar el estado de la UI, incluyendo la barra de búsqueda.
        filtrarBotones();
    }
    
    
    private HBox crearBotonTag(String tag) {
        HBox tagBox = new HBox(3);
        tagBox.setAlignment(Pos.CENTER);
        tagBox.setStyle("-fx-background-color: #3870b2; -fx-background-radius: 3; -fx-padding: 3 5 3 5;");
        
        Label tagLabel = new Label(tag);
        tagLabel.setStyle("-fx-text-fill: white; -fx-font-size: 11px;");

        Button closeButton = new Button("✕");
        closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-padding: 0; -fx-font-size: 8px;");
        closeButton.setOnAction(e -> {
            eliminarTagDeBarra(tag);
            // Aseguramos que el botón en el popup refleje el cambio visualmente
            actualizarBotonEnPopup(tag, false);
        });
        
        tagBox.getChildren().addAll(tagLabel, closeButton);
        return tagBox;
    }

    private void actualizarBotonEnPopup(String tag, boolean seleccionado) {
        if (popupTags != null && tagsContainer != null) {
            tagsContainer.getChildren().stream()
                .filter(node -> node instanceof Button && ((Button) node).getText().equals(tag))
                .findFirst()
                .ifPresent(node -> {
                    Button btn = (Button) node;
                    if (seleccionado) {
                        btn.getStyleClass().add("tag-seleccionado");
                    } else {
                        btn.getStyleClass().remove("tag-seleccionado");
                    }
                });
        }
    }
    
    private Set<String> obtenerTagsDeBarra() {
        Set<String> tags = new HashSet<>();
        for (Node node : filtroTagsContainer.getChildren()) {
            if (node instanceof HBox) {
                Label label = (Label) ((HBox) node).getChildren().get(0);
                tags.add(label.getText().toLowerCase());
            }
        }
        return tags;
    }

 // Asegúrate de que botonFiltrarTag sea una variable de clase
    // private Button botonFiltrarTag;

    private void filtrarBotones() {
        
    	panelBotones.getChildren().clear();
        Set<String> tagsBuscadosSet = obtenerTagsDeBarra();
        String textoBusqueda = barraBusqueda.getText().toLowerCase().trim();

        boolean hayTags = !tagsBuscadosSet.isEmpty();
        boolean hayTextoEnBarra = !textoBusqueda.isEmpty();

        botonFiltrarTag.setDisable(hayTextoEnBarra);

        barraBusqueda.setDisable(hayTags);
        barraBusqueda.setPromptText(hayTags ? "" : "Find Sound...");

        botonLimpiarTags.setVisible(hayTags);
        botonLimpiarTags.setManaged(hayTags);

        // Lógica de Filtrado de botones
        if (hayTags) {
            // Filtrar por tags
            for (botonSonido boton : mapaBotonesSonido.values()) {
                boolean coincideAlgunTag = tagsBuscadosSet.stream()
                        .anyMatch(tagBuscado -> boton.getTags().stream()
                                .anyMatch(tagBoton -> tagBoton.toLowerCase().contains(tagBuscado)));
                if (coincideAlgunTag) {
                    panelBotones.getChildren().add(boton.getContenedor());
                }
            }
        } else if (hayTextoEnBarra) {
            // Filtrar por nombre si no hay tags activos
            for (botonSonido boton : mapaBotonesSonido.values()) {
                if (boton.getNombreArchivo().toLowerCase().contains(textoBusqueda)) {
                    panelBotones.getChildren().add(boton.getContenedor());
                }
            }
        } else {
            // Mostrar todos los botones si el filtro está completamente vacío
            for (botonSonido boton : mapaBotonesSonido.values()) {
                panelBotones.getChildren().add(boton.getContenedor());
            }
        }

        // --- NUEVA LÓGICA: Mostrar mensaje si no hay botones ---
        if (panelBotones.getChildren().isEmpty()) {
            Label noResultsLabel = new Label("No result Found.");
            noResultsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: gray;");
            
            // Para centrar el mensaje en el panel, se puede usar un StackPane
            StackPane mensajeContenedor = new StackPane(noResultsLabel);
            mensajeContenedor.setPrefSize(panelBotones.getPrefWidth(), panelBotones.getPrefHeight());
            
            panelBotones.getChildren().add(mensajeContenedor);
        }
    }    
    private void limpiarTags() {
        
    	filtroTagsContainer.getChildren().clear();
        filtroTagsContainer.getChildren().add(barraBusqueda);
        barraBusqueda.setText("");
        
        // La barra de búsqueda siempre es visible y manejada después de limpiar
        barraBusqueda.setVisible(true);
        barraBusqueda.setManaged(true);
        barraBusqueda.setDisable(false);
        filtrarBotones();
        
        // Aseguramos que los botones en el popup se deseleccionen
        if (popupTags != null && tagsContainer != null) {
            for (Node node : tagsContainer.getChildren()) {
                if (node instanceof Button) {
                    ((Button) node).getStyleClass().remove("tag-seleccionado");
                }
            }
        }
    }    
    
    private void abrirVentanaTags()
    {
    	
    	if(popupTags != null) 
    	{
    		popupTags.hide();
    	}
    	
    	ventanaTags vt = new ventanaTags(tagsGlobales);
        vt.showAndWait();
        Set<String> tagsGlobalesAuxiliares = vt.getTagsActuales();

        refrescarTagsVentanas(tagsGlobalesAuxiliares);
    }
    
    private void refrescarTagsVentanas(Set<String> tagsGlobalesAuxiliares) 
    { 
        // BARRA DE BUSQUEDA
    	Set<String> tagsEnBarra = obtenerTagsDeBarra();

        if (!tagsEnBarra.isEmpty())
        {
        	  Set<String> tagsAEliminar = new HashSet<>();
              
              for (String tag : tagsEnBarra)
              {
                  if (!tagsGlobalesAuxiliares.contains(tag.toLowerCase())) {
                      tagsAEliminar.add(tag);
                  }
              }
              
              for (String tag : tagsAEliminar)
            	  eliminarTagDeBarra(tag);
              
              if(tagsAEliminar.size()  > 0)
            	  filtrarBotones();
        }
        

        tagsGlobales = tagsGlobalesAuxiliares;
        List<String> listaTags = new ArrayList<>(tagsGlobales);
        controlador.guardarTags(listaTags);
        
        //VENTANAS SONIDO Y BOTONES
        for(botonSonido boton : mapaBotonesSonido.values()) 
        {
        	Stage ventana =  boton.getVentanaEdicion();
        	
        	if(ventana == null) 
        	{
        		boton.checkearTags(listaTags);
        	}
        	else 
        	{
            	ventanaEdicionSonido  ventanaSonido = (ventanaEdicionSonido)ventana;
            	ventanaSonido.checkearTags(listaTags);
        	}
        }
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

	private void setearImagenBoton(int idBoton, String ruta) {
	    Image imagen = new Image(ruta);
	    ImageView imageView = new ImageView(imagen);
	
	    botonSonido boton = mapaBotonesSonido.get(idBoton);
	    Button botonFx = boton.getBotonAsociado();
	    StackPane contenedor = boton.getBordeBoton();
	
	    // Set image size to fit the button container
	    imageView.setPreserveRatio(false);
	    imageView.setFitWidth(contenedor.getPrefWidth());
	    imageView.setFitHeight(contenedor.getPrefHeight());
	
	    // Create a rounded rectangle for clipping the image
	    Rectangle clip = new Rectangle();
	    clip.setWidth(contenedor.getPrefWidth());
	    clip.setHeight(contenedor.getPrefHeight());
	    
	    // Set the corner radius to match the border-radius of the StackPane
	    // This value should be the same as the one used in your CSS or inline styles for the border-radius.
	    clip.setArcWidth(20); // Adjust this value as needed
	    clip.setArcHeight(20); // Adjust this value as needed
	
	    // Apply the clip to the ImageView
	    imageView.setClip(clip);
	
	    // Add the image and the button to the StackPane
	    contenedor.getChildren().setAll(imageView, botonFx);
	    botonFx.toFront(); // Ensure the button remains clickable
	
	    boton.setRutaImagen(ruta);
	}
    
    // llamado desde el controlador
	// En tu clase 'vista'
	public String[] seleccionarArchivos() {
	    // 1. Configurar el FileChooser
	    buscadorArchivos = new FileChooser(); // Asumimos que buscadorArchivos es un atributo de la clase
	    buscadorArchivos.setTitle("Seleccionar Archivos de Sonido");

	    // --- CAMBIO CLAVE: Agregar el filtro de extensiones ---
	    FileChooser.ExtensionFilter filtroAudio =
	        new FileChooser.ExtensionFilter("Archivos de Audio (.mp3, .wav)", "*.mp3", "*.wav");
	    
	    buscadorArchivos.getExtensionFilters().add(filtroAudio);
	    
	    // 2. Abrir el diálogo y obtener la lista de archivos
	    List<File> archivos = buscadorArchivos.showOpenMultipleDialog(null);

	    // 3. Procesar los archivos seleccionados y devolver las rutas
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
    	System.out.println(botonSonido.getLoop());
    	  // Crear el menú contextual
        MenuContextual menuContextual = new MenuContextual(this,botonSonido.getLoop(),
        																botonSonido.getIdBoton());

        // Configurar el evento de menú contextual
        boton.setOnContextMenuRequested((ContextMenuEvent e) -> {
        	
        	menuContextual.actualizarLoop(botonSonido.getLoop());
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

    		List<String> listaTags = new ArrayList<>(tagsGlobales);
    		ventanaEdicionSonido nuevaVentana = new ventanaEdicionSonido(botonActualSeleccionado,controlador,listaTags);
            
            botonActualSeleccionado.setVentanaEdicion(nuevaVentana);
            nuevaVentana.show();
        } else {
        	ventanaBoton.toFront(); // Llevar la ventana existente al frente
        	ventanaBoton.requestFocus(); // Asegurar que la ventana tenga el foco
        }
    }
	
	public void colorearBotonReproduccion(int idBoton, boolean reproduciendo) {
	    botonSonido botonSonido = mapaBotonesSonido.get(idBoton);
	    StackPane borde = botonSonido.getBordeBoton();

	    // Actualiza el estado de reproducción del objeto botonSonido
	    botonSonido.setBotonApretado(reproduciendo);

	    // Define el valor de border-radius una sola vez para mantener la consistencia
	    // Asegúrate de que este valor coincida con el que usas en 'configurarListenersEstilo'
	    String bordeRedondeado = "-fx-border-radius: 10;"; // O el valor que estés usando

	    // Aplica el estilo completo, incluyendo el borde redondeado
	    if (botonSonido.getBotonApretado()) {
	        borde.setStyle("-fx-border-color: yellow; -fx-border-width: 2;" + bordeRedondeado);
	    } else if (botonSonido.getBotonAsociado().isFocused()) {
	        borde.setStyle("-fx-border-color: green; -fx-border-width: 2;" + bordeRedondeado);
	    } else if (botonSonido.getBotonAsociado().isHover()) {
	        borde.setStyle("-fx-border-color: dodgerblue; -fx-border-width: 2;" + bordeRedondeado);
	    } else {
	        borde.setStyle("-fx-border-color: black; -fx-border-width: 2;" + bordeRedondeado);
	    }
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
	public void onReproduccionTerminada(int idBoton)
	{
	    botonSonido boton = mapaBotonesSonido.get(idBoton);
	    Button botonFx = boton.getBotonAsociado();
	    StackPane borde = boton.getBordeBoton();

	    // Define el valor de border-radius para no repetirlo
	    // Asegúrate de que este valor coincida con el de tus otros métodos
	    String bordeRedondeado = "-fx-border-radius: 10;"; 

	    if (botonFx.isFocused()) {
	        borde.setStyle("-fx-border-color: green; -fx-border-width: 2;" + bordeRedondeado);
	    } else {
	        borde.setStyle("-fx-border-color: black; -fx-border-width: 2;" + bordeRedondeado);
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
    
    public void setearLoopMenuContextual(int idBoton, boolean loop) 
    {
    	botonSonido boton = mapaBotonesSonido.get(idBoton);
    	boton.setLoop(loop);
    	ventanaEdicionSonido ventanaBoton = (ventanaEdicionSonido) boton.getVentanaEdicion();
    	if(ventanaBoton != null)
    		ventanaBoton.setLoopBox(loop);
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
    
    public void setTagsGlobales(List<String> listaTags) 
    {
    	tagsGlobales = new HashSet<>(listaTags);
    }
    
    public void setTagsBoton(int idBoton,List<String> listaTags) 
    {
    	List<String> listaMutable = new ArrayList<>(listaTags);
    	botonSonido boton =  mapaBotonesSonido.get(idBoton);
    	boton.setListaTagInicial(listaMutable,tagsGlobales);
    }

	@Override
	public void avanceReproduccion(int idBoton, double avance) {
		// TODO Auto-generated method stub
		
	}
}
