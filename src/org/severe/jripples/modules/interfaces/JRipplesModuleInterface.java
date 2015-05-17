/*
 * Created on Oct 20, 2005
 *
 */
package org.severe.jripples.modules.interfaces;

/**
 * This class is a common interface for all JRipples modules. It 
 * provides methods that notify a module when it is loaded or 
 * shut down. It also tells a module what was a role of a controller,
 * which triggered activation or deactivation of this module, to process 
 * load and shut down events more appropriately. 
 * @author Maksym Petrenko
 * 
 */
public interface JRipplesModuleInterface {
	
	/**
	 * Constant indicating an event was triggered by {@link org.severe.jripples.modules.manager.ModuleProxy} class.  
	 */
	public static final int CONTROLLER_TYPE_MODULEPROXY=1;
	/**
	 * Constant indicating an event was triggered by JRipples Start Analysis wizard; 
	 * <br>typically means that user just started an analysis session and thus the 
	 * selected project should be analyzed from scratch.
	 */
	public static final int CONTROLLER_TYPE_START=2;
	/**
	 * Constant indicating an event was triggered by loading one of 
	 * the previously saved JRipples states; <br>typically means that no 
	 * data processing should be done as everything is already loaded from disk. 
	 */
	public static final int CONTROLLER_TYPE_IMPORTEXPORT=4;
	/**
	 * Constant indicating an event was triggered by user through a configuration menu; 
	 * <br>typically means that one module was replaced by another module in order to
	 * obtain different perspective on the JRipples EIG data.  
	 */
	public static final int CONTROLLER_TYPE_USER=8;
	/**
	 * Constant indicating an event was triggered by some Eclipse event; 
	 * <br>typically means that a GUI module was disposed or activated through Eclipse controls.
	 */
	public static final int CONTROLLER_TYPE_ECLIPSE=16;
	/**
	 * Constant indicating general type of unknown controller. 
	 * 
	 */

	public static final int CONTROLLER_TYPE_UNKNOWN=32;
	/**
	 * Constant indicating an event was triggered by some module itself; 
	 * <br>typically means that some module monitored Eclipse environment and avter a certain 
	 * event decided that it is the most appropriate module for the user at that moment of
	 * time. 
	 */
	public static final int CONTROLLER_TYPE_SELF=64;
	
	
	/**
	 * The method is called when the module implementing this interface is activated through {@link org.severe.jripples.modules.manager.ModuleProxy} class.
	*	<br>The controllerType parameter is supplied to indicate a role of a component that triggered deactivation of this module, to process load and shut down events 
	 * more appropriately;
	 * <br>type is one of the constants, defined in this {@link JRipplesModuleInterface} interface.
 	 * @param controllerType
 	 * 	type of controller that triggered activation of this module; type is one of the constants, defined in {@link JRipplesModuleInterface} class  
	 */
	public void loadUp(int controllerType);

	/**
	 * The method is called when the module implementing this interface is deactivated up through {@link org.severe.jripples.modules.manager.ModuleProxy} class.
	 * <br>The controllerType parameter is supplied to indicate a role of a component that triggered deactivation of this module, to process load and shut down events 
	 * more appropriately;
	 * <br>type is one of the constants, defined in this {@link JRipplesModuleInterface} interface.
	 * @param controllerType
	 * type of controller that triggered deactivation of this module
	 */
	public void shutDown(int controllerType);

}
