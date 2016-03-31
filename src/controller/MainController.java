package controller;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Path;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.*;
import util.commands.*;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import util.persistence.PersistenceManager;
import view.*;

import java.awt.geom.Point2D;
import java.io.File;
import java.util.*;

/**
 * Created by marcusisaksson on 2016-02-11.
 */
public class MainController {
    //For testing with mouse and keyboard
    private boolean mouseCreationActivated = false;

    //Controllers
    private CreateNodeController createNodeController;
    private NodeController nodeController;
    private EdgeController edgeController;
    private GraphController graphController;
    private SketchController sketchController;
    private RecognizeController recognizeController;

    private Graph graph;
    private Stage aStage;

    //Node lists and maps
    private ArrayList<AbstractNodeView> selectedNodes = new ArrayList<>();
    private ArrayList<AbstractEdgeView> selectedEdges = new ArrayList<>();
    private ArrayList<Sketch> selectedSketches = new ArrayList<>();
    private ArrayList<AbstractNodeView> allNodeViews = new ArrayList<>();
    private ArrayList<AbstractEdgeView> allEdgeViews = new ArrayList<>();
    private ArrayList<AnchorPane> allDialogs = new ArrayList<>();


    private HashMap<AbstractNodeView, AbstractNode> nodeMap = new HashMap<>();

    //Copy nodes logic
    private ArrayList<AbstractNode> currentlyCopiedNodes = new ArrayList<>();
    private ArrayList<AbstractEdge> currentlyCopiedEdges = new ArrayList<>();
    private HashMap<AbstractNode, double[]> copyDeltas = new HashMap<>();
    private double[] copyPasteCoords;

    //For drag-selecting nodes
    private double selectStartX, selectStartY;
    private Rectangle selectRectangle;

    //For drawing
    private ArrayList<Sketch> allSketches = new ArrayList<>();
    //private Path drawPath;
    private Map<Integer, Path> currentPaths = new HashMap<>();

    private boolean selected = false; //A node is currently selected
    private double currentScale = 1;

    private UndoManager undoManager;

    private Mode mode = Mode.NO_MODE;
    private enum Mode {
        NO_MODE, SELECTING, DRAGGING, RESIZING, ZOOMING, MOVING, DRAWING, CREATING, CONTEXT_MENU
    }

    private ToolEnum tool = ToolEnum.CREATE;
    public enum ToolEnum{
        CREATE, SELECT, DRAW, PACKAGE, EDGE, MOVE_SCENE
    }

    //Views
    private boolean umlVisible = true;
    private boolean sketchesVisible = true;

    //Selection logic
    private boolean nodeWasDragged = true;

    @FXML private Pane aDrawPane;
    @FXML private CheckMenuItem umlMenuItem;
    @FXML private CheckMenuItem sketchesMenuItem;
    @FXML private CheckMenuItem mouseMenuItem;
    @FXML private CheckMenuItem gridMenuItem;
    @FXML private Slider zoomSlider;
    @FXML private BorderPane aBorderPane;

    private ContextMenu aContextMenu;

    private AbstractNodeView nodeClicked;


    @FXML
    public void initialize() {

        selectRectangle = new Rectangle();
        selectRectangle.setFill(null);
        selectRectangle.setStroke(Color.BLACK);
        selectRectangle.getStrokeDashArray().addAll(4.0,5.0,4.0,5.0);


        initDrawPaneActions();
        initToolBarActions();
        initContextMenu();
        initZoomSlider();

        graph = new Graph();

        createNodeController = new CreateNodeController(aDrawPane, this);
        nodeController = new NodeController(aDrawPane, this);
        graphController = new GraphController(aDrawPane, this);
        edgeController = new EdgeController(aDrawPane, this);
        sketchController = new SketchController(aDrawPane, this);
        recognizeController = new RecognizeController(aDrawPane, this);

        undoManager = new UndoManager();

        drawGrid();
    }

    public void addDialog(AnchorPane dialog) {
        allDialogs.add(dialog);
    }

    public boolean removeDialog(AnchorPane dialog) {
        return allDialogs.remove(dialog);
    }

    public ArrayList<AnchorPane> getAllDialogs() {
        return allDialogs;
    }

