/*
 * Created on Oct 20, 2005
 *
 */
package org.severe.jripples.eig;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.severe.jripples.eig.internal.JRipplesEclipseResourseListeners;


/**
 * <p>A software system can be viewed as a set of components and their interoperations and 
 * can be formally modeled as graph. In a nutshell, in this 
 * graph components are represented as nodes and interactions are represented as edges 
 * among the nodes. As the software evolves and changes, its components change too. 
 * Moreover, a change, introduced in a one component, may propagate through the 
 * interoperation dependencies to other components as well. To reflect the change 
 * process in the graph, we use marks.</p> 
 * <p>The described graph, which contains components, their interoperations, marks and 
 * has a set of propagation rules defined, is called <b>Evolving Interoperation Graph (EIG)</b>.</p>
 * <p>JRipples EIG is created by  
 * <ol>
 * <li>setting up a project under analysis, 
 * <li>setting up a main class to an analysis from, 
 * <li>adding IMember Java Elements of the project under analysis to wrapped with JRipples EIG nodes
 * <li>adding edges between created nodes
 * </ol>
 * </p>
 * <p>There is only one instance of JRipplesEIG is created during JRipples plug-in 
 * lifecycle and thus only one project can be analyzed in the same time. Consequently, most of the 
 * JRipplesEIG methods are static and refer to the same dependency graph.</p>
 * <p>Even though storing in the same way, JRipples EIG classifies every node as a top node or 
 * a member node based on whether underlying IMember object is a top in a class nesting hierarchy.</p>
 * <p>
 * Here is a short example on how JRipples EIG is typically used:
 * <pre>
 * <code>
 *
 * JRipplesEIG.initEIG();
 * 
 * JRipplesEIG.setProject((IProject) someProject);
 * JRipplesEIG.setMainClass((IType) someType);
 *
 *JRipplesEIG.doLock(this);
 * JRipplesEIGNode node1=JRippelsEIG.addNode((IMember) member1);
 * JRipplesEIGNode node2=JRippelsEIG.addNode((IMember) member2);
 * JRipplesEIGNode node3=JRippelsEIG.addNode((IMember) member3);
 * 
 * JRippelsEIGEdge edge1=JRippelsEIG.addEdge(node1, node2);
 * JRippelsEIGEdge edge1=JRippelsEIG.addEdge(node1, JRipplesEIG.getNode((IMember)member2));
 * JRippelsEIGEdge edge1=JRippelsEIG.addEdge(JRipplesEIG.getNode((IMember)member3), JRipplesEIG.getNode((IMember)member2));
 * JRipplesEIG.doUnLock(this); //All the EIG listeners will be notified of the changes
 * 
 * ...some logic...
 * 
 * JRipplesEIG.doLock(this);
 * 
 * node1.setMark("Changed");
 * JRipplesEIG.removeNode(node2);
 * node3.setMark("Visited");
 * 
 * JRipplesEIG.doUnLock(this); //All the EIG listeners will be notified of the changes
 * 
 * node3.setProbability("12"); //All the EIG listeners will be notified of the change
 * 
 * </code>
 * </pre>
 * </p>
 * 
 * @author Maksym Petrenko
 * @see #initEIG()
 * @see #setProject(IProject)
 * @see #setMainClass(IType)
 * @see #addNode(IMember)
 * @see #addEdge(JRipplesEIGNode, JRipplesEIGNode)
 * @see JRipplesEIGNode
 * @see JRipplesEIGEdge
 * 
 */
public final class JRipplesEIG {

	private static JRipplesEIG EIG;

	private static Map<IMember, JRipplesEIGNode> nodes;
	private static Set<JRipplesEIGNode> topNodes;
	private static Map<JRipplesEIGNode, HashSet<JRipplesEIGNode>> members; //Node -> Set of members

	private static Collection<JRipplesEIGEdge> edges;
	private static Map<JRipplesEIGNode, HashMap<JRipplesEIGNode, JRipplesEIGEdge>> edgesAdjacency; //Node -> Set of related nodes 

	private static IProject project;
	private static IType mainClass;

	private static Set<JRipplesEIGListener> eigListeners = Collections.synchronizedSet(new  HashSet<JRipplesEIGListener>());
	private static ArrayList<JRipplesEIGNodeEvent> nodeEvents = new ArrayList<JRipplesEIGNodeEvent>();
	private static ArrayList<JRipplesEIGEdgeEvent> edgeEvents = new ArrayList<JRipplesEIGEdgeEvent>();

	private static boolean lock;
	private static Object lockingObj=null;


	private static LinkedList<LinkedList<Object>> undoHistory = new LinkedList<LinkedList<Object>>();
	private static LinkedList<LinkedList<Object>> redoHistory = new LinkedList<LinkedList<Object>>();
	protected static boolean redoInProgress = false;

	protected static final int UNDOABLE = 1;
	protected static final int REDOABLE = 2;
	protected static final int NONEABLE = 4;



	// ------------------------------- EIG General ------------------------------

	protected JRipplesEIG()  {

		EIG = this;
		if ((nodes == null) || (edges == null))
			initEIG();
	}

	/**
	 * Returns an instance of JRipples EIG. JRipples EIG instance is created once the JRipples plugin is activated and remains the same throughout the plug-in lifecycle. 
	 * @return
	 * the instance of JRipples EIG 
	 * 
	 */
	public static JRipplesEIG getEIG() {
		if (EIG == null)
			EIG = new JRipplesEIG();
		return EIG;
	}

	/**
	 * Reinitializes JRipples EIG for a new analysis - that is, it deletes all nodes and edges from the database and clears undo / redo history.
	 * <br>Please note, that project and main class are not reset.  
	 */
	public static void initEIG() {

		if (EIG==null) EIG=new JRipplesEIG();
		JRipplesEIG.lockingObj=null;
		JRipplesEIG.doLock(EIG);


		if  (edges!=null)
			synchronized (edges) { 
				for (Iterator<JRipplesEIGEdge> iter=edges.iterator();iter.hasNext();) {
					fireJRipplesEIGChanged(iter.next(), JRipplesEIGEdgeEvent.EDGE_REMOVED,
							JRipplesEIG.NONEABLE);
				}
			}

		if  (nodes!=null)
			synchronized (nodes) {
				for (Iterator<JRipplesEIGNode> iter=nodes.values().iterator();iter.hasNext();) {
					fireJRipplesEIGChanged(iter.next(), JRipplesEIGNodeEvent.NODE_REMOVED,
							JRipplesEIG.NONEABLE);
				}

			}

		JRipplesEIG.doUnLock(EIG);
		JRipplesEIG.doLock(EIG);

		/*
		if (nodes!=null)
			if (nodes.values()!=null)
				batchRemoveNodes( new LinkedHashSet (nodes.values()));
		 */

		if (nodes!=null) {
			JRipplesEIGNode []nodesArr=JRipplesEIG.getAllNodes();
			nodes.clear();
			for (int i=0;i<nodesArr.length;i++) {
				nodesArr[i]=null;
			}

		}
		if (edges!=null) {
			JRipplesEIGEdge []edgesArr=JRipplesEIG.getAllEdges();
			edges.clear();			
			for (int i=0;i<edgesArr.length;i++) {
				edgesArr[i]=null;
			}
		}


		if (nodes == null)
			nodes = Collections.synchronizedMap(new  HashMap<IMember, JRipplesEIGNode>());

		if (topNodes!=null) topNodes.clear();
		else topNodes = Collections.synchronizedSet(new HashSet<JRipplesEIGNode>());

		if (members!=null) members.clear();
		else members=Collections.synchronizedMap(new  HashMap<JRipplesEIGNode, HashSet<JRipplesEIGNode>>()); 


		if (edges == null)
			edges = Collections.synchronizedSet(new HashSet<JRipplesEIGEdge>());

		if (edgesAdjacency!=null) edgesAdjacency.clear();
		else edgesAdjacency = Collections.synchronizedMap(new  HashMap<JRipplesEIGNode, HashMap<JRipplesEIGNode, JRipplesEIGEdge>>());


		JRipplesEIG.clearUndoHistory();
		JRipplesEIG.clearRedoHistory();
		//lockingObj=EIG;
		doUnLock(EIG);
		JRipplesEIG.clearHistory();
	}



