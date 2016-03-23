package controller;

import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import model.AbstractNode;
import model.ClassNode;
import model.PackageNode;
import view.AbstractNodeView;
import view.ClassNodeView;
import view.NodeView;
import view.PackageNodeView;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by marcusisaksson on 2016-02-12.
 */
//TODO should be moved to NodeController!
public class CreateNodeController {

    //For drag-creating rectangles
    private HashMap<Integer, Rectangle> dragRectangles = new HashMap<>();
    private HashMap<Integer, Point2D> dragStarts = new HashMap();
    private Rectangle mouseDragRectangle;
    private double mouseDragStartX;
    private double mouseDragStartY;

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

        dragStarts.put(event.getTouchPoint().getId(), new Point2D(event.getTouchPoint().getX(), event.getTouchPoint().getY()));
        dragRectangles.put(event.getTouchPoint().getId(), dragRectangle);
        aDrawPane.getChildren().add(dragRectangle);
    }

    public void onTouchDragged(TouchEvent event){
        Rectangle dragRectangle = dragRectangles.get(event.getTouchPoint().getId());
        Point2D startPoint = dragStarts.get(event.getTouchPoint().getId());

        if(event.getSource() instanceof AbstractNodeView){
            dragRectangle.setWidth(Math.abs(((AbstractNodeView)event.getSource()).getX() + event.getTouchPoint().getX() - startPoint.getX()));
            dragRectangle.setHeight(Math.abs(((AbstractNodeView)event.getSource()).getY() + event.getTouchPoint().getY() - startPoint.getY()));
            dragRectangle.setX(Math.min(startPoint.getX(), ((AbstractNodeView)event.getSource()).getX() + event.getTouchPoint().getX()));
            dragRectangle.setY(Math.min(startPoint.getY(), ((AbstractNodeView)event.getSource()).getY() + event.getTouchPoint().getY()));


        } else {
            dragRectangle.setWidth(Math.abs(startPoint.getX() - event.getTouchPoint().getX()));
            dragRectangle.setHeight(Math.abs(startPoint.getY() - event.getTouchPoint().getY()));
            dragRectangle.setX(Math.min(startPoint.getX(), event.getTouchPoint().getX()));
            dragRectangle.setY(Math.min(startPoint.getY(), event.getTouchPoint().getY()));

        }
    }

    public NodeView onTouchReleased(AbstractNode node, Double currentScale)
    {
        AbstractNodeView nodeView = null;
        if (node instanceof PackageNode){
            nodeView = createPackageFromDrag((PackageNode)node, currentScale);
            nodeView.toBack();
            aMainController.gridToBack();
        }
        else if (node instanceof ClassNode)
        {
            nodeView = createClassNodeFromDrag((ClassNode)node, currentScale);
        }
        return nodeView;
    }

    public ClassNode createClassNode(TouchEvent event) {
        Rectangle dragRectangle = dragRectangles.get(event.getTouchPoint().getId());
        ClassNode node = new ClassNode(dragRectangle.getX(), dragRectangle.getY(),
                dragRectangle.getWidth(), dragRectangle.getHeight());
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

    private void killDragRectangle(TouchEvent event){
        Rectangle dragRectangle = dragRectangles.get(event.getTouchPoint().getId());
        dragRectangle.setWidth(0);
        dragRectangle.setHeight(0);
        aDrawPane.getChildren().remove(dragRectangle);
        dragStarts.remove(event.getTouchPoint().getId());
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


    //--------------------- MOUSE EVENTS FOR DEVELOPMENT ----------------------------- //TODO
    public void onMousePressed(MouseEvent event){
        mouseDragRectangle = new Rectangle();
        mouseDragRectangle.setFill(null);
        mouseDragRectangle.setStroke(Color.BLACK);
        aDrawPane.getChildren().add(mouseDragRectangle);
        mouseDragStartX = event.getX();
        mouseDragStartY = event.getY();


        if(event.getSource() instanceof AbstractNodeView){
            mouseDragRectangle.setX(((AbstractNodeView)event.getSource()).getX() + mouseDragStartX);
            mouseDragRectangle.setY(((AbstractNodeView)event.getSource()).getY() + mouseDragStartY);

        } else {
            mouseDragRectangle.setX(event.getX());
            mouseDragRectangle.setY(event.getY());
        }
    }

    public void onMouseDragged(MouseEvent event){
        if(event.getSource() instanceof AbstractNodeView){
            mouseDragRectangle.setWidth(Math.abs(((AbstractNodeView)event.getSource()).getX() + event.getX() - mouseDragStartX));
            mouseDragRectangle.setHeight(Math.abs(((AbstractNodeView)event.getSource()).getY() + event.getY() - mouseDragStartY));
            mouseDragRectangle.setX(Math.min(mouseDragStartX, ((AbstractNodeView)event.getSource()).getX() + event.getX()));
            mouseDragRectangle.setY(Math.min(mouseDragStartY, ((AbstractNodeView)event.getSource()).getY() + event.getY()));

        } else {
            mouseDragRectangle.setWidth(Math.abs(mouseDragStartX - event.getX()));
            mouseDragRectangle.setHeight(Math.abs(mouseDragStartY - event.getY()));
            mouseDragRectangle.setX(Math.min(mouseDragStartX, event.getX()));
            mouseDragRectangle.setY(Math.min(mouseDragStartY, event.getY()));
        }
    }

    public NodeView onMouseReleased(MouseEvent event, AbstractNode node, Double currentScale)
    {
        aDrawPane.getChildren().remove(mouseDragRectangle);
        AbstractNodeView nodeView = null;
        if (node instanceof PackageNode){
            nodeView = createPackageFromDrag((PackageNode)node, currentScale);
            nodeView.toBack();
            aMainController.gridToBack();
        }
        else if (node instanceof ClassNode)
        {
            nodeView = createClassNodeFromDrag((ClassNode)node, currentScale);
        }
        mouseDragRectangle.setWidth(0);
        mouseDragRectangle.setHeight(0);
        return nodeView;
    }

    public ClassNode createClassNodeMouse(MouseEvent event) {
        return new ClassNode(mouseDragRectangle.getX(), mouseDragRectangle.getY(),
                mouseDragRectangle.getWidth(),
                mouseDragRectangle.getHeight());
    }

    public PackageNode createPackageNodeMouse(MouseEvent event) {
        return new PackageNode(mouseDragRectangle.getX(), mouseDragRectangle.getY(),
                mouseDragRectangle.getWidth(),
                mouseDragRectangle.getHeight());
    }

}
