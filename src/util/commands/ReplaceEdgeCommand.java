package util.commands;

import controller.AbstractDiagramController;
import model.edges.AbstractEdge;
import model.Graph;
import view.edges.AbstractEdgeView;

/**
 * Commands used when switching between different kinds of edges.
 */
public class ReplaceEdgeCommand implements Command {
    private AbstractEdge oldEdge;
    private AbstractEdge newEdge;
    private AbstractEdgeView oldEdgeView;
    private AbstractEdgeView newEdgeView;
    private AbstractDiagramController aController;
    private Graph aGraph;

    private AddDeleteEdgeCommand oldEdgeCommand;
    private AddDeleteEdgeCommand newEdgeCommand;

    /**
     * Creates the command.
     */
    public ReplaceEdgeCommand(AbstractEdge pOldEdge, AbstractEdge pNewEdge,
                              AbstractEdgeView pOldEdgeView, AbstractEdgeView pNewEdgeView,
                              AbstractDiagramController pController, Graph pGraph)
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
