package org.severe.jripples.states;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;


import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.PlatformUI;
import org.severe.jripples.constants.JRipplesConstants;
import org.severe.jripples.eig.JRipplesEIG;
import org.severe.jripples.eig.JRipplesEIGEdge;
import org.severe.jripples.eig.JRipplesEIGNode;
import org.severe.jripples.logging.JRipplesLog;
import org.severe.jripples.modules.interfaces.JRipplesModuleInterface;
import org.severe.jripples.modules.interfaces.JRipplesPresentationModuleInterface;
import org.severe.jripples.modules.manager.ModuleProxy;

/**
 * This class is used to save and load JRipples states using input/output streams.
 */
public class JRipplesStatesSaveAndLoad {

	/**
	 * Loads JRipples EIG state from a provided input stream. Also, activates saved JRipples modules.
	 * @param stream
	 * input stream
	 */
	public static void loadJRipplesStateFromStream (final InputStream stream) {

		JRipplesEIG.initEIG();
		ProgressMonitorDialog progress = new ProgressMonitorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		try {
			progress.run(true, false, 		new IRunnableWithProgress() {

				public void run(IProgressMonitor monitor)
				throws InvocationTargetException, InterruptedException {
					BufferedReader in = new BufferedReader(new InputStreamReader(stream));
					String line;

					int nodesCount=0;
					int edgesCount=0;
					IMember member;
					JRipplesEIGNode node;
					String index;
					String index1;
					String mark;
					String probability;
					Integer count=null;
					JRipplesEIGNode nodes[]=null;

					boolean pflag=false;
					boolean tflag=false;
					
					JRipplesEIG.doLock(stream);
					
					try {
						while ((line = in.readLine()) != null) {
							if ((nodesCount>0) && (edgesCount>0) && (!tflag))
								{monitor.beginTask("Reading configuration from file.",nodesCount+edgesCount);tflag=true;};
							
							if (line.indexOf("nodes")==0) {
								nodesCount=Integer.parseInt(line.substring(line.indexOf("\t")+1));
								nodes=new JRipplesEIGNode[nodesCount];
							} else if (line.indexOf("edges")==0) {
								edgesCount=Integer.parseInt(line.substring(line.indexOf("\t")+1));
							} else if (line.indexOf("mainnode")==0) {
								try {
									member = (IMember) JavaCore.create(line.substring(line.indexOf("\t")+1));
									JRipplesEIG.setMainClass((IType)member);
									if (!pflag) {JRipplesEIG.setProject(member.getJavaProject().getProject());pflag=true;};
								} catch (Exception e) {
									JRipplesLog.logError("Core error.", e);
								}
							} else if (line.indexOf("node\t")==0) {
								try {
									line=line.substring(line.indexOf("\t")+1);
									index=line.substring(0, line.indexOf("\t"));
									line=line.substring(line.indexOf("\t")+1);
									mark=line.substring(0, line.indexOf("\t"));
									line=line.substring(line.indexOf("\t")+1);
									probability=line.substring(0, line.indexOf("\t"));
									member = (IMember) JavaCore.create(line.substring(line.indexOf("\t")+1));
									node=JRipplesEIG.addNode(member);
									if (mark!=null)
										if ((mark.compareTo("")!=0) && (mark.compareTo("null")!=0)) 
											node.setMark(mark);
									if (probability!=null)
										if ((probability.compareTo("")!=0) && (probability.compareTo("null")!=0))
											node.setProbability(probability);
									if (!pflag) {JRipplesEIG.setProject(member.getJavaProject().getProject());pflag=true;};
									nodes[Integer.valueOf(index).intValue()]=node;
									monitor.worked(1);
								} catch (Exception e) {
									JRipplesLog.logError("Core error.", e);
								}
							} else if (line.indexOf("edge\t")==0) {
								line=line.substring(line.indexOf("\t")+1);
								index=line.substring(0, line.indexOf("\t"));
								line=line.substring(line.indexOf("\t")+1);
								index=line.substring(0, line.indexOf("\t"));
								line=line.substring(line.indexOf("\t")+1);
								index1=line.substring(0, line.indexOf("\t"));
								line=line.substring(line.indexOf("\t")+1);
								mark=line.substring(0, line.indexOf("\t"));
								line=line.substring(line.indexOf("\t")+1);
								if (line.indexOf("\t")>-1) {
									count=Integer.parseInt(line.substring(0, line.indexOf("\t")));
									line=line.substring(line.indexOf("\t")+1);
								}
								probability=line;
								
								JRipplesEIGEdge edge=JRipplesEIG.addEdge(nodes[Integer.valueOf(index).intValue()], nodes[Integer.valueOf(index1).intValue()]);
								if (mark!=null)
									if ((mark.compareTo("")!=0) && (mark.compareTo("null")!=0)) 
										edge.setMark(mark);
								if (probability!=null)
									if ((probability.compareTo("")!=0) && (probability.compareTo("null")!=0))
										edge.setProbability(probability);
								if (count!=null)
										edge.setCount(count);
										
								monitor.worked(1);
							} else if (line.indexOf("module")==0) {
								line=line.substring(line.indexOf("\t")+1);
								line=line.substring(line.indexOf("\t")+1);
								
								//System.out.println("Activate "+line);
								try {
									ModuleProxy.setActiveModule(line, JRipplesModuleInterface.CONTROLLER_TYPE_IMPORTEXPORT);
								} catch (Exception e) {
									JRipplesLog.logError(e);
								}
								
							}

						}
					} catch (IOException e) {
						JRipplesLog.logError(e);
					}
					JRipplesEIG.doUnLock(stream);
					
					((JRipplesPresentationModuleInterface)ModuleProxy.getActiveCategoryModule(JRipplesConstants.CATEGORY_MODULE_PRESENTATION)).DisplayEIG();
					
				} 
			});	
		}
		catch (Exception e) {
			JRipplesLog.logError("Unable to load cofiguration file.", e);
		}



	}

