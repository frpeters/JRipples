package org.severe.jripples.eig;

import java.util.LinkedList;

/**
 * JRipplesEIGEdge class represents a dependency (interoperation) between two nodes of 
 * the EIG (Evolving Interoperation Graph). It also serves as the container for the 
 * extra information that can be associated with this dependency like 
 * Incremental Change status (mark) and probability.
 * 
 * @see JRipplesEIG
 * @see JRipplesEIGNode
 * @author Maksym Petrenko
 * 
 */
public class JRipplesEIGEdge {


	private JRipplesEIGNode fromNode;

	private JRipplesEIGNode toNode;

	private String mark;
	private String probability;

	private LinkedList undoHistory = new LinkedList();

	private LinkedList redoHistory = new LinkedList();

	private JRipplesEIGEdge edge;
	private Integer count=1;




	/** 
	 * Constructor - creates an edge that represents a dependency
	 * between two nodes  and
	 * sets edge's mark and probability to empty string. <br>( fromNode O-------------------------> toNode )<br>  
	 * Please note that edges, created directly with the constructor, 
	 * will not be handled in the EIG. To create a dependency that is
	 * handled by the EIG, use {@link JRipplesEIG#addEdge(JRipplesEIGNode, JRipplesEIGNode)} 
	 * instead. 
	 * @param fromNode
	 * 		a node, from which this dependency originates
	 * @param toNode
	 * 		a node, to which which this dependency points
	 */
	public JRipplesEIGEdge(JRipplesEIGNode fromNode, JRipplesEIGNode toNode) {
		this.edge = this;

		this.setFromNode(fromNode);
		this.setToNode(toNode);
		this.setMark(null);
		this.setProbability(null);
		this.clearUndoHistory();
		this.clearRedoHistory();
	}



	/**
	 * Associates EIG mark with the edge during Incremental
	 * Change process.
	 * 
	 * @return EIG mark of the edge if there is a one; <code>null</code>
	 *         otherwise.
	 * @see #setMark(String)
	 */

	public String getMark() {
		return this.mark;
	}




	/**
	 * Return probability value associated with the edge. Probabilities can 
	 * be evaluated through different software metrics during 
	 * Incremental Change process.
	 * 
	 * @return probability, associated with the edge if there is a one;
	 *         <code>null</code> otherwise.
	 * @see #setProbability(String)
	 */

	public String getProbability() {
		return this.probability;
	}




	/**
	 * Associates EIG mark with the edge during Incremental
	 * Change process.
	 * 
	 * @param mark
	 *            EIG mark to be associated with this edge
	 * @see #getMark()
	 */
	public void setMark(String mark) {
		this.undoHistory.addFirst(this.getMark());
		this.undoHistory.addFirst("setMark");
		if (!JRipplesEIG.redoInProgress) this.clearRedoHistory();
		this.mark = mark;
		JRipplesEIG
		.fireJRipplesEIGChanged(this.edge,
				JRipplesEIGEdgeEvent.EDGE_PROBABILITY_CHANGED,
				JRipplesEIG.UNDOABLE);
	}

	/**
	 * associate probability value with the edge. Probabilities can be 
	 * evaluated through different software metrics during 
	 * Incremental Change process.
	 * 
	 * @param probability
	 *            probability to be associated with this edge
	 * @see #getProbability()
	 */

	public void setProbability(String probability) {
		if ((probability!=null) && this.probability!=null)
			if (probability.compareTo(this.probability)==0) return;

		this.probability = probability;
		/*undoHistory.addFirst(this.getProbability());
		undoHistory.addFirst("setProbability");
		if (!JRipplesEIG.redoInProgress) clearRedoHistory();
		this.probability = probability;*/
		JRipplesEIG
		.fireJRipplesEIGChanged(this.edge,
				JRipplesEIGEdgeEvent.EDGE_MARK_CHANGED,
				JRipplesEIG.NONEABLE);
	}



	/**
	 * Returns a {@link JRipplesEIGNode}, from which this dependency originates.
	 * @return a {@link JRipplesEIGNode}, from which this dependency originates
	 */
	public JRipplesEIGNode getFromNode() {
		return this.fromNode;
	}

	/**
	 *Returns a {@link JRipplesEIGNode}, to which which this dependency points. 
	 * @return a {@link JRipplesEIGNode}, to which which this dependency points
	 */
	public JRipplesEIGNode getToNode() {
		return this.toNode;
	}


	/**
	 * Returns number of times the edges appears in the code. 
	 * @return number of times the edges appears in the code
	 */
	public Integer getCount() {
		return count;
	}


	/**
	 * Sets the number of times the edge appears in the code. Typically used by parsers.
	 * @param count
	 * 		 number of times the edges appears in the code
	 */
	public void setCount(Integer count) {
		if ((count!=null) && this.count!=null)
			if (count.compareTo(this.count)==0) return;

		this.count = count;
		/*undoHistory.addFirst(this.getProbability());
		undoHistory.addFirst("setProbability");
		if (!JRipplesEIG.redoInProgress) clearRedoHistory();
		this.probability = probability;*/
		JRipplesEIG
		.fireJRipplesEIGChanged(this.edge,
				JRipplesEIGEdgeEvent.EDGE_COUNT_CHANGED,
				JRipplesEIG.NONEABLE);
	}



	/**
	 * @param param
	 * 		 a {@link JRipplesEIGNode}, from which this dependency originates
	 */
	private void setFromNode(JRipplesEIGNode param) {
		//undoHistory.addFirst(this.getFromNode());
		//undoHistory.addFirst("setFromNode");
		//if (!JRipplesEIG.redoInProgress) clearRedoHistory();
		this.fromNode = param;
		JRipplesEIG
		.fireJRipplesEIGChanged(this.edge,
				JRipplesEIGEdgeEvent.EDGE_FROM_NODE_CHANGED,
				JRipplesEIG.NONEABLE);
	}


