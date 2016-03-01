package controller;

import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import model.AbstractNode;
import model.ClassNode;
import model.Graph;
import model.PackageNode;
import view.AbstractNodeView;
import view.ClassNodeView;
import view.NodeView;
import view.PackageNodeView;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by marcusisaksson on 2016-02-12.
 */
//TODO should be moved to NodeController!
public class CreateNodeController {

    //For drag-creating rectangles
    private double mouseDragStartX, mouseDragStartY;
    private HashMap<Integer, Rectangle> dragRectangles = new HashMap<>();
    private Rectangle mouseDragRectangle;

    private MainController aMainController;
    private Pane aDrawPane;

    public CreateNodeController(Pane pDrawPane, MainController pMainController){
        aMainController = pMainController;
        aDrawPane = pDrawPane;
    }

    public void onTouchPressed(TouchEvent event){
        Rectangle dragRectangle = new Rectangle();
        dragRectangle.setFill(null);
        dragRectangle.setStroke(Color.BLACK);

        if(event.getSource() instanceof AbstractNodeView){
            dragRectangle.setX(event.getTouchPoint().getX() + ((AbstractNodeView) event.getSource()).getX());
            dragRectangle.setY(event.getTouchPoint().getY() + ((AbstractNodeView) event.getSource()).getY());
        } else {
            dragRectangle.setX(event.getTouchPoint().getX());
            dragRectangle.setY(event.getTouchPoint().getY());
        }
        dragRectangles.put(event.getTouchPoint().getId(), dragRectangle);
        aDrawPane.getChildren().add(dragRectangle);
    }

    public void onTouchDragged(TouchEvent event){
        Rectangle dragRectangle = dragRectangles.get(event.getTouchPoint().getId());
        if(event.getSource() instanceof AbstractNodeView){
            dragRectangle.setWidth(((AbstractNodeView)event.getSource()).getX()+ event.getTouchPoint().getX() - dragRectangle.getX());
            dragRectangle.setHeight(((AbstractNodeView)event.getSource()).getY() + event.getTouchPoint().getY() - dragRectangle.getY());
        } else {
            dragRectangle.setWidth(event.getTouchPoint().getX() - dragRectangle.getX());
            dragRectangle.setHeight(event.getTouchPoint().getY() - dragRectangle.getY());
        }
    }

    public ClassNode createClassNode(TouchEvent event) {
        Rectangle dragRectangle = dragRectangles.get(event.getTouchPoint().getId());
        ClassNode node = new ClassNode(dragRectangle.getX(), dragRectangle.getY(),
                event.getTouchPoint().getSceneX() - dragRectangle.getX(),
                event.getTouchPoint().getSceneY() - dragRectangle.getY());
        killDragRectangle(event);
        return node;
    }

    public PackageNode createPackageNode(TouchEvent event) {
        Rectangle dragRectangle = dragRectangles.get(event.getTouchPoint().getId());
        PackageNode node = new PackageNode(dragRectangle.getX(), dragRectangle.getY(),
                event.getTouchPoint().getSceneX() - dragRectangle.getX(),
                event.getTouchPoint().getSceneY() - dragRectangle.getY());
        killDragRectangle(event);
        return node;
    }

    public NodeView onTouchReleased(AbstractNode node, Double currentScale)
    {
        AbstractNodeView nodeView = null;
        if (node instanceof PackageNode){
            nodeView = createPackageFromDrag((PackageNode)node, currentScale);
        }
        else if (node instanceof ClassNode)
        {
            nodeView = createClassNodeFromDrag((ClassNode)node, currentScale);
        }
        return nodeView;
    }

    private void killDragRectangle(TouchEvent event){
        Rectangle dragRectangle = dragRectangles.get(event.getTouchPoint().getId());
        dragRectangle.setWidth(0);
        dragRectangle.setHeight(0);

        aDrawPane.getChildren().remove(dragRectangle);
    }

    //TODO Duplicate code
    private ClassNodeView createClassNodeFromDrag(ClassNode node, Double currentScale){
        ClassNodeView nodeView = new ClassNodeView(node);
        aDrawPane.getChildren().add(nodeView);
        putNodeInPackage(nodeView, node);
        aMainController.getGraphModel().addNode(node);

        return nodeView;
    }

    private PackageNodeView createPackageFromDrag(PackageNode node, Double currentScale){
        PackageNodeView nodeView = new PackageNodeView(node);
        aDrawPane.getChildren().add(nodeView);
        putNodeInPackage(nodeView, node);
        aMainController.getGraphModel().addNode(node);

        return nodeView;
    }

    /**
    * Puts the given class node as a child of a package if it is contained in one.
     */
    //TODO This is maybe not necessary, as the graph now adds the child to the package.
    private boolean putNodeInPackage(AbstractNodeView nodeView, AbstractNode potentialChildModel){
        Map<AbstractNodeView, AbstractNode> nodeMap = aMainController.getNodeMap();
        for(AbstractNodeView potentialParent : aMainController.getAllNodeViews()){
            if(potentialParent instanceof PackageNodeView)
            {
                if(potentialParent.getBoundsInParent().contains(nodeView.getBounds()))
                {
                    ((PackageNode)nodeMap.get(potentialParent)).addChild(potentialChildModel);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean currentlyCreating(){
        return !dragRectangles.isEmpty();
    }


    //--------------------- MOUSE EVENTS FOR DEVELOPING -----------------------------
    public void onMousePressed(MouseEvent event){
        mouseDragRectangle = new Rectangle();
        mouseDragRectangle.setFill(null);
        mouseDragRectangle.setStroke(Color.BLACK);
        mouseDragRectangle.setX(event.getSceneX());
        mouseDragRectangle.setY(event.getSceneY());
        aDrawPane.getChildren().add(mouseDragRectangle);
    }

    public void onMouseDragged(MouseEvent event){
        mouseDragRectangle.setWidth(event.getSceneX() - mouseDragRectangle.getX());
        mouseDragRectangle.setHeight(event.getSceneY() - mouseDragRectangle.getY());
    }

    public NodeView onMouseReleased(MouseEvent event, AbstractNode node, Double currentScale)
    {
        mouseDragRectangle.setWidth(0);
        mouseDragRectangle.setHeight(0);

        aDrawPane.getChildren().remove(mouseDragRectangle);
        AbstractNodeView nodeView = null;
        if (node instanceof PackageNode){
            nodeView = createPackageFromDrag((PackageNode)node, currentScale);
        }
        else if (node instanceof ClassNode)
        {
            nodeView = createClassNodeFromDrag((ClassNode)node, currentScale);
        }
        return nodeView;
    }

    public ClassNode createClassNodeMouse(MouseEvent event) {
        return new ClassNode(mouseDragRectangle.getX(), mouseDragRectangle.getY(),
                event.getSceneX() - mouseDragRectangle.getX(),
                event.getSceneY() - mouseDragRectangle.getY());
    }

    public PackageNode createPackageNodeMouse(MouseEvent event) {
        return new PackageNode(mouseDragRectangle.getX(), mouseDragRectangle.getY(),
                event.getSceneX() - mouseDragRectangle.getX(),
                event.getSceneY() - mouseDragRectangle.getY());
    }

}
