/*
 * Created on Oct 20, 2005
 *
 */
package org.severe.jripples.modules.interfaces;



import java.util.Set;

import org.severe.jripples.eig.JRipplesEIGNode;

/**
 * Interface of JRipples modules that provide various estimations for JRipplesEIG nodes.
 * @author Maksym Petrenko
 * @see JRipplesEIG
 * @see JRipplesEIGNode
 * 
 */
public interface JRipplesAnalysisModuleInterface extends
		JRipplesModuleInterface {

	/**
	 * Called to calculate estimations for all nodes in the JRipple EIG. Typically is called upon EIG initialization.
	 * @see JRipplesEIG
	 * @see JRipplesEIGNode.getProbability()
	 */
	public void AnalyzeProject();

	/**
	 * Called to reestimate probability values of a set of JRipple EIG nodes. Typically is called to reflect changes in particular nodes.
	 * @param changed_nodes
	 * set of {@link JRipplesEIGNode} nodes to be displayed in the GUI
	 */
	public void ReAnalyzeProjectAtNodes(Set<JRipplesEIGNode> changed_nodes);
	
	/**
	 * Returns units in which this module does estimations (e.g. rank, number of matches, degree of coupling etc.). 
	 * @return
	 * units in which this module does estimations
	 */
	public String getUnitsTitle(); 

}

