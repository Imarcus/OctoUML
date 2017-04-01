package controller;

import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import model.nodes.AbstractNode;
import model.nodes.ClassNode;
import model.nodes.SequenceObject;
import model.nodes.PackageNode;
import view.nodes.AbstractNodeView;
import view.nodes.PackageNodeView;

import java.util.HashMap;
import java.util.Map;

/**
 * Used by MainController for handling when a user creates a node using the class or package tool.
 */
//TODO should be moved to NodeController!
public class CreateNodeController {

    //For drag-creating rectangles
    private HashMap<Integer, Rectangle> dragRectangles = new HashMap<>();
    private HashMap<Integer, Point2D> dragStarts = new HashMap();
    private Rectangle mouseDragRectangle;
    private double mouseDragStartX;
    private double mouseDragStartY;

    private AbstractDiagramController diagramController;
    private Pane aDrawPane;



    public CreateNodeController(Pane pDrawPane, AbstractDiagramController pDiagramController){
        diagramController = pDiagramController;
        aDrawPane = pDrawPane;
    }

    public void onTouchPressed(TouchEvent event){
        Rectangle dragRectangle = new Rectangle();
        dragRectangle.setFill(null);
        dragRectangle.setStroke(Color.BLACK);

        if(event.getSource() instanceof AbstractNodeView){
            dragRectangle.setX(event.getTouchPoint().getX() + ((AbstractNodeView) event.getSource()).getX());
            dragRectangle.setY(event.getTouchPoint().getY() + ((AbstractNodeView) event.getSource()).getY());
            dragStarts.put(event.getTouchPoint().getId(), new Point2D(
                    event.getTouchPoint().getX() + ((AbstractNodeView) event.getSource()).getX()
                    , event.getTouchPoint().getY() + ((AbstractNodeView) event.getSource()).getY()));

        } else {
            dragRectangle.setX(event.getTouchPoint().getX());
            dragRectangle.setY(event.getTouchPoint().getY());
            dragStarts.put(event.getTouchPoint().getId(), new Point2D(event.getTouchPoint().getX(), event.getTouchPoint().getY()));
        }

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

    public void onTouchReleasedClass(TouchEvent event)
    {
        Rectangle dragRectangle = dragRectangles.get(event.getTouchPoint().getId());
        diagramController.createNodeView(new ClassNode(dragRectangle.getX(), dragRectangle.getY(),
                dragRectangle.getWidth(), dragRectangle.getHeight()), false);
        finish(dragRectangle);

    }

    public void onTouchReleasedPackage(TouchEvent event)
    {
        Rectangle dragRectangle = dragRectangles.get(event.getTouchPoint().getId());
        diagramController.createNodeView(new PackageNode(dragRectangle.getX(), dragRectangle.getY(),
                dragRectangle.getWidth(), dragRectangle.getHeight()), false);
        finish(dragRectangle);

    }
    
    public void onTouchReleasedLifeline(TouchEvent event){
    	 Rectangle dragRectangle = dragRectangles.get(event.getTouchPoint().getId());
         diagramController.createNodeView(new SequenceObject(dragRectangle.getX(), dragRectangle.getY(),
                 dragRectangle.getWidth(), dragRectangle.getHeight()), false);
         finish(dragRectangle);
    }

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

    public void onMouseReleasedClass(){
        diagramController.createNodeView(new ClassNode(mouseDragRectangle.getX(), mouseDragRectangle.getY(),
                mouseDragRectangle.getWidth(),
                mouseDragRectangle.getHeight()), false);
        finish(mouseDragRectangle);
    }

    public void onMouseReleasedPackage(){
        diagramController.createNodeView(new PackageNode(mouseDragRectangle.getX(), mouseDragRectangle.getY(),
                mouseDragRectangle.getWidth(),
                mouseDragRectangle.getHeight()), false);
        finish(mouseDragRectangle);
    }

    public void onMouseReleasedLifeline(){
        diagramController.createNodeView(new SequenceObject(mouseDragRectangle.getX(), mouseDragRectangle.getY(),
                mouseDragRectangle.getWidth(),
                mouseDragRectangle.getHeight()), false);
        finish(mouseDragRectangle);
    }
    // remove rectangle's background boarder
    private void finish(Rectangle rectanlge){
        aDrawPane.getChildren().remove(rectanlge);
        try{
        	rectanlge.setWidth(0);
        	rectanlge.setHeight(0);        	
        }catch (Exception e){
        	e.printStackTrace();
        }
    }

    /**
     * Puts the given class node as a child of a package if it is contained in one.
     */
    //TODO This is maybe not necessary, as the graph now adds the child to the package.
    private boolean putNodeInPackage(AbstractNodeView nodeView, AbstractNode potentialChildModel){
        Map<AbstractNodeView, AbstractNode> nodeMap = diagramController.getNodeMap();
        for(AbstractNodeView potentialParent : diagramController.getAllNodeViews()){
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

}
