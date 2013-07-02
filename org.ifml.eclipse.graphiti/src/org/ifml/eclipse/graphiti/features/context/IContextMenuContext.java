package org.ifml.eclipse.graphiti.features.context;

import org.eclipse.graphiti.features.context.IContext;

/**
 * The context of a context menu request.
 */
public interface IContextMenuContext extends IContext {

    /**
     * Returns the model object the context menu is applied to.
     * 
     * @return the model object.
     */
    Object getModelObject();

}
