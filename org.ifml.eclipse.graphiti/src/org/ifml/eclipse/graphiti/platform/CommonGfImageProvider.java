package org.ifml.eclipse.graphiti.platform;

import org.eclipse.graphiti.platform.AbstractExtension;
import org.eclipse.graphiti.ui.platform.IImageProvider;
import org.ifml.eclipse.ui.CommonUi;
import org.ifml.eclipse.ui.graphics.CommonImage;
import org.ifml.eclipse.ui.graphics.ImageProviders;

import com.google.common.base.Objects;

/**
 * A Graphiti image provider for the images provided by {@link CommonImage}.
 */
public final class CommonGfImageProvider extends AbstractExtension implements IImageProvider {

    private static final String PREFIX = CommonImage.class.getCanonicalName() + "/";

    @Override
    public void setPluginId(String pluginId) {
        // ignored
    }

    @Override
    public String getPluginId() {
        return CommonUi.ID;
    }

    @Override
    public String getImageFilePath(String imageId) {
        if (imageId.startsWith(PREFIX)) {
            try {
                CommonImage img = CommonImage.valueOf(imageId.substring(PREFIX.length()));
                return Objects.firstNonNull(img.getSharedSymbolicName(), ImageProviders.getPath(img));
            } catch (IllegalArgumentException e) {
            }
        }
        return null;
    }

}
