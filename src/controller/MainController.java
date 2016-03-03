package controller;

import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.shape.Path;
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
import view.*;

import java.awt.geom.Point2D;
import java.util.*;

/**
 * Created by marcusisaksson on 2016-02-11.
 */
public class MainController {

    private boolean mouseCreationActivated = false;

    private CreateNodeController createNodeController;
    private NodeController nodeController;
    private EdgeController edgeController;
    private GraphController graphController;
    private SketchController sketchController;
    private RecognizeController recognizeController;

    private Graph graph;

    private ArrayList<AbstractNodeView> selectedNodes = new ArrayList<>();

    private ArrayList<AbstractNodeView> allNodeViews = new ArrayList<>();
    private ArrayList<AbstractEdgeView> allEdgeViews = new ArrayList<>();

    private ArrayList<AbstractEdgeView> selectedEdges = new ArrayList<>();

    private HashMap<AbstractNodeView, AbstractNode> nodeMap = new HashMap<>();

    //Copy nodes logic
    private ArrayList<AbstractNode> currentlyCopiedNodes = new ArrayList<>();
    private HashMap<AbstractNode, double[]> copyDeltas = new HashMap<>();
    private double[] copyPasteCoords;



    //For drag-selecting nodes
    private double selectStartX, selectStartY;
    private Rectangle selectRectangle;

    //For drawing
    private ArrayList<Sketch> allSketches = new ArrayList<>();
    //private Path drawPath;
    private Map<Integer, Path> currentPaths = new HashMap<>();

    //For max/min zoom check
    private boolean zoomMaxedOut;
    private double totalZoomFactor;
    private final double MAX_ZOOM = 7;
    private final double MIN_ZOOM = 0.3;

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
    @FXML private ToolBar aToolBar;
    @FXML private CheckMenuItem umlMenuItem;
    @FXML private CheckMenuItem sketchesMenuItem;
    @FXML private CheckMenuItem mouseMenuItem;

    private ContextMenu aContextMenu;

    private AbstractNodeView nodeClicked;


    @FXML
    public void initialize() {

        selectRectangle = new Rectangle();
        selectRectangle.setFill(null);
        selectRectangle.setStroke(Color.BLACK);

        initDrawPaneActions();
        //initSceneActions();
        initToolBarActions();
        initContextMenu();

        graph = new Graph();

        createNodeController = new CreateNodeController(aDrawPane, this);
        nodeController = new NodeController(aDrawPane, this);
        graphController = new GraphController(aDrawPane, this);
        edgeController = new EdgeController(aDrawPane);
        sketchController = new SketchController(aDrawPane, this);
        recognizeController = new RecognizeController(aDrawPane, this);

        undoManager = new UndoManager();

    }

    
    //TODO FIX SO THAT YOU CAN USE KEYACTIONS
    /*private void initSceneActions(){
        aScene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.isControlDown() && keyEvent.getCode() == KeyCode.Z) {

                    undoManager.undoCommand();
                    keyEvent.consume();
                }
                if (keyEvent.isControlDown() && keyEvent.getCode() == KeyCode.Y) {
                    undoManager.redoCommand();
                    keyEvent.consume();
                }
            }
        });
    }*/