	/**
	 * Checks whether the JRipples EIG is initialized for analysis - that is, whether a main project and main class are set, and whether EIG has at least one node registered with it.
	 * @return
	 * <code>true</code> if the JRipples EIG is initialized for analysis, <br><code>null</code> otherwise
	 * @see #getProject()
	 * @see #getMainClass()
	 * @see #getAllNodes()
	 */
	public static boolean isInitialized() {
		if (getProject() == null || getMainClass() == null || nodes == null || nodes.size() == 0) {
			return false;
		};
		return true;
	}




	/**
	 * Sets a project under analysis, usually chosen through "JRipples > Start" menu.
	 * @param proj
	 * a project this JRipples EIG analysis refers to
	 * @see #getProject()
	 * @see #getMainClass()
	 * @see #setMainClass(IType)
	 */
	public static void setProject(IProject proj) {
		project = proj;
		if (proj!=null) JRipplesEclipseResourseListeners.listen();
	}

	/**
	 *Returns a project under analysis, usually chosen through "JRipples > Start" menu.
	 * @return
	 * a project this JRipples EIG analysis refers to
	 * @see #setProject(IProject)
	 * @see #getMainClass()
	 * @see #setMainClass(IType)
	 */
	public static IProject getProject() {
		return project;

	}

	/**
	 * Sets a main class (a class to start the analysis from) of a project under analysis, usually chosen through "JRipples > Start" menu.
	 * @param type
	 * a main class of a project this JRipples EIG analysis refers to
	 * @see #getProject()
	 * @see #setProject(IProject)
	 * @see #getMainClass()
	 * 
	 */
	public static void setMainClass(IType type) {
		mainClass = type;

	}

	/**
	 * Returns the main class (a class to start the analysis from) of a project under analysis, usually chosen through "JRipples > Start" menu.
	 * @return
	 * a main class of a project this JRipples EIG analysis refers to
	 * @see #getProject()
	 * @see #setProject(IProject)
	 * @see #setMainClass(IType)
	 */
	public static IType getMainClass() {
		return mainClass;

	}

	// ------------------------------------- EIG Node operations -------------------------


	/**
	 * Creates and adds to the JRipples EIG a node that wraps a supplied IMember object
	 * @return
	 *  an existing node if one found in JRipples EIG,<br> 
	 *  a created node if there was no such node declared before,<br> 
	 *  <code>null</code> if the supplied IMember object is <code>null</code> 
	 */
	public static JRipplesEIGNode addNode(IMember nodeIMember) {

		if (nodeIMember==null) return null;
		if (nodes.containsKey(nodeIMember)) return nodes.get(nodeIMember);
		JRipplesEIGNode node=null;
		node=new JRipplesEIGNode(nodeIMember);

		if (!members.keySet().contains(node)) {
			HashSet<JRipplesEIGNode> nodeMembers=new HashSet<JRipplesEIGNode>();
			members.put(node,nodeMembers);
		}

		try {
			if (node.isTop()) { 
				topNodes.add(node);
			} else {

				JRipplesEIGNode parentNode=findParentNodeForMemberNode(node);
				if (parentNode==null) parentNode=JRipplesEIG.addNode(JRipplesIMemberServices.getMemberParent(nodeIMember));

				if (parentNode!=null) {
					members.get(parentNode).add(node);
				}
			}
		} catch (Exception e) {
			//do nothing
		}

		nodes.put(nodeIMember, node);

		fireJRipplesEIGChanged(node, JRipplesEIGNodeEvent.NODE_ADDED,JRipplesEIG.NONEABLE);
		return node;
	}


	/**
	 * Removes a node from the JRipples EIG. It also removes all edges,
	 * associated with this node. If the node is top, the same set of
	 * actions will be applied to all member node of this node as well - 
	 * the member nodes together with their associated edges will
	 * be removed from the JRipples EIG.
	 * @param node
	 * 	node to remove
	 * @see #getNodeMembers(JRipplesEIGNode)
	 * @see JRipplesEIGNode#isTop()
	 * @see #removeEdge(JRipplesEIGEdge)
	 */

	public static void removeNode(JRipplesEIGNode node) {
		//Remove members if any
		LinkedHashSet<JRipplesEIGNode> nodesToBeRemoved=new LinkedHashSet<JRipplesEIGNode>();
		nodesToBeRemoved.add(node);
		if (node.isTop()) topNodes.remove(node);
		walkMembers(node,nodesToBeRemoved);	

		batchRemoveNodes(nodesToBeRemoved); //Remove encountered nodes and edges
		nodesToBeRemoved.clear();
		nodesToBeRemoved=null;
	}

	/**
	 * Walks through the members of the node and adds them to the list of the nodes to be removed in the batch
	 * @param node
	 * @param nodesToBeRemoved
	 */
	private static void walkMembers(JRipplesEIGNode node, LinkedHashSet<JRipplesEIGNode> nodesToBeRemoved) {
		HashSet<JRipplesEIGNode> membersSet=members.get(node);
		if (membersSet.size()>0) {
			for (Iterator <JRipplesEIGNode>iter = membersSet.iterator();iter.hasNext();) {
				JRipplesEIGNode nodeTmp=iter.next();
				nodesToBeRemoved.add(nodeTmp);
				walkMembers(nodeTmp, nodesToBeRemoved);
			}
			membersSet.clear();
			members.remove(node);
			membersSet=null;
		}
	}
	
