package util.commands;

import java.util.List;

import model.nodes.ClassNode;
import model.nodes.Node;
import model.nodes.Operation;

/**
 * Created by chalmers on 2016-08-29.
 */
public class SetNodeOperationsCommand implements Command {
    private ClassNode node;
    private List<Operation> newOperations;
    private List<Operation> oldOperations;

    public SetNodeOperationsCommand(ClassNode pNode, List<Operation> pNewOperations, List<Operation> pOldOperations){
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

    public List<Operation> getNewOperations() {
        return newOperations;
    }

    public List<Operation> getOldOperations() {
        return oldOperations;
    }
}
