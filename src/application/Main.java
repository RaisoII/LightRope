package application;
import vista.vista;


import controlador.controlador;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;

public class Main extends Application {
	
	@SuppressWarnings("unused")
	private vista vista;
	private controlador controlador;
	
	@Override
	public void start(Stage primaryStage) {
		
		try {
            System.out.println("Aplicación arrancada correctamente");
            vista vista = new vista();
    		controlador = new controlador(vista);
    		vista.setControlador(controlador);
    		// Crear la escena con la raíz proporcionada por la vista
            Scene scene = new Scene(vista.getRoot(), 600, 400);
            
            // Configurar el Stage
            primaryStage.setTitle("HardRope");
            primaryStage.setScene(scene);
            primaryStage.show();
         // Cargar el archivo CSS
            
            String css = getClass().getResource("/resources/estilosBoton.css").toExternalForm();
            scene.getStylesheets().add(css);	
    	
        } catch (Exception e) {
            e.printStackTrace();  // Esto debería mostrar cualquier excepción en la consola
        }
	}
	
	public static void main(String[] args) {
		System.out.println("inciando...");
		launch(args);
	}
}
