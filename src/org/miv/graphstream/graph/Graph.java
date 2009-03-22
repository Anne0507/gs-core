/*
 * This file is part of GraphStream.
 * 
 * GraphStream is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * GraphStream is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with GraphStream.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2006 - 2009
 * 	Julien Baudry
 * 	Antoine Dutot
 * 	Yoann Pigné
 * 	Guilhelm Savin
 */

package org.miv.graphstream.graph; 

import java.io.IOException;
import java.util.Iterator;

import org.miv.graphstream.io.GraphParseException;
import org.miv.graphstream.io.GraphReader;
import org.miv.graphstream.io.GraphWriter;
import org.miv.graphstream.ui.GraphViewerRemote;
import org.miv.util.NotFoundException;
import org.miv.util.SingletonException;

/**
 * An Interface that advises general purpose methods for handling graphs.
 * 
 * <p>
 * This interface is the main interface of GraphStream. It defines the services
 * provided by a graph structure. Graphs implementations must at least implement
 * this interface (but are free to provide more services). 
 * </p>
 * 
 * <h3>Important</h3>
 * <p>
 * Implementing classes should indicate the complexity of their implementation for each method.
 * </p>
 */
public interface Graph extends Element, Iterable<Node>
{
	/**
	 * Get a node by its identifier.
	 * @param id Identifier of the node to find.
	 * @return The searched node or null if not found.
	 */
	public Node getNode( String id );

	/**
	 * Get an edge by its identifier.
	 * @param id Identifier of the edge to find.
	 * @return The searched edge or null if not found.
	 */
	public Edge getEdge( String id );

	/**
	 * Number of nodes in this graph.
	 * @return The number of nodes.
	 */
	public int getNodeCount();

	/**
	 * Number of edges in this graph.
	 * @return The number of edges.
	 */
	public int getEdgeCount();

	/**
	 * Iterator on the set of node, in random order.
	 * @return The iterator.
	 */
	public Iterator<? extends Node> getNodeIterator();

	/**
	 * Iterator on the set of edges, in random order..
	 * @return The iterator.
	 */
	public Iterator<? extends Edge> getEdgeIterator();

	/**
	 * Set of nodes usable in a for-each instruction.
	 * @return An "iterable" view of the set of nodes.
	 * @see #getNodeIterator()
	 */
	public Iterable<? extends Node> getNodeSet();

	/**
	 * Set of edges usable in a for-each instruction.
	 * @return An "iterable" view of the set of edges.
	 * @see #getEdgeIterator()
	 */
	public Iterable<? extends Edge> getEdgeSet();

	/**
	 * The factory used to create node instances.
	 * The factory can be changed to refine the node class generated for this graph.
	 * @see #setNodeFactory(NodeFactory)
	 * @see #edgeFactory()
	 */
	NodeFactory nodeFactory();
	
	/**
	 * Set the node factory used to create nodes.
	 * @param nf the new NodeFactory
	 */
	void setNodeFactory( NodeFactory nf );
	
	/**
	 * The factory used to create edge instances.
	 * The factory can be changed to refine the edge class generated for this graph.
	 * @see #setEdgeFactory(EdgeFactory)
	 * @see #nodeFactory()
	 */
	EdgeFactory edgeFactory();
	
	/**
	 * Set the edge factory used to create edges.
	 * @param ef the new EdgeFactory
	 */
	void setEdgeFactory( EdgeFactory ef );
	
	/**
	 * Empties the graph completely by removing any references to nodes or edges.
	 * Every attribute is also removed. However, listeners are kept.
	 * @see #clearListeners()
	 */
	public void clear();

	/**
	 * Remove any reference to listeners.
	 * @see #clear()
	 */
	public void clearListeners();

	/**
	 * Is strict checking enabled?. If strict checking is enabled the graph
	 * checks for name space conflicts (e.g. insertion of two nodes with the
	 * same name), removal of non-existing elements, use of non existing
	 * elements (create an edge between two non existing nodes). Graph implementations
	 * are free to respect strict checking or not.
	 * @return True if enabled.
	 */
	public boolean isStrictCheckingEnabled();

