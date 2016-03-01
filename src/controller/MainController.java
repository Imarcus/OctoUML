package controller;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.shape.Path;
import model.*;
import util.commands.*;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
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

    private HashMap<AbstractNodeView, AbstractNode> nodeMap = new HashMap<>();



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
        NO_MODE, SELECTING, DRAGGING, RESIZING, ZOOMING, MOVING, DRAWING, CREATING
    }

    private ToolEnum tool = ToolEnum.CREATE;
    public enum ToolEnum{
        CREATE, SELECT, DRAW, PACKAGE, EDGE, MOVE_SCENE
    }

    @FXML private Pane aDrawPane;
    @FXML private ToolBar aToolBar;

    @FXML
    public void initialize() {

        selectRectangle = new Rectangle();
        selectRectangle.setFill(null);
        selectRectangle.setStroke(Color.BLACK);

        initDrawPaneActions();
        //initSceneActions();
        initToolBarActions();

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
                    if (tool == ToolEnum.EDGE) {
                        mode = Mode.CREATING;
                        edgeController.onMousePressed(event);
                    }
                    else if (tool == ToolEnum.SELECT)
                    {
                        mode = Mode.SELECTING;
                        if (!event.isShiftDown())
                        {
                            selectedNodes.clear();
                        }
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
                    drawSelected();
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
                    }

                    drawSelected();
                    selectRectangle.setWidth(0);
                    selectRectangle.setHeight(0);
                    aDrawPane.getChildren().remove(selectRectangle);
                    selected = false;
                    mode = Mode.NO_MODE;
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
                if ((tool == ToolEnum.CREATE || tool == ToolEnum.PACKAGE)) {
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

        aDrawPane.setOnTouchReleased(new EventHandler<TouchEvent>() {
            @Override
            public void handle(TouchEvent event) {
                if (tool == ToolEnum.CREATE && mode == Mode.CREATING)
                {
                    //Create ClassNode
                    ClassNode node = createNodeController.createClassNode(event);

                    //Use CreateController to create ClassNodeView.
                    ClassNodeView nodeView = (ClassNodeView) createNodeController.onTouchReleased(event, node, currentScale);

                    nodeMap.put(nodeView, node);
                    allNodeViews.add(nodeView);
                    initNodeActions(nodeView);

                    undoManager.add(new AddDeleteNodeCommand(aDrawPane, nodeView, nodeMap.get(nodeView), graph, true));
                    if(!createNodeController.currentlyCreating()){
                        mode = Mode.NO_MODE;
                    }

                } else if (tool == ToolEnum.PACKAGE && mode == Mode.CREATING) { //TODO: combine double code
                    PackageNode node = createNodeController.createPackageNode(event);
                    PackageNodeView nodeView = (PackageNodeView) createNodeController.onTouchReleased(event, node, currentScale);
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
                    if(sketchController.currentlyDrawing()){
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

    @FXML
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
    }

    //TODO THis should take a GraphElement(View?) instead!
    private void initNodeActions(AbstractNodeView nodeView){
        nodeView.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                //TODO Maybe needs some check here?
                if (event.getClickCount() == 2) {
                    nodeController.addNodeTitle(nodeMap.get(nodeView));
                }
                if (tool == ToolEnum.SELECT){

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
                        if (!event.isShiftDown())
                        {
                            selectedNodes.clear();
                        }
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
                    event.consume();
                }
                else if(event.getTouchCount() > 2 && mode == Mode.NO_MODE && tool == ToolEnum.MOVE_SCENE){ //TODO MORE THAN 2?
                    mode = Mode.MOVING;
                    graphController.movePaneStart(graph.getAllGraphElements(), event);
                    event.consume();
                }
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
                    ClassNodeView nodeView = (ClassNodeView) createNodeController.onTouchReleased(event, node, currentScale);

                    nodeMap.put(nodeView, node);
                    allNodeViews.add(nodeView);
                    initNodeActions(nodeView);

                    undoManager.add(new AddDeleteNodeCommand(aDrawPane, nodeView, nodeMap.get(nodeView), graph, true));
                    if(!createNodeController.currentlyCreating()){
                        mode = Mode.NO_MODE;
                    }

                } else if (tool == ToolEnum.PACKAGE && mode == Mode.CREATING) { //TODO: combine double code
                    PackageNode node = createNodeController.createPackageNode(event);
                    PackageNodeView nodeView = (PackageNodeView) createNodeController.onTouchReleased(event, node, currentScale);
                    nodeMap.put(nodeView, node);
                    initNodeActions(nodeView);
                    undoManager.add(new AddDeleteNodeCommand(aDrawPane, nodeView, nodeMap.get(nodeView), graph, true));
                    allNodeViews.add(nodeView);
                    //
                    if(createNodeController.currentlyCreating()){
                        mode = Mode.NO_MODE;
                    }
                }
                else if (tool == ToolEnum.DRAW && mode == Mode.DRAWING)
                {
                    Sketch sketch = sketchController.onTouchReleased(event);
                    allSketches.add(sketch);
                    undoManager.add(new AddDeleteSketchCommand(aDrawPane, sketch, true));

                    //We only want to move out of drawing mode if there are no other current drawings
                    if(sketchController.currentlyDrawing()){
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
            else if (((Button)button).getText().equals("Recognize"))
            {
                ((Button)button).setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
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
}
