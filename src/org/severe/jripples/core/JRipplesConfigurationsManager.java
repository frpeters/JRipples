package org.severe.jripples.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.severe.jripples.constants.JRipplesConstants;
import org.severe.jripples.logging.JRipplesLog;
import org.severe.jripples.modules.manager.ModuleProxy;
import org.severe.jripples.states.JRipplesStatesSaveAndLoad;

/**
 * 
 * A class that provides a number of utilities for management of the JRipples states,
 *  where the state is a combination of JRipples EIG state and state of the active modules.
 * @author Maksym Petrenko
 * 
 */
public class JRipplesConfigurationsManager {

	/**
	 * constant indicating that JRipples states dialog should be displayed in Save mode
	 * <br>to be used with {@link #displayDialog(String)}
	 */
	public static final String CONFIGURATIONS_DIALOG_SAVE_MODE = "save";
	/**
	 * constant indicating that JRipples states dialog should be displayed in Load mode
	 * <br>to be used with {@link #displayDialog(String)}
	 */
	public static final String CONFIGURATIONS_DIALOG_LOAD_MODE = "load";
	/**
	 * constant indicating that JRipples states dialog should be displayed in Manage mode
	 * <br>to be used with {@link #displayDialog(String)}
	 */
	public static final String CONFIGURATIONS_DIALOG_MANAGE_MODE = "manage";

	private static void fillTable(Table table) {
		table.removeAll();

		// column2.setWidth(220);
		if (Configurations == null) getConfigurationsFromFile();

		TableItem item1;

		if (Configurations != null) {
			String confName;
			String confComment;
			String confDate;
			for (Iterator<String> iter=Configurations.keySet().iterator();iter.hasNext();) {
				confName=(String) iter.next();
				confComment=Configurations.get(confName)[0];
				confDate=Configurations.get(confName)[1];
				Date date=new Date(Long.parseLong(confDate)); 
				item1 = new TableItem(table, SWT.NULL);
				item1.setText(new String[] {confName,confComment,DateFormat.getDateTimeInstance().format(date)});
				

			}
		}

	}

