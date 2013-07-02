package org.ifml.eclipse.graphiti.patterns;

import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.graphiti.datatypes.IDimension;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.context.ICreateContext;
import org.eclipse.graphiti.features.context.ILayoutContext;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.pattern.AbstractPattern;
import org.eclipse.graphiti.ui.services.GraphitiUi;
import org.ifml.base.Objects2;
import org.ifml.base.WordFormat;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * An abstract base class for Graphiti shape patterns based on EMF models.
 * 
 * @param <T>
 *            the model type.
 */
public abstract class AbstractGfEmfShapePattern<T extends EObject> extends AbstractPattern {

    private final Class<T> instanceClass;

    /**
     * Constructs a new pattern.
     * 
     * @param instanceClass
     *            the instance class.
     */
    public AbstractGfEmfShapePattern(Class<T> instanceClass) {
        super(null);
        this.instanceClass = instanceClass;
    }

    /**
     * Returns the model {@link EClass} associated with this pattern.
     * 
     * @return the model class.
     */
    public abstract EClass getEClass();

    @Override
    public final boolean isMainBusinessObjectApplicable(Object mainBusinessObject) {
        if (mainBusinessObject instanceof EObject) {
            return getEClass().equals(((EObject) mainBusinessObject).eClass());
        }
        return false;
    }

    @Override
    public final boolean canAdd(IAddContext context) {
        if (isMainBusinessObjectApplicable(context.getNewObject())) {
            PictogramElement targetElem = Objects.firstNonNull(context.getTargetContainer(), context.getTargetConnection());
            EObject parentObject = Objects2.as(getBusinessObjectForPictogramElement(targetElem), EObject.class);
            return canAddTo(parentObject);
        }
        return false;
    }

    @Override
    public final boolean canCreate(ICreateContext context) {
        PictogramElement targetElem = Objects.firstNonNull(context.getTargetContainer(), context.getTargetConnection());
        EObject parentObject = Objects2.as(getBusinessObjectForPictogramElement(targetElem), EObject.class);
        return canAddTo(parentObject);
    }

    /**
     * Returns whether a new model object, having type identified by the {@link #getEClass() method} can be added to a specific parent
     * object.
     * 
     * @param parentObject
     *            the parent object.
     * @return {@code true} if a new model object can be added to {@code parentObject}.
     */
    protected abstract boolean canAddTo(EObject parentObject);

