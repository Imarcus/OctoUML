package controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Line;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.*;
import util.Constants;
import util.NetworkUtils;
import util.commands.*;
import util.insertIMG.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import util.persistence.PersistenceManager;
import view.*;
import java.util.*;
import java.awt.geom.Point2D;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;


/**
 * Controls all user inputs and delegates work to other controllers.
 */
public class MainController {
    //For testing with mouse and keyboard
    private Graph graph;
    private Stage aStage;

    boolean mouseCreationActivated = true;

    //Controllers
    CreateNodeController createNodeController;
    NodeController nodeController;
    EdgeController edgeController;
    GraphController graphController;
    SketchController sketchController;
    RecognizeController recognizeController;
    SelectController selectController;
    CopyPasteController copyPasteController;
    VoiceController voiceController;

    //Node lists and maps
    ArrayList<AbstractNodeView> selectedNodes = new ArrayList<>();
    ArrayList<AbstractEdgeView> selectedEdges = new ArrayList<>();
    ArrayList<Sketch> selectedSketches = new ArrayList<>();
    ArrayList<AbstractNodeView> allNodeViews = new ArrayList<>();
    ArrayList<AbstractEdgeView> allEdgeViews = new ArrayList<>();
    ArrayList<AnchorPane> allDialogs = new ArrayList<>();
    HashMap<AbstractNodeView, AbstractNode> nodeMap = new HashMap<>();

    ArrayList<ServerController> serverControllers = new ArrayList<>();
    ArrayList<ClientController> clientControllers = new ArrayList<>();

    boolean selected = false; //A node is currently selected

    private UndoManager undoManager;

    //Mode
    private Mode mode = Mode.NO_MODE;

    public enum Mode {
        NO_MODE, SELECTING, DRAGGING, RESIZING, MOVING, DRAWING, CREATING, CONTEXT_MENU
    }


    //Tool
    private ToolEnum tool = ToolEnum.CREATE_CLASS;

    public enum ToolEnum {
        CREATE_CLASS, SELECT, DRAW, CREATE_PACKAGE, EDGE, MOVE_SCENE
    }

    //Views
    private boolean umlVisible = true;
    private boolean sketchesVisible = true;

    //Selection logic
    private boolean nodeWasDragged = true;


    @FXML private BorderPane aBorderPane;
    @FXML private Pane aDrawPane;
    @FXML private Slider zoomSlider;
    @FXML private ScrollPane aScrollPane;
    @FXML private ColorPicker colorPicker;
    @FXML private Label serverLabel;

    ContextMenu aContextMenu;
    private MainController instance = this;

    @FXML
    public void initialize() {
        initDrawPaneActions();
        initToolBarActions();
        initContextMenu();
        initZoomSlider();
        initColorPicker();

        graph = new Graph();

        createNodeController = new CreateNodeController(aDrawPane, this);
        nodeController = new NodeController(aDrawPane, this);
        graphController = new GraphController(aDrawPane, this, aScrollPane);
        edgeController = new EdgeController(aDrawPane, this);
        sketchController = new SketchController(aDrawPane, this);
        recognizeController = new RecognizeController(aDrawPane, this);
        selectController = new SelectController(aDrawPane, this);
        copyPasteController = new CopyPasteController(aDrawPane, this);
        voiceController = new VoiceController(this);

        undoManager = new UndoManager();

        drawGrid();
    }