	/**
	 * Opens JRipples states dialog to either load, save, or manage JRipples states,
	 *  where the state is a combination of JRipples EIG state and state of the active modules.   
	 * @param mode
	 * 	mode, in which JRipples states dialog should be displayed.<br>Can be one of the following:
	 * <ul>
	 * <li>{@link #CONFIGURATIONS_DIALOG_LOAD_MODE}
	 * <li>{@link #CONFIGURATIONS_DIALOG_SAVE_MODE}
	 * <li>{@link #CONFIGURATIONS_DIALOG_MANAGE_MODE}
	 * </ul>
	 *  
	 */
	public static void displayDialog(String mode) {
		final Shell shell = new Shell(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getShell(), SWT.DIALOG_TRIM
				| SWT.APPLICATION_MODAL);
		shell.setText("JRipples Configurations Manager");
		shell.setLayout(new GridLayout(4, false));
		shell.setBounds(100, 100, 400, 400);

		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.grabExcessHorizontalSpace = false;
		gd.horizontalSpan = 4;
		final Table table = new Table(shell, SWT.SINGLE | SWT.BORDER
				| SWT.FULL_SELECTION | SWT.V_SCROLL );
		table.setLayoutData(gd);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		table.getHorizontalBar().setVisible(false);
		
		TableColumn column1 = new TableColumn(table, SWT.NULL);
		column1.setText("Name");

		TableColumn column2 = new TableColumn(table, SWT.NULL);
		column2.setText("Info");
		
		TableColumn column3 = new TableColumn(table, SWT.NULL);
		column3.setText("Date of last modification");

		
		fillTable(table);
		column1.pack();
		column2.pack();
		column3.pack();

						
		if (mode.compareTo(CONFIGURATIONS_DIALOG_LOAD_MODE) == 0) {
			table.addSelectionListener(new SelectionAdapter() {
				public void widgetDefaultSelected(SelectionEvent event) {
					TableItem[] selected = table.getSelection();
					if (selected.length > 0) {
						String confName=selected[0].getText();
						shell.close();
						loadConfigurationFromFile(confName);
						
					}
				}
			});

			final Button b1 = new Button(shell, SWT.PUSH);
			b1.setText("Load");
			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 4;
			b1.setLayoutData(gd);
			b1.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					TableItem[] selected = table.getSelection();
					if (selected.length > 0) {
						loadConfigurationFromFile(selected[0].getText());
						shell.close();
					}
				}

			});

		} else if (mode.compareTo(CONFIGURATIONS_DIALOG_SAVE_MODE) == 0) {

			Label lable = new Label(shell, SWT.NONE);
			lable.setText("Name:");

			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 3;
			gd.grabExcessHorizontalSpace = true;

			final Text tx = new Text(shell, SWT.NULL);
			tx.setLayoutData(gd);
			tx.setText("Configuration");

			lable = new Label(shell, SWT.NONE);
			lable.setText("Info:");
			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 3;
			gd.grabExcessHorizontalSpace = true;
			final Text info = new Text(shell, SWT.NULL);
			info.setLayoutData(gd);
			info.setText("");

			final Button b1 = new Button(shell, SWT.PUSH);
			b1.setText("Save");
			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 4;
			b1.setLayoutData(gd);
			b1.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					boolean tmpFlag=true;
					getConfigurationsFromFile();
					
					if (Configurations.containsKey(tx.getText())) {
						tmpFlag=false;
						MessageDialog dialog=new MessageDialog(PlatformUI
								.getWorkbench().getActiveWorkbenchWindow()
								.getShell(), "Configuration exists", null, "A configuration with the ["+tx.getText()+"] name already exists. Do you want to replace it?",MessageDialog.WARNING, new String[] {"Yes","No"},1);
						dialog.open();
						if (dialog.getReturnCode()==0) tmpFlag=true;
					}
					
					if (tmpFlag) {
						saveCurrentConfiguration(tx.getText(), info.getText());
						shell.close();
					}
				}

			});
			table.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					TableItem[] selected = table.getSelection();
					if (selected.length > 0) {
						tx.setText(selected[0].getText());
						info.setText(selected[0].getText(1));
					}
				}
			});

			table.addSelectionListener(new SelectionAdapter() {
				public void widgetDefaultSelected(SelectionEvent event) {
					TableItem[] selected = table.getSelection();
					if (selected.length > 0) {
						saveCurrentConfiguration(tx.getText(), info.getText());
						shell.close();
					}
				}
			});

		}

		if (mode.compareTo(CONFIGURATIONS_DIALOG_MANAGE_MODE) == 0) {
			final Button b3 = new Button(shell, SWT.PUSH);
			b3.setText("Delete");

			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			b3.setLayoutData(gd);

			b3.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					TableItem[] selected = table.getSelection();
					if (selected.length > 0) {
						deleteConfiguration(selected[0].getText());
						fillTable(table);
					}
				}

			});

			final Button b4 = new Button(shell, SWT.PUSH);
			b4.setText("Rename");

			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			b4.setLayoutData(gd);
			b4.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					TableItem[] selected = table.getSelection();
					
					if (selected.length > 0) {
						InputDialog id1 = new InputDialog(PlatformUI
								.getWorkbench().getActiveWorkbenchWindow()
								.getShell(), "Configuration name",
								"Enter new configuration name:", selected[0]
										.getText(), null);
						id1.open();
						InputDialog id2 = new InputDialog(PlatformUI
								.getWorkbench().getActiveWorkbenchWindow()
								.getShell(), "Configuration info",
								"Enter new configuration info:", selected[0]
										.getText(1), null);
						id2.open();
						renameConfiguration(selected[0].getText(), id1
								.getValue(), id2.getValue());
						fillTable(table);
					}
				}

			});

		}

		shell.open();

	}

	// -------------------------------Load / Save---------------------------

	private static HashMap<String,String[]> Configurations;

	/**
	 * Composes a name of JRipples configuration file
	 * @param name
	 * @return
	 */
	private static File getConfigurationFile(String name) {
		return JRipplesPlugin.getDefault().getStateLocation().append(
				name + ".jar").toFile();
	}

	/**
	 * Loads a list of available saved states into the private variable <code>Configurations</code>.
	 */
	private static void getConfigurationsFromFile () {
		Configurations=null;
		Configurations=new HashMap<String,String[]>();
		
		String file_name = JRipplesConstants.JRIPPLES_CONF_FILE;
		if (file_name == null)
			return;
		if (file_name.compareToIgnoreCase("") == 0)
			return;
			
		JarFile jarFile=null;
		Enumeration<JarEntry> jarEntries;
		if (getConfigurationFile(file_name).exists()) {
			try {
				jarFile=new JarFile (getConfigurationFile(file_name));
				if (jarFile==null) return;
				jarEntries=jarFile.entries();	
				
				JarEntry jarEntry;
				while (jarEntries.hasMoreElements()){
					jarEntry=(JarEntry) jarEntries.nextElement();
					
					Configurations.put(jarEntry.getName().split(".jrp")[0], new String[] {jarEntry.getComment(),""+jarEntry.getTime()});
				}
			} catch (FileNotFoundException e) {
				// log the exception and move on
				JRipplesLog.logError(e);
			} catch (Exception e) {
				// log the exception and move on
				JRipplesLog.logError(e);
			} finally {
				try {
					
					if (jarFile != null)
						jarFile.close();
					
				} catch (IOException e) {
					JRipplesLog.logError(e);
				}
			}
		}

		else {
			//JRipplesLog.logInfo("Configuration file does not exist, creating a new one.");
			//Do nothing
		}
		;
		return;
		
	}
	

	
	
	/**
	 * Loads a saved JRipples state (that is, EIG and active modules). 
	 * @param confName
	 * name of the saved state to load
	 */
	public static void loadConfigurationFromFile(String confName) {
		String file_name = JRipplesConstants.JRIPPLES_CONF_FILE;
					
		if (file_name == null)
			return;
		if (file_name.compareToIgnoreCase("") == 0)
			return;
		String entryName=confName;
		if (!entryName.contains(".jrp")) entryName=entryName+".jrp";
		
		InputStreamReader jarReader=null;
		JarFile jarFile=null;	
		
		if (getConfigurationFile(file_name).exists()) {
			try {
				
				jarFile=new JarFile (getConfigurationFile(file_name));
				JarEntry jarEntry=jarFile.getJarEntry(entryName);
				if (jarEntry==null) return ;
				JRipplesStatesSaveAndLoad.loadJRipplesStateFromStream(jarFile.getInputStream(jarEntry));
				
			} catch (FileNotFoundException e) {
				JRipplesLog.logError(e);
			} catch (Exception e) {
				JRipplesLog.logError(e);
			} finally {
				try {
					if (jarReader != null)
						jarReader.close();
					if (jarFile != null)
						jarFile.close();
				} catch (IOException e) {
					JRipplesLog.logError(e);
				}
			}
		}
		else {
			JRipplesLog.logInfo("Configuration file does not exist.");
		}
		
	}


	private static void writeConfigurationToFile(String conf_name,String conf_info, HashSet<String> configurationsToSkip, HashMap <String,String[]> configurationsToRename) {
		String file_name = JRipplesConstants.JRIPPLES_CONF_FILE;
	       
        File newJarFile = getConfigurationFile(file_name+"_tmp1");
		File oldJarFile = getConfigurationFile(file_name+"_tmp2");
		File jarFile = getConfigurationFile(file_name);
		JarOutputStream jarOutput=null;
        OutputStreamWriter jarWriter=null;
        JarFile jarInput=null;
		
		if (jarFile.exists()) {
			jarFile.renameTo(oldJarFile);
			jarFile = getConfigurationFile(file_name);
		}
		
	    
		// Initialize a flag that will indicate that the jar was updated.
        
		//boolean jarUpdated = false;
		try {
			try {
	        // Write new memo if specified
	        if ((conf_name!=null) && (conf_name.compareTo("")!=0) ) {
	        	jarOutput=new JarOutputStream(new FileOutputStream(newJarFile));
	        	
			    try {
			            	jarWriter=new OutputStreamWriter(jarOutput);
			            	JarEntry entry = new JarEntry(conf_name+".jrp");
			            	entry.setComment(conf_info);
			            	jarOutput.putNextEntry(entry);
			            	
			            	JRipplesStatesSaveAndLoad.saveJRipplesStateToStream(jarWriter, conf_name, conf_info);
			    			
			    }  catch (Exception e) {
		 			JRipplesLog.logError("Unable to save cofiguration file.", e);
		         } finally {
			    	if (jarWriter!=null)
			    		jarWriter.close();
			    	if (jarOutput!=null)
			    		jarOutput.close();
			    }
	        }
		            
	
	        jarOutput=new JarOutputStream(new FileOutputStream(jarFile));
	       	byte[] buffer = new byte[1024];
	       	int bytesRead;
	       	
	       	if (newJarFile.exists())  {
			    // Loop through the jar entries of the new file and add them to the main jar
			    try{        	
			       	jarInput= new JarFile(newJarFile);
				    for (Enumeration<JarEntry> entries = jarInput.entries(); entries.hasMoreElements(); ) {
				    	// Get the next entry.
			            JarEntry entry = entries.nextElement();
			            // Get an input stream for the entry.
			            InputStream entryStream = jarInput.getInputStream(entry);
			            // Read the entry and write it to the temp jar.
			            jarOutput.putNextEntry(entry);
			            while ((bytesRead = entryStream.read(buffer)) != -1) {
			            	jarOutput.write(buffer, 0, bytesRead);
			            }
			            if (entryStream!=null) 
			        		entryStream.close();
				    }
			    } catch (Exception e) {
		 			JRipplesLog.logError("Unable to save cofiguration file.", e);
		        } finally {
		        	
			    	if (jarInput!=null)
			    		jarInput.close();
			    	if (newJarFile.exists()) newJarFile.delete();
			    }  
	       	}
	       	
	        if (oldJarFile.exists())  {
		        // Loop through the jar entries of the old file and add them to the main jar
			    try{        	
			       	jarInput= new JarFile(oldJarFile);
				    for (Enumeration<JarEntry> entries = jarInput.entries(); entries.hasMoreElements(); ) {
				    	// Get the next entry.
			            JarEntry entry =  entries.nextElement();
			            
				        if (!(((conf_name!=null) && (entry.getName().equals(conf_name+".jrp"))) || ((configurationsToSkip!=null) && (configurationsToSkip.contains(entry.getName().split(".jrp")[0]))) )) {
				        	if (configurationsToRename.containsKey(entry.getName().substring(0,entry.getName().indexOf(".jrp")))){
				        		JarEntry newEntry = new JarEntry(configurationsToRename.get(entry.getName().substring(0,entry.getName().indexOf(".jrp")))[0]+".jrp");
				            	newEntry.setComment(configurationsToRename.get(entry.getName().substring(0,entry.getName().indexOf(".jrp")))[1]);
				            	jarOutput.putNextEntry(newEntry);
				        	} else jarOutput.putNextEntry(entry);

				        	// Get an input stream for the entry.
				            InputStream entryStream = jarInput.getInputStream(entry);

				            // Read the entry and write it to the temp jar.
				            while ((bytesRead = entryStream.read(buffer)) != -1) {
				            	jarOutput.write(buffer, 0, bytesRead);
				            }
				            if (entryStream!=null) 
				        		entryStream.close();
			            }
				    }
			    } catch (Exception e) {
		 			JRipplesLog.logError("Unable to save cofiguration file.", e);
		        } finally {
		        	
			    	if (jarInput!=null)
			    		jarInput.close();
			    	if (oldJarFile.exists()) oldJarFile.delete();
			    }
	        }
        
		}  catch (Exception e) {
 			JRipplesLog.logError("Unable to save cofiguration file.", e);
         } finally {
        	 if (jarOutput!=null)
 	    		 jarOutput.close();
        	
	    }    
		}  catch (Exception e) {
 			JRipplesLog.logError("Unable to save cofiguration file.", e);
        } finally {
        	 getConfigurationsFromFile();
        }
       
	}



	/**
	 * Saves current JRipples state under the given name, 
	 * where the state is a combination of JRipples EIG state and state of the active modules 
	 * @param conf_name
	 * name to save the current configuration under
	 */
	public static void saveCurrentConfiguration(String conf_name) {
		saveCurrentConfiguration(conf_name, "");
	}


	/**
	  * Saves current JRipples state under the given name and description, 
	 * where the state is a combination of JRipples EIG state and state of the active modules 
	 * @param conf_name
	 * name to save the current configuration under
	 * @param conf_info
	 * description to save the current configuration under
	 */
	public static void saveCurrentConfiguration(String conf_name,String conf_info) {
		writeConfigurationToFile(conf_name, conf_info,new HashSet<String>(),new HashMap<String,String[]>());
	}
	
	
	private static void deleteConfiguration(String confName) {
		HashSet<String> excludedElements=new HashSet<String>();
		excludedElements.add(confName);
		writeConfigurationToFile(null,null,excludedElements,new HashMap<String,String[]>());
	}
	

	private static void renameConfiguration(String old_conf, String new_conf,String new_info) {
		if (new_conf==null) return;
		if (new_info==null) new_info="";
		HashMap<String,String[]> renameElements=new HashMap<String,String[]>();
		renameElements.put(old_conf, new String[]{new_conf,new_info});
		writeConfigurationToFile(null, null,new HashSet<String>(),renameElements);
	}

}
