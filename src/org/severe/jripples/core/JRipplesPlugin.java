package org.severe.jripples.core;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.severe.jripples.eig.JRipplesEIG;
import org.severe.jripples.modules.manager.ModuleProxy;

/**
 * The main plugin class to be used in the workspace.
 */
public class JRipplesPlugin extends AbstractUIPlugin {
	// The shared instance.
	private static JRipplesPlugin plugin;

	// Resource bundle.
	private ResourceBundle resourceBundle;

	public static ModuleProxy MProxy;

	/**
	 * The constructor.
	 */
	public JRipplesPlugin() {
		super();
		
		plugin = this;

	}

	/**
	 * This method is called upon plug-in activation.
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		JRipplesEIG.initEIG();

		MProxy = new ModuleProxy();
		
	}

	/**
	 * This method is called when the plug-in is stopped.
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
		resourceBundle = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static JRipplesPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle, or 'key' if not
	 * found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = JRipplesPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle.
	 */
	public ResourceBundle getResourceBundle() {
		try {
			if (resourceBundle == null)
				resourceBundle = ResourceBundle
						.getBundle("org.severe.jripples.core.JRipplesPluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
		return resourceBundle;
	}

}
