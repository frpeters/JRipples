/*
 * Created on Oct 20, 2005
 *
 */
package org.severe.jripples.modules.manager;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;
import org.severe.jripples.constants.JRipplesConstants;
import org.severe.jripples.core.JRipplesPlugin;
import org.severe.jripples.logging.JRipplesLog;
import org.severe.jripples.modules.interfaces.JRipplesModuleInterface;



/**
 *
 * ModuleProxy class is used to manipulate modules, installed in Eclipse 
 * environment for JRipples tool. 
 * 
 * @author Maksym Petrenko
 *  
 * 
 */
public class ModuleProxy {
	

	private static Map<String, JRipplesModuleLoader> moduleHash;

	private static Map<String, HashSet<String>> categories = new HashMap<String, HashSet<String>>();

	private static Map<String, String> activeCategoryModules = new HashMap<String, String>();

	private static Set<String> tmph = new LinkedHashSet<String>();
	
	private static Set<ModuleSwitchListener> moduleSwitchListeners = Collections.synchronizedSet(new  HashSet<ModuleSwitchListener>());

	// Resource bundle.

	

	/**
	 * The default constructor. As most utilities provided by ModuleProxy are static, this is typically not needed.
	 */
	public ModuleProxy() {
		ReviewExtensions();
		
	}

	private void ReviewExtensions() {
		if (moduleHash != null)
			return;
		moduleHash = new TreeMap<String, JRipplesModuleLoader>();
		IConfigurationElement[] configElements = Platform
				.getExtensionRegistry().getConfigurationElementsFor(
						JRipplesConstants.ID_JRIPPLES_PLUGIN, "modules");

		for (int j = 0; j < configElements.length; j++) {

			JRipplesModuleLoader proxy = parseModule(configElements[j]);
			if (proxy != null) {
				moduleHash.put(proxy.getName(), proxy);
				// put module name in hash for use in menus
				if (!categories.containsKey(proxy.getCategory())) {
					categories.put(proxy.getCategory(), new HashSet<String>());
				}
				;
				tmph = categories.get(proxy.getCategory());
				tmph.add(proxy.getName());

			}

		}
	}

	private static JRipplesModuleLoader parseModule(
			IConfigurationElement configElement) {

		// check for permitted module categories can be performed here 
		//if (!configElement.getName().equals("itemType")) return null;

		try {
			return new JRipplesModuleLoader(configElement);
		} catch (Exception e) {
			String name = configElement.getAttribute("name");
			if (name == null)
				name = "[missing name attribute]";
			JRipplesLog.logError("Failed to load module [" + name + "] in ["
					+ configElement.getDeclaringExtension().getNamespaceIdentifier()+"]", e);

			return null;

		}
	}

	/**
	 * Returns module loaders for all JRipples modules intalled in Eclipse. 
	 * @return
	 * a set of module loaders for all JRipples modules intalled in Eclipse
	 */
	public static Set<String> getModulesLoaders() {

		return moduleHash.keySet();
	}

	
	/**
	 * Returns module loader for a module with the specified name.
	 * @param ModuleName
	 *	name of the module to find 
	 * @return
	 * 	module loader with the requested name if any, <br><code>null</code> otherwise
	 */
	public static JRipplesModuleLoader getModuleLoader(String ModuleName) {

		return moduleHash.get(ModuleName);

	}

	/**
	 * Returns a set with the names of all categories of modules, available in JRipples.
	 * @return
	 * 	a set with the names of all categories of modules, available in JRipples
	 */
	public static Set<String> getModulesCategories() {

		return categories.keySet();
	}

	/**
	 * Returns module loaders for a specified category of JRipples modules intalled in Eclipse.
	 * @param Category
	 * 	needed category of modules
	 * @return
	 * 	a set of module loaders 
	 */
	public static Set<String> getModuleCategoryLoaders(String Category) {

		return categories.get(Category);
	}

	/**
	 * Returns the name of an active module in the provided category.
	 * @param Category
	 * category, for which an active module should be returned
	 * @return
	 * name of an active module in the given category if any, <br>empty string otherwise
	 */
	public static String getActiveCategoryModuleName(String Category) {
		String active = activeCategoryModules.get(Category);
		if (active == null)
			//active = LoadDefaultCategoryModule(Category);
			return "";
		return active;
	}

	/**
	 * Returns the active module in the provided category.
	 * @param Category
	 *  category, for which an active module should be returned
	 * @return
	 *  active module in the given category if any, <br><code>null</code> otherwise
	 */
	public static JRipplesModuleInterface getActiveCategoryModule(
			String Category) {
		String activeModule=getActiveCategoryModuleName(Category);
		if (activeModule.compareTo("")!=0) return getModuleLoader(activeModule)
				.getModule();
		return null;
	}


	/* A role of a controller,
	 * which triggered this event, should be provided so that clients registered for receiving this
	 * kind of events, as well as modules involved in this event, can process 
	 * activation and deactivation events more appropriately.
	 */

