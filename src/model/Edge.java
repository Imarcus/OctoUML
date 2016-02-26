package model;

/**
 * Created by chris on 2016-02-15.
 */
public interface Edge extends GraphElement {
    public Node getStartNode();
    public Node getEndNode();
}
