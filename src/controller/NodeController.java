package controller;

import controller.dialog.NodeEditDialogController;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import model.nodes.AbstractNode;
import model.nodes.ClassNode;
import model.nodes.PackageNode;
import util.Constants;
import util.commands.*;
import view.nodes.AbstractNodeView;
import view.nodes.ClassNodeView;
import view.nodes.PackageNodeView;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Used by MainController for handling moving and resizing Nodes, among other things.
 */
public class NodeController {
    //For resizing rectangles
    private Rectangle dragRectangle;

    private Pane aDrawPane;
    private AbstractDiagramController diagramController;
    private boolean snapToGrid = true, snapIndicators = false;
    private AbstractNode currentResizeNode;

    //For drag-moving nodes
    private double initMoveX, initMoveY;
    private HashMap<AbstractNode, Point2D.Double> initTranslateMap = new HashMap<>();
    private ArrayList<AbstractNode> toBeMoved = new ArrayList<>();
    private HashMap<AbstractNode, Line> xSnapIndicatorMap = new HashMap<>();
    private HashMap<AbstractNode, Line> ySnapIndicatorMap = new HashMap<>();

    public NodeController(Pane pDrawPane, AbstractDiagramController pDiagramController){

        diagramController = pDiagramController;
        aDrawPane = pDrawPane;

        dragRectangle = new Rectangle();
        dragRectangle.setFill(null);
        dragRectangle.setStroke(Color.BLACK);
    }

    public void translateDragRectangle(double translateX, double translateY)
    {
        dragRectangle.setTranslateX(translateX);
        dragRectangle.setTranslateY(translateY);
    }

    public void resizeStart(AbstractNodeView nodeView){
        aDrawPane.getChildren().add(dragRectangle);
        dragRectangle.setWidth(nodeView.getWidth());
        dragRectangle.setHeight(nodeView.getHeight());
        dragRectangle.setX(nodeView.getTranslateX());
        dragRectangle.setY(nodeView.getTranslateY());
        currentResizeNode = nodeView.getRefNode();
        createSnapIndicators(currentResizeNode);
    }

    public void resize(MouseEvent event){
        dragRectangle.setWidth(event.getX());
        dragRectangle.setHeight(event.getY());

        if(snapIndicators){
            Double w = dragRectangle.getWidth() + dragRectangle.getX();
            Double h = dragRectangle.getHeight() + dragRectangle.getY();
            setSnapIndicators(closestInteger(w.intValue(), Constants.GRID_DISTANCE), closestInteger(h.intValue(), Constants.GRID_DISTANCE), currentResizeNode, false);
        }

        ArrayList<AbstractNodeView> selectedNodes = diagramController.getAllNodeViews();
        for(AbstractNodeView n : selectedNodes){
            if(n instanceof PackageNodeView){
                checkChildren((PackageNodeView)n);
            }
            putNodeInPackage(n);
        }
    }

    public void resizeFinished(AbstractNode node){
        double oldWidth = node.getWidth();
        double oldHeight = node.getHeight();
        if(snapToGrid){
            Double w = dragRectangle.getWidth();
            Double h = dragRectangle.getHeight();
            node.setWidth(closestInteger(w.intValue(), Constants.GRID_DISTANCE));
            node.setHeight(closestInteger(h.intValue(), Constants.GRID_DISTANCE));
        } else {
            node.setWidth(dragRectangle.getWidth());
            node.setHeight(dragRectangle.getHeight());
        }

        diagramController.getUndoManager().add(new ResizeNodeCommand(node, oldWidth, oldHeight, node.getWidth(), node.getHeight()));

        removeSnapIndicators();
        dragRectangle.setHeight(0);
        dragRectangle.setWidth(0);
        translateDragRectangle(0,0);
        aDrawPane.getChildren().remove(dragRectangle);
    }

