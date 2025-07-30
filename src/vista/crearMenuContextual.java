package vista;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

public class crearMenuContextual {
	
	  public static  ContextMenu menuContextual(vista vista,int idBoton) {
	        
		  ContextMenu menu = new ContextMenu();

	        // Crear ítem de menú "Edit"
	        MenuItem itemEdit = new MenuItem("Edit");
	        MenuItem itemPicture = new MenuItem("Load Picture");
	        MenuItem itemDelete = new MenuItem("Delete");
	        
	        itemEdit.setOnAction(e -> vista.abrirVentanaEditarSonido());

	        itemDelete.setOnAction(e -> {
	            vista.borrarBoton(idBoton);
	        });
	        
	        itemPicture.setOnAction(e ->
	        {
	        	vista.seleccionarImagenParaBoton(idBoton);
	        });
	        // Agregar los ítems al menú contextual
	        menu.getItems().addAll(itemEdit,itemPicture,itemDelete);

	        return menu;
	    }
	}

