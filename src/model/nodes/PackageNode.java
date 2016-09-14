package model.nodes;

import javafx.geometry.Point2D;

import java.util.ArrayList;

/**
 * Represents a UML-package.
 */
public class PackageNode extends AbstractNode
{
    private static final String type = "PACKAGE";
    private ArrayList<AbstractNode> childNodes = new ArrayList<>();

    public PackageNode(double x, double y, double width, double height)
    {
        super(x, y, width, height );

        //Don't accept nodes with size less than minWidth * minHeight.
        this.width = width < PACKAGE_MIN_WIDTH ? PACKAGE_MIN_WIDTH : width;
        this.height = height < PACKAGE_MIN_HEIGHT ? PACKAGE_MIN_HEIGHT : height;
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
    public void setHeight(double height) {
        this.height = height < CLASS_MIN_HEIGHT ? CLASS_MIN_HEIGHT : height;
        super.setHeight(height);
    }

    @Override
    public void setWidth(double width) {
        this.width = width < CLASS_MIN_WIDTH ? CLASS_MIN_WIDTH : width;
        super.setWidth(width);
    }

    @Override
    public void remoteSetHeight(double height) {
        this.height = height < CLASS_MIN_HEIGHT ? CLASS_MIN_HEIGHT : height;
        super.remoteSetHeight(height);
    }

    @Override
    public void remoteSetWidth(double width) {
        this.width = width < CLASS_MIN_WIDTH ? CLASS_MIN_WIDTH : width;
        super.remoteSetWidth(width);
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

    public String getType(){
        return type;
    }
}