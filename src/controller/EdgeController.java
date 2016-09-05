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
    private AbstractDiagramController diagramController;
    private AbstractNodeView startNodeView;
    private AbstractNodeView endNodeView;

    public EdgeController(Pane pDrawPane, AbstractDiagramController diagramController) {
        aDrawPane = pDrawPane;
        dragLine = new Line();
        dragLine.setStroke(Color.DARKGRAY);
        dragLine.setStrokeWidth(2);
        this.diagramController = diagramController;
    }

    public void onMousePressedOnNode(MouseEvent event) {
        dragStartX = event.getX() + ((AbstractNodeView) event.getSource()).getTranslateX();
        dragStartY = event.getY() + ((AbstractNodeView) event.getSource()).getTranslateY();
        startNodeView = (AbstractNodeView) event.getSource();
        aDrawPane.getChildren().add(dragLine);
    }

    public void onMousePressedOnCanvas(MouseEvent event){
        dragStartX = event.getX();
        dragStartY = event.getY();
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

    /*
    * Used for sequence diagrams
    */
    public void onMouseReleasedSequence(){
        for(AbstractNodeView nodeView : diagramController.getAllNodeViews()){ //TODO implement getAllLifelines
            if(nodeView instanceof LifelineView && ((LifelineView) nodeView).isOnLifeline(getEndPoint())){
                endNodeView = nodeView;
            }
        }
        if(endNodeView != null){
            MessageEdge edge = new MessageEdge(dragStartX, dragStartY, diagramController.getNodeMap().get(endNodeView));
            ((SequenceDiagramController)diagramController).createEdgeView(edge, null, endNodeView);
        } //TODO When there's a start nodeView
        finish();
    }

    /*
    * Used for class diagrams
    */
    public void onMouseReleasedRelation() {
        for(AbstractNodeView nodeView : diagramController.getAllNodeViews()){
            if(nodeView.contains(getEndPoint())){
                endNodeView = nodeView;
            }
        }
        if(endNodeView != null){
            AssociationEdge edge = new AssociationEdge(diagramController.getNodeMap().get(startNodeView), diagramController.getNodeMap().get(endNodeView));
            diagramController.createEdgeView(edge, startNodeView, endNodeView);
        }
        finish();
    }

    private void finish() {
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
            // Load the classDiagramView.fxml file and create a new stage for the popup
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("view/fxml/edgeEditDialog.fxml"));
            AnchorPane dialog = loader.load();
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
                            diagramController.getUndoManager().add(new DirectionChangeEdgeCommand(edge, edge.getDirection(),
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
                    diagramController.removeDialog(dialog);
                }
            });
            controller.getCancelButton().setOnAction(event -> {
                aDrawPane.getChildren().remove(dialog);
                diagramController.removeDialog(dialog);
            });
            diagramController.addDialog(dialog);
            aDrawPane.getChildren().add(dialog);

            return controller.isOkClicked();

        } catch (IOException e) {
            // Exception gets thrown if the classDiagramView.fxml file could not be loaded
            e.printStackTrace();
            return false;
        }
    }

    public boolean showMessageEditDialog(MessageEdge edge) {
        try {
            // Load the classDiagramView.fxml file and create a new stage for the popup
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("view/fxml/messageEditDialog.fxml"));
            AnchorPane dialog = loader.load();
            dialog.setBackground(new Background(new BackgroundFill(Color.WHITESMOKE, new CornerRadii(1), null)));
            dialog.setStyle("-fx-border-color: black");
            //Set location for "dialog".
            if(edge.getStartNode() != null) {
                dialog.setLayoutX((edge.getStartNode().getTranslateX() + edge.getEndNode().getTranslateX()) / 2);
                dialog.setLayoutY((edge.getStartNode().getTranslateY() + edge.getEndNode().getTranslateY()) / 2);
            } else {
                dialog.setLayoutX((edge.getStartX() + edge.getEndNode().getTranslateX()) / 2);
                dialog.setLayoutY((edge.getStartY() + edge.getEndNode().getTranslateY()) / 2);

            }

            MessageEditDialogController controller = loader.getController();
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
                            diagramController.getUndoManager().add(new DirectionChangeEdgeCommand(edge, edge.getDirection(),
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
                    diagramController.removeDialog(dialog);
                }
            });
            controller.getCancelButton().setOnAction(event -> {
                aDrawPane.getChildren().remove(dialog);
                diagramController.removeDialog(dialog);
            });
            diagramController.addDialog(dialog);
            aDrawPane.getChildren().add(dialog);

            return controller.isOkClicked();

        } catch (IOException e) {
            // Exception gets thrown if the classDiagramView.fxml file could not be loaded
            e.printStackTrace();
            return false;
        }
    }

    public boolean replaceEdge(AbstractEdge oldEdge, AbstractEdge newEdge) {
        AbstractEdgeView oldEdgeView = null;
        for (AbstractEdgeView edgeView : diagramController.getAllEdgeViews()) {
            if (edgeView.getRefEdge().equals(oldEdge)) {
                oldEdgeView = edgeView;
                break;
            }
        }
        if (oldEdgeView == null) {
            return false;
        }
        diagramController.deleteEdgeView(oldEdgeView, null, true, false);

        AbstractEdgeView newEdgeView = diagramController.createEdgeView(newEdge, oldEdgeView.getStartNode(), oldEdgeView.getEndNode());

        diagramController.getUndoManager().add(
                new ReplaceEdgeCommand(oldEdge, newEdge, oldEdgeView, newEdgeView, diagramController, diagramController.getGraphModel())
        );

        return true;
    }
}
