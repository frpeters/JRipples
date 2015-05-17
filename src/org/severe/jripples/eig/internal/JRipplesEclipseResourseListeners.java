package org.severe.jripples.eig.internal;


import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.severe.jripples.constants.JRipplesConstants;
import org.severe.jripples.eig.JRipplesEIG;
import org.severe.jripples.eig.JRipplesEIGNode;
import org.severe.jripples.logging.JRipplesLog;
import org.severe.jripples.modules.interfaces.JRipplesDependencyGraphModuleInterface;
import org.severe.jripples.modules.manager.ModuleProxy;
import java.util.Set;
import java.util.HashSet;

/**
 * Utility class that is used to track Eclipse resources and 
 * add / remove / update JRipplesEIG nodes and edges according to changes
 * in those resources.
 * @see JRipplesEIG
 * @author Maksym Petrenko
 *
 */
public class JRipplesEclipseResourseListeners {

	protected JRipplesEclipseResourseListeners() {

	}

	/**
	 * Registers itself with Eclipse API to be able to listen for changes in Eclipse resources
	 */
	public static void listen() {
		ResourcesPlugin.getWorkspace().addResourceChangeListener(
				new ResourceJRipplesListener());
		
	}

	protected static class ResourceJRipplesListener implements
			IResourceChangeListener {

		private final IPath DOC_PATH = JRipplesEIG.getProject().getFullPath();

		public void resourceChanged(IResourceChangeEvent event) {
			// we are only interested in POST_CHANGE events
			
			if (event.getType() != IResourceChangeEvent.POST_CHANGE)
				return;

			IResourceDelta rootDelta = event.getDelta();

			// get the delta, if any, for the documentation directory
			IResourceDelta docDelta = (IResourceDelta) rootDelta
					.findMember(DOC_PATH);

			if (docDelta == null)
				return;
			
			IResourceDeltaVisitor visitor = new IResourceDeltaVisitor() {
				public boolean visit(IResourceDelta delta) {
					
					// only interested in added or removed resources
					if (delta.getKind() != IResourceDelta.ADDED
							&& delta.getKind() != IResourceDelta.REMOVED
							&& delta.getKind() != IResourceDelta.CHANGED)
						return true;

					if ((delta.getFlags() & IResourceDelta.CONTENT) == 0
							&& delta.getKind() == IResourceDelta.CHANGED)
						return true;
					
					
					// only interested in files with the "Java" extension
					IResource resource = delta.getResource();
					if (resource.getType() == IResource.FILE
							&& "java".equalsIgnoreCase(resource
									.getFileExtension())) {
						ICompilationUnit cu = JavaCore
								.createCompilationUnitFrom((IFile) resource);
						IType[] types = null;
						try {
							types = cu.getTypes();
						} catch (Exception e) {
							//Error happens here 
							return true;
						}
						if (types == null)
							return true;
						
						Set set=new HashSet();
						
						for (int i = 0; i < types.length; i++) {
							
							if (delta.getKind() == IResourceDelta.ADDED) {
								JRipplesEIGNode node=JRipplesEIG.addNode(types[i]);
								set.add(node);
								
							} else if (delta.getKind() == IResourceDelta.REMOVED) {
								JRipplesEIG.removeNode(JRipplesEIG.getNode(types[i]));
							} else if (delta.getKind() == IResourceDelta.CHANGED) {
								set.add(JRipplesEIG.getNode(types[i]));
							}
						}
					
						if ((delta.getKind() == IResourceDelta.CHANGED) || (delta.getKind() == IResourceDelta.ADDED)) {
							((JRipplesDependencyGraphModuleInterface) ModuleProxy
									.getActiveCategoryModule(JRipplesConstants.CATEGORY_MODULE_DEPENEDENCY_GRAPH)).ReAnalyzeProjectAtNodes(set);

						}

					}

					return true;
				}
			};

			try {
				docDelta.accept(visitor);

			} catch (CoreException e) {
				JRipplesLog.logError(e);
			}

		}
	};

}
