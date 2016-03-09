package model;

/**
 * Abstract Edge to hide some basic Edge-functionality.
 */
public abstract class AbstractEdge implements Edge {
    private Node startNode;
    private Node endNode;

    public AbstractEdge(Node startNode, Node endNode) {
        this.startNode = startNode;
        this.endNode = endNode;
    }

    public Node getStartNode() {
        return startNode;
    }

    public void setStartNode(Node node) {
        this.startNode = node;
    }

    public Node getEndNode() {
        return endNode;
    }

    public void setEndNode(Node node) {
        this.endNode = node;
    }


}