	/**
	 * Is the automatic creation of missing elements enabled?. If enabled, when
	 * an edge is created and one or two of its nodes are not already present
	 * in the graph, the nodes are automatically created.
	 * @return True if enabled.
	 */
	public boolean isAutoCreationEnabled();

	/**
	 * Enable or disable strict checking.
	 * @see #isStrictCheckingEnabled()
	 * @param on True or false.
	 */
	public void setStrictChecking( boolean on );

	/**
	 * Enable or disable the automatic creation of missing elements.
	 * @see #isAutoCreationEnabled()
	 * @param on True or false.
	 */
	public void setAutoCreate( boolean on );

	/**
	 * Add a node in the graph. This acts as a factory, creating the node
	 * instance automatically (and eventually using the node factory provided).
	 * An event is generated toward the listeners.
	 * If strict checking is enabled, and a node already exists with this
	 * identifier, a singleton exception is raised. Else the error is silently
	 * ignored and the already existing node is returned.
	 * @param id Arbitrary and unique string identifying the node.
	 * @return The created node (or the already existing node).
	 * @throws SingletonException If the identifier is already used.
	 */
	public Node addNode( String id ) throws SingletonException;

	/**
	 * Remove the node using its identifier. An event is generated toward the
	 * listeners. Note that removing a node may remove all edges it is connected
	 * to. In this case corresponding events will also be generated toward the
	 * listeners.
	 * @param id The unique identifier of the node to remove.
	 * @return The removed node, if strict checking is disabled, it can return
	 * 	       null if the node to remove does not exist.
	 * @complexity O(1)
	 * @throws NotFoundException If no node matches the given identifier.
	 */
	public Node removeNode( String id ) throws NotFoundException;

	/**
	 * Add an undirected edge between nodes. An event is sent toward the
	 * listeners. If strict checking is enabled and at least one of the two
	 * given nodes do not exist, a "not found" exception is raised. Else if the
	 * auto-creation feature is disabled, the error is silently ignored, and
	 * null is returned. If the auto-creation feature is enabled (see
	 * {@link #setAutoCreate(boolean)}) and one or two of the given nodes do not
	 * exist, they are automatically created.
	 * 
	 * @param id Unique an arbitrary string identifying the edge.
	 * @param node1 The first node identifier.
	 * @param node2 The second node identifier.
	 * 
	 * @return The newly created edge (this can return null, if strict checking
	 *         is disabled, auto-creation disabled, and one or two of the given
	 *         nodes do not exist).
	 * @throws SingletonException If an edge already exist between 'from' and 'to',
	 *         strict checking is enabled and the graph is not a multi-graph.
	 * @throws NotFoundException If strict checking is enabled, and the 'from'
	 *         or 'to' node is not registered in the graph.
	 */
	public Edge addEdge( String id, String node1, String node2 ) throws SingletonException, NotFoundException;

	/**
	 * Like {@link #addEdge(String, String, String)}, but this edge can be
	 * directed between the two given nodes. If directed, the edge goes in the
	 * 'from' -&gt; 'to' direction. An event is sent toward the listeners.
	 * 
	 * @param id Unique an arbitrary string identifying the node.
	 * @param from The source node identifier.
	 * @param to The target node identifier.
	 * @param directed Is the edge directed?.
	 * 
	 * @return The newly created edge (this can return null, if strict checking
	 *         is disabled, auto-creation disabled, and one or two of the given
	 *         nodes do not exist).
	 * @throws SingletonException If an edge already exist between 'from' and 'to',
	 *         strict checking is enabled, and the graph is not a multi-graph.
	 * @throws NotFoundException If strict checking is enabled, and the 'from'
	 *         or 'to' node is not registered in the graph.
	 */
	public Edge addEdge( String id, String from, String to, boolean directed ) throws SingletonException, NotFoundException;

