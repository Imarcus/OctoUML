package view.edges;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import model.edges.AbstractEdge;
import util.Constants;
import view.nodes.AbstractNodeView;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Visual representation of AbstractEdge class.
 */
public abstract class AbstractEdgeView extends Group implements EdgeView, PropertyChangeListener {
    private static int objectCounter = 0;

    protected AbstractEdge refEdge;
    protected AbstractNodeView startNode;
    protected AbstractNodeView endNode;
    protected boolean selected = false;
    public final double STROKE_WIDTH = 1;
    public enum Position{
        ABOVE, BELOW, RIGHT, LEFT, NONE
    }

    protected Position position = Position.NONE;
    private Text startMultiplicity;
    private Text endMultiplicity;

    protected Line startLine = new Line();
    protected Line middleLine = new Line();
    protected Line endLine = new Line();

    public AbstractEdgeView(AbstractEdge edge, AbstractNodeView startNode, AbstractNodeView endNode) {
        super();

        setId("VIEWASSOCIATION_" + ++objectCounter);

        this.refEdge = edge;
        this.startNode = startNode;
        this.endNode = endNode;
        this.setVisible(true);
        this.getChildren().add(startLine);
        this.getChildren().add(middleLine);
        this.getChildren().add(endLine);
        startMultiplicity = new Text(edge.getStartMultiplicity());
        endMultiplicity = new Text(edge.getEndMultiplicity());
        startLine.setStrokeWidth(STROKE_WIDTH);
        middleLine.setStrokeWidth(STROKE_WIDTH);
        endLine.setStrokeWidth(STROKE_WIDTH);

        refEdge.addPropertyChangeListener(this);
        if(startNode != null){
            startNode.getRefNode().addPropertyChangeListener(this);
        }
        endNode.getRefNode().addPropertyChangeListener(this);
    }

    protected void draw() {
        //Draw multiplicity
        Position position = getPosition();
        final double OFFSET = 20;

        switch (position) {
            case RIGHT:
                startMultiplicity.setX(getStartLine().getStartX() + OFFSET);
                startMultiplicity.setY(getStartLine().getStartY() + OFFSET);
                endMultiplicity.setX(getEndLine().getEndX() - OFFSET - endMultiplicity.getText().length() -5);
                endMultiplicity.setY(getEndLine().getEndY() + OFFSET);
                break;
            case LEFT:
                startMultiplicity.setX(getStartLine().getStartX() - OFFSET - endMultiplicity.getText().length() -5);
                startMultiplicity.setY(getStartLine().getStartY() + OFFSET);
                endMultiplicity.setX(getEndLine().getEndX() + OFFSET);
                endMultiplicity.setY(getEndLine().getEndY() + OFFSET);
                break;
            case ABOVE:
                startMultiplicity.setX(getStartLine().getStartX() + OFFSET);
                startMultiplicity.setY(getStartLine().getStartY() - OFFSET);
                endMultiplicity.setX(getEndLine().getEndX() + OFFSET);
                endMultiplicity.setY(getEndLine().getEndY() + OFFSET);
                break;
            case BELOW:
                startMultiplicity.setX(getStartLine().getStartX() + OFFSET);
                startMultiplicity.setY(getStartLine().getStartY() + OFFSET);
                endMultiplicity.setX(getEndLine().getEndX() + OFFSET);
                endMultiplicity.setY(getEndLine().getEndY() - OFFSET);
                break;
        }
        startMultiplicity.toFront();
        endMultiplicity.toFront();
        //TODO This doesn't seem to work?
        //getChildren().add(startMultiplicity);
        //getChildren().add(endMultiplicity);
    }

    public Text getStartMultiplicity() {
        return startMultiplicity;
    }

    public Text getEndMultiplicity() {
        return endMultiplicity;
    }

    public AbstractEdge getRefEdge() {
        return refEdge;
    }

    public void setStrokeWidth(double width) {
        startLine.setStrokeWidth(width);
    }