    private void initDrawPaneActions() {
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

                        selectStartX = event.getX();
                        selectStartY = event.getY();
                        aDrawPane.getChildren().add(selectRectangle);

                    }
                    //--------- MOUSE EVENT FOR TESTING ---------- TODO
                    else if((tool == ToolEnum.CREATE || tool == ToolEnum.PACKAGE) && mouseCreationActivated){
                        mode = Mode.CREATING;
                        createNodeController.onMousePressed(event);
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
                    selectRectangle.setX(selectStartX);
                    selectRectangle.setY(selectStartY);
                    selectRectangle.setWidth(event.getX() - selectStartX);
                    selectRectangle.setHeight(event.getY() - selectStartY);
                    //drawSelected();
                }
                //--------- MOUSE EVENT FOR TESTING ---------- TODO
                else if ((tool == ToolEnum.CREATE || tool == ToolEnum.PACKAGE) && mode == Mode.CREATING && mouseCreationActivated) {
                    createNodeController.onMouseDragged(event);
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

                    undoManager.add(new AddDeleteNodeCommand(aDrawPane, nodeView, nodeMap.get(nodeView), graph, true));
                    if(!createNodeController.currentlyCreating()){
                        mode = Mode.NO_MODE;
                    }

                } else if (tool == ToolEnum.PACKAGE && mode == Mode.CREATING && mouseCreationActivated) { //TODO: combine double code
                    PackageNode node = createNodeController.createPackageNodeMouse(event);
                    PackageNodeView nodeView = (PackageNodeView) createNodeController.onMouseReleased(event, node, currentScale);
                    nodeMap.put(nodeView, node);
                    initNodeActions(nodeView);
                    undoManager.add(new AddDeleteNodeCommand(aDrawPane, nodeView, nodeMap.get(nodeView), graph, true));
                    allNodeViews.add(nodeView);
                    //
                    if(!createNodeController.currentlyCreating()){
                        mode = Mode.NO_MODE;
                    }
                }
            }
        });

        /*aDrawPane.setOnZoomStarted(new EventHandler<ZoomEvent>() {
            @Override
            public void handle(ZoomEvent event) {
                if(tool != ToolEnum.MOVE_SCENE){
                    mode = Mode.ZOOMING;
                    event.consume();
                }

            }
        });

        aDrawPane.setOnZoom(new EventHandler<ZoomEvent>() {
            @Override
            public void handle(ZoomEvent event) {
                if(mode == Mode.ZOOMING && tool != ToolEnum.MOVE_SCENE){
                    if(!zoomMaxedOut){
                        if((event.getTotalZoomFactor()*currentScale < MIN_ZOOM) ||
                                (event.getTotalZoomFactor()*currentScale > MAX_ZOOM)){ //TODO magic numbers
                            zoomMaxedOut = true;
                            totalZoomFactor = event.getTotalZoomFactor();
                        } else {
                            graphController.zoomPane(event);
                        }
                        event.consume();
                    }
               }
            }
        });

        aDrawPane.setOnZoomFinished(new EventHandler<ZoomEvent>() {
            @Override
            public void handle(ZoomEvent event) {
                if(mode == Mode.ZOOMING && tool != ToolEnum.MOVE_SCENE)
                {
                    if(zoomMaxedOut)
                    {
                        currentScale = totalZoomFactor * currentScale;
                        currentScale = Math.min(MAX_ZOOM, currentScale);
                        currentScale = Math.max(MIN_ZOOM, currentScale);
                    }
                    else
                    {
                        currentScale = event.getTotalZoomFactor() * currentScale;
                    }
                    mode = Mode.NO_MODE;
                    zoomMaxedOut = false;
                    totalZoomFactor = 0;
                    event.consume();
                }
            }
        });*/

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
                    event.consume();
                }
                else if(event.getTouchCount() > 2 && mode == Mode.NO_MODE && tool == ToolEnum.MOVE_SCENE){ //TODO MORE THAN 2?
                    mode = Mode.MOVING;
                    graphController.movePaneStart(graph.getAllGraphElements(), event);
                    event.consume();
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
                else if(event.getTouchCount() > 2 && mode == Mode.MOVING && tool == ToolEnum.MOVE_SCENE)
                {
                    graphController.movePane(graph.getAllGraphElements(), event);
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

                    undoManager.add(new AddDeleteNodeCommand(aDrawPane, nodeView, nodeMap.get(nodeView), graph, true));
                    if(!createNodeController.currentlyCreating()){
                        mode = Mode.NO_MODE;
                    }

                } else if (tool == ToolEnum.PACKAGE && mode == Mode.CREATING && !mouseCreationActivated) { //TODO: combine double code
                    PackageNode node = createNodeController.createPackageNode(event);
                    PackageNodeView nodeView = (PackageNodeView) createNodeController.onTouchReleased(node, currentScale);
                    nodeMap.put(nodeView, node);
                    initNodeActions(nodeView);
                    undoManager.add(new AddDeleteNodeCommand(aDrawPane, nodeView, nodeMap.get(nodeView), graph, true));
                    allNodeViews.add(nodeView);
                    //
                    if(!createNodeController.currentlyCreating()){
                        mode = Mode.NO_MODE;
                    }
                }
                else if (tool == ToolEnum.DRAW && mode == Mode.DRAWING)
                {
                    Sketch sketch = sketchController.onTouchReleased(event);
                    allSketches.add(sketch);
                    undoManager.add(new AddDeleteSketchCommand(aDrawPane, sketch, true));

                    //We only want to move out of drawing mode if there are no other current drawings
                    if(!sketchController.currentlyDrawing()){
                        mode = Mode.NO_MODE;
                    }
                }
                else if(event.getTouchCount() > 2 && mode == Mode.MOVING && tool == ToolEnum.MOVE_SCENE) {
                    graphController.movePaneFinished();
                    mode = Mode.NO_MODE;
                }
                event.consume();
            }
        });

        /*aDrawPane.setOnScroll(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) {
                for (javafx.scene.Node n : aDrawPane.getChildren()) {
                    n.setTranslateX(n.getTranslateX() + event.getDeltaX());
                    n.setTranslateY(n.getTranslateY() + event.getDeltaY());
                    event.consume();
                }
            }
        });*/
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
    }

    //TODO THis should take a GraphElement(View?) instead!
    private void initNodeActions(AbstractNodeView nodeView){
        nodeView.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                //TODO Maybe needs some check here?
                if(event.getButton() == MouseButton.SECONDARY){
                    nodeClicked = nodeView;
                    copyPasteCoords = new double[]{nodeView.getX() + event.getX(), nodeView.getY() + event.getY()};
                    aContextMenu.show(nodeView, event.getScreenX(), event.getScreenY());
                }
                if (event.getClickCount() == 2) {
                    nodeController.addNodeTitle(nodeMap.get(nodeView));
                }
                if (tool == ToolEnum.SELECT){
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
                    nodeWasDragged = true;


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
                    CompoundCommand compoundCommand = new CompoundCommand();
                    for(AbstractNodeView movedView : selectedNodes){
                        compoundCommand.add(new MoveNodeCommand(nodeMap.get(movedView), deltaTranslateVector[0], deltaTranslateVector[1]));
                    }
                    undoManager.add(compoundCommand);
                    if(!nodeWasDragged) {
                        selectedNodes.remove(nodeView);
                        drawSelected();
                        nodeWasDragged = false;
                    }
                }
                else if (tool == ToolEnum.SELECT && mode == Mode.RESIZING)
                {
                    nodeController.resizeFinished(nodeMap.get(nodeView), event);

                }
                else if (tool == ToolEnum.EDGE && mode == Mode.CREATING) {
                    model.Node startNode = graph.findNode(edgeController.getStartPoint());
                    model.Node endNode = graph.findNode(edgeController.getEndPoint());

                    AssociationEdge edge = new AssociationEdge(startNode, endNode);
                    //Only add the edge to the graph if it connects two nodes.
                    AbstractNodeView startNodeView = null;
                    AbstractNodeView endNodeView = null;

                    if (graph.connect(startNode, endNode, edge)) {
                        for (AbstractNodeView nView : allNodeViews) {
                            if (nView.contains(startNode.getX(), startNode.getY())) {
                                startNodeView = nView;
                                break;
                            }
                        }

                        for (AbstractNodeView nView : allNodeViews) {
                            if (nView.contains(endNode.getX(), endNode.getY())) {
                                endNodeView = nView;
                                break;
                            }
                        }
                    }
                    AssociationEdgeView edgeView = (AssociationEdgeView) edgeController.
                            onMouseReleased(edge, startNodeView, endNodeView);
                    //TODO This check shouldn't be necessary?
                    if (startNodeView != null && endNodeView != null) {
                        initEdgeActions(edgeView);
                        allEdgeViews.add(edgeView);
                        undoManager.add(new AddDeleteEdgeCommand(aDrawPane, edgeView, edge, graph, true));
                        System.out.println("STARTNODE x = " + startNodeView.getX() +
                                " y = " + startNodeView.getY());
                        System.out.println("ENDNODE x = " + endNodeView.getX() +
                                " y = " + endNodeView.getY());
                        System.out.println("CREATING EDGE: startX = " +
                                edgeView.getStartX() +
                                " startY = " + edgeView.getStartY() +
                                " endX = " + edgeView.getEndX() +
                                " endY = " + edgeView.getEndY());
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
                else if(event.getTouchCount() > 2 && mode == Mode.NO_MODE && tool == ToolEnum.MOVE_SCENE){ //TODO MORE THAN 2?
                    mode = Mode.MOVING;
                    graphController.movePaneStart(graph.getAllGraphElements(), event);
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
                else if(event.getTouchCount() > 2 && mode == Mode.MOVING && tool == ToolEnum.MOVE_SCENE)
                {
                    graphController.movePane(graph.getAllGraphElements(), event);
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

                    undoManager.add(new AddDeleteNodeCommand(aDrawPane, nodeView, nodeMap.get(nodeView), graph, true));
                    if(!createNodeController.currentlyCreating()){
                        mode = Mode.NO_MODE;
                    }

                } else if (tool == ToolEnum.PACKAGE && mode == Mode.CREATING) { //TODO: combine double code
                    PackageNode node = createNodeController.createPackageNode(event);
                    PackageNodeView nodeView = (PackageNodeView) createNodeController.onTouchReleased(node, currentScale);
                    nodeMap.put(nodeView, node);
                    initNodeActions(nodeView);
                    undoManager.add(new AddDeleteNodeCommand(aDrawPane, nodeView, nodeMap.get(nodeView), graph, true));
                    allNodeViews.add(nodeView);
                    //
                    if(!createNodeController.currentlyCreating()){
                        mode = Mode.NO_MODE;
                    }
                }
                else if (tool == ToolEnum.DRAW && mode == Mode.DRAWING)
                {
                    Sketch sketch = sketchController.onTouchReleased(event);
                    allSketches.add(sketch);
                    undoManager.add(new AddDeleteSketchCommand(aDrawPane, sketch, true));

                    //We only want to move out of drawing mode if there are no other current drawings
                    if(!sketchController.currentlyDrawing()){
                        mode = Mode.NO_MODE;
                    }
                }
                else if(event.getTouchCount() > 2 && mode == Mode.MOVING && tool == ToolEnum.MOVE_SCENE) {
                    graphController.movePaneFinished();
                    mode = Mode.NO_MODE;
                }
                event.consume();
            }
        });

        ///////////////////////////////////////////
    }

    /**
     * Deletes all selected nodes and their associated edges (using deleteNodeEdges)
     */
    private void deleteSelected(){
        CompoundCommand command = new CompoundCommand();
        System.out.println("SelectedEdges size: " + selectedEdges.size());
        for(AbstractNodeView nodeView : selectedNodes){
            deleteNode(nodeView, command);
        }
        for (AbstractEdgeView edgeView : selectedEdges) {
            deleteEdge(edgeView, command);
        }
        selectedNodes.clear();
        selectedEdges.clear();
        undoManager.add(command);
    }

    private void deleteNode(AbstractNodeView nodeView, CompoundCommand pCommand){
        CompoundCommand command;
        if(pCommand == null){
            command = new CompoundCommand();
        } else {
            command = pCommand;
        }

        AbstractNode node = nodeMap.get(nodeView);
        deleteNodeEdges(node, command);
        getGraphModel().removeNode(node);
        aDrawPane.getChildren().remove(nodeView);
        allNodeViews.remove(nodeView);
        command.add(new AddDeleteNodeCommand(aDrawPane, nodeView, node, getGraphModel(), false));

        if(pCommand == null){
            undoManager.add(command);
        }
    }

    private void deleteEdge(AbstractEdgeView edgeView, CompoundCommand pCommand) {
        CompoundCommand command;
        //TODO Maybe not necessary for edges.
        if (pCommand == null) {
            command = new CompoundCommand();
        } else {
            command = pCommand;
        }

        AbstractEdge edge = edgeView.getRefEdge();
        getGraphModel().removeEdge(edge);
        aDrawPane.getChildren().remove(edgeView);
        allEdgeViews.remove(edgeView);
        command.add(new AddDeleteEdgeCommand(aDrawPane, edgeView, edge, getGraphModel(), false));
    }

    /**
     * Deletes all edges associated with the node
     * @param node
     * @param command
     */
    private void deleteNodeEdges(AbstractNode node, CompoundCommand command){
        AbstractEdge edge;
        ArrayList<AbstractEdgeView> edgeViewsToBeDeleted = new ArrayList<>();
        for(AbstractEdgeView edgeView : allEdgeViews){
            edge = edgeView.getRefEdge();
            if(edge.getEndNode().equals(node) || edge.getStartNode().equals(node)){
                getGraphModel().removeEdge(edgeView.getRefEdge());
                aDrawPane.getChildren().remove(edgeView);
                edgeViewsToBeDeleted.add(edgeView);
                command.add(new AddDeleteEdgeCommand(aDrawPane, edgeView, edgeView.getRefEdge(), getGraphModel(), false));
            }
        }
        allEdgeViews.removeAll(edgeViewsToBeDeleted);
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

private void initEdgeActions(AbstractEdgeView edgeView){
    edgeView.setOnMouseClicked(new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            if (mouseCreationActivated) {
                if (edgeView.isSelected()) {
                    selectedEdges.remove(edgeView);
                    edgeView.setSelected(false);
                } else {
                    selectedEdges.add(edgeView);
                    edgeView.setSelected(true);
                }
            }
        }
    });

    //TODO
    edgeView.setOnTouchPressed(new EventHandler<TouchEvent>() {
        @Override
        public void handle(TouchEvent event) {
            if (edgeView.isSelected()) {
                edgeView.setFill(Color.BLACK);
                selectedEdges.remove(edgeView);
                edgeView.setSelected(false);
            } else {
                edgeView.setFill(Color.MEDIUMVIOLETRED);
                selectedEdges.add(edgeView);
                edgeView.setSelected(true);
            }
        }
    });


    edgeView.setOnTouchReleased(new EventHandler<TouchEvent>() {
        @Override
        public void handle(TouchEvent event) {

        }
    });
}

    /**
     * initialize handlers for a sketch.
     * @param sketch
     */
    private void initSketchActions(Sketch sketch) {
        //TODO Implement this.
        sketch.getPath().setOnTouchPressed(new EventHandler<TouchEvent>() {
            @Override
            public void handle(TouchEvent event) {

            }
        });

        sketch.getPath().setOnTouchMoved(new EventHandler<TouchEvent>() {
            @Override
            public void handle(TouchEvent event) {

            }
        });

        sketch.getPath().setOnTouchReleased(new EventHandler<TouchEvent>() {
            @Override
            public void handle(TouchEvent event) {

            }
        });
    }

    @FXML
    private void initToolBarActions(){
        ObservableList<javafx.scene.Node> buttonList = aToolBar.getItems();

        //TODO Hardcoded string-vales!
        for (javafx.scene.Node button : buttonList)
        {
            if (((Button)button).getText().equals("Create"))
            {
                ((Button)button).setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        tool = ToolEnum.CREATE;
                    }
                });
            }
            else if (((Button)button).getText().equals("Edge"))
            {
                ((Button)button).setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        tool = ToolEnum.EDGE;
                    }
                });
            }
            else if (((Button)button).getText().equals("Select"))
            {
                ((Button)button).setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        tool = ToolEnum.SELECT;
                    }
                });
            }
            else if (((Button)button).getText().equals("Draw"))
            {
                ((Button)button).setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        tool = ToolEnum.DRAW;
                    }
                });
            }
            else if (((Button)button).getText().equals("Package"))
            {
                ((Button)button).setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        tool = ToolEnum.PACKAGE;
                    }
                });
            }
            else if (((Button)button).getText().equals("Undo"))
            {
                ((Button)button).setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        undoManager.undoCommand();
                    }
                });
            }
            else if (((Button)button).getText().equals("Redo"))
            {
                ((Button)button).setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        undoManager.redoCommand();
                    }
                });
            }
            else if (((Button)button).getText().equals("Move"))
            {
                ((Button)button).setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        tool = ToolEnum.MOVE_SCENE;
                    }
                });
            }
            else if (((Button)button).getText().equals("Delete"))
            {
                ((Button)button).setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        deleteSelected();
                    }
                });
            }
            else if (((Button)button).getText().equals("Recognize"))
            {
                ((Button)button).setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) { //TODO MOVE THIS SOMEWHERE ELSE
                        ArrayList<GraphElement> list = recognizeController.recognize(allSketches);
                        CompoundCommand recognizeCompoundCommand = new CompoundCommand();

                        //TODO Remove
                        System.out.println("Entered handle ActionEvent");
                        for (GraphElement e : list) {
                            if (e instanceof ClassNode) {
                                ClassNodeView nodeView = new ClassNodeView((ClassNode)e);
                                recognizeCompoundCommand.add(
                                        new AddDeleteNodeCommand(aDrawPane, nodeView, (ClassNode)e,graph, true));
                                nodeMap.put(nodeView, (ClassNode) e);
                                aDrawPane.getChildren().add(nodeView);
                                allNodeViews.add(nodeView);
                                initNodeActions(nodeView);
                            }
                        }
                        for (GraphElement e2 : list) {
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
                                    recognizeCompoundCommand.add(new AddDeleteEdgeCommand(aDrawPane, edgeView, edge, graph, true));
                                }
                            }
                        }
                        //Add the removal of sketches to UndoManager:
                        for (Sketch sketch : recognizeController.getSketchesToBeRemoved()) {
                            recognizeCompoundCommand.add(new AddDeleteSketchCommand(aDrawPane, sketch, false));
                            aDrawPane.getChildren().remove(sketch);
                        }
                        allSketches.removeAll(recognizeController.getSketchesToBeRemoved());
                        undoManager.add(recognizeCompoundCommand);
                        //Bring all sketches to front:
                        for (Sketch sketch : allSketches) {
                            sketch.getPath().toFront();
                        }
                        mode = Mode.NO_MODE;
                    }
                });
            }

        }
    }

    //---------------------- MENU HANDLERS -----------------------------------------------------------------------------

    List<String> umlButtonStrings = Arrays.asList("Create", "Package", "Edge");

    public void handleMenuActionUML(){
        if(umlVisible){
            for(AbstractNodeView nodeView : allNodeViews){
                aDrawPane.getChildren().remove(nodeView);
            }
            setButtons(true, umlButtonStrings);
            umlMenuItem.setSelected(false);
            umlVisible = false;
        } else {
            for(AbstractNodeView nodeView : allNodeViews){
                aDrawPane.getChildren().add(nodeView);
            }
            setButtons(false, umlButtonStrings);
            umlMenuItem.setSelected(true);
            umlVisible = true;
        }
    }

    public void handleMenuActionSketches(){
        if(sketchesVisible){
            for(Sketch sketch : allSketches){
                aDrawPane.getChildren().remove(sketch.getPath());
            }

            setButtons(true, Arrays.asList("Draw"));

            sketchesMenuItem.setSelected(false);
            sketchesVisible = false;
        } else {
            for(Sketch sketch : allSketches){
                aDrawPane.getChildren().add(sketch.getPath());
            }

            setButtons(false, Arrays.asList("Draw"));
            sketchesMenuItem.setSelected(true);
            sketchesVisible = true;
        }
    }

    /**
     * Disables or enables buttons provided in the list.
     * @param disable
     * @param buttonStrings
     */
    private void setButtons(boolean disable, List<String> buttonStrings){
        ObservableList<javafx.scene.Node> buttonList = aToolBar.getItems();
        for (javafx.scene.Node button : buttonList){
            if(buttonStrings.contains(((Button) button).getText())){
                button.setDisable(disable);
            }
            if(((Button) button).getText().equals("Select")){
                ((Button) button).fire();
            }
        }
    }

    public void handleMenuActionMouse(){
        mouseCreationActivated = !mouseCreationActivated;
        mouseMenuItem.setSelected(mouseCreationActivated);
    }


    //------------------------- COPY-PASTE FEATURE --------------------------------------------------------------------
    private void initContextMenu(){
        aContextMenu  = new ContextMenu();

        MenuItem cmItemDelete = new MenuItem("Delete");
        cmItemDelete.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(aContextMenu.getOwnerNode() instanceof AbstractNodeView){
                    deleteNode((AbstractNodeView) aContextMenu.getOwnerNode(), null);
                }
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


        aContextMenu.getItems().addAll(cmItemDelete, cmItemCopy, cmItemPaste);
    }

    //TODO Copy edges and sketches as well
    private void copy(){
        currentlyCopiedNodes.clear();
        copyDeltas.clear();

        for(AbstractNodeView nodeView : selectedNodes){
            AbstractNode node = nodeMap.get(nodeView);
            currentlyCopiedNodes.add(node);
        }
        setUpCopyCoords();
    }

    private void setUpCopyCoords(){
        double currentClosestToCorner = Double.MAX_VALUE;
        AbstractNode closest = null;
        for(AbstractNode node: currentlyCopiedNodes){
            if((node.getTranslateX() + node.getTranslateY()) < currentClosestToCorner){
                currentClosestToCorner = node.getTranslateX() + node.getTranslateY();
                closest = node;
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
        for (AbstractNode old : currentlyCopiedNodes) {
            AbstractNode copy = old.copy();
            getGraphModel().addNode(copy);
            copy.setTranslateX(copyPasteCoords[0] + copyDeltas.get(old)[0]);
            copy.setTranslateY(copyPasteCoords[1] + copyDeltas.get(old)[1]);
            AbstractNodeView newView = addNodeView(copy);
            command.add(new AddDeleteNodeCommand(aDrawPane, newView, copy, getGraphModel(), true));
        }
        undoManager.add(command);
    }

    public AbstractNodeView addNodeView(AbstractNode node){
        AbstractNodeView newView;
        if(node instanceof ClassNode){
            newView = new ClassNodeView((ClassNode)node);
        } else /*if (node instanceof PackageNode)*/{
            newView = new PackageNodeView((PackageNode)node);
        }
        aDrawPane.getChildren().add(newView);
        initNodeActions(newView);
        nodeMap.put(newView, node);
        allNodeViews.add(newView);

        return newView;
    }

}
