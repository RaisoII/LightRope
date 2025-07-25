package vista;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

public class crearMenuContextual {
	
	  public static  ContextMenu menuContextual(vista vista) {
	        
		  ContextMenu menu = new ContextMenu();

	        // Crear ítem de menú "Edit"
	        MenuItem item1 = new MenuItem("Edit");
	        item1.setOnAction(e -> vista.abrirVentanaEditarSonido());

	        // Crear ítem de menú "Delete"
	        MenuItem item2 = new MenuItem("Delete");
	        item2.setOnAction(e -> {
	            // Acción para "Delete"
	            System.out.println("será prontamente eliminada...");
	        });

	        // Agregar los ítems al menú contextual
	        menu.getItems().addAll(item1, item2);

	        return menu;
	    }

	    /*private static void abrirVentanaEditarSonido() {
	        // Lógica para abrir la ventana de edición de sonido
	        Stage ventanaEdicion = new Stage();
	        ventanaEdicion.setTitle("Edit Sound");
	        ventanaEdicion.show();
	    }*/
	}

