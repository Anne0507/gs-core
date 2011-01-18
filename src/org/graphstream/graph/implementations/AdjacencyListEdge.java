/*
 * Copyright 2006 - 2011 
 *     Julien Baudry	<julien.baudry@graphstream-project.org>
 *     Antoine Dutot	<antoine.dutot@graphstream-project.org>
 *     Yoann Pigné		<yoann.pigne@graphstream-project.org>
 *     Guilhelm Savin	<guilhelm.savin@graphstream-project.org>
 * 
 * This file is part of GraphStream <http://graphstream-project.org>.
 * 
 * GraphStream is a library whose purpose is to handle static or dynamic
 * graph, create them from scratch, file or any source and display them.
 * 
 * This program is free software distributed under the terms of two licenses, the
 * CeCILL-C license that fits European law, and the GNU Lesser General Public
 * License. You can  use, modify and/ or redistribute the software under the terms
 * of the CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
 * URL <http://www.cecill.info> or under the terms of the GNU LGPL as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C and LGPL licenses and that you accept their terms.
 */
package org.graphstream.graph.implementations;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.stream.SourceBase.ElementType;

/**
 * <p>
 * A lightweight edge class intended to allow the construction of big graphs
 * (millions of elements).
 * </p>
 * <p>
 * The main purpose here is to minimize memory consumption even if the
 * management of such a graph implies more CPU consuming. See the
 * <code>complexity</code> tags on each method so as to figure out the impact on
 * the CPU.
 * </p>
 * 
 * @since July 12 2007
 * 
 */
public class AdjacencyListEdge extends AbstractElement implements Edge {

	/**
	 * The source node of this link.
	 */
	AdjacencyListNode n0;

	/**
	 * The destination node of this link.
	 */
	AdjacencyListNode n1;

	/**
	 * Decides either the edge is directed or not.
	 */
	boolean directed = false;

	/**
	 * Construct a new edge with an unique identifier, a source node, a
	 * destination node and a boolean value deciding whether or not the edge is
	 * directed.
	 * 
	 * @param id
	 *            Unique identifier of this edge.
	 * @param src
	 *            Source node of this edge.
	 * @param dst
	 *            Destination node of this edge.
	 * @param directed
	 *            Boolean indicating whether or not the edge is directed.
	 */
	protected AdjacencyListEdge(String id, Node src, Node dst, boolean directed) {
		super(id);

		if ((src != null && !(src instanceof AdjacencyListNode))
				|| (dst != null && !(dst instanceof AdjacencyListNode)))
			throw new ClassCastException("AdjacencyListEdge needs an "
					+ "extended class AdjacencyListNode");

		this.n0 = (AdjacencyListNode) src;
		this.n1 = (AdjacencyListNode) dst;
		this.directed = directed;
	}

	@Override
	protected String myGraphId() {
		return n0.graph.getId();
	}

	@Override
	protected long newEvent() {
		return ((AdjacencyListGraph) n0.graph).newEvent();
	}
	
	@Override
	protected boolean nullAttributesAreErrors() {
		return n0.graph.nullAttributesAreErrors();
	}

	@SuppressWarnings("unchecked")
	public <T extends Node> T getNode0() {
		return (T) n0;
	}

	@SuppressWarnings("unchecked")
	public <T extends Node> T getNode1() {
		return (T) n1;
	}

	@SuppressWarnings("unchecked")
	public <T extends Node> T getOpposite(T node) {
		if (node == n0)
			return (T) n1;
		else if (node == n1)
			return (T) n0;
		else
			return null;
	}

	@SuppressWarnings("unchecked")
	public <T extends Node> T getSourceNode() {
		return (T) n0;
	}

	@SuppressWarnings("unchecked")
	public <T extends Node> T getTargetNode() {
		return (T) n1;
	}

	public boolean isDirected() {
		return directed;
	}
	
	public boolean isLoop() {
		return (n0 == n1);
	}

	public void setDirected(boolean on) {
		// XXX Bug, the new edge created in the event stream will loose all its
		// attributes.
		((AdjacencyListGraph) n0.graph).listeners.sendEdgeRemoved(myGraphId(),
				newEvent(), getId());
		this.directed = on;
		((AdjacencyListGraph) n0.graph).listeners.sendEdgeAdded(myGraphId(),
				newEvent(), getId(), n0.getId(), n1.getId(), directed);
	}

	public void switchDirection() {
		// XXX Bug, the new edge create in the event stream will loose all its
		// attributes.
		((AdjacencyListGraph) n0.graph).listeners.sendEdgeRemoved(myGraphId(),
				newEvent(), getId());
		AdjacencyListNode n = n0;
		n0 = n1;
		n1 = n;
		((AdjacencyListGraph) n0.graph).listeners.sendEdgeAdded(myGraphId(),
				newEvent(), getId(), n0.getId(), n1.getId(), directed);
	}

	@Override
	protected void attributeChanged(String sourceId, long timeId,
			String attribute, AttributeChangeEvent event, Object oldValue,
			Object newValue) {
		if (n0 != null)
			((AdjacencyListGraph) n0.graph).listeners
					.sendAttributeChangedEvent(sourceId, timeId, getId(),
							ElementType.EDGE, attribute, event, oldValue,
							newValue);
	}
}