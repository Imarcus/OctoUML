package model.edges;

import model.nodes.AbstractNode;
import model.nodes.Node;

/**
 * Represents an associate relationship between two UML-classes.
 */
public class AssociationEdge extends AbstractEdge {

    public AssociationEdge(Node startNode, Node endNode) {
        super(startNode, endNode);
    }

    //TODO IMPLEMENT:

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
    public double getScaleX() {
        return 0;
    }

    @Override
    public double getScaleY() {
        return 0;
    }

    /**
     * No-arg constructor for JavaBean convention
     */
    public AssociationEdge(){
    }
    
    public AssociationEdge copy(AbstractNode startNodeCopy, AbstractNode endNodeCopy){
        return new AssociationEdge(startNodeCopy, endNodeCopy);
    }

    public String getType(){
        return "Association";
    }
}
