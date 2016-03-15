package controller;

import controller.dialog.EdgeEditDialogController;
import controller.dialog.NodeEditDialogController;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.*;
import view.*;
import weka.core.neighboursearch.balltrees.PointsClosestToFurthestChildren;

import java.io.IOException;

/**
 * Controller class for Edges.
 */
public class EdgeController {
    private double dragStartX, dragStartY;
    private Line dragLine;
    private Pane aDrawPane;
    private MainController mainController;

    public EdgeController(Pane pDrawPane, MainController mainController) {
        aDrawPane = pDrawPane;
        dragLine = new Line();
        dragLine.setStroke(Color.DARKGRAY);
        dragLine.setStrokeWidth(2);
        this.mainController = mainController;
    }

    public void onMousePressed(MouseEvent event) {
        aDrawPane.getChildren().remove(dragLine);
        if(event.getSource() instanceof AbstractNodeView) {
            dragStartX = event.getX() + ((AbstractNodeView) event.getSource()).getTranslateX();
            dragStartY = event.getY() + ((AbstractNodeView) event.getSource()).getTranslateY();
        } else {
            dragStartX = event.getX();
            dragStartY = event.getY();
        }

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
        aDrawPane.getChildren().remove(dragLine); //TODO why not use removeDragLine?
        dragLine.setStartX(0);
        dragLine.setStartY(0);
        dragLine.setEndX(0);
        dragLine.setEndY(0);
        aDrawPane.getChildren().add(edgeView);
        return edgeView;
    }

    public void removeDragLine() {
        dragLine.setStartX(0);
        dragLine.setStartY(0);
        dragLine.setEndX(0);
        dragLine.setEndY(0);
        aDrawPane.getChildren().remove(dragLine);
    }


    public Point2D getStartPoint() {
        return new Point2D(dragStartX, dragStartY);
    }

    public Point2D getEndPoint() {
        return new Point2D(dragLine.getEndX(), dragLine.getEndY());
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

    public boolean showEdgeEditDialog(AbstractEdge edge) {
        try {
            // Load the fxml file and create a new stage for the popup
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("edgeEditDialog.fxml"));
            AnchorPane dialog = (AnchorPane) loader.load();
            dialog.setBackground(new Background(new BackgroundFill(Color.WHITESMOKE, new CornerRadii(1), null)));
            dialog.setStyle("-fx-border-color: black");
            //Set location for "dialog".
            double maxX = aDrawPane.getWidth() - dialog.getPrefWidth();
            double maxY = aDrawPane.getHeight() - dialog.getPrefHeight();
            dialog.setLayoutX(Math.min(maxX,(edge.getStartNode().getTranslateX() + edge.getEndNode().getTranslateX())/2));
            dialog.setLayoutY(Math.min(maxY,(edge.getStartNode().getTranslateY() + edge.getEndNode().getTranslateY())/2));

            EdgeEditDialogController controller = loader.getController();
            controller.setEdge(edge);
            ChoiceBox box = controller.getDirectionBox();
            ChoiceBox type = controller.getTypeBox();
            controller.getOkButton().setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    if (box.getValue() != null) {
                        edge.setDirection(AbstractEdge.Direction.valueOf(box.getValue().toString()));
                    }
                    edge.setStartMultiplicity(controller.getStartMultiplicity());
                    edge.setEndMultiplicity(controller.getEndMultiplicity());
                    if (type.getValue() != null) {
                        if (type.getValue().equals("Inheritance") && !(edge instanceof InheritanceEdge)) {
                            InheritanceEdge newEdge = new InheritanceEdge(edge.getStartNode(), edge.getEndNode());
                            mainController.replaceEdge(edge, newEdge);
                        } else if (type.getValue().equals("Association") && !(edge instanceof AssociationEdge)) {
                            AssociationEdge newEdge = new AssociationEdge(edge.getStartNode(), edge.getEndNode());
                            mainController.replaceEdge(edge, newEdge);
                        }
                    }
                    aDrawPane.getChildren().remove(dialog);
                }
            });
            controller.getCancelButton().setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    aDrawPane.getChildren().remove(dialog);
                }
            });
            aDrawPane.getChildren().add(dialog);

            return controller.isOkClicked();

        } catch (IOException e) {
            // Exception gets thrown if the fxml file could not be loaded
            e.printStackTrace();
            return false;
        }
    }
}
