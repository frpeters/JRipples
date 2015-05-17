package org.severe.jripples.modules.manager;

/**
 * ModuleSwitchListener interface should be implemented by classes who wish 
 * to be notified upon activation and deactivation of the JRipples modules.
 * @author Maksym Petrenko
 * @see ModuleSwitchEvent
 * @see ModuleProxy
 *
 */
public interface ModuleSwitchListener {
	/**
	 * Method is called upon activation and deactivation of the JRipples modules.
	 * @param evt
	 * 	event of activation and deactivation of the JRipples modules
	 * @see ModuleSwitchEvent
	 * @see ModuleProxy
	 * 
	 */
	void JRipplesModuleSwitchedChanged(ModuleSwitchEvent evt);
}
