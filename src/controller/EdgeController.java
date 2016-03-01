package controller;

import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import model.AbstractEdge;
import model.AssociationEdge;
import model.Node;
import model.PackageNode;
import view.AbstractEdgeView;
import view.AbstractNodeView;
import view.AssociationEdgeView;
import view.EdgeView;

/**
 * Created by chris on 2016-02-15.
 */
public class EdgeController {
    private double dragStartX, dragStartY;
    private Line dragLine;
    private Pane aDrawPane;

    public EdgeController(Pane pDrawPane) {
        aDrawPane = pDrawPane;
        dragLine = new Line();
        dragLine.setStroke(Color.DARKGRAY);
        dragLine.setStrokeWidth(2);
    }

    public void onMousePressed(MouseEvent event) {
        aDrawPane.getChildren().remove(dragLine);
        dragStartX = event.getSceneX();
        dragStartY = event.getSceneY();
        aDrawPane.getChildren().add(dragLine);
    }

    public void onMouseDragged(MouseEvent event){
        dragLine.setStartX(dragStartX);
        dragLine.setStartY(dragStartY);
        dragLine.setEndX(event.getSceneX());
        dragLine.setEndY(event.getSceneY());
    }

    public Point2D getStartPoint() {
        return new Point2D(dragStartX, dragStartY);
    }

    public Point2D getEndPoint() {
        return new Point2D(dragLine.getEndX(), dragLine.getEndY());
    }

    public EdgeView onMouseReleased(AbstractEdge abstractEdge,
                                               AbstractNodeView startNode,
                                               AbstractNodeView endNode) {
        if (startNode == null || endNode == null) {
            return null;
        }
        AbstractEdgeView edgeView = null;
        if (abstractEdge instanceof AssociationEdge) {
            edgeView = createAssociationEdgeView(abstractEdge, startNode, endNode);
        }
        aDrawPane.getChildren().remove(dragLine);
        dragLine.setStartX(0);
        dragLine.setStartY(0);
        dragLine.setEndX(0);
        dragLine.setEndY(0);
        aDrawPane.getChildren().add(edgeView);
        return edgeView;
    }

    public void removeDragLine() {
        aDrawPane.getChildren().remove(dragLine);
    }
    //TODO Should have nullchecks?
    private AssociationEdgeView createAssociationEdgeView(AbstractEdge edge,
                                                          AbstractNodeView startNode,
                                                          AbstractNodeView endNode) {
        return new AssociationEdgeView(edge, startNode, endNode);
    }

    //TODO Not used?
    private AssociationEdge createAssociationEdge(Node startNode, Node endNode) {
        return new AssociationEdge(startNode, endNode);
    }
}
