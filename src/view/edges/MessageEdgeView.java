package view.edges;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import model.edges.AbstractEdge;
import model.edges.MessageEdge;
import util.Constants;
import view.nodes.AbstractNodeView;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;

/**
 * The Graphical Representation of a AssociationEdge.
 */
public class MessageEdgeView extends AbstractEdgeView {
    private AbstractNodeView startNode;
    private AbstractNodeView endNode;
    private ArrayList<Line> arrowHeadLines = new ArrayList<>();
    private Double startX;
    private Double startY;
    private Circle circleHandle;
    private Group arrowHead;
    private Text title = new Text();



    //TODO these constructors are a mess.
    public MessageEdgeView(MessageEdge edge,  Double pStartX, Double pStartY, AbstractNodeView startNode, AbstractNodeView endNode) {
        super(edge, startNode, endNode);
        arrowHead = new Group();
        this.getChildren().add(arrowHead);
        startX = pStartX;
        startY = pStartY;
        this.startNode = startNode;
        this.endNode = endNode;
        this.setStrokeWidth(super.STROKE_WIDTH);
        this.setStroke(Color.BLACK);
        setPosition();
        draw();
        this.getChildren().add(title);
    }

    public MessageEdgeView(MessageEdge edge, Double pStartX, Double pStartY, AbstractNodeView endNode) {
        super(edge, null, endNode);
        arrowHead = new Group();
        this.getChildren().add(arrowHead);
        startX = pStartX;
        startY = pStartY;
        this.startNode = null;
        this.endNode = endNode;
        this.setStrokeWidth(super.STROKE_WIDTH);
        this.setStroke(Color.BLACK);
        setPosition();
        drawCircleHandle();
        draw();
        this.getChildren().add(title);
    }


    @Override
    protected void draw() {
        AbstractEdge.Direction direction = refEdge.getDirection();
        //Draw arrows.
        switch(direction) {
            case NO_DIRECTION:
                break;
            case START_TO_END:
                drawArrowHead(startLine.getEndX(), startLine.getEndY(), startLine.getStartX(), startLine.getStartY());
                break;
            case END_TO_START:
                drawArrowHead(startLine.getStartX(), startLine.getStartY(), startLine.getEndX(), startLine.getEndY());
                break;
        }
    }

    private void drawTitle(String titleStr){
        title.setText(titleStr);
        title.setX((startLine.getStartX()  + endNode.getX())/2);
        title.setY(startLine.getStartY() - 5);
        title.toFront();
    }

    @Override
    protected void setPosition() {
        if(startNode != null){
            setPosition2();//WithStartNode();
        } else {
            setPositionNoStartNode();
        }
        //setPositionNoStartNode();
        drawTitle(title.getText());
    }

    private void setPosition2(){
        //If end node is to the right of startPos:
        if (startX <= endNode.getTranslateX()) {
            startLine.setStartX(startNode.getTranslateX() + (startNode.getWidth()/2));
            startLine.setStartY(startY);
            startLine.setEndX(endNode.getTranslateX() + (endNode.getWidth()/2));
            startLine.setEndY(startY);

            position = Position.RIGHT;
        }
        //If end node is to the left of startPos:
        else if (startX > endNode.getTranslateX() + endNode.getWidth()) {
            startLine.setStartX(startNode.getTranslateX() + (startNode.getWidth()/2));
            startLine.setStartY(startY);
            startLine.setEndX(endNode.getTranslateX() + (endNode.getWidth()/2));
            startLine.setEndY(startY);

            position = Position.LEFT;
        }
        //TODO Handle when the nodes are overlapping.
    }

