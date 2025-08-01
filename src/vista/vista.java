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
import javafx.scene.layout.*;
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
    private MenuItem openItem,saveItem,loadItem;
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
        openItem = new MenuItem("Open");
        saveItem = new MenuItem("Save");
        loadItem = new MenuItem("Load");
        saveItem.setDisable(true);
        fileMenu.getItems().addAll(openItem, saveItem, loadItem);
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

        // Botón "MÁS" para abrir/cerrar el popup de tags
        botonFiltrarTag = new Button("MÁS");
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
    
    private void filtrarTags(Button botonQueAbre) {
        // 1. Asegurarse de que el Popup y el contenedor de tags existan
        if (popupTags == null) {
            popupTags = new Popup();
            VBox popupLayout = new VBox(5);
            popupLayout.setPadding(new Insets(10));
            popupLayout.setStyle("-fx-background-color: white; -fx-border-color: gray; -fx-border-width: 1; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);");

            tagsContainer = new FlowPane(5, 5);
            tagsContainer.setPrefWrapLength(200); // Esto ayuda a que el popup no sea excesivamente ancho

            popupLayout.getChildren().add(tagsContainer);
            popupTags.getContent().add(popupLayout);
            popupTags.setAutoHide(true);
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
        
        // --- Lógica CLAVE: Posicionar y mostrar el Popup de forma dinámica ---
        // 3. Ocultar si ya está abierto
        if (popupTags.isShowing()) {
            popupTags.hide();
        }

        // 4. Calcular la posición
        double buttonScreenX = botonQueAbre.localToScreen(0, 0).getX();
        double buttonScreenY = botonQueAbre.localToScreen(0, 0).getY();
        double buttonHeight = botonQueAbre.getHeight();

        // Se posiciona el popup justo debajo del botón.
        // Esto es más simple y predecible que intentar centrarlo en X.
        double popupX = buttonScreenX;
        double popupY = buttonScreenY + buttonHeight + 5; // +5 para un pequeño margen

        // 5. Mostrar el popup en la posición calculada
        popupTags.show(botonQueAbre, popupX, popupY);
        barraBusqueda.setEditable(false);
        // --- FIN Lógica de Posicionamiento ---
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
        // CAMBIO: En lugar de eliminarlo, lo ocultamos
        barraBusqueda.setVisible(false);
        barraBusqueda.setManaged(false);
        
        // Si el tag ya existe, no hacemos nada
        if (filtroTagsContainer.getChildren().stream().anyMatch(node -> {
            if (node instanceof HBox) {
                return ((Label) ((HBox) node).getChildren().get(0)).getText().equals(tag);
            }
            return false;
        })) {
            return;
        }

        // Crear el botón de tag con su 'x' para eliminar
        HBox tagNode = crearBotonTag(tag);
        filtroTagsContainer.getChildren().add(filtroTagsContainer.getChildren().size(), tagNode); // Agregamos al final
        
        // Actualizar el filtrado de botones en el panel principal
        filtrarBotones();
    }
    
    private void eliminarTagDeBarra(String tag) {
        filtroTagsContainer.getChildren().removeIf(node -> {
            if (node instanceof HBox) {
                return ((Label) ((HBox) node).getChildren().get(0)).getText().equals(tag);
            }
            return false;
        });

        // CAMBIO: Si no quedan tags, hacemos la barra de búsqueda visible de nuevo
        if (filtroTagsContainer.getChildren().isEmpty()) {
            barraBusqueda.setVisible(true);
            barraBusqueda.setManaged(true);
        }
        
        // Actualizar el filtrado de botones
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

        // Control del botón "MÁS"
        botonFiltrarTag.setDisable(hayTextoEnBarra);
        
        // La barra de búsqueda (TextField) se deshabilita si hay tags
        barraBusqueda.setDisable(hayTags);
        
        // --- NUEVO: Control de la visibilidad del botón de limpiar ---
        botonLimpiarTags.setVisible(hayTags);
        botonLimpiarTags.setManaged(hayTags);
        // --- FIN NUEVO CONTROL ---


        // Lógica de Filtrado de botones (sin cambios)
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
    
    private void abrirVentanaTags() {
        ventanaTags vt = new ventanaTags(tagsGlobales);
        vt.showAndWait();
        tagsGlobales = vt.getTagsActuales(); // actualiza global
        List<String> listaTags = new ArrayList<String>(tagsGlobales);
        controlador.guardarTags(listaTags);
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

    		List<String> listaTags = new ArrayList<>(tagsGlobales);
    		ventanaEdicionSonido nuevaVentana = new ventanaEdicionSonido(botonActualSeleccionado,controlador,listaTags);
            
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
    
    public void setTagsGlobales(List<String> listaTags) 
    {
    	tagsGlobales = new HashSet<>(listaTags);
    }

	@Override
	public void avanceReproduccion(int idBoton, double avance) {
		// TODO Auto-generated method stub
		
	}
}
