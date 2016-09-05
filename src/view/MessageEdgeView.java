package view;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import model.AbstractEdge;
import util.Constants;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;

/**
 * The Graphical Representation of a AssociationEdge.
 */
public class MessageEdgeView extends AbstractEdgeView {
    private AbstractEdge refEdge;
    private AbstractNodeView startNode;
    private AbstractNodeView endNode;
    private ArrayList<Line> arrowHeadLines = new ArrayList<>();
    private Double startX;
    private Double startY;
    private Double deltaY; //Distance from end node Lifeline-box
    private Circle circleHandle;



    public MessageEdgeView(AbstractEdge edge, AbstractNodeView startNode, AbstractNodeView endNode) {
        super(edge, startNode, endNode);
        this.refEdge = edge;
        this.startNode = startNode;
        this.endNode = endNode;
        this.setStrokeWidth(super.STROKE_WIDTH);
        this.setStroke(Color.BLACK);
        setPosition();
        draw();
    }

    public MessageEdgeView(AbstractEdge edge, Double pStartX, Double pStartY, AbstractNodeView endNode) {
        super(edge, null, endNode);
        startX = pStartX;
        startY = pStartY;
        this.refEdge = edge;
        this.startNode = null;
        this.endNode = endNode;
        this.setStrokeWidth(super.STROKE_WIDTH);
        this.setStroke(Color.BLACK);
        deltaY = startY - endNode.getTranslateY();
        setPosition();
        draw();
        drawCircleHandle();
    }

    @Override
    protected void draw() {
        if(startNode != null){
            drawWithStartNode();
        }
    }

    protected void drawWithStartNode() {
        AbstractEdge.Direction direction = refEdge.getDirection();
        getChildren().clear();
        getChildren().add(getStartLine());
        super.draw();
        this.getChildren().add(super.getEndMultiplicity());
        this.getChildren().add(super.getStartMultiplicity());

        //Draw arrows.
        switch(direction) {
            case NO_DIRECTION:
                //Do nothing.
                break;
            case START_TO_END:
                this.getChildren().add(drawArrowHead(getStartLine().getEndX(), getStartLine().getEndY(), getStartLine().getStartX(), getStartLine().getStartY()));
                break;
            case END_TO_START:
                this.getChildren().add(drawArrowHead(getStartLine().getStartX(), getStartLine().getStartY(), getStartLine().getEndX(), getStartLine().getEndY()));
                break;
            case BIDIRECTIONAL:
                this.getChildren().add(drawArrowHead(getStartLine().getStartX(), getStartLine().getStartY(), getStartLine().getEndX(), getStartLine().getEndY()));
                this.getChildren().add(drawArrowHead(getStartLine().getEndX(), getStartLine().getEndY(), getStartLine().getStartX(), getStartLine().getStartY()));
                break;
        }
    }

    @Override
    protected void setPosition() {
        if(startNode != null){
            setPositionWithStartNode();
        } else {
            setPositionNoStartNode();
        }
    }

    private void setPositionWithStartNode(){
        //If end node is to the right of startNode:
        if (startNode.getTranslateX() + startNode.getWidth() <= endNode.getTranslateX()) {
            startLine.setStartX(startNode.getTranslateX() + startNode.getWidth());
            startLine.setStartY(startNode.getTranslateY() + (startNode.getHeight() / 2));
            startLine.setEndX(endNode.getTranslateX());
            startLine.setEndY(endNode.getTranslateY() + (endNode.getHeight() / 2));

            position = Position.RIGHT;
        }
        //If end node is to the left of startNode:
        else if (startNode.getTranslateX() > endNode.getTranslateX() + endNode.getWidth()) {
            startLine.setStartX(startNode.getTranslateX());
            startLine.setStartY(startNode.getTranslateY() + (startNode.getHeight() / 2));
            startLine.setEndX(endNode.getTranslateX() + endNode.getWidth());
            startLine.setEndY(endNode.getTranslateY() + (endNode.getHeight() / 2));

            position = Position.LEFT;
        }
    }

    private void setPositionNoStartNode(){
        //If end node is to the right of startNode:
        if (startX <= endNode.getTranslateX()) {
            startLine.setStartX(startX);
            startLine.setStartY(endNode.getTranslateY() + deltaY);
            startLine.setEndX(endNode.getTranslateX() + (endNode.getWidth()/2));
            startLine.setEndY(endNode.getTranslateY() + deltaY);

            position = Position.RIGHT;
        }
        //If end node is to the left of startNode:
        else if (startX > endNode.getTranslateX() + endNode.getWidth()) {
            startLine.setStartX(startX);
            startLine.setStartY(endNode.getTranslateY() + deltaY);
            startLine.setEndX(endNode.getTranslateX() + (endNode.getWidth()/2));
            startLine.setEndY(endNode.getTranslateY() + deltaY);

            position = Position.LEFT;
        }
        //TODO Handle when the nodes are overlapping.
    }

    private void circleHandleDragged(){
        deltaY = startY - endNode.getTranslateY();
        setPositionNoStartNode();
    }

    private void drawCircleHandle(){
        circleHandle = new Circle(10);
        circleHandle.centerXProperty().bind(startLine.startXProperty());
        circleHandle.centerYProperty().bind(startLine.startYProperty());
        this.getChildren().add(circleHandle);
    }

    public void setSelected(boolean selected){
        super.setSelected(selected);
        if(selected){
            for(Line l : arrowHeadLines){
                l.setStroke(Constants.selected_color);
            }
            circleHandle.setFill(Constants.selected_color);
        } else {
            for (Line l : arrowHeadLines) {
                l.setStroke(Color.BLACK);
            }
            circleHandle.setFill(Color.BLACK);
        }
    }

    /**
     * Draws an ArrowHead and returns it in a group.
     * Based on code from http://www.coderanch.com/t/340443/GUI/java/Draw-arrow-head-line
     * @param startX
     * @param startY
     * @param endX
     * @param endY
     * @return Group.
     */
    private Group drawArrowHead(double startX, double startY, double endX, double endY) {
        Group group = new Group();
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
            arrowHeadLines.add(arrowHeadLine);
            if(super.isSelected()){
                arrowHeadLine.setStroke(Constants.selected_color);
            }
            group.getChildren().add(arrowHeadLine);
            rho = theta - phi;
        }
        return group;
    }

    public void propertyChange(PropertyChangeEvent evt){
        super.propertyChange(evt);
        String propertyName = evt.getPropertyName();
        if(propertyName.equals(Constants.changeNodeTranslateX) || propertyName.equals(Constants.changeNodeTranslateY) ||
                propertyName.equals(Constants.changeEdgeDirection)) {
            draw();
        } else if (propertyName.equals(Constants.changeMessageStartX) ){
            startX = (Double)evt.getNewValue();
            circleHandleDragged();
        } else if (propertyName.equals(Constants.changeMessageStartY)){
            startY = (Double)evt.getNewValue();
            circleHandleDragged();
        }
    }

    public Circle getCircleHandle(){
        return circleHandle;
    }
}
