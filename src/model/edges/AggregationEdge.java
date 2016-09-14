package model.edges;

import model.nodes.AbstractNode;
import model.nodes.Node;

/**
 * Represents an aggregate relationship between two UML-classes.
 */
public class AggregationEdge extends AbstractEdge {

    public AggregationEdge(Node startNode, Node endNode) {
        super(startNode, endNode);
    }

    public Edge copy(AbstractNode startNodeCopy, AbstractNode endNodeCopy) {
        return new AggregationEdge(getStartNode(), getEndNode());
    }

    @Override
    public void setTranslateX(double x) {

    }

    @Override
    public void setTranslateY(double y) {

    }

    @Override
    public void setScaleX(double x) {

    }

    @Override
    public void setScaleY(double y) {

    }

    @Override
    public double getTranslateX() {
        return 0;
    }

    @Override
    public double getTranslateY() {
        return 0;
    }

    @Override
    public double getScaleY() {
        return 0;
    }

    @Override
    public double getScaleX() {
        return 0;
    }

    public String getType(){
        return "Aggregation";
    }

    public AggregationEdge(){

    }
}
