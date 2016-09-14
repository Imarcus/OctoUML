package model.edges;

import model.nodes.AbstractNode;
import model.nodes.Node;

/**
 * Represents an inheritance relationship between two classes.
 */
public class InheritanceEdge extends AbstractEdge {

    public InheritanceEdge(Node startNode, Node endNode) {
        super(startNode, endNode);
    }

    /**
     * No-arg constructor for JavaBean convention
     */
    public InheritanceEdge() {}

    public Edge copy(AbstractNode startNodeCopy, AbstractNode endNodeCopy) {
        return new InheritanceEdge(startNodeCopy, endNodeCopy);
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
        return "Inheritance";
    }
}
