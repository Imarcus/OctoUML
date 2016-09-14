package util.commands;

import model.nodes.ClassNode;
import model.nodes.Node;

/**
 * Created by chalmers on 2016-08-29.
 */
public class SetNodeAttributeCommand implements Command {
    private ClassNode node;
    private String newAttribute;
    private String oldAttribute;

    public SetNodeAttributeCommand(ClassNode pNode, String pNewAttribute, String pOldAttribute){
        node = pNode;
        newAttribute = pNewAttribute;
        oldAttribute = pOldAttribute;
    }

    @Override
    public void undo() {
        node.setAttributes(oldAttribute);
    }

    @Override
    public void execute() {
        node.setAttributes(newAttribute);
    }

    public Node getNode() {
        return node;
    }

    public String getNewAttribute() {
        return newAttribute;
    }

    public String getOldAttribute() {
        return oldAttribute;
    }
}
