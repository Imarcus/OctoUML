/*******************************************************************************
 * JetUML - A desktop application for fast UML diagramming.
 *
 * Copyright (C) 2016 by the contributors of the JetUML project.
 *
 * See: https://github.com/prmr/JetUML
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package util.commands;

import controller.AbstractDiagramController;
import model.nodes.AbstractNode;
import model.Graph;
import view.nodes.AbstractNodeView;

/**
 * Stores the addition/removal of a node from the graph.
 * @author EJBQ
 */
public class AddDeleteNodeCommand implements Command
{
	private AbstractDiagramController aController;
	private Graph aGraph;
	private AbstractNodeView aNodeView;
	private AbstractNode aNode;
	private boolean aAdding; //true for adding, false for deleting
	
	/**
	 * Creates the command.
	 * @param pNode The node to be added/deleted
	 * @param pAdding True when adding, false when deleting
	 */
	public AddDeleteNodeCommand(AbstractDiagramController pController,
								Graph pGraph, AbstractNodeView pNodeView, AbstractNode pNode, boolean pAdding)
	{
		aController = pController;
		aGraph = pGraph;
		aNodeView = pNodeView;
		aNode = pNode;
		aAdding = pAdding;
	}

	/**
	 * Undoes the command and adds/deletes the node.
	 */
	public void undo() 
	{
		if(aAdding)
		{
			delete();
		}
		else
		{
			add();
		}
	}

	/**
	 * Performs the command and adds/deletes the node.
	 */
	public void execute() 
	{
		if(aAdding)
		{
			add();
		}
		else
		{
			delete();
		}
	}

	/**
	 * Removes the node from the graph.
	 */
	private void delete() 
	{
		aGraph.removeNode(aNode, false); //TODO needed?
		aController.deleteNode(aNodeView, null, true, false);
	}
	
	/**
	 * Adds the edge to the graph at the point in its properties.
	 */
	private void add() 
	{
		aGraph.addNode(aNode, false);
		aController.addNodeView(aNodeView, aNode);
		aController.getGraphController().sketchesToFront();
	}

	public AbstractNode getNode() {
		return aNode;
	}

	public boolean isAdding() {
		return aAdding;
	}
	
}
