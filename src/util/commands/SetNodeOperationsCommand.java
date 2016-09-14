package util.commands;

import model.nodes.ClassNode;
import model.nodes.Node;

/**
 * Created by chalmers on 2016-08-29.
 */
public class SetNodeOperationsCommand implements Command {
    private ClassNode node;
    private String newOperations;
    private String oldOperations;

    public SetNodeOperationsCommand(ClassNode pNode, String pNewOperations, String pOldOperations){
        node = pNode;
        newOperations = pNewOperations;
        oldOperations = pOldOperations;
    }

    @Override
    public void undo() {
        node.setOperations(oldOperations);
    }

    @Override
    public void execute() {
        node.setOperations(newOperations);
    }

    public Node getNode() {
        return node;
    }

    public String getNewOperations() {
        return newOperations;
    }

    public String getOldOperations() {
        return oldOperations;
    }
}
