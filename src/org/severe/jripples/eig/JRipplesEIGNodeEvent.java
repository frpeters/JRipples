package org.severe.jripples.eig;

import org.eclipse.swt.events.TypedEvent;


/**
 * 
 * JRipplesEIGNodeEvent class represents lifecycle and content events that happen with a particular {@link JRipplesEIGNode}. That is,
 * creation, changes in underlying member, mark and probability, and node removal. 
 * 
 * @see JRipplesEIGNode
 * @author Maksym Petrenko
 *
 */

public class JRipplesEIGNodeEvent extends TypedEvent {

	private static final long serialVersionUID = 3641606803166267260L;

	/**
	 *  Event constant for {@link JRipplesEIGNode} mark changes. Will occur if  {@link JRipplesEIGNode#setMark(String)} was called.
	 */
	public static final int NODE_MARK_CHANGED = 1;
	
	/**
	 * Event constant for {@link JRipplesEIGNode} probability changes. Will occur if {@link JRipplesEIGNode#setProbability(String)} was called. 
	 */
	public static final int NODE_PROBABILITY_CHANGED = 2;
	
	/**
	 * Event constant for {@link JRipplesEIGNode} underlying IMember changes. Will occured if {@link JRipplesEIGNode#JRipplesEIGNode(org.eclipse.jdt.core.IMember)} constructor was called.
	 */
	public static final int NODE_MEMBER_CHANGED = 4;

	/**
	 * Event constant for {@link JRipplesEIGNode} creation. Will occured if {@link JRipplesEIG#addNode(org.eclipse.jdt.core.IMember)} was called. 
	 */
	public static final int NODE_ADDED = 8;

	/**
	 * Event constant for {@link JRipplesEIGNode} removal. Will occured if {@link JRipplesEIG#removeNode(JRipplesEIGNode)} was called, or if a Node of declaring class of this node was removed.
	 */
	public static final int NODE_REMOVED = 16;

	private JRipplesEIGNode node;

	private int type = 0;

	/**
	 * @param node
	 * The node this event occured on
	 * @param type
	 * Type of the node event, where type is one of the constants defined in {@link JRipplesEIGNodeEvent}
	 */
	public JRipplesEIGNodeEvent(JRipplesEIGNode node, int type) {
		super(node);
		this.node = node;
		this.type = type;
	}

	/**
	 * Returns the node this event occured on.
	 * @return
	 * The node this event occured on
	 */
	public JRipplesEIGNode getNode() {
		return node;
	}

	/**
	 * Returns a type of the node event, where type is one of the constants defined in {@link JRipplesEIGNodeEvent}.
	 * @return
	 * Type of the node event, where type is one of the constants defined in {@link JRipplesEIGNodeEvent}
	 */
	public int getEventType() {
		return type;
	}

}
