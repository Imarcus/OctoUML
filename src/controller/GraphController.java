package controller;

import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Line;
import model.GraphElement;
import javafx.scene.layout.Pane;
import view.PictureNodeView;

import java.util.HashMap;
import java.util.List;

/**
 * Created by marcusisaksson on 2016-02-12.
 */
public class GraphController {

    //For touch-moving the pane
    private double initMoveX, initMoveY;
    private HashMap<GraphElement, Double>  xInitTranslateMap = new HashMap<>();
    private HashMap<GraphElement, Double>  yInitTranslateMap = new HashMap<>();
    private HashMap<Line, Double> xInitTranslateMapGrid = new HashMap<>();
    private HashMap<Line, Double> yInitTranslateMapGrid = new HashMap<>();
    private HashMap<AnchorPane, Double> xDialogInitTranslateMap = new HashMap<>();
    private HashMap<AnchorPane, Double> yDialogInitTranslateMap = new HashMap<>();
    private HashMap<PictureNodeView, Double> xPictureInitTranslateMap = new HashMap<>();
    private HashMap<PictureNodeView, Double> yPictureInitTranslateMap = new HashMap<>();

    private double initPaneTranslateX;
    private double initPaneTranslateY;


    private Pane aDrawPane;
    private MainController aMainController;

    //For calculating zooming pivot point
    private double drawPaneXOffset;
    private double drawPaneYOffset;

    public GraphController(Pane pDrawPane, MainController pMainController)
    {
        aDrawPane = pDrawPane;
        aMainController = pMainController;
        drawPaneXOffset = 0;
        drawPaneYOffset = 0;

    }

    public void movePaneStart(List<GraphElement> elements, MouseEvent event)
        {

        initMoveX = event.getSceneX();
        initMoveY = event.getSceneY();

    }

    public void movePane(List<GraphElement> elements, MouseEvent event)
    {
        ScrollPane scrollPane = aMainController.getScrollPane();
        double xScroll =  (initMoveX - event.getSceneX())/8000; //8000 is the size of aDrawPane set in view.fxml
        double yScroll = (initMoveY - event.getSceneY())/8000;




        scrollPane.setHvalue(scrollPane.getHvalue() + xScroll);
        scrollPane.setVvalue(scrollPane.getVvalue() + yScroll);

        initMoveX = event.getSceneX();
        initMoveY = event.getSceneY();

    }

    public void movePaneFinished(MouseEvent event)
    {
        xInitTranslateMap.clear();
        yInitTranslateMap.clear();
        initPaneTranslateX = 0;
        initPaneTranslateY = 0;
        initMoveX = 0;
        initMoveY = 0;
    }
    public void zoomPaneStart()
    {
        //Not used
    }

    public void zoomPane(double oldZoom, double newZoom)
    {
        double scale = newZoom/100;
        aDrawPane.setScaleX(scale);
        aDrawPane.setScaleY(scale);
    }

    public void zoomPaneFinished()
    {
        //Not used
    }

    public void resetDrawPaneOffset(){
        drawPaneYOffset = 0;
        drawPaneYOffset = 0;
    }
}
