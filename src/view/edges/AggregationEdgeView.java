package view.edges;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import model.edges.AbstractEdge;
import util.Constants;
import view.nodes.AbstractNodeView;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Visual representation of AggregationEdge class.
 */
public class AggregationEdgeView extends AbstractEdgeView {
    private AbstractEdge refEdge;
    private ArrayList<Line> diamondLines = new ArrayList<>();

    public AggregationEdgeView(AbstractEdge edge, AbstractNodeView startNode, AbstractNodeView endNode) {
        super(edge, startNode, endNode);
        this.refEdge = edge;
        this.setStrokeWidth(super.STROKE_WIDTH);
        this.setStroke(Color.BLACK);
        setPosition();
        draw();
    }

    @Override
    protected void draw() {
        AbstractEdge.Direction direction = refEdge.getDirection();
        getChildren().clear();
        getChildren().add(getStartLine());

        getChildren().add(getMiddleLine());
        getChildren().add(getEndLine());
        super.draw();
        this.getChildren().add(super.getEndMultiplicity());
        this.getChildren().add(super.getStartMultiplicity());

        //Draw arrows.
        switch(direction) {
            case NO_DIRECTION:
                //Do nothing.
                break;
            case START_TO_END:
                this.getChildren().add(drawDiamond(getEndLine().getEndX(), getEndLine().getEndY(), getEndLine().getStartX(), getEndLine().getStartY()));
                break;
            case END_TO_START:
                this.getChildren().add(drawDiamond(getStartLine().getStartX(), getStartLine().getStartY(), getStartLine().getEndX(), getStartLine().getEndY()));
                break;
            case BIDIRECTIONAL:
                this.getChildren().add(drawDiamond(getStartLine().getStartX(), getStartLine().getStartY(), getStartLine().getEndX(), getStartLine().getEndY()));
                this.getChildren().add(drawDiamond(getEndLine().getEndX(), getEndLine().getEndY(), getEndLine().getStartX(), getEndLine().getStartY()));
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
        background.getPoints().setAll(startX, startY,
                xs[0], ys[0],
                x4, y4,
                xs[1], ys[1]);
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
        diamondLines.addAll(Arrays.asList(line1, line2, line3, line4));
        if(super.isSelected()){
            for(Line l : diamondLines){
                l.setStroke(Constants.selected_color);
            }
        }
        return group;
    }

    public void setSelected(boolean selected){
        super.setSelected(selected);
        if(selected){
            for(Line l : diamondLines){
                l.setStroke(Constants.selected_color);
            }
        } else {
            for (Line l : diamondLines) {
                l.setStroke(Color.BLACK);
            }
        }
    }

    public void propertyChange(PropertyChangeEvent evt){
        super.propertyChange(evt);
        if(evt.getPropertyName().equals(Constants.changeNodeTranslateX) || evt.getPropertyName().equals(Constants.changeNodeTranslateY) ||
                evt.getPropertyName().equals(Constants.changeEdgeDirection)) {
            draw();
        }
    }
}
