/*
 * Created on Dec 5, 2005
 *
 */
package org.severe.jripples.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.internal.ui.DefaultLabelProvider;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.ide.IDE;
import org.severe.jripples.constants.JRipplesConstants;
import org.severe.jripples.eig.JRipplesEIG;
import org.severe.jripples.eig.JRipplesEIGNode;
import org.severe.jripples.logging.JRipplesLog;
import org.severe.jripples.modules.interfaces.JRipplesAnalysisModuleInterface;
import org.severe.jripples.modules.interfaces.JRipplesDependencyGraphModuleInterface;
import org.severe.jripples.modules.interfaces.JRipplesICModuleInterface;
import org.severe.jripples.modules.interfaces.JRipplesModuleInterface;
import org.severe.jripples.modules.interfaces.JRipplesPresentationModuleInterface;
import org.severe.jripples.modules.manager.ModuleProxy;

/**
 * Action for starting JRipples analysis Wizard, accessible through "JRipples > Start" menu. 
 * @author Maksym Petrenko
 * 
 */
public class JRipplesStart implements IWorkbenchWindowPulldownDelegate {

	private static WizardDialog wd;

	private static IProject selectedProject;

	private static IType selectedClass;

	private Set<String> selectedModules;

	private class ProjectLabelProvider extends DefaultLabelProvider {
		public String getText(Object obj) {
			return (((IProject) obj).getName());
		}