	/**
	 * Remove an edge given the identifier of its two linked nodes. If the edge
	 * is directed it is removed only if its source and destination nodes are
	 * identified by 'from' and 'to' respectively. If the graph is a multi graph
	 * and there are several edges between the two nodes, one of the edge at
	 * random is removed. An event is sent toward the
	 * listeners. If strict checking is enabled and at least one of the two
	 * given nodes does not exist, a not found exception is raised. Else the
	 * error is silently ignored, and null is returned.
	 * @param from The origin node identifier to select the edge.
	 * @param to The destination node identifier to select the edge.
	 * @return The removed edge, or null if strict checking is disabled and
	 *         at least one of the two given nodes does not exist.
	 * @throws NotFoundException If the 'from' or 'to' node is not registered in
	 *         the graph and strict checking is enabled.
	 */
	public Edge removeEdge( String from, String to ) throws NotFoundException;

	/**
	 * Remove the edge knowing its identifier. An event is sent toward the
	 * listeners. If strict checking is enabled and the edge does not exist,
	 * a not found exception is raised. Else the error is silently ignored and
	 * null is returned.
	 * @param id Identifier of the edge to remove.
	 * @return The removed edge, or null if strict checking is disabled and
	 *         the edge does not exist.
	 * @throws NotFoundException If no edge matches the identifier and strict
	 *         checking is enabled.
	 */
	public Edge removeEdge( String id ) throws NotFoundException;

	/**
	 * <p>
	 * Since dynamic graphs are based on discrete event modifications, the notion of step is defined
	 * to simulate elapsed time between events. So a step is a event that occure in the graph, it
	 * does not modify it but it gives a kind of timestamp that allow the tracking of the progress
	 * of the graph over the time.
	 * </p>
	 * <p>
	 * This kind of event is useful for dynamic algorithms that listen to the dynamic graph and need
	 * to measure the time in the graph's evolution.
	 * </p>
	 * 
	 * @param time
	 *            A numerical value that may give a timestamp to track the evolution of the graph
	 *            over the time.
	 */
	public void stepBegins( double time );
	
	/**
	 * Add a listener for all events concerning this graph.
	 * @param listener The listener to register.
	 * @see #addGraphAttributesListener(GraphAttributesListener)
	 * @see #addGraphElementsListener(GraphElementsListener)
	 */
	public void addGraphListener( GraphListener listener );

	/**
	 * Remove the given listener from the list of object interested in all events on
	 * this graph. This method fails silently if listener is not registered in
	 * this graph.
	 * @param listener The listener to remove.
	 * @see #addGraphAttributesListener(GraphAttributesListener)
	 * @see #addGraphElementsListener(GraphElementsListener)
	 */
	public void removeGraphListener( GraphListener listener );

	/**
	 * Add a listener only on the attributes (variables stored on the graph, nodes and edges) of
	 * the graph. 
	 * @param listener The listener to register.
	 * @see #addGraphElementsListener(GraphElementsListener)
	 */
	public void addGraphAttributesListener( GraphAttributesListener listener );
	
	/**
	 * Add a listener only on the elements (nodes and edges) of the graph. 
	 * @param listener The listener to register.
	 * @see #addGraphAttributesListener(GraphAttributesListener)
	 */
	public void addGraphElementsListener( GraphElementsListener listener );
	
	/**
	 * Remove the given listener from the list of objects interested in attributes events on this
	 * graph. This method fails silently if listener is not registered in this graph.
	 * @param listener The listener to remove.
	 */
	public void removeGraphAttributesListener( GraphAttributesListener listener);
	
	/**
	 * Remove the given listener from the list of objects interested in elements events on this
	 * graph. This method fails silently if listener is not registered in this graph.
	 * @param listener The listener to remove.
	 */
	public void removeGraphElementsListener( GraphElementsListener listener );
	
	/**
	 * Returns an "iterable" of {@link GraphAttributesListener} objects registered to this graph.
	 * @return the set of {@link GraphAttributesListener} under the form of an iterable object.
	 */
	public Iterable<GraphAttributesListener> getGraphAttributesListeners();
	
