package model;

/**
 * Created by chris on 2016-02-15.
 */
public interface Edge extends GraphElement {
    Node getStartNode();
    Node getEndNode();
    Edge copy(AbstractNode startNodeCopy, AbstractNode endNodeCopy);
    String getType();
}
