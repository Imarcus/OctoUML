package controller;

import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import model.Sketch;
import view.AbstractEdgeView;
import view.AbstractNodeView;
import controller.MainController.ToolEnum;
import controller.MainController.Mode;

/**
 * Created by chris on 2016-02-15.
 */
//TODO Refactor code from MainController here.
public class SelectController {

    //For drag-selecting nodes
    double selectStartX, selectStartY;
    Rectangle selectRectangle;

    private Pane aDrawPane;
    private MainController mainController;

    public SelectController(Pane pDrawPane, MainController mainController){
        aDrawPane = pDrawPane;
        this.mainController = mainController;

        //init selectRectangle
        selectRectangle = new Rectangle();
        selectRectangle.setFill(null);
        selectRectangle.setStroke(Color.BLACK);
        selectRectangle.getStrokeDashArray().addAll(4.0,5.0,4.0,5.0);
    }

    public void onMousePressed(MouseEvent event){
        if (mainController.getTool() == MainController.ToolEnum.EDGE)
        {
            for(AbstractEdgeView edgeView : mainController.allEdgeViews){
                if (distanceToLine(edgeView.getLine(), event.getX(), event.getY()) < 15){
                    mainController.selected = true;
                    mainController.selectedEdges.add(edgeView);
                    if(event.getClickCount() > 1){
                        mainController.edgeController.showEdgeEditDialog(edgeView.getRefEdge());
                        mainController.setTool(ToolEnum.SELECT);
                        mainController.setButtonClicked(mainController.selectBtn);
                    }
                }
            }

            mainController.setMode(Mode.CREATING);
            mainController.edgeController.onMousePressed(event);
        }
        else if (mainController.getTool() == ToolEnum.SELECT)
        {
            for(AbstractEdgeView edgeView : mainController.allEdgeViews){
                if (distanceToLine(edgeView.getLine(), event.getX(), event.getY()) < 15){
                    mainController.selected = true;
                    mainController.selectedEdges.add(edgeView);
                    if(event.getClickCount() > 1){
                        mainController.edgeController.showEdgeEditDialog(edgeView.getRefEdge());
                        mainController.setTool(ToolEnum.SELECT);
                        mainController.setButtonClicked(mainController.selectBtn);
                    }
                }
            }

            mainController.setMode(Mode.SELECTING);
            //TODO This should not be needed, should be in nodeView.initActions().
            for(AbstractNodeView nodeView : mainController.allNodeViews){
                if (nodeView.getBoundsInParent().contains(event.getX(), event.getY()))
                {
                    mainController.selectedNodes.add(nodeView);
                    mainController.selected = true;
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

    void onMouseDragged(MouseEvent event){
        selectRectangle.setX(Math.min(selectStartX, event.getX()));
        selectRectangle.setY(Math.min(selectStartY, event.getY()));
        selectRectangle.setWidth(Math.abs(selectStartX - event.getX()));
        selectRectangle.setHeight(Math.abs(selectStartY - event.getY()));


        selectRectangle.setHeight(Math.max(event.getY() - selectStartY, selectStartY - event.getY()));
        //drawSelected();
    }

    void onMouseReleased(MouseEvent event){
        for(AbstractNodeView nodeView : mainController.allNodeViews) {
            if (selectRectangle.getBoundsInParent().contains(nodeView.getBoundsInParent()))
            {
                mainController.selected = true;
                mainController.selectedNodes.add(nodeView);
            }
        }
        for (AbstractEdgeView edgeView: mainController.allEdgeViews) {
            if (selectRectangle.getBoundsInParent().intersects(edgeView.getBoundsInParent()))
            {
                mainController.selected = true;
                mainController.selectedEdges.add(edgeView);
            }
        }
        for (Sketch sketch : mainController.allSketches) {
            if (selectRectangle.getBoundsInParent().intersects(sketch.getPath().getBoundsInParent())) {
                mainController.selected = true;
                mainController.selectedSketches.add(sketch);
            }
        }
                    /* //TODO Selectable sketches
                    for (javafx.scene.Node p : allSketches)
                    {
                        if (selectRectangle.getBoundsInParent().contains(p.getBoundsInParent()))
                        {
                            selected = true;
                            selectedNodes.add(p);
                        }
                    }*/

        //If no nodes were contained, remove all selections
        if (!mainController.selected) {
            mainController.selectedNodes.clear();
            mainController.selectedEdges.clear();
            mainController.selectedSketches.clear();
            mainController.selectedPictures.clear();
        }

        mainController.drawSelected();
        selectRectangle.setWidth(0);
        selectRectangle.setHeight(0);
        aDrawPane.getChildren().remove(selectRectangle);
        mainController.selected = false;
        mainController.setMode(Mode.NO_MODE);
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
