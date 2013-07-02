package org.ifml.eclipse.graphiti.features.context.impl;

import org.eclipse.graphiti.PropertyBag;
import org.ifml.eclipse.graphiti.features.context.IContextMenuContext;

/**
 * A concrete implementation of the {@link IContextMenuContext} interface.
 */
public class ContextMenuContext extends PropertyBag implements IContextMenuContext {

    private final Object obj;

    /**
     * Constructs a new context.
     * 
     * @param obj
     *            the model object the context menu is applied to.
     */
    public ContextMenuContext(Object obj) {
        this.obj = obj;
    }

    @Override
    public Object getModelObject() {
        return obj;
    }

}
