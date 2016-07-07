package util.commands;

import model.AbstractEdge;

/**
 * Created by chalmers on 2016-07-07.
 */
public class DirectionChangeEdgeCommand implements Command{

    AbstractEdge aEdge;
    AbstractEdge.Direction aOldDirection;
    AbstractEdge.Direction aNewDirection;

    public DirectionChangeEdgeCommand(AbstractEdge pEdge, AbstractEdge.Direction pOldDirection,
                                      AbstractEdge.Direction pNewDirection){
        aEdge = pEdge;
        aOldDirection = pOldDirection;
        aNewDirection = pNewDirection;
    }

    public void undo(){
        aEdge.setDirection(aOldDirection);
    }

    public void execute(){
        aEdge.setDirection(aNewDirection);
    }
}
