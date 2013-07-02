package org.ifml.eclipse.graphiti.features;

import org.eclipse.graphiti.datatypes.IDimension;
import org.eclipse.graphiti.features.ILayoutFeature;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.algorithms.Polyline;
import org.eclipse.graphiti.mm.algorithms.styles.Point;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;

/**
 * Provides utility methods for {@link ILayoutFeature}s.
 */
public final class LayoutFeatures {

    private LayoutFeatures() {
    }

    /**
     * Resizes a shape providing a minimum width and height.
     * 
     * @param shape
     *            the shape to resize.
     * @param minWidth
     *            the minimum width.
     * @param minHeight
     *            the minimum height.
     * @return {@code true} if either the width or height changed.
     */
    public static final boolean resize(ContainerShape shape, int minWidth, int minHeight) {
        boolean changed = false;
        GraphicsAlgorithm ga = shape.getGraphicsAlgorithm();
        if (ga.getHeight() < minHeight) {
            ga.setHeight(minHeight);
            changed = true;
        }
        if (ga.getWidth() < minWidth) {
            ga.setWidth(minWidth);
            changed = true;
        }
        return changed;

    }

    /**
     * Resizes the width of child shapes of a container shape.
     * 
     * @param containerShape
     *            the container shape.
     * @return {@code true} if something changed.
     */
    public static final boolean resizeChildrenHorizontally(ContainerShape containerShape) {
        boolean changed = false;
        IGaService gaService = Graphiti.getGaService();
        GraphicsAlgorithm containerGa = containerShape.getGraphicsAlgorithm();
        int containerWidth = containerGa.getWidth();
        for (Shape shape : containerShape.getChildren()) {
            if (shape.getLink() == null) {
                GraphicsAlgorithm ga = shape.getGraphicsAlgorithm();
                IDimension size = gaService.calculateSize(ga);
                if (containerWidth != size.getWidth()) {
                    if (ga instanceof Polyline) {
                        Polyline polyline = (Polyline) ga;
                        Point secondPoint = polyline.getPoints().get(1);
                        Point newSecondPoint = gaService.createPoint(containerWidth, secondPoint.getY());
                        polyline.getPoints().set(1, newSecondPoint);
                        changed = true;
                    } else {
                        gaService.setWidth(ga, containerWidth);
                        changed = true;
                    }
                }
            }
        }
        return changed;
    }

}