    private void initDrawPaneActions() {
        //Makes sure the pane doesn't scroll when using a touch screen.
        aDrawPane.setOnScroll(event -> event.consume());

        //Controlls the look of the cursor
        aDrawPane.addEventHandler(InputEvent.ANY, mouseEvent -> {
            getStage().getScene().setCursor(Cursor.DEFAULT);
            mouseEvent.consume();
        });

        aDrawPane.setOnMousePressed(event -> {
            if (mode == Mode.NO_MODE) {
                if (event.getButton() == MouseButton.SECONDARY) { //Create context menu on right-click.
                    mode = Mode.CONTEXT_MENU;
                    copyPasteController.copyPasteCoords = new double[]{event.getX(), event.getY()};
                    aContextMenu.show(aDrawPane, event.getScreenX(), event.getScreenY());
                }
                else if (tool == ToolEnum.SELECT || tool == ToolEnum.EDGE) { //Start selecting elements.
                    selectController.onMousePressed(event);
                }
                else if ((tool == ToolEnum.CREATE_CLASS || tool == ToolEnum.CREATE_PACKAGE) && mouseCreationActivated) { //Start creation of package or class.
                    mode = Mode.CREATING;
                    createNodeController.onMousePressed(event);
                }
                else if (tool == ToolEnum.MOVE_SCENE) { //Start panning of graph.
                    mode = Mode.MOVING;
                    graphController.movePaneStart(event);
                }
                else if (tool == ToolEnum.DRAW) { //Start drawing.
                    mode = Mode.DRAWING;
                    sketchController.onTouchPressed(event);
                }

            } else if (mode == Mode.CONTEXT_MENU) {
                if (event.getButton() == MouseButton.SECONDARY) {
                    copyPasteController.copyPasteCoords = new double[]{event.getX(), event.getY()};
                    aContextMenu.show(aDrawPane, event.getScreenX(), event.getScreenY());
                } else {
                    aContextMenu.hide();
                }
            }
            event.consume();
        });

        aDrawPane.setOnMouseDragged(event -> {
            if (tool == ToolEnum.SELECT && mode == Mode.SELECTING) { //Continue selection of elements.
                selectController.onMouseDragged(event);
            } else if (tool == ToolEnum.DRAW && mode == Mode.DRAWING) { //Continue drawing.
                sketchController.onTouchMoved(event);
            }
            else if ((tool == ToolEnum.CREATE_CLASS || tool == ToolEnum.CREATE_PACKAGE) && mode == Mode.CREATING && mouseCreationActivated) { //Continue creation of class or package.
                createNodeController.onMouseDragged(event);
            } else if (mode == Mode.MOVING && tool == ToolEnum.MOVE_SCENE) { //Continue panning of graph.
                graphController.movePane(event);
            }
            event.consume();
        });

        aDrawPane.setOnMouseReleased(event -> {
            if (tool == ToolEnum.SELECT && mode == Mode.SELECTING) { //Finish selecting elements.
                selectController.onMouseReleased();
            }
            else if (tool == ToolEnum.DRAW && mode == Mode.DRAWING) { //Finish drawing.
                sketchController.onTouchReleased(event);
                //We only want to move out of drawing mode if there are no other current drawings.
                if (!sketchController.currentlyDrawing()) {
                    mode = Mode.NO_MODE;
                }
            } else
            if (tool == ToolEnum.CREATE_CLASS && mode == Mode.CREATING && mouseCreationActivated) { //Finish creation of class.
                createNodeController.onMouseReleasedClass();
                if (!createNodeController.currentlyCreating()) {
                    mode = Mode.NO_MODE;
                }

            } else if (tool == ToolEnum.CREATE_PACKAGE && mode == Mode.CREATING && mouseCreationActivated) { //Finish creation of package.
                createNodeController.onMouseReleasedPackage();
                if (!createNodeController.currentlyCreating()) {
                    mode = Mode.NO_MODE;
                }
            } else if (mode == Mode.MOVING && tool == ToolEnum.MOVE_SCENE) { //Finish panning of graph.
                graphController.movePaneFinished();
                mode = Mode.NO_MODE;
            }
        });

        //------------------------- Touch ---------------------------------
        //There are specific events for touch when creating and drawing to utilize multitouch. //TODO edge creation multi-user support.
        aDrawPane.setOnTouchPressed(event -> {
            if ((tool == ToolEnum.CREATE_CLASS || tool == ToolEnum.CREATE_PACKAGE) && !mouseCreationActivated) {
                mode = Mode.CREATING;
                createNodeController.onTouchPressed(event);
            } else if (tool == ToolEnum.DRAW) {
                mode = Mode.DRAWING;
                sketchController.onTouchPressed(event);
            }
        });

        aDrawPane.setOnTouchMoved(event -> {
            if ((tool == ToolEnum.CREATE_CLASS || tool == ToolEnum.CREATE_PACKAGE) && mode == Mode.CREATING && !mouseCreationActivated) {
                createNodeController.onTouchDragged(event);
            } else if (tool == ToolEnum.DRAW && mode == Mode.DRAWING) {
                sketchController.onTouchMoved(event);
            }
            event.consume();
        });

        aDrawPane.setOnTouchReleased(event -> {
            if (tool == ToolEnum.CREATE_CLASS && mode == Mode.CREATING && !mouseCreationActivated) {
                createNodeController.onTouchReleasedClass(event);
                if (!createNodeController.currentlyCreating()) {
                    mode = Mode.NO_MODE;
                }

            } else if (tool == ToolEnum.CREATE_PACKAGE && mode == Mode.CREATING && !mouseCreationActivated) {
                createNodeController.onTouchReleasedPackage(event);
                if (!createNodeController.currentlyCreating()) {
                    mode = Mode.NO_MODE;
                }
            } else if (tool == ToolEnum.DRAW && mode == Mode.DRAWING) {
                sketchController.onTouchReleased(event);
                if (!sketchController.currentlyDrawing()) {
                    mode = Mode.NO_MODE;
                }
            }
            event.consume();
        });
    }

