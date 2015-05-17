/*
 * Created on Oct 20, 2005
 *
 */
package org.severe.jripples.modules.interfaces;

import java.util.Set;

import org.severe.jripples.eig.JRipplesEIGNode;

/**
 * Interface of JRipples modules that provide a GUI for the JRipples EIG.  
 * @author Maksym Petrenko
 * @see JRipplesEIG
 *
 */
public interface JRipplesPresentationModuleInterface extends
		JRipplesModuleInterface {

	/**
	 * Called to display the whole JRipple EIG in the GUI of this module. Typically is called upon EIG initialization.
	 * @see JRipplesEIG  
	 */
	public void DisplayEIG();

	/**
	 * Called to display a set of JRipple EIG nodes in the GUI of this module. Typically is called to reflect changes in particular nodes.
	 * @param changed_nodes
	 * set of {@link JRipplesEIGNode} nodes to be displayed in the GUI
	 */
	public void RefreshEIGAtNodes(Set<JRipplesEIGNode> changed_nodes);

}
