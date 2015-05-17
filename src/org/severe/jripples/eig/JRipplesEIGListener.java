package org.severe.jripples.eig;
/**
 * 
 * JRipplesEIGListener interface should be implemented by classes who wish to receive updates on lifecyle and content events of nodes and edges.
 * 
 * @see JRipplesEIGEvent 
 * @see JRipplesEIG#addJRipplesEIGListener(JRipplesEIGListener)
 * @see JRipplesEIG#removeJRipplesEIGListener(JRipplesEIGListener)
 * @see JRipplesEIGNodeEvent
 * @see JRipplesEIGEdgeEvent
 * @author Maksym Petrenko
 *
 */

public interface JRipplesEIGListener {
	/**
	 * The method is called upon events in the JRipples EIG
	 * @param evt
	 * 	 an event that occured in the JRipples EIG
	 */
	void JRipplesEIGChanged(JRipplesEIGEvent evt);
}
