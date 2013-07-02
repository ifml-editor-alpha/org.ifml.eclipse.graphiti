package org.ifml.eclipse.graphiti.geometry;

import java.util.List;

import org.eclipse.draw2d.geometry.Rectangle;

/** Provides utility methods for dimensions. */
public class Rectangles {

    private Rectangles() {
    }

    /**
     * Returns the union of a list of rectangles.
     * 
     * @param rects
     *            the list of rectangles.
     * @return the union.
     */
    public static Rectangle union(List<Rectangle> rects) {
        Rectangle result = new Rectangle();
        for (Rectangle rect : rects) {
            result = result.union(rect);
        }
        return result;
    }

}
