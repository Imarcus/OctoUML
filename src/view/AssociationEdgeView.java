package view;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.text.Text;
import model.AbstractEdge;

/**
 * Created by chris on 2016-02-18.
 */
public class AssociationEdgeView extends AbstractEdgeView {
    private AbstractEdge refEdge;
    private AbstractNodeView startNode;
    private AbstractNodeView endNode;
    private Text startMultiplicity;
    private Text endMultiplicity;

    
    public AssociationEdgeView(AbstractEdge edge, AbstractNodeView startNode, AbstractNodeView endNode) {
        super(edge, startNode, endNode);
        this.refEdge = edge;
        this.startNode = startNode;
        this.endNode = endNode;
        this.setStrokeWidth(super.STROKE_WIDTH);
        this.setStroke(Color.BLACK);
        startMultiplicity = new Text(edge.getStartMultiplicity());
        endMultiplicity = new Text(edge.getEndMultiplicity());
        
        drawArrowHead();
        setChangeListeners();



    }

    private void drawArrowHead() {
        //Based on code from http://www.coderanch.com/t/340443/GUI/java/Draw-arrow-head-line
        getChildren().clear();
        getChildren().add(getLine());
        if (refEdge.isNavigable()) {
            double phi = Math.toRadians(40);
            int barb = 20;
            double dy = getStartY() - getEndY();
            double dx = getStartX() - getEndX();
            double theta = Math.atan2(dy, dx);
            //System.out.println("theta = " + Math.toDegrees(theta));
            double x, y, rho = theta + phi;

            for (int j = 0; j < 2; j++) {
                x = getStartX() - barb * Math.cos(rho);
                y = getStartY() - barb * Math.sin(rho);
                Line arrowHeadLine = new Line(getStartX(), getStartY(), x, y);
                arrowHeadLine.setStrokeWidth(super.STROKE_WIDTH);
                getChildren().add(arrowHeadLine);
                //g2.draw(new Line2D.Double(startNode.x, startNode.y, x, y));
                rho = theta - phi;
            }
        }
    }

    private void setChangeListeners() {
        super.getLine().endXProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                drawArrowHead();
            }
        });

        super.getLine().endYProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                drawArrowHead();
            }
        });

        super.getLine().startXProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                drawArrowHead();
            }
        });

        super.getLine().startYProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                drawArrowHead();
            }
        });

        refEdge.getNavigableProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                drawArrowHead();
            }
        });

        refEdge.startMultiplicityProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                startMultiplicity.setText(newValue);
            }
        });

        refEdge.endMultiplicityProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                endMultiplicity.setText(newValue);
            }
        });
    }
}
