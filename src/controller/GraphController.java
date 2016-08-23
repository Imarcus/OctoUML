package controller;

import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

/**
 * Used by MainController for zooming and panning the view.
 */
public class GraphController {

    //For touch-moving the pane
    private double initMoveX, initMoveY;

    private Pane aDrawPane;
    private MainController aMainController;
    private ScrollPane aScrollPane;

    //For calculating zooming pivot point
    private double drawPaneXOffset;
    private double drawPaneYOffset;

    public GraphController(Pane pDrawPane, MainController pMainController, ScrollPane pScrollPane)
    {
        aDrawPane = pDrawPane;
        aMainController = pMainController;
        aScrollPane = pScrollPane;
        drawPaneXOffset = 0;
        drawPaneYOffset = 0;

        // center the scroll contents.
        aScrollPane.setHvalue(aScrollPane.getHmin() + (aScrollPane.getHmax() - aScrollPane.getHmin()) / 2);
        aScrollPane.setVvalue(aScrollPane.getVmin() + (aScrollPane.getVmax() - aScrollPane.getVmin()) / 2);
        aScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        aScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

    }

    public void movePaneStart(MouseEvent event)
        {

        initMoveX = event.getSceneX();
        initMoveY = event.getSceneY();

    }

    public void movePane(MouseEvent event)
    {
        ScrollPane scrollPane = aMainController.getScrollPane();
        double xScroll =  (initMoveX - event.getSceneX())/8000; //8000 is the size of aDrawPane set in view.view.fxml
        double yScroll = (initMoveY - event.getSceneY())/8000;




        scrollPane.setHvalue(scrollPane.getHvalue() + xScroll);
        scrollPane.setVvalue(scrollPane.getVvalue() + yScroll);

        initMoveX = event.getSceneX();
        initMoveY = event.getSceneY();

    }

    public void movePaneFinished()
    {
        initMoveX = 0;
        initMoveY = 0;
    }

    public void zoomPane(double newZoom)
    {
        double scale = newZoom/100;
        aDrawPane.setScaleX(scale);
        aDrawPane.setScaleY(scale);
    }

    public void resetDrawPaneOffset(){
        drawPaneYOffset = 0;
        drawPaneYOffset = 0;
    }
}
