package org.ifml.eclipse.graphiti.patterns;

import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.graphiti.features.context.IAddConnectionContext;
import org.eclipse.graphiti.features.context.IAddContext;
import org.eclipse.graphiti.features.context.ICreateConnectionContext;
import org.eclipse.graphiti.features.context.impl.AddConnectionContext;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.pattern.AbstractConnectionPattern;
import org.ifml.base.Objects2;
import org.ifml.base.WordFormat;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

/**
 * An abstract base class for Graphiti connection patterns based on EMF models.
 * 
 * @param <T>
 *            the model type.
 */
public abstract class AbstractGfEmfConnectionPattern<T extends EObject> extends AbstractConnectionPattern {

    private final Class<T> instanceClass;

    /**
     * Constructs a new pattern.
     * 
     * @param instanceClass
     *            the instance class.
     */
    public AbstractGfEmfConnectionPattern(Class<T> instanceClass) {
        this.instanceClass = instanceClass;
    }

    /**
     * Returns the model {@link EClass} associated with this pattern.
     * 
     * @return the model class.
     */
    protected abstract EClass getEClass();

    private boolean isMainBusinessObjectApplicable(Object mainBusinessObject) {
        return getEClass().isInstance(mainBusinessObject);
    }

    @Override
    public final boolean canAdd(IAddContext context) {
        return (context instanceof IAddConnectionContext) && (isMainBusinessObjectApplicable(context.getNewObject()));
    }

    @Override
    public final boolean canStartConnection(ICreateConnectionContext context) {
        EObject sourceObject = Objects2.as(getBusinessObjectForPictogramElement(context.getSourcePictogramElement()), EObject.class);
        if (sourceObject == null) {
            return false;
        }
        return canStartFrom(sourceObject);
    }

    /**
     * Returns whether a new model object, having type identified by the {@link #getEClass() method} can start from the given source
     * object.
     * 
     * @param sourceObject
     *            the source object.
     * @return {@code true} if a new model object can start from {@code sourceObject}.
     */
    protected abstract boolean canStartFrom(EObject sourceObject);

    @Override
    public final boolean canCreate(ICreateConnectionContext context) {
        EObject sourceObject = Objects2.as(getBusinessObjectForPictogramElement(context.getSourcePictogramElement()), EObject.class);
        EObject targetObject = Objects2.as(getBusinessObjectForPictogramElement(context.getTargetPictogramElement()), EObject.class);
        if ((sourceObject == null) || (targetObject == null)) {
            return false;
        }
        return canConnect(sourceObject, targetObject);
    }

    /**
     * Returns whether a new model object, having type identified by the {@link #getEClass() method} can connect the given source and
     * target objects.
     * 
     * @param sourceObject
     *            the source object.
     * @param targetObject
     *            the target object.
     * @return {@code true} if a new model object can be used to connect {@code sourceObject} with {@code targetObject}
     */
    protected abstract boolean canConnect(EObject sourceObject, EObject targetObject);

    @Override
    public final PictogramElement add(IAddContext context) {
        PictogramElement pictogramElement = addPictogramElement((IAddConnectionContext) context);
        Object addedDomainObject = context.getNewObject();
        link(pictogramElement, addedDomainObject);
        return pictogramElement;
    }

    /**
     * Adds a pictogram element to the diagram.
     * 
     * @param context
     *            the add context holding information about the added domain object.
     * @return the newly created pictogram element.
     */
    protected abstract PictogramElement addPictogramElement(IAddConnectionContext context);

    @Override
    public final String getCreateName() {
        return WordFormat.UPPER_CAMEL.to(WordFormat.CAPITALIZED_WORDS, getEClass().getName());
    }

    @Override
    public final Connection create(ICreateConnectionContext context) {
        EObject newObject = EcoreUtil.create(getEClass());
        EObject sourceObject = Objects2.as(getBusinessObjectForPictogramElement(context.getSourcePictogramElement()), EObject.class);
        EObject targetObject = Objects2.as(getBusinessObjectForPictogramElement(context.getTargetPictogramElement()), EObject.class);
        Optional<EReference> connRef = getConnectionReference(sourceObject.eClass());
        Preconditions.checkState(connRef.isPresent(), "No connection reference specified for %s (source = %s)", getEClass().getName(),
                sourceObject.eClass().getName());
        if (connRef.get().isMany()) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) sourceObject.eGet(connRef.get());
            list.add(newObject);
        } else {
            sourceObject.eSet(connRef.get(), newObject);
        }

        Optional<EReference> inverseConnRef = getInverseConnectionReference(targetObject.eClass());
        if (inverseConnRef.isPresent()) {
            if (inverseConnRef.get().isMany()) {
                @SuppressWarnings("unchecked")
                List<Object> list = (List<Object>) targetObject.eGet(inverseConnRef.get());
                list.add(newObject);
            } else {
                targetObject.eSet(inverseConnRef.get(), newObject);
            }
        }
        Optional<EReference> sourceRef = getSourceReference();
        Preconditions.checkState(sourceRef.isPresent(), "No source attribute reference specified for %s", getEClass());
        newObject.eSet(sourceRef.get(), sourceObject);
        Optional<EReference> targetRef = getTargetReference();
        Preconditions.checkState(targetRef.isPresent(), "No target attribute reference specified for %s", getEClass());
        newObject.eSet(targetRef.get(), targetObject);
        AddConnectionContext addContext = new AddConnectionContext(context.getSourceAnchor(), context.getTargetAnchor());
        addContext.setNewObject(newObject);
        Connection newConnection = (Connection) getFeatureProvider().addIfPossible(addContext);
        return newConnection;
    }

    /**
     * Returns the reference for a connection relationship, where the source type is specified by the {@code sourceClass} argument and
     * the connection type is specified by the {@link #getEClass()} method.
     * 
     * @param sourceClass
     *            the source class.
     * @return the connection reference.
     */
    protected abstract Optional<EReference> getConnectionReference(EClass sourceClass);

    /**
     * Returns the optional inverse reference for a connection relationship, where the target type is specified by the
     * {@code targetClass} argument and the connection type is specified by the {@link #getEClass()} method.
     * 
     * @param targetClass
     *            the target class.
     * @return the inverse connection reference.
     */
    protected abstract Optional<EReference> getInverseConnectionReference(EClass targetClass);

    /**
     * Returns the reference for specifying the source of a connection.
     * 
     * @return the source reference.
     */
    protected abstract Optional<EReference> getSourceReference();

    /**
     * Returns the reference for specifying the target of a connection.
     * 
     * @return the target reference.
     */
    protected abstract Optional<EReference> getTargetReference();

}
