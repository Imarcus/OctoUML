package model;

import javafx.geometry.Point2D;
import model.edges.AbstractEdge;
import model.edges.Edge;
import model.nodes.AbstractNode;
import model.nodes.Node;
import model.nodes.PackageNode;
import util.Constants;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Model-representation of a Graph.
 */
public class Graph implements Serializable, PropertyChangeListener {

    private static int objectCount = 0;
    private int id;

    private transient PropertyChangeSupport remoteChanges = new PropertyChangeSupport(this);

    private List<AbstractNode> allNodes = new ArrayList<>();
    private List<Edge> allEdges = new ArrayList<>();
    private transient List<Sketch> allSketches = new ArrayList<>();

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
     * @param remote, true if change comes from a remote server
     */
    public void addNode(AbstractNode n, boolean remote){
        assert n != null;
        for (AbstractNode node : allNodes) {
            if (node instanceof PackageNode) {
                if (n.getX() >= node.getX() && n.getX() <= node.getX() + node.getWidth()
                        && n.getY() >= node.getY() && n.getY() <= node.getY() + node.getHeight() ) {
                    ((PackageNode) node).addChild(n);
                }
            }
        }
        allNodes.add(n);
        if(!remote){
            remoteChanges.firePropertyChange(Constants.NodeAdd, null, n);
        } else {
            AbstractNode.incrementObjectCount();
        }
        n.addRemotePropertyChangeListener(this);
    }

    /**
     * Add an Edge to the Graph.
     * @param remote, true if change comes from a remote server
     * @param e, cannot be null.
     */
    public boolean addEdge(Edge e, boolean remote){
        assert e != null;
        boolean success = allEdges.add(e);
        if(!remote){
            remoteChanges.firePropertyChange(Constants.EdgeAdd, null, e);
        } else {
            AbstractEdge.incrementObjectCount();
        }
        return success;
    }

    /**
     * Add a Sketch to the Graph
     * @param s, cannot be null.
     */
    public void addSketch(Sketch s, boolean remote) {
        assert s != null;
        if(!remote){
            remoteChanges.firePropertyChange(Constants.sketchAdd, null, s);
        }
        allSketches.add(s);
        s.addPropertyChangeListener(this);
    }


    /**
     * Removes a Node from the graph.
     * @param n to remove
     * @param remote, true if change comes from a remote server
     * @return true if the Node is removed, otherwise false.
     */
    public boolean removeNode(Node n, boolean remote) {
        assert n != null;
        if(!remote) {
            remoteChanges.firePropertyChange(Constants.NodeRemove, null, n.getId());
        }
        ((AbstractNode)n).removePropertyChangeListener(this);
        return allNodes.remove(n);
    }

    /**
     * Removes an Edge from the Graph.
     * @param e, Edge to be removed.
     * @param remote, true if change comes from a remote server
     * @return true if the Edge is successfully removed.
     */
    public boolean removeEdge(Edge e, boolean remote) {
        assert e != null;
        if(!remote){
            remoteChanges.firePropertyChange(Constants.EdgeRemove, null, e.getId());
        }
        return allEdges.remove(e);
    }

    public boolean removeSketch(Sketch s, boolean remote) {
        assert s != null;
        if(!remote){
            remoteChanges.firePropertyChange(Constants.sketchRemove, null, s.getId());
        }
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
            System.out.println("Edge, edge.getStartNode or edge.getEndNode is null.");
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

    public void addRemotePropertyChangeListener(PropertyChangeListener l) {
        remoteChanges.addPropertyChangeListener(l);
    }

    public void removeRemotePropertyChangeListener(PropertyChangeListener l) {
        remoteChanges.removePropertyChangeListener(l);
    }

    public void listenToElement(Object e){
        if(e instanceof AbstractNode){
            ((AbstractNode)e).addRemotePropertyChangeListener(this);
        } else if (e instanceof Sketch){
            ((Sketch) e).addPropertyChangeListener(this);
        }

    }

    /**
     * Only Server listens to this.
     * @param evt
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        remoteChanges.firePropertyChange(evt);
    }
}