	/**
	 * Removes nodes and their edges in a batch
	 * @param nodesToBeRemoved
	 */
	private static void batchRemoveNodes(LinkedHashSet<JRipplesEIGNode> nodesToBeRemoved) {

		if (nodesToBeRemoved==null) return;
		if (nodesToBeRemoved.size()==0) return;

		Collection<JRipplesEIGEdge> tmpEdges = new HashSet<JRipplesEIGEdge>();
		synchronized (edges) {
			for (Iterator<JRipplesEIGEdge> iter = edges.iterator(); iter.hasNext();) {
				JRipplesEIGEdge edge = iter.next();
				if ((edge.getFromNode() != null) && ((edge.getToNode() != null)))
					if ((nodesToBeRemoved.contains(edge.getFromNode()))
							|| (nodesToBeRemoved.contains(edge.getToNode())))
						tmpEdges.add(edge);
			}
		}

		if (tmpEdges.size() > 0) {
			for (Iterator<JRipplesEIGEdge> iter=tmpEdges.iterator();iter.hasNext();) {
				removeEdge(iter.next());
			}
			tmpEdges.clear();
		}
		tmpEdges=null;

		for (Iterator<JRipplesEIGNode> iter=nodesToBeRemoved.iterator();iter.hasNext();) {
			JRipplesEIGNode nodeTmp=iter.next();

			nodes.remove(nodeTmp.getNodeIMember());

			fireJRipplesEIGChanged(nodeTmp, JRipplesEIGNodeEvent.NODE_REMOVED,
					JRipplesEIG.NONEABLE);
			nodeTmp = null;
		}

	}

	/**
	 * Checks whether JRipples EIG database contains a node with provided 
	 * IMember object.
	 * @param nodeMember
	 * underlying IMember object of a node to find  
	 * @return
	 * <code>true</code> if JRipples EIG database contains a node with provided 
	 * IMember object, <br> <code>false</code> otherwise
	 * @see JRipplesEIGNode#getNodeIMember()
	 * @see #getNode(IMember)
	 * 
	 */
	public static boolean existsNode(IMember nodeMember) {
		if (nodeMember==null) return false; 
		return nodes.keySet().contains(nodeMember);
	}

	/**
	 * Checks whether JRipples EIG database contains a node with provided 
	 * IMember object and returns it if any.
	 * @param nodeMember
	 * underlying IMember object of a node to return  
	 * @return
	 * a node with supplied underlying IMember object if JRipples EIG database contains such a node 
	 * , <br> <code>null</code> otherwise
	 * @see #existsNode(IMember)
	 * @see JRipplesEIGNode#getNodeIMember()
	 */
	public static JRipplesEIGNode getNode(IMember nodeMember) {
		if (nodeMember==null) return null;
		if (!existsNode(nodeMember)) return null; 
		return nodes.get(nodeMember);
	}


	/**
	 * Returns all the nodes, registered with this EIG (that is, created with {@link #addNode(IMember)} method).
	 * @return
	 * all the nodes, registered with this EIG (that is, created with {@link #addNode(IMember)} method),<br> or empty array if no node was found.
	 */
	public static JRipplesEIGNode[] getAllNodes() {
		if (nodes == null) return new JRipplesEIGNode[0];
		return nodes.values().toArray(new JRipplesEIGNode[nodes.values().size()]);
	}

	/**
	 * Returns all the top nodes (nodes, whose underlying IMember object is top class hierarchy), registered with this EIG.
	 * @return
	 * all the top nodes, registered with this EIG, <br> or empty array if no top node was found.
	 */
	public static JRipplesEIGNode[] getTopNodes() {
		if (topNodes == null) return new JRipplesEIGNode[0];
		return topNodes.toArray(new JRipplesEIGNode[topNodes.size()]);
	}


	//===============Member's operations ============================================


	/**
	 * Returns member nodes, whose underlying IMember Java elements are defined within IMember Java element of a supplied top node.  
	 * @param node
	 * node, whose member nodes should be returned
	 * @return
	 * nodes, whose underlying IMember Java elements are defined within IMember Java element of a supplied top node, if any, <br>
	 * empty array otherwise
	 * @see JRipplesEIGNode#isTop()
	 * @see #getTopNodes()
	 * @see #findTopNodeForMemberNode(JRipplesEIGNode) 
	 */
	public static JRipplesEIGNode[] getNodeMembers(JRipplesEIGNode node) {
		if (node==null) return new JRipplesEIGNode[0]; 
		if (!members.keySet().contains(node)) return new JRipplesEIGNode[0];
		HashSet<JRipplesEIGNode> membersSet=members.get(node);
		return (JRipplesEIGNode[]) membersSet.toArray(new JRipplesEIGNode[membersSet.size()]);
	}



	/**
	 * Finds a top node, whose underlying IMember object declares the supplied member parameter.
	 * @param member
	 * member object, for which a top node should be found
	 * @return
	 * a node, if any, whose underlying IMember object declares the supplied member parameter, <br>or <code>null</code> otherwise
	 * @see #getTopNodes()
	 * @see #getNodeMembers(JRipplesEIGNode)
	 * @see #findTopNodeForMemberNode(JRipplesEIGNode)
	 * @see JRipplesEIGNode#isTop()
	 */
	public static JRipplesEIGNode findTopNodeForIMember(IMember member) {
		IType declaringType=JRipplesIMemberServices.getTopDeclaringType(member);
		if (declaringType==null) return null;
		JRipplesEIGNode topNode=JRipplesEIG.getNode(declaringType);
		return topNode;
	}

	/**
	 * Finds a parent node, whose underlying IMember object declares the supplied member parameter.
	 * @param member
	 * member object, for which a top node should be found
	 * @return
	 * a parent node, if any, whose underlying IMember object declares the supplied member parameter, <br>or <code>null</code> otherwise
	 * @see #getTopNodes()
	 * @see #getNodeMembers(JRipplesEIGNode)
	 * @see #findTopNodeForMemberNode(JRipplesEIGNode)
	 * @see JRipplesEIGNode#isTop()
	 */
	private static JRipplesEIGNode findParentNodeForIMember(IMember member) {
		IMember parent=JRipplesIMemberServices.getMemberParent(member);
		if (parent==null) return null;
		JRipplesEIGNode topNode=JRipplesEIG.getNode(parent);
		return topNode;
	}

	/**
	 * Finds a top node for a supplied member node.
	 * @param node
	 * @return
	 * the supplied node if it is top,<br>
	 * a top node, whose underlying IMember Java element declares underlying IMember Java element of the supplied node, <br>
	 *<code>null</code> otherwise   
	 * @see JRipplesEIGNode#isTop() 
	 * @see #getNodeMembers(JRipplesEIGNode)
	 * @see #getTopNodes() 
	 */
	public static JRipplesEIGNode findTopNodeForMemberNode(JRipplesEIGNode node) {
		//if (node.isTop()) return node;
		return findTopNodeForIMember(node.getNodeIMember());
	}


