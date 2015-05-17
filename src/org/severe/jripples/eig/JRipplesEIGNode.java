/*
 * Created on Dec 4, 2005
 *
 */
package org.severe.jripples.eig;

import java.util.*;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.IMember;
import org.eclipse.core.resources.IResource;
/**
 * JRipplesEIGNode class represents a node of the EIG (Evolving Interoperation
 * Graph). Node serves as the container for the components of the project under
 * analysis, as well as the container for the extra information that can be associated
 * with this class like Incremental Change status(mark) and probability.
 * @see JRipplesEIG
 * @author Maksym Petrenko
 * 
 */
public  class JRipplesEIGNode implements IAdaptable, Comparable {
	// public static JRipplesEIGNode[] NONE = new JRipplesEIGNode[] {};
	
	private String mark;
	private String probability;

	private IMember nodeMember;
	
	private JRipplesEIGNode node;

	private LinkedList undoHistory = new LinkedList();

	private LinkedList redoHistory = new LinkedList();



	/**
	 * Constructor - creates a node with the specified IMember and
	 * sets node's mark and probability to empty string.<br>
	 * Please note that nodes, created directly with the constructor, 
	 * will not be handeleded in the EIG. To creare a node that is
	 * handeled by the EIG, use {@link JRipplesEIG#addNode(IMember)} 
	 * instead. 
	 * 
	 * @param member
	 *            IMember, which this node represents
	 */

	public JRipplesEIGNode(IMember member) {
		if (member==null) return;
		
		node = this;

		this.setNodeIMember(member);
		this.setMark(null);
		this.setProbability(null);
			
		this.clearUndoHistory();
		this.clearRedoHistory();
	}

	/**
	 * Returns node's short name - simple, human-readable string, which is used
	 * for node presentation in GUI. Simple name is obtained by calling 
	 * {@link org.eclipse.jdt.core.IMember.getElementName()}. 
	 * Couple different nodes within the same EIG can have the same short name. 
	 * Use {@link #getFullName()} instead if you need to get node's unique name.
	 * 
	 * @return short name of the node's underlying <code>IMember<code> if it is set, 
	 * empty string otherwise  
	 * @domain
	 * EIG node
	 */
	
	public String getShortName() {
	
		if(nodeMember==null) return "";
		String name=nodeMember.getElementName();
		if (name!=null) return name;
		return "";
		
	}

	/**
	 * Returns node's fully qualified name. For if the underlying memeber is
	 * of type IType, the returned full name is equal to the 
	 * fully qualified name of the node's underlying IMember. For IMethod and
	 * IField type of IMember, the full name is composed by taking fully qualified name
	 * of the top declaring class of this IMember, and adding the short name
	 * of the <code>IMember<code> to it, separated by a "::" string.
	 * 
	 * Use {@link #getShortName()} if you need to get node's simlple name
	 * to present this node in a GUI.
	 * 
	 * @return fully qualified name of the underlying member if it is set, 
	 * 	empty string otherwise 
	 */

	public String getFullName() {
		String fullName;
		if(nodeMember==null) return "";
		if (nodeMember instanceof IType) fullName=((IType) nodeMember).getFullyQualifiedName();
			else {fullName=JRipplesIMemberServices.getTopDeclaringType(nodeMember).getFullyQualifiedName()+"::"+nodeMember.getElementName();
			
			}
		
		if (fullName!=null) return fullName;
		return "";
	}

	/**
	 * associates EIG mark with the node. Typically, EIG marks are used to
	 * denote the status of the node's underlying member during Incremental
	 * Change process.
	 * 
	 * @return EIG mark of the node if there is a one; blank string
	 *         otherwise.
	 * @see #setMark(String)
	 */

	public String getMark() {
		if (this.mark==null) this.mark="";
		return mark;
	}

	/**
	 * Returns node's underlying {@link org.eclipse.jdt.core.IMember}  - 
	 * the actual component of <code>IMember<code> type of the project ander analysis,
	 * which this node represents.
	 * 
	 * @return underlying instance of <code>IMember<code> of the node if there is a one; <code>null</code>
	 *         otherwise.
	 * @see #setNodeIMember(IMember)
	 */

	public IMember getNodeIMember() {
		return nodeMember;
	}

