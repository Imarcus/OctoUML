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
        drawDirectionality();
        setChangeListeners();

    }

    private void drawDirectionality() {
        AbstractEdge.Direction direction = refEdge.getDirection();
        switch(direction) {
            case NO_DIRECTION:
                break;
            case START_TO_END:
                drawArrowHead(getStartX(), getStartY(), getEndX(), getEndY());
                break;
            case END_TO_START:
                drawArrowHead(getEndX(), getEndY(), getStartX(), getStartY());
                break;
            case BIDIRECTIONAL:
                drawArrowHead(getStartX(), getStartY(), getEndX(), getEndY());
                drawArrowHead(getEndX(), getEndY(), getStartX(), getStartY());
                break;
        }
    }

    private void drawArrowHead(double startX, double startY, double endX, double endY) {
        //Based on code from http://www.coderanch.com/t/340443/GUI/java/Draw-arrow-head-line
        getChildren().clear();
        getChildren().add(getLine());
        double phi = Math.toRadians(40);
        int barb = 20;
        double dy = startY - endY;
        double dx = startX - endX;
        double theta = Math.atan2(dy, dx);
        double x, y, rho = theta + phi;

        for (int j = 0; j < 2; j++) {
            x = startX - barb * Math.cos(rho);
            y = startY - barb * Math.sin(rho);
            Line arrowHeadLine = new Line(startX, startY, x, y);
            arrowHeadLine.setStrokeWidth(super.STROKE_WIDTH);
            getChildren().add(arrowHeadLine);
            rho = theta - phi;
        }
    }

    private void setChangeListeners() {
        super.getLine().endXProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                drawDirectionality();
            }
        });

        super.getLine().endYProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                drawDirectionality();
            }
        });

        super.getLine().startXProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                drawDirectionality();
            }
        });

        super.getLine().startYProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                drawDirectionality();
            }
        });

        refEdge.getNavigableProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                drawDirectionality();
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