	/**
	 * Finds a parent node for a supplied member node.
	 * @param node
	 * @return
	 * the supplied node if it is top,<br>
	 * a parent node, whose underlying IMember Java element declares underlying IMember Java element of the supplied node, <br>
	 *<code>null</code> otherwise   
	 * @see JRipplesEIGNode#isTop() 
	 * @see #getNodeMembers(JRipplesEIGNode)
	 * @see #getTopNodes() 
	 */
	public static JRipplesEIGNode findParentNodeForMemberNode(JRipplesEIGNode node) {
		return findParentNodeForIMember(node.getNodeIMember());
	}

	//	-----------------------------------Node Neigbors -----------------------------------------

	/**
	 * direction constant indicating that only calling nodes (nodes, that call the supplied centralNode) should be returned
	 * <br>to be used with {@link #edgesToNeigbors(Set, int, int)}
	 */

	public static final int DIRECTION_CONSIDERED_CALLING_NODES_ONLY=-1;
	/**
	 * direction constant indicating that both called and calling nodes should be returned
	 * <br>to be used with {@link #edgesToNeigbors(Set, int, int)}
	 */
	public static final int DIRECTION_CONSIDERED_BOTH_CALLING_AND_CALLED=0;
	/**
	 * direction constant indicating that only called nodes (nodes, that are called from the supplied centralNode) should be returned
	 * <br>to be used with {@link #edgesToNeigbors(Set, int, int)}
	 */
	public static final int DIRECTION_CONSIDERED_CALLED_NODES_ONLY=1;
	/**
	 * nesting constant indicating that only top nodes should be returned
	 * <br>to be used with {@link #edgesToNeigbors(Set, int, int)}
	 */
	public static final int NESTING_CONSIDERED_TOP_NODES_ONLY=-1;
	/**
	 * nesting constant indicating that both top and member nodes should be returned
	 * <br>to be used with {@link #edgesToNeigbors(Set, int, int)}
	 */
	public static final int NESTING_CONSIDERED_BOTH_TOP_AND_MEMBER_NODES=0;
	/**
	 * nesting constant indicating that only member nodes should be returned
	 * <br>to be used with {@link #edgesToNeigbors(Set, int, int)}
	 */
	public static final int NESTING_CONSIDERED_MEMBER_NODES_ONLY=1;


	/**
	 *Based on supplied parameters, returns a set of JRippelsEIGNodes that contain  
	 *<ul>
	 *<li>either top nodes, or member nodes, or both 
	 *<li>that 
	 *<li>either call, or are called, or both 
	 *<li>by 
	 *<li>any of the supplied nodes (directly, but not transitively through the nodes's members)
	 *</ul>
	 * <br>Direction constants:
	 * <ul>
	 * <li>{@link #DIRECTION_CONSIDERED_BOTH_CALLING_AND_CALLED}
	 * <li>{@link #DIRECTION_CONSIDERED_CALLING_NODES_ONLY}
	 * <li>{@link #DIRECTION_CONSIDERED_CALLED_NODES_ONLY}
	 * </ul>
	 * <br>Nesting constants:
	 * <ul>
	 * <li>{@link #NESTING_CONSIDERED_BOTH_TOP_AND_MEMBER_NODES}
	 * <li>{@link #NESTING_CONSIDERED_TOP_NODES_ONLY}
	 * <li>{@link #NESTING_CONSIDERED_MEMBER_NODES_ONLY}
	 * </ul>
	 * 
	 * @param nodes
	 * 	Set of {@link JRipplesEIGNode} nodes, of which direct (not transitive through the node's members) neighbors should be returned
	 * @param directionConsidered
	 * 	whether to return neighbors that only call, are called by, or do both to the supplied nodes  
	 * @param nestingConsidered
	 * 	whether to return neighbors that are top nodes, member nodes, or both
	 * @return
	 * 	Set of {@link JRipplesEIGNode} nodes that satisfy specified requirements; set may be empty
	 * @see #getAllAnyNodeNeighbors(JRipplesEIGNode)
	 * @see #getAllTopNodeNeighbors(JRipplesEIGNode)
	 * @see #getAllMemberNodeNeighbors(JRipplesEIGNode)
	 * @see #getIncomingAnyNodeNeighbors(JRipplesEIGNode)
	 * @see #getIncomingTopNodeNeighbors(JRipplesEIGNode)
	 * @see #getIncomingMemberNodeNeighbors(JRipplesEIGNode)
	 * @see #getOutgoingAnyNodeNeighbors(JRipplesEIGNode)
	 * @see #getOutgoingTopNodeNeighbors(JRipplesEIGNode)
	 * @see #getOutgoingMemberNodeNeighbors(JRipplesEIGNode)
	 * 
	 * 
	 */
	public static HashSet<JRipplesEIGNode> edgesToNeigbors(Set<JRipplesEIGNode> nodes, int directionConsidered, int nestingConsidered ) {
		//TODO Can be done as bit masks and enums

		HashSet<JRipplesEIGNode> result=new HashSet<JRipplesEIGNode>();
		if (nodes==null) return result;
		if (nodes.size()==0) return result;

		synchronized (edges) {
			for (Iterator<JRipplesEIGEdge> iter = edges.iterator(); iter.hasNext();) {
				JRipplesEIGEdge edge = iter.next();
				if ((edge.getFromNode() != null) && ((edge.getToNode() != null))) {
					//fromNode->centralNode (centralNode==toNode)
					if ((nodes.contains(edge.getToNode()))) {
						if ((directionConsidered==DIRECTION_CONSIDERED_CALLING_NODES_ONLY) || (directionConsidered==DIRECTION_CONSIDERED_BOTH_CALLING_AND_CALLED)) {
							if (topNodes.contains(edge.getFromNode())) {
								if ((nestingConsidered==NESTING_CONSIDERED_TOP_NODES_ONLY) || (nestingConsidered==NESTING_CONSIDERED_BOTH_TOP_AND_MEMBER_NODES)) 
									result.add(edge.getFromNode());
							} else	{
								if ((nestingConsidered==NESTING_CONSIDERED_MEMBER_NODES_ONLY) || (nestingConsidered==NESTING_CONSIDERED_BOTH_TOP_AND_MEMBER_NODES)) 
									result.add(edge.getFromNode());
								if ((nestingConsidered==NESTING_CONSIDERED_TOP_NODES_ONLY) || (nestingConsidered==NESTING_CONSIDERED_BOTH_TOP_AND_MEMBER_NODES)) 
									result.add(JRipplesEIG.findTopNodeForMemberNode(edge.getFromNode()));
							}
						}
					}
					//centralNode->toNode (centralNode==fromNode)
					if ((nodes.contains(edge.getFromNode()))) {
						if ((directionConsidered==DIRECTION_CONSIDERED_CALLED_NODES_ONLY) || (directionConsidered==DIRECTION_CONSIDERED_BOTH_CALLING_AND_CALLED)) {
							if (topNodes.contains(edge.getToNode())) {
								if ((nestingConsidered==NESTING_CONSIDERED_TOP_NODES_ONLY) || (nestingConsidered==NESTING_CONSIDERED_BOTH_TOP_AND_MEMBER_NODES)) 
									result.add(edge.getToNode());
							} else	{
								if ((nestingConsidered==NESTING_CONSIDERED_MEMBER_NODES_ONLY) || (nestingConsidered==NESTING_CONSIDERED_BOTH_TOP_AND_MEMBER_NODES)) 
									result.add(edge.getToNode());
								if ((nestingConsidered==NESTING_CONSIDERED_TOP_NODES_ONLY) || (nestingConsidered==NESTING_CONSIDERED_BOTH_TOP_AND_MEMBER_NODES)) 
									result.add(JRipplesEIG.findTopNodeForMemberNode(edge.getToNode()));
							}
						}
					}
				}
			}
		}
		
		return result;
	}

		
	//-----Node-to-TopNode neigbors ----

