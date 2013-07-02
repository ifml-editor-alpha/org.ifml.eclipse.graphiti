package org.ifml.eclipse.graphiti.services;

import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.swt.graphics.Point;

import com.google.common.base.Preconditions;

/**
 * Provides utility methods for Graphiti graphics algorithms
 */
public final class GaServices {

    private GaServices() {
    }

    /**
     * Returns the maximum X coordinate value.
     * 
     * @param xy
     *            the array of coordinates.
     * @return the maximum X coordinate value.
     */
    public static final int getMaxX(int[] xy) {
        Preconditions.checkArgument(xy.length > 0, "Empty array of coordinates");
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < xy.length; i++) {
            if ((i % 2) == 0) {
                max = Math.max(max, xy[i]);
            }
        }
        return max;
    }

    /**
     * Returns the maximum Y coordinate value.
     * 
     * @param xy
     *            the array of coordinates.
     * @return the maximum Y coordinate value.
     */
    public static final int getMaxY(int[] xy) {
        Preconditions.checkArgument(xy.length > 1, "Invalid array of coordinates");
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < xy.length; i++) {
            if ((i % 2) == 1) {
                max = Math.max(max, xy[i]);
            }
        }
        return max;
    }

    /**
     * Transforms a pair of coordinates into an absolute point relative to the ancestor diagram.
     * 
     * @param x
     *            the horizontal coordinate relative to {@code containerShape}.
     * @param y
     *            the vertical coordinate relative to {@code containerShape}.
     * @param containerShape
     *            the container shape.
     * @return the absolute position relative to the ancestor diagram.
     */
    public static final Point toAbsolute(int x, int y, ContainerShape containerShape) {
        while (!(containerShape instanceof Diagram)) {
            x = containerShape.getGraphicsAlgorithm().getX() + x;
            y = containerShape.getGraphicsAlgorithm().getY() + y;
            containerShape = containerShape.getContainer();
        }
        return new Point(x, y);
    }
}
