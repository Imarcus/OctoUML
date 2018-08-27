package util.commands;

import model.nodes.ClassNode;
import model.nodes.Node;

import java.util.List;

import model.nodes.Attribute;

/**
 * Created by chalmers on 2016-08-29.
 */
public class SetNodeAttributeCommand implements Command {
    private ClassNode node;
    private List<Attribute> newAttribute;
    private List<Attribute> oldAttribute;

    public SetNodeAttributeCommand(ClassNode pNode, List<Attribute> pNewAttribute, List<Attribute> pOldAttribute){
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

    public List<Attribute> getNewAttribute() {
        return newAttribute;
    }

    public List<Attribute> getOldAttribute() {
        return oldAttribute;
    }
}