	/**
	 * Activates a module with the given name.<br>
	 * A role of a controller,which triggered this event, should be provided so that clients registered for receiving this
	 * kind of events, as well as modules involved in this event, can process 
	 * activation and deactivation events more appropriately.
	 * 
	  @param module
	 *	 name of a module to activate
	 * @param controllerType
	 *   role of a controller that requests module activation
	 */
	public static void setActiveModule(String module,int controllerType) {
		JRipplesModuleLoader oldLoader=null;
		JRipplesModuleLoader moduleProspective = moduleHash
				.get(module);
		if (moduleProspective == null) return;
		boolean active=false;
		String activeCategoryModule=getActiveCategoryModuleName(moduleProspective.getCategory());
		if (activeCategoryModule!=null)
			if (activeCategoryModule.compareTo("")!=0) {
				if (activeCategoryModule.compareTo(module)!=0) {
					oldLoader=getModuleLoader(activeCategoryModule);
					if (oldLoader!=null) oldLoader.moduleDeactivate(controllerType);
				} else {
					active=true;
					//Do nothing - the new module is already in active modules' list
				}
			}
		activeCategoryModules.put(moduleProspective.getCategory(), module);
		boolean loadSuccessStatus=moduleProspective.moduleActivate(controllerType);
		
		notifyListeners(new ModuleSwitchEvent(moduleProspective,oldLoader,controllerType));
		
		if (!loadSuccessStatus) {

		

			// Switch to default if failed
			if (!active) { 
				boolean switchToDefault=MessageDialog.openQuestion(PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getShell(),
						"Critical error", "Unable to load requested module.");
				if (switchToDefault) setActiveModule(getDefaultCategoryModuleName(moduleProspective.getCategory()),controllerType);
				JRipplesLog.logInfo("Unable to load requested module, loading default module instead.");
			} else JRipplesLog.logInfo("Unable to load requested module.");
		} else {
			
		}

	}

	/**
	 * Deactivates a module with the given name. <br>
	 * A role of a controller,which triggered this event, should be provided so that clients registered for receiving this
	 * kind of events, as well as modules involved in this event, can process 
	 * activation and deactivation events more appropriately.
	 * @param module
	 *	 name of a module to deactivate
	 * @param controllerType
	 *   role of a controller that requests module deactivation
	 */
	public static void deactivateModule(String module, int controllerType) {
		
		JRipplesModuleLoader moduleProspective = moduleHash
				.get(module);
		if (moduleProspective == null) return;
		
		String activeCategoryModule=getActiveCategoryModuleName(moduleProspective.getCategory());
		if (activeCategoryModule!=null)
			if (activeCategoryModule.compareTo("")!=0)
				if (activeCategoryModule.compareTo(module)==0)
					activeCategoryModules.put(moduleProspective.getCategory(), "");
		
		moduleProspective.moduleDeactivate(controllerType);
		notifyListeners(new ModuleSwitchEvent(null,moduleProspective,controllerType));
	}
	// -------------------- Saving / Loading configurations ----------------------------

	
	/**
	 * Finds the name of a default module for a given category of modules.
	 * @param category
	 *  category of modules
	 * @return
	 *  the name of a default module for a given category of modules
	 */
	public static String getDefaultCategoryModuleName(String category) {
		String module = JRipplesPlugin.getDefault().getPluginPreferences().getString(category);
		return module;
	}

	/**
	 * Finds and activates a default module for a given category of modules.
	 * @param category
	 * 	category, for which a default module should be activated
	 * @return
	 *  name of the activated module
	 */
	public static String loadDefaultCategoryModule(String category) {
		
		String module=getDefaultCategoryModuleName(category);
		
		if (module != null) {
			setActiveModule(module,JRipplesModuleInterface.CONTROLLER_TYPE_MODULEPROXY);
			return module;
		}

		return null;
	}

//------------------------events ----------------
	
	/**
	 * Registers a listener who wish to receive updates on module switching events.
	 * @see ModuleSwitchEvent
	 * @see #removeJRipplesModuleSwitchingListener(ModuleSwitchListener)
	 * @param listener
	 * listener to register
	 */
	public static void addJRipplesModuleSwitchingListener(
			ModuleSwitchListener listener) {
		if (!moduleSwitchListeners.contains(listener))
			moduleSwitchListeners.add(listener);
	}

	/**
	 * Unregisters a listener, previously registered with {@link #addJRipplesModuleSwitchingListener(ModuleSwitchListener)}.
	 * @see ModuleSwitchEvent
	 * @see #removeJRipplesModuleSwitchingListener(ModuleSwitchListener)
	 * @param listener
	 * listener to unregister
	 */
	public static void removeJRipplesModuleSwitchingListener(
			ModuleSwitchListener listener) {
		moduleSwitchListeners.remove(listener);
	}

	private static void notifyListeners(ModuleSwitchEvent event) {
		ModuleSwitchListener[] listeners=moduleSwitchListeners.toArray(new ModuleSwitchListener[moduleSwitchListeners.size()]);
		for (int i=0;i<listeners.length;i++) {
			if (listeners[i]!=null)
				listeners[i].JRipplesModuleSwitchedChanged(event);
		}
		
	}
	
}
