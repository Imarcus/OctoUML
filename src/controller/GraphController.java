package controller;

import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import model.Sketch;
import util.Constants;

import java.util.ArrayList;

/**
 * Used by MainController for zooming and panning the view.
 */
public class GraphController {

    private double initMoveX, initMoveY;

    private Pane aDrawPane;
    private AbstractDiagramController diagramController;
    private ScrollPane aScrollPane;

    private ArrayList<Line> grid = new ArrayList<>();
    private boolean isGridVisible = true;


    GraphController(Pane pDrawPane, AbstractDiagramController pDiagramController, ScrollPane pScrollPane)
    {
        aDrawPane = pDrawPane;
        diagramController = pDiagramController;
        aScrollPane = pScrollPane;

        // center the scroll contents.
        aScrollPane.setHvalue(aScrollPane.getHmin() + (aScrollPane.getHmax() - aScrollPane.getHmin()) / 2);
        aScrollPane.setVvalue(aScrollPane.getVmin() + (aScrollPane.getVmax() - aScrollPane.getVmin()) / 2);
        aScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        aScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

    }

    void movePaneStart(MouseEvent event)
        {

        initMoveX = event.getSceneX();
        initMoveY = event.getSceneY();

    }

    void movePane(MouseEvent event)
    {
        ScrollPane scrollPane = diagramController.getScrollPane();
        double xScroll =  (initMoveX - event.getSceneX())/8000; //8000 is the size of aDrawPane set in view.classDiagramView.fxml
        double yScroll = (initMoveY - event.getSceneY())/8000;




        scrollPane.setHvalue(scrollPane.getHvalue() + xScroll);
        scrollPane.setVvalue(scrollPane.getVvalue() + yScroll);

        initMoveX = event.getSceneX();
        initMoveY = event.getSceneY();

    }

    void movePaneFinished()
    {
        initMoveX = 0;
        initMoveY = 0;
    }

    void zoomPane(double newZoom)
    {
        double scale = newZoom/100;
        aDrawPane.setScaleX(scale);
        aDrawPane.setScaleY(scale);
    }

    //------------------------------------ GRID -------------------------------

    void drawGrid() {
        grid.clear();
        for (int i = 0; i < 8000; i += Constants.GRID_DISTANCE) {
            Line line1 = new Line(i, 0, i, 8000);
            line1.setStroke(Color.LIGHTGRAY);
            Line line2 = new Line(0, i, 8000, i);
            line2.setStroke(Color.LIGHTGRAY);
            grid.add(line1);
            grid.add(line2);
            aDrawPane.getChildren().addAll(line1, line2);
        }
    }

    void gridToBack() {
        for (Line line : grid) {
            line.toBack();
        }
    }


    void setGridVisible(boolean visible) {
        for (Line line : grid) {
            line.setVisible(visible);
        }
        isGridVisible = visible;
    }

    boolean isGridVisible() {
        return isGridVisible;
    }

    public void sketchesToFront() {
        for (Sketch sketch : diagramController.getGraphModel().getAllSketches()) {
            sketch.getPath().toFront();
        }
    }
}
