package org.ifml.eclipse.graphiti.diagrams;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.command.CommandStack;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.ui.services.GraphitiUi;
import org.eclipse.graphiti.ui.services.IEmfService;
import org.ifml.eclipse.core.runtime.Statuses;
import org.ifml.eclipse.ui.CommonUi;

/**
 * Provides utility methods for Graphiti diagrams.
 */
public final class Diagrams {

    private Diagrams() {
    }

    /**
     * Serializes a set of diagrams into an EMF file.
     * 
     * @param fileResourceUri
     *            the file resource URI.
     * @param diagrams
     *            the diagrams.
     * @param monitor
     *            the progress monitor.
     * @throws CoreException
     *             if an exception occurred saving the EMF file.
     */
    public static void createEmfFileForDiagram(URI fileResourceUri, final List<Diagram> diagrams, IProgressMonitor monitor)
            throws CoreException {
        IEmfService emfService = GraphitiUi.getEmfService();
        TransactionalEditingDomain editingDomain = emfService.createResourceSetAndEditingDomain();
        ResourceSet resourceSet = editingDomain.getResourceSet();
        final Resource resource = resourceSet.createResource(fileResourceUri);
        final CommandStack commandStack = editingDomain.getCommandStack();
        commandStack.execute(new RecordingCommand(editingDomain) {

            @Override
            protected void doExecute() {
                resource.setTrackingModification(true);
                for (Diagram diagram : diagrams) {
                    resource.getContents().add(diagram);
                }
            }
        });
        save(editingDomain, monitor);
        editingDomain.dispose();
    }

    private static void save(TransactionalEditingDomain editingDomain, IProgressMonitor monitor) throws CoreException {
        IWorkspaceRunnable runnable = new SaveDiagramWorkspaceRunnable(editingDomain);
        ResourcesPlugin.getWorkspace().run(runnable, monitor);
    }

    private static final class SaveDiagramWorkspaceRunnable implements IWorkspaceRunnable {

        private final TransactionalEditingDomain editingDomain;

        public SaveDiagramWorkspaceRunnable(TransactionalEditingDomain editingDomain) {
            this.editingDomain = editingDomain;
        }

        @Override
        public void run(IProgressMonitor monitor) throws CoreException {
            try {
                editingDomain.runExclusive(new SaveDiagramRunnable(editingDomain));
                editingDomain.getCommandStack().flush();
            } catch (RuntimeException e) {
                throw new CoreException(Statuses.getErrorStatus(e, null, CommonUi.getDefault().getBundle()));
            } catch (InterruptedException e) {
                throw new CoreException(Statuses.getErrorStatus(e, null, CommonUi.getDefault().getBundle()));
            }
        }

    }

    private static final class SaveDiagramRunnable implements Runnable {

        private final TransactionalEditingDomain editingDomain;

        public SaveDiagramRunnable(TransactionalEditingDomain editingDomain) {
            this.editingDomain = editingDomain;
        }

        @Override
        public void run() {
            EList<Resource> resources = editingDomain.getResourceSet().getResources();
            Resource[] resourcesArray = new Resource[resources.size()];
            resourcesArray = resources.toArray(resourcesArray);
            final Set<Resource> savedResources = new HashSet<Resource>();
            for (int i = 0; i < resourcesArray.length; i++) {
                final Resource resource = resourcesArray[i];
                if (resource.isModified()) {
                    try {
                        resource.save(null);
                    } catch (IOException e) {
                        throw new WrappedException(e);
                    }
                    savedResources.add(resource);
                }
            }

        }
    }

}
