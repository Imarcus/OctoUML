package view.edges;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Text;
import model.edges.AbstractEdge;
import model.edges.AbstractEdge.Direction;
import util.Constants;
import view.nodes.AbstractNodeView;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;

/**
 * The Graphical Representation of a AssociationEdge.
 */
public class AssociationEdgeView extends AbstractEdgeView {
    private AbstractEdge refEdge;
    private AbstractNodeView startNode;
    private AbstractNodeView endNode;
    private ArrayList<Line> arrowHeadLines = new ArrayList<>();
    private Text startMultplicity;
    private Text endMultplicity;


    
    public AssociationEdgeView(AbstractEdge edge, AbstractNodeView startNode, AbstractNodeView endNode) {
        super(edge, startNode, endNode);
        this.refEdge = edge;
        this.startNode = startNode;
        this.endNode = endNode;
        this.setStrokeWidth(super.STROKE_WIDTH);
        this.setStroke(Color.BLACK);
        startMultplicity = new Text(edge.getStartMultiplicity());
        endMultplicity = new Text(edge.getEndMultiplicity());
        setPosition();
        draw();
    }

    public Text getStartMultplicity() {
		return startMultplicity;
	}

	public void setStartMultplicity(Text startMultplicity) {
		this.startMultplicity = startMultplicity;
	}

	public Text getEndMultplicity() {
		return endMultplicity;
	}

	public void setEndMultplicity(Text endMultplicity) {
		this.endMultplicity = endMultplicity;
	}

	protected void draw() {
		Text aux = new Text();
        AbstractEdge.Direction direction = refEdge.getDirection();
        getChildren().clear();
        getChildren().add(getStartLine());
        getChildren().add(getMiddleLine());
        getChildren().add(getEndLine());
        super.draw();
        this.getChildren().add(super.getStartMultiplicity());
        this.getChildren().add(super.getEndMultiplicity());
        

        //Draw arrows.
        switch(direction) {
            case NO_DIRECTION:
                //Do nothing.
                break;
            case START_TO_END:
            	if(!endNode.isSelected() && startNode.isSelected()) {
            		this.getChildren().add(drawArrowHead(getStartLine().getStartX(), getStartLine().getStartY(), getStartLine().getEndX(), getStartLine().getEndY()));
            		
              	} else if(endNode.isSelected() && !startNode.isSelected() )  {
            		this.getChildren().add(drawArrowHead(getEndLine().getEndX(), getEndLine().getEndY(), getEndLine().getStartX(), getEndLine().getStartY()));
          		
               	} else if (!endNode.isSelected() && !startNode.isSelected()) {
            		this.getChildren().add(drawArrowHead(getStartLine().getStartX(), getStartLine().getStartY(), getStartLine().getEndX(), getStartLine().getEndY()));

            	} else {
            		this.getChildren().add(drawArrowHead(getEndLine().getEndX(), getEndLine().getEndY(), getEndLine().getStartX(), getEndLine().getStartY()));	             		
            	}
                break;
            case END_TO_START:
            	if(!endNode.isSelected() && startNode.isSelected() ) {
            		this.getChildren().add(drawArrowHead(getEndLine().getEndX(), getEndLine().getEndY(), getEndLine().getStartX(), getEndLine().getStartY()));
            	} else if(endNode.isSelected() && !startNode.isSelected() )  {
            		this.getChildren().add(drawArrowHead(getStartLine().getStartX(), getStartLine().getStartY(), getStartLine().getEndX(), getStartLine().getEndY()));	
            	} else if (!endNode.isSelected() && !startNode.isSelected()) {
            		this.getChildren().add(drawArrowHead(getEndLine().getEndX(), getEndLine().getEndY(), getEndLine().getStartX(), getEndLine().getStartY()));
            	} else {
            		this.getChildren().add(drawArrowHead(getStartLine().getStartX(), getStartLine().getStartY(), getStartLine().getEndX(), getStartLine().getEndY()));	
            	}
                break;
            case BIDIRECTIONAL:
                this.getChildren().add(drawArrowHead(getStartLine().getStartX(), getStartLine().getStartY(), getStartLine().getEndX(), getStartLine().getEndY()));
                this.getChildren().add(drawArrowHead(getEndLine().getEndX(), getEndLine().getEndY(), getEndLine().getStartX(), getEndLine().getStartY()));
                break;       
        }       	
    }

    public void setSelected(boolean selected){
        super.setSelected(selected);
        if(selected){
            for(Line l : arrowHeadLines){
                l.setStroke(Constants.selected_color);
            }
        } else {
            for (Line l : arrowHeadLines) {
                l.setStroke(Color.BLACK);
            }
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
        if(evt.getPropertyName().equals(Constants.changeNodeTranslateX) || evt.getPropertyName().equals(Constants.changeNodeTranslateY) ||
                evt.getPropertyName().equals(Constants.changeEdgeDirection)) {
            draw();
        }   else if(evt.getPropertyName().equals(Constants.changeEdgeStartMultiplicity)) {
            startMultplicity.setText((String)evt.getNewValue());
            draw();
        } else if(evt.getPropertyName().equals(Constants.changeEdgeEndMultiplicity)){
            endMultplicity.setText((String)evt.getNewValue());
            draw();
    }
        }
}