    private void initDrawPaneActions() {
        aBorderPane.setPickOnBounds(false);

        aDrawPane.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(mode == Mode.NO_MODE)
                {
                    if(event.getButton() == MouseButton.SECONDARY){
                        mode = Mode.CONTEXT_MENU;
                        copyPasteCoords = new double[]{event.getX(), event.getY()};
                        aContextMenu.show(aDrawPane, event.getScreenX(), event.getScreenY());
                    }
                    else if (tool == ToolEnum.EDGE)
                    {
                        mode = Mode.CREATING;
                        edgeController.onMousePressed(event);
                    }
                    else if (tool == ToolEnum.SELECT)
                    {
                        mode = Mode.SELECTING;
                        //TODO This should not be needed, should be in nodeView.initActions().
                        for(AbstractNodeView nodeView : allNodeViews){
                            if (nodeView.getBoundsInParent().contains(event.getX(), event.getY()))
                            {
                                selectedNodes.add(nodeView);
                                selected = true;
                            }
                        }

                        for(AbstractEdgeView edgeView : allEdgeViews){
                            if (edgeView.getBoundsInParent().contains(event.getX(), event.getY())){
                                selected = true;
                                selectedEdges.add(edgeView);
                                if(event.getClickCount() > 1){
                                    edgeController.showEdgeEditDialog(edgeView.getRefEdge());
                                }
                            }
                        }

                        selectStartX = event.getX();
                        selectStartY = event.getY();
                        selectRectangle.setX(event.getX());
                        selectRectangle.setY(event.getY());
                        if (!aDrawPane.getChildren().contains(selectRectangle)) {
                            aDrawPane.getChildren().add(selectRectangle);
                        }

                    }
                    //--------- MOUSE EVENT FOR TESTING ---------- TODO
                    else if((tool == ToolEnum.CREATE || tool == ToolEnum.PACKAGE) && mouseCreationActivated){
                        mode = Mode.CREATING;
                        createNodeController.onMousePressed(event);
                    }
                    else if(tool == ToolEnum.MOVE_SCENE){
                        mode = Mode.MOVING;
                        graphController.movePaneStart(graph.getAllGraphElements(), event);
                        event.consume();
                    }

                } else if (mode == Mode.CONTEXT_MENU)
                {
                    if(event.getButton() == MouseButton.SECONDARY){
                        copyPasteCoords = new double[]{event.getX(), event.getY()};
                        aContextMenu.show(aDrawPane, event.getScreenX(), event.getScreenY());
                    } else {
                        aContextMenu.hide();
                    }
                }
                event.consume();
            }
        });

        aDrawPane.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (tool == ToolEnum.EDGE && mode == Mode.CREATING){
                    edgeController.onMouseDragged(event);
                }
                else if (tool == ToolEnum.SELECT && mode == Mode.SELECTING)
                {
                    selectRectangle.setX(Math.min(selectStartX, event.getX()));
                    selectRectangle.setY(Math.min(selectStartY, event.getY()));
                    selectRectangle.setWidth(Math.abs(selectStartX - event.getX()));
                    selectRectangle.setHeight(Math.abs(selectStartY - event.getY()));


                    selectRectangle.setHeight(Math.max(event.getY() - selectStartY, selectStartY - event.getY()));
                    //drawSelected();
                }
                //--------- MOUSE EVENT FOR TESTING ---------- TODO
                else if ((tool == ToolEnum.CREATE || tool == ToolEnum.PACKAGE) && mode == Mode.CREATING && mouseCreationActivated) {
                    createNodeController.onMouseDragged(event);
                } else if(mode == Mode.MOVING && tool == ToolEnum.MOVE_SCENE)
                {
                    graphController.movePane(graph.getAllGraphElements(), event);
                }
                event.consume();
            }
        });

        aDrawPane.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event)
            {
                if (tool == ToolEnum.EDGE && mode == Mode.CREATING) {
                    //User is not creating a Edge between two nodes, so we don't handle that.
                    edgeController.removeDragLine();

                    mode = Mode.NO_MODE;

                }
                else if (tool == ToolEnum.EDGE) {
                    edgeController.removeDragLine();
                }
                else if (tool == ToolEnum.SELECT && mode == Mode.SELECTING)
                {
                    for(AbstractNodeView nodeView : allNodeViews) {
                        if (selectRectangle.getBoundsInParent().contains(nodeView.getBoundsInParent()))
                        {
                            selected = true;
                            selectedNodes.add(nodeView);
                        }
                    }
                    for (AbstractEdgeView edgeView: allEdgeViews) {
                        if (selectRectangle.getBoundsInParent().intersects(edgeView.getBoundsInParent()))
                        {
                            selected = true;
                            selectedEdges.add(edgeView);
                        }
                    }
                    for (Sketch sketch : allSketches) {
                        if (selectRectangle.getBoundsInParent().intersects(sketch.getPath().getBoundsInParent())) {
                            selected = true;
                            selectedSketches.add(sketch);
                        }
                    }
                    /* //TODO Selectable nodes
                    for (javafx.scene.Node p : allSketches)
                    {
                        if (selectRectangle.getBoundsInParent().contains(p.getBoundsInParent()))
                        {
                            selected = true;
                            selectedNodes.add(p);
                        }
                    }*/

                    //If no nodes were contained, remove all selections
                    if (!selected) {
                        selectedNodes.clear();
                        selectedEdges.clear();
                        selectedSketches.clear();
                    }

                    drawSelected();
                    selectRectangle.setWidth(0);
                    selectRectangle.setHeight(0);
                    aDrawPane.getChildren().remove(selectRectangle);
                    selected = false;
                    mode = Mode.NO_MODE;
                }
                // -------------- MOUSE EVENT FOR TESTING ---------------- TODO
                if (tool == ToolEnum.CREATE && mode == Mode.CREATING && mouseCreationActivated)
                {
                    //Create ClassNode
                    ClassNode node = createNodeController.createClassNodeMouse(event);

                    //Use CreateController to create ClassNodeView.
                    ClassNodeView nodeView = (ClassNodeView) createNodeController.onMouseReleased(event, node, currentScale);

                    nodeMap.put(nodeView, node);
                    allNodeViews.add(nodeView);
                    initNodeActions(nodeView);

                    undoManager.add(new AddDeleteNodeCommand(MainController.this, graph, nodeView, nodeMap.get(nodeView), true));
                    if(!createNodeController.currentlyCreating()){
                        mode = Mode.NO_MODE;
                    }

                }
                else if (tool == ToolEnum.PACKAGE && mode == Mode.CREATING && mouseCreationActivated)
                { //TODO: combine double code
                    PackageNode node = createNodeController.createPackageNodeMouse(event);
                    PackageNodeView nodeView = (PackageNodeView) createNodeController.onMouseReleased(event, node, currentScale);
                    nodeMap.put(nodeView, node);
                    initNodeActions(nodeView);
                    undoManager.add(new AddDeleteNodeCommand(MainController.this, graph, nodeView, nodeMap.get(nodeView), true));
                    allNodeViews.add(nodeView);
                    //
                    if(!createNodeController.currentlyCreating()){
                        mode = Mode.NO_MODE;
                    }
                }
                else if (mode == Mode.MOVING && tool == ToolEnum.MOVE_SCENE)
                {
                    graphController.movePaneFinished(event);
                    mode = Mode.NO_MODE;
                }
            }
        });


        aDrawPane.setOnTouchPressed(new EventHandler<TouchEvent>() {
            @Override
            public void handle(TouchEvent event) {
                if ((tool == ToolEnum.CREATE || tool == ToolEnum.PACKAGE) && !mouseCreationActivated) {
                    mode = Mode.CREATING;
                    createNodeController.onTouchPressed(event);
                }
                else if (tool == ToolEnum.DRAW)
                {
                    mode = Mode.DRAWING;
                    sketchController.onTouchPressed(event);
                }
            }
        });

        aDrawPane.setOnTouchMoved(new EventHandler<TouchEvent>() {
            @Override
            public void handle(TouchEvent event) {
                if ((tool == ToolEnum.CREATE || tool == ToolEnum.PACKAGE) && mode == Mode.CREATING && !mouseCreationActivated) {
                    createNodeController.onTouchDragged(event);
                }
                else if (tool == ToolEnum.DRAW && mode == Mode.DRAWING)
                {
                    sketchController.onTouchMoved(event);
                }
                event.consume();

            }
        });

        aDrawPane.setOnTouchReleased(new EventHandler<TouchEvent>() {
            @Override
            public void handle(TouchEvent event) {
                if (tool == ToolEnum.CREATE && mode == Mode.CREATING && !mouseCreationActivated)
                {
                    //Create ClassNode
                    ClassNode node = createNodeController.createClassNode(event);

                    //Use CreateController to create ClassNodeView.
                    ClassNodeView nodeView = (ClassNodeView) createNodeController.onTouchReleased(node, currentScale);

                    nodeMap.put(nodeView, node);
                    allNodeViews.add(nodeView);
                    initNodeActions(nodeView);

                    undoManager.add(new AddDeleteNodeCommand(MainController.this,  graph, nodeView, nodeMap.get(nodeView), true));
                    if(!createNodeController.currentlyCreating()){
                        mode = Mode.NO_MODE;
                    }

                }
                else if (tool == ToolEnum.PACKAGE && mode == Mode.CREATING && !mouseCreationActivated) { //TODO: combine double code
                    PackageNode node = createNodeController.createPackageNode(event);
                    PackageNodeView nodeView = (PackageNodeView) createNodeController.onTouchReleased(node, currentScale);
                    nodeMap.put(nodeView, node);
                    initNodeActions(nodeView);
                    undoManager.add(new AddDeleteNodeCommand(MainController.this, graph, nodeView, nodeMap.get(nodeView), true));
                    allNodeViews.add(nodeView);
                    //
                    if(!createNodeController.currentlyCreating()){
                        mode = Mode.NO_MODE;
                    }
                }
                else if (tool == ToolEnum.DRAW && mode == Mode.DRAWING)
                {
                    Sketch sketch = sketchController.onTouchReleased(event);
                    initSketchActions(sketch);
                    allSketches.add(sketch);
                    graph.addSketch(sketch);
                    undoManager.add(new AddDeleteSketchCommand(aDrawPane, sketch, true));

                    //We only want to move out of drawing mode if there are no other current drawings
                    if(!sketchController.currentlyDrawing()){
                        mode = Mode.NO_MODE;
                    }
                }
                event.consume();
            }
        });
    }


    private void drawSelected(){
        for(AbstractNodeView nodeView : allNodeViews){
            if (selectedNodes.contains(nodeView))
            {
                nodeView.setFill(Color.MEDIUMVIOLETRED);
            }
            else
            {
                nodeView.setFill(Color.LIGHTSKYBLUE);
            }
        }
        for (AbstractEdgeView edgeView : allEdgeViews) {
            if (selectedEdges.contains(edgeView))
            {
                edgeView.setSelected(true);
            } else {
                edgeView.setSelected(false);
            }
        }
        for (Sketch sketch : allSketches) {
            if (selectedSketches.contains(sketch)){
                sketch.setSelected(true);
                sketch.getPath().toFront();
            } else {
                sketch.setSelected(false);
                sketch.getPath().toFront();
            }
        }
    }

    private void initNodeActions(AbstractNodeView nodeView){
        nodeView.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                //TODO Maybe needs some check here?
                if (event.getClickCount() == 2) {
                    nodeController.onDoubleClick(nodeView);
                }
                else if(tool == ToolEnum.MOVE_SCENE) {
                    mode = Mode.MOVING;
                    graphController.movePaneStart(graph.getAllGraphElements(), event);
                    event.consume();
                }
                else if(tool == ToolEnum.MOVE_SCENE){
                    mode = Mode.MOVING;
                    graphController.movePaneStart(graph.getAllGraphElements(), event);
                    event.consume();
                }
                else if(event.getButton() == MouseButton.SECONDARY){
                    nodeClicked = nodeView;
                    copyPasteCoords = new double[]{nodeView.getX() + event.getX(), nodeView.getY() + event.getY()};
                    aContextMenu.show(nodeView, event.getScreenX(), event.getScreenY());
                }

                else if (tool == ToolEnum.SELECT){
                    if (!(nodeView instanceof PackageNodeView)) {
                        nodeView.toFront();
                    }
                    if (mode == Mode.NO_MODE) //Resize, rectangles only
                    {
                        Point2D.Double eventPoint = new Point2D.Double(event.getX(), event.getY());
                        Point2D.Double cornerPoint = new Point2D.Double(nodeView.getWidth(), nodeView.getHeight());

                        if (eventPoint.distance(cornerPoint) < 20)
                        {
                            mode = Mode.RESIZING;
                            nodeController.resizeStart(nodeView, event);
                        }
                    }

                    if (mode == Mode.NO_MODE) //Move, any kind of node
                    {
                        mode = Mode.DRAGGING;
                        if (!selectedNodes.contains(nodeView))
                        {
                            selectedNodes.add(nodeView);
                        }
                        drawSelected();

                        nodeController.moveNodesStart(event);
                        //TODO THis is for testing!!!
                        sketchController.moveSketchStart(event);
                    }
                } else if (tool == ToolEnum.EDGE) {
                    mode = Mode.CREATING;
                    edgeController.onMousePressed(event);
                }
                event.consume();
            }
        });

        nodeView.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (tool == ToolEnum.SELECT && mode == Mode.DRAGGING)
                {
                    ArrayList<AbstractNode> selected = new ArrayList<>();
                    for(AbstractNodeView n : selectedNodes){
                        selected.add(nodeMap.get(n));
                    }
                    nodeController.moveNodes(event);
                    //TODO JUST FOR TESTING:
                    sketchController.moveSketches(event);
                    nodeWasDragged = true;


                }
                else if(mode == Mode.MOVING && tool == ToolEnum.MOVE_SCENE)
                {
                    graphController.movePane(graph.getAllGraphElements(), event);
                }
                else if (tool == ToolEnum.SELECT && mode == Mode.RESIZING)
                {
                    nodeController.resize(nodeView, event);
                }
                else if (tool == ToolEnum.EDGE && mode == Mode.CREATING) {
                    edgeController.onMouseDragged(event);
                }
                /*else if(mode == Mode.CREATING && (tool == ToolEnum.CREATE || tool == ToolEnum.PACKAGE))
                {
                    createNodeController.onTouchMoved(event);

                }*/
                event.consume();

            }
        });

        nodeView.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (tool == ToolEnum.SELECT && mode == Mode.DRAGGING)
                {
                    double[] deltaTranslateVector = nodeController.moveNodesFinished(event);
                    //TODO JUST FOR TESTING
                    sketchController.moveSketchFinished(event);
                    CompoundCommand compoundCommand = new CompoundCommand();
                    for(AbstractNodeView movedView : selectedNodes){
                        compoundCommand.add(new MoveGraphElementCommand(nodeMap.get(movedView), deltaTranslateVector[0], deltaTranslateVector[1]));
                    }
                    //TODO JUST FOR TESTING:
                    for (Sketch sketch : selectedSketches){
                        compoundCommand.add(new MoveGraphElementCommand(sketch, deltaTranslateVector[0], deltaTranslateVector[1]));
                    }
                    undoManager.add(compoundCommand);
                    if(!nodeWasDragged) {
                        selectedNodes.remove(nodeView);
                        drawSelected();
                        nodeWasDragged = false;
                    }
                } else if (mode == Mode.MOVING && tool == ToolEnum.MOVE_SCENE)
                {
                    graphController.movePaneFinished(event);
                    mode = Mode.NO_MODE;
                }
                else if (tool == ToolEnum.SELECT && mode == Mode.RESIZING)
                {
                    nodeController.resizeFinished(nodeMap.get(nodeView), event);

                }
                else if (tool == ToolEnum.EDGE && mode == Mode.CREATING) {
                    AbstractNodeView startNodeView = null;
                    AbstractNodeView endNodeView = null;
                    model.Node startNode = null;
                    model.Node endNode = null;
                    for(AbstractNodeView nodeView : allNodeViews){
                        if (nodeView.contains(edgeController.getStartPoint())){
                            startNodeView = nodeView;
                            startNode = graph.findNode(edgeController.getStartPoint());
                        } else if (nodeView.contains(edgeController.getEndPoint())){
                            endNodeView = nodeView;
                            endNode = graph.findNode(edgeController.getEndPoint());
                        }
                    }

                    AssociationEdge edge = new AssociationEdge(startNode, endNode);

                    AssociationEdgeView edgeView = (AssociationEdgeView) edgeController.
                            onMouseReleased(edge, startNodeView, endNodeView);
                    //TODO This check shouldn't be necessary?
                    if (startNodeView != null && endNodeView != null) {
                        initEdgeActions(edgeView);
                        allEdgeViews.add(edgeView);
                        graph.getAllEdges().add(edge);
                        undoManager.add(new AddDeleteEdgeCommand(MainController.this, edgeView, edge, true));
                    }
                    edgeController.removeDragLine();

                } /*else if (tool == ToolEnum.DRAW && mode == Mode.DRAWING) { //TODO Draw on nodes
                    allPaths.add(drawPath);
                    //TODO Fix initNodeActions.
                    //initNodeActions(drawPath);
                    undoManager.add(new AddDeletePathCommand(aDrawPane, drawPath, true));
                    drawPath = null;

                }*/
               /* else if (tool == ToolEnum.CREATE && mode == Mode.CREATING) //TODO create in nodes
                {
                    //Create ClassNode
                    ClassNode node = createNodeController.createClassNode(event);

                    //Use CreateController to create ClassNodeView.
                    ClassNodeView nodeView = (ClassNodeView) createNodeController.onTouchReleased(node, currentScale);
                    nodeMap.put(nodeView, node);
                    initNodeActions(nodeView);

                    undoManager.add(new AddDeleteNodeCommand(aDrawPane, nodeView, nodeMap.get(nodeView), graph, true));

                    mode = Mode.NO_MODE;

                }
                else if (tool == ToolEnum.PACKAGE && mode == Mode.CREATING)
                { //TODO: combine duplicated code?
                    PackageNode node = createNodeController.createPackageNode(event);
                    PackageNodeView nodeView = (PackageNodeView) createNodeController.onTouchReleased(node, currentScale);
                    nodeMap.put(nodeView, node);
                    initNodeActions(nodeView);
                    undoManager.add(new AddDeleteNodeCommand(aDrawPane, nodeView, nodeMap.get(nodeView), graph, true));

                    mode = Mode.NO_MODE;
                }*/
                mode = Mode.NO_MODE;
                event.consume();
            }
        });

        ////////////////////////////////////////////////////////////////
        nodeView.setOnTouchPressed(new EventHandler<TouchEvent>() {
            @Override
            public void handle(TouchEvent event) {
                if ((tool == ToolEnum.CREATE || tool == ToolEnum.PACKAGE)) {
                    mode = Mode.CREATING;
                    createNodeController.onTouchPressed(event);
                }
                else if (tool == ToolEnum.DRAW)
                {
                    mode = Mode.DRAWING;
                    sketchController.onTouchPressed(event);
                }
                event.consume();
            }
        });

        nodeView.setOnTouchMoved(new EventHandler<TouchEvent>() {
            @Override
            public void handle(TouchEvent event) {
                if ((tool == ToolEnum.CREATE || tool == ToolEnum.PACKAGE) && mode == Mode.CREATING) {
                    createNodeController.onTouchDragged(event);
                }
                else if (tool == ToolEnum.DRAW && mode == Mode.DRAWING)
                {
                    sketchController.onTouchMoved(event);
                }
                event.consume();

            }
        });

        nodeView.setOnTouchReleased(new EventHandler<TouchEvent>() {
            @Override
            public void handle(TouchEvent event) {
                if (tool == ToolEnum.CREATE && mode == Mode.CREATING)
                {
                    //Create ClassNode
                    ClassNode node = createNodeController.createClassNode(event);

                    //Use CreateController to create ClassNodeView.
                    ClassNodeView nodeView = (ClassNodeView) createNodeController.onTouchReleased(node, currentScale);

                    nodeMap.put(nodeView, node);
                    allNodeViews.add(nodeView);
                    initNodeActions(nodeView);

                    undoManager.add(new AddDeleteNodeCommand(MainController.this, graph, nodeView, nodeMap.get(nodeView), true));
                    if(!createNodeController.currentlyCreating()){
                        mode = Mode.NO_MODE;
                    }

                } else if (tool == ToolEnum.PACKAGE && mode == Mode.CREATING) { //TODO: combine double code
                    PackageNode node = createNodeController.createPackageNode(event);
                    PackageNodeView nodeView = (PackageNodeView) createNodeController.onTouchReleased(node, currentScale);
                    nodeMap.put(nodeView, node);
                    initNodeActions(nodeView);
                    undoManager.add(new AddDeleteNodeCommand(MainController.this, graph, nodeView, nodeMap.get(nodeView), true));
                    allNodeViews.add(nodeView);
                    //
                    if(!createNodeController.currentlyCreating()){
                        mode = Mode.NO_MODE;
                    }
                }
                else if (tool == ToolEnum.DRAW && mode == Mode.DRAWING)
                {
                    Sketch sketch = sketchController.onTouchReleased(event);
                    initSketchActions(sketch);
                    allSketches.add(sketch);
                    graph.addSketch(sketch);
                    undoManager.add(new AddDeleteSketchCommand(aDrawPane, sketch, true));

                    //We only want to move out of drawing mode if there are no other current drawings
                    if(!sketchController.currentlyDrawing()){
                        mode = Mode.NO_MODE;
                    }
                }
                else if(mode == Mode.MOVING && tool == ToolEnum.MOVE_SCENE) {
                    //graphController.movePaneFinished(event); TODO
                    mode = Mode.NO_MODE;
                }
                event.consume();
            }
        });

        ///////////////////////////////////////////
    }

    /**
     * Deletes all selected nodes, edges and sketches.
     */
    private void deleteSelected(){
        CompoundCommand command = new CompoundCommand();
        System.out.println("SelectedEdges size: " + selectedEdges.size());
        for(AbstractNodeView nodeView : selectedNodes){
            deleteNode(nodeView, command, false);
        }
        for (AbstractEdgeView edgeView : selectedEdges) {
            deleteEdgeView(edgeView, command, false);
        }
        for (Sketch sketch : selectedSketches) {
            deleteSketch(sketch, command);
        }

        selectedNodes.clear();
        selectedEdges.clear();
        selectedSketches.clear();

        undoManager.add(command);
    }

    /**
     * Deletes nodes and its associated edges
     * @param nodeView
     * @param pCommand Compound command from deleting all selected, if not null we create our own command.
     * @param undo If true this is an undo and no command should be created
     */
    public void deleteNode(AbstractNodeView nodeView, CompoundCommand pCommand, boolean undo){
        CompoundCommand command = null;
        if(pCommand == null && !undo){
            command = new CompoundCommand();
            selectedNodes.remove(nodeView); //Fix for concurrentModificationException
        } else if (!undo) {
            command = pCommand;
        }

        AbstractNode node = nodeMap.get(nodeView);
        deleteNodeEdges(node, command, undo);
        getGraphModel().removeNode(node);
        aDrawPane.getChildren().remove(nodeView);
        allNodeViews.remove(nodeView);

        if(!undo){
            command.add(new AddDeleteNodeCommand(this, graph, nodeView, node, false));
        }
        if(pCommand == null && !undo){
            undoManager.add(command);
        }
    }

    /**
     * Deletes edfe
     * @param edgeView
     * @param pCommand Compound command from deleting all selected, if not null we create our own command.
     * @param undo If true this is an undo and no command should be created, also used by replaceEdge in EdgeController
     */
    public void deleteEdgeView(AbstractEdgeView edgeView, CompoundCommand pCommand, boolean undo) {
        CompoundCommand command = null;
        //TODO Ugly solution for replace.
        if (pCommand == null) {
            command = new CompoundCommand();
            selectedEdges.remove(edgeView);
        } else if (!undo){
            command = pCommand;
        }

        AbstractEdge edge = edgeView.getRefEdge();
        getGraphModel().removeEdge(edge);
        aDrawPane.getChildren().remove(edgeView);
        allEdgeViews.remove(edgeView);
        if (!undo) {
            command.add(new AddDeleteEdgeCommand(this, edgeView, edge, false));
        }
        if(pCommand == null && !undo){
            undoManager.add(command);
        }
    }

    private void deleteSketch(Sketch sketch, CompoundCommand pCommand) {
        CompoundCommand command;
        //TODO Maybe not necessary for edges.
        if (pCommand == null) {
            command = new CompoundCommand();
        } else {
            command = pCommand;
        }

        getGraphModel().removeSketch(sketch);
        aDrawPane.getChildren().remove(sketch.getPath());
        allSketches.remove(sketch);
        command.add(new AddDeleteSketchCommand(aDrawPane, sketch, false));
    }

    /**
     * Deletes all edges associated with the node
     * @param node
     * @param command
     */
    public void deleteNodeEdges(AbstractNode node, CompoundCommand command, boolean undo){
        AbstractEdge edge;
        ArrayList<AbstractEdgeView> edgeViewsToBeDeleted = new ArrayList<>();
        for(AbstractEdgeView edgeView : allEdgeViews){
            edge = edgeView.getRefEdge();
            if(edge.getEndNode().equals(node) || edge.getStartNode().equals(node)){
                getGraphModel().removeEdge(edgeView.getRefEdge());
                aDrawPane.getChildren().remove(edgeView);
                selectedEdges.remove(edgeView);
                edgeViewsToBeDeleted.add(edgeView);
                if(!undo){
                    command.add(new AddDeleteEdgeCommand(this, edgeView, edgeView.getRefEdge(), false));
                }
            }
        }
        allEdgeViews.removeAll(edgeViewsToBeDeleted);
    }

    public Stage getStage() {
        return aStage;
    }

    public void setStage(Stage pStage) {
        this.aStage = pStage;
    }

    protected HashMap<AbstractNodeView, AbstractNode> getNodeMap() {
        return nodeMap;
    }

    protected Graph getGraphModel(){
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

    public UndoManager getUndoManager(){
        return undoManager;
    }

private void initEdgeActions(AbstractEdgeView edgeView){
    edgeView.setOnMousePressed(new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            if (mouseCreationActivated) {
                handleOnEdgeViewPressedEvents(edgeView);
            }
            if (event.getClickCount() == 2 || event.getButton() == MouseButton.SECONDARY) {
                //TODO If more kinds of Edges implemented: this will not work:
                edgeController.showEdgeEditDialog(edgeView.getRefEdge());
            }
        }
    });

    edgeView.setOnTouchPressed(new EventHandler<TouchEvent>() {
        @Override
        public void handle(TouchEvent event) {
            if (!mouseCreationActivated) {
                handleOnEdgeViewPressedEvents(edgeView);
                if (event.getTouchCount() == 2) {
                    //TODO If more kinds of Edges implemented: this will not work:
                    edgeController.showEdgeEditDialog((AssociationEdge) edgeView.getRefEdge());
                }
            }
        }
    });
}

