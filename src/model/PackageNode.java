package model;

import javafx.geometry.Point2D;
import view.ClassNodeView;

import java.util.ArrayList;

/**
 * Created by marcusisaksson on 2016-02-17.
 */
public class PackageNode extends AbstractNode
{
    private ArrayList<AbstractNode> childNodes = new ArrayList<>();


    public PackageNode(double x, double y, double width, double height)
    {
        super(x, y, width, height );
    }

    public ArrayList<AbstractNode> getChildNodes() {
        return childNodes;
    }

    public void removeChild(AbstractNode childNode){
        childNode.setIsChild(false);
        this.childNodes.remove(childNode);
    }

    /**
     * Finds a node in this package from a Point2D.
     * @param point
     * @return the node if found, otherwise null.
     */
    public AbstractNode findNode(Point2D point) {
        for (AbstractNode node : childNodes) {
            if (point.getX() >= node.getX() && point.getX() <= node.getX()+ node.getWidth()
                    && point.getY() >= node.getY() && point.getY() <= node.getY() + node.getHeight()) {
                System.out.println("\t IN PACKAGENODE:findNode, found this:" + node.toString());
                return node;
            }
        }
        return null;
    }

    public void addChild(AbstractNode childNode) {
        if (!childNodes.contains(childNode)) {
            childNode.setIsChild(true);
            this.childNodes.add(childNode);
        }
    }

    @Override
    public PackageNode copy(){
        PackageNode newCopy = new PackageNode(this.getX(), this.getY(), this.getWidth(), this.getHeight());
        newCopy.setTranslateX(this.getTranslateX());
        newCopy.setTranslateY(this.getTranslateY());
        newCopy.setScaleX(this.getScaleX());
        newCopy.setScaleY(this.getScaleY());

        if(this.getTitle() != null){
            newCopy.setTitle(this.getTitle());

        }
        /*if(this.getChildNodes() != null){
            for(AbstractNode child : this.getChildNodes()){
                newCopy.addChild(child.copy());
            }
        }*/
        return newCopy;
    }

    /**
     * No-arg constructor for JavaBean convention
     */
    public PackageNode(){
    }
}