    public void moveNodesStart(MouseEvent event){
        initMoveX = event.getSceneX();
        initMoveY = event.getSceneY();

        Point2D.Double initTranslate;
        ArrayList<AbstractNode> selectedNodes = new ArrayList<>();
        for(AbstractNodeView nodeView : diagramController.getSelectedNodes()){
            selectedNodes.add(diagramController.getNodeMap().get(nodeView));
        }

        //Move all selected nodes and their children. (Only package nodes can have children)
        for(AbstractNode n : diagramController.getGraphModel().getAllNodes()){
            if(selectedNodes.contains(n)) {
                initTranslate = new Point2D.Double(n.getTranslateX(), n.getTranslateY());
                initTranslateMap.put(n, initTranslate);
                toBeMoved.add(n);
                if(snapIndicators){
                    createSnapIndicators(n);
                }
                if (n instanceof PackageNode) {
                    for (AbstractNode child : ((PackageNode) n).getChildNodes()) {
                        if (!selectedNodes.contains(child)) {
                            initTranslate = new Point2D.Double(child.getTranslateX(), child.getTranslateY());
                            initTranslateMap.put(child, initTranslate);
                            toBeMoved.add(child);
                        }
                    }
                }
            }
        }
    }

    public void moveNodes(MouseEvent event){
        double offsetX = (event.getSceneX() - initMoveX)*(1/diagramController.drawPane.getScaleX());
        double offsetY = (event.getSceneY() - initMoveY)*(1/diagramController.drawPane.getScaleY());

        //Drag all selected nodes and their children
        for(AbstractNode n : toBeMoved)
        {
            Double x = initTranslateMap.get(n).getX() + offsetX;
            Double y = initTranslateMap.get(n).getY() + offsetY;
            n.setTranslateX(x);
            n.setTranslateY(y);
            n.setX(x);
            n.setY(y);
            if(snapIndicators){
                setSnapIndicators(closestInteger(x.intValue(), Constants.GRID_DISTANCE), closestInteger(y.intValue(), Constants.GRID_DISTANCE), n, true);
            }
        }
    }

    public double[] moveNodesFinished(MouseEvent event){
        for(AbstractNode n : toBeMoved) {
            Double x = n.getTranslateX();
            Double y = n.getTranslateY();
            if(snapToGrid){
                int xSnap = closestInteger(x.intValue(), 20); //Snap to grid
                int ySnap = closestInteger(y.intValue(), 20);
                n.setTranslateX(xSnap);
                n.setTranslateY(ySnap);
                n.setX(xSnap);
                n.setY(ySnap);
            } else {
                n.setTranslateX(x);
                n.setTranslateY(y);
                n.setX(x);
                n.setY(y);
            }

        }

        toBeMoved.clear();
        initTranslateMap.clear();
        if (snapIndicators){
            removeSnapIndicators();
        }

        double[] deltaTranslateVector = new double[2];
        deltaTranslateVector[0] = event.getSceneX() - initMoveX;
        deltaTranslateVector[1] = event.getSceneY() - initMoveY;

        ArrayList<AbstractNodeView> selectedNodes = diagramController.getSelectedNodes();

        for(AbstractNodeView n : selectedNodes){
            if(n instanceof PackageNodeView){
                checkChildren((PackageNodeView)n);
            }
            putNodeInPackage(n);
        }
        return deltaTranslateVector;
    }

    /**
     * @param a
     * @param b
     * @return Multiple of b that is closest to a. Used for "snapping to grid".
     */
    static int closestInteger(int a, int b) { //TODO GRID DISTANCE CONSTANT
        int c1 = a - (a % b);
        int c2 = (a + b) - (a % b);
        if (a - c1 > c2 - a) {
            return c2;
        } else {
            return c1;
        }
    }

    /**
     * If potentialChild is graphically inside a package view it will be added as a child to that package node.
     * @param potentialChild
     * @return
     */
    private boolean putNodeInPackage(AbstractNodeView potentialChild){
        boolean childMovedInside = false;
        Map<AbstractNodeView, AbstractNode> nodeMap = diagramController.getNodeMap();
        for(AbstractNodeView potentialParent : diagramController.getAllNodeViews()){
            if(potentialParent instanceof PackageNodeView && potentialParent != potentialChild)
            {
                if(potentialParent.getBoundsInParent().contains(potentialChild.getBoundsInParent()))
                {
                    if(!((PackageNode)nodeMap.get(potentialParent)).getChildNodes().contains(nodeMap.get(potentialChild))){
                        ((PackageNode)nodeMap.get(potentialParent)).addChild(nodeMap.get(potentialChild));
                    }
                    childMovedInside = true;
                } else {
                    //Remove child if it is moved out of the package
                    ((PackageNode)nodeMap.get(potentialParent)).getChildNodes().remove(nodeMap.get(potentialChild));

                }
            }
        }
        return childMovedInside;
    }

