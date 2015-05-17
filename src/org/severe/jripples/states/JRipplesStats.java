package org.severe.jripples.states;

import org.eclipse.jdt.core.IMember;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.severe.jripples.eig.JRipplesEIG;
import org.severe.jripples.eig.JRipplesEIGEdge;
import org.severe.jripples.eig.JRipplesEIGNode;

/**
 * This class represents a page showing statistics on JRipples EIG, including number of nodes, edges etc. 
 */
public class JRipplesStats implements IWorkbenchWindowActionDelegate {

	
	@Override
	public void dispose() {
		

	}

	@Override
	public void init(IWorkbenchWindow window) {
			
	}

	@Override
	public void run(IAction action) {
		
		final Shell shell = new Shell(PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getShell(), SWT.DIALOG_TRIM
				| SWT.APPLICATION_MODAL);
		GridLayout gd=new GridLayout(2, false);
		gd.marginWidth=50;
		shell.setText("JRipples Stats");
		shell.setLayout(gd);
		shell.setBounds(100, 100, 400, 400);
		
		if (!JRipplesEIG.isInitialized()) {
			Label label = new Label(shell, SWT.NONE);
			label.setText("JRipples EIG is not initialized.");
			shell.pack();
			shell.open();
			return;
		}
		
		Label label = new Label(shell, SWT.NONE);
		label.setText("Project:");
		label = new Label(shell, SWT.NONE);
		label.setText(JRipplesEIG.getProject().getName());

		label = new Label(shell, SWT.NONE);
		label.setText("Main class:");
		label = new Label(shell, SWT.NONE);
		label.setText(JRipplesEIG.getMainClass().getFullyQualifiedName());
		
		label = new Label(shell, SWT.NONE);
		label.setText("All nodes:");
		label = new Label(shell, SWT.NONE);
		label.setText(""+JRipplesEIG.getAllNodes().length);
		
		label = new Label(shell, SWT.NONE);
		label.setText("Top nodes (classes):");
		label = new Label(shell, SWT.NONE);
		label.setText(""+JRipplesEIG.getTopNodes().length);
		
		
		label = new Label(shell, SWT.NONE);
		label.setText("Member nodes:");
		label = new Label(shell, SWT.NONE);
		label.setText(""+(JRipplesEIG.getAllNodes().length-JRipplesEIG.getTopNodes().length));
		
		JRipplesEIGNode nodes[]=JRipplesEIG.getAllNodes();
		int methods=0;
		int classes=0;
		int variables=0;
		
		for (int i=0;i<nodes.length;i++) {
			if (nodes[i].isTop()) continue;
			switch (nodes[i].getNodeIMember().getElementType()) {
				case IMember.FIELD:variables++;break;
				case IMember.METHOD:methods++;break;
				case IMember.TYPE:classes++;break;
			}
		}
		
		label = new Label(shell, SWT.NONE);
		label.setText("     Subclasses:");
		label = new Label(shell, SWT.NONE);
		label.setText(""+classes);
		
		label = new Label(shell, SWT.NONE);
		label.setText("     Methods:");
		label = new Label(shell, SWT.NONE);
		label.setText(""+methods);
		
		label = new Label(shell, SWT.NONE);
		label.setText("     Variables:");
		label = new Label(shell, SWT.NONE);
		label.setText(""+variables);
		
		label = new Label(shell, SWT.NONE);
		label.setText("Edges:");
		label = new Label(shell, SWT.NONE);
		label.setText(""+JRipplesEIG.getAllEdges().length);
		
		JRipplesEIGEdge edges[]=JRipplesEIG.getAllEdges();
		int edge=0;
		
		
		for (int i=0;i<edges.length;i++) {
			if (edges[i].getMark()!=null)
				if (edges[i].getMark().compareTo("Custom")==0)
					edge++;
		}
		
		label = new Label(shell, SWT.NONE);
		label.setText("     Custom edges:");
		label = new Label(shell, SWT.NONE);
		label.setText(""+edge);
		
		shell.pack();
		
		shell.open();
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		

	}

}