	/**
	 * Writes JRipples EIG state into a provided output stream. Also, saves a list of active JRipples modules.
	 * @param jarWriter
	 * output stream
	 * @param conf_name
	 * name of the configuration to be saved
	 * @param conf_info
	 * comments on the configuration to be saved
	 */
	public static void saveJRipplesStateToStream (OutputStreamWriter jarWriter,String conf_name,	String conf_info) {
		if (!JRipplesEIG.isInitialized()) return;
		PrintWriter out;

		try {
			out = new PrintWriter(jarWriter);
		} catch (Exception e) {
			JRipplesLog.logError("ERROR: Can't open data file for output.", e);
			return;
		}

		JRipplesEIGNode[] nodes = JRipplesEIG.getAllNodes();
		JRipplesEIGEdge[] edges = JRipplesEIG.getAllEdges();

		out.println("configuration"+"\t"+conf_name+"\t"+conf_info);
		out.println("nodes"+"\t"+nodes.length);
		out.println("edges"+"\t"+edges.length);
		out.println("project"+"\t"+JRipplesEIG.getProject().getName());
		out.println("mainnode"+"\t"+JRipplesEIG.getMainClass().getHandleIdentifier());

		HashMap<JRipplesEIGNode, String> nodesNoden = new HashMap<JRipplesEIGNode, String>();

		for (int i = 0; i < nodes.length; i++) {
			out.println("node"+"\t" + i + "\t"+nodes[i].getMark()+"\t"+nodes[i].getProbability()+"\t"+ nodes[i].getNodeIMember().getHandleIdentifier());
			nodesNoden.put(nodes[i], "" + i);
		}

		for (int i = 0; i < edges.length; i++) {
			out.println("edge" +"\t"+ i + "\t"
					+ nodesNoden.get(edges[i].getFromNode()) + "\t"
					+ nodesNoden.get(edges[i].getToNode())
					+ "\t"+edges[i].getMark()+"\t"+edges[i].getCount()+
					"\t"+edges[i].getProbability());
		}


		Iterator<String> iter = ModuleProxy.getModulesCategories().iterator();
		while (iter.hasNext()) {
			String category = iter.next();
			out.println("module"+"\t"+category+"\t"+ModuleProxy.getActiveCategoryModuleName(category));

		}

		out.close();


	}

}