    @Override
    public final Object[] create(ICreateContext context) {
        EObject newObject = EcoreUtil.create(getEClass());
        EObject parentObject = (EObject) getBusinessObjectForPictogramElement(Objects.firstNonNull(context.getTargetContainer(),
                context.getTargetConnection()));
        Optional<EReference> containmentReference = getContainmentReference(parentObject, newObject);
        Preconditions.checkState(containmentReference.isPresent(), "No containment reference specified for %s (parent = %s)",
                getEClass().getName(), parentObject.eClass().getName());
        if (containmentReference.get().isMany()) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) parentObject.eGet(containmentReference.get());
            list.add(newObject);
        } else {
            parentObject.eSet(containmentReference.get(), newObject);
        }
        postCreate(newObject);
        addGraphicalRepresentation(context, newObject);
        return new Object[] { newObject };
    }

    /**
     * Invoked at the end of the {@link #create(ICreateContext)} method, just before the graphical representation is added to the
     * object.
     * <p>
     * The default implementation does nothing. Sub-classes can override it.
     * 
     * @param newObject
     *            the new model object.
     */
    protected void postCreate(EObject newObject) {
    }

    @Override
    public final PictogramElement add(IAddContext context) {
        List<PictogramElement> pictogramElements = addPictogramElements(context);
        Object addedDomainObject = context.getNewObject();
        for (PictogramElement pictogramElement : pictogramElements) {
            link(pictogramElement, addedDomainObject);
            layoutPictogramElement(pictogramElement);
        }
        return pictogramElements.get(0);
    }

    /**
     * Adds pictogram elements to the diagram.
     * <p>
     * This method delegates to the {@link #addPictogramElement(IAddContext)} method, which returns an unique element. Sub-classes can
     * override.
     * 
     * @param context
     *            the add context holding information about the added domain object.
     * @return the newly created pictogram elements.
     */
    protected List<PictogramElement> addPictogramElements(IAddContext context) {
        return ImmutableList.of(addPictogramElement(context));
    }

    /**
     * Adds a pictogram element to the diagram.
     * 
     * @param context
     *            the add context holding information about the added domain object.
     * @return the newly created pictogram element.
     */
    protected abstract PictogramElement addPictogramElement(IAddContext context);

    /**
     * Returns the containment reference for a parent-child relationship.
     * 
     * @param parentObject
     *            the parent object.
     * @param childObject
     *            the child object.
     * @return the containment reference.
     */
    protected abstract Optional<EReference> getContainmentReference(EObject parentObject, EObject childObject);

    @Override
    public final String getCreateName() {
        return WordFormat.UPPER_CAMEL.to(WordFormat.CAPITALIZED_WORDS, getEClass().getName());
    }

    @Override
    protected final boolean isPatternControlled(PictogramElement pictogramElement) {
        Object domainObject = getBusinessObjectForPictogramElement(pictogramElement);
        return isMainBusinessObjectApplicable(domainObject);
    }

    @Override
    protected final boolean isPatternRoot(PictogramElement pictogramElement) {
        Object domainObject = getBusinessObjectForPictogramElement(pictogramElement);
        return isMainBusinessObjectApplicable(domainObject);
    }

    @Override
    public final boolean canLayout(ILayoutContext context) {
        return super.canLayout(context);
    }

    /**
     * Retrieves the list of dimensions of the shapes associated with a list of model objects.
     * 
     * @param eObjs
     *            the list of model objects.
     * @return the list of dimensions.
     */
    protected final List<Dimension> calculateSizes(List<? extends EObject> eObjs) {
        List<Dimension> dims = Lists.newArrayList();
        for (EObject eObj : eObjs) {
            ContainerShape shape = (ContainerShape) getMappingProvider().getPictogramElementForBusinessObject(eObj);
            GraphicsAlgorithm ga = shape.getGraphicsAlgorithm();
            dims.add(new Dimension(ga.getWidth(), ga.getHeight()));
        }
        return dims;
    }

    /**
     * Retrieves the list of rectangle bounds of the shapes associated with a list of model objects.
     * 
     * @param eObjs
     *            the list of model objects.
     * @return the list of rectangle bounds.
     */
    protected final List<Rectangle> calculateBounds(List<? extends EObject> eObjs) {
        List<Rectangle> rects = Lists.newArrayList();
        for (EObject eObj : eObjs) {
            ContainerShape shape = (ContainerShape) getMappingProvider().getPictogramElementForBusinessObject(eObj);
            GraphicsAlgorithm ga = shape.getGraphicsAlgorithm();
            rects.add(new Rectangle(ga.getX(), ga.getY(), ga.getWidth(), ga.getHeight()));
        }
        return rects;
    }

    /**
     * Calculates the size of a text.
     * 
     * @param text
     *            the text.
     * @param minWidth
     *            the minimum width.
     * @param minHeight
     *            the minimum height.
     * @return the text size.
     */
    protected final Dimension calculateSize(Text text, int minWidth, int minHeight) {
        IDimension textDim = GraphitiUi.getUiLayoutService().calculateTextSize(text.getValue(), text.getFont());
        int width = Math.max(textDim.getWidth(), 25);
        int height = Math.max(textDim.getHeight(), 14);
        return new Dimension(width, height);
    }

    /**
     * Returns a {@link GraphicsAlgorithm} nested in the hierarchy of a specific container shape.
     * 
     * @param containerShape
     *            the root container shape.
     * @param gaClass
     *            the target class of the {@link GraphicsAlgorithm} to be returned.
     * @param nestings
     *            the sequence of nested indices to be used accessing each level of the hierarchy.
     * @return the nested graphics algorithm.
     */
    public static <T extends GraphicsAlgorithm> T getGa(ContainerShape containerShape, Class<T> gaClass, int... nestings) {
        GraphicsAlgorithm ga = containerShape.getGraphicsAlgorithm();
        for (int nesting : nestings) {
            ga = ga.getGraphicsAlgorithmChildren().get(nesting);
        }
        return gaClass.cast(ga);
    }

}
