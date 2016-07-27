package model;

/**
 * Interfaced used by all Edge-classes, represents a relationship between two nodes.
 */
public interface Edge extends GraphElement {
    Node getStartNode();
    Node getEndNode();
    Edge copy(AbstractNode startNodeCopy, AbstractNode endNodeCopy);
    String getType();
    String getId();
}