    private void initNodeActions(AbstractNodeView nodeView) {
        nodeView.setOnMousePressed(event -> {
            if (event.getClickCount() == 2) { //Open dialog window on double click.
                nodeController.onDoubleClick(nodeView);
                tool = ToolEnum.SELECT;
                setButtonClicked(selectBtn);
            } else if (tool == ToolEnum.MOVE_SCENE) { //Start panning of graph.
                mode = Mode.MOVING;
                graphController.movePaneStart(event);
                event.consume();
            } else if (event.getButton() == MouseButton.SECONDARY) { //Open context menu on left click.
                copyPasteController.copyPasteCoords = new double[]{nodeView.getX() + event.getX(), nodeView.getY() + event.getY()};
                aContextMenu.show(nodeView, event.getScreenX(), event.getScreenY());
            } else if (tool == ToolEnum.SELECT || tool == ToolEnum.CREATE_CLASS) { //Select node
                setTool(ToolEnum.SELECT);
                setButtonClicked(selectBtn);
                if (!(nodeView instanceof PackageNodeView)) {
                    nodeView.toFront();
                }
                if (mode == Mode.NO_MODE) { //Either drag selected elements or resize node.
                    Point2D.Double eventPoint = new Point2D.Double(event.getX(), event.getY());
                    if (eventPoint.distance(new Point2D.Double(nodeView.getWidth(), nodeView.getHeight())) < 20) {  //Resize if event is close to corner of node
                        mode = Mode.RESIZING;
                        nodeController.resizeStart(nodeView);
                    } else {
                        mode = Mode.DRAGGING;
                        if (!selectedNodes.contains(nodeView)) { //Drag
                            selectedNodes.add(nodeView);
                        }
                        drawSelected();
                        nodeController.moveNodesStart(event);
                        sketchController.moveSketchStart(event);
                    }
                }
            } else if (tool == ToolEnum.EDGE) { //Start edge creation.
                mode = Mode.CREATING;
                edgeController.onMousePressed(event);
            }
            event.consume();
        });

        nodeView.setOnMouseDragged(event -> {
            if ((tool == ToolEnum.SELECT || tool == ToolEnum.CREATE_CLASS) && mode == Mode.DRAGGING) { //Continue dragging selected elements
                nodeController.moveNodes(event);
                sketchController.moveSketches(event);
                nodeWasDragged = true;
            } else if (mode == Mode.MOVING && tool == ToolEnum.MOVE_SCENE) { //Continue panning graph.
                graphController.movePane(event);
            } else if ((tool == ToolEnum.SELECT || tool == ToolEnum.CREATE_CLASS) && mode == Mode.RESIZING) { //Continue resizing node.
                nodeController.resize(event);
            } else if (tool == ToolEnum.EDGE && mode == Mode.CREATING) { //Continue creating edge.
                edgeController.onMouseDragged(event);
            }
            event.consume();

        });

        nodeView.setOnMouseReleased(event -> {
            if ((tool == ToolEnum.SELECT || tool == ToolEnum.CREATE_CLASS) && mode == Mode.DRAGGING) { //Finish dragging nodes and create a compound command.
                double[] deltaTranslateVector = nodeController.moveNodesFinished(event);
                sketchController.moveSketchFinished(event);
                CompoundCommand compoundCommand = new CompoundCommand();
                for (AbstractNodeView movedView : selectedNodes) {
                    compoundCommand.add(new MoveGraphElementCommand(nodeMap.get(movedView), deltaTranslateVector[0], deltaTranslateVector[1]));
                }
                for (Sketch sketch : selectedSketches) {
                    compoundCommand.add(new MoveGraphElementCommand(sketch, deltaTranslateVector[0], deltaTranslateVector[1]));
                }
                undoManager.add(compoundCommand);
                if (!nodeWasDragged) {
                    selectedNodes.remove(nodeView);
                    drawSelected();
                    nodeWasDragged = false;
                }
            } else if (mode == Mode.MOVING && tool == ToolEnum.MOVE_SCENE) { //Finish panning of graph.
                graphController.movePaneFinished();
                mode = Mode.NO_MODE;
            } else if ((tool == ToolEnum.SELECT || tool == ToolEnum.CREATE_CLASS) && mode == Mode.RESIZING) { //Finish resizing node.
                nodeController.resizeFinished(nodeMap.get(nodeView));
            } else if (tool == ToolEnum.EDGE && mode == Mode.CREATING) { //Finish creation of edge.
                edgeController.onMouseReleased();
            }
            mode = Mode.NO_MODE;
            event.consume();
        });

        ////////////////////////////////////////////////////////////////

        nodeView.setOnTouchPressed(event -> {
            if (nodeView instanceof PackageNodeView && (tool == ToolEnum.CREATE_CLASS || tool == ToolEnum.CREATE_PACKAGE)) {
                mode = Mode.CREATING;
                createNodeController.onTouchPressed(event);
            } else if (tool == ToolEnum.DRAW) {
                mode = Mode.DRAWING;
                sketchController.onTouchPressed(event);
            }
            event.consume();
        });

        nodeView.setOnTouchMoved(event -> {
            if (nodeView instanceof PackageNodeView && (tool == ToolEnum.CREATE_CLASS || tool == ToolEnum.CREATE_PACKAGE) &&
                    mode == Mode.CREATING) {
                createNodeController.onTouchDragged(event);
            } else if (tool == ToolEnum.DRAW && mode == Mode.DRAWING) {
                sketchController.onTouchMoved(event);
            }
            event.consume();

        });

        nodeView.setOnTouchReleased(event -> {
            if (nodeView instanceof PackageNodeView && tool == ToolEnum.CREATE_CLASS && mode == Mode.CREATING) {
                createNodeController.onTouchReleasedClass(event);
                if (!createNodeController.currentlyCreating()) {
                    mode = Mode.NO_MODE;
                }

            } else if (nodeView instanceof PackageNodeView && tool == ToolEnum.CREATE_PACKAGE && mode == Mode.CREATING) {
                createNodeController.onTouchReleasedPackage(event);
                if (!createNodeController.currentlyCreating()) {
                    mode = Mode.NO_MODE;
                }
            } else if (tool == ToolEnum.DRAW && mode == Mode.DRAWING) {
                sketchController.onTouchReleased(event);
                if (!sketchController.currentlyDrawing()) {
                    mode = Mode.NO_MODE;
                }
            } else if (mode == Mode.MOVING && tool == ToolEnum.MOVE_SCENE) {
                mode = Mode.NO_MODE;
            }
            event.consume();
        });
    }

    //----------------- DELETING ----------------------------------------

    /**
     * Deletes all selected nodes, edges and sketches.
     */
    void deleteSelected() {
        CompoundCommand command = new CompoundCommand();
        for (AbstractNodeView nodeView : selectedNodes) {
            deleteNode(nodeView, command, false, false);
        }
        for (AbstractEdgeView edgeView : selectedEdges) {
            deleteEdgeView(edgeView, command, false, false);
        }
        for (Sketch sketch : selectedSketches) {
            deleteSketch(sketch, command, false);
        }
        selectedNodes.clear();
        selectedEdges.clear();
        selectedSketches.clear();
        undoManager.add(command);
    }

    /**
     * Deletes nodes and its associated edges
     *
     * @param nodeView
     * @param pCommand Compound command from deleting all selected, if null we create our own command.
     * @param undo     If true this is an undo and no command should be created
     * @param remote, If true this command was received from a remote server.
     */
    public void deleteNode(AbstractNodeView nodeView, CompoundCommand pCommand, boolean undo, boolean remote) {
        CompoundCommand command = null;
        if (pCommand == null && !undo) {
            command = new CompoundCommand();
            selectedNodes.remove(nodeView); //Fix for concurrentModificationException
        } else if (!undo) {
            command = pCommand;
        }

        AbstractNode node = nodeMap.get(nodeView);
        deleteNodeEdges(node, command, undo, remote);
        getGraphModel().removeNode(node, remote);
        aDrawPane.getChildren().remove(nodeView);
        allNodeViews.remove(nodeView);

        if (!undo) {
            command.add(new AddDeleteNodeCommand(this, graph, nodeView, node, false));
        }
        if (pCommand == null && !undo) {
            undoManager.add(command);
        }
    }

    /**
     * Deletes edge
     *
     * @param edgeView
     * @param pCommand Compound command from deleting all selected, if null we create our own command.
     * @param remote, true if change comes from a remote server
     * @param undo     If true this is an undo and no command should be created, also used by replaceEdge in EdgeController
     */
    public void deleteEdgeView(AbstractEdgeView edgeView, CompoundCommand pCommand, boolean undo, boolean remote) {
        CompoundCommand command = null;
        //TODO Ugly solution for replace.
        if (pCommand == null) {
            command = new CompoundCommand();
            selectedEdges.remove(edgeView);
        } else if (!undo) {
            command = pCommand;
        }

        AbstractEdge edge = edgeView.getRefEdge();
        graph.removeEdge(edge, remote);
        aDrawPane.getChildren().remove(edgeView);
        edgeView.setSelected(false);
        allEdgeViews.remove(edgeView);
        if (!undo) {
            command.add(new AddDeleteEdgeCommand(this, edgeView, edge, false));
        }
        if (pCommand == null && !undo) {
            undoManager.add(command);
        }
    }

