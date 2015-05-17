package org.severe.jripples.modules.manager;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.severe.jripples.logging.JRipplesLog;
import org.severe.jripples.modules.interfaces.JRipplesModuleInterface;
import org.severe.jripples.modules.manager.internal.ImageCache;

/**
 * 
 * Provides a convenient access to the attributes of the module, defined in terms of JRipples extension points.
 * @author Maksym Petrenko
 * 
 */
public class JRipplesModuleLoader {

	private static final String ATT_ID = "id";

	private static final String ATT_NAME = "name";

	private static final String ATT_ICON = "icon";

	private static final String ATT_CLASS = "class";

	//private static final String ATT_CATEGORY = "category";

	//private static final JRipplesModuleLoader[] MODULES = {};

	private final String id;

	private final String category;

	private final String name;

	private final String className;

	private ImageDescriptor imageDescriptor;

	private final IConfigurationElement configElement;

	private static final ImageCache iconCache = new ImageCache();

	private JRipplesModuleInterface factory;

	/**
	 * Returns the id of the module.
	 * @return
	 * id of the module
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the name of the module.
	 * @return
	 * name of the module
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the name of the class name of this module.
	 * @return
	 * class name of the module
	 * @see #getModule()
	 */
	public String getClassName() {
		return className;
	}
	/**
	 * Returns the category of the module.
	 * @return
	 * category of the module
	 */
	public String getCategory() {
		return category.replace("_", " ");
	}

	/**
	 * Returns the icon of the module. Clients should not dispose the icon.
	 * @return
	 * icon of the module if any, <br><code>null</null> otherwise
	 * @see #getImageDescriptor()
	 */
	public Image getIcon() {
		return iconCache.getImage(getImageDescriptor());

	}

	protected boolean moduleActivate(int controllerType) {
		instantiateModule();
		if (factory == null)
			return false;
		factory.loadUp(controllerType);
		return true;

	}

	protected void moduleDeactivate(int controllerType) {
		if (factory != null)
			factory.shutDown(controllerType);
		factory = null;
	}

	
	/**
	 * Returns the image descriptor of the module's icon.
	 * @return
	 * image descriptor of the module's icon
	 * @see #getIcon()
	 */
	public ImageDescriptor getImageDescriptor() {
		if (imageDescriptor != null)
			return imageDescriptor;
		String iconName = configElement.getAttribute(ATT_ICON);
		if (iconName == null)
			return null;
		IExtension extension = configElement.getDeclaringExtension();

		String extendingPluginId = extension.getNamespaceIdentifier();
		imageDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(
				extendingPluginId, iconName);
		return imageDescriptor;

	}

	protected JRipplesModuleLoader(IConfigurationElement configElem) {

		this.configElement = configElem;
		category = configElem.getName();
		id = getAttribute(configElem, ATT_ID, null);
		
		name = getAttribute(configElem, ATT_NAME, id);

		className = getAttribute(configElem, ATT_CLASS, null);

	}

	private static String getAttribute(IConfigurationElement configElem,
			String name, String defaultValue) {
		String value = configElem.getAttribute(name);

		if (value != null)
			return value;
		if (defaultValue != null)
			return defaultValue;
		throw new IllegalArgumentException("Missing " + name + " attribute");
	}

	// Creating instance of requred module



	/**
	 * Returns the actual object of this module implementing {@link JRipplesModuleInterface} interface.
	 * @return
	 * the actual object of this module implementing {@link JRipplesModuleInterface} interface
	 * @see #getClassName()
	 */
	public JRipplesModuleInterface getModule() {
		if (factory == null)
			instantiateModule();
		return factory;
	}

	private void instantiateModule() {
		try {
			
			factory = (JRipplesModuleInterface) configElement
					.createExecutableExtension(ATT_CLASS);
		} catch (Exception e) {
			JRipplesLog.logError("Failed to instantiate factory: "
					+ configElement.getAttribute(ATT_CLASS) + " in type: " + id
					+ " in plugin: "
					+ configElement.getDeclaringExtension().getNamespaceIdentifier(),e);
		}
	}
}
