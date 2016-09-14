package controller;

import javafx.scene.layout.Pane;
import model.edges.AbstractEdge;
import model.nodes.AbstractNode;
import model.GraphElement;
import util.commands.AddDeleteEdgeCommand;
import util.commands.AddDeleteNodeCommand;
import util.commands.CompoundCommand;
import view.edges.AbstractEdgeView;
import view.nodes.AbstractNodeView;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Used by MainController for handling the copying and pasting of elements.
 */
public class CopyPasteController {

    Pane aDrawingPane;
    AbstractDiagramController diagramController;

    //Copy nodes logic
    ArrayList<AbstractNode> currentlyCopiedNodes = new ArrayList<>();
    ArrayList<AbstractEdge> currentlyCopiedEdges = new ArrayList<>();
    HashMap<AbstractNode, double[]> copyDeltas = new HashMap<>();
    double[] copyPasteCoords;


    public CopyPasteController(Pane pDrawingPane, AbstractDiagramController diagramController) {
        aDrawingPane = pDrawingPane;
        this.diagramController = diagramController;
    }

    //TODO Copy edges and sketches as well
    void copy(){
        currentlyCopiedNodes.clear();
        copyDeltas.clear();
        currentlyCopiedEdges.clear();

        for(AbstractNodeView nodeView : diagramController.selectedNodes){
            currentlyCopiedNodes.add(diagramController.nodeMap.get(nodeView));
        }
        for(AbstractEdgeView edgeView : diagramController.selectedEdges){
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

                        diagramController.getGraphModel().addNode(newStartNode, false);
                        newStartNode.setTranslateX(copyPasteCoords[0] + copyDeltas.get(node)[0]);
                        newStartNode.setTranslateY(copyPasteCoords[1] + copyDeltas.get(node)[1]);
                        newStartNodeView = diagramController.createNodeView(newStartNode, false);
                        command.add(new AddDeleteNodeCommand(diagramController, diagramController.getGraphModel(), newStartNodeView, newStartNode, true));
                    } else {
                        newStartNode = alreadyCopiedNodes.get(node);
                    }

                } else if (node.equals(oldEdge.getEndNode())){
                    if (!alreadyCopiedNodes.containsKey(node)) { //If end node is not already copied
                        newEndNode = ((AbstractNode) oldEdge.getEndNode()).copy();
                        alreadyCopiedNodes.put(node, newEndNode);

                        diagramController.getGraphModel().addNode(newEndNode, false);
                        newEndNode.setTranslateX(copyPasteCoords[0] + copyDeltas.get(node)[0]);
                        newEndNode.setTranslateY(copyPasteCoords[1] + copyDeltas.get(node)[1]);
                        newEndNodeView = diagramController.createNodeView(newEndNode, false);
                        command.add(new AddDeleteNodeCommand(diagramController, diagramController.getGraphModel(), newEndNodeView, newEndNode, true));
                    } else {
                        newEndNode = alreadyCopiedNodes.get(node);
                    }
                }
            }
            currentlyCopiedNodes.removeAll(alreadyCopiedNodes.keySet());
            AbstractEdge copy = (AbstractEdge)oldEdge.copy(newStartNode, newEndNode);
            diagramController.getGraphModel().getAllEdges().add(copy);
            AbstractEdgeView newEdgeView = diagramController.createEdgeView(copy, newStartNodeView, newEndNodeView);
            command.add(new AddDeleteEdgeCommand(diagramController, newEdgeView, copy, true));
        }

        for (GraphElement old : currentlyCopiedNodes) {
            AbstractNode copy = ((AbstractNode)old).copy();
            diagramController.getGraphModel().addNode(copy, false);
            copy.setTranslateX(copyPasteCoords[0] + copyDeltas.get(old)[0]);
            copy.setTranslateY(copyPasteCoords[1] + copyDeltas.get(old)[1]);
            AbstractNodeView newView = diagramController.createNodeView(copy, false);
            command.add(new AddDeleteNodeCommand(diagramController, diagramController.getGraphModel(), newView, copy, true));

        }
        currentlyCopiedNodes.clear();
        currentlyCopiedEdges.clear();
        if(command.size() != 0){
            diagramController.getUndoManager().add(command);
        }
    }





}
