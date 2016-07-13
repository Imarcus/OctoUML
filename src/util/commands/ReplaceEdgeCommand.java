package util.commands;

import controller.MainController;
import model.AbstractEdge;
import model.Graph;
import view.AbstractEdgeView;

/**
 * Created by marcusisaksson on 2016-03-17.
 */
public class ReplaceEdgeCommand implements Command {
    private AbstractEdge oldEdge;
    private AbstractEdge newEdge;
    private AbstractEdgeView oldEdgeView;
    private AbstractEdgeView newEdgeView;
    private MainController aController;
    private Graph aGraph;

    private AddDeleteEdgeCommand oldEdgeCommand;
    private AddDeleteEdgeCommand newEdgeCommand;

    /**
     * Creates the command.
     */
    public ReplaceEdgeCommand(AbstractEdge pOldEdge, AbstractEdge pNewEdge,
                              AbstractEdgeView pOldEdgeView, AbstractEdgeView pNewEdgeView,
                              MainController pController, Graph pGraph)
    {
        oldEdge = pOldEdge;
        newEdge = pNewEdge;
        oldEdgeView = pOldEdgeView;
        newEdgeView = pNewEdgeView;
        aController = pController;
        aGraph = pGraph;

        oldEdgeCommand = new AddDeleteEdgeCommand(aController, oldEdgeView, oldEdge, false);
        newEdgeCommand = new AddDeleteEdgeCommand(aController, newEdgeView, newEdge, true);
    }

    /**
     * Undoes the command and unreplaces the edge.
     */
    public void undo()
    {
        oldEdgeCommand.undo();
        newEdgeCommand.undo();
    }

    /**
     * Performs the command and replaces the edge.
     */
    public void execute()
    {
        oldEdgeCommand.execute();
        newEdgeCommand.execute();
    }

}
