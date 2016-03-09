package controller;

import controller.dialog.EdgeEditDialogController;
import controller.dialog.NodeEditDialogController;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.*;
import view.AbstractEdgeView;
import view.AbstractNodeView;
import view.AssociationEdgeView;
import view.EdgeView;

import java.io.IOException;

/**
 * Created by chris on 2016-02-15.
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
        dragLine.setStartX(0);
        dragLine.setStartY(0);
        dragLine.setEndX(0);
        dragLine.setEndY(0);
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

    public boolean showEdgeEditDialog(AssociationEdge edge) {
        try {
            // Load the fxml file and create a new stage for the popup
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("edgeEditDialog.fxml"));
            AnchorPane page = (AnchorPane) loader.load();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit Edge");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(mainController.getStage());
            Scene scene = new Scene(page);
            dialogStage.setScene(scene);

            EdgeEditDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setEdge(edge);

            dialogStage.showAndWait();

            return controller.isOkClicked();

        } catch (IOException e) {
            // Exception gets thrown if the fxml file could not be loaded
            e.printStackTrace();
            return false;
        }
    }
}
