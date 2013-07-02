package org.ifml.eclipse.graphiti.diagrams;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.ResourceSetChangeEvent;
import org.eclipse.emf.transaction.ResourceSetListener;
import org.eclipse.emf.transaction.ResourceSetListenerImpl;
import org.eclipse.emf.transaction.RollbackException;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.editparts.AbstractTreeEditPart;
import org.eclipse.gef.editparts.RootTreeEditPart;
import org.eclipse.gef.ui.parts.ContentOutlinePage;
import org.eclipse.gef.ui.parts.SelectionSynchronizer;
import org.eclipse.gef.ui.parts.TreeViewer;
import org.eclipse.graphiti.dt.IDiagramTypeProvider;
import org.eclipse.graphiti.ui.editor.DiagramEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.part.PageBook;
import org.ifml.base.Objects2;
import org.ifml.eclipse.core.runtime.Adaptables;
import org.ifml.eclipse.emf.ui.editparts.EditPartEmfSelections;
import org.ifml.eclipse.ui.viewers.Selections;

import com.google.common.collect.Sets;

/**
 * An abstract content outline page for a Graphiti-based editor with an EMF-based model.
 */
public abstract class AbstractGfEmfContentOutlinePage extends ContentOutlinePage {

    private final IDiagramTypeProvider dtp;

    private PageBook pageBook;

    private Control outline;

    private ResourceSetListener modelChangeListener;

    /**
     * Constructs a new content outline page.
     * 
     * @param dtp
     *            the diagram type provider.
     */
    public AbstractGfEmfContentOutlinePage(IDiagramTypeProvider dtp) {
        super(createTreeViewer());
        this.dtp = dtp;
    }

    private static final TreeViewer createTreeViewer() {
        TreeViewer viewer = new TreeViewer();
        return viewer;
    }

    private DiagramEditor getDiagramEditor() {
        return (DiagramEditor) dtp.getDiagramEditor();
    }

    @Override
    public final void createControl(Composite parent) {
        pageBook = new PageBook(parent, SWT.NONE);
        getViewer().setRootEditPart(new DiagramRootTreeEditPart());
        outline = getViewer().createControl(pageBook);
        getViewer().setContextMenu(createContextMenuProvider());
        pageBook.showPage(outline);
        configureOutlineViewer();
        hookOutlineViewer();
        initializeOutlineViewer();
    }

    /**
     * Creates the context menu provider.
     * 
     * @return the context menu provider.
     */
    protected abstract ContextMenuProvider createContextMenuProvider();

    private void configureOutlineViewer() {
        getViewer().setEditDomain(getDiagramEditor().getEditDomain());
        getViewer().setEditPartFactory(new OutlineEditPartFactory());
    }

    private void hookOutlineViewer() {
        // TODO: fix synchronizer: models of editor's edit parts are shapes, not EObjects
        Adaptables.getAdapter(getDiagramEditor(), SelectionSynchronizer.class).addViewer(getViewer());
        modelChangeListener = new ModelChangeListener();
        getDiagramEditor().getEditingDomain().addResourceSetListener(modelChangeListener);
    }

    private void initializeOutlineViewer() {
        try {
            EObject rootElem = getRootlElement(dtp);
            getViewer().setContents(rootElem);
        } catch (CoreException e) {
            getViewer().setContents(e.getStatus());
        }
    }

    /**
     * Returns the root model element for the content outline tree.
     * 
     * @param dtp
     *            the diagram type provider.
     * @return the root model element.
     * @throws CoreException
     *             if an exception occurred retrieving the root model element.
     */
    protected abstract EObject getRootlElement(IDiagramTypeProvider dtp) throws CoreException;

    @Override
    public final Control getControl() {
        return pageBook;
    }

    @Override
    public void dispose() {
        unhookOutlineViewer();
        super.dispose();
    }

    private final void unhookOutlineViewer() {
        getDiagramEditor().getEditingDomain().removeResourceSetListener(modelChangeListener);
        Adaptables.getAdapter(getDiagramEditor(), SelectionSynchronizer.class).removeViewer(getViewer());
    }

    /**
     * @see ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
     */
    @Override
    public void setSelection(ISelection selection) {
        super.setSelection(EditPartEmfSelections.toEditParts(Selections.toStructuredSelection(selection).toList(), getViewer()));
    }

    /**
     * Returns the label provider able to decorate nodes with text and image.
     * 
     * @return the label provider.
     */
    protected abstract ILabelProvider getLabelProvider();

    private static final class DiagramRootTreeEditPart extends RootTreeEditPart {

        private TreeItem diagramTreeItem;

        @Override
        protected void addChildVisual(EditPart childEditPart, int index) {
            diagramTreeItem = new TreeItem((Tree) getWidget(), 0, index);
            ((TreeEditPart) childEditPart).setWidget(diagramTreeItem);
        }

        @Override
        protected void removeChildVisual(EditPart childEditPart) {
            diagramTreeItem.dispose();
            super.removeChildVisual(childEditPart);
        }
    }

    private final class OutlineEditPartFactory implements EditPartFactory {

        @Override
        public EditPart createEditPart(EditPart context, Object model) {
            return new TreeEditPart(model);
        }
    }

    private final class TreeEditPart extends AbstractTreeEditPart {

        TreeEditPart(Object model) {
            super(model);
        }

        @Override
        protected String getText() {
            return getLabelProvider().getText(getModel());
        }

        @Override
        protected Image getImage() {
            return getLabelProvider().getImage(getModel());
        }

        @Override
        public List<?> getModelChildren() {
            EObject model = Objects2.as(getModel(), EObject.class);
            if (model != null) {
                return model.eContents();
            }
            return super.getChildren();
        }

    }

    private final class ModelChangeListener extends ResourceSetListenerImpl {

        @Override
        public boolean isPostcommitOnly() {
            return true;
        }

        @Override
        public void resourceSetChanged(ResourceSetChangeEvent event) {
            final Set<EObject> changedModels = Sets.newHashSet();
            for (Notification notification : event.getNotifications()) {
                EObject changedModel = Objects2.as(notification.getNotifier(), EObject.class);
                if (changedModel != null) {
                    changedModels.add(changedModel);
                }
            }
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    Map<?, ?> editPartRegistry = getViewer().getEditPartRegistry();
                    for (EObject changedModel : changedModels) {
                        EditPart editPart = Objects2.as(editPartRegistry.get(changedModel), EditPart.class);
                        if (editPart != null) {
                            editPart.refresh();
                        }
                    }
                }
            });
        }

        @Override
        public Command transactionAboutToCommit(ResourceSetChangeEvent arg0) throws RollbackException {
            return null;
        }

    }

}