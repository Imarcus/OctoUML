package controller;

import javafx.scene.control.Dialog;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import model.AbstractNode;
import model.PackageNode;
import view.AbstractNodeView;
import view.PackageNodeView;

import java.awt.geom.Point2D;
import java.util.*;

/**
 * Created by marcusisaksson on 2016-02-12.
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

    public void resizeStart(AbstractNodeView nodeView, MouseEvent event){
        aDrawPane.getChildren().add(dragRectangle);
        dragRectangle.setX(0);
        dragRectangle.setY(0);
        translateDragRectangle(nodeView.getTranslateX(), nodeView.getTranslateY());
        dragRectangle.setWidth(event.getSceneX() -  nodeView.getTranslateX());
        dragRectangle.setHeight(event.getSceneY() - nodeView.getTranslateY());
    }

    public void resize(AbstractNodeView nodeView, MouseEvent event){
        dragRectangle.setWidth(event.getSceneX() - nodeView.getTranslateX());
        dragRectangle.setHeight(event.getSceneY() - nodeView.getTranslateY());
        //putNodeInPackage(nodeView);

        ArrayList<AbstractNodeView> selectedNodes = aMainController.getAllNodeViews();
        for(AbstractNodeView n : selectedNodes){
            if(n instanceof PackageNodeView){
                checkChildren((PackageNodeView)n);
            }
            putNodeInPackage(n);
        }
    }

    public void resizeFinished(AbstractNode node, MouseEvent event){
        node.setWidth(event.getSceneX() - node.getTranslateX());
        node.setHeight(event.getSceneY() - node.getTranslateY());
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
            n.setTranslateX(initTranslateMap.get(n).getX() + offsetX);
            n.setTranslateY(initTranslateMap.get(n).getY() + offsetY);
        }
    }

    public double[] moveNodesFinished(MouseEvent event){
        toBeMoved.clear();
        initTranslateMap.clear();
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
     * Brings up a dialog to give a title to a Node.
     * @param node, the Node to give a Title
     * @return false if node == null, otherwise true.
     */
    public boolean addNodeTitle(AbstractNode node){
        if (node == null)
        {
            return false;
        }
        Dialog <String> dialog = new TextInputDialog();
        dialog.setTitle("Choose title");
        dialog.setHeaderText("Choose title");

        Optional<String> result = dialog.showAndWait();
        String entered = "none.";

        if (result.isPresent())
        {
            entered = result.get();
        }

        if(!entered.equals("none."))
        {
            node.setTitle(entered);
        }
        return true;
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
                    System.out.println("INSIDE");
                } else {
                    System.out.println("OUTSIDE");
                    //Remove child if it is moved out of the package
                    ((PackageNode)nodeMap.get(potentialParent)).getChildNodes().remove(nodeMap.get(potentialChild));

                }
                System.out.println("CHILDLIST SIZE: " + ((PackageNode) nodeMap.get(potentialParent)).getChildNodes().size());
                for(AbstractNode n : ((PackageNode)nodeMap.get(potentialParent)).getChildNodes()) {
                    System.out.println(n);
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
}