		public Image getColumnImage(Object obj, int index) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(
					IDE.SharedImages.IMG_OBJ_PROJECT);
		}
	}

	class MyWizard extends Wizard {

		public boolean existsProject(String pName) {
			IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
					.getProjects();
			for (int i = 0; i < projects.length; i++) {
				if (projects[i].getName().compareTo(pName) == 0)
					return true;
			}
			return false;
		}

		// the model object.

		public MyWizard() {
			setWindowTitle("JRipples analysis configuration");
			setNeedsProgressMonitor(true);

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.wizard.IWizard#addPages()
		 */
		public void addPages() {
			addPage(page1);
			addPage(page2);
			
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.wizard.IWizard#performFinish()
		 */
		public boolean performFinish() {
			String className = page1.className.getText();
			String projectName = page1.projectName.getText();
			if ((projectName == null)) {
				MessageDialog.openWarning(getShell(), "Project info missing",
						"Please select project.");
				return false;
			}
			;
			if ((projectName.compareTo("") == 0)) {
				MessageDialog.openWarning(getShell(), "Project info missing",
						"Please select project.");
				return false;
			}
			;
			if ((className == null)) {
				MessageDialog.openWarning(getShell(), "Class info missing",
						"Please select main class.");
				return false;
			}
			;
			if ((className.compareTo("") == 0)) {
				MessageDialog.openWarning(getShell(), "Class info missing",
						"Please select main class.");
				return false;
			}
			;

			if (!existsProject(projectName)) {
				MessageDialog.openError(getShell(), "Incorrect project info",
						"Please select valid project name.");
				return false;
			}
			;
			IProject chosenProject = ResourcesPlugin.getWorkspace().getRoot()
					.getProject(projectName);
			IJavaProject javaProject = JavaCore.create(chosenProject);

			IType chosenClass = null;
			try {
				chosenClass = javaProject.findType(className);
			} catch (JavaModelException e) {
				MessageDialog.openError(getShell(), "Incorrect class info",
						"Please select valid class name  on the first page.");
				return false;
			}
			;
			if ((chosenClass == null)) {
				MessageDialog.openError(getShell(), "Incorrect class info",
						"Please select valid class name  on the first page.");
				return false;
			}
			;
			/*
			if ((JRipplesEIG.getProject() == chosenProject)
					&& (JRipplesEIG.getMainClass() == chosenClass)) {
				((JRipplesPresentationModuleInterface) ModuleProxy
						.GetActiveCategoryModule("Presentation")).DisplayEIG();
				MessageDialog
						.openInformation(PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow().getShell(),
								"Already analyzed",
								"Selected project and class are already under analysis. No actions performed.");
				return true;
			}
			*/
			Set<String> chosenModules = new HashSet<String>();
			for (int i = 0; i < page2.categoryCombos.length; i++) {

				chosenModules.add(page2.categoryCombos[i].getText());
			}
			selectedProject = chosenProject;
			selectedClass = chosenClass;
			selectedModules = chosenModules;

			return true;

		}

		private Page1 page1 = new Page1();

		private Page2 page2 = new Page2();

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.wizard.IWizard#performCancel()
		 */
		public boolean performCancel() {
			/*
			 * boolean ans = MessageDialog.openConfirm(getShell(),
			 * "Confirmation", "Are you sure to cancel the task?"); if(ans)
			 * return true; else return false;
			 */
			return true;
		}

		private class Page1 extends WizardPage {
			final Page1 page;
			private boolean mainClassOnly;
			public Page1() {
				super("Page 1");
				mainClassOnly=true;
				setTitle("[Project for analysis selection]");
				setDescription("Page 1 of JRipples analysis configuration");
				setMessage("Select the project", 1);
				// setPageComplete(false);
				page = this;
				

			}
			
			public void performHelp() {
				PlatformUI.getWorkbench().getHelpSystem().displayHelp(JRipplesConstants.HELP_CONTEXT_START);
			}
			public boolean changeFieldsText() {
				String text = projectName.getText();
				String text2 = className.getText();
				if (text == null) {
					setMessage("Select the project", 1);
					setPageComplete(false);
					return false;
				} else if ((text.compareTo("")) == 0) {
					setMessage("Select the project", 1);
					setPageComplete(false);
					return false;
				} else if (!existsProject(text)) {
					setMessage("Project doesn't exist", 2);
					setPageComplete(false);
					return false;
				}
				if (text2 == null) {
					setMessage("Select the main class", 1);
					setPageComplete(false);
					return false;
				} else if ((text2.compareTo("")) == 0) {
					setMessage("Select the main class", 1);
					setPageComplete(false);
					return false;
				}
				setMessage("Run the analysis", 1);
				setPageComplete(true);
				
				return true;
			}

			
			public void createControl(Composite composite) {
				Composite pane = new Composite(composite, SWT.NULL);
				GridLayout layout = new GridLayout();
				layout.numColumns = 3;
				pane.setLayout(layout);
				Label label = new Label(pane, SWT.NULL);
				label.setText("Project:");

				projectName = new Text(pane, SWT.SINGLE | SWT.BORDER);
				{
					GridData data = new GridData(GridData.FILL_HORIZONTAL);
					projectName.setLayoutData(data);
				}
				IStructuredSelection selectedWorkspaceItems = null;
				
				
				
				if (PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getActivePage().getSelection() != null) {
					if (PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getActivePage().getSelection() instanceof IStructuredSelection)
						selectedWorkspaceItems = (IStructuredSelection) PlatformUI
								.getWorkbench().getActiveWorkbenchWindow()
								.getActivePage().getSelection();
				}
				if (selectedWorkspaceItems != null) {
					List<IResource> items = new ArrayList<IResource>();
					Iterator iter = selectedWorkspaceItems.iterator();
					while (iter.hasNext()) {
						Object object = iter.next();
						if (!(object instanceof IAdaptable))
							continue;
						if (!(object instanceof IAdaptable))
							continue;
						IResource item = (IResource) ((IAdaptable) object)
								.getAdapter(IResource.class);
						if (item == null)
							continue;
						items.add(item);
					}

					if (items.size() != 0) {

						chosenProject = items.get(0).getProject();
						projectName.setText(chosenProject.getName());

					}
				}

				projectName.addModifyListener(new ModifyListener() {
					public void modifyText(ModifyEvent e) {
						changeFieldsText();

					}
				});
				final Button button0 = new Button(pane, SWT.PUSH | SWT.RIGHT);

				button0.setText("Browse");
				button0.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent event) {

						ILabelProvider labelProvider = (ILabelProvider) new ProjectLabelProvider();
						ElementListSelectionDialog dialog = new ElementListSelectionDialog(
								button0.getShell(), labelProvider);
						dialog.setTitle("Select Project");
						dialog.setMessage("Choose the project to analyse:");
						dialog.setElements(ResourcesPlugin.getWorkspace()
								.getRoot().getProjects());
						// dialog.setInitialSelections(selectedWorkspaceProjects);

						if (dialog.open() == ElementListSelectionDialog.CANCEL) {
							return;
						}
						Object[] results = dialog.getResult();
						if ((results == null)) {
							return;
						}
						IProject type = (IProject) results[0];
						if (type != null) {
							if (chosenProject != type) {
								chosenClass = null;
								className.setText("");
							}
							;
							chosenProject = type;
							projectName.setText(type.getName());

						}

					}
				}

				);
				label = new Label(pane, SWT.NULL);
				label.setText("Main class:");
				className = new Text(pane, SWT.SINGLE | SWT.BORDER);
				{
					GridData data = new GridData(GridData.FILL_HORIZONTAL);
					className.setLayoutData(data);
				}

				className.addModifyListener(new ModifyListener() {
					public void modifyText(ModifyEvent e) {
						changeFieldsText();

					}
				});

				final Button button = new Button(pane, SWT.PUSH);
				button.setText("Search");
				button.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent event) {

						if (projectName.getText() != null) {
							if (projectName.getText().compareTo("") != 0)
								chosenProject = ResourcesPlugin.getWorkspace()
										.getRoot().getProject(
												projectName.getText());
							else
								chosenProject = null;
						} else
							chosenProject = null;
						
						IJavaProject javaProject = JavaCore
								.create(chosenProject);

						IJavaSearchScope searchScope = null;
						if ((javaProject == null) || !javaProject.exists()) {
							IJavaModel javaModel = JavaCore.create(ResourcesPlugin.getWorkspace()
									.getRoot());
							try {
							searchScope = SearchEngine.createJavaSearchScope(
									javaModel.getJavaProjects(), IJavaSearchScope.SOURCES);
							} catch (Exception e) {
								JRipplesLog.logError(e);
							}
							
						} else {
							searchScope = SearchEngine.createJavaSearchScope(
									new IJavaElement[] { javaProject }, IJavaSearchScope.SOURCES);
						}
						int constraints = IJavaElementSearchConstants.CONSIDER_BINARIES;
						SelectionDialog dialog=null;
						if (mainClassOnly)  dialog = JavaUI.createMainTypeDialog(
								button.getShell(), JRipplesStart.getWd(),
								searchScope, constraints, false, "*");
						else {
							try {
							constraints = IJavaElementSearchConstants.CONSIDER_CLASSES_AND_INTERFACES;
							dialog = JavaUI.createTypeDialog(
								button.getShell(), JRipplesStart.getWd(),
								searchScope, constraints, false, "*");
							} catch (Exception e) {
								JRipplesLog.logError(e);
							}
						}
						if (dialog==null) return;
						dialog.setTitle("Choose main type");
						dialog
								.setMessage("Choose a main type to start analysis:");
						if (dialog.open() == SelectionDialog.CANCEL) {

							return;
						}
						Object[] results = dialog.getResult();
						if ((results == null)) {
							return;
						}
						IType type = (IType) results[0];
						if (type != null) {

							chosenClass = type;
							className.setText(type.getFullyQualifiedName());
							chosenProject = type.getJavaProject().getProject();
							projectName.setText(chosenProject.getName());

						}
					}
				}

				);
				
				label = new Label(pane, SWT.NULL);
				label.setText("Search only for classes that define \"main\" method");
				GridData gridData = new GridData(GridData.VERTICAL_ALIGN_END);
			    gridData.horizontalSpan = 2;
			    gridData.horizontalAlignment = GridData.FILL;
			    label.setLayoutData(gridData);

				final Button button1 = new Button(pane, SWT.CHECK);
				button1.setSelection(mainClassOnly);
				button1.addSelectionListener(new SelectionListener(){

					public void widgetDefaultSelected(SelectionEvent e) {
						mainClassOnly=button1.getSelection();
						
					}

					public void widgetSelected(SelectionEvent e) {
						mainClassOnly=button1.getSelection();
						
					}
					
				});
				
				setControl(pane);
				changeFieldsText();
			}

			public String getText() {
				return "JRipples start dialog";
			}

			Text projectName, className;

			IType chosenClass;

			IProject chosenProject;
		}

		private class Page2 extends WizardPage {

			final Page2 page;

			final Combo[] categoryCombos = new Combo[ModuleProxy
					.getModulesCategories().size()];;

			public Page2() {

				super("Page 2");

				setTitle("[Modules configuration]");
				setDescription("Page 2 of JRipples analysis configuration");
				setMessage(
						"Select the module configuration or leave for deafult settings",
						1);
				// setPageComplete(false);
				page = this;

			}
			public void performHelp() {
				PlatformUI.getWorkbench().getHelpSystem().displayHelp(JRipplesConstants.HELP_CONTEXT_START);
			}
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
			 */
			public void createControl(Composite composite) {

				Composite pane = new Composite(composite, SWT.NULL);
				GridLayout layout = new GridLayout();

				layout.numColumns = 2;
				pane.setLayout(layout);
				Label label;
				Set<String> menuCategoryMembers;
				Set<String> categories = ModuleProxy.getModulesCategories();
				int i = 0;
				for (Iterator<String> c_iter = categories.iterator(); c_iter.hasNext();) {
					String category =  c_iter.next();

					label = new Label(pane, SWT.NULL);
					label.setText(category.replace("_", " ") + ":");
					categoryCombos[i] = new Combo(pane, SWT.READ_ONLY);

					menuCategoryMembers = ModuleProxy
							.getModuleCategoryLoaders(category);
					for (Iterator<String> it1 = menuCategoryMembers.iterator(); it1
							.hasNext();) {
						String moduleName = (String) it1.next();
						categoryCombos[i].add(moduleName);
					}
					categoryCombos[i].setText(ModuleProxy
							.getDefaultCategoryModuleName(category));
					
					{
						GridData data = new GridData(GridData.FILL_HORIZONTAL);
						categoryCombos[i].setLayoutData(data);
					}
					i++;
				}

				label = new Label(pane, SWT.NULL);
				label = new Label(pane, SWT.NULL);
				label = new Label(pane, SWT.NULL);
				final Button button3 = new Button(pane, SWT.PUSH | SWT.RIGHT);

				button3.setText("Restore Defaults");
				button3.addSelectionListener(new SelectionAdapter() {
					public void widgetSelected(SelectionEvent event) {
						for (int i = 0; i < categoryCombos.length; i++) {
							categoryCombos[i]
									.setText(ModuleProxy
											.getDefaultCategoryModuleName(ModuleProxy
													.getModuleLoader(
															categoryCombos[i]
																	.getText())
													.getCategory()));
						}

					}
				});
				label = new Label(pane, SWT.NULL);
				label = new Label(pane, SWT.NULL);
				setControl(pane);
				

			}

			public String getText() {
				return "JRipples start dialog";
			}
		}
	}

	// private ILaunchConfigurationDialog fDialog;

	
	private static WizardDialog getWd() {
		return wd;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWindowPulldownDelegate#getMenu(org.eclipse.swt.widgets.Control)
	 */
	public Menu getMenu(Control parent) {

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getShell();
		wd = new WizardDialog (shell, new MyWizard());
		wd.setHelpAvailable(true);
			
		if (wd.open() == WizardDialog.CANCEL)
			return;

		launchJRipplesAnalysis();
		return;

	}

	private void launchJRipplesAnalysis() {
		if (selectedModules==null) return;
		for (Iterator<String> iter = selectedModules.iterator(); iter.hasNext();) {
			ModuleProxy.setActiveModule(iter.next(),JRipplesModuleInterface.CONTROLLER_TYPE_START);
		}
		JRipplesEIG.initEIG();
		JRipplesEIG.setProject(selectedProject);
		JRipplesEIG.setMainClass(selectedClass);
		((JRipplesDependencyGraphModuleInterface) ModuleProxy
				.getActiveCategoryModule(JRipplesConstants.CATEGORY_MODULE_DEPENEDENCY_GRAPH)).AnalyzeProject();
		((JRipplesICModuleInterface) ModuleProxy
				.getActiveCategoryModule(JRipplesConstants.CATEGORY_MODULE_INCREMENTAL_CHANGE))
				.InitializeStage();
		((JRipplesAnalysisModuleInterface) ModuleProxy
				.getActiveCategoryModule(JRipplesConstants.CATEGORY_MODULE_ANALYSIS)).AnalyzeProject();
		((JRipplesPresentationModuleInterface) ModuleProxy
				.getActiveCategoryModule(JRipplesConstants.CATEGORY_MODULE_PRESENTATION)).DisplayEIG();
		((JRipplesPresentationModuleInterface) ModuleProxy
				.getActiveCategoryModule(JRipplesConstants.CATEGORY_MODULE_PRESENTATION))
				.RefreshEIGAtNodes(new HashSet<JRipplesEIGNode>());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
	 *      org.eclipse.jface.viewers.ISelection)
	 */

	public void selectionChanged(IAction action, ISelection selection) {

	}

}