	/**
	 * Returns top nodes that both call and are called by both the supplied node and it's member nodes if any
	 * <br>Works by calling {@link #edgesToNeigbors(Set, int, int)} method with predefined parameters
	 * @param node
	 * 	node, whose neighboring nodes should be returned 
	 * @return
	 * 	neighboring nodes
	 */
	public static JRipplesEIGNode[] getAllTopNodeNeighbors(
			JRipplesEIGNode node) {
		if (node==null) return new JRipplesEIGNode[0];
		HashSet<JRipplesEIGNode> nodes=new HashSet<JRipplesEIGNode>();
		nodes.add(node);
		if (members.containsKey(node))
			nodes.addAll(members.get(node));
		Collection<JRipplesEIGNode> neighbors=edgesToNeigbors(nodes,DIRECTION_CONSIDERED_BOTH_CALLING_AND_CALLED,NESTING_CONSIDERED_TOP_NODES_ONLY);
		return  neighbors
		.toArray(new JRipplesEIGNode[neighbors.size()]);
	}


	/**
	 * Returns top nodes that call both the supplied node and it's member nodes if any
	 * <br>Works by calling {@link #edgesToNeigbors(Set, int, int)} method with predefined parameters
	 * @param node
	 * 	node, whose neighboring nodes should be returned 
	 * @return
	 *  neighboring nodes
	 */

	public static JRipplesEIGNode[] getIncomingTopNodeNeighbors(
			JRipplesEIGNode node) {
		if (node==null) return new JRipplesEIGNode[0];
		HashSet<JRipplesEIGNode> nodes=new HashSet<JRipplesEIGNode>();
		nodes.add(node);
		if (members.containsKey(node))
			nodes.addAll(members.get(node));
		Collection<JRipplesEIGNode> neighbors=edgesToNeigbors(nodes,DIRECTION_CONSIDERED_CALLING_NODES_ONLY,NESTING_CONSIDERED_TOP_NODES_ONLY);
		return  neighbors
		.toArray(new JRipplesEIGNode[neighbors.size()]);
	}

	/**
	 * Returns top nodes that are called by both the supplied node and it's member nodes if any
	 * <br>Works by calling {@link #edgesToNeigbors(Set, int, int)} method with predefined parameters
	 * @param node
	 * 	node, whose neighboring nodes should be returned 
	 * @return
	 * 	neighboring nodes
	 */


	public static JRipplesEIGNode[] getOutgoingTopNodeNeighbors(
			JRipplesEIGNode node) {
		if (node==null) return new JRipplesEIGNode[0];
		HashSet<JRipplesEIGNode> nodes=new HashSet<JRipplesEIGNode>();
		nodes.add(node);
		if (members.containsKey(node))
			nodes.addAll(members.get(node));
		Collection<JRipplesEIGNode> neighbors=edgesToNeigbors(nodes,DIRECTION_CONSIDERED_CALLED_NODES_ONLY,NESTING_CONSIDERED_TOP_NODES_ONLY);
		return  neighbors
		.toArray(new JRipplesEIGNode[neighbors.size()]);
	}


	//-----Node-to-MemberNode neigbors ----

	/**
	 * Returns member nodes that both call and are called by both the supplied node and it's member nodes if any
	 * <br>Works by calling {@link #edgesToNeigbors(Set, int, int)} method with predefined parameters
	 * @param node
	 * 	node, whose neighboring nodes should be returned 
	 * @return
	 * 	neighboring nodes
	 */
	public static JRipplesEIGNode[] getAllMemberNodeNeighbors(
			JRipplesEIGNode node) {
		if (node==null) return new JRipplesEIGNode[0];
		HashSet<JRipplesEIGNode> nodes=new HashSet<JRipplesEIGNode>();
		nodes.add(node);
		if (members.containsKey(node))
			nodes.addAll(members.get(node));
		Collection<JRipplesEIGNode> neighbors=edgesToNeigbors(nodes,DIRECTION_CONSIDERED_BOTH_CALLING_AND_CALLED,NESTING_CONSIDERED_MEMBER_NODES_ONLY);
		return  neighbors
		.toArray(new JRipplesEIGNode[neighbors.size()]);
	}

	/**
	 * Returns member nodes that call both the supplied node and it's member nodes if any
	 * <br>Works by calling {@link #edgesToNeigbors(Set, int, int)} method with predefined parameters
	 * @param node
	 * 	node, whose neighboring nodes should be returned 
	 * @return
	 * 	neighboring nodes
	 */
	public static JRipplesEIGNode[] getIncomingMemberNodeNeighbors(
			JRipplesEIGNode node) {
		if (node==null) return new JRipplesEIGNode[0];
		HashSet<JRipplesEIGNode> nodes=new HashSet<JRipplesEIGNode>();
		nodes.add(node);
		if (members.containsKey(node))
			nodes.addAll(members.get(node));
		Collection<JRipplesEIGNode> neighbors=edgesToNeigbors(nodes,DIRECTION_CONSIDERED_CALLING_NODES_ONLY,NESTING_CONSIDERED_MEMBER_NODES_ONLY);
		return  neighbors
		.toArray(new JRipplesEIGNode[neighbors.size()]);
	}


	/**
	 * Returns member nodes that are called by both the supplied node and it's member nodes if any
	 * <br>Works by calling {@link #edgesToNeigbors(Set, int, int)} method with predefined parameters
	 * @param node
	 * 	node, whose neighboring nodes should be returned 
	 * @return
	 * 	neighboring nodes
	 */

	public static JRipplesEIGNode[] getOutgoingMemberNodeNeighbors(
			JRipplesEIGNode node) {
		if (node==null) return new JRipplesEIGNode[0];
		HashSet<JRipplesEIGNode> nodes=new HashSet<JRipplesEIGNode>();
		nodes.add(node);
		if (members.containsKey(node))
			nodes.addAll(members.get(node));
		Collection<JRipplesEIGNode> neighbors=edgesToNeigbors(nodes,DIRECTION_CONSIDERED_CALLED_NODES_ONLY,NESTING_CONSIDERED_MEMBER_NODES_ONLY);
		return  neighbors
		.toArray(new JRipplesEIGNode[neighbors.size()]);
	}

	//-----Node-to-Any Node neigbors ----

