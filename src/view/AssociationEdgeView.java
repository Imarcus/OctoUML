package view;

import javafx.scene.paint.Color;
import model.AbstractEdge;

/**
 * Created by chris on 2016-02-18.
 */
public class AssociationEdgeView extends AbstractEdgeView {
    private AbstractEdge refEdge;
    private AbstractNodeView startNode;
    private AbstractNodeView endNode;

    
    public AssociationEdgeView(AbstractEdge edge, AbstractNodeView startNode, AbstractNodeView endNode) {
        super(edge, startNode, endNode);
        this.refEdge = edge;
        this.startNode = startNode;
        this.endNode = endNode;
        //TODO Hardcoded value:
        this.setStrokeWidth(2);
        this.setFill(Color.BLACK);
    }
}
