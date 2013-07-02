package org.ifml.eclipse.graphiti.platform;

import org.ifml.eclipse.ui.graphics.IImageProvider;

/**
 * Provides utility methods for Graphiti image providers.
 */
public final class GfImageProviders {

    private GfImageProviders() {
    }

    /**
     * Returns the image identifier for a specific image provider.
     * 
     * @param imageProvider
     *            the image provider.
     * @return the image identifier.
     */
    public static <T extends Enum<T> & IImageProvider> String getId(T imageProvider) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(imageProvider.getClass().getCanonicalName());
        buffer.append('/');
        buffer.append(imageProvider.name());
        return buffer.toString();
    }

}
