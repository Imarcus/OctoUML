package controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Pane;
import model.AbstractEdge;
import model.AbstractNode;
import model.GraphElement;
import util.commands.AddDeleteEdgeCommand;
import util.commands.AddDeleteNodeCommand;
import util.commands.CompoundCommand;
import view.AbstractEdgeView;
import view.AbstractNodeView;
import controller.MainController.*;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by marcusisaksson on 2016-06-20.
 */
public class CopyPasteController {

    Pane aDrawingPane;
    MainController mainController;

    //Copy nodes logic
    ArrayList<AbstractNode> currentlyCopiedNodes = new ArrayList<>();
    ArrayList<AbstractEdge> currentlyCopiedEdges = new ArrayList<>();
    HashMap<AbstractNode, double[]> copyDeltas = new HashMap<>();
    double[] copyPasteCoords;


    public CopyPasteController(Pane pDrawingPane, MainController mainController) {
        aDrawingPane = pDrawingPane;
        this.mainController = mainController;
    }

    //TODO Copy edges and sketches as well
    void copy(){
        currentlyCopiedNodes.clear();
        copyDeltas.clear();
        currentlyCopiedEdges.clear();

        for(AbstractNodeView nodeView : mainController.selectedNodes){
            currentlyCopiedNodes.add(mainController.nodeMap.get(nodeView));
        }
        for(AbstractEdgeView edgeView : mainController.selectedEdges){
            currentlyCopiedEdges.add(edgeView.getRefEdge());
        }
        setUpCopyCoords();
    }

    /**
     * Sets up relative coordinates for the nodes being copied
     */
    void setUpCopyCoords(){
        double currentClosestToCorner = Double.MAX_VALUE;
        AbstractNode closest = null;
        for(GraphElement element: currentlyCopiedNodes){
            if(element instanceof AbstractNode){
                if((element.getTranslateX() + element.getTranslateY()) < currentClosestToCorner){
                    currentClosestToCorner = element.getTranslateX() + element.getTranslateY();
                    closest = (AbstractNode) element;
                }
            }

        }

        for(AbstractNode node : currentlyCopiedNodes){
            if(node != closest){
                copyDeltas.put(node, new double[]{node.getTranslateX() - closest.getTranslateX(),
                        node.getTranslateY() - closest.getTranslateY()});
            } else {
                copyDeltas.put(node, new double[]{0,0});
            }
        }
    }

    //TODO Paste two times in a row
    void paste(){
        CompoundCommand command = new CompoundCommand();


        AbstractNode newStartNode = null;
        AbstractNode newEndNode = null;
        AbstractNodeView newStartNodeView = null;
        AbstractNodeView newEndNodeView = null;

        //If a node has several edges it will be found more than once in the currentlyCopiedEdges loop, so we put nodes
        //that are already copied in this map.
        HashMap<AbstractNode, AbstractNode> alreadyCopiedNodes = new HashMap<>();

        //Paste edges and their start and end nodes
        for(AbstractEdge oldEdge : currentlyCopiedEdges){
            for(AbstractNode node : currentlyCopiedNodes){
                if(node.equals(oldEdge.getStartNode())){
                    if(!alreadyCopiedNodes.containsKey(node)){ //If start node is not already copied
                        newStartNode = ((AbstractNode)oldEdge.getStartNode()).copy();
                        alreadyCopiedNodes.put(node, newStartNode);

                        mainController.getGraphModel().addNode(newStartNode);
                        newStartNode.setTranslateX(copyPasteCoords[0] + copyDeltas.get(node)[0]);
                        newStartNode.setTranslateY(copyPasteCoords[1] + copyDeltas.get(node)[1]);
                        newStartNodeView = mainController.createNodeView(newStartNode);
                        command.add(new AddDeleteNodeCommand(mainController, mainController.getGraphModel(), newStartNodeView, newStartNode, true));
                    } else {
                        newStartNode = alreadyCopiedNodes.get(node);
                    }

                } else if (node.equals(oldEdge.getEndNode())){
                    if (!alreadyCopiedNodes.containsKey(node)) { //If end node is not already copied
                        newEndNode = ((AbstractNode) oldEdge.getEndNode()).copy();
                        alreadyCopiedNodes.put(node, newEndNode);

                        mainController.getGraphModel().addNode(newEndNode);
                        newEndNode.setTranslateX(copyPasteCoords[0] + copyDeltas.get(node)[0]);
                        newEndNode.setTranslateY(copyPasteCoords[1] + copyDeltas.get(node)[1]);
                        newEndNodeView = mainController.createNodeView(newEndNode);
                        command.add(new AddDeleteNodeCommand(mainController, mainController.getGraphModel(), newEndNodeView, newEndNode, true));
                    } else {
                        newEndNode = alreadyCopiedNodes.get(node);
                    }
                }
            }
            currentlyCopiedNodes.removeAll(alreadyCopiedNodes.keySet());
            AbstractEdge copy = (AbstractEdge)oldEdge.copy(newStartNode, newEndNode);
            mainController.getGraphModel().getAllEdges().add(copy);
            AbstractEdgeView newEdgeView = mainController.createEdgeView(copy, newStartNodeView, newEndNodeView);
            command.add(new AddDeleteEdgeCommand(mainController, newEdgeView, copy, true));
        }

        for (GraphElement old : currentlyCopiedNodes) {
            AbstractNode copy = ((AbstractNode)old).copy();
            mainController.getGraphModel().addNode(copy);
            copy.setTranslateX(copyPasteCoords[0] + copyDeltas.get(old)[0]);
            copy.setTranslateY(copyPasteCoords[1] + copyDeltas.get(old)[1]);
            AbstractNodeView newView = mainController.createNodeView(copy);
            command.add(new AddDeleteNodeCommand(mainController, mainController.getGraphModel(), newView, copy, true));

        }
        currentlyCopiedNodes.clear();
        currentlyCopiedEdges.clear();
        if(command.size() != 0){
            mainController.getUndoManager().add(command);
        }
    }
}
