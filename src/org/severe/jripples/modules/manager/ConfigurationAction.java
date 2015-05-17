/*
 * Created on Aug 5, 2005
 *
 */
package org.severe.jripples.modules.manager;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate;
import org.severe.jripples.constants.JRipplesConstants;
import org.severe.jripples.core.JRipplesConfigurationsManager;
import org.severe.jripples.modules.interfaces.JRipplesModuleInterface;
/**
 *
 * Pull-down action that provides a menu for switching between JRipples modules. 
 * Modules in the menu are organized by their category.
 * @author Maksym Petrenko
 * @see ModuleProxy
 * 
 */

public class ConfigurationAction implements IWorkbenchWindowPulldownDelegate,
		IMenuCreator {

	private IWorkbenchWindow window;

	private Menu menu;

	private IMenuManager manager = new MenuManager(JRipplesConstants.MANAGERS_CONFIGURATION);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {

		setMenu(null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {


		this.window = window;

		String categoryName;
		String moduleName;
		Action moduleSelectionAction;

		Set<String> menuCategories = ModuleProxy.getModulesCategories();
		Set<String> menuCategoryMembers;

		int i;
		Iterator<String> it;
		Iterator<String> it1;

		IMenuManager[] menuManager = new MenuManager[20];
	
		for (it = menuCategories.iterator(), i = 0; it.hasNext(); i++) {
			categoryName = it.next();
			menuManager[i] = new MenuManager(categoryName);
			menuManager[i].add(new GroupMarker("gr" + String.valueOf(i)));
			menuCategoryMembers = ModuleProxy
					.getModuleCategoryLoaders(categoryName);

			for (it1 = menuCategoryMembers.iterator(); it1.hasNext();) {
				moduleName = it1.next();

				moduleSelectionAction = new Action(moduleName, Action.AS_RADIO_BUTTON) {

					public void run() {
						if (this.isChecked())
							ModuleProxy.setActiveModule(this.getText(),JRipplesModuleInterface.CONTROLLER_TYPE_USER);
					}
				};

				moduleSelectionAction.setToolTipText(moduleName);

				if (ModuleProxy.getActiveCategoryModuleName(categoryName)
						.equals(moduleName))
					moduleSelectionAction.setChecked(true);
				moduleSelectionAction.setImageDescriptor(ModuleProxy
						.getModuleLoader(moduleName).getImageDescriptor());

				menuManager[i].add(moduleSelectionAction);

			}

			manager.add(menuManager[i]);
		}
		//Other plug-ins can contribute there actions here
		//manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));


	}

	private void refreshManager() {
		IContributionItem[] items = manager.getItems();
		IContributionItem[] actionItems = null;
		if (items != null)
			try {
				for (int i = 0; i < items.length; i++) {

					if (((items[i]) instanceof MenuManager)) {

						actionItems = ((IMenuManager) items[i]).getItems();
						if (actionItems != null) {
							for (int k = 0; k < actionItems.length; k++) {

								if ((actionItems[k]) instanceof ActionContributionItem) {

									if (ModuleProxy
											.getActiveCategoryModuleName(
													ModuleProxy
															.getModuleLoader(
																	((ActionContributionItem) actionItems[k])
																			.getAction()
																			.getText())
															.getCategory())
											.compareTo(
													((ActionContributionItem) actionItems[k])
															.getAction()
															.getText()) == 0) {

										((ActionContributionItem) actionItems[k])
												.getAction().setChecked(true);
									} else
										((ActionContributionItem) actionItems[k])
												.getAction().setChecked(false);
								}
							}
						}
					}
				}
			} catch (Exception e) {

			}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	
	/**
	 * Runs JRipples states configuration manager.
	 * @see JRipplesConfigurationsManager
	 * 
	 */
	public void run(IAction action) {
		JRipplesConfigurationsManager
				.displayDialog(JRipplesConfigurationsManager.CONFIGURATIONS_DIALOG_MANAGE_MODE);
		// TODO Add module preference settings here
	
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {

	}

	/**
	 * Returns manager with a menu for modules configuration.
	 * @return
	 *  manager with a menu for modules configuration
	 */
	public IMenuManager getManager() {
		return manager;
	}

	protected void fillMenu(Menu menu) {
		
		refreshManager();
		IContributionItem[] items=manager.getItems();
		for (int i=0;i<items.length;i++) {
			items[i].fill(menu,-1);
		}
		//manager.fill(menu, -1);
		
	}

	protected void addToMenu(Menu menu, IAction action) {
	
		action.setText(action.getText());
		ActionContributionItem item = new ActionContributionItem(action);
		item.fill(menu, -1);
	}

	/*private void initMenu() {
	
		// Add listener to repopulate the menu each time
		// it is shown because of dynamic nature
		menu.addMenuListener(new MenuAdapter() {
			public void menuShown(MenuEvent e) {
				Menu m = (Menu) e.widget;
				MenuItem[] items = m.getItems();
				for (int i = 0; i < items.length; i++) {
					items[i].dispose();
				}
				fillMenu(m);
			}
		});
	}
	*/


	private void setMenu(Menu menu) {

		if (this.menu != null) {
			this.menu.dispose();
		}
		this.menu = menu;
	}

	
	
	/*
	 * Returns a menu for modules configuration.
	 * @return
	 *  manager with a menu for modules configuration
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWindowPulldownDelegate#getMenu(org.eclipse.swt.widgets.Control)
	 */
	
	/**
	 *Returns a menu for modules configuration.
	 *@param parent
	 *	parent to create the menu in
	 * @return
	 *   menu for modules configuration
	 */
	public Menu getMenu(Control parent) {
		
		setMenu(new Menu(parent));
		fillMenu(menu);
		// initMenu();

		return menu;

	}

	/**
	 *Returns a menu for modules configuration.
	 *  @param 
	 *	parent to create the menu in
	 * @return
	 *   menu for modules configuration
	 */
	public Menu getMenu(Menu parent) {
		
		setMenu(new Menu(parent));
		fillMenu(menu);
		// initMenu();

		return menu;
	}

}
