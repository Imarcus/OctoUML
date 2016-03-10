package model;

/**
 * Created by chris on 2016-02-15.
 */
public class AssociationEdge extends AbstractEdge {
    private Node startNode; //TODO these are not needed since they are in the super class
    private Node endNode;

    public AssociationEdge(Node startNode, Node endNode) {
        super(startNode, endNode);
        this.startNode = startNode;
        this.endNode = endNode;
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
}
