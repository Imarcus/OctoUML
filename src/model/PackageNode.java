package model;

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

    public void addChild(AbstractNode childNode) {
        childNode.setIsChild(true);
        this.childNodes.add(childNode);
    }
}