    /**
     * Checks whether a packageNode contains any children.
     */
    private void checkChildren(PackageNodeView packageNodeView){
        Map<AbstractNodeView, AbstractNode> nodeMap = diagramController.getNodeMap();
        PackageNode packageNodeModel = (PackageNode) nodeMap.get(packageNodeView);
        AbstractNode potentialChildModel;
        for (AbstractNodeView potentialChild : diagramController.getAllNodeViews()){
            potentialChildModel = nodeMap.get(potentialChild);
            if(packageNodeView != potentialChild && packageNodeView.getBoundsInParent().contains(potentialChild.getBoundsInParent())){
                if(!packageNodeModel.getChildNodes().contains(potentialChildModel)){
                    packageNodeModel.addChild(potentialChildModel);
                }
            } else {
                packageNodeModel.getChildNodes().remove(potentialChildModel);
            }
        }
    }

    /**
     * Initializes snap inidicators for AbstractNode.
     * @param n
     */
    private void createSnapIndicators(AbstractNode n){
        Line xSnapIndicator = new Line(0,0,0,0);
        Line ySnapIndicator = new Line(0,0,0,0);
        xSnapIndicator.setStroke(Color.BLACK);
        ySnapIndicator.setStroke(Color.BLACK);
        aDrawPane.getChildren().addAll(xSnapIndicator, ySnapIndicator);
        xSnapIndicatorMap.put(n, xSnapIndicator);
        ySnapIndicatorMap.put(n, ySnapIndicator);
    }

    /**
     * Places snap indicators to where the node would be snapped to.
     * @param move True if moving, false if resizing.
     * @param xSnap
     * @param ySnap
     * @param n
     */
    private void setSnapIndicators(int xSnap, int ySnap, AbstractNode n, boolean move){
        Line xSnapIndicator = xSnapIndicatorMap.get(n);
        Line ySnapIndicator = ySnapIndicatorMap.get(n);

        xSnapIndicator.setStartX(xSnap);
        xSnapIndicator.setEndX(xSnap);
        xSnapIndicator.setStartY(ySnap);
        if(move){ xSnapIndicator.setEndY(ySnap+Constants.GRID_DISTANCE);
        } else { xSnapIndicator.setEndY(ySnap-Constants.GRID_DISTANCE);}

        ySnapIndicator.setStartX(xSnap);
        if (move) { ySnapIndicator.setEndX(xSnap+Constants.GRID_DISTANCE);
        } else {ySnapIndicator.setEndX(xSnap-Constants.GRID_DISTANCE);}

        ySnapIndicator.setStartY(ySnap);
        ySnapIndicator.setEndY(ySnap);
    }

    /**
     * Removes all snap indicators from the view.
     */
    private void removeSnapIndicators(){
        aDrawPane.getChildren().removeAll(xSnapIndicatorMap.values());
        aDrawPane.getChildren().removeAll(ySnapIndicatorMap.values());
        xSnapIndicatorMap.clear();
        ySnapIndicatorMap.clear();
        currentResizeNode = null; //Only needed when resizing
    }

    public void onDoubleClick(AbstractNodeView nodeView){
        if(nodeView instanceof ClassNodeView){
            showClassNodeEditDialog((ClassNode) diagramController.getNodeMap().get(nodeView));
        }
        else { //PackageNode
            showNodeTitleDialog(diagramController.getNodeMap().get(nodeView));
        }
    }

    public void setSnapIndicators(boolean snapIndicators) {
        this.snapIndicators = snapIndicators;
    }

    public void setSnapToGrid(boolean snapToGrid) {
        this.snapToGrid = snapToGrid;
    }


