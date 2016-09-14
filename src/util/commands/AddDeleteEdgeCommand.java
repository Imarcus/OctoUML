///*******************************************************************************
// * JetUML - A desktop application for fast UML diagramming.
// *
// * Copyright (C) 2016 by the contributors of the JetUML project.
// *
// * See: https://github.com/prmr/JetUML
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Public License for more details.
// *
// * You should have received a copy of the GNU General Public License
// * along with this program.  If not, see <http://www.gnu.org/licenses/>.
// *******************************************************************************/
package util.commands;


import controller.AbstractDiagramController;
import model.edges.AbstractEdge;
import model.edges.Edge;
import view.edges.AbstractEdgeView;

/**
 * Stores the addition/removal of a node from the graph.
 * @author EJBQ
 */
public class AddDeleteEdgeCommand implements Command
{
	private AbstractDiagramController aController;
    private AbstractEdgeView aEdgeView;
	private Edge aEdge;
	private boolean aAdding; //true for adding, false for deleting

	/**
	 * Creates the command.
	 * @param pEdge The edge to be added/deleted
	 * @param pAdding True when adding, false when deleting
	 */
	public AddDeleteEdgeCommand(AbstractDiagramController pController, AbstractEdgeView pEdgeView, AbstractEdge pEdge, boolean pAdding)
	{
		aController = pController;
        aEdgeView = pEdgeView;
		aEdge = pEdge;
		aAdding = pAdding;
	}

	/**
	 * Undoes the command and adds/deletes the edge.
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
	 * Performs the command and adds/deletes the edge.
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
		aController.deleteEdgeView(aEdgeView, null, true, false);
	}

	/**
	 * Adds the edge to the graph at the points in its start and end node properties.
	 */
	private void add()
	{
		aController.addEdgeView(aEdgeView);
	}

	public Edge getEdge() {
		return aEdge;
	}

	public boolean isAdding() {
		return aAdding;
	}
}
