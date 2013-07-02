package org.ifml.eclipse.graphiti.features;

import javax.annotation.Nullable;

import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;

/**
 * Provides utility methods for {@link ICustomContext}.
 */
public final class CustomContexts {

    private CustomContexts() {
    }

    /**
     * Returns the single selected pictogram element.
     * <p>
     * Returns {@code null} if zero or more than one element is selected.
     * 
     * @param context
     *            the custom context.
     * @return the single selected pictogram element.
     */
    public static @Nullable
    PictogramElement getSinglePictogramElement(ICustomContext context) {
        PictogramElement[] pes = context.getPictogramElements();
        if (pes != null && pes.length == 1) {
            return pes[0];
        }
        return null;
    }

    /**
     * Returns the single business object associated with the currently selected pictogram element.
     * <p>
     * Returns {@code null} if zero or more than one element is selected.
     * 
     * @param context
     *            the custom context.
     * @param featureProvider
     *            the feature provider.
     * @return the single business object associated with the currently selected pictogram element.
     */
    public static @Nullable
    Object getSingleBusinessObject(ICustomContext context, IFeatureProvider featureProvider) {
        PictogramElement pe = getSinglePictogramElement(context);
        if (pe != null) {
            return featureProvider.getBusinessObjectForPictogramElement(pe);
        }
        return null;
    }

}
