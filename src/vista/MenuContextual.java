package vista;

import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;

public class MenuContextual extends ContextMenu {
	
	private CheckMenuItem itemLoop;
	
	public MenuContextual(vista vista, boolean isLoop, int idBoton) {        

        MenuItem itemEdit = new MenuItem("Edit");
        itemLoop = new CheckMenuItem("Loop");
        MenuItem itemPicture = new MenuItem("Load Picture");
        MenuItem itemDelete = new MenuItem("Delete");
        itemLoop.setSelected(isLoop);
        
        itemEdit.setOnAction(e -> vista.abrirVentanaEditarSonido());
        
        // Esta es la clave para que no se cierre el menú contextual.
        // Intercepta el evento de liberación del mouse y lo consume.
        itemLoop.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            e.consume();
        });
        
        itemLoop.setOnAction(e -> {
            vista.setearLoopMenuContextual(idBoton, itemLoop.isSelected());
        });
        
        itemDelete.setOnAction(e -> {
            vista.borrarBoton(idBoton);
        });
        
        itemPicture.setOnAction(e -> {
        	vista.seleccionarImagenParaBoton(idBoton);
        });
        
        getItems().addAll(itemEdit, itemLoop, itemPicture, itemDelete);
	}
	  
  	public void actualizarLoop(boolean loop) {
  		itemLoop.setSelected(loop);
  	}
}