package model;

/**
 * Created by chris on 2016-03-11.
 */
public class InheritanceEdge extends AbstractEdge {

    public InheritanceEdge(Node startNode, Node endNode) {
        super(startNode, endNode);
    }

    /**
     * No-arg constructor for JavaBean convention
     */
    public InheritanceEdge() {}

    @Override
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
}