	/**
	 * Return probability value associated with the node. Typically, probabilities 
	 * are used to denote the probability of some event, that can happen with the 
	 * node's underlying member. Probabilities can be evaluated through different
	 * software metrics during Incremental Change process.
	 * 
	 * @return probability, associated with the node if there is a one;
	 *         <code>null</code> otherwise.
	 * @see #setProbability(String)
	 */

	public String getProbability() {
		return probability;
	}

	/**
	 * Tests if the node is a top-level class. This is done by investigating
	 * underlying <code>IMember<code> java element.
	 * 
	 * @return <code>true<code>, if the class is a top class;
	 *         <code>false</code> otherwise.
	 * @see #getNodeIMember()
	 * 
 	 */
	
	public boolean isTop() {
		try {
			if ((nodeMember instanceof IType) && (!((IType)nodeMember).isMember())) return true;
		} catch (Exception e) {
			//do nothing
		}
		return false;
	}
	
	/**
	 * associates EIG mark with the node. Typically, EIG marks are used to
	 * denote the status of the node's underlying member during Incremental
	 * Change process.
	 * 
	 * @param mark
	 *            EIG mark to be associated with this node
	 * @see #getMark()
	 */
	public void setMark(String mark) {
		if ((mark!=null) && (this.mark!=null))
			if (mark.compareTo(this.mark)==0) return;
		undoHistory.addFirst(this.getMark());
		undoHistory.addFirst("setMark");
		if (!JRipplesEIG.redoInProgress) clearRedoHistory();
		
		this.mark = mark;
		JRipplesEIG
				.fireJRipplesEIGChanged(node,
						JRipplesEIGNodeEvent.NODE_MARK_CHANGED,
						JRipplesEIG.UNDOABLE);
	}

	/**
	 * associate probability value with the node. Typically, probabilities are
	 * used to denote the probability of some event, that can happen with the
	 * node's underlying member. Probabilities can be evaluated through different
	 * software metrics during Incremental Change process.
	 * 
	 * @param probability
	 *            probability to be associated with this node
	 * @see #getProbability()
	 */

	public void setProbability(String probability) {
		if ((probability!=null) && this.probability!=null)
			if (probability.compareTo(this.probability)==0) return;
		
		this.probability = probability;
		/*undoHistory.addFirst(this.getProbability());
		undoHistory.addFirst("setProbability");
		if (!JRipplesEIG.redoInProgress) clearRedoHistory();
		this.probability = probability;
		*/
		//will not be put into the EIG history
		
		JRipplesEIG
				.fireJRipplesEIGChanged(node,
						JRipplesEIGNodeEvent.NODE_PROBABILITY_CHANGED,
						JRipplesEIG.NONEABLE);
	}

	/**
	 * Sets node's underlying member - the actual member of the project ander
	 * analysis, which this node represents.
	 * 
	 * @param nodeMember
	 *            node's underlying member
	 * @see #getNodeClass()
	*/ 

	private void setNodeIMember(IMember nodeMember) {
		//undoHistory.addFirst(this.getNodeIMember());
		//undoHistory.addFirst("setNodeMember");
		//if (!JRipplesEIG.redoInProgress) clearRedoHistory();
		this.nodeMember = nodeMember;
		JRipplesEIG
				.fireJRipplesEIGChanged(node,
						JRipplesEIGNodeEvent.NODE_MEMBER_CHANGED,
						JRipplesEIG.NONEABLE);
	}
	
//-------------------------- Undo / Redo -------------------------------------
	protected void clearHistory(){
		undoHistory.clear();
		redoHistory.clear();
	}
	
	/**
	 * Clears node's undo history
	 * 
	 */
	protected void clearUndoHistory() {
		undoHistory.clear();
	}

	/**
	 * Clears node's redo history
	 * 
	 */

	protected void clearRedoHistory() {
		redoHistory.clear();
	}