    public void addSketch(Sketch sketch, boolean isImport, boolean remote){
        initSketchActions(sketch);
        aDrawPane.getChildren().add(sketch.getPath());
        if(!isImport){
            undoManager.add(new AddDeleteSketchCommand(instance, aDrawPane, sketch, true));
            graph.addSketch(sketch, remote);
        }
    }

    public void deleteSketch(Sketch sketch, CompoundCommand pCommand, boolean remote) {
        CompoundCommand command;
        if (pCommand == null) {
            command = new CompoundCommand();
        } else {
            command = pCommand;
        }
        selectedSketches.remove(sketch);
        graph.removeSketch(sketch, remote);
        aDrawPane.getChildren().remove(sketch.getPath());
        command.add(new AddDeleteSketchCommand(this, aDrawPane, sketch, false));
    }

    /**
     * Deletes all edges associated with the node
     *
     * @param node
     * @param command
     * @param remote, true if change comes from a remote server
     */
    public void deleteNodeEdges(AbstractNode node, CompoundCommand command, boolean undo, boolean remote) {
        AbstractEdge edge;
        ArrayList<AbstractEdgeView> edgeViewsToBeDeleted = new ArrayList<>();
        for (AbstractEdgeView edgeView : allEdgeViews) {
            edge = edgeView.getRefEdge();
            if (edge.getEndNode().equals(node) || edge.getStartNode().equals(node)) {
                getGraphModel().removeEdge(edgeView.getRefEdge(), remote);
                aDrawPane.getChildren().remove(edgeView);
                selectedEdges.remove(edgeView);
                edgeViewsToBeDeleted.add(edgeView);
                if (!undo && command != null) {
                    command.add(new AddDeleteEdgeCommand(this, edgeView, edgeView.getRefEdge(), false));
                }
            }
        }
        allEdgeViews.removeAll(edgeViewsToBeDeleted);
    }

    /**
     * initialize handlers for a sketch.
     *
     * @param sketch
     */
    private void initSketchActions(Sketch sketch) {
        sketch.getPath().setOnMousePressed(event -> {
            if (mouseCreationActivated) {
                handleOnSketchPressedEvents(sketch);

                //TODO DUPLICATED CODE FROM nodeView.setOnMousePressed()
                if (tool == ToolEnum.SELECT) {
                    if (mode == Mode.NO_MODE) //Move, any kind of node
                    {
                        mode = Mode.DRAGGING;
                        if (!selectedSketches.contains(sketch)) {
                            selectedSketches.add(sketch);
                        }
                        drawSelected();
                        sketchController.moveSketchStart(event);
                    }

                }
            }
            event.consume();
        });

        sketch.getPath().setOnMouseDragged(event -> {
            if (mouseCreationActivated) {
                if (tool == ToolEnum.SELECT && mode == Mode.DRAGGING) {
                    sketchController.moveSketches(event);
                }
            }
            event.consume();
        });

        sketch.getPath().setOnMouseReleased(event -> {
            //TODO DUPLICATED CODE FROM nodeView.setOnMouseReleased()
            if (tool == ToolEnum.SELECT && mode == Mode.DRAGGING) {
                double[] deltaTranslateVector = sketchController.moveSketchFinished(event);
                sketchController.moveSketchFinished(event);
                CompoundCommand compoundCommand = new CompoundCommand();
                for (AbstractNodeView movedView : selectedNodes) {
                    compoundCommand.add(new MoveGraphElementCommand(nodeMap.get(movedView), deltaTranslateVector[0], deltaTranslateVector[1]));
                }
                for (Sketch sketch1 : selectedSketches) {
                    compoundCommand.add(new MoveGraphElementCommand(sketch1, deltaTranslateVector[0], deltaTranslateVector[1]));
                }
                undoManager.add(compoundCommand);
                drawSelected();
            }
            mode = Mode.NO_MODE;
            event.consume();
        });

        sketch.getPath().setOnTouchPressed(event -> {
            if (!mouseCreationActivated) {
                handleOnSketchPressedEvents(sketch);
            }
        });

    }

    private void handleOnSketchPressedEvents(Sketch sketch) {
        if (sketch.isSelected()) {
            selectedSketches.remove(sketch);
            sketch.setSelected(false);
        } else {
            selectedSketches.add(sketch);
            sketch.setSelected(true);
        }
    }

    public List<Sketch> getSelectedSketches() {
        return selectedSketches;
    }

    //---------------------- MENU HANDLERS ---------------------------------


    public void handleMenuActionUML() {
        List<Button> umlButtons = Arrays.asList(createBtn, packageBtn, edgeBtn);

        if (umlVisible) {
            for (AbstractNodeView nodeView : allNodeViews) {
                aDrawPane.getChildren().remove(nodeView);
            }
            for (AbstractEdgeView edgView : allEdgeViews) {
                aDrawPane.getChildren().remove(edgView);
            }
            setButtons(true, umlButtons);
            umlVisible = false;
        } else {
            for (AbstractNodeView nodeView : allNodeViews) {
                aDrawPane.getChildren().add(nodeView);
            }
            for (AbstractEdgeView edgView : allEdgeViews) {
                aDrawPane.getChildren().add(edgView);
            }
            setButtons(false, umlButtons);
            umlVisible = true;
            sketchesToFront();
        }
    }

    public void handleMenuActionSketches() {
        if (sketchesVisible) {
            for (Sketch sketch : graph.getAllSketches()) {
                aDrawPane.getChildren().remove(sketch.getPath());
            }

            setButtons(true, Collections.singletonList(drawBtn));

            //sketchesMenuItem.setSelected(false);
            sketchesVisible = false;
        } else {
            for (Sketch sketch : graph.getAllSketches()) {
                aDrawPane.getChildren().add(sketch.getPath());
            }

            setButtons(false, Collections.singletonList(drawBtn));
            //sketchesMenuItem.setSelected(true);
            sketchesVisible = true;
        }
    }

