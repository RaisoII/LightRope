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
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
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
    private Set<String> tagsGlobales;
    ListView<String> listaTagsSugeridos;
    private botonSonido botonActualSeleccionado;
    
    // Elementos de la interfaz
	private VBox root;
    private FileChooser buscadorArchivos;
    private MenuItem openItem,saveItem,loadItem;
    private TilePane  panelBotones;
    private TextField barraBusqueda;
    
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

        // Barra de búsqueda
        barraBusqueda = new TextField();
        barraBusqueda.setPromptText("Find Sound...");
        // Define el ancho preferido y máximo de la barra de búsqueda.
        // Esto es crucial para controlar el ancho de la lista de sugerencias.
        barraBusqueda.setPrefWidth(250); // Puedes ajustar este valor
        barraBusqueda.setMaxWidth(250);  // Asegura que no se estire más allá de este punto
        barraBusqueda.setStyle(
            "-fx-font-size: 11px; " +
            "-fx-padding: 2 6 2 6;" +
            "-fx-pref-height: 22;"
        );
        barraBusqueda.textProperty().addListener((obs, oldText, newText) -> filtrarBotones(newText));

        // Botón de tags
        Button botonTags = new Button("Tags");
        botonTags.setOnAction(e -> abrirVentanaTags());

        // --- Sugerencias flotantes ---
        listaTagsSugeridos = new ListView<>();
        listaTagsSugeridos.setMaxHeight(100);
        // IMPORTANTE: Establecemos un MaxWidth inicial. Este será actualizado en Platform.runLater
        // pero es bueno tener un valor por defecto que no sea "infinito"
        listaTagsSugeridos.setMaxWidth(barraBusqueda.getMaxWidth()); // Limita inicialmente al max de la barra
        listaTagsSugeridos.setVisible(false);
        listaTagsSugeridos.setManaged(false);
        listaTagsSugeridos.setMouseTransparent(false);
        listaTagsSugeridos.setStyle("-fx-background-color: white; -fx-border-color: gray; -fx-border-width: 1px;");

        listaTagsSugeridos.setOnMouseClicked(e -> {
            String seleccionado = listaTagsSugeridos.getSelectionModel().getSelectedItem();
            if (seleccionado != null) {
                barraBusqueda.setText("#" + seleccionado);
                barraBusqueda.positionCaret(barraBusqueda.getText().length());
                listaTagsSugeridos.setVisible(false);
                listaTagsSugeridos.setManaged(false);
            }
        });

        // Alineación búsqueda + botón
        HBox barraBusquedaBox = new HBox(10, barraBusqueda, botonTags);
        barraBusquedaBox.setAlignment(Pos.CENTER_LEFT);
        barraBusquedaBox.setPadding(new Insets(5));
        // Es buena práctica asegurarse de que los hijos no crezcan más allá de lo necesario en un HBox
        HBox.setHgrow(barraBusqueda, Priority.NEVER); // No permitimos que la barra de búsqueda se estire infinitamente

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

        // Contenedor raíz con StackPane para permitir superposición flotante
        StackPane stack = new StackPane();
        stack.getChildren().add(borderPane); // El diseño principal
        stack.getChildren().add(listaTagsSugeridos); // La lista de sugerencias flotante
        // Alineamos la lista de sugerencias al TOP_LEFT del StackPane para que los márgenes funcionen correctamente
        StackPane.setAlignment(listaTagsSugeridos, Pos.TOP_LEFT);

        root = new VBox(stack);
        VBox.setVgrow(stack, Priority.ALWAYS);

        // --- Posicionar y ajustar ancho de la lista flotante debajo de barraBusqueda ---
        Platform.runLater(() -> {
            // Obtener la posición de la barra de búsqueda en las coordenadas de la escena
            javafx.geometry.Point2D barraBusquedaScenePos = barraBusqueda.localToScene(0, 0);

            // Convertir esa posición a las coordenadas locales del StackPane
            javafx.geometry.Point2D listaSugerenciasStackPos = stack.sceneToLocal(barraBusquedaScenePos.getX(), barraBusquedaScenePos.getY());

            // Calcular los márgenes para la lista de sugerencias
            double margenTop = listaSugerenciasStackPos.getY() + barraBusqueda.getHeight();
            double margenLeft = listaSugerenciasStackPos.getX();

            // Aplicar los márgenes para posicionar la lista
            StackPane.setMargin(listaTagsSugeridos, new Insets(margenTop, 0, 0, margenLeft));

            // Establecer el ancho preferido y máximo de la lista de sugerencias
            // Es crucial establecer AMBOS para evitar que se estire
            listaTagsSugeridos.setPrefWidth(barraBusqueda.getWidth());
            listaTagsSugeridos.setMaxWidth(barraBusqueda.getWidth()); // <-- ESTO ES CLAVE
        });

        // Llamada al método que configura listeners adicionales para la barra de búsqueda
        configurarListenersBusqueda();

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
    
    
    private void configurarListenersBusqueda() {
        // Barra: Navegar hacia abajo, cerrar con ESC
        barraBusqueda.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case DOWN:
                    if (listaTagsSugeridos.isVisible() && !listaTagsSugeridos.getItems().isEmpty()) {
                        listaTagsSugeridos.requestFocus();
                        listaTagsSugeridos.getSelectionModel().selectFirst();
                        event.consume(); // Consumir el evento para que no se mueva el caret en la barra
                    }
                    break;
                case ESCAPE:
                    listaTagsSugeridos.setVisible(false);
                    listaTagsSugeridos.setManaged(false);
                    barraBusqueda.requestFocus(); // Asegurar foco en la barra al cerrar
                    Platform.runLater(() -> barraBusqueda.deselect()); // Deseleccionar por si acaso
                    event.consume(); // Consumir el evento para que no se propague
                    break;
            }
        });

        // Lista: Enter para seleccionar, ESC para cerrar, navegación con flechas (DOWN, UP)
        listaTagsSugeridos.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER:
                    String seleccionado = listaTagsSugeridos.getSelectionModel().getSelectedItem();
                    if (seleccionado != null) {
                        String textoActual = barraBusqueda.getText();
                        int lastHashIndex = textoActual.lastIndexOf('#');
                        String textoBaseParaNuevoTag = "";

                        if (lastHashIndex != -1) {
                            textoBaseParaNuevoTag = textoActual.substring(0, lastHashIndex + 1);
                            if (lastHashIndex + 1 < textoActual.length()) {
                                String tagParcial = textoActual.substring(lastHashIndex + 1);
                                if (!tagParcial.isEmpty() && !tagParcial.contains("#")) {
                                    textoBaseParaNuevoTag = textoActual.substring(0, lastHashIndex + 1);
                                }
                            }
                        } else {
                            textoBaseParaNuevoTag = "#";
                        }

                        String nuevoTexto = textoBaseParaNuevoTag + seleccionado + "#";
                        barraBusqueda.setText(nuevoTexto);

                        // --- LA SOLUCIÓN MÁS ROBUSTA AQUÍ ---
                        // Programamos estas acciones para que se ejecuten después de que el setText()
                        // y el ciclo de layout de JavaFX se hayan completado.
                        Platform.runLater(() -> {
                            barraBusqueda.requestFocus(); // Reafirmar el foco
                            barraBusqueda.positionCaret(nuevoTexto.length()); // Posicionar el cursor al final
                            barraBusqueda.deselect(); // Eliminar cualquier selección
                        });
                        // --- FIN SOLUCIÓN ROBUSTA ---
                    }
                    listaTagsSugeridos.setVisible(false);
                    listaTagsSugeridos.setManaged(false);
                    // No necesitas requestFocus aquí, ya lo hace el Platform.runLater
                    event.consume();
                    break;

                case ESCAPE:
                    listaTagsSugeridos.setVisible(false);
                    listaTagsSugeridos.setManaged(false);
                    barraBusqueda.requestFocus();
                    Platform.runLater(() -> barraBusqueda.deselect()); // También al cerrar con ESC
                    event.consume();
                    break;

                case UP:
                    if (listaTagsSugeridos.getSelectionModel().getSelectedIndex() == 0) {
                        barraBusqueda.requestFocus();
                        barraBusqueda.positionCaret(barraBusqueda.getText().length());
                        barraBusqueda.selectEnd(); // Asegura que no haya selección
                        listaTagsSugeridos.setVisible(false);
                        listaTagsSugeridos.setManaged(false);
                        event.consume();
                    }
                    break;
            }
        });

        // Asegura que la lista responda al teclado
        listaTagsSugeridos.setFocusTraversable(true);
    }
    
    private void filtrarBotones(String texto) {
        panelBotones.getChildren().clear();
        String textoBusqueda = texto.toLowerCase().trim();

        // Limpia y oculta la lista de sugerencias al inicio de cada filtro
        listaTagsSugeridos.getItems().clear();
        listaTagsSugeridos.setVisible(false);
        listaTagsSugeridos.setManaged(false);

        // --- Lógica para Múltiples Tags y Sugerencias ---
        if (textoBusqueda.startsWith("#")) {
            // Obtenemos el texto que sigue al '#'
            String contenidoDespuesHash = textoBusqueda.substring(1);

            // Dividir el texto de búsqueda en tags individuales si hay más de un '#'
            String[] tagsBuscadosArray = contenidoDespuesHash.split("#");
            Set<String> tagsBuscadosSet = new HashSet<>();
            for (String tag : tagsBuscadosArray) {
                if (!tag.isEmpty()) { // Asegurarse de no añadir tags vacíos
                    tagsBuscadosSet.add(tag);
                }
            }

            // --- Lógica de Sugerencias para el último tag parcial o todos los tags si solo hay '#' ---
            String tagParcialParaSugerir = "";
            if (contenidoDespuesHash.isEmpty() || contenidoDespuesHash.endsWith("#")) {
                // Si solo hay '#' o termina con '#', sugerir TODOS los tags
                // O si el último elemento del array de tagsBuscadosArray está vacío (ej. "#tag1#")
                tagParcialParaSugerir = ""; // Esto significa que no hay un tag parcial específico
            } else if (tagsBuscadosArray.length > 0) {
                // Si hay algo escrito después del último '#', ese es el tag parcial para sugerir
                tagParcialParaSugerir = tagsBuscadosArray[tagsBuscadosArray.length - 1];
            }


            // Poblar la lista de sugerencias
            for (String tagGlobal : tagsGlobales) {
                if (tagParcialParaSugerir.isEmpty() || tagGlobal.toLowerCase().contains(tagParcialParaSugerir)) {
                    listaTagsSugeridos.getItems().add(tagGlobal);
                }
            }

            // Mostrar la lista si hay coincidencias (o si solo se escribió '#')
            listaTagsSugeridos.setVisible(!listaTagsSugeridos.getItems().isEmpty());
            listaTagsSugeridos.setManaged(!listaTagsSugeridos.getItems().isEmpty());


            // --- Filtrar botones por CUALQUIERA de los tags buscados ---
            // Si no hay tags válidos en la búsqueda (ej. solo "#" o "###"), filtramos por "ningún tag" o no mostramos nada
            // Pero para el objetivo, si hay tags en tagsBuscadosSet, filtramos por ellos.
            // Si tagsBuscadosSet está vacío pero la búsqueda empieza con '#', mostramos todos los botones con tags.
            // La lógica actual de filtering ya muestra todos si no hay match, así que está bien.

            boolean hayTagsValidosParaFiltrar = !tagsBuscadosSet.isEmpty();

            for (botonSonido boton : mapaBotonesSonido.values()) {
                boolean coincideAlgunTag = false;

                if (hayTagsValidosParaFiltrar) {
                    // Si hay tags específicos siendo buscados (ej: #rock#pop)
                    for (String tagBuscado : tagsBuscadosSet) {
                        for (String tagBoton : boton.getTags()) {
                            if (tagBoton.toLowerCase().contains(tagBuscado)) {
                                coincideAlgunTag = true;
                                break;
                            }
                        }
                        if (coincideAlgunTag) {
                            break;
                        }
                    }
                } else {
                    // Si solo se escribió '#' o '#' y algo que no es un tag,
                    // queremos mostrar todos los botones que tengan CUALQUIER tag asociado.
                    // Asumo que 'boton.getTags()' devuelve una colección.
                    coincideAlgunTag = !boton.getTags().isEmpty();
                }

                if (coincideAlgunTag) {
                    panelBotones.getChildren().add(boton.getContenedor());
                }
            }

        } else { // Si el texto de búsqueda NO empieza con '#' (búsqueda por nombre de archivo)
            for (botonSonido boton : mapaBotonesSonido.values()) {
                boolean coincideNombre = boton.getNombreArchivo().toLowerCase().contains(textoBusqueda);
                if (coincideNombre) {
                    panelBotones.getChildren().add(boton.getContenedor());
                }
            }
        }
    }
    
    private void abrirVentanaTags() {
        ventanaTags vt = new ventanaTags(tagsGlobales);
        vt.showAndWait();
        tagsGlobales = vt.getTagsActuales(); // actualiza global
        for(String tag: tagsGlobales)
        	System.out.println(tag);
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