    public void setStroke(Paint value){
        startLine.setStroke(value);
    }

    public double getStartX() {
        return startLine.getStartX();
    }

    public double getStartY(){
        return startLine.getStartY();
    }

    public double getEndX(){
        return endLine.getEndX();
    }

    public double getEndY(){
        return endLine.getEndY();
    }

    public Line getStartLine() {
        return startLine;
    }

    public Line getMiddleLine() {
        return middleLine;
    }

    public Line getEndLine() {
        return endLine;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected){
        this.selected = selected;
        if (selected){
            startLine.setStroke(Constants.selected_color);
            middleLine.setStroke(Constants.selected_color);
            endLine.setStroke(Constants.selected_color);
        } else {
            startLine.setStroke(Color.BLACK);
            middleLine.setStroke(Color.BLACK);
            endLine.setStroke(Color.BLACK);
        }
    }

    public Position getPosition() {
        return position;
    }

    protected void setPosition() {
        //If end node is to the right of startNode:
        if (startNode.getTranslateX() + startNode.getWidth() <= endNode.getTranslateX()) { //Straight line if height difference is small
            if(Math.abs(startNode.getTranslateY() + (startNode.getHeight()/2) - (endNode.getTranslateY() + (endNode.getHeight()/2))) < 20){
                startLine.setStartX(startNode.getTranslateX() + startNode.getWidth());
                startLine.setStartY(startNode.getTranslateY() + (startNode.getHeight() / 2));
                startLine.setEndX(endNode.getTranslateX());
                startLine.setEndY(endNode.getTranslateY() + (endNode.getHeight() / 2));

                middleLine.setStartX(0);
                middleLine.setStartY(0);
                middleLine.setEndX(0);
                middleLine.setEndY(0);

                endLine.setStartX(0);
                endLine.setStartY(0);
                endLine.setEndX(0);
                endLine.setEndY(0);
            } else {
                startLine.setStartX(startNode.getTranslateX() + startNode.getWidth());
                startLine.setStartY(startNode.getTranslateY() + (startNode.getHeight() / 2));
                startLine.setEndX(startNode.getTranslateX() + startNode.getWidth() + ((endNode.getTranslateX() - (startNode.getTranslateX() + startNode.getWidth()))/2));
                startLine.setEndY(startNode.getTranslateY() + (startNode.getHeight() / 2));

                middleLine.setStartX(startNode.getTranslateX() + startNode.getWidth() + ((endNode.getTranslateX() - (startNode.getTranslateX() + startNode.getWidth()))/2));
                middleLine.setStartY(startNode.getTranslateY() + (startNode.getHeight() / 2));
                middleLine.setEndX(startNode.getTranslateX() + startNode.getWidth() + ((endNode.getTranslateX() - (startNode.getTranslateX() + startNode.getWidth()))/2));
                middleLine.setEndY(endNode.getTranslateY() + (endNode.getHeight() / 2));

                endLine.setStartX(startNode.getTranslateX() + startNode.getWidth() + ((endNode.getTranslateX() - (startNode.getTranslateX() + startNode.getWidth()))/2));
                endLine.setStartY(endNode.getTranslateY() + (endNode.getHeight() / 2));
                endLine.setEndX(endNode.getTranslateX());
                endLine.setEndY(endNode.getTranslateY() + (endNode.getHeight() / 2));
            }

            position = Position.RIGHT;
        }
        //If end node is to the left of startNode:
        else if (startNode.getTranslateX() > endNode.getTranslateX() + endNode.getWidth()) {
            if(Math.abs(startNode.getTranslateY() + (startNode.getHeight()/2) - (endNode.getTranslateY() + (endNode.getHeight()/2))) < 20){
                startLine.setStartX(startNode.getTranslateX());
                startLine.setStartY(startNode.getTranslateY() + (startNode.getHeight() / 2));
                startLine.setEndX(endNode.getTranslateX() + endNode.getWidth());
                startLine.setEndY(endNode.getTranslateY() + (endNode.getHeight() / 2));

                middleLine.setStartX(0);
                middleLine.setStartY(0);
                middleLine.setEndX(0);
                middleLine.setEndY(0);

                endLine.setStartX(0);
                endLine.setStartY(0);
                endLine.setEndX(0);
                endLine.setEndY(0);
            } else {
                startLine.setStartX(startNode.getTranslateX());
                startLine.setStartY(startNode.getTranslateY() + (startNode.getHeight() / 2));
                startLine.setEndX(endNode.getTranslateX() + endNode.getWidth()  + ((startNode.getTranslateX() - (endNode.getTranslateX() + endNode.getWidth()))/2));
                startLine.setEndY(startNode.getTranslateY() + (startNode.getHeight() / 2));

                middleLine.setStartX(endNode.getTranslateX() + endNode.getWidth()  + ((startNode.getTranslateX() - (endNode.getTranslateX() + endNode.getWidth()))/2));
                middleLine.setStartY(startNode.getTranslateY() + (startNode.getHeight() / 2));
                middleLine.setEndX(endNode.getTranslateX() + endNode.getWidth()  + ((startNode.getTranslateX() - (endNode.getTranslateX() + endNode.getWidth()))/2));
                middleLine.setEndY(endNode.getTranslateY() + (endNode.getHeight() / 2));

                endLine.setStartX(endNode.getTranslateX() + endNode.getWidth()  + ((startNode.getTranslateX() - (endNode.getTranslateX() + endNode.getWidth()))/2));
                endLine.setStartY(endNode.getTranslateY() + (endNode.getHeight() / 2));
                endLine.setEndX(endNode.getTranslateX() + endNode.getWidth());
                endLine.setEndY(endNode.getTranslateY() + (endNode.getHeight() / 2));
            }

            position = Position.LEFT;
        }
        // If end node is below startNode:
        else if (startNode.getTranslateY() + startNode.getHeight() < endNode.getTranslateY()){
            if(Math.abs(startNode.getTranslateX() + (startNode.getWidth()/2) - (endNode.getTranslateX() + (endNode.getWidth()/2))) < 20){
                startLine.setStartX(startNode.getTranslateX() + (startNode.getWidth() /2));
                startLine.setStartY(startNode.getTranslateY() + startNode.getHeight());
                startLine.setEndX(endNode.getTranslateX() + (endNode.getWidth()/2));
                startLine.setEndY(endNode.getTranslateY());

                middleLine.setStartX(0);
                middleLine.setStartY(0);
                middleLine.setEndX(0);
                middleLine.setEndY(0);

                endLine.setStartX(0);
                endLine.setStartY(0);
                endLine.setEndX(0);
                endLine.setEndY(0);
            } else {
                startLine.setStartX(startNode.getTranslateX() + (startNode.getWidth() /2));
                startLine.setStartY(startNode.getTranslateY() + startNode.getHeight());
                startLine.setEndX(startNode.getTranslateX() + (startNode.getWidth() /2));
                startLine.setEndY(startNode.getTranslateY() + startNode.getHeight() + ((endNode.getTranslateY() - (startNode.getTranslateY() + startNode.getHeight()))/2));

                middleLine.setStartX(startNode.getTranslateX() + (startNode.getWidth() /2));
                middleLine.setStartY(startNode.getTranslateY() + startNode.getHeight() + ((endNode.getTranslateY() - (startNode.getTranslateY() + startNode.getHeight()))/2));
                middleLine.setEndX(endNode.getTranslateX() + (endNode.getWidth()/2));
                middleLine.setEndY(startNode.getTranslateY() + startNode.getHeight() + ((endNode.getTranslateY() - (startNode.getTranslateY() + startNode.getHeight()))/2));

                endLine.setStartX(endNode.getTranslateX() + (endNode.getWidth()/2));
                endLine.setStartY(startNode.getTranslateY() + startNode.getHeight() + ((endNode.getTranslateY() - (startNode.getTranslateY() + startNode.getHeight()))/2));
                endLine.setEndX(endNode.getTranslateX() + (endNode.getWidth()/2));
                endLine.setEndY(endNode.getTranslateY());
            }

            position = Position.BELOW;
        }
        //If end node is above startNode:
        else if (startNode.getTranslateY() >= endNode.getTranslateY() + endNode.getHeight()) {
            if(Math.abs(startNode.getTranslateX() + (startNode.getWidth()/2) - (endNode.getTranslateX() + (endNode.getWidth()/2))) < 20){
                startLine.setStartX(startNode.getTranslateX() + (startNode.getWidth() / 2));
                startLine.setStartY(startNode.getTranslateY());
                startLine.setEndX(endNode.getTranslateX() + (endNode.getWidth()/2));
                startLine.setEndY(endNode.getTranslateY() + endNode.getHeight());

                middleLine.setStartX(0);
                middleLine.setStartY(0);
                middleLine.setEndX(0);
                middleLine.setEndY(0);

                endLine.setStartX(0);
                endLine.setStartY(0);
                endLine.setEndX(0);
                endLine.setEndY(0);
            } else {
                startLine.setStartX(startNode.getTranslateX() + (startNode.getWidth() /2));
                startLine.setStartY(startNode.getTranslateY());
                startLine.setEndX(startNode.getTranslateX() + (startNode.getWidth() /2));
                startLine.setEndY(endNode.getTranslateY() + endNode.getHeight() + ((startNode.getTranslateY() - (endNode.getTranslateY() + endNode.getHeight()))/2));

                middleLine.setStartX(startNode.getTranslateX() + (startNode.getWidth() /2));
                middleLine.setStartY(endNode.getTranslateY() + endNode.getHeight() + ((startNode.getTranslateY() - (endNode.getTranslateY() + endNode.getHeight()))/2));
                middleLine.setEndX(endNode.getTranslateX() + (endNode.getWidth()/2));
                middleLine.setEndY(endNode.getTranslateY() + endNode.getHeight() + ((startNode.getTranslateY() - (endNode.getTranslateY() + endNode.getHeight()))/2));

                endLine.setStartX(endNode.getTranslateX() + (endNode.getWidth()/2));
                endLine.setStartY(endNode.getTranslateY() + endNode.getHeight() + ((startNode.getTranslateY() - (endNode.getTranslateY() + endNode.getHeight()))/2));
                endLine.setEndX(endNode.getTranslateX() + (endNode.getWidth()/2));
                endLine.setEndY(endNode.getTranslateY() + endNode.getHeight());
            }

            position = Position.ABOVE;
        }
        //TODO Handle when the nodes are overlapping.
    }

    public AbstractNodeView getStartNode() {
        return startNode;
    }

    public void setStartNode(AbstractNodeView startNode) {
        this.startNode = startNode;
    }

    public AbstractNodeView getEndNode() {
        return endNode;
    }

    public void setEndNode(AbstractNodeView endNode) {
        this.endNode = endNode;
    }


    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals(Constants.changeNodeTranslateX) || evt.getPropertyName().equals(Constants.changeNodeTranslateY)){
            setPosition();
        } else if(evt.getPropertyName().equals(Constants.changeEdgeZoom)) {
            setStrokeWidth((double)evt.getNewValue());
            setPosition();
        } else if(evt.getPropertyName().equals(Constants.changeEdgeStartMultiplicity)) {
            startMultiplicity.setText((String)evt.getNewValue());
            draw();
        } else if(evt.getPropertyName().equals(Constants.changeEdgeEndMultiplicity)){
            endMultiplicity.setText((String)evt.getNewValue());
            draw();
        } else if (evt.getPropertyName().equals(Constants.changeNodeWidth) || evt.getPropertyName().equals(Constants.changeNodeHeight)){
            setPosition();
        }
    }
}
