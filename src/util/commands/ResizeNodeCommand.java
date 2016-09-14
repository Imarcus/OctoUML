package util.commands;

import model.nodes.Node;

/**
 * Created by Marcus on 2016-08-29.
 */
public class ResizeNodeCommand implements Command {

    private Node node;
    private double oldWidth;
    private double oldHeight;
    private double newWidth;
    private double newHeight;

    public ResizeNodeCommand(Node pNode, double pOldWidth, double pOldHeight, double pNewWidth, double pNewHeight){
        node = pNode;
        oldWidth = pOldWidth;
        oldHeight = pOldHeight;
        newWidth = pNewWidth;
        newHeight = pNewHeight;
    }


    @Override
    public void undo() {
        node.setWidth(oldWidth);
        node.setHeight(oldHeight);
    }

    @Override
    public void execute() {
        node.setWidth(newWidth);
        node.setHeight(newHeight);
    }

    public Node getNode() {
        return node;
    }

    public double getOldWidth() {
        return oldWidth;
    }

    public double getOldHeight() {
        return oldHeight;
    }

    public double getNewWidth() {
        return newWidth;
    }

    public double getNewHeight() {
        return newHeight;
    }
}
