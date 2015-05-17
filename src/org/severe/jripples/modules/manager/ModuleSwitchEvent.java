package org.severe.jripples.modules.manager;

/**
 * This class represents an event of activation and / or deactivation of JRipples modules. If
 * both {@link #getNewLoader()} and {@link #getOldLoader()} are set, then an old loader was 
 * replaced by a new loader as they both share the same category of JRipples modules. <br>
 * @see ModuleProxy
 * @see ModuleSwitchEvent
 * @author Maksym Petrenko
 *
 */
public class ModuleSwitchEvent {
	private JRipplesModuleLoader oldLoader;
	private JRipplesModuleLoader newLoader;
	private int controllerType;
	
	/**
	 * @param oldLoader
	 * @param newLoader
	 * @param controllerType
	 */
	protected ModuleSwitchEvent(JRipplesModuleLoader oldLoader, JRipplesModuleLoader newLoader, int controllerType) {
		this.controllerType=controllerType;
		this.oldLoader=oldLoader;
		this.newLoader=newLoader;
	}

	/**
	 * Returns a role of a controller,
	 * which triggered this event, so that clients registered for receiving this
	 * kind of event can process the event more appropriately. 
	 * @return
	 *  role of a controller, which triggered activation or deactivation of this event
	 */
	public int getControllerType() {
		return controllerType;
	}

	/**
	 * Returns a module that was activated in this event.
	 * @return
	 * a module that was activated in this event if any, <br><code>null</code> otherwise
	 */
	public JRipplesModuleLoader getNewLoader() {
		return newLoader;
	}

	/**
	 * Returns a module that was deactivated in this 
	 * event. This can happen as a simple order of {@link ModuleProxy} or 
	 * as a result of activation of another module in the 
	 * same category of modules.
	 * @return
	 * a module that was deactivated if any, <br><code>null</code> otherwise 
	 * @see #getNewLoader() 
	 */
	public JRipplesModuleLoader getOldLoader() {
		return oldLoader;
	}
	
}
