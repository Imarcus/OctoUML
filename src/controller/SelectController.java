package controller;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import model.edges.MessageEdge;
import model.Sketch;
import view.edges.AbstractEdgeView;
import view.nodes.AbstractNodeView;
import controller.AbstractDiagramController.ToolEnum;
import controller.AbstractDiagramController.Mode;
import view.edges.MessageEdgeView;

/**
 * Used by MainController for handling when a user tries to select elements in the graph.
 */
//TODO Refactor code from MainController here.
public class SelectController {

    //For drag-selecting nodes
    double selectStartX, selectStartY;
    Rectangle selectRectangle;

    private Pane aDrawPane;
    private AbstractDiagramController diagramController;

    public SelectController(Pane pDrawPane, AbstractDiagramController diagramController){
        aDrawPane = pDrawPane;
        this.diagramController = diagramController;

        //init selectRectangle
        selectRectangle = new Rectangle();
        selectRectangle.setFill(null);
        selectRectangle.setStroke(Color.BLACK);
        selectRectangle.getStrokeDashArray().addAll(4.0,5.0,4.0,5.0);
    }

    public void onMousePressed(MouseEvent event){
        if (diagramController.getTool() == AbstractDiagramController.ToolEnum.EDGE)
        {
            for(AbstractEdgeView edgeView : diagramController.allEdgeViews){
                if (!(edgeView instanceof MessageEdgeView) && (distanceToLine(edgeView.getStartLine(), event.getX(), event.getY()) < 15 ||
                        distanceToLine(edgeView.getMiddleLine(), event.getX(), event.getY()) < 15 ||
                        distanceToLine(edgeView.getStartLine(), event.getX(), event.getY()) < 15)){
                    diagramController.selectedEdges.add(edgeView);
                    if(event.getClickCount() > 1){
                        diagramController.edgeController.showEdgeEditDialog(edgeView.getRefEdge());
                        diagramController.setTool(ToolEnum.SELECT);
                        diagramController.setButtonClicked(diagramController.selectBtn);
                    }
                } else if((edgeView instanceof MessageEdgeView) && distanceToLine(edgeView.getStartLine(), event.getX(), event.getY()) < 15){
                    diagramController.selectedEdges.add(edgeView);
                    if(event.getClickCount() > 1){
                        diagramController.edgeController.showMessageEditDialog((MessageEdge)edgeView.getRefEdge());
                        diagramController.setTool(ToolEnum.SELECT);
                        diagramController.setButtonClicked(diagramController.selectBtn);
                    }
                }
            }
        }
        else if (diagramController.getTool() == ToolEnum.SELECT)
        {
            for(AbstractEdgeView edgeView : diagramController.allEdgeViews){
                if (!(edgeView instanceof MessageEdgeView) && (distanceToLine(edgeView.getStartLine(), event.getX(), event.getY()) < 15 ||
                        distanceToLine(edgeView.getMiddleLine(), event.getX(), event.getY()) < 15 ||
                        distanceToLine(edgeView.getStartLine(), event.getX(), event.getY()) < 15)){
                    if(!diagramController.selectedEdges.contains(edgeView)){
                        diagramController.selectedEdges.add(edgeView);
                    }
                    if(event.getClickCount() > 1){
                        diagramController.edgeController.showEdgeEditDialog(edgeView.getRefEdge());
                        diagramController.setTool(ToolEnum.SELECT);
                        diagramController.setButtonClicked(diagramController.selectBtn);
                    }
                } else if((edgeView instanceof MessageEdgeView) && distanceToLine(edgeView.getStartLine(), event.getX(), event.getY()) < 15){
                    if(!diagramController.selectedEdges.contains(edgeView)){
                        diagramController.selectedEdges.add(edgeView);
                    }
                    if(event.getClickCount() > 1){
                        diagramController.edgeController.showMessageEditDialog((MessageEdge)edgeView.getRefEdge());
                        diagramController.setTool(ToolEnum.SELECT);
                        diagramController.setButtonClicked(diagramController.selectBtn);
                    } else {
                        diagramController.setTool(ToolEnum.SELECT);
                        diagramController.setButtonClicked(diagramController.selectBtn);
                        diagramController.edgeController.onMousePressDragEdge(event);
                    }
                    diagramController.drawSelected();
                }
            }
            if(diagramController.mode != Mode.DRAGGING_EDGE){
                diagramController.setMode(Mode.SELECTING);
                //TODO This should not be needed, should be in nodeView.initActions().
                for(AbstractNodeView nodeView : diagramController.allNodeViews){
                    if (nodeView.getBoundsInParent().contains(event.getX(), event.getY()))
                    {
                        diagramController.selectedNodes.add(nodeView);
                    }
                }
                selectStartX = event.getX();
                selectStartY = event.getY();
                selectRectangle.setX(event.getX());
                selectRectangle.setY(event.getY());
                if (!aDrawPane.getChildren().contains(selectRectangle)) {
                    aDrawPane.getChildren().add(selectRectangle);
                }
            }
        }
    }

    void onMouseDragged(MouseEvent event){
        selectRectangle.setX(Math.min(selectStartX, event.getX()));
        selectRectangle.setY(Math.min(selectStartY, event.getY()));
        selectRectangle.setWidth(Math.abs(selectStartX - event.getX()));
        selectRectangle.setHeight(Math.abs(selectStartY - event.getY()));
        selectRectangle.setHeight(Math.max(event.getY() - selectStartY, selectStartY - event.getY()));
    }

    void onMouseReleased(){
        for(AbstractNodeView nodeView : diagramController.allNodeViews) {
            if (selectRectangle.getBoundsInParent().contains(nodeView.getBoundsInParent()))
            {
                diagramController.selected = true;
                diagramController.selectedNodes.add(nodeView);
            }
        }
        for (Sketch sketch : diagramController.getGraphModel().getAllSketches()) {
            if (selectRectangle.getBoundsInParent().intersects(sketch.getPath().getBoundsInParent())) {
                diagramController.selected = true;
                diagramController.selectedSketches.add(sketch);
            }
        }

        //If no nodes were contained, remove all selections
        if (!diagramController.selected) {
            diagramController.selectedNodes.clear();
            diagramController.selectedEdges.clear();
            diagramController.selectedSketches.clear();
        }

        diagramController.drawSelected();
        selectRectangle.setWidth(0);
        selectRectangle.setHeight(0);
        aDrawPane.getChildren().remove(selectRectangle);
        diagramController.selected = false;
        diagramController.setMode(Mode.NO_MODE);
    }

    //Code copied, sorry!
    //Used to determine if a click on canvas is in within range of an edge
    private double distanceToLine(Line l, double pointX, double pointY){
        double y1 = l.getStartY();
        double y2 = l.getEndY();
        double x1 = l.getStartX();
        double x2 = l.getEndX();

        double px = x2-x1;
        double py = y2-y1;

        double something = px*px + py*py;

        double u =  ((pointX - x1) * px + (pointY - y1) * py) / something;

        if (u > 1){
            u = 1;
        }
        else if (u < 0){
            u = 0;
        }

        double x = x1 + u * px;
        double y = y1 + u * py;

        double dx = x - pointX;
        double dy = y - pointY;

        double dist = Math.sqrt(dx*dx + dy*dy);

        return dist;

    }
}
