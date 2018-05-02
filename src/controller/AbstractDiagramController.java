package controller;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.*;
import model.edges.*;
import model.nodes.*;
import util.Constants;
import util.NetworkUtils;
import util.commands.*;
import util.insertIMG.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import util.persistence.PersistenceManager;
import view.edges.*;
import view.nodes.*;

import java.util.*;
import java.awt.geom.Point2D;

import javax.imageio.ImageIO;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.IOException;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Controls all user inputs and delegates work to other controllers.
 */
public abstract class AbstractDiagramController {
    protected Graph graph;
    protected Stage aStage;

    //Set to true for allowing classes & packages & sketches to be created with the mouse.
    // With this set to false, multiple users can create these elements while interacting with a touch screen at the same time
    //TODO Implement multi-touch support for more actions (e.g. creating edges)
    static boolean mouseCreationActivated = false;

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

    protected UndoManager undoManager;

    //Mode
    protected Mode mode = Mode.NO_MODE;
    protected enum Mode {
        NO_MODE, SELECTING, DRAGGING, RESIZING, MOVING, DRAWING, CREATING, CONTEXT_MENU, DRAGGING_EDGE
    }

    //Tool
    protected ToolEnum tool = ToolEnum.CREATE_CLASS;
    protected enum ToolEnum {
        CREATE_CLASS, SELECT, DRAW, CREATE_PACKAGE, EDGE, MOVE_SCENE
    }

    //Views
    private boolean umlVisible = true;
    private boolean sketchesVisible = true;

    @FXML BorderPane borderPane;
    @FXML Pane drawPane;
    @FXML Slider zoomSlider;
    @FXML ScrollPane scrollPane;
    @FXML ColorPicker colorPicker;
    @FXML Label serverLabel;
    @FXML CheckMenuItem mouseMenuItem;

    @FXML
    protected Button createBtn, packageBtn, edgeBtn, selectBtn, drawBtn, undoBtn, redoBtn, moveBtn, deleteBtn, recognizeBtn, voiceBtn;

    ContextMenu aContextMenu;
    private AbstractDiagramController instance = this;


    public void initialize() {
        initDrawPaneActions();
        initContextMenu();
        initZoomSlider();
        initColorPicker();

        graph = new Graph();

        createNodeController = new CreateNodeController(drawPane, this);
        nodeController = new NodeController(drawPane, this);
        graphController = new GraphController(drawPane, this, scrollPane);
        edgeController = new EdgeController(drawPane, this);
        sketchController = new SketchController(drawPane, this);
        recognizeController = new RecognizeController(drawPane, this);
        selectController = new SelectController(drawPane, this);
        copyPasteController = new CopyPasteController(drawPane, this);
        voiceController = new VoiceController(this);

        undoManager = new UndoManager();

        graphController.drawGrid();
    }

    private void initDrawPaneActions() {
        //Makes sure the pane doesn't scroll when using a touch screen.
        drawPane.setOnScroll(event -> event.consume());

        //Controls the look of the cursor, we only want the default look
        drawPane.addEventHandler(InputEvent.ANY, mouseEvent -> {
            getStage().getScene().setCursor(Cursor.DEFAULT);
            mouseEvent.consume();
        });
    }

    abstract void initNodeActions(AbstractNodeView nodeView);

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
        ArrayList<Sketch> deletedSketches = new ArrayList<Sketch>();
        for (Sketch sketch : selectedSketches) {
        	deletedSketches.add(sketch);
        }
        Iterator<Sketch> itr = deletedSketches.iterator();
        while(itr.hasNext()) {
            Sketch element = (Sketch) itr.next();
            deleteSketch(element, command, false);
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
        drawPane.getChildren().remove(nodeView);
        allNodeViews.remove(nodeView);

        if (!undo) {
            command.add(new AddDeleteNodeCommand(this, graph, nodeView, node, false));
        }
        if (pCommand == null && !undo) {
            undoManager.add(command);
        }
    }

    /**
     * Deletes given edge from graph
     *
     * @param edgeView
     * @param pCommand  CompoundCommand from deleting all selected, if null we create our own command.
     * @param remote    True if change comes from a remote server
     * @param undo      If true this is an undo and no command should be created
     */
    public void deleteEdgeView(AbstractEdgeView edgeView, CompoundCommand pCommand, boolean undo, boolean remote) {
        CompoundCommand command = null;
        if (pCommand == null && !undo) { //If this is not part of a compoundcommand
            command = new CompoundCommand();
            selectedEdges.remove(edgeView); //We can safely delete from selectedEdges since we are not looping through it.
        } else if (pCommand != null && !undo) {
            command = pCommand;
        }

        AbstractEdge edge = edgeView.getRefEdge();
        graph.removeEdge(edge, remote);
        drawPane.getChildren().remove(edgeView);
        edgeView.setSelected(false);
        allEdgeViews.remove(edgeView);
        
        if (!undo) {
            command.add(new AddDeleteEdgeCommand(this, edgeView, edge, false));
        }
        if (pCommand == null && !undo) { //If this is not part of a compoundcommand we add this directly to the UndoManager
            undoManager.add(command);
        }
    }

