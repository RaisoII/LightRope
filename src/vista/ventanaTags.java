package vista;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.HashSet;
import java.util.Set;

public class ventanaTags extends Stage {

    private Set<String> tags;
    private FlowPane panelTags; // CAMBIO CLAVE: Usamos FlowPane en lugar de TilePane

    public ventanaTags(Set<String> tagsGlobales) {
        
        this.tags = new HashSet<>(tagsGlobales);
        setTitle("Tags Manager");
        initModality(Modality.APPLICATION_MODAL);

        // --- Sección de agregar nuevos tags (arriba) ---
        TextField nuevoTagField = new TextField();
        nuevoTagField.setPromptText("New tag");
        nuevoTagField.setMaxWidth(Double.MAX_VALUE);

        nuevoTagField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.contains(" ")) {
                nuevoTagField.setText(newText.replace(" ", ""));
            }
        });
        
        Button agregarBtn = new Button("Add");

        Runnable addTagAction = () -> {
            String nuevoTag = nuevoTagField.getText().trim();
            if (!nuevoTag.isEmpty()) {
                if (!this.tags.contains(nuevoTag)) {
                    this.tags.add(nuevoTag);
                    nuevoTagField.clear();
                    refrescarLista();
                } else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "This tag '" + nuevoTag + "' already exists.", ButtonType.OK);
                    alert.setTitle("Duplicated Tag");
                    alert.setHeaderText(null);
                    alert.showAndWait();
                }
            }
        };

        agregarBtn.setOnAction(e -> addTagAction.run());
        nuevoTagField.setOnAction(e -> addTagAction.run());

        HBox agregarTagContainer = new HBox(5, nuevoTagField, agregarBtn);
        agregarTagContainer.setPadding(new Insets(10, 10, 0, 10));
        HBox.setHgrow(nuevoTagField, Priority.ALWAYS);

        // --- Sección para los tags existentes (scrollable, diseño de matriz) ---
        panelTags = new FlowPane(5, 5); // CAMBIO CLAVE: El constructor establece el espacio
        panelTags.setPadding(new Insets(10));
        panelTags.setAlignment(Pos.TOP_LEFT);

        ScrollPane scrollTags = new ScrollPane(panelTags);
        scrollTags.setFitToWidth(true);
        scrollTags.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // --- Layout principal ---
        BorderPane rootLayout = new BorderPane();
        rootLayout.setTop(agregarTagContainer);
        rootLayout.setCenter(scrollTags);

        VBox.setVgrow(scrollTags, Priority.ALWAYS);

        refrescarLista();

        setScene(new Scene(rootLayout, 350, 450));
    }
    
    // --- Método auxiliar para crear un tag ---
    private HBox crearTagItem(String tag, boolean isDeletable) {
        HBox tagBox = new HBox(3);
        tagBox.setAlignment(Pos.CENTER_LEFT);
        tagBox.setStyle("-fx-background-color: #3870b2; -fx-background-radius: 3; -fx-padding: 3 5 3 5;");
        
        Label tagLabel = new Label(tag);
        tagLabel.setStyle("-fx-text-fill: white; -fx-font-size: 11px;");
        
        tagBox.getChildren().add(tagLabel);
        
        if (isDeletable) {
            Button closeButton = new Button("✕");
            closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-padding: 0; -fx-font-size: 8px;");
            
            closeButton.setOnAction(e -> {
                this.tags.remove(tag);
                refrescarLista();
            });
            
            tagBox.getChildren().add(closeButton);
        }
        
        return tagBox;
    }

    // --- Método que actualiza la lista de tags en la interfaz ---
    public void refrescarLista() {
        panelTags.getChildren().clear();
        for (String tag : tags) {
            HBox tagItem = crearTagItem(tag, true);
            panelTags.getChildren().add(tagItem);
        }
    }
    
    public Set<String> getTagsActuales() {
        return tags;
    }
}