	/**
	 * @param param
	 * 		a {@link JRipplesEIGNode}, to which which this dependency points
	 */
	private void setToNode(JRipplesEIGNode param) {
		//undoHistory.addFirst(this.getToNode());
		//undoHistory.addFirst("setToNode");
		//if (!JRipplesEIG.redoInProgress) clearRedoHistory();
		this.toNode = param;
		JRipplesEIG
		.fireJRipplesEIGChanged(this.edge,
				JRipplesEIGEdgeEvent.EDGE_TO_NODE_CHANGED,
				JRipplesEIG.NONEABLE);
	}


	/**
	 * Redoes last action done to this edge
	 */
	protected void redo() {
		if (this.redoHistory.size() == 0)
			return;

		String s = (String) this.redoHistory.removeFirst();
		if (s.compareTo("setMark") == 0) {
			String mmark = (String) this.redoHistory.removeFirst();
			this.undoHistory.addFirst(this.getMark());
			this.undoHistory.addFirst("setMark");
			this.mark = mmark;
			JRipplesEIG.fireJRipplesEIGChanged(this.edge,
					JRipplesEIGEdgeEvent.EDGE_MARK_CHANGED,
					JRipplesEIG.UNDOABLE);
		}else if (s.compareTo("setProbability") == 0) {
			String prob = (String) this.redoHistory.removeFirst();
			this.undoHistory.addFirst(this.getProbability());
			this.undoHistory.addFirst("setProbability");
			this.probability = prob;
			JRipplesEIG.fireJRipplesEIGChanged(this.edge,
					JRipplesEIGEdgeEvent.EDGE_PROBABILITY_CHANGED,
					JRipplesEIG.NONEABLE);
		} else	if (s.compareTo("setFromNode") == 0) {
			JRipplesEIGNode node = (JRipplesEIGNode) this.redoHistory.removeFirst();
			this.undoHistory.addFirst(this.getFromNode());
			this.undoHistory.addFirst("setFromNode");
			this.fromNode = node;
			JRipplesEIG.fireJRipplesEIGChanged(this.edge,
					JRipplesEIGEdgeEvent.EDGE_FROM_NODE_CHANGED,
					JRipplesEIG.NONEABLE);
		} else if (s.compareTo("setToNode") == 0) {
			JRipplesEIGNode node = (JRipplesEIGNode) this.redoHistory.removeFirst();
			this.undoHistory.addFirst(this.getToNode());
			this.undoHistory.addFirst("setToNode");
			this.toNode = node;
			JRipplesEIG.fireJRipplesEIGChanged(this.edge,
					JRipplesEIGEdgeEvent.EDGE_TO_NODE_CHANGED,
					JRipplesEIG.NONEABLE);
		} else
			return;
	}



	private void clearRedoHistory() {

		this.redoHistory.clear();
		/*
		 * fromNodeRedoHistory.clear(); toNodeRedoHistory.clear();
		 * fromMethodRedoHistory.clear(); toMethodRedoHistory.clear();
		 */
	}

	private void clearUndoHistory() {
		this.undoHistory.clear();
		/*
		 * fromNodeUndoHistory.clear(); toNodeUndoHistory.clear();
		 * fromMethodUndoHistory.clear(); toMethodUndoHistory.clear();
		 */
	}


	protected void undo() {
		if (this.undoHistory.size() == 0)
			return;

		String s = (String) this.undoHistory.removeFirst();
		if (s.compareTo("setMark") == 0) {
			String mmark = (String) this.undoHistory.removeFirst();
			this.redoHistory.addFirst(this.getMark());
			this.redoHistory.addFirst("setMark");
			this.mark = mmark;
			JRipplesEIG.fireJRipplesEIGChanged(this.edge,
					JRipplesEIGEdgeEvent.EDGE_MARK_CHANGED,
					JRipplesEIG.REDOABLE);
		}	if (s.compareTo("setProbability") == 0) {
			String prob = (String) this.undoHistory.removeFirst();
			this.redoHistory.addFirst(this.getProbability());
			this.redoHistory.addFirst("setProbability");
			this.probability = prob;
			JRipplesEIG.fireJRipplesEIGChanged(this.edge,
					JRipplesEIGEdgeEvent.EDGE_PROBABILITY_CHANGED,
					JRipplesEIG.NONEABLE);
		} else if (s.compareTo("setFromNode") == 0) {
			JRipplesEIGNode node = (JRipplesEIGNode) this.undoHistory.removeFirst();
			this.redoHistory.addFirst(this.getFromNode());
			this.redoHistory.addFirst("setFromNode");
			this.fromNode = node;
			JRipplesEIG.fireJRipplesEIGChanged(this.edge,
					JRipplesEIGEdgeEvent.EDGE_FROM_NODE_CHANGED,
					JRipplesEIG.NONEABLE);
		} else if (s.compareTo("setToNode") == 0) {
			JRipplesEIGNode node = (JRipplesEIGNode) this.undoHistory.removeFirst();
			this.redoHistory.addFirst(this.getToNode());
			this.redoHistory.addFirst("setToNode");
			this.toNode = node;
			JRipplesEIG.fireJRipplesEIGChanged(this.edge,
					JRipplesEIGEdgeEvent.EDGE_TO_NODE_CHANGED,
					JRipplesEIG.NONEABLE);
		} else
			return;
	}


	/**
	 * Returns string representation of the edge in the form of "fromNode O-------------------------> toNode".
	 * @return string representation of the edge in the form of "fromNode O-------------------------> toNode".
	 */
	public String toString() {
		return this.getFromNode().getFullName()+" O-------------------------> "+this.getToNode().getFullName();
	}
}
