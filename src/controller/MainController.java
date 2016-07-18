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
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Path;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.*;
import util.Constants;
import util.commands.*;
import util.insertIMG.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import util.persistence.PersistenceManager;
import view.*;
import java.util.*;
import java.awt.geom.Point2D;
import java.io.File;




/**
 * Created by marcusisaksson on 2016-02-11.
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

    //Node lists and maps
    ArrayList<AbstractNodeView> selectedNodes = new ArrayList<>();
    ArrayList<AbstractEdgeView> selectedEdges = new ArrayList<>();
    ArrayList<Sketch> selectedSketches = new ArrayList<>();
    ArrayList<AbstractNodeView> allNodeViews = new ArrayList<>();
    ArrayList<AbstractEdgeView> allEdgeViews = new ArrayList<>();
    ArrayList<AnchorPane> allDialogs = new ArrayList<>();
    HashMap<AbstractNodeView, AbstractNode> nodeMap = new HashMap<>();


    //For drawing
    ArrayList<Sketch> allSketches = new ArrayList<>();

    //private Path drawPath;
    Map<Integer, Path> currentPaths = new HashMap<>();

    boolean selected = false; //A node is currently selected
    double currentScale = 1;

    private UndoManager undoManager;

    //Mode
    private Mode mode = Mode.NO_MODE;

    public enum Mode {
        NO_MODE, SELECTING, DRAGGING, RESIZING, ZOOMING, MOVING, DRAWING, CREATING, CONTEXT_MENU
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode pMode) {
        mode = pMode;
    }

    //Tool
    private ToolEnum tool = ToolEnum.CREATE;

    public enum ToolEnum {
        CREATE, SELECT, DRAW, PACKAGE, EDGE, MOVE_SCENE
    }

    public ToolEnum getTool() {
        return tool;
    }

    public void setTool(ToolEnum pTool) {
        tool = pTool;
    }

    //Views
    private boolean umlVisible = true;
    private boolean sketchesVisible = true;

    //Selection logic
    private boolean nodeWasDragged = true;

    @FXML
    private Pane aDrawPane;
    @FXML
    private Slider zoomSlider;
    @FXML
    private BorderPane aBorderPane;
    @FXML
    private ScrollPane aScrollPane;

    ContextMenu aContextMenu;
    double orgSceneX, orgSceneY;
    double orgTranslateX, orgTranslateY;
    private AbstractNodeView nodeClicked;
    private MainController instance = this;


    @FXML
    public void initialize() {
        initDrawPaneActions();
        initToolBarActions();
        initContextMenu();
        initZoomSlider();

        // center the scroll contents.
        aScrollPane.setHvalue(aScrollPane.getHmin() + (aScrollPane.getHmax() - aScrollPane.getHmin()) / 2);
        aScrollPane.setVvalue(aScrollPane.getVmin() + (aScrollPane.getVmax() - aScrollPane.getVmin()) / 2);
        aScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        aScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        graph = new Graph();

        createNodeController = new CreateNodeController(aDrawPane, this);
        nodeController = new NodeController(aDrawPane, this);
        graphController = new GraphController(aDrawPane, this);
        edgeController = new EdgeController(aDrawPane, this);
        sketchController = new SketchController(aDrawPane, this);
        recognizeController = new RecognizeController(aDrawPane, this);
        selectController = new SelectController(aDrawPane, this);
        copyPasteController = new CopyPasteController(aDrawPane, this);

        undoManager = new UndoManager();

        drawGrid();
        //mouseMenuItem.setSelected(mouseCreationActivated);
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

    private void initDrawPaneActions() {
        aBorderPane.setPickOnBounds(false);

        aDrawPane.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (mode == Mode.NO_MODE) {
                    if (event.getButton() == MouseButton.SECONDARY) {
                        mode = Mode.CONTEXT_MENU;
                        copyPasteController.copyPasteCoords = new double[]{event.getX(), event.getY()};
                        aContextMenu.show(aDrawPane, event.getScreenX(), event.getScreenY());
                    } else if (tool == ToolEnum.SELECT || tool == ToolEnum.EDGE) {
                        selectController.onMousePressed(event);
                    }

                    //--------- MOUSE EVENT FOR TESTING ---------- TODO
                    else if ((tool == ToolEnum.CREATE || tool == ToolEnum.PACKAGE) && mouseCreationActivated) {
                        mode = Mode.CREATING;
                        createNodeController.onMousePressed(event);
                    } else if (tool == ToolEnum.MOVE_SCENE) {
                        mode = Mode.MOVING;
                        graphController.movePaneStart(graph.getAllGraphElements(), event);
                        event.consume();
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
            }
        });

        aDrawPane.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (tool == ToolEnum.EDGE && mode == Mode.CREATING) {
                    edgeController.onMouseDragged(event);
                } else if (tool == ToolEnum.SELECT && mode == Mode.SELECTING) {
                    selectController.onMouseDragged(event);
                }

                //--------- MOUSE EVENT FOR TESTING ---------- TODO
                else if ((tool == ToolEnum.CREATE || tool == ToolEnum.PACKAGE) && mode == Mode.CREATING && mouseCreationActivated) {
                    createNodeController.onMouseDragged(event);
                } else if (mode == Mode.MOVING && tool == ToolEnum.MOVE_SCENE) {
                    graphController.movePane(graph.getAllGraphElements(), event);
                }
                event.consume();
            }
        });

        aDrawPane.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (tool == ToolEnum.EDGE && mode == Mode.CREATING) {
                    //User is not creating an Edge between two nodes, so we don't handle that.
                    edgeController.removeDragLine();

                    mode = Mode.NO_MODE;

                } else if (tool == ToolEnum.EDGE) {
                    edgeController.removeDragLine();
                } else if (tool == ToolEnum.SELECT && mode == Mode.SELECTING) {
                    selectController.onMouseReleased(event);
                }
                // -------------- MOUSE EVENT FOR TESTING ---------------- TODO
                if (tool == ToolEnum.CREATE && mode == Mode.CREATING && mouseCreationActivated) {
                    //Create ClassNode
                    ClassNode node = createNodeController.createClassNodeMouse(event);

                    //Use CreateController to create ClassNodeView.
                    ClassNodeView nodeView = (ClassNodeView) createNodeController.onMouseReleased(event, node, currentScale);

                    nodeMap.put(nodeView, node);
                    allNodeViews.add(nodeView);
                    initNodeActions(nodeView);

                    undoManager.add(new AddDeleteNodeCommand(MainController.this, graph, nodeView, nodeMap.get(nodeView), true));
                    if (!createNodeController.currentlyCreating()) {
                        mode = Mode.NO_MODE;
                    }

                } else if (tool == ToolEnum.PACKAGE && mode == Mode.CREATING && mouseCreationActivated) { //TODO: combine double code
                    PackageNode node = createNodeController.createPackageNodeMouse(event);
                    PackageNodeView nodeView = (PackageNodeView) createNodeController.onMouseReleased(event, node, currentScale);
                    nodeMap.put(nodeView, node);
                    initNodeActions(nodeView);
                    undoManager.add(new AddDeleteNodeCommand(MainController.this, graph, nodeView, nodeMap.get(nodeView), true));
                    allNodeViews.add(nodeView);
                    //
                    if (!createNodeController.currentlyCreating()) {
                        mode = Mode.NO_MODE;
                    }
                } else if (mode == Mode.MOVING && tool == ToolEnum.MOVE_SCENE) {
                    graphController.movePaneFinished(event);
                    mode = Mode.NO_MODE;
                }
            }
        });

        ////////////////////////////////////////////////////////////////

        aDrawPane.setOnTouchPressed(new EventHandler<TouchEvent>() {
            @Override
            public void handle(TouchEvent event) {
                if ((tool == ToolEnum.CREATE || tool == ToolEnum.PACKAGE) && !mouseCreationActivated) {
                    mode = Mode.CREATING;
                    createNodeController.onTouchPressed(event);
                } else if (tool == ToolEnum.DRAW) {
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
                } else if (tool == ToolEnum.DRAW && mode == Mode.DRAWING) {
                    sketchController.onTouchMoved(event);
                }
                event.consume();

            }
        });

        aDrawPane.setOnTouchReleased(new EventHandler<TouchEvent>() {
            @Override
            public void handle(TouchEvent event) {
                if (tool == ToolEnum.CREATE && mode == Mode.CREATING && !mouseCreationActivated) {
                    //Create ClassNode
                    ClassNode node = createNodeController.createClassNode(event);

                    //Use CreateController to create ClassNodeView.
                    ClassNodeView nodeView = (ClassNodeView) createNodeController.onTouchReleased(node, currentScale);

                    nodeMap.put(nodeView, node);
                    allNodeViews.add(nodeView);
                    initNodeActions(nodeView);

                    undoManager.add(new AddDeleteNodeCommand(MainController.this, graph, nodeView, nodeMap.get(nodeView), true));
                    if (!createNodeController.currentlyCreating()) {
                        mode = Mode.NO_MODE;
                    }

                } else if (tool == ToolEnum.PACKAGE && mode == Mode.CREATING && !mouseCreationActivated) { //TODO: combine double code
                    PackageNode node = createNodeController.createPackageNode(event);
                    PackageNodeView nodeView = (PackageNodeView) createNodeController.onTouchReleased(node, currentScale);
                    nodeMap.put(nodeView, node);
                    initNodeActions(nodeView);
                    undoManager.add(new AddDeleteNodeCommand(MainController.this, graph, nodeView, nodeMap.get(nodeView), true));
                    allNodeViews.add(nodeView);
                    //
                    if (!createNodeController.currentlyCreating()) {
                        mode = Mode.NO_MODE;
                    }
                } else if (tool == ToolEnum.DRAW && mode == Mode.DRAWING) {
                    addSketch(sketchController.onTouchReleased(event), false);


                    //We only want to move out of drawing mode if there are no other current drawings
                    if (!sketchController.currentlyDrawing()) {
                        mode = Mode.NO_MODE;
                    }
                }
                event.consume();
            }
        });
    }

    public void addSketch(Sketch sketch, boolean isImport){
        initSketchActions(sketch);
        allSketches.add(sketch);
        if(isImport){
            aDrawPane.getChildren().add(sketch.getPath());
        } else {
            graph.addSketch(sketch);
            undoManager.add(new AddDeleteSketchCommand(instance, aDrawPane, sketch, true));
        }
    }

    private void initNodeActions(AbstractNodeView nodeView) {

        nodeView.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                //TODO Maybe needs some check here?
                if (event.getClickCount() == 2) {
                    nodeController.onDoubleClick(nodeView);
                    tool = ToolEnum.SELECT;
                    setButtonClicked(selectBtn);
                } else if (tool == ToolEnum.MOVE_SCENE) {
                    mode = Mode.MOVING;
                    graphController.movePaneStart(graph.getAllGraphElements(), event);
                    event.consume();
                } else if (event.getButton() == MouseButton.SECONDARY) {
                    nodeClicked = nodeView;
                    copyPasteController.copyPasteCoords = new double[]{nodeView.getX() + event.getX(), nodeView.getY() + event.getY()};
                    aContextMenu.show(nodeView, event.getScreenX(), event.getScreenY());
                } else if (tool == ToolEnum.SELECT) {
                    if (!(nodeView instanceof PackageNodeView)) {
                        nodeView.toFront();
                    }
                    if (mode == Mode.NO_MODE) //Resize
                    {
                        Point2D.Double eventPoint = new Point2D.Double(event.getX(), event.getY());
                        Point2D.Double cornerPoint = new Point2D.Double(nodeView.getWidth(), nodeView.getHeight());

                        if (eventPoint.distance(cornerPoint) < 20) {
                            mode = Mode.RESIZING;
                            nodeController.resizeStart(nodeView, event);
                        }
                    }

                    if (mode == Mode.NO_MODE) //Move, any kind of node
                    {
                        mode = Mode.DRAGGING;
                        if (!selectedNodes.contains(nodeView)) {
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
                if (tool == ToolEnum.SELECT && mode == Mode.DRAGGING) {
                    ArrayList<AbstractNode> selected = new ArrayList<>();
                    for (AbstractNodeView n : selectedNodes) {
                        selected.add(nodeMap.get(n));
                    }
                    nodeController.moveNodes(event);
                    //TODO JUST FOR TESTING:
                    sketchController.moveSketches(event);
                    nodeWasDragged = true;


                } else if (mode == Mode.MOVING && tool == ToolEnum.MOVE_SCENE) {
                    graphController.movePane(graph.getAllGraphElements(), event);
                } else if (tool == ToolEnum.SELECT && mode == Mode.RESIZING) {
                    nodeController.resize(nodeView, event);
                } else if (tool == ToolEnum.EDGE && mode == Mode.CREATING) {
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
                if (tool == ToolEnum.SELECT && mode == Mode.DRAGGING) {
                    double[] deltaTranslateVector = nodeController.moveNodesFinished(event);
                    //TODO JUST FOR TESTING
                    sketchController.moveSketchFinished(event);
                    CompoundCommand compoundCommand = new CompoundCommand();
                    for (AbstractNodeView movedView : selectedNodes) {
                        compoundCommand.add(new MoveGraphElementCommand(nodeMap.get(movedView), deltaTranslateVector[0], deltaTranslateVector[1]));
                    }
                    //TODO JUST FOR TESTING:
                    for (Sketch sketch : selectedSketches) {
                        compoundCommand.add(new MoveGraphElementCommand(sketch, deltaTranslateVector[0], deltaTranslateVector[1]));
                    }
                    undoManager.add(compoundCommand);
                    if (!nodeWasDragged) {
                        selectedNodes.remove(nodeView);
                        drawSelected();
                        nodeWasDragged = false;
                    }
                } else if (mode == Mode.MOVING && tool == ToolEnum.MOVE_SCENE) {
                    graphController.movePaneFinished(event);
                    mode = Mode.NO_MODE;
                } else if (tool == ToolEnum.SELECT && mode == Mode.RESIZING) {
                    nodeController.resizeFinished(nodeMap.get(nodeView), event);

                } else if (tool == ToolEnum.EDGE && mode == Mode.CREATING) {
                    AbstractNodeView startNodeView = null;
                    AbstractNodeView endNodeView = null;
                    model.Node startNode = null;
                    model.Node endNode = null;
                    for (AbstractNodeView nodeView : allNodeViews) {
                        if (nodeView.contains(edgeController.getStartPoint())) {
                            startNodeView = nodeView;
                            startNode = graph.findNode(edgeController.getStartPoint());
                        } else if (nodeView.contains(edgeController.getEndPoint())) {
                            endNodeView = nodeView;
                            endNode = graph.findNode(edgeController.getEndPoint());
                        }
                    }

                    AssociationEdge edge = new AssociationEdge(startNode, endNode);

                    AssociationEdgeView edgeView = (AssociationEdgeView) edgeController.
                            onMouseReleased(edge, startNodeView, endNodeView);

                    if (startNodeView != null && endNodeView != null && !graph.hasEdge(edge)) {
                        //initEdgeActions(edgeView);
                        allEdgeViews.add(edgeView);
                        graph.addEdge(edge);
                        aDrawPane.getChildren().add(edgeView);
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
                if (nodeView instanceof PackageNodeView && (tool == ToolEnum.CREATE || tool == ToolEnum.PACKAGE)) {
                    mode = Mode.CREATING;
                    createNodeController.onTouchPressed(event);
                } else if (tool == ToolEnum.DRAW) {
                    mode = Mode.DRAWING;
                    sketchController.onTouchPressed(event);
                }
                event.consume();
            }
        });

        nodeView.setOnTouchMoved(new EventHandler<TouchEvent>() {
            @Override
            public void handle(TouchEvent event) {
                if (nodeView instanceof PackageNodeView && (tool == ToolEnum.CREATE || tool == ToolEnum.PACKAGE) &&
                        mode == Mode.CREATING) {
                    createNodeController.onTouchDragged(event);
                } else if (tool == ToolEnum.DRAW && mode == Mode.DRAWING) {
                    sketchController.onTouchMoved(event);
                }
                event.consume();

            }
        });

        nodeView.setOnTouchReleased(new EventHandler<TouchEvent>() {
            @Override
            public void handle(TouchEvent event) {
                if (nodeView instanceof PackageNodeView && tool == ToolEnum.CREATE && mode == Mode.CREATING) {
                    //Create ClassNode
                    ClassNode node = createNodeController.createClassNode(event);

                    //Use CreateController to create ClassNodeView.
                    ClassNodeView nodeView = (ClassNodeView) createNodeController.onTouchReleased(node, currentScale);

                    nodeMap.put(nodeView, node);
                    allNodeViews.add(nodeView);
                    initNodeActions(nodeView);
                    undoManager.add(new AddDeleteNodeCommand(MainController.this, graph, nodeView, nodeMap.get(nodeView), true));

                    //If someone else is creating at the same time.
                    if (!createNodeController.currentlyCreating()) {
                        mode = Mode.NO_MODE;
                    }

                } else if (nodeView instanceof PackageNodeView && tool == ToolEnum.PACKAGE && mode == Mode.CREATING) { //TODO: combine double code
                    PackageNode node = createNodeController.createPackageNode(event);
                    PackageNodeView nodeView = (PackageNodeView) createNodeController.onTouchReleased(node, currentScale);
                    nodeMap.put(nodeView, node);
                    initNodeActions(nodeView);
                    undoManager.add(new AddDeleteNodeCommand(MainController.this, graph, nodeView, nodeMap.get(nodeView), true));
                    allNodeViews.add(nodeView);

                    //If someone else is creating at the same time.
                    if (!createNodeController.currentlyCreating()) {
                        mode = Mode.NO_MODE;
                    }
                } else if (tool == ToolEnum.DRAW && mode == Mode.DRAWING) {
                    Sketch sketch = sketchController.onTouchReleased(event);
                    initSketchActions(sketch);
                    allSketches.add(sketch);
                    graph.addSketch(sketch);
                    undoManager.add(new AddDeleteSketchCommand(instance, aDrawPane, sketch, true));

                    //We only want to move out of drawing mode if there are no other current drawings
                    if (!sketchController.currentlyDrawing()) {
                        mode = Mode.NO_MODE;
                    }
                } else if (mode == Mode.MOVING && tool == ToolEnum.MOVE_SCENE) {
                    //graphController.movePaneFinished(event); TODO
                    mode = Mode.NO_MODE;
                }
                event.consume();
            }
        });
    }

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
        for (Sketch sketch : allSketches) {
            if (selectedSketches.contains(sketch)) {
                sketch.setSelected(true);
                sketch.getPath().toFront();
            } else {
                sketch.setSelected(false);
                sketch.getPath().toFront();
            }
        }
    }

    /**
     * Deletes all selected nodes, edges and sketches.
     */
    void deleteSelected() {
        CompoundCommand command = new CompoundCommand();
        for (AbstractNodeView nodeView : selectedNodes) {
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

    //----------------- DELETING ----------------------------------------

    /**
     * Deletes nodes and its associated edges
     *
     * @param nodeView
     * @param pCommand Compound command from deleting all selected, if not null we create our own command.
     * @param undo     If true this is an undo and no command should be created
     */
    public void deleteNode(AbstractNodeView nodeView, CompoundCommand pCommand, boolean undo) {
        CompoundCommand command = null;
        if (pCommand == null && !undo) {
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
     * @param undo     If true this is an undo and no command should be created, also used by replaceEdge in EdgeController
     */
    public void deleteEdgeView(AbstractEdgeView edgeView, CompoundCommand pCommand, boolean undo) {
        CompoundCommand command = null;
        //TODO Ugly solution for replace.
        if (pCommand == null) {
            command = new CompoundCommand();
            selectedEdges.remove(edgeView);
        } else if (!undo) {
            command = pCommand;
        }

        AbstractEdge edge = edgeView.getRefEdge();
        graph.removeEdge(edge);
        aDrawPane.getChildren().remove(edgeView);
        selectedEdges.remove(edgeView);
        edgeView.setSelected(false);
        allEdgeViews.remove(edgeView);
        if (!undo) {
            command.add(new AddDeleteEdgeCommand(this, edgeView, edge, false));
        }
        if (pCommand == null && !undo) {
            undoManager.add(command);
        }
    }

    public void deleteSketch(Sketch sketch, CompoundCommand pCommand) {
        CompoundCommand command;
        //TODO Maybe not necessary for sketches.
        if (pCommand == null) {
            command = new CompoundCommand();
        } else {
            command = pCommand;
        }

        getGraphModel().removeSketch(sketch);
        aDrawPane.getChildren().remove(sketch.getPath());
        allSketches.remove(sketch);
        command.add(new AddDeleteSketchCommand(this, aDrawPane, sketch, false));
    }

    /**
     * Deletes all edges associated with the node
     *
     * @param node
     * @param command
     */
    public void deleteNodeEdges(AbstractNode node, CompoundCommand command, boolean undo) {
        AbstractEdge edge;
        ArrayList<AbstractEdgeView> edgeViewsToBeDeleted = new ArrayList<>();
        for (AbstractEdgeView edgeView : allEdgeViews) {
            edge = edgeView.getRefEdge();
            if (edge.getEndNode().equals(node) || edge.getStartNode().equals(node)) {
                getGraphModel().removeEdge(edgeView.getRefEdge());
                aDrawPane.getChildren().remove(edgeView);
                selectedEdges.remove(edgeView);
                edgeViewsToBeDeleted.add(edgeView);
                if (!undo) {
                    command.add(new AddDeleteEdgeCommand(this, edgeView, edgeView.getRefEdge(), false));
                }
            }
        }
        allEdgeViews.removeAll(edgeViewsToBeDeleted);
    }

    private void handleOnEdgeViewPressedEvents(AbstractEdgeView edgeView) {
        /*if (edgeView.isSelected()) {
            selectedEdges.remove(edgeView);
            edgeView.setSelected(false);
        } else {
            selectedEdges.add(edgeView);
            edgeView.setSelected(true);
        }*/
    }

    /**
     * initialize handlers for a sketch.
     *
     * @param sketch
     */
    private void initSketchActions(Sketch sketch) {
        //TODO Implement this.
        sketch.getPath().setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
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
                if (tool == ToolEnum.SELECT && mode == Mode.DRAGGING) {
                    double[] deltaTranslateVector = sketchController.moveSketchFinished(event);
                    sketchController.moveSketchFinished(event);
                    CompoundCommand compoundCommand = new CompoundCommand();
                    for (AbstractNodeView movedView : selectedNodes) {
                        compoundCommand.add(new MoveGraphElementCommand(nodeMap.get(movedView), deltaTranslateVector[0], deltaTranslateVector[1]));
                    }
                    //TODO JUST FOR TESTING:
                    for (Sketch sketch : selectedSketches) {
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


    //------------ Init Button -------------------------------------------

    @FXML
    Button createBtn, packageBtn, edgeBtn, selectBtn, drawBtn, undoBtn, redoBtn, moveBtn, deleteBtn, recognizeBtn;
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

        buttonInUse = createBtn;
        buttonInUse.getStyleClass().add("button-in-use");


        //---------------------- Actions for buttons ----------------------------
        createBtn.setOnAction(event -> {
            tool = ToolEnum.CREATE;
            setButtonClicked(createBtn);
        });

        packageBtn.setOnAction(event -> {
            tool = ToolEnum.PACKAGE;
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

        recognizeBtn.setOnAction(event -> recognize());
    }

    void setButtonClicked(Button b) {
        buttonInUse.getStyleClass().remove("button-in-use");
        buttonInUse = b;
        buttonInUse.getStyleClass().add("button-in-use");
    }

    private void recognize() {
        ArrayList<GraphElement> recognized = recognizeController.recognize(selectedSketches);
        CompoundCommand recognizeCompoundCommand = new CompoundCommand();

        for (GraphElement e : recognized) {
            if (e instanceof ClassNode) {
                ClassNodeView nodeView = new ClassNodeView((ClassNode) e);
                recognizeCompoundCommand.add(
                        new AddDeleteNodeCommand(MainController.this, graph, nodeView, (ClassNode) e, true));
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
                AbstractEdgeView edgeView = addEdgeView(edge);
                if (edgeView != null) {
                    recognizeCompoundCommand.add(new AddDeleteEdgeCommand(MainController.this, edgeView, edge, true));
                }
            }
        }
        //Add the removal of sketches to UndoManager:
        for (Sketch sketch : recognizeController.getSketchesToBeRemoved()) {
            recognizeCompoundCommand.add(new AddDeleteSketchCommand(this, aDrawPane, sketch, false));
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
            //umlMenuItem.setSelected(false);
            umlVisible = false;
        } else {
            for (AbstractNodeView nodeView : allNodeViews) {
                aDrawPane.getChildren().add(nodeView);
            }
            for (AbstractEdgeView edgView : allEdgeViews) {
                aDrawPane.getChildren().add(edgView);
            }
            setButtons(false, umlButtons);
            //umlMenuItem.setSelected(true);
            umlVisible = true;
            sketchesToFront();
        }
    }

    public void handleMenuActionSketches() {
        if (sketchesVisible) {
            for (Sketch sketch : allSketches) {
                aDrawPane.getChildren().remove(sketch.getPath());
            }

            setButtons(true, Arrays.asList(drawBtn));

            //sketchesMenuItem.setSelected(false);
            sketchesVisible = false;
        } else {
            for (Sketch sketch : allSketches) {
                aDrawPane.getChildren().add(sketch.getPath());
            }

            setButtons(false, Arrays.asList(drawBtn));
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
        graph.setName(file.getName());
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
            graph = PersistenceManager.importXMI(file.getAbsolutePath());
        }
        load(graph);
    }

    public void handleMenuActionNew() {
        reset();
    }


    //------------------------- Context Menu ---------------------------------
    private void initContextMenu() {
        aContextMenu = new ContextMenu();

        MenuItem cmItemDelete = new MenuItem("Delete");
        cmItemDelete.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //TODO Is this really needed? Why just not delete all selected?
                if (aContextMenu.getOwnerNode() instanceof AbstractNodeView) {
                    deleteNode((AbstractNodeView) aContextMenu.getOwnerNode(), null, false);
                }
                deleteSelected();
            }
        });


        MenuItem cmItemCopy = new MenuItem("Copy");
        cmItemCopy.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                copyPasteController.copy();
                mode = Mode.NO_MODE;
            }
        });

        MenuItem cmItemPaste = new MenuItem("Paste");
        cmItemPaste.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                copyPasteController.paste();
                mode = Mode.NO_MODE;
            }
        });

        MenuItem cmItemInsertImg = new MenuItem("Insert Image");
        cmItemInsertImg.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Point2D.Double point = new Point2D.Double(copyPasteController.copyPasteCoords[0], copyPasteController.copyPasteCoords[1]);
                InsertIMG insertImg = new InsertIMG(aStage, aDrawPane);
                insertImg.openFileChooser(MainController.this, point);
            }

            });

        aContextMenu.getItems().addAll(cmItemCopy, cmItemPaste, cmItemDelete,cmItemInsertImg);
    }

    /**
     * Creates and adds a new NodeView
     *
     * @param node
     * @return
     */
    public AbstractNodeView createNodeView(AbstractNode node) {
        AbstractNodeView newView;
        if (node instanceof ClassNode) {
            newView = new ClassNodeView((ClassNode) node);
        } else /*if (node instanceof PackageNode)*/ {
            newView = new PackageNodeView((PackageNode) node);
        }

        if (newView instanceof ClassNodeView) {
            newView.toFront();
        } else {
            newView.toBack();
            gridToBack();
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
        graph.addNode(picNode);
        nodeMap.put(picView, picNode);
        return picView;
    }

    /**
     * Creates and adds a new EdgeView
     *
     * @param edge
     * @param startNodeView
     * @param endNodeView
     * @return
     */
    public AbstractEdgeView createEdgeView(AbstractEdge edge, AbstractNodeView startNodeView, AbstractNodeView endNodeView) {
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
            graph.addEdge(edgeView.getRefEdge());
            //initEdgeActions(edgeView);
            allEdgeViews.add(edgeView);
        }

        return edgeView;
    }

    /**
     * @param edge
     * @return null if graph already hasEdge or start/endnodeview is null. Otherwise the created AbstractEdgeView.
     */
    public AbstractEdgeView addEdgeView(AbstractEdge edge) {
        //TODO Really ugly
        AbstractNodeView startNodeView = null;
        AbstractNodeView endNodeView = null;
        for (AbstractNodeView nodeView : allNodeViews) {
            if (edge.getStartNode() == nodeMap.get(nodeView)) {
                startNodeView = nodeView;
            } else if (edge.getEndNode() == nodeMap.get(nodeView)) {
                endNodeView = nodeView;
            }
        }
        if (startNodeView == null || endNodeView == null /*|| graph.hasEdge(edge)*/) {
            System.out.println("Failed to find start or end node, or graph already has edge.");
            return null;
        } else {
            AbstractEdgeView edgeView;
            if(edge instanceof AssociationEdge) {
                edgeView = new AssociationEdgeView(edge, startNodeView, endNodeView);
            } else if (edge instanceof AggregationEdge){
                edgeView = new AggregationEdgeView(edge, startNodeView, endNodeView);
            } else if (edge instanceof CompositionEdge) {
                edgeView = new CompositionEdgeView(edge, startNodeView, endNodeView);
            } else if (edge instanceof InheritanceEdge) {
                edgeView = new InheritanceEdgeView(edge, startNodeView, endNodeView);
            } else {
                System.out.println("Edge type not recognised. In addEdgeView(AbstractEdge edge).");
                return null;
            }
            //initEdgeActions(edgeView);
            allEdgeViews.add(edgeView);
            aDrawPane.getChildren().add(edgeView);
            return edgeView;
        }
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


    private void load(Graph pGraph) {
        reset();

        if (pGraph != null) {
            this.graph = pGraph;
            for (AbstractNode node : graph.getAllNodes()) {
                createNodeView(node);
            }

            for (Edge edge : graph.getAllEdges()) {
                addEdgeView((AbstractEdge) edge);
            }

            for(Sketch sketch : graph.getAllSketches()){
                addSketch(sketch, true);
            }
        }
    }

    //------------------------------------ GRID -------------------------------

    private ArrayList<Line> grid = new ArrayList<>();

    public ArrayList<Line> getGrid() {
        return grid;
    }

    private void drawGrid() {
        for (int i = 0; i < 8000; i += 20) {
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
        for (Sketch sketch : allSketches) {
            sketch.getPath().toFront();
        }
    }

    //------------------------ Zoom-feature -------------------------------------

    private void initZoomSlider() {

        zoomSlider.valueProperty().addListener(new ChangeListener<Number>() {
            public void changed(ObservableValue<? extends Number> ov,
                                Number old_val, Number new_val) {
                if (zoomSlider.isValueChanging()) {
                    graphController.zoomPane(old_val.doubleValue(), new_val.doubleValue());
                }
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

    protected Graph getGraphModel() {
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

    public ArrayList getAllSketches() {
        return allSketches;
    }

    public UndoManager getUndoManager() {
        return undoManager;
    }

    public ScrollPane getScrollPane(){
        return aScrollPane;
    }
    //------------------------------Insert Image ----------------------------------

    public void handleMenuActionInsert (){
        InsertIMG insertIMG = new InsertIMG(aStage, aDrawPane);
        insertIMG.openFileChooser(this, new Point2D.Double(0,0));
    }

// end
}

