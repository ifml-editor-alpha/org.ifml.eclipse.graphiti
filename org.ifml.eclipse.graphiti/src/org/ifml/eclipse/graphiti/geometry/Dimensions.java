package org.ifml.eclipse.graphiti.geometry;

import org.eclipse.draw2d.geometry.Dimension;

/** Provides utility methods for dimensions. */
public final class Dimensions {

    private Dimensions() {
    }

    /**
     * Returns the maximum width among a list of dimensions.
     * 
     * @param dims
     *            the list of dimensions.
     * @return the maximum width.
     */
    public static int maxWidth(Iterable<Dimension> dims) {
        int max = 0;
        for (Dimension dim : dims) {
            max = Math.max(max, dim.width);
        }
        return max;
    }

    /**
     * Returns the sum of all height of a list of dimensions.
     * 
     * @param dims
     *            the list of dimensions.
     * @return the sum of heights.
     */
    public static int sumHeight(Iterable<Dimension> dims) {
        int sum = 0;
        for (Dimension dim : dims) {
            sum += dim.height;
        }
        return sum;
    }

}
