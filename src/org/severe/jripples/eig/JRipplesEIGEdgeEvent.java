package org.severe.jripples.eig;

import org.eclipse.swt.events.TypedEvent;


/**
 * 
 * JRipplesEIGEdgeEvent class represents lifecycle and content events that happen with a particular {@link JRipplesEIGEdge}. That is,
 * creation, changes in From and To nodes this edge connects, mark and probability, and edge removal. 
 * 
 * @see JRipplesEIGEdge
 * @author Maksym Petrenko
 *
 */


public class JRipplesEIGEdgeEvent extends TypedEvent {


	
	private static final long serialVersionUID = 1131230984632238215L;

	/**
	 * Event constant for edge's mark changes. Will occur if  {@link JRipplesEIGEdge#setMark(String)} was called.
	 */
	public static final int EDGE_MARK_CHANGED = 1;
	
	/**
	 * Event constant for edge's probability changes. Will occur if {@link JRipplesEIGEdge#setProbability(String)} was called. 
	 */
	public static final int EDGE_PROBABILITY_CHANGED = 2;
	
	/**
	 * Event constant for {@link JRipplesEIGEdge} From Node changes. Will occured if {@link JRipplesEIGEdge#JRipplesEIGEdge(JRipplesEIGNode, JRipplesEIGNode)} constructor was called.
	 */
	public static final int EDGE_FROM_NODE_CHANGED = 4;
	
	/**
	 * Event constant for {@link JRipplesEIGEdge} To Node changes. Will occured if {@link JRipplesEIGEdge#JRipplesEIGEdge(JRipplesEIGNode, JRipplesEIGNode)} constructor was called.
	 */
	public static final int EDGE_TO_NODE_CHANGED = 8;
	
	
	/**
	 * Event constant for {@link JRipplesEIGEdge} creation. Will occured if {@link JRipplesEIG#addEdge(JRipplesEIGNode, JRipplesEIGNode)} was called. 
	 */
	public static final int EDGE_ADDED = 16;

	/**
	 * Event constant for {@link JRipplesEIGEdge} removal. Will occured if {@link JRipplesEIG#removeEdge(JRipplesEIGEdge)} was called or either Form Node or To Node was removed fromt the EIG (or their respective declaring class Nodes).
	 */
	public static final int EDGE_REMOVED = 32;
	
	/**
	 * Event constant for edge's count changes. Will occur if {@link JRipplesEIGEdge#setCount(Integer)} was called. 
	 */
	public static final int EDGE_COUNT_CHANGED = 64;

	private JRipplesEIGEdge edge;

	private int type = 0;

	
	/**
	 * @param edge
	 * The edge this event occured on
	 * @param type
	 * Type of the edge event, where type is one of the constants defined in {@link JRipplesEIGEdgeEvent}
	 */
	public JRipplesEIGEdgeEvent(JRipplesEIGEdge edge, int type) {
		super(edge);
		this.edge = edge;
		this.type = type;
		
	}

	/**
	 * Returns the edge this event occured on.
	 * @return
	 * The edge this event occured on
	 * 
	 */
	public JRipplesEIGEdge getEdge() {
		return edge;
	}

	/**
	 * Returns the type of the edge event, where type is one of the constants defined in {@link JRipplesEIGEdgeEvent}.
	 * @return
	 * Type of the edge event, where type is one of the constants defined in {@link JRipplesEIGEdgeEvent} 
	 */
	public int getEventType() {
		return type;
	}

}
