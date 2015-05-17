/*
 * Created on Oct 20, 2005
 *
 */
package org.severe.jripples.modules.interfaces;


import java.util.Set;

import org.severe.jripples.eig.JRipplesEIGNode;

/**
 * Interface of JRipples modules that build a dependency graph and populate JRipples EIG database.
 * @author Maksym Petrenko
 * @see JRipplesEIG
 * @see JRipplesEIGNode
 * 
 */
public interface JRipplesDependencyGraphModuleInterface extends
		JRipplesModuleInterface {

	/**
	 * Called to calculate a dependency graph for all nodes in the JRipple EIG. Typically is called upon EIG initialization.
	 * @see JRipplesEIG
	 * @see JRipplesEIGNode.getProbability()
	 */
	public void AnalyzeProject();
	/**
	 * Called to recalculate a dependency graph for a set of JRipple EIG nodes. Typically is called to reflect changes in particular nodes.
	 * @param changed_nodes
	 * set of {@link JRipplesEIGNode} nodes to be displayed in the GUI
	 */
	public void ReAnalyzeProjectAtNodes(Set<JRipplesEIGNode> changed_nodes);

}