	/**
	 * Returns an "iterable" of {@link GraphElementsListener} objects registered to this graph.
	 * @return the list of {@link GraphElementsListener} under the form of an iterable object.
	 */
	public Iterable<GraphElementsListener> getGraphElementsListeners();
	
	/**
	 * Utility method to read a graph. This method tries to identify the graph
	 * format by itself and instantiates the corresponding reader automatically.
	 * If this process fails, a NotFoundException is raised.
	 * @param filename The graph filename.
	 * @throws NotFoundException If the file cannot be found or if the format is not recognised.
	 * @throws GraphParseException If there is a parsing error while reading the file.
	 * @throws IOException If an input output error occurs during the graph reading.
	 * @see org.miv.graphstream.io.GraphReader
	 */
	public void read( String filename ) throws IOException, GraphParseException, NotFoundException;

	/**
	 * Utility method to read a graph using the given reader.
	 * @param reader An appropriate reader for the filename.
	 * @param filename The graph filename.
	 * @throws NotFoundException If the file cannot be found or if the format is not recognised.
	 * @throws GraphParseException If there is a parsing error while reading the file.
	 * @throws IOException If an input/output error occurs during the graph reading.
	 * @see org.miv.graphstream.io.GraphReader
	 */
	public void read( GraphReader reader, String filename ) throws IOException, GraphParseException;

	/**
	 * Utility method to write a graph in DGS format to a file.
	 * @param filename The file that will contain the saved graph.
	 * @throws IOException If an input/output error occurs during the graph writing.
	 * @see org.miv.graphstream.io.GraphWriter
	 */
	public void write( String filename ) throws IOException;

	/**
	 * Utility method to write a graph in the chosen format to a file.
	 * @param writer The output format to use.
	 * @param filename THe file that will contain the saved graph.
	 * @throws IOException If an input/output error occurs during the graph writing.
	 * @see org.miv.graphstream.io.GraphWriter
	 */
	public void write( GraphWriter writer, String filename ) throws IOException;

	/**
	 * Utility method to read a map of node identifiers to 3D coordinates and
	 * store it in the graph. This method read a "position" file and put
	 * "x", "y" and "z" attributes on nodes of the graph according on what is
	 * read. The position file associates a node identifier with coordinates. It
	 * is possible that the given file do not map each node of the current graph.
	 * In this case, the number of non-mapped node is returned.
	 * The format for the position file is:
	 * <ul>
	 * 		<li>each line contains informations for one unique node;</li>
	 * 		<li>a line begins by the node identifier followed by ":";</li>
	 * 		<li>the ":" are followed by three numbers separated by spaces;</li>
	 * 		<li>no other information is accepted on a line.</li>
	 * </ul>
	 * @param posFileName Name of the position file.
	 * @return The number of nodes that where not mapped to coordinates.
	 * @throws IOException If an error occurred during the position file reading.
	 */
	// TODO : this is not the place for this !
	public int readPositionFile( String posFileName ) throws IOException;

	/**
	 * Utility method that create a new graph viewer, and register the graph in
	 * it. Notice that this method is a quick way to see a graph, and only this.
	 * It can be used to prototype a program, but may be limited. This method
	 * automatically launch a graph layout algorithm in its own thread to
	 * compute best node positions.
	 * @see org.miv.graphstream.ui.GraphViewerRemote
	 * @see #display(boolean)
	 * @return a graph viewer remote that allows to command the viewer (it is a remote since the
	 *         viewer often run in another thread).
	 */
	public GraphViewerRemote display();

	/**
	 * Utility method that create a new graph viewer, and register the graph in
	 * it. Notice that this method is a quick way to see a graph, and only this.
	 * It can be used to prototype a program, but is very limited.
	 * @param autoLayout If true a layout algorithm is launched in its own
	 *        thread to compute best node positions.
	 * @see org.miv.graphstream.ui.GraphViewerRemote
	 * @see #display()
	 * @return a graph viewer remote that allows to command the viewer (it is a remote since the
	 *         viewer often run in another thread).
	 */
	public GraphViewerRemote display( boolean autoLayout );
}