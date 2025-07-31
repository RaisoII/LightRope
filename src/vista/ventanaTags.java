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
    private TilePane panelTags; // Cambiamos a TilePane para el diseño de matriz

    public ventanaTags(Set<String> tagsGlobales) {
        
    	this.tags = new HashSet<>(tagsGlobales);
        setTitle("Tags Manager");
        initModality(Modality.APPLICATION_MODAL);

        // --- Sección de agregar nuevos tags (arriba) ---
        TextField nuevoTagField = new TextField();
        nuevoTagField.setPromptText("New tag");
        nuevoTagField.setMaxWidth(Double.MAX_VALUE); // Allows the field to grow horizontally

        nuevoTagField.textProperty().addListener((obs, oldText, newText) -> {
            if (newText.contains(" ")) {
                // If the new text contains a space, replace it with nothing.
                // This effectively prevents spaces from being typed.
                nuevoTagField.setText(newText.replace(" ", ""));
            }
        });
        
        
        Button agregarBtn = new Button("Add");

        // Centralized logic for adding a tag
        Runnable addTagAction = () -> {
            String nuevoTag = nuevoTagField.getText().trim();
            if (!nuevoTag.isEmpty()) {
                if (!this.tags.contains(nuevoTag)) { // Add check to prevent duplicates
                    this.tags.add(nuevoTag);
                    nuevoTagField.clear();
                    refrescarLista();
                } else {
                    // Optional: Show an alert if the tag already exists
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "This tag '" + nuevoTag + "' already exists.", ButtonType.OK);
                    alert.setTitle("Duplicated Tag");
                    alert.setHeaderText(null);
                    alert.showAndWait();
                }
            }
        };

        // Assign the action to the button
        agregarBtn.setOnAction(e -> addTagAction.run());

        // Assign the action to the TextField when Enter is pressed
        nuevoTagField.setOnAction(e -> addTagAction.run()); // <-- NEW: This line adds the Enter key functionality

        // Container for the text field and the add button
        HBox agregarTagContainer = new HBox(5, nuevoTagField, agregarBtn);
        agregarTagContainer.setPadding(new Insets(10, 10, 0, 10)); // More padding top and sides, less bottom
        HBox.setHgrow(nuevoTagField, Priority.ALWAYS); // Allows the text field to expand

        // --- Section for existing tags (scrollable, matrix layout) ---
        panelTags = new TilePane();
        panelTags.setHgap(5); // Horizontal space between tags
        panelTags.setVgap(5); // Vertical space between tags
        panelTags.setPadding(new Insets(10)); // Padding around all tags
        panelTags.setAlignment(Pos.TOP_LEFT); // Aligns tags to the left
        panelTags.setPrefColumns(3); // Preferred number of columns, you can adjust this

        ScrollPane scrollTags = new ScrollPane(panelTags);
        scrollTags.setFitToWidth(true); // Ensures content fits the ScrollPane's width
        scrollTags.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // Shows vertical scrollbar only if needed

        // --- Main Layout ---
        // We use BorderPane to place the add section at the top and the tags list in the center
        BorderPane rootLayout = new BorderPane();
        rootLayout.setTop(agregarTagContainer); // The add section goes at the top
        rootLayout.setCenter(scrollTags); // The ScrollPane with tags goes in the center

        // Ensures the scroll pane grows with the window
        VBox.setVgrow(scrollTags, Priority.ALWAYS);

        // Update the initial list of tags
        refrescarLista();

        setScene(new Scene(rootLayout, 350, 450)); // Adjust initial window size
    }

    private void refrescarLista() {
        panelTags.getChildren().clear(); // Clear the TilePane instead of the VBox
        for (String tag : tags) {
            // Each tag is represented as an HBox with a Label and a Button
            HBox tagItem = new HBox(5); // Space between the label and the delete button
            tagItem.setAlignment(Pos.CENTER_LEFT); // Aligns content within the HBox

            Label tagLabel = new Label("#" + tag);
            tagLabel.setStyle("-fx-font-size: 12px; -fx-padding: 3 5; -fx-background-color: #e0e0e0; -fx-border-radius: 3; -fx-background-radius: 3;"); // Style for the tag

            Button eliminarBtn = new Button("✕");
            eliminarBtn.setStyle("-fx-font-size: 10px; -fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 1 4; -fx-min-width: 20px; -fx-min-height: 20px; -fx-background-radius: 10; -fx-border-radius: 10;"); // Delete button style
            eliminarBtn.setOnAction(e -> {
                this.tags.remove(tag); // Remove the tag from the set
                refrescarLista(); // Redraw the list
            });

            tagItem.getChildren().addAll(tagLabel, eliminarBtn);
            panelTags.getChildren().add(tagItem); // Add the HBox to the TilePane
        }
    }

    public Set<String> getTagsActuales() {
        return tags;
    }
}