    private void setPositionWithStartNode(){
        //If end node is to the right of startNode:
        AbstractNodeView lowestNodeView;
        if(startNode.getTranslateY() <= endNode.getTranslateY()){
            lowestNodeView = endNode;
        } else {
            lowestNodeView = startNode;
        }

        if (startNode.getTranslateX() + startNode.getWidth() <= endNode.getTranslateX()) {
            startLine.setStartX(startNode.getTranslateX() + (startNode.getWidth()/2));
            startLine.setStartY(lowestNodeView.getTranslateY() + lowestNodeView.getHeight() +  20);
            startLine.setEndX(endNode.getTranslateX() + (endNode.getWidth()/2));
            startLine.setEndY(lowestNodeView.getTranslateY() + lowestNodeView.getHeight() + 20);

            position = Position.RIGHT;
        }
        //If end node is to the left of startNode:
        else if (startNode.getTranslateX() > endNode.getTranslateX() + endNode.getWidth()) {
            startLine.setStartX(startNode.getTranslateX() + (startNode.getWidth()/2));
            startLine.setStartY(lowestNodeView.getTranslateY() + lowestNodeView.getHeight() + 20);
            startLine.setEndX(endNode.getTranslateX() + (endNode.getWidth()/2));
            startLine.setEndY(lowestNodeView.getTranslateY() + lowestNodeView.getHeight() + 20);

            position = Position.LEFT;
        }
    }

    private void setPositionNoStartNode(){
        //If end node is to the right of startPos:
        if (startX <= endNode.getTranslateX()) {
            startLine.setStartX(startX);
            startLine.setStartY(startY);
            startLine.setEndX(endNode.getTranslateX() + (endNode.getWidth()/2));
            startLine.setEndY(startY);

            position = Position.RIGHT;
        }
        //If end node is to the left of startPos:
        else if (startX > endNode.getTranslateX() + endNode.getWidth()) {
            startLine.setStartX(startX);
            startLine.setStartY(startY);
            startLine.setEndX(endNode.getTranslateX() + (endNode.getWidth()/2));
            startLine.setEndY(startY);

            position = Position.LEFT;
        }
        //TODO Handle when the nodes are overlapping.
    }

    private void drawCircleHandle(){
        circleHandle = new Circle(10);
        circleHandle.centerXProperty().bind(startLine.startXProperty());
        circleHandle.centerYProperty().bind(startLine.startYProperty());
        this.getChildren().add(circleHandle);
    }

    public void setSelected(boolean selected){
        super.setSelected(selected);
        Color color;
        if(selected){
            color = Constants.selected_color;
        } else {
            color = Color.BLACK;
        }

        for(Line l : arrowHeadLines){
            l.setStroke(color);
        }
        title.setFill(color);
        if(circleHandle != null){
            circleHandle.setFill(color);
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
    private void drawArrowHead(double startX, double startY, double endX, double endY) {
        arrowHead.getChildren().clear();
        double phi = Math.toRadians(30);
        int barb = 15;
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
            arrowHead.getChildren().add(arrowHeadLine);
            rho = theta - phi;
        }
    }

    public void propertyChange(PropertyChangeEvent evt){
        super.propertyChange(evt);
        String propertyName = evt.getPropertyName();
        if(propertyName.equals(Constants.changeNodeTranslateX) || propertyName.equals(Constants.changeNodeTranslateY) ||
                propertyName.equals(Constants.changeEdgeDirection)) {
            draw();
        } else if (propertyName.equals(Constants.changeMessageStartX) ){
            startX = (Double)evt.getNewValue();
            setPositionNoStartNode();
            drawTitle(title.getText());
            draw();
        } else if (propertyName.equals(Constants.changeMessageStartY)){
            startY = (Double)evt.getNewValue();
            if(startNode != null){
                setPosition2();
            } else {
                setPositionNoStartNode();
            }
            drawTitle(title.getText());
            draw();
        } else if (propertyName.equals(Constants.changeMessageTitle)){
            drawTitle((String)evt.getNewValue());
        } else if (propertyName.equals(Constants.changeMessageType)){
            if(evt.getNewValue() == MessageEdge.MessageType.RESPONSE){
                startLine.getStrokeDashArray().addAll(15d, 10d);
            } else {
                startLine.getStrokeDashArray().clear();
            }
        } else if (evt.getPropertyName().equals(Constants.changeNodeTranslateX) || evt.getPropertyName().equals(Constants.changeNodeTranslateY)){
        }
    }

    public Circle getCircleHandle(){
        return circleHandle;
    }
}
