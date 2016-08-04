package controller;

import controller.dialog.NodeEditDialogController;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.AbstractNode;
import model.ClassNode;
import model.PackageNode;
import util.Constants;
import view.AbstractNodeView;
import view.ClassNodeView;
import view.PackageNodeView;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.*;

/**
 * Used by MainController for handling moving and resizing Nodes, among other things.
 */
public class NodeController {
    //For resizing rectangles
    private Rectangle dragRectangle;

    private Pane aDrawPane;
    private MainController aMainController;

    //For drag-moving nodes
    private double initMoveX, initMoveY;
    private HashMap<AbstractNode, Point2D.Double> initTranslateMap = new HashMap<>();
    private ArrayList<AbstractNode> toBeMoved = new ArrayList<>();
    private HashMap<AbstractNode, Line> xSnapIndicatorMap = new HashMap<>();
    private HashMap<AbstractNode, Line> ySnapIndicatorMap = new HashMap<>();

    public NodeController(Pane pDrawPane, MainController pMainController){

        aMainController = pMainController;
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

    public void scaleDragRectangle(double scaleX, double scaleY)
    {
        dragRectangle.setScaleX(scaleX);
        dragRectangle.setScaleY(scaleY);
    }

    public void resizeStart(AbstractNodeView nodeView){
        aDrawPane.getChildren().add(dragRectangle);
        dragRectangle.setWidth(nodeView.getWidth());
        dragRectangle.setHeight(nodeView.getHeight());
        dragRectangle.setX(nodeView.getTranslateX());
        dragRectangle.setY(nodeView.getTranslateY());
    }

    public void resize(MouseEvent event){
        dragRectangle.setWidth(event.getX());
        dragRectangle.setHeight(event.getY());

        ArrayList<AbstractNodeView> selectedNodes = aMainController.getAllNodeViews();
        for(AbstractNodeView n : selectedNodes){
            if(n instanceof PackageNodeView){
                checkChildren((PackageNodeView)n);
            }
            putNodeInPackage(n);
        }
    }

    public void resizeFinished(AbstractNode node){ //TODO event parameter not needed
        node.setWidth(dragRectangle.getWidth());
        node.setHeight(dragRectangle.getHeight());
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
        for(AbstractNodeView nodeView : aMainController.getSelectedNodes()){
            selectedNodes.add(aMainController.getNodeMap().get(nodeView));
        }

        //Move all selected nodes and their children. (Only package nodes can have children)
        for(AbstractNode n : aMainController.getGraphModel().getAllNodes()){
            if(selectedNodes.contains(n)) {
                initTranslate = new Point2D.Double(n.getTranslateX(), n.getTranslateY());
                initTranslateMap.put(n, initTranslate);
                toBeMoved.add(n);
                createSnapIndicators(n);
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
        double offsetX = event.getSceneX() - initMoveX;
        double offsetY = event.getSceneY() - initMoveY;

        //Drag all selected nodes and their children
        for(AbstractNode n : toBeMoved)
        {
            Double x = initTranslateMap.get(n).getX() + offsetX;
            Double y = initTranslateMap.get(n).getY() + offsetY;
            n.setTranslateX(x);
            n.setTranslateY(y);
            n.setX(x);
            n.setY(y);
            setSnapIndicators(closestInteger(x.intValue(), Constants.GRID_DISTANCE), closestInteger(y.intValue(), Constants.GRID_DISTANCE), n);


        }
    }

    public double[] moveNodesFinished(MouseEvent event){
        double offsetX = event.getSceneX() - initMoveX;
        double offsetY = event.getSceneY() - initMoveY;
        for(AbstractNode n : toBeMoved) {
            Double x = initTranslateMap.get(n).getX() + offsetX; //Calculate real position after move
            Double y = initTranslateMap.get(n).getY() + offsetY;
            int xSnap = closestInteger(x.intValue(), 20); //Snap to grid
            int ySnap = closestInteger(y.intValue(), 20);
            n.setTranslateX(xSnap);
            n.setTranslateY(ySnap);
            n.setX(xSnap);
            n.setY(ySnap);
        }

        toBeMoved.clear();
        initTranslateMap.clear();
        removeSnapIndicators();
        double[] deltaTranslateVector = new double[2];
        deltaTranslateVector[0] = event.getSceneX() - initMoveX;
        deltaTranslateVector[1] = event.getSceneY() - initMoveY;

        ArrayList<AbstractNodeView> selectedNodes = aMainController.getSelectedNodes();

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
        Map<AbstractNodeView, AbstractNode> nodeMap = aMainController.getNodeMap();
        for(AbstractNodeView potentialParent : aMainController.getAllNodeViews()){
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
        Map<AbstractNodeView, AbstractNode> nodeMap = aMainController.getNodeMap();
        PackageNode packageNodeModel = (PackageNode) nodeMap.get(packageNodeView);
        AbstractNode potentialChildModel;
        for (AbstractNodeView potentialChild : aMainController.getAllNodeViews()){
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
     * @param xSnap
     * @param ySnap
     * @param n
     */
    private void setSnapIndicators(int xSnap, int ySnap, AbstractNode n){
        Line xSnapIndicator = xSnapIndicatorMap.get(n);
        Line ySnapIndicator = ySnapIndicatorMap.get(n);

        xSnapIndicator.setStartX(xSnap);
        xSnapIndicator.setEndX(xSnap);
        xSnapIndicator.setStartY(ySnap);
        xSnapIndicator.setEndY(ySnap+Constants.GRID_DISTANCE);

        ySnapIndicator.setStartX(xSnap);
        ySnapIndicator.setEndX(xSnap+Constants.GRID_DISTANCE);
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
    }

    public void onDoubleClick(AbstractNodeView nodeView){
        if(nodeView instanceof ClassNodeView){
            showClassNodeEditDialog((ClassNode) aMainController.getNodeMap().get(nodeView));
        }
        else {
            showNodeTitleDialog(aMainController.getNodeMap().get(nodeView));

        }
    }

    /**
     * Brings up a dialog to give a title to a Node.
     * @param node, the Node to give a Title
     * @return false if node == null, otherwise true.
     */
    private boolean showNodeTitleDialog(AbstractNode node){
        if (node == null) {
            return false;
        }
        VBox group = new VBox();
        TextField input = new TextField();
        Button okButton = new Button("Ok");
        okButton.setOnAction(event -> {
            node.setTitle(input.getText());
            aDrawPane.getChildren().remove(group);
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(event -> aDrawPane.getChildren().remove(group));

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
        try {
            // Load the fxml file and create a new stage for the popup
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("nodeEditDialog.fxml"));

            AnchorPane dialog = (AnchorPane) loader.load();
            dialog.setBackground(new Background(new BackgroundFill(Color.WHITESMOKE, new CornerRadii(1), null)));
            dialog.setStyle("-fx-border-color: black");

            //Set location for dialog.
            double maxX = aDrawPane.getWidth() - dialog.getPrefWidth();
            double maxY = aDrawPane.getHeight() - dialog.getPrefHeight();
            dialog.setLayoutX(Math.min(maxX,node.getTranslateX()+5));
            dialog.setLayoutY(Math.min(maxY, node.getTranslateY()+5));

            NodeEditDialogController controller = loader.getController();

            controller.setNode(node);
            controller.getOkButton().setOnMousePressed(event -> {
                node.setTitle(controller.getTitle());
                node.setAttributes(controller.getAttributes());
                node.setOperations(controller.getOperations());
                aDrawPane.getChildren().remove(dialog);
                aMainController.removeDialog(dialog);
            });

            controller.getCancelButton().setOnAction(event -> {
                aDrawPane.getChildren().remove(dialog);
                aMainController.removeDialog(dialog);
            });
            aDrawPane.getChildren().add(dialog);
            aMainController.addDialog(dialog);
            return controller.isOkClicked();

        } catch (IOException e) {
            // Exception gets thrown if the fxml file could not be loaded
            e.printStackTrace();
            return false;
        }
    }
}