    //------------------- VOICE ----------------------
    /**
     * Brings up a dialog to give a title to a Node.
     * @param node, the Node to give a Title
     * @return false if node == null, otherwise true.
     */
    private boolean showNodeTitleDialog(AbstractNode node){
        if(diagramController.voiceController.voiceEnabled){
            //Change variable testing in VoiceController to 1(true)
            diagramController.voiceController.testing = 1;

            String title2 = "";
            int time = 0;
            //Looking for a name you want to add to the package or until 5 seconds have passed
            while((title2.equals("") || title2 == null) && time < 500){
                try {
                    TimeUnit.MILLISECONDS.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //Check if a name has been recognised
                title2 = diagramController.voiceController.titleName;
                time++;
            }

            //Change variable testing in VoiceController to 0(false)
            diagramController.voiceController.testing = 0;

            //If name found in less then 5 seconds it sets the name to the package
            if(time < 500) {
                diagramController.voiceController.titleName = "";
                node.setTitle(title2);
            }
            //Else the name is not changed to a new name
            else{
                diagramController.voiceController.titleName = "";
            }

            node.setTitle(title2);
        }

        VBox group = new VBox();
        TextField input = new TextField();
        input.setText(node.getTitle());
        Button okButton = new Button("Ok");
        okButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                node.setTitle(input.getText());
                aDrawPane.getChildren().remove(group);
            }
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                aDrawPane.getChildren().remove(group);
            }
        });

        Label label = new Label("Choose title");
        group.getChildren().add(label);
        group.getChildren().add(input);
        HBox buttons = new HBox();
        buttons.getChildren().add(okButton);
        buttons.getChildren().add(cancelButton);
        buttons.setPadding(new Insets(15, 0, 0, 0));
        group.getChildren().add(buttons);
        group.setLayoutX(node.getX()+5);
        group.setLayoutY(node.getY()+5);
        group.setBackground(new Background(new BackgroundFill(Color.WHITESMOKE, new CornerRadii(1), null)));
        group.setStyle("-fx-border-color: black");
        group.setPadding(new Insets(15, 12, 15, 12));
        aDrawPane.getChildren().add(group);

        return true;
    }

    public boolean showClassNodeEditDialog(ClassNode node) {
        if(diagramController.voiceController.voiceEnabled) {

            //Change variable testing in MainController to 1(true)
            diagramController.voiceController.testing = 1;

            String title = "";
            int time = 0;
            //Looking for a name you want to add to the class or until 5 seconds have passed
            while ((title.equals("") || title == null) && time < 500) {
                try {
                    TimeUnit.MILLISECONDS.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //Check if a name has been commanded
                title = diagramController.voiceController.titleName;
                time++;
            }

            //Change variable testing in MainController to 0(false)
            diagramController.voiceController.testing = 0;

            //If name found in less then 5 seconds it sets the name to the class
            if (time < 500) {
                diagramController.voiceController.titleName = "";
                node.setTitle(title);
            }
            //Else the name is not changed to a new name
            else {
                diagramController.voiceController.titleName = "";
            }
        }


        try {
            //Load the classDiagramView.fxml file and create a new stage for the popup
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("view/fxml/nodeEditDialog.fxml"));

            AnchorPane dialog = loader.load();
            dialog.setBackground(new Background(new BackgroundFill(Color.WHITESMOKE, new CornerRadii(1), null)));
            dialog.setStyle("-fx-border-color: black");
            //Set location for dialog.
            double maxX = aDrawPane.getWidth() - dialog.getPrefWidth();
            double maxY = aDrawPane.getHeight() - dialog.getPrefHeight();
            dialog.setLayoutX(Math.min(maxX,node.getTranslateX()+5));
            dialog.setLayoutY(Math.min(maxY, node.getTranslateY()+5));

            NodeEditDialogController controller = loader.getController();
            controller.setNode(node);
            controller.getOkButton().setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    CompoundCommand command = new CompoundCommand();
                    if(controller.hasTitledChanged()){
                        command.add(new SetNodeTitleCommand(node, controller.getTitle(), node.getTitle()));
                        node.setTitle(controller.getTitle());
                    }
                    if(controller.hasAttributesChanged()){
                        command.add(new SetNodeAttributeCommand(node, controller.getAttributes(), node.getAttributes()));
                        node.setAttributes(controller.getAttributes());
                    }
                    if(controller.hasOperationsChanged()){
                        command.add(new SetNodeOperationsCommand(node, controller.getOperations(), node.getOperations()));
                        node.setOperations(controller.getOperations());
                    }
                    if(command.size() > 0){
                        diagramController.getUndoManager().add(command);
                    }
                    aDrawPane.getChildren().remove(dialog);
                    diagramController.removeDialog(dialog);
                }
            });

            controller.getCancelButton().setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    aDrawPane.getChildren().remove(dialog);
                    diagramController.removeDialog(dialog);
                }
            });
            aDrawPane.getChildren().add(dialog);
            diagramController.addDialog(dialog);
            return controller.isOkClicked();

        } catch (IOException e) {
            //Exception gets thrown if the classDiagramView.fxml file could not be loaded
            e.printStackTrace();
            return false;
        }
    }
}