    public void addSketch(Sketch sketch, boolean isImport, boolean remote){
        initSketchActions(sketch);
        drawPane.getChildren().add(sketch.getPath());
        if(!isImport){
            undoManager.add(new AddDeleteSketchCommand(instance, drawPane, sketch, true));
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
        drawPane.getChildren().remove(sketch.getPath());
        command.add(new AddDeleteSketchCommand(this, drawPane, sketch, false));
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
                drawPane.getChildren().remove(edgeView);
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
    
    public ArrayList<AbstractEdgeView> getSelectedEdges() {
        return selectedEdges;
    }

    //---------------------- MENU HANDLERS ---------------------------------

    public void handleMenuActionUML() {
        List<Button> umlButtons = Arrays.asList(createBtn, packageBtn, edgeBtn);

        if (umlVisible) {
            for (AbstractNodeView nodeView : allNodeViews) {
                drawPane.getChildren().remove(nodeView);
            }
            for (AbstractEdgeView edgView : allEdgeViews) {
                drawPane.getChildren().remove(edgView);
            }
            setButtons(true, umlButtons);
            umlVisible = false;
        } else {
            for (AbstractNodeView nodeView : allNodeViews) {
                drawPane.getChildren().add(nodeView);
            }
            for (AbstractEdgeView edgView : allEdgeViews) {
                drawPane.getChildren().add(edgView);
            }
            setButtons(false, umlButtons);
            umlVisible = true;
            graphController.sketchesToFront();
        }
    }

    public void handleMenuActionSketches() {
        if (sketchesVisible) {
            for (Sketch sketch : graph.getAllSketches()) {
                drawPane.getChildren().remove(sketch.getPath());
            }

            setButtons(true, Collections.singletonList(drawBtn));
            sketchesVisible = false;
        } else {
            for (Sketch sketch : graph.getAllSketches()) {
                drawPane.getChildren().add(sketch.getPath());
            }
            setButtons(false, Collections.singletonList(drawBtn));
            sketchesVisible = true;
        }
    }

    public void handleMenuActionGrid() {
        if (graphController.isGridVisible()) {
            graphController.setGridVisible(false);
        } else {
            graphController.setGridVisible(true);
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

    public void createXMI(String path) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            DOMSource source = new DOMSource(PersistenceManager.createXmi(graph));

            StreamResult result = new StreamResult(new File(path));
            transformer.transform(source, result);
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }

    }

    public void handleMenuActionInsert (){
        InsertIMG insertIMG = new InsertIMG(aStage, drawPane);
        insertIMG.openFileChooser(this, new Point2D.Double(0,0));
    }

    public void handleMenuActionNew() {
        reset();
    }

    //---------------------------- Remote Collaboration features ---------------------------------

    public void handleMenuActionServer(){
        TextInputDialog portDialog = new TextInputDialog("54555");
        portDialog.setTitle("Server Port");
        portDialog.setHeaderText("Please enter port number");
        portDialog.setContentText("Port:");

        Optional<String> port = portDialog.showAndWait();

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
            WritableImage image = getSnapShot();
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Image");
            File output = fileChooser.showSaveDialog(getStage());
            ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", output);
        } catch (IOException ex) {
            Logger.getLogger(AbstractDiagramController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public WritableImage getSnapShot(){
        SnapshotParameters sp = new SnapshotParameters();
        Bounds bounds = scrollPane.getViewportBounds();
        //Not sure why abs is needed, the minX/Y values are negative.
        sp.setViewport(new Rectangle2D(Math.abs(bounds.getMinX()), Math.abs(bounds.getMinY()), bounds.getWidth(), bounds.getHeight()));
        return drawPane.snapshot(sp, new WritableImage((int)bounds.getWidth(),(int)bounds.getHeight()));
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
            InsertIMG insertImg = new InsertIMG(aStage, drawPane);
            insertImg.openFileChooser(AbstractDiagramController.this, point);
        });

        aContextMenu.getItems().addAll(cmItemCopy, cmItemPaste, cmItemDelete, cmItemInsertImg);
    }

    /**
     * Creates  a new NodeView from the given node and adds them to the graph.
     *
     * @param node - Any AbstractNode
     * @return the created AbstractNodeView
     */
    public AbstractNodeView createNodeView(AbstractNode node, boolean remote) {
        AbstractNodeView newView;
        if (node instanceof ClassNode) {
            newView = new ClassNodeView((ClassNode) node);
        } else if (node instanceof PackageNode) {
            newView = new PackageNodeView((PackageNode) node);
        } else {
            newView = new SequenceObjectView((SequenceObject) node);
            ((SequenceDiagramController)this).initLifelineHandleActions((SequenceObjectView)newView);
        }

        if(!graph.getAllNodes().contains(node)){
            graph.addNode(node, remote);
            undoManager.add(new AddDeleteNodeCommand(AbstractDiagramController.this, graph, newView, node, true));
        }
        return addNodeView(newView, node);
    }

    /**
     * Adds a NodeView to the graph
     *
     * @param nodeView
     * @param node
     * @return
     */
    public AbstractNodeView addNodeView(AbstractNodeView nodeView, AbstractNode node) {
        drawPane.getChildren().add(nodeView);
        initNodeActions(nodeView);
        nodeMap.put(nodeView, node);
        allNodeViews.add(nodeView);
        if(nodeView instanceof PackageNodeView){
            nodeView.toBack();
            graphController.gridToBack();
        } else { //ClassNode or Lifeline
            nodeView.toFront();
        }
        return nodeView;
    }

    /**
     * Creates a new picture node from the given image and position and adds it to the graph.
     * @param view
     * @param image
     * @param point
     * @return
     */
    public PictureNodeView createPictureView (ImageView view, Image image, Point2D.Double point){
        PictureNode picNode = new PictureNode(image, point.getX(), point.getY(), view.getImage().getWidth(), view.getImage().getHeight());
        PictureNodeView picView = new PictureNodeView(view, picNode);
        picNode.setTranslateX(point.getX());
        picNode.setTranslateY(point.getY());
        picView.setX(point.getX());
        picView.setY(point.getY());
        drawPane.getChildren().add(picView);
        initNodeActions(picView);
        allNodeViews.add(picView);
        graph.addNode(picNode, false);
        nodeMap.put(picView, picNode);
        undoManager.add(new AddDeleteNodeCommand(this, graph, picView, picNode, true));
        return picView;
    }

    //TODO Have this somewhere else?
    /**
     * Called when the model has been modified remotely.
     * @param dataArray
     * [0] = Type of change
     * [1] = id of element
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
        } else if (dataArray[0].equals(Constants.changeLabel)){
            for(Edge edge : graph.getAllEdges()){
                if(dataArray[1].equals(edge.getId())){
                    ((AbstractEdge) edge).remoteSetLabel(dataArray[2]);
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
            drawPane.getChildren().add(edgeView);
            graph.addEdge(edgeView.getRefEdge(), false);
            allEdgeViews.add(edgeView);
        }
        undoManager.add(new AddDeleteEdgeCommand(AbstractDiagramController.this, edgeView, edgeView.getRefEdge(), true));
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
            if (edge.getStartNode().getId().equals(tempNode.getId())) {
                edge.setStartNode(tempNode);
                startNodeView = nodeView;
            } else if (edge.getEndNode().getId().equals(tempNode.getId())) {
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
        drawPane.getChildren().add(edgeView);
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
        drawPane.getChildren().clear();
        nodeMap.clear();
        allNodeViews.clear();
        zoomSlider.setValue(zoomSlider.getMax() / 2);
        undoManager = new UndoManager();
        graphController.drawGrid();
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

    Button buttonInUse;

    private void initColorPicker(){
        colorPicker.setValue(Color.BLACK);
        colorPicker.setOnAction(t -> sketchController.color = colorPicker.getValue());
    }
    void setButtonClicked(Button b) {
        buttonInUse.getStyleClass().remove("button-in-use");
        buttonInUse = b;
        buttonInUse.getStyleClass().add("button-in-use");
    }

    //------------------------ misc. getters and setters -------------------------------------

    Stage getStage() {
        return aStage;
    }

    void setStage(Stage pStage) {
        this.aStage = pStage;
    }

    HashMap<AbstractNodeView, AbstractNode> getNodeMap() {
        return nodeMap;
    }

    public Graph getGraphModel() {
        return graph;
    }

    ArrayList<AbstractNodeView> getSelectedNodes() {
        return selectedNodes;
    }

    ArrayList<AbstractNodeView> getAllNodeViews() {
        return allNodeViews;
    }

    ArrayList<AbstractEdgeView> getAllEdgeViews() {
        return allEdgeViews;
    }

    UndoManager getUndoManager() {
        return undoManager;
    }

    void setMode(Mode pMode) {
        mode = pMode;
    }

    ScrollPane getScrollPane(){
        return scrollPane;
    }

    ToolEnum getTool() {
        return tool;
    }

    void setTool(ToolEnum pTool) {
        tool = pTool;
    }

    void addDialog(AnchorPane dialog) {
        allDialogs.add(dialog);
    }

    boolean removeDialog(AnchorPane dialog) {
        mode = Mode.NO_MODE;
        return allDialogs.remove(dialog);
    }

    void closeLog(){
        undoManager.closeLog();
    }

    public GraphController getGraphController(){
        return graphController;
    }
}

