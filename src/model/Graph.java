package model;

import javafx.geometry.Point2D;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

/**
 * Model-representation of a Graph.
 */
public class Graph implements Serializable, PropertyChangeListener {

    private static int objectCount = 0;
    private int id = 0;

    private PropertyChangeSupport changes = new PropertyChangeSupport(this);

    private List<AbstractNode> allNodes = new ArrayList<>();
    private List<Edge> allEdges = new ArrayList<>();
    private List<Sketch> allSketches = new ArrayList<>();

    private String name = "";

    public Graph() {
        id = ++ objectCount;
    }

    public void setName(String name){
        this.name = name;

    }

    public String getName(){
        return name;
    }

    /**
     * Adds a Node to the Graph. If the node is added inside a package, the node is also added as a child to
     * that package. 
     * @param n, the Node that should be added.
     */
    public boolean addNode(AbstractNode n){
        assert n != null;
        for (AbstractNode node : allNodes) {
            if (node instanceof PackageNode) {
                //TODO this is weird, looking for graphical position in model
                if (n.getX() >= node.getX() && n.getX() <= node.getX() + node.getWidth()
                        && n.getY() >= node.getY() && n.getY() <= node.getY() + node.getHeight() ) {
                    ((PackageNode) node).addChild(n);
                }
            }
        }
        n.addPropertyChangeListener(this);
        changes.firePropertyChange("AddNode", null, null);
        return allNodes.add(n);
    }

    /**
     * Add an Edge to the Graph.
     * @param e, cannot be null.
     */
    public boolean addEdge(Edge e){
        assert e != null;
        changes.firePropertyChange("AddEdge", null, null);
        return allEdges.add(e);
    }

    /**
     * Add a Sketch to the Graph
     * @param s, cannot be null.
     */
    public void addSketch(Sketch s) {
        assert s != null;
        changes.firePropertyChange("AddSketch", null, null);
        allSketches.add(s);
    }

    public boolean connect(Node startNode, Node endNode, Edge edge){
        if (startNode != null && endNode != null && edge != null) {
            if (allNodes.contains(startNode) && allNodes.contains(endNode)) {
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
        changes.firePropertyChange("RemoveNode", null, null);
        return allNodes.remove(n);
    }

    /**
     * Removes an Edge from the Graph.
     * @param e, Edge to be removed.
     * @return true if the Edge is successfully removed.
     */
    public boolean removeEdge(Edge e) {
        assert e != null;
        changes.firePropertyChange("RemoveEdge", null, null);
        return allEdges.remove(e);
    }

    public boolean removeSketch(Sketch s) {
        assert s != null;
        return allSketches.remove(s);
    }

    public List<AbstractNode> getAllNodes() {
        return allNodes;
    }

    public List<Edge> getAllEdges() {
        return allEdges;
    }

    public List<Sketch> getAllSketches(){
        return allSketches;
    }

    public List<GraphElement> getAllGraphElements() {
        ArrayList list = new ArrayList();
        list.addAll(allNodes);
        list.addAll(allEdges);
        list.addAll(allSketches);
        return list;
    }

    /**
     * Returns true if the graph have an edge that connects the same nodes.
     * @param edge
     * @return
     */
    public boolean hasEdge(Edge edge) {
        if (edge == null || edge.getStartNode() == null || edge.getEndNode() == null) {
            System.out.println("CRITICAL: Edge, edge.getStartNode or edge.getEndNode is null.");
            //TODO what should this return?
            return false;
        }
        for(Edge e: allEdges) {
            if ((e.getStartNode().equals(edge.getStartNode()) || e.getEndNode().equals(edge.getStartNode())) &&
                    (e.getEndNode().equals(edge.getEndNode()) || e.getStartNode().equals(edge.getEndNode()))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the Node given a Point where it's located.
     * @param point can not be null.
     * @return the node if found, otherwise null.
     */
    public Node findNode(Point2D point) {
        assert point != null;
        for (Node node : allNodes){
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


    //------------------------------- Implemented for serializable ----------------------------------------------------

    public void setAllNodes(List<AbstractNode> allNodes) {
        this.allNodes.clear();
        this.allNodes.addAll(allNodes);
    }

    public void setAllEdges(List<Edge> allEdges) {
        this.allEdges.clear();
        this.allEdges.addAll(allEdges);
    }

    public void setAllSketches(List<Sketch> allSketches) {
        this.allSketches.clear();
        this.allSketches.addAll(allSketches);
    }

    public String getId(){
        return "GRAPH_" + id;
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        changes.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        changes.removePropertyChangeListener(l);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        changes.firePropertyChange(evt);
    }
}