	/**
	 * Returns both top and member nodes that both call and are called by both the supplied node and it's member nodes if any
	 * <br>Works by calling {@link #edgesToNeigbors(Set, int, int)} method with predefined parameters
	 * @param node
	 * 	node, whose neighboring nodes should be returned 
	 * @return
	 * 	neighboring nodes
	 */
	public static JRipplesEIGNode[] getAllAnyNodeNeighbors(
			JRipplesEIGNode node) {
		if (node==null) return new JRipplesEIGNode[0];
		HashSet<JRipplesEIGNode> nodes=new HashSet<JRipplesEIGNode>();
		nodes.add(node);
		if (members.containsKey(node))
			nodes.addAll(members.get(node));
		Collection<JRipplesEIGNode> neighbors=edgesToNeigbors(nodes,DIRECTION_CONSIDERED_BOTH_CALLING_AND_CALLED,NESTING_CONSIDERED_BOTH_TOP_AND_MEMBER_NODES);
		return  neighbors
		.toArray(new JRipplesEIGNode[neighbors.size()]);
	}


	/**
	 * Returns both top and member nodes that call both the supplied node and it's member nodes if any
	 * <br>Works by calling {@link #edgesToNeigbors(Set, int, int)} method with predefined parameters
	 * @param node
	 * 	node, whose neighboring nodes should be returned 
	 * @return
	 * 	neighboring nodes
	 */

	public static JRipplesEIGNode[] getIncomingAnyNodeNeighbors(
			JRipplesEIGNode node) {
		if (node==null) return new JRipplesEIGNode[0];
		HashSet<JRipplesEIGNode> nodes=new HashSet<JRipplesEIGNode>();
		nodes.add(node);
		if (members.containsKey(node))
			nodes.addAll(members.get(node));
		Collection<JRipplesEIGNode> neighbors=edgesToNeigbors(nodes, DIRECTION_CONSIDERED_CALLING_NODES_ONLY,NESTING_CONSIDERED_BOTH_TOP_AND_MEMBER_NODES);
		return  neighbors
		.toArray(new JRipplesEIGNode[neighbors.size()]);
	}



	/**
	 * Returns both top and member nodes that are called by both the supplied node and it's member nodes if any
	 * <br>Works by calling {@link #edgesToNeigbors(Set, int, int)} method with predefined parameters 
	 * @param node
	 * 	node, whose neighboring nodes should be returned 
	 * @return
	 * 	neighboring nodes
	 */

	public static JRipplesEIGNode[] getOutgoingAnyNodeNeighbors(
			JRipplesEIGNode node) {
		if (node==null) return new JRipplesEIGNode[0];
		HashSet<JRipplesEIGNode> nodes=new HashSet<JRipplesEIGNode>();
		nodes.add(node);
		
		if (members.containsKey(node))
			nodes.addAll(members.get(node));
		Collection<JRipplesEIGNode> neighbors=edgesToNeigbors(nodes,DIRECTION_CONSIDERED_CALLED_NODES_ONLY,NESTING_CONSIDERED_BOTH_TOP_AND_MEMBER_NODES);
		return  neighbors
		.toArray(new JRipplesEIGNode[neighbors.size()]);
	}



	// ------------------------------------- EIG edge operations ------------------------

	/**
	 * Creates and adds to the JRipples EIG an edge connecting two supplied nodes:
	 * <br>( fromNode O-------------------------> toNode )<br>
	 * Please note, that the edge is omni-directional, that is  fromNode->toNode does not imply toNode->fromNode.
	 * @param nodeFrom
	 * 	a node, from which this dependency originates
	 * @param nodeTo
	 * 	a node, to which which this dependency points
	 * @return
	 *  an existing edge if one found in JRipples EIG,<br> 
	 *  a created edge if there was no such edge declared before,<br> 
	 *  <code>null</code> if one or both of the supplied nodes is <code>null</code>, or nodes are equal, or nodes are declared within the same top class 
	 */
	public static JRipplesEIGEdge addEdge(JRipplesEIGNode nodeFrom,
			JRipplesEIGNode nodeTo) {

		if ((nodeTo == null) || (nodeFrom == null)) return null;
		if (nodeTo == nodeFrom ) return null;

		//enforces dependencies between different classes only
		//if (JRipplesEIG.findTopNodeForMemberNode(nodeTo) == JRipplesEIG.findTopNodeForMemberNode(nodeFrom)) return null;

		JRipplesEIGEdge oldEdge=getEdge(nodeFrom,nodeTo);
		if (oldEdge!=null) {
			oldEdge.setCount(oldEdge.getCount()+1);
			return oldEdge;
		}

		JRipplesEIGEdge edge = new JRipplesEIGEdge(nodeFrom, nodeTo);
		//Add to the list of edges

		edges.add(edge);

		//Add to the adjacency matrix


		if (!edgesAdjacency.containsKey(edge.getFromNode())) edgesAdjacency.put(edge.getFromNode(), new HashMap<JRipplesEIGNode, JRipplesEIGEdge>());
		HashMap<JRipplesEIGNode, JRipplesEIGEdge> refferedNodes=edgesAdjacency.get(edge.getFromNode());
		refferedNodes.put(edge.getToNode(),edge);

		fireJRipplesEIGChanged(edge, JRipplesEIGEdgeEvent.EDGE_ADDED,
				JRipplesEIG.NONEABLE);
		return edge;


	}

	public static void flattenEIG() {
		
		JRipplesEIGEdge edges[]=JRipplesEIG.getAllEdges();
		Set<JRipplesEIGNode> nodesFrom=new HashSet<JRipplesEIGNode>();
		Set<JRipplesEIGNode> nodesTo=new HashSet<JRipplesEIGNode>();
		for (int i=0;i<edges.length;i++) {
			nodesFrom.clear();
			nodesTo.clear();
			nodesFrom.add(edges[i].getFromNode());
			nodesTo.add(edges[i].getToNode());
			getAllParents(edges[i].getFromNode(),nodesFrom);
			getAllParents(edges[i].getToNode(),nodesTo);
			
			for (Iterator <JRipplesEIGNode> iterFrom=nodesFrom.iterator();iterFrom.hasNext();) {
				JRipplesEIGNode nodeFrom=iterFrom.next();
				for (Iterator <JRipplesEIGNode> iterTo=nodesTo.iterator();iterTo.hasNext();) {
					JRipplesEIGNode nodeTo=iterTo.next();
					addEdge(nodeFrom, nodeTo);
				}
			}
		}
	}
	
	
	private static void getAllParents (JRipplesEIGNode node, Set<JRipplesEIGNode> parents) {
		if (node.isTop()) {
			return;
		} else {
			JRipplesEIGNode parent=JRipplesEIG.findParentNodeForMemberNode(node);
			parents.add(parent);
			getAllParents(parent,parents);
		}
	}
	