private void handleOnEdgeViewPressedEvents(AbstractEdgeView edgeView) {
    if (edgeView.isSelected()) {
        selectedEdges.remove(edgeView);
        edgeView.setSelected(false);
    } else {
        selectedEdges.add(edgeView);
        edgeView.setSelected(true);
    }
}
    /**
     * initialize handlers for a sketch.
     * @param sketch
     */
    private void initSketchActions(Sketch sketch) {
        //TODO Implement this.
        sketch.getPath().setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (mouseCreationActivated){
                    handleOnSketchPressedEvents(sketch);

                    //TODO DUPLICATED CODE FROM nodeView.setOnMousePressed()
                    if (tool == ToolEnum.SELECT) {
                        if (mode == Mode.NO_MODE) //Move, any kind of node
                        {
                            mode = Mode.DRAGGING;
                            if (!selectedSketches.contains(sketch))
                            {
                                selectedSketches.add(sketch);
                            }
                            drawSelected();
                            sketchController.moveSketchStart(event);
                        }

                    }
                }
                event.consume();
            }
        });

        sketch.getPath().setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (mouseCreationActivated) {
                    if (tool == ToolEnum.SELECT && mode == Mode.DRAGGING) {
                        sketchController.moveSketches(event);
                    }
                }
                event.consume();
            }
        });

        sketch.getPath().setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                //TODO DUPLICATED CODE FROM nodeView.setOnMouseReleased()
                if (tool == ToolEnum.SELECT && mode == Mode.DRAGGING)
                {
                    double[] deltaTranslateVector = sketchController.moveSketchFinished(event);
                    sketchController.moveSketchFinished(event);
                    CompoundCommand compoundCommand = new CompoundCommand();
                    for(AbstractNodeView movedView : selectedNodes){
                        compoundCommand.add(new MoveGraphElementCommand(nodeMap.get(movedView), deltaTranslateVector[0], deltaTranslateVector[1]));
                    }
                    //TODO JUST FOR TESTING:
                    for (Sketch sketch : selectedSketches){
                        compoundCommand.add(new MoveGraphElementCommand(sketch, deltaTranslateVector[0], deltaTranslateVector[1]));
                    }
                    undoManager.add(compoundCommand);
                    drawSelected();
                }
                mode = Mode.NO_MODE;
                event.consume();
            }
        });

        sketch.getPath().setOnTouchPressed(new EventHandler<TouchEvent>() {
            @Override
            public void handle(TouchEvent event) {
                if (!mouseCreationActivated) {
                    handleOnSketchPressedEvents(sketch);
                }
            }
        });

    }

    private void handleOnSketchPressedEvents(Sketch sketch){
        if (sketch.isSelected()) {
            selectedSketches.remove(sketch);
            sketch.setSelected(false);
        } else {
            selectedSketches.add(sketch);
            sketch.setSelected(true);
        }
    }

    public List<Sketch> getSelectedSketches(){
        return selectedSketches;
    }


    @FXML private Button createBtn;
    @FXML private Button packageBtn;
    @FXML private Button edgeBtn;
    @FXML private Button selectBtn;
    @FXML private Button drawBtn;
    @FXML private Button undoBtn;
    @FXML private Button redoBtn;
    @FXML private Button moveBtn;
    @FXML private Button deleteBtn;
    @FXML private Button recognizeBtn;
    private Button buttonInUse;

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

        buttonInUse = createBtn;
        buttonInUse.getStyleClass().add("button-in-use");

        createBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                tool = ToolEnum.CREATE;
                setButtonClicked(createBtn);
            }
        });

        packageBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                tool = ToolEnum.PACKAGE;
                setButtonClicked(packageBtn);
            }
        });

        edgeBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                tool = ToolEnum.EDGE;
                setButtonClicked(edgeBtn);
            }
        });

        selectBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                tool = ToolEnum.SELECT;
                setButtonClicked(selectBtn);
            }
        });

        drawBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                tool = ToolEnum.DRAW;
                setButtonClicked(drawBtn);
            }
        });

        undoBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                undoManager.undoCommand();
            }
        });

        redoBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                undoManager.redoCommand();
            }
        });

        moveBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                setButtonClicked(moveBtn);
                tool = ToolEnum.MOVE_SCENE;
            }
        });

        deleteBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                deleteSelected();
            }
        });

        recognizeBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                recognize();
            }
        });

    }

    private void setButtonClicked(Button b){
        buttonInUse.getStyleClass().remove("button-in-use");
        buttonInUse = b;
        buttonInUse.getStyleClass().add("button-in-use");
    }

    private void recognize(){
        ArrayList<GraphElement> recognized = recognizeController.recognize(selectedSketches);
        CompoundCommand recognizeCompoundCommand = new CompoundCommand();

        for (GraphElement e : recognized) {
            if (e instanceof ClassNode) {
                ClassNodeView nodeView = new ClassNodeView((ClassNode)e);
                recognizeCompoundCommand.add(
                        new AddDeleteNodeCommand(MainController.this, graph, nodeView, (ClassNode)e, true));
                nodeMap.put(nodeView, (ClassNode) e);
                aDrawPane.getChildren().add(nodeView);
                allNodeViews.add(nodeView);
                initNodeActions(nodeView);
            }
        }
        for (GraphElement e2 : recognized) {
            if (e2 instanceof AssociationEdge) {
                AssociationEdge edge = (AssociationEdge) e2;
                //Only add the edge to the graph if it connects two nodes.
                AbstractNodeView startNodeView = null;
                AbstractNodeView endNodeView = null;

                for (AbstractNodeView nView : allNodeViews) {
                    if (nView.contains(edge.getStartNode().getX(), edge.getStartNode().getY())) {
                        startNodeView = nView;
                        break;
                    }
                }

                for (AbstractNodeView nView : allNodeViews) {
                    if (nView.contains(edge.getEndNode().getX(),
                            edge.getEndNode().getY())) {
                        endNodeView = nView;
                        break;
                    }
                }

                AssociationEdgeView edgeView = (AssociationEdgeView) edgeController.
                        onMouseReleased(edge, startNodeView, endNodeView);
                //TODO This check shouldn't be necessary?
                if (startNodeView != null && endNodeView != null) {
                    initEdgeActions(edgeView);
                    allEdgeViews.add(edgeView);
                    recognizeCompoundCommand.add(new AddDeleteEdgeCommand(MainController.this, edgeView, edge, true));
                }
            }
        }
        //Add the removal of sketches to UndoManager:
        for (Sketch sketch : recognizeController.getSketchesToBeRemoved()) {
            recognizeCompoundCommand.add(new AddDeleteSketchCommand(aDrawPane, sketch, false));
            aDrawPane.getChildren().remove(sketch);
            graph.removeSketch(sketch);
        }
        allSketches.removeAll(recognizeController.getSketchesToBeRemoved());
        selectedSketches.removeAll(recognizeController.getSketchesToBeRemoved());
        undoManager.add(recognizeCompoundCommand);
        //Bring all sketches to front:
        for (Sketch sketch : allSketches) {
            sketch.getPath().toFront();
        }
        mode = Mode.NO_MODE;
    }


    //---------------------- MENU HANDLERS -----------------------------------------------------------------------------


    public void handleMenuActionUML(){
        List<Button> umlButtons = Arrays.asList(createBtn, packageBtn, edgeBtn);

        if(umlVisible){
            for(AbstractNodeView nodeView : allNodeViews){
                aDrawPane.getChildren().remove(nodeView);
            }
            for(AbstractEdgeView edgView: allEdgeViews) {
                aDrawPane.getChildren().remove(edgView);
            }
            setButtons(true, umlButtons);
            umlMenuItem.setSelected(false);
            umlVisible = false;
        } else {
            for(AbstractNodeView nodeView : allNodeViews){
                aDrawPane.getChildren().add(nodeView);
            }
            for(AbstractEdgeView edgView: allEdgeViews) {
                aDrawPane.getChildren().add(edgView);
            }
            setButtons(false, umlButtons);
            umlMenuItem.setSelected(true);
            umlVisible = true;
            sketchesToFront();
        }
    }

    public void handleMenuActionSketches(){
        if(sketchesVisible){
            for(Sketch sketch : allSketches){
                aDrawPane.getChildren().remove(sketch.getPath());
            }

            setButtons(true, Arrays.asList(drawBtn));

            sketchesMenuItem.setSelected(false);
            sketchesVisible = false;
        } else {
            for(Sketch sketch : allSketches){
                aDrawPane.getChildren().add(sketch.getPath());
            }

            setButtons(false, Arrays.asList(drawBtn));
            sketchesMenuItem.setSelected(true);
            sketchesVisible = true;
        }
    }

    public void handleMenuActionGrid(){
        if(isGridVisible()){
            setGridVisible(false);
        } else {
            setGridVisible(true);
        }
    }

    /**
     * Disables or enables buttons provided in the list.
     * @param disable
     * @param buttons
     */
    private void setButtons(boolean disable, List<Button> buttons){
        for (Button button : buttons){
            button.setDisable(disable);
        }
        selectBtn.fire();
    }

    //------------------------- SAVE-LOAD FEATURE --------------------------------------------------------------------

    public void handleMenuActionMouse(){
        mouseCreationActivated = !mouseCreationActivated;
        mouseMenuItem.setSelected(mouseCreationActivated);
    }

    public void handleMenuActionExit() {
        Platform.exit();
    }

    public void handleMenuActionSave(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Diagram");
        File file = fileChooser.showSaveDialog(getStage());
        if(graph.getName().equals("")){
            fileChooser.setInitialFileName(graph.getName() + ".xml");
        } else {
            fileChooser.setInitialFileName("mydiagram.xml");
        }
        PersistenceManager.saveFile(graph, file.getAbsolutePath());
    }

    public void handleMenuActionLoad(){
        final FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(getStage());
        fileChooser.setTitle("Choose XML-file");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML", "*.xml"));
        Graph graph = null;
        if (file != null) {
            graph = PersistenceManager.loadFile(file.getAbsolutePath());
        }
        load(graph);
    }

    public void handleMenuActionNew(){
        reset();
    }


    //------------------------- COPY-PASTE FEATURE --------------------------------------------------------------------
    private void initContextMenu(){
        aContextMenu  = new ContextMenu();

        MenuItem cmItemDelete = new MenuItem("Delete");
        cmItemDelete.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //TODO Is this really needed? Why just not delete all selected?
                if(aContextMenu.getOwnerNode() instanceof AbstractNodeView){
                    deleteNode((AbstractNodeView) aContextMenu.getOwnerNode(), null, false);
                }
                deleteSelected();
            }
        });


        MenuItem cmItemCopy = new MenuItem("Copy");
        cmItemCopy.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                copy();
                mode = Mode.NO_MODE;
            }
        });

        MenuItem cmItemPaste = new MenuItem("Paste");
        cmItemPaste.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                paste();
                mode = Mode.NO_MODE;
            }
        });
        aContextMenu.getItems().addAll(cmItemCopy, cmItemPaste, cmItemDelete);
    }

    //TODO Copy edges and sketches as well
    private void copy(){
        currentlyCopiedNodes.clear();
        copyDeltas.clear();
        currentlyCopiedEdges.clear();

        for(AbstractNodeView nodeView : selectedNodes){
            currentlyCopiedNodes.add(nodeMap.get(nodeView));
        }
        for(AbstractEdgeView edgeView : selectedEdges){
            currentlyCopiedEdges.add(edgeView.getRefEdge());
        }
        setUpCopyCoords();
    }

    /**
     * Sets up relative coordinates for the nodes being copied
     */
    private void setUpCopyCoords(){
        double currentClosestToCorner = Double.MAX_VALUE;
        AbstractNode closest = null;
        for(GraphElement element: currentlyCopiedNodes){
            if(element instanceof AbstractNode){
                if((element.getTranslateX() + element.getTranslateY()) < currentClosestToCorner){
                    currentClosestToCorner = element.getTranslateX() + element.getTranslateY();
                    closest = (AbstractNode) element;
                }
            }

        }

        for(AbstractNode node : currentlyCopiedNodes){
            if(node != closest){
                copyDeltas.put(node, new double[]{node.getTranslateX() - closest.getTranslateX(),
                        node.getTranslateY() - closest.getTranslateY()});
            } else {
                copyDeltas.put(node, new double[]{0,0});
            }
        }
    }

    //TODO Paste two times in a row
    private void paste(){
        CompoundCommand command = new CompoundCommand();


        AbstractNode newStartNode = null;
        AbstractNode newEndNode = null;
        AbstractNodeView newStartNodeView = null;
        AbstractNodeView newEndNodeView = null;

        //If a node has several edges it will be found more than once in the currentlyCopiedEdges loop, so we put nodes
        //that are already copied in this map.
        HashMap<AbstractNode, AbstractNode> alreadyCopiedNodes = new HashMap<>();

        //Paste edges and their start and end nodes
        for(AbstractEdge oldEdge : currentlyCopiedEdges){
            for(AbstractNode node : currentlyCopiedNodes){
                if(node.equals(oldEdge.getStartNode())){
                    if(!alreadyCopiedNodes.containsKey(node)){ //If start node is not already copied
                        newStartNode = ((AbstractNode)oldEdge.getStartNode()).copy();
                        alreadyCopiedNodes.put(node, newStartNode);

                        getGraphModel().addNode(newStartNode);
                        newStartNode.setTranslateX(copyPasteCoords[0] + copyDeltas.get(node)[0]);
                        newStartNode.setTranslateY(copyPasteCoords[1] + copyDeltas.get(node)[1]);
                        newStartNodeView = createNodeView(newStartNode);
                        command.add(new AddDeleteNodeCommand(this, graph, newStartNodeView, newStartNode, true));
                    } else {
                        newStartNode = alreadyCopiedNodes.get(node);
                    }

                } else if (node.equals(oldEdge.getEndNode())){
                    if (!alreadyCopiedNodes.containsKey(node)) { //If end node is not already copied
                        newEndNode = ((AbstractNode) oldEdge.getEndNode()).copy();
                        alreadyCopiedNodes.put(node, newEndNode);

                        getGraphModel().addNode(newEndNode);
                        newEndNode.setTranslateX(copyPasteCoords[0] + copyDeltas.get(node)[0]);
                        newEndNode.setTranslateY(copyPasteCoords[1] + copyDeltas.get(node)[1]);
                        newEndNodeView = createNodeView(newEndNode);
                        command.add(new AddDeleteNodeCommand(this, graph, newEndNodeView, newEndNode, true));
                    } else {
                        newEndNode = alreadyCopiedNodes.get(node);
                    }
                }
            }
            currentlyCopiedNodes.removeAll(alreadyCopiedNodes.keySet());
            AbstractEdge copy = (AbstractEdge)oldEdge.copy(newStartNode, newEndNode);
            getGraphModel().getAllEdges().add(copy);
            AbstractEdgeView newEdgeView = createEdgeView(copy, newStartNodeView, newEndNodeView);
            command.add(new AddDeleteEdgeCommand(this,newEdgeView, copy, true));
        }

        for (GraphElement old : currentlyCopiedNodes) {
            AbstractNode copy = ((AbstractNode)old).copy();
            getGraphModel().addNode(copy);
            copy.setTranslateX(copyPasteCoords[0] + copyDeltas.get(old)[0]);
            copy.setTranslateY(copyPasteCoords[1] + copyDeltas.get(old)[1]);
            AbstractNodeView newView = createNodeView(copy);
            command.add(new AddDeleteNodeCommand(this, graph, newView, copy, true));

        }
        currentlyCopiedNodes.clear();
        currentlyCopiedEdges.clear();
        if(command.size() != 0){
            undoManager.add(command);
        }
    }

    /**
     * Creates and adds a new NodeView
     * @param node
     * @return
     */
    public AbstractNodeView createNodeView(AbstractNode node){
        AbstractNodeView newView;
        if(node instanceof ClassNode){
            newView = new ClassNodeView((ClassNode)node);
        } else /*if (node instanceof PackageNode)*/{
            newView = new PackageNodeView((PackageNode)node);
        }

        if(newView instanceof ClassNodeView){
            newView.toFront();
        } else {
            newView.toBack();
            gridToBack();
        }
        return addNodeView(newView, node);
    }

    /**
     * Adds a NodeView
     * @param nodeView
     * @param node
     * @return
     */
    public AbstractNodeView addNodeView(AbstractNodeView nodeView, AbstractNode node){
        aDrawPane.getChildren().add(nodeView);
        initNodeActions(nodeView);
        nodeMap.put(nodeView, node);
        allNodeViews.add(nodeView);

        return nodeView;
    }

    /**
     * Creates and adds a new EdgeView
     * @param edge
     * @param startNodeView
     * @param endNodeView
     * @return
     */
    public AbstractEdgeView createEdgeView(AbstractEdge edge, AbstractNodeView startNodeView, AbstractNodeView endNodeView){
        AbstractEdgeView edgeView;
        if(edge instanceof AssociationEdge){
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
     * @param edgeView
     * @return
     */
    public AbstractEdgeView addEdgeView(AbstractEdgeView edgeView){
        if(edgeView != null) {
            aDrawPane.getChildren().add(edgeView);
            initEdgeActions(edgeView);
            allEdgeViews.add(edgeView);
        }

        return edgeView;
    }

    public AbstractEdgeView addEdgeView(AbstractEdge edge){
        //TODO Really ugly
        AbstractNodeView startNodeView = null;
        AbstractNodeView endNodeView = null;
        for(AbstractNodeView nodeView : allNodeViews){
            if(edge.getStartNode() == nodeMap.get(nodeView)){
                startNodeView = nodeView;
            } else if (edge.getEndNode() == nodeMap.get(nodeView)){
                endNodeView = nodeView;
            }
        }
        if(startNodeView == null || endNodeView == null) {
            System.out.println("Failed to find start or end node");
            return null;
        } else {
            AssociationEdgeView edgeView = new AssociationEdgeView(edge, startNodeView, endNodeView);
            initEdgeActions(edgeView);
            allEdgeViews.add(edgeView);
            aDrawPane.getChildren().add(edgeView);
            return edgeView;
        }
    }

    private void reset(){
        graph = new Graph();
        aDrawPane.getChildren().clear();
        nodeMap.clear();
        allNodeViews.clear();
        zoomSlider.setValue(zoomSlider.getMax()/2);
        graphController.resetDrawPaneOffset();
        undoManager = new UndoManager();
        drawGrid();
    }



    private void load(Graph graph){
        reset();

        if (graph != null) {
            this.graph = graph;
            for(AbstractNode node : this.graph.getAllNodes()){
                createNodeView(node);
            }

            for(Edge edge : this.graph.getAllEdges()){
                addEdgeView((AbstractEdge) edge);
            }
        }
    }

    //------------------------------------ GRID -------------------------------------
    private ArrayList<Line> grid = new ArrayList<>();

    public ArrayList<Line> getGrid(){
        return grid;
    }

    private void drawGrid(){
        for (int i = 0; i < 10000; i+=20)
        {
            Line line1 = new Line(i, 0, i, 18000);
            line1.setStroke(Color.LIGHTGRAY);
            Line line2 = new Line(0, i, 15000, i);
            line2.setStroke(Color.LIGHTGRAY);
            grid.add(line1);
            grid.add(line2);
            aDrawPane.getChildren().addAll(line1, line2);
        }
    }

    public void gridToBack(){
        for(Line line : grid){
            line.toBack();
        }
    }

    private boolean isGridVisible = true;

    public void setGridVisible(boolean visible){
        for (Line line : grid){
            line.setVisible(visible);
        }
        isGridVisible = visible;
        gridMenuItem.setSelected(visible);
    }

    public boolean isGridVisible(){
        return isGridVisible;
    }

    public void sketchesToFront(){
        for(Sketch sketch : allSketches){
            sketch.getPath().toFront();
        }
    }

    //------------------------ Zoom-feature ------------------------------------------------------------------------
    private void initZoomSlider(){

        zoomSlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                                Number old_val, Number new_val) {
                if(zoomSlider.isValueChanging()){
                    graphController.zoomPane(old_val.doubleValue(), new_val.doubleValue());
                }
            }
        });
        zoomSlider.setShowTickMarks(true);
        zoomSlider.setPrefWidth(200);
    }

    public double getZoomScale(){
        return zoomSlider.getValue();
    }
}
