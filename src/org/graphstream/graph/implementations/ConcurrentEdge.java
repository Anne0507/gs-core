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
 * An implementation of an Edge with multi-thread capabilities.
 * </p>
 * <p>
 * It is similar to the
 * {@link org.graphstream.graph.implementations.AdjacencyListEdge} class, but
 * with thread-safe data structures.
 * </p>
 * <p>
 * Time and memory complexity is comparable to the values given in
 * {@link org.graphstream.graph.implementations.AdjacencyListEdge}. Consider
 * some time overhead due to the thread synchronization machinery.
 * </p>
 * 
 * @see org.graphstream.graph.implementations.AdjacencyListEdge
 */
public class ConcurrentEdge extends AbstractElement implements Edge {

	ConcurrentNode n0;

	ConcurrentNode n1;

	boolean directed = false;

	/**
	 * Constructor for a ConcurrentEdge with specified id nodes and information
	 * about the direction.
	 * 
	 * @param id
	 *            Unique identifier.
	 * @param src
	 *            Source node.
	 * @param dst
	 *            Target Node.
	 * @param directed
	 *            Say whether the edge is directed or not.
	 */
	protected ConcurrentEdge(String id, Node src, Node dst, boolean directed) {
		super(id);

		if ((src != null && !(src instanceof ConcurrentNode))
				|| (dst != null && !(dst instanceof ConcurrentNode)))
			throw new ClassCastException("ConcurrentEdge needs an "
					+ "extended class ConcurrentNode");

		this.n0 = (ConcurrentNode) src;
		this.n1 = (ConcurrentNode) dst;
		this.directed = directed;
	}

	@Override
	protected String myGraphId() {
		return n0.graph.getId();
	}

	@Override
	protected long newEvent() {
		return ((ConcurrentGraph) n0.graph).newEvent();
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

	@Override
	protected void attributeChanged(String sourceId, long timeId,
			String attribute, AttributeChangeEvent event, Object oldValue,
			Object newValue) {
		if (n0 != null)
			((ConcurrentGraph) n0.graph).listeners.sendAttributeChangedEvent(
					sourceId, timeId, getId(), ElementType.EDGE, attribute,
					event, oldValue, newValue);
	}
}