	/**
	 * Returns previously created edge between two nodes, if one found in JRipples EIG.<br>
	 * Please note, that the edge is omni-directional, that is request for fromNode->toNode edge will not return toNode->fromNode 
	 * edge even if such exists.
	 * @param nodeFrom
	 * a node, from which this dependency originates
	 * @param nodeTo
	 * a node, to which which this dependency points
	 * @return
	 * previously created edge between two nodes, if one found in JRipples EIG, <br>
	 * <code>null</code> otherwise
	 */
	public static JRipplesEIGEdge getEdge(JRipplesEIGNode nodeFrom,
			JRipplesEIGNode nodeTo) {
		if ((nodeFrom == null) || (nodeTo == null)) return null;

		if (!edgesAdjacency.containsKey(nodeFrom)) return null;

		HashMap<JRipplesEIGNode, JRipplesEIGEdge> refferedNodes=edgesAdjacency.get(nodeFrom);
		if (!refferedNodes.containsKey(nodeTo)) return null;

		return refferedNodes.get(nodeTo);
	}





	/**
	 * Returns all the edges, registered with this EIG (that is, created with {@link #addEdge(JRipplesEIGNode, JRipplesEIGNode)} method).
	 * @return
	 * all the edges, registered with this EIG (that is, created with {@link #addEdge(JRipplesEIGNode, JRipplesEIGNode)} method).
	 */
	public static JRipplesEIGEdge[] getAllEdges() {
		if (edges==null) return new JRipplesEIGEdge[0];
		return edges.toArray(new JRipplesEIGEdge[edges.size()]);
	}



	/**
	 * Removes an edge from the JRipples EIG.
	 * @param edge
	 * 	edge to remove
	 */
	public static void removeEdge(JRipplesEIGEdge edge) {
		if (edges.contains(edge)) {
			fireJRipplesEIGChanged(edge, JRipplesEIGEdgeEvent.EDGE_REMOVED,
					JRipplesEIG.NONEABLE);

			//Remove from common list

			edges.remove(edge);


			//Remove from Adjacency matrix
			if (!edgesAdjacency.containsKey(edge.getFromNode())) return;
			HashMap<JRipplesEIGNode, JRipplesEIGEdge> refferedNodes=edgesAdjacency.get(edge.getFromNode());
			if (!refferedNodes.containsKey(edge.getToNode())) return;
			refferedNodes.remove(edge.getToNode());

		}
	}



	/**
	 * Checks whether JRipples EIG contains an edge between two given nodes. <br>
	 * Please note, that this operation is omni-directional, that is <code>existsEdge(A,B)==true</code> does not imply <code>existsEdge(B,A)==true</code> 
	 * @param nodeFrom
	 * 	a node, from which this edge originates
	 * @param nodeTo
	 *  a node, to which this to which which this dependency points
	 * @return
	 * <code>true</code> if an edge between two given nodes exists, <br><code>false</code> otherwise
	 */
	public static boolean existsEdge(JRipplesEIGNode nodeFrom,
			JRipplesEIGNode nodeTo) {
		if ((nodeFrom == null) || (nodeTo == null)) return false;

		if (!edgesAdjacency.containsKey(nodeFrom)) return false;

		HashMap <JRipplesEIGNode, JRipplesEIGEdge> refferedNodes=edgesAdjacency.get(nodeFrom);
		if (!refferedNodes.containsKey(nodeTo)) return false;

		return true;
	}






	// ------------------------------------ EIG listener------------------------------------------------


	/**
	 * Registers a {@link JRipplesEIGListener} to receive updates on lyficycle and content events of JRipples EIG nodes and edges
	 * @param listener
	 * 	Listener to register
	 */
	public static void addJRipplesEIGListener(
			JRipplesEIGListener listener) {
		if (!eigListeners.contains(listener))
			eigListeners.add(listener);
	}

	/**
	 * Unregisters a listener, previously registered with {@link #addJRipplesEIGListener(JRipplesEIGListener)}.
	 * @param listener
	 * 	Listener to unregister
	 */
	public static void removeJRipplesEIGListener(
			JRipplesEIGListener listener) {
		eigListeners.remove(listener);
	}



	protected static void fireJRipplesEIGChanged(JRipplesEIGNode item,
			int type, int undoType) {

		addToHistory(item, "Node", undoType);
		JRipplesEIGNodeEvent event = new JRipplesEIGNodeEvent(item,type);

		if (!JRipplesEIG.isLocked()) {
			fireSavedEIGEvents();
			notifyListeners(new JRipplesEIGEvent(EIG, new JRipplesEIGNodeEvent[]{event},null ));
		}
		else {
			nodeEvents.add(event);
		}

	}

	protected static void fireJRipplesEIGChanged(JRipplesEIGEdge item,
			int type, int undoType) {

		addToHistory(item, "Edge", undoType);
		JRipplesEIGEdgeEvent event = new JRipplesEIGEdgeEvent(item,	type);

		if (!JRipplesEIG.isLocked()) {
			fireSavedEIGEvents();
			notifyListeners(new JRipplesEIGEvent(EIG, null, new JRipplesEIGEdgeEvent[]{event}));
		}
		else 
			edgeEvents.add(event);
	}


	private static void fireSavedEIGEvents() {
		if ((nodeEvents.size()!=0) ||(edgeEvents.size()!=0)) {
			JRipplesEIGNodeEvent[] nodeEventsArr=nodeEvents.toArray(new JRipplesEIGNodeEvent[nodeEvents.size()]);
			JRipplesEIGEdgeEvent[] edgeEventsArr=edgeEvents.toArray(new JRipplesEIGEdgeEvent[edgeEvents.size()]);
			nodeEvents.clear();
			edgeEvents.clear();
			notifyListeners(new JRipplesEIGEvent(EIG,nodeEventsArr, edgeEventsArr));
		}
	}

	private static  void notifyListeners(JRipplesEIGEvent event) {

		JRipplesEIGListener[] listeners=eigListeners.toArray(new JRipplesEIGListener[eigListeners.size()]);
		for (int i=0;i<listeners.length;i++) {
			if (listeners[i]!=null)
				listeners[i].JRipplesEIGChanged(event);
		}



	}

	// ------------------------------------ Undo / Redo -------------------


	/**
	 * Checks is the JRipples EIG is locked. This usually means that some 
	 * changes are done EIG and more changes should follow, and thus it is 
	 * better to wait until the EIG is unlocked. See {@link #doLock(Object)} 
	 * for more details.   
	 * @return
	 * <code>true</code> if the JRipples EIG is locked, <br><code>false</code> otherwise
	 */
	public synchronized static boolean isLocked() {

		if (lockingObj==null) lock=false;
		if (getProject() == null) {
			lock = true;
		}
		;
		if (getMainClass() == null) {
			lock = true;
		}
		;

		if (nodes == null) {
			lock = true;
		}
		;
		return lock;
	}

