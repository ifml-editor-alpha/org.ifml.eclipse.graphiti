package org.ifml.eclipse.graphiti.algorithms;

import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

/**
 * Provides utility methods for {@link GraphicsAlgorithm}.
 */
public final class GraphicsAlgorithms {

    private GraphicsAlgorithms() {
    }

    /**
     * Returns the bounds of the input {@link GraphicsAlgorithm} as a SWT rectangle.
     * 
     * @param ga
     *            the graphics algorithm.
     * @return the rectangle bounds.
     */
    public static Rectangle getBounds(GraphicsAlgorithm ga) {
        return new Rectangle(ga.getX(), ga.getY(), ga.getWidth(), ga.getHeight());
    }

    /**
     * Returns the center of the input {@link GraphicsAlgorithm} as a SWT point.
     * 
     * @param ga
     *            the graphics algorithm.
     * @return the center point.
     */
    public static Point getCenter(GraphicsAlgorithm ga) {
        return new Point(ga.getX() + (ga.getWidth() / 2), ga.getY() + (ga.getHeight() / 2));
    }

}