	/**
	 * Undoes the last action, performed on this node
	 * 
	 */
	protected void undo() {
		if (undoHistory.size() == 0)
			return;
		
		
		String s = (String) undoHistory.removeFirst();

		if (s.compareTo("setMark") == 0) {
			String mmark = (String) undoHistory.removeFirst();
			redoHistory.addFirst(this.getMark());
			redoHistory.addFirst("setMark");
			this.mark = mmark;
			JRipplesEIG.fireJRipplesEIGChanged(node,
					JRipplesEIGNodeEvent.NODE_MARK_CHANGED,
					JRipplesEIG.REDOABLE);
			
		} else if (s.compareTo("setProbability") == 0) {
			String prob = (String) undoHistory.removeFirst();
			redoHistory.addFirst(this.getProbability());
			redoHistory.addFirst("setProbability");
			this.probability = prob;
			JRipplesEIG.fireJRipplesEIGChanged(node,
					JRipplesEIGNodeEvent.NODE_PROBABILITY_CHANGED,
					JRipplesEIG.NONEABLE);
		} else if (s.compareTo("setNodeMember") == 0) {
			IMember ttype = (IMember) undoHistory.removeFirst();
			redoHistory.addFirst(this.getNodeIMember());
			redoHistory.addFirst("setNodeMember");
			this.nodeMember = ttype;
			JRipplesEIG.fireJRipplesEIGChanged(node,
					JRipplesEIGNodeEvent.NODE_MEMBER_CHANGED,
					JRipplesEIG.NONEABLE);
		} else
			return;
	}

	/**
	 * Redoes last undone action
	 * 
	 */
	protected void redo() {
		if (redoHistory.size() == 0)
			return;

		String s = (String) redoHistory.removeFirst();

		if (s.compareTo("setMark") == 0) {
			String mmark = (String) redoHistory.removeFirst();
			undoHistory.addFirst(this.getMark());
			undoHistory.addFirst("setMark");
			this.mark = mmark;
			JRipplesEIG.fireJRipplesEIGChanged(node,
					JRipplesEIGNodeEvent.NODE_MARK_CHANGED,
					JRipplesEIG.UNDOABLE);
		} else if (s.compareTo("setProbability") == 0) {
			String prob = (String) redoHistory.removeFirst();
			undoHistory.addFirst(this.getProbability());
			undoHistory.addFirst("setProbability");
			this.probability = prob;
			JRipplesEIG.fireJRipplesEIGChanged(node,
					JRipplesEIGNodeEvent.NODE_PROBABILITY_CHANGED,
					JRipplesEIG.NONEABLE);
		} else if (s.compareTo("setNodeMember") == 0) {
			IMember ttype = (IMember) redoHistory.removeFirst();
			undoHistory.addFirst(this.getNodeIMember());
			undoHistory.addFirst("setNodeMember");
			this.nodeMember = ttype;
			JRipplesEIG.fireJRipplesEIGChanged(node,
					JRipplesEIGNodeEvent.NODE_MEMBER_CHANGED,
					JRipplesEIG.NONEABLE);
		} else
			return;
	}

	/**
	 * Returns node's fully qualified name - see {@link #getFullName()}.
	 * @return node's fully qualified name - see {@link #getFullName()} for more details.
	 */
	public String toString() {
		return this.getFullName();
	}

	/**
	 * Compares two nodes by comparing their underlying <code>IMember<code>s
	 * (see {@link JRipplesIMemberServices#areSimilar(IMember, IMember)}).
	 * 
	 * @param node
	 *            node to compare with
	 * @return true if nodes' fully qualified names are equal; <code>null</code>
	 *         otherwise
	 */
	public int compareTo(JRipplesEIGNode node) {
		
		if (node==null) return -1;
		if (JRipplesIMemberServices.areSimilar(this.getNodeIMember(), node.getNodeIMember())) return 0;
		return -1;
	}

	/**
	 * Provides an adapter for the <code>IMember</code>, <code>IResource</code>
	 * <code>JRipplesEIGNode</code> and <code>String</code> classes.
	 * 
	 * @param adapter
	 *            class for which adapter is needed
	 */

	public Object getAdapter(Class adapter) {
		if (adapter.isInstance(this))
			return this;
		if (adapter.isInstance(getNodeIMember()))
			return getNodeIMember();
		if (adapter.isInstance(getFullName()))
			return getFullName();
		if (adapter.isInstance(IResource.class))
			return getNodeIMember().getAdapter(IResource.class);
		
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}

	@Override
	public int compareTo(Object arg0) {
		if (arg0==null) return -1;
		if (!(arg0 instanceof JRipplesEIGNode)) return -1;
		return this.getFullName().compareTo(((JRipplesEIGNode) arg0).getFullName());
	}

}
