package application;
import vista.vista;

import java.net.URL;

import controlador.controlador;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;

public class Main extends Application {
	
	private vista vista;
	private controlador controlador;
	
	@Override
	public void start(Stage primaryStage) {
		
		vista vista = new vista();
		controlador = new controlador(vista);
		
		// Crear la escena con la raíz proporcionada por la vista
        Scene scene = new Scene(vista.getRoot(), 600, 400);
        
        // Configurar el Stage
        primaryStage.setTitle("HardRope");
        primaryStage.setScene(scene);
        primaryStage.show();
     // Cargar el archivo CSS
        
        String css = getClass().getResource("/resources/estilosBoton.css").toExternalForm();
        scene.getStylesheets().add(css);	
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
