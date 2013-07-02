package org.ifml.eclipse.graphiti.actions;

import java.util.List;

import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.ifml.eclipse.emf.ui.editparts.EditPartEmfSelections;
import org.ifml.eclipse.ui.Workbenches;
import org.ifml.eclipse.ui.actions.ContextAction;
import org.ifml.eclipse.ui.widgets.Displays;

/**
 * An action able to add a newly created child {@link EObject} to a parent {@link EObject} using a specific containment feature.
 * 
 * @param <T>
 *            the
 */
public final class AddChildAction<T extends EObject> extends ContextAction<T> {

    private final EClass parentClass;

    private final EClass childClass;

    private final EStructuralFeature containmentFeature;

    private final TransactionalEditingDomain domain;

    /**
     * Constructs a new action.
     * 
     * @param parentClass
     *            the parent class.
     * @param childClass
     *            the child class.
     * @param containmentFeature
     *            the containment feature.
     * @param domain
     *            the editing domain.
     */
    public AddChildAction(EClass parentClass, EClass childClass, EStructuralFeature containmentFeature,
            TransactionalEditingDomain domain) {
        this.parentClass = parentClass;
        this.childClass = childClass;
        this.containmentFeature = containmentFeature;
        this.domain = domain;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean isEnabled() {
        if (getContext() == null) {
            return false;
        }
        if (((Class<EObject>) parentClass.getInstanceClass()).isInstance(getContext())) {
            if (containmentFeature.getUpperBound() == EStructuralFeature.UNBOUNDED_MULTIPLICITY) {
                return true;
            }
            ISelection sel = Workbenches.getActivePart().getSite().getSelectionProvider().getSelection();
            List<EObject> modelObjects = EditPartEmfSelections.getModelObjects(sel, EObject.class, true);
            if (modelObjects.size() == 1) {
                EObject selObj = modelObjects.get(0);
                return (selObj.eGet(containmentFeature) == null);
            }
        }
        return false;
    }

    @Override
    public void run() {
        final EObject newObject = EcoreUtil.create(childClass);
        Command cmd = null;
        if (containmentFeature.isMany()) {
            cmd = AddCommand.create(domain, getContext(), containmentFeature, newObject);
        } else {
            cmd = SetCommand.create(domain, getContext(), containmentFeature, newObject);
        }
        domain.getCommandStack().execute(cmd);
        Displays.getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                Workbenches.getActivePart().getSite().getSelectionProvider().setSelection(new StructuredSelection(newObject));
            }
        });
    }

}
