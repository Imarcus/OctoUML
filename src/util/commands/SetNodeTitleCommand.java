package util.commands;

import model.nodes.Node;

/**
 * Created by chalmers on 2016-08-29.
 */
public class SetNodeTitleCommand implements Command {

    private Node node;
    private String newTitle;
    private String oldTitle;

    public SetNodeTitleCommand(Node pNode, String pNewTitle, String pOldTitle){
        node = pNode;
        newTitle = pNewTitle;
        oldTitle = pOldTitle;
    }

    @Override
    public void undo() {
        node.setTitle(oldTitle);
    }

    @Override
    public void execute() {
        node.setTitle(newTitle);
    }

    public Node getNode() {
        return node;
    }

    public String getNewTitle() {
        return newTitle;
    }

    public String getOldTitle() {
        return oldTitle;
    }
}
