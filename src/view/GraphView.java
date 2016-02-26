package view;

import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;


public class GraphView {

    private Pane drawPane;
    private Scene aScene;
    private ToolBar aToolBar;
    private static GraphView instance = null;

    public static GraphView getInstance(){
        if(instance == null){
            instance = new GraphView();
        }
        return instance;
    }

    private GraphView() {
        Group root = new Group();
        aScene = new Scene(root, 1000, 800);


        BorderPane bPane = new BorderPane();
        bPane.prefHeightProperty().bind(aScene.heightProperty());
        bPane.prefWidthProperty().bind(aScene.widthProperty());
        bPane.maxHeightProperty().bind(aScene.heightProperty());
        bPane.maxWidthProperty().bind(aScene.widthProperty());

        drawPane = new Pane();
        aToolBar = initToolBar();
        bPane.setRight(aToolBar);
        bPane.setCenter(drawPane);

        root.getChildren().add(bPane);
    }

    private ToolBar initToolBar(){
        Button createButton = new Button("Create");
        Button selectButton = new Button("Select");
        Button drawButton = new Button("Draw");
        Button packageButton = new Button("Package");
        Button undoButton = new Button("Undo");
        Button redoButton = new Button("Redo");

        ToolBar toolBar = new ToolBar(
                createButton,
                selectButton,
                drawButton,
                packageButton,
                undoButton,
                redoButton
        );
        toolBar.setOrientation(Orientation.VERTICAL);
        return toolBar;
    }

    public ToolBar getToolBar(){
        return aToolBar;
    }

    public Pane getDrawPane(){
        return drawPane;
    }

    public Scene getScene(){
        return aScene;
    }
}
