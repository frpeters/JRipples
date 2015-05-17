package org.severe.jripples.preferences;

import java.util.Iterator;
import java.util.Set;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.severe.jripples.core.JRipplesPlugin;
import org.severe.jripples.modules.manager.ModuleProxy;


/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class MainJRipplesPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	public MainJRipplesPreferencePage() {
		super(GRID);
		IPreferenceStore store = JRipplesPlugin.getDefault().getPreferenceStore();
		setPreferenceStore(store);
		setDescription("Expand the tree to edit preferences for a specific module of JRipples.");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		Group group= new Group(getFieldEditorParent(),SWT.NULL);
		group.setText("Default modules for JRipples Start dialog.");
		
		Set <String> categories=ModuleProxy.getModulesCategories();
		for (Iterator <String> iter=categories.iterator();iter.hasNext();) {
			String category=iter.next();
			Set <String> modules= ModuleProxy.getModuleCategoryLoaders(category);
			String str[][]=new String [modules.size()][2];
			int i=0;
			for (Iterator <String> iter1=modules.iterator();iter1.hasNext();i++) {
				String module=iter1.next();
				str[i][0]=module;
				str[i][1]=module;
			}
			addField(new ComboFieldEditor(category,category,str,group));
		}
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		
	}
	
}