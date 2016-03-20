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
    }

    /**
     * Undoes the command and unreplaces the edge.
     */
    public void undo()
    {
        aGraph.removeEdge(newEdge);
        aController.deleteEdgeView(newEdgeView, null, true);

        aGraph.addEdge(oldEdge);
        aController.addEdgeView(oldEdgeView);
    }

    /**
     * Performs the command and replaces the edge.
     */
    public void execute()
    {
        aGraph.removeEdge(oldEdge);
        aController.deleteEdgeView(oldEdgeView, null, true);

        aGraph.addEdge(newEdge);
        aController.addEdgeView(newEdgeView);
    }

}
