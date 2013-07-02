package org.ifml.eclipse.graphiti.properties;

import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.graphiti.dt.IDiagramTypeProvider;
import org.eclipse.graphiti.features.context.impl.LayoutContext;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.platform.IDiagramEditor;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.ui.editor.DiagramEditor;
import org.eclipse.graphiti.ui.platform.AbstractPropertySectionFilter;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.part.IContributedContentsView;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.ifml.base.Objects2;
import org.ifml.eclipse.emf.ui.editparts.EditPartEmfSelections;
import org.ifml.eclipse.emf.ui.properties.EmfPropertySection;
import org.ifml.eclipse.emf.ui.properties.EmfPropertyConfigurationSet;

import com.google.common.base.Optional;

/**
 * A property section for Graphiti-based EMF models.
 * 
 * @param <T>
 *            the model type.
 */
public final class GfEmfPropertySection<T extends EObject> extends EmfPropertySection<T> implements ITabbedPropertyConstants {

    private final IFilter peFilter = new PictogramElementPropertySectionFilter();

    /**
     * Constructs a new property section.
     * 
     * @param instanceClass
     *            the instance class.
     * @param eClass
     *            the EMF class.
     * @param configSet
     *            the property configuration set.
     */
    public GfEmfPropertySection(Class<T> instanceClass, EClass eClass, EmfPropertyConfigurationSet configSet) {
        super(instanceClass, eClass, configSet);
    }

    @Override
    public boolean select(Object toTest) {
        if (peFilter.select(toTest)) {
            return true;
        } else {
            return super.select(toTest);
        }
    }

    private IDiagramEditor getDiagramEditor() {
        IWorkbenchPart part = getPart();
        if (part instanceof FormEditor) {
            IDiagramEditor diagramEditor = getDiagramEditor((FormEditor) part);
            if (diagramEditor != null) {
                return diagramEditor;
            }
        }
        IContributedContentsView contributedView = (IContributedContentsView) part.getAdapter(IContributedContentsView.class);
        if (contributedView != null) {
            part = contributedView.getContributingPart();
            if (part instanceof FormEditor) {
                IDiagramEditor diagramEditor = getDiagramEditor((FormEditor) part);
                if (diagramEditor != null) {
                    return diagramEditor;
                }
            }
        }
        return null;
    }

    private IDiagramEditor getDiagramEditor(FormEditor formEditor) {
        IEditorPart activePart = formEditor.getActiveEditor();
        if (activePart instanceof DiagramEditor) {
            return (DiagramEditor) activePart;
        }
        return null;
    }

    private IDiagramTypeProvider getDiagramTypeProvider() {
        IDiagramEditor diagramEditor = getDiagramEditor();
        if (diagramEditor == null) {
            return null;
        }
        return diagramEditor.getDiagramTypeProvider();
    }

    /**
     * Returns the single selected model element in the active part.
     * 
     * @return the single selected model element or {@code null} if the selection is multiple or the model element type is not valid.
     */
    @Override
    public Optional<T> getSingleSelection() {
        PictogramElement pe = getSelectedPictogramElement();
        if (pe != null) {
            return Optional.fromNullable(Objects2.as(Graphiti.getLinkService().getBusinessObjectForLinkedPictogramElement(pe),
                    getInstanceClass()));
        } else { // content outline tree node
            return Optional.fromNullable(Objects2.as(EditPartEmfSelections.getSingleModelObject(getSelection(), getInstanceClass()),
                    getInstanceClass()));
        }
    }

    private PictogramElement getSelectedPictogramElement() {
        if (getSelection() instanceof StructuredSelection) {
            StructuredSelection structuredSelection = (StructuredSelection) getSelection();
            Object firstElement = structuredSelection.getFirstElement();
            if (firstElement instanceof PictogramElement) {
                return (PictogramElement) firstElement;
            }
            EditPart editPart = null;
            if (firstElement instanceof EditPart) {
                editPart = (EditPart) firstElement;
            } else if (firstElement instanceof IAdaptable) {
                editPart = (EditPart) ((IAdaptable) firstElement).getAdapter(EditPart.class);
            }
            if (editPart != null && editPart.getModel() instanceof PictogramElement) {
                return (PictogramElement) editPart.getModel();
            }
        }
        return null;
    }

    @Override
    protected void handleModelObservable(IObservableValue modelObservable) {
        modelObservable.addChangeListener(new IChangeListener() {

            @Override
            public void handleChange(ChangeEvent event) {
                LayoutContext context = new LayoutContext(getSelectedPictogramElement());
                getDiagramTypeProvider().getFeatureProvider().layoutIfPossible(context);
            }
        });
    }

    @Override
    protected TransactionalEditingDomain getEditingDomain() {
        return getDiagramEditor().getEditingDomain();
    }

    private class PictogramElementPropertySectionFilter extends AbstractPropertySectionFilter {

        @Override
        protected boolean accept(PictogramElement pictogramElement) {
            EObject eObj = Objects2.as(Graphiti.getLinkService().getBusinessObjectForLinkedPictogramElement(pictogramElement),
                    EObject.class);
            return (eObj != null) && (eObj.eClass() == getEClass());
        }
    }

}