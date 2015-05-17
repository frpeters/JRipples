package org.severe.jripples.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.severe.jripples.constants.JRipplesConstants;
import org.severe.jripples.core.JRipplesPlugin;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
				
		IPreferenceStore store = JRipplesPlugin.getDefault().getPreferenceStore();
		
		store.setDefault(JRipplesConstants.CATEGORY_MODULE_INCREMENTAL_CHANGE, "Concept Location");
		store.setDefault(JRipplesConstants.CATEGORY_MODULE_DEPENEDENCY_GRAPH, "Dependency Builder");
		store.setDefault(JRipplesConstants.CATEGORY_MODULE_PRESENTATION,"Hierarchy Presentation");
		store.setDefault(JRipplesConstants.CATEGORY_MODULE_ANALYSIS,"None");
				
		if (store.getString(JRipplesConstants.CATEGORY_MODULE_INCREMENTAL_CHANGE).isEmpty())
			store.setToDefault(JRipplesConstants.CATEGORY_MODULE_INCREMENTAL_CHANGE);
		
		if (store.getString(JRipplesConstants.CATEGORY_MODULE_DEPENEDENCY_GRAPH).isEmpty())
			store.setToDefault(JRipplesConstants.CATEGORY_MODULE_DEPENEDENCY_GRAPH);
		
		if (store.getString(JRipplesConstants.CATEGORY_MODULE_PRESENTATION).isEmpty())
			store.setToDefault(JRipplesConstants.CATEGORY_MODULE_PRESENTATION);
		
		if (store.getString(JRipplesConstants.CATEGORY_MODULE_ANALYSIS).isEmpty())
			store.setToDefault(JRipplesConstants.CATEGORY_MODULE_ANALYSIS);
	}

}
