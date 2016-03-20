package view;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import model.AbstractEdge;

/**
 * Created by Chris on 2016-03-16.
 */
public class AggregationEdgeView extends AbstractEdgeView {
    private AbstractEdge refEdge;

    public AggregationEdgeView(AbstractEdge edge, AbstractNodeView startNode, AbstractNodeView endNode) {
        super(edge, startNode, endNode);
        this.refEdge = edge;
        this.setStrokeWidth(super.STROKE_WIDTH);
        this.setStroke(Color.BLACK);
        setChangeListeners();
        draw();
    }

    @Override
    protected void draw() {
        AbstractEdge.Direction direction = refEdge.getDirection();
        getChildren().clear();
        getChildren().add(getLine());
        super.draw();
        this.getChildren().add(super.getEndMultiplicity());
        this.getChildren().add(super.getStartMultiplicity());

        //Draw arrows.
        switch(direction) {
            case NO_DIRECTION:
                //Do nothing.
                break;
            case START_TO_END:
                this.getChildren().add(drawDiamond(getStartX(), getStartY(), getEndX(), getEndY()));
                break;
            case END_TO_START:
                this.getChildren().add(drawDiamond(getEndX(), getEndY(), getStartX(), getStartY()));
                break;
            case BIDIRECTIONAL:
                this.getChildren().add(drawDiamond(getStartX(), getStartY(), getEndX(), getEndY()));
                this.getChildren().add(drawDiamond(getEndX(), getEndY(), getStartX(), getStartY()));
                break;
        }
    }

    private Group drawDiamond(double startX, double startY, double endX, double endY){
        Group group = new Group();
        double phi = Math.toRadians(40);
        int barb = 14;
        double dy = startY - endY;
        double dx = startX - endX;
        double theta = Math.atan2(dy, dx);
        double x, y, rho = theta + phi;

        double[] xs = new double[2];
        double[] ys = new double[2];
        double x4, y4;
        x4 = startX - 23*Math.cos(theta);
        y4 = startY - 23*Math.sin(theta);
        for (int j = 0; j < 2; j++) {
            x = startX - barb * Math.cos(rho);
            y = startY - barb * Math.sin(rho);
            xs[j] = x;
            ys[j] = y;
            rho = theta - phi;
        }

        Polygon background = new Polygon();
        background.getPoints().setAll(new Double[] {
                startX, startY,
                xs[0], ys[0],
                x4, y4,
                xs[1], ys[1]
        });
        background.setFill(Color.WHITE);
        background.toBack();
        Line line1 = new Line(startX, startY, xs[0], ys[0]);
        Line line2 = new Line(startX, startY, xs[1], ys[1]);
        Line line3 = new Line(xs[0], ys[0], x4, y4);
        Line line4 = new Line(xs[1], ys[1], x4, y4);
        line1.setStrokeWidth(super.STROKE_WIDTH);
        line2.setStrokeWidth(super.STROKE_WIDTH);
        line3.setStrokeWidth(super.STROKE_WIDTH);
        line4.setStrokeWidth(super.STROKE_WIDTH);
        group.getChildren().add(background);
        group.getChildren().add(line1);
        group.getChildren().add(line2);
        group.getChildren().add(line3);
        group.getChildren().add(line4);
        return group;
    }

    private void setChangeListeners() {
        super.getLine().endXProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                draw();
            }
        });

        super.getLine().endYProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                draw();
            }
        });

        super.getLine().startXProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                draw();
            }
        });

        super.getLine().startYProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                draw();
            }
        });

        refEdge.getDirectionProperty().addListener(new ChangeListener<AbstractEdge.Direction>() {
            @Override
            public void changed(ObservableValue<? extends AbstractEdge.Direction> observable,
                                AbstractEdge.Direction oldValue, AbstractEdge.Direction newValue) {
                draw();
            }
        });
    }
}