	/**
	 * Notifies JRipples EIG that all lifecycle and content changes to EIG nodes 
	 * and edges that will follow until the {@link #doUnLock(Object)} is called, 
	 * should be treated as a one event. <br> All the changes, done within this time 
	 * frame, can be undone or redone simultaneously as a one operation.<br> No lister, 
	 * registered with the EIG, is notified of changes until the {@link #doUnLock(Object)} 
	 * is called to unlock the EIG.<br>Has no effect if EIG is already locked 
	 * (which probably means that the object was called as a result of actions in another
	 * object and thus an events of this object are a part of a bigger event). 
	 * Still,  even if the EIG is locked, {@link #doLock(Object)} and  {@link #doUnLock(Object)} 
	 * should be used to ensure the correctness of the organization of events.  
	 * <br> Please note, that it is a responsibility of the 
	 * locking object to call the {@link #doUnLock(Object)} as JRipples EIG does not do 
	 * any checks on the object’s status and thus locking state persists while the object is alive. 
	 *@param lockingObj
	 * 	object that starts a sequence of actions;<br> 
	 *  the same object should be used as a parameter to {@link #doUnLock(Object)} method to unlock the EIG.
	 *@see #doUnLock(Object)
	 *@see #isLocked() 
	 * 
	 */
	public static void doLock(Object lockingObj) {

		if (JRipplesEIG.lockingObj==null) { 
			lock=false;
			if(lockingObj!=null){
				LinkedList<Object> eventsSequence = new LinkedList<Object>();
				undoHistory.addFirst(eventsSequence);
				lock = true;
				JRipplesEIG.lockingObj=lockingObj;
			}
		}
	}

	private static void doReLock(Object lockingObj) {
		if (JRipplesEIG.lockingObj==null) {
			if(lockingObj!=null){
				LinkedList<Object> eventsSequence = new LinkedList<Object>();
				redoHistory.addFirst(eventsSequence);
				lock = true;
				JRipplesEIG.lockingObj=lockingObj;
			}
		}
	}

	/**
	 * Unlocks JRipples EIG and notifies all listers, registered with the EIG, of the
	 * all changes that occured during the event that has ended. <br>Has no effect if the 
	 * lockingObj is not a one used to lock the EIG. 
	 *@param lockingObj
	 * 	the same object that was used to lock the EIG with the {@link #doLock(Object)} command<br> 
	 *@see #doLock(Object)
	 *@see #isLocked() 
	 * 
	 */


	public static void doUnLock(Object lockingObj) {
		if (JRipplesEIG.lockingObj==null) lock=false;
		if (lockingObj==JRipplesEIG.lockingObj) {
			JRipplesEIG.lockingObj=null;
			lock = false;

			fireSavedEIGEvents();
		}

	}


	private static void clearUndoHistory() {
		undoHistory.clear();
		/*for (Iterator iter=nodes.iterator();iter.hasNext();) {
			((JRipplesEIGNode)iter.next()).clearUndoHistory();
		}*/
	}

	private static void clearRedoHistory() {
		redoHistory.clear();
		/*for (Iterator iter=nodes.iterator();iter.hasNext();) {
			((JRipplesEIGNode)iter.next()).clearRedoHistory();
		}*/

	}

	/**
	 * Clears the list of operations, availbale for undo and redo.
	 * @see #undo()
	 * @see #redo() 
	 */
	public static void clearHistory() {
		clearRedoHistory();
		clearUndoHistory();
	}

	private static void addToHistory(Object obj, String objType,
			int undoType) {
		if (undoType==JRipplesEIG.NONEABLE) return;

		LinkedList<Object> eventsSequence;

		if (undoType==UNDOABLE) {
			if (!isLocked())
				eventsSequence = new LinkedList<Object>();
			else if (undoHistory.size() == 0)
				eventsSequence = new LinkedList<Object>();
			else
				eventsSequence = undoHistory.removeFirst();

			eventsSequence.addFirst(obj);
			eventsSequence.addFirst(objType);

			undoHistory.addFirst(eventsSequence);
			if (!redoInProgress)
				clearRedoHistory();
		}

		else if (undoType==JRipplesEIG.REDOABLE) {
			if (!isLocked())
				eventsSequence = new LinkedList<Object>();
			else if (redoHistory.size() == 0)
				eventsSequence = new LinkedList<Object>();
			else
				eventsSequence = redoHistory.removeFirst();

			eventsSequence.addFirst(obj);
			eventsSequence.addFirst(objType);

			redoHistory.addFirst(eventsSequence);

		}
	}

	/**
	 * Cancels the most recent node and/or edge content operation or set of the operations on JRipples EIG.<br>
	 * Content operations are changes in marks and probabilities of nodes and edges.<br> 
	 * Any content operation, done between calls of {@link #doLock(Object)} and {@link #doUnLock(Object)} methods 
	 * will be grouped into a single set of operations and, thus, canceled as such.
	 * @see #redo() 
	 */
	@SuppressWarnings("unchecked")
	public static void undo() {
		//flattenEIG();

		if (!canUndo())
			return;
		LinkedList eventsSequence = undoHistory.removeFirst();

		redoInProgress = true;
		doReLock(EIG);
		for (; eventsSequence.size() > 0;) {
			String s = (String) eventsSequence.removeFirst();

			if (s.compareTo("Node") == 0) {
				JRipplesEIGNode node = (JRipplesEIGNode) eventsSequence
				.removeFirst();
				node.undo();

			} else if (s.compareTo("Edge") == 0) {
				JRipplesEIGEdge edge = (JRipplesEIGEdge) eventsSequence
				.removeFirst();
				edge.undo();

			}
		}
		;
		//if (!saved_state)
		doUnLock(EIG);
		redoInProgress = false;



	}

	/**
	 * Cancels the last undo operation.
	 * @see #undo() 
	 */
	@SuppressWarnings("unchecked")
	public static void redo() {

		if (!canRedo())
			return;

		LinkedList eventsSequence = redoHistory.removeFirst();

		redoInProgress = true;
		doLock(EIG);
		for (; eventsSequence.size() > 0;) {
			String s = (String) eventsSequence.removeFirst();

			if (s.compareTo("Node") == 0) {
				JRipplesEIGNode node = (JRipplesEIGNode) eventsSequence
				.removeFirst();
				node.redo();

			} else if (s.compareTo("Edge") == 0) {
				JRipplesEIGEdge edge = (JRipplesEIGEdge) eventsSequence
				.removeFirst();
				edge.redo();

			}
		}
		;
		//if (!saved_state)
		doUnLock(EIG);
		redoInProgress = false;
	}		


	/**
	 * Checks if there are any operations, availbale for redo.
	 * @return
	 * 	<code>true</code> if there are any operations, availbale for redo<br>
	 *  <code>false</code> otherwise
	 * @see #redo()
	 */
	public static boolean canRedo() {


		if (redoHistory.size() > 0)
			return true;

		return false;
	}

	/**
	 * Checks if there are any operations, availbale for undo.
	 * @return
	 * 	<code>true</code> if there are any operations, availbale for undo<br>
	 *  <code>false</code> otherwise
	 * @see #undo()
	 */
	public static boolean canUndo() {

		if (undoHistory.size() > 0)
			return true;
		return false;
	}




}