    public void handleMenuActionGrid() {
        if (isGridVisible()) {
            setGridVisible(false);
        } else {
            setGridVisible(true);
        }
    }

    public void handleMenuActionSnapToGrid(boolean b) {
        nodeController.setSnapToGrid(b);
    }

    public void handleMenuActionSnapIndicators(boolean b) {
        nodeController.setSnapIndicators(b);
    }

    /**
     * Disables or enables buttons provided in the list.
     *
     * @param disable
     * @param buttons
     */
    private void setButtons(boolean disable, List<Button> buttons) {
        for (Button button : buttons) {
            button.setDisable(disable);
        }
        selectBtn.fire();
    }

    //------------------------- SAVE-LOAD FEATURE ---------------------------

    public void handleMenuActionMouse() {
        mouseCreationActivated = !mouseCreationActivated;
        //mouseMenuItem.setSelected(mouseCreationActivated);
    }

    public void handleMenuActionExit() {
        Platform.exit();
    }

    public void handleMenuActionSave() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Diagram");
        if (!graph.getName().equals("")) {
            fileChooser.setInitialFileName(graph.getName() + ".xml");
        } else {
            fileChooser.setInitialFileName("mydiagram.xml");
        }
        File file = fileChooser.showSaveDialog(getStage());
        String graphName = file.getName().subSequence(0, file.getName().indexOf('.')).toString();
        graph.setName(graphName);
        PersistenceManager.exportXMI(graph, file.getAbsolutePath());
    }

    public void handleMenuActionLoad() {
        final FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(getStage());
        fileChooser.setTitle("Choose XML-file");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML", "*.xml"));
        Graph graph = null;
        if (file != null) {
            graph = PersistenceManager.importXMIFromPath(file.getAbsolutePath());
        }
        load(graph, false);
    }

    public void handleMenuActionInsert (){
        InsertIMG insertIMG = new InsertIMG(aStage, aDrawPane);
        insertIMG.openFileChooser(this, new Point2D.Double(0,0));
    }

    public void handleMenuActionNew() {
        reset();
    }

    public void handleMenuActionServer(){
        TextInputDialog portDialog = new TextInputDialog("54555");
        portDialog.setTitle("Server Port");
        portDialog.setHeaderText("Please enter port number");
        portDialog.setContentText("Port:");

        Optional<String> port = portDialog.showAndWait();

        //TODO how to handle these?
        ServerController server = new ServerController(graph, this, Integer.parseInt(port.get()));
        serverControllers.add(server);
    }

    public boolean handleMenuActionClient(){

        String[] result = NetworkUtils.queryServerPort();

        if (result != null) {
            ClientController client = new ClientController(this, result[0], Integer.parseInt(result[1]));
            if(!client.connect()){
                client.close();
                return false;
            } else {
                clientControllers.add(client);
                return true;
            }
        } else {
            return false;
        }
    }

    public void setServerLabel(String s){
        serverLabel.setText(s);
    }

    public void closeServers(){
        for (ServerController server : serverControllers) {
            server.closeServer();
        }
    }

    public void closeClients(){
        for(ClientController client : clientControllers){
            client.closeClient();
        }
    }

    public void handleMenuActionImage(){
        try{

            SnapshotParameters sp = new SnapshotParameters();
            Bounds bounds = aScrollPane.getViewportBounds();
            //Not sure why abs is needed, the minX/Y values are negative.
            sp.setViewport(new Rectangle2D(Math.abs(bounds.getMinX()), Math.abs(bounds.getMinY()), bounds.getWidth(), bounds.getHeight()));
            WritableImage image = aDrawPane.snapshot(sp, new WritableImage((int)bounds.getWidth(),(int)bounds.getHeight()));
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Image");
            File output = fileChooser.showSaveDialog(getStage());
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", output);
        } catch (IOException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    //------------------------- Context Menu ---------------------------------
    private void initContextMenu() {
        aContextMenu = new ContextMenu();

        MenuItem cmItemDelete = new MenuItem("Delete");
        cmItemDelete.setOnAction(event -> {
            //TODO Is this really needed? Why just not delete all selected?
            if (aContextMenu.getOwnerNode() instanceof AbstractNodeView) {
                deleteNode((AbstractNodeView) aContextMenu.getOwnerNode(), null, false, false);
            }
            deleteSelected();
        });


        MenuItem cmItemCopy = new MenuItem("Copy");
        cmItemCopy.setOnAction(e -> {
            copyPasteController.copy();
            mode = Mode.NO_MODE;
        });

        MenuItem cmItemPaste = new MenuItem("Paste");
        cmItemPaste.setOnAction(e -> {
            copyPasteController.paste();
            mode = Mode.NO_MODE;
        });

        MenuItem cmItemInsertImg = new MenuItem("Insert Image");
        cmItemInsertImg.setOnAction(event -> {
            Point2D.Double point = new Point2D.Double(copyPasteController.copyPasteCoords[0], copyPasteController.copyPasteCoords[1]);
            InsertIMG insertImg = new InsertIMG(aStage, aDrawPane);
            insertImg.openFileChooser(MainController.this, point);
        });

        aContextMenu.getItems().addAll(cmItemCopy, cmItemPaste, cmItemDelete,cmItemInsertImg);
    }

    /**
     * Creates and adds a new NodeView
     *
     * @param node
     * @return
     */
    public AbstractNodeView createNodeView(AbstractNode node, boolean remote) {
        AbstractNodeView newView;
        if (node instanceof ClassNode) {
            newView = new ClassNodeView((ClassNode) node);
        } else /*if (node instanceof PackageNode)*/ {
            newView = new PackageNodeView((PackageNode) node);
        }

        if(!graph.getAllNodes().contains(node)){
            graph.addNode(node, remote);
            undoManager.add(new AddDeleteNodeCommand(MainController.this, graph, newView, node, true));
        }
        return addNodeView(newView, node);
    }

    /**
     * Adds a NodeView
     *
     * @param nodeView
     * @param node
     * @return
     */
    public AbstractNodeView addNodeView(AbstractNodeView nodeView, AbstractNode node) {
        aDrawPane.getChildren().add(nodeView);
        initNodeActions(nodeView);
        nodeMap.put(nodeView, node);
        allNodeViews.add(nodeView);
        if(nodeView instanceof ClassNodeView){
            nodeView.toFront();
        } else {//if (nodeView instanceof PackageNodeView)
            nodeView.toBack();
            gridToBack();
        }
        return nodeView;
    }

    public PictureNodeView createPictureView (ImageView view, Image image, Point2D.Double point){
        PictureNode picNode = new PictureNode(image, point.getX(), point.getY(), view.getImage().getWidth(), view.getImage().getHeight());
        PictureNodeView picView = new PictureNodeView(view, picNode);
        picNode.setTranslateX(point.getX());
        picNode.setTranslateY(point.getY());
        picView.setX(point.getX());
        picView.setY(point.getY());
        aDrawPane.getChildren().add(picView);
        initNodeActions(picView);
        allNodeViews.add(picView);
        graph.addNode(picNode, false);
        nodeMap.put(picView, picNode);
        return picView;
    }

    //TODO Have this somewhere else?
    /**
     * Called when the model has been modified remotely.
     * @param dataArray
     * [0] = Type of change
     * [1] = id of node
     * [2+] = Optional new values
     */
    public void remoteCommand(String[] dataArray){
        if(dataArray[0].equals(Constants.changeSketchPoint)){
            for(Sketch sketch : graph.getAllSketches()){
                if(dataArray[1].equals(sketch.getId())){
                    sketch.addPointRemote(Double.parseDouble(dataArray[2]), Double.parseDouble(dataArray[3]));
                }
            }
        }
        else if (dataArray[0].equals(Constants.changeSketchStart)){
            for(Sketch sketch : graph.getAllSketches()){
                if(dataArray[1].equals(sketch.getId())){
                    sketch.setStartRemote(Double.parseDouble(dataArray[2]), Double.parseDouble(dataArray[3]));
                    sketch.setColor(Color.web(dataArray[4]));
                }
            }
        }
        else if (dataArray[0].equals(Constants.sketchAdd)){
            addSketch(new Sketch(), false, true);
        }
        else if (dataArray[0].equals(Constants.sketchRemove)){
            Sketch sketchToBeDeleted = null;
            for(Sketch sketch : graph.getAllSketches()){
                if(dataArray[1].equals(sketch.getId())){
                    sketchToBeDeleted = sketch; //ConcurrentModificationException fix
                    break;
                }
            }
            deleteSketch(sketchToBeDeleted, null, true);
        }
        else if(dataArray[0].equals(Constants.changeNodeTranslateY) || dataArray[0].equals(Constants.changeNodeTranslateX)){
            for(AbstractNode node : graph.getAllNodes()){
                if(dataArray[1].equals(node.getId())){
                    node.remoteSetTranslateX(Double.parseDouble(dataArray[2]));
                    node.remoteSetTranslateY(Double.parseDouble(dataArray[3]));
                    node.remoteSetX(Double.parseDouble(dataArray[2]));
                    node.remoteSetY(Double.parseDouble(dataArray[3]));
                    break;
                }
            }
        } else if (dataArray[0].equals(Constants.changeNodeWidth) || dataArray[0].equals(Constants.changeNodeHeight)) {
            for(AbstractNode node : graph.getAllNodes()){
                if(dataArray[1].equals(node.getId())){
                    node.remoteSetWidth(Double.parseDouble(dataArray[2]));
                    node.remoteSetHeight(Double.parseDouble(dataArray[3]));
                    break;
                }
            }
        } else if (dataArray[0].equals(Constants.changeNodeTitle)){
            for(AbstractNode node : graph.getAllNodes()){
                if(dataArray[1].equals(node.getId())){
                    node.remoteSetTitle(dataArray[2]);
                    break;
                }
            }
        } else if (dataArray[0].equals(Constants.NodeRemove)) {
            AbstractNodeView nodeToBeDeleted = null;
            for(AbstractNodeView nodeView : allNodeViews){
                if(dataArray[1].equals(nodeView.getRefNode().getId())){
                    nodeToBeDeleted = nodeView; //ConcurrentModificationException fix
                    break;
                }
            }
            deleteNode(nodeToBeDeleted, null, false, true);
        } else if (dataArray[0].equals(Constants.EdgeRemove)) {
            AbstractEdgeView edgeToBeDeleted = null;
            for(AbstractEdgeView edgeView : allEdgeViews){
                if(dataArray[1].equals(edgeView.getRefEdge().getId())){
                    edgeToBeDeleted = edgeView;
                    break;
                }
            }
            deleteEdgeView(edgeToBeDeleted, null, false, true);
        } else if (dataArray[0].equals(Constants.changeClassNodeAttributes) ||dataArray[0].equals(Constants.changeClassNodeOperations)){
            for(AbstractNode node : graph.getAllNodes()){
                if(dataArray[1].equals(node.getId())){
                    ((ClassNode)node).remoteSetAttributes(dataArray[2]);
                    ((ClassNode)node).remoteSetOperations(dataArray[3]);
                    break;
                }
            }
        } else if (dataArray[0].equals(Constants.changeEdgeStartMultiplicity) || dataArray[0].equals(Constants.changeEdgeEndMultiplicity)){
            for(Edge edge : graph.getAllEdges()){
                if(dataArray[1].equals(edge.getId())){
                    ((AbstractEdge) edge).remoteSetStartMultiplicity(dataArray[2]);
                    ((AbstractEdge) edge).remoteSetEndMultiplicity(dataArray[3]);
                }
            }
        } else if (dataArray[0].equals(Constants.changeSketchTranslateX)) {
            for(Sketch sketch : graph.getAllSketches()){
                if(dataArray[1].equals(sketch.getId())){
                    sketch.remoteSetTranslateX(Double.parseDouble(dataArray[2]));
                }
            }
        } else if (dataArray[0].equals(Constants.changeSketchTranslateY)) {
            for(Sketch sketch : graph.getAllSketches()){
                if(dataArray[1].equals(sketch.getId())){
                    sketch.remoteSetTranslateY(Double.parseDouble(dataArray[2]));
                }
            }
        }
    }

    /**
     * Creates and adds a new EdgeView
     *
     * @param edge
     * @param startNodeView
     * @param endNodeView
     * @return
     */
    public AbstractEdgeView createEdgeView(AbstractEdge edge, AbstractNodeView startNodeView,
                                           AbstractNodeView endNodeView) {
        AbstractEdgeView edgeView;
        if (edge instanceof AssociationEdge) {
            edgeView = new AssociationEdgeView(edge, startNodeView, endNodeView);
        } else if (edge instanceof AggregationEdge) {
            edgeView = new AggregationEdgeView(edge, startNodeView, endNodeView);
        } else if (edge instanceof CompositionEdge) {
            edgeView = new CompositionEdgeView(edge, startNodeView, endNodeView);
        } else if (edge instanceof InheritanceEdge) {
            edgeView = new InheritanceEdgeView(edge, startNodeView, endNodeView);
        } else {
            edgeView = null;
        }
        return addEdgeView(edgeView);
    }

    /**
     * Adds an EdgeView
     *
     * @param edgeView
     * @return
     */
    public AbstractEdgeView addEdgeView(AbstractEdgeView edgeView) {
        if (edgeView != null) {
            aDrawPane.getChildren().add(edgeView);
            graph.addEdge(edgeView.getRefEdge(), false);
            allEdgeViews.add(edgeView);
        }
        undoManager.add(new AddDeleteEdgeCommand(MainController.this, edgeView, edgeView.getRefEdge(), true));
        return edgeView;
    }

    /**
     * @param edge
     * @param remote, true if change comes from a remote server
     * @return null if graph already hasEdge or start/endnodeview is null. Otherwise the created AbstractEdgeView.
     */
    public AbstractEdgeView addEdgeView(AbstractEdge edge, boolean remote) {
        AbstractNodeView startNodeView = null;
        AbstractNodeView endNodeView = null;
        AbstractNode tempNode;
        for (AbstractNodeView nodeView : allNodeViews) {
            tempNode = nodeMap.get(nodeView);
            if (((AbstractNode)edge.getStartNode()).getId().equals(tempNode.getId())) {
                edge.setStartNode(tempNode);
                startNodeView = nodeView;
            } else if (((AbstractNode)edge.getEndNode()).getId().equals(tempNode.getId())) {
                edge.setEndNode(tempNode);
                endNodeView = nodeView;
            }
        }
        AbstractEdgeView edgeView;
        if (edge instanceof AggregationEdge){
            edgeView = new AggregationEdgeView(edge, startNodeView, endNodeView);
        } else if (edge instanceof CompositionEdge) {
            edgeView = new CompositionEdgeView(edge, startNodeView, endNodeView);
        } else if (edge instanceof InheritanceEdge) {
            edgeView = new InheritanceEdgeView(edge, startNodeView, endNodeView);
        } else { //Association
            edgeView = new AssociationEdgeView(edge, startNodeView, endNodeView);
        }
        allEdgeViews.add(edgeView);
        aDrawPane.getChildren().add(edgeView);
        if(!graph.getAllEdges().contains(edge)){
            graph.addEdge(edge, remote);
        }
        return edgeView;
    }

    /**
     * Resets the program, removes everything on the canvas
     */
    private void reset() {
        graph = new Graph();
        aDrawPane.getChildren().clear();
        nodeMap.clear();
        allNodeViews.clear();
        zoomSlider.setValue(zoomSlider.getMax() / 2);
        graphController.resetDrawPaneOffset();
        undoManager = new UndoManager();
        drawGrid();
    }

    /**
     * Removes everything on the canvas and loads the given graph
     * @param pGraph The graph to be loaded
     * @param remote True if graph comes from a remote server
     */
    public void load(Graph pGraph, boolean remote) {
        reset();

        if (pGraph != null) {
            this.graph = pGraph;
            for (AbstractNode node : graph.getAllNodes()) {
                AbstractNode.incrementObjectCount();
                createNodeView(node, remote);
                graph.listenToElement(node);
            }

            for (Edge edge : graph.getAllEdges()) {
                AbstractEdge.incrementObjectCount();
                addEdgeView((AbstractEdge) edge, remote);
            }

            for(Sketch sketch : graph.getAllSketches()){
                Sketch.incrementObjectCount();
                addSketch(sketch, true, remote);
                graph.listenToElement(sketch);
            }
        }
    }

    //------------------------------------ GRID -------------------------------
    //TODO move this to graph controller
    private ArrayList<Line> grid = new ArrayList<>();
    private HashMap<Integer, Line> xGrid = new HashMap<>();
    private HashMap<Integer, Line> yGrid = new HashMap<>();

    public ArrayList<Line> getGrid() {
        return grid;
    }

    private void drawGrid() {
        grid.clear();
        for (int i = 0; i < 8000; i += Constants.GRID_DISTANCE) {
            Line line1 = new Line(i, 0, i, 8000);
            line1.setStroke(Color.LIGHTGRAY);
            Line line2 = new Line(0, i, 8000, i);
            line2.setStroke(Color.LIGHTGRAY);
            grid.add(line1);
            grid.add(line2);
            aDrawPane.getChildren().addAll(line1, line2);
        }
    }

    public void gridToBack() {
        for (Line line : grid) {
            line.toBack();
        }
    }

    private boolean isGridVisible = true;

    public void setGridVisible(boolean visible) {
        for (Line line : grid) {
            line.setVisible(visible);
        }
        isGridVisible = visible;
        //gridMenuItem.setSelected(visible);
    }

    public boolean isGridVisible() {
        return isGridVisible;
    }

    public void sketchesToFront() {
        for (Sketch sketch : graph.getAllSketches()) {
            sketch.getPath().toFront();
        }
    }

    //------------------------ Zoom-feature -------------------------------------

    private void initZoomSlider() {

        zoomSlider.valueProperty().addListener((ov, old_val, new_val) -> {
            if (zoomSlider.isValueChanging()) {
                graphController.zoomPane(new_val.doubleValue());
            }
        });
        zoomSlider.setShowTickMarks(true);
        zoomSlider.setPrefWidth(200);
    }

    public double getZoomScale() {
        return zoomSlider.getValue();
    }

    //------------------------ misc. getters -------------------------------------

    public Stage getStage() {
        return aStage;
    }

    public void setStage(Stage pStage) {
        this.aStage = pStage;
    }

    protected HashMap<AbstractNodeView, AbstractNode> getNodeMap() {
        return nodeMap;
    }

    public Graph getGraphModel() {
        return graph;
    }

    public ArrayList<AbstractNodeView> getSelectedNodes() {
        return selectedNodes;
    }

    public ArrayList<AbstractNodeView> getAllNodeViews() {
        return allNodeViews;
    }


    public ArrayList<AbstractEdgeView> getAllEdgeViews() {
        return allEdgeViews;
    }

    public UndoManager getUndoManager() {
        return undoManager;
    }

    public void setMode(Mode pMode) {
        mode = pMode;
    }

    public ScrollPane getScrollPane(){
        return aScrollPane;
    }

    public ToolEnum getTool() {
        return tool;
    }

    public void setTool(ToolEnum pTool) {
        tool = pTool;
    }

    public void addDialog(AnchorPane dialog) {
        allDialogs.add(dialog);
    }

    public boolean removeDialog(AnchorPane dialog) {
        mode = Mode.NO_MODE;
        return allDialogs.remove(dialog);
    }

    public ArrayList<AnchorPane> getAllDialogs() {
        return allDialogs;
    }

    /**
     * Visualises which graph elements are selected and which are not.
     */
    void drawSelected() {
        for (AbstractNodeView nodeView : allNodeViews) {
            if (selectedNodes.contains(nodeView)) {
                nodeView.setSelected(true);
            } else {
                nodeView.setSelected(false);
            }
        }
        for (AbstractEdgeView edgeView : allEdgeViews) {
            if (selectedEdges.contains(edgeView)) {
                edgeView.setSelected(true);
            } else {
                edgeView.setSelected(false);
            }
        }
        for (Sketch sketch : graph.getAllSketches()) {
            if (selectedSketches.contains(sketch)) {
                sketch.setSelected(true);
                sketch.getPath().toFront();
            } else {
                sketch.setSelected(false);
                sketch.getPath().toFront();
            }
        }
    }

    //------------ Init Buttons -------------------------------------------

    @FXML
    Button createBtn, packageBtn, edgeBtn, selectBtn, drawBtn, undoBtn, redoBtn, moveBtn, deleteBtn, recognizeBtn, voiceBtn;
    Button buttonInUse;

    private void initToolBarActions() {

        Image image = new Image("/icons/classw.png");
        createBtn.setGraphic(new ImageView(image));
        createBtn.setText("");

        image = new Image("/icons/packagew.png");
        packageBtn.setGraphic(new ImageView(image));
        packageBtn.setText("");

        image = new Image("/icons/edgew.png");
        edgeBtn.setGraphic(new ImageView(image));
        edgeBtn.setText("");

        image = new Image("/icons/selectw.png");
        selectBtn.setGraphic(new ImageView(image));
        selectBtn.setText("");

        image = new Image("/icons/undow.png");
        undoBtn.setGraphic(new ImageView(image));
        undoBtn.setText("");

        image = new Image("/icons/redow.png");
        redoBtn.setGraphic(new ImageView(image));
        redoBtn.setText("");

        image = new Image("/icons/movew.png");
        moveBtn.setGraphic(new ImageView(image));
        moveBtn.setText("");

        image = new Image("/icons/deletew.png");
        deleteBtn.setGraphic(new ImageView(image));
        deleteBtn.setText("");

        image = new Image("/icons/draww.png");
        drawBtn.setGraphic(new ImageView(image));
        drawBtn.setText("");

        image = new Image("/icons/recow.png");
        recognizeBtn.setGraphic(new ImageView(image));
        recognizeBtn.setText("");

        image = new Image("/icons/micw.png");
        voiceBtn.setGraphic(new ImageView(image));
        voiceBtn.setText("");

        buttonInUse = createBtn;
        buttonInUse.getStyleClass().add("button-in-use");


        //---------------------- Actions for buttons ----------------------------
        createBtn.setOnAction(event -> {
            tool = ToolEnum.CREATE_CLASS;
            setButtonClicked(createBtn);
        });

        packageBtn.setOnAction(event -> {
            tool = ToolEnum.CREATE_PACKAGE;
            setButtonClicked(packageBtn);
        });

        edgeBtn.setOnAction(event -> {
            tool = ToolEnum.EDGE;
            setButtonClicked(edgeBtn);
        });

        selectBtn.setOnAction(event -> {
            tool = ToolEnum.SELECT;
            setButtonClicked(selectBtn);
        });

        drawBtn.setOnAction(event -> {
            tool = ToolEnum.DRAW;
            setButtonClicked(drawBtn);
        });

        moveBtn.setOnAction(event -> {
            setButtonClicked(moveBtn);
            tool = ToolEnum.MOVE_SCENE;
        });

        undoBtn.setOnAction(event -> undoManager.undoCommand());

        redoBtn.setOnAction(event -> undoManager.redoCommand());

        deleteBtn.setOnAction(event -> deleteSelected());

        recognizeBtn.setOnAction(event -> recognizeController.recognize(selectedSketches));

        voiceBtn.setOnAction(event -> voiceController.onVoiceButtonClick());
    }

    private void initColorPicker(){
        colorPicker.setValue(Color.BLACK);
        colorPicker.setOnAction(t -> sketchController.color = colorPicker.getValue());
    }
    void setButtonClicked(Button b) {
        buttonInUse.getStyleClass().remove("button-in-use");
        buttonInUse = b;
        buttonInUse.getStyleClass().add("button-in-use");
    }


    //----------------------------------- VOICE --------------------------------------------




}

