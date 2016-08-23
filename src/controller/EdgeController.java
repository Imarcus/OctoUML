package controller;

import controller.dialog.EdgeEditDialogController;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.control.ChoiceBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import model.*;
import util.commands.DirectionChangeEdgeCommand;
import util.commands.ReplaceEdgeCommand;
import view.*;

import java.io.IOException;

/**
 * Controller class for Edges.
 */
public class EdgeController {
    private double dragStartX, dragStartY;
    private Line dragLine;
    private Pane aDrawPane;
    private MainController mainController;
    private AbstractNodeView startNodeView;
    private AbstractNodeView endNodeView;

    public EdgeController(Pane pDrawPane, MainController mainController) {
        aDrawPane = pDrawPane;
        dragLine = new Line();
        dragLine.setStroke(Color.DARKGRAY);
        dragLine.setStrokeWidth(2);
        this.mainController = mainController;
    }

    public void onMousePressed(MouseEvent event) {
        dragStartX = event.getX() + ((AbstractNodeView) event.getSource()).getTranslateX();
        dragStartY = event.getY() + ((AbstractNodeView) event.getSource()).getTranslateY();
        startNodeView = (AbstractNodeView) event.getSource();
        aDrawPane.getChildren().add(dragLine);
    }

    public void onMouseDragged(MouseEvent event){
        dragLine.setStartX(dragStartX);
        dragLine.setStartY(dragStartY);
        
        if(event.getSource() instanceof AbstractNodeView) {
            dragLine.setEndX(event.getX() + ((AbstractNodeView) event.getSource()).getTranslateX());
            dragLine.setEndY(event.getY() + ((AbstractNodeView) event.getSource()).getTranslateY());
        } else {
            dragLine.setEndX(event.getX());
            dragLine.setEndY(event.getY());
        }
    }

    public void onMouseReleased() {
        for(AbstractNodeView nodeView : mainController.getAllNodeViews()){
            if(nodeView.contains(getEndPoint())){
                endNodeView = nodeView;
            }
        }
        if(endNodeView != null){
            AssociationEdge edge = new AssociationEdge(mainController.getNodeMap().get(startNodeView), mainController.getNodeMap().get(endNodeView));
            mainController.createEdgeView(edge, startNodeView, endNodeView);
        }
        finish();
    }

    public void finish() {
        dragLine.setStartX(0);
        dragLine.setStartY(0);
        dragLine.setEndX(0);
        dragLine.setEndY(0);
        aDrawPane.getChildren().remove(dragLine);
        startNodeView = null;
        endNodeView = null;
    }


    public Point2D getStartPoint() {
        return new Point2D(dragStartX, dragStartY);
    }

    public Point2D getEndPoint() {
        return new Point2D(dragLine.getEndX(), dragLine.getEndY());
    }

    public boolean showEdgeEditDialog(AbstractEdge edge) {
        try {
            // Load the view.fxml file and create a new stage for the popup
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("view/fxml/edgeEditDialog.fxml"));
            AnchorPane dialog = (AnchorPane) loader.load();
            dialog.setBackground(new Background(new BackgroundFill(Color.WHITESMOKE, new CornerRadii(1), null)));
            dialog.setStyle("-fx-border-color: black");
            //Set location for "dialog".
            dialog.setLayoutX((edge.getStartNode().getTranslateX() + edge.getEndNode().getTranslateX())/2);
            dialog.setLayoutY((edge.getStartNode().getTranslateY() + edge.getEndNode().getTranslateY())/2);

            EdgeEditDialogController controller = loader.getController();
            controller.setEdge(edge);
            ChoiceBox directionBox = controller.getDirectionBox();
            ChoiceBox typeBox = controller.getTypeBox();
            controller.getOkButton().setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    //If no change in type of edge we just change direction of old edge
                    if(typeBox.getValue().equals(edge.getType()) || typeBox.getValue() == null){

                        edge.setStartMultiplicity(controller.getStartMultiplicity());
                        edge.setEndMultiplicity(controller.getEndMultiplicity());
                        if (directionBox.getValue() != null) {
                            mainController.getUndoManager().add(new DirectionChangeEdgeCommand(edge, edge.getDirection(),
                                    AbstractEdge.Direction.valueOf(directionBox.getValue().toString())));
                            edge.setDirection(AbstractEdge.Direction.valueOf(directionBox.getValue().toString()));
                        }


                    } else { //Else we create a new one to replace the old
                        AbstractEdge newEdge = null;
                        if (typeBox.getValue().equals("Inheritance")) {
                            newEdge = new InheritanceEdge(edge.getStartNode(), edge.getEndNode());
                        } else if (typeBox.getValue().equals("Association") ) {
                            newEdge = new AssociationEdge(edge.getStartNode(), edge.getEndNode());
                        } else if (typeBox.getValue().equals("Aggregation")) {
                            newEdge = new AggregationEdge(edge.getStartNode(), edge.getEndNode());
                        } else if (typeBox.getValue().equals("Composition")) {
                            newEdge = new CompositionEdge(edge.getStartNode(), edge.getEndNode());
                        }
                        newEdge.setDirection(AbstractEdge.Direction.valueOf(directionBox.getValue().toString()));
                        newEdge.setStartMultiplicity(controller.getStartMultiplicity());
                        newEdge.setEndMultiplicity(controller.getEndMultiplicity());
                        replaceEdge(edge, newEdge);
                    }



                    aDrawPane.getChildren().remove(dialog);
                    mainController.removeDialog(dialog);
                }
            });
            controller.getCancelButton().setOnAction(event -> {
                aDrawPane.getChildren().remove(dialog);
                mainController.removeDialog(dialog);
            });
            mainController.addDialog(dialog);
            aDrawPane.getChildren().add(dialog);

            return controller.isOkClicked();

        } catch (IOException e) {
            // Exception gets thrown if the view.fxml file could not be loaded
            e.printStackTrace();
            return false;
        }
    }
    public boolean replaceEdge(AbstractEdge oldEdge, AbstractEdge newEdge) {
        AbstractEdgeView oldEdgeView = null;
        for (AbstractEdgeView edgeView : mainController.getAllEdgeViews()) {
            if (edgeView.getRefEdge().equals(oldEdge)) {
                oldEdgeView = edgeView;
                break;
            }
        }
        if (oldEdgeView == null) {
            return false;
        }
        mainController.deleteEdgeView(oldEdgeView, null, true, false);

        //newEdge.setDirection(oldEdge.getDirection());
        //newEdge.setStartMultiplicity(oldEdge.getStartMultiplicity());
        //newEdge.setEndMultiplicity(oldEdge.getEndMultiplicity());
        //mainController.getGraphModel().addEdge(newEdge);

        AbstractEdgeView newEdgeView = mainController.createEdgeView(newEdge, oldEdgeView.getStartNode(), oldEdgeView.getEndNode());

        mainController.getUndoManager().add(
                new ReplaceEdgeCommand(oldEdge, newEdge, oldEdgeView, newEdgeView, mainController, mainController.getGraphModel())
        );

        System.out.println("Replaced Edge: Old edge:" + oldEdge.toString() + " new edge: "+ newEdge.toString());
        return true;
    }
}
