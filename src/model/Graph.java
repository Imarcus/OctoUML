package model;

import javafx.geometry.Point2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Model-representation of a Graph.
 */
public class Graph {
    private List<AbstractNode> nodes;
    private List<Edge> edges;
    private List<Sketch> sketches;

    public Graph() {
        nodes = new LinkedList<>();
        edges = new LinkedList<>();
        sketches = new LinkedList<>();
    }

    /**
     * Adds a Node to the Graph. If the node is added inside a package, the node is also added as a child to
     * that package. 
     * @param n, the Node that should be added.
     */
    public boolean addNode(AbstractNode n){
        assert n != null;
        for (AbstractNode node : nodes) {
            if (node instanceof PackageNode) {
                if (n.getX() >= node.getX() && n.getX() <= node.getX() + node.getWidth()
                        && n.getY() >= node.getY() && n.getY() <= node.getY() + node.getHeight() ) {
                    ((PackageNode) node).addChild(n);
                }
            }
        }
        return nodes.add(n);
    }

    /**
     * Add an Edge to the Graph.
     * @param e, cannot be null.
     */
    public boolean addEdge(Edge e){
        assert e != null;
        return edges.add(e);
    }

    /**
     * Add a Sketch to the Graph
     * @param s, cannot be null.
     */
    public void addSketch(Sketch s) {
        assert s != null;
        sketches.add(s);
    }

    //TODO This should maybe also connect the Nodes somehow?
    public boolean connect(Node startNode, Node endNode, Edge edge){
        if (startNode != null && endNode != null && edge != null) {
            if (nodes.contains(startNode) && nodes.contains(endNode)) {
                return addEdge(edge);
            }
        }
        return false;
    }


    /**
     * Removes a Node from the graph.
     * @param n to remove
     * @return true if the Node is removed, otherwise false.
     */
    public boolean removeNode(Node n) {
        assert n != null;
        return nodes.remove(n);
    }

    /**
     * Removes an Edge from the Graph.
     * @param e, Edge to be removed.
     * @return true if the Edge is successfully removed.
     */
    public boolean removeEdge(Edge e) {
        assert e != null;
        return edges.remove(e);
    }

    public boolean removeSketch(Sketch s) {
        assert s != null;
        return sketches.remove(s);
    }

    public List<AbstractNode> getAllNodes() {
        return nodes;
    }

    public List<Edge> getAllEdges() {
        return edges;
    }

    public List<Sketch> getAllSketches(){
        return sketches;
    }

    public List<GraphElement> getAllGraphElements() {
        ArrayList list = new ArrayList();
        list.addAll(nodes);
        list.addAll(edges);
        list.addAll(sketches);
        return list;
    }

    /**
     * Returns the Node given a Point where it's located.
     * @param point can not be null.
     * @return the node if found, otherwise null.
     */
    public Node findNode(Point2D point) {
        assert point != null;
        for (Node node : nodes){
            if (node.getBounds().contains(point.getX(), point.getY())) {
                if (node instanceof PackageNode && ((PackageNode) node).findNode(point) != null) {
                    System.out.println("PackageNode found: x=" + node.getX() + " y=" + node.getY() +
                            " width=" + node.getWidth() + " height=" + node.getHeight());
                    return ((PackageNode) node).findNode(point);
                }
                else {
                    return node;
                }
            }
        }
        return null;
    }
}
