package org.severe.jripples.eig;

import org.eclipse.swt.events.TypedEvent;
import java.util.ArrayList;


/**
 * JRipplesEIGEvent represent an {@link JRipplesEIG} event that encapsulates various events of nodes and edges lifecyle, and content.
 * Event may contain any number of {@link JRipplesEIGNodeEvent} and {@link JRipplesEIGEdgeEvent} events of any particular type, that happened in the same time (i.e. between calls of
 *  {@link JRipplesEIG#doLock(Object)} and {@link JRipplesEIG#doUnLock(Object)} methods).
 * @author Maksym Petrenko
 *
 */
public class JRipplesEIGEvent extends TypedEvent {

	
	private static final long serialVersionUID = -8395520734747078071L;

	private JRipplesEIG EIG;
	
	private JRipplesEIGNodeEvent[] nodeEvents;
	private JRipplesEIGEdgeEvent[] edgeEvents; 
	
	
	protected JRipplesEIGEvent(JRipplesEIG EIG,  JRipplesEIGNodeEvent[] nodeEvents, JRipplesEIGEdgeEvent[] edgeEvents ) {
		super(EIG);
		this.EIG = EIG;
		
		if (nodeEvents!=null) this.nodeEvents=nodeEvents;
			else this.nodeEvents=new JRipplesEIGNodeEvent[0];
		
		if (edgeEvents!=null) this.edgeEvents=edgeEvents;
			else this.edgeEvents=new JRipplesEIGEdgeEvent[0];
	}
	
	
	/**
	 * Returns {@link JRipplesEIG} object in which this event happened. As JRipplesEIG stores its data in a static way, this method is likely to have no specific use.
	 * @return
	 * {@link JRipplesEIG} object in which this event happened. As JRipplesEIG stores its data in a static way, this method is likely to have no specific use.
	 */
	protected JRipplesEIG getEIG() {
		return EIG;
	}
	
	/**
	 * Tells whether the event has any node events.
	 * @return
	 * 	<code>true</code> if this event has any node events, <code>false</code> otherwise 
	 */
	public boolean hasNodeEvents() {
		return (nodeEvents.length>0);
	}
	
	/**
	 * Tells whether the event has any edge events.
	 * @return
	 * 	<code>true</code> if this event has any edge events, <code>false</code> otherwise 
	 */
	public boolean hasEdgeEvents() {
		return (edgeEvents.length>0);
	}
	
	/**
	 * Returns the array of all node events related to this event.
	 * @return
	 * 	Array of all node events related to this event. If there is no node events registered with this event, empty array will be returned. 
	 */
	public JRipplesEIGNodeEvent[] getNodeEvents() {
		return nodeEvents;
	}
	
	/**
	 * Returns specific node events.
	 * @param types
	 * 	desired types of event, where types are one or more of constants defined in {@link JRipplesEIGNodeEvent}
	 * @return
	 * 	Array of node events of the requested types related to this event. If there is no node events of requested types registered with this event, empty array will be returned.
	 */

	
	public JRipplesEIGNodeEvent[] getNodeTypedEvents(int[] types) {
		ArrayList<JRipplesEIGNodeEvent> tmpEventsSet=new ArrayList<JRipplesEIGNodeEvent>();
		
		ArrayList<Integer> tmpTypesSet=new ArrayList<Integer>();
		for (int i=0;i<types.length;i++)
		{
			tmpTypesSet.add(Integer.valueOf(types[i]));
		}
		
		for (int i=0;i<nodeEvents.length;i++) {
			if (tmpTypesSet.contains(Integer.valueOf(nodeEvents[i].getEventType()))) 
					tmpEventsSet.add(nodeEvents[i]);
		}
		
		return tmpEventsSet.toArray(new JRipplesEIGNodeEvent[tmpEventsSet.size()]);
	}
	

	
	/**
	 * Returns specific node events.
	 * @param type
	 * 	desired type of event, where type is one of a constants defined in {@link JRipplesEIGNodeEvent}
	 * @return
	 * 	Array of node events of the requested type related to this event. If there is no node events of requested type registered with this event, empty array will be returned.
	 */
	public JRipplesEIGNodeEvent[] getNodeTypedEvents(int type) {
		ArrayList<JRipplesEIGNodeEvent> tmpEventsSet=new ArrayList<JRipplesEIGNodeEvent>();
		
		
		for (int i=0;i<nodeEvents.length;i++) {
			if (type==nodeEvents[i].getEventType()) 
					tmpEventsSet.add(nodeEvents[i]);
		}
		
		return tmpEventsSet.toArray(new JRipplesEIGNodeEvent[tmpEventsSet.size()]);
	}
	

	/**
	 * Returns all edge events.
	 * @return
	 * 	Array of all edge events related to this event. If there is no edge events registered with this event, empty array will be returned. 
	 */

	public JRipplesEIGEdgeEvent[] getEdgeEvents() {

		return edgeEvents;
	}
	
	/**
	 * Returns specific edge events.
	 * @param types
	 * 	desired types of event, where types are one or more of constants defined in {@link JRipplesEIGEdgeEvent}
	 * @return
	 * 	Array of edge events of the requested types related to this event. If there is no edge events of requested types registered with this event, empty array will be returned.
	 */


	
	public JRipplesEIGEdgeEvent[] getEdgeTypedEvents(int[] types) {
		
		ArrayList<JRipplesEIGEdgeEvent> tmpEventsSet=new ArrayList<JRipplesEIGEdgeEvent>();
		
		ArrayList<Integer> tmpTypesSet=new ArrayList<Integer>();
		
		for (int i=0;i<types.length;i++)
		{
			tmpTypesSet.add(Integer.valueOf(types[i]));
		}
		
		for (int i=0;i<edgeEvents.length;i++) {
			if (tmpTypesSet.contains(Integer.valueOf(edgeEvents[i].getEventType()))) 
					tmpEventsSet.add(edgeEvents[i]);
		}
		
		return tmpEventsSet.toArray(new JRipplesEIGEdgeEvent[tmpEventsSet.size()]);

	}
	
	/**
	 * Returns specific edge events.
	 * @param type
	 * 	desired type of event, where type is one of a constants defined in {@link JRipplesEIGEdgeEvent}
	 * @return
	 * 	Array of edge events of the requested type related to this event. If there is no edge events of requested types registered with this event, empty array will be returned.
	 */

	public JRipplesEIGEdgeEvent[] getEdgeTypedEvents(int type) {
		ArrayList<JRipplesEIGEdgeEvent> tmpEventsSet=new ArrayList<JRipplesEIGEdgeEvent>();
		
		for (int i=0;i<edgeEvents.length;i++) {
			if (type==edgeEvents[i].getEventType()) 
					tmpEventsSet.add(edgeEvents[i]);
		}
		
		return tmpEventsSet.toArray(new JRipplesEIGEdgeEvent[tmpEventsSet.size()]);
	}
	
}
