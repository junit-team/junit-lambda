/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.support;

import static org.apiguardian.api.API.Status.DEPRECATED;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.apiguardian.api.API.Status.MAINTAINED;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * {@code AnnotationSupport} provides static utility methods for common tasks
 * regarding annotations &mdash; for example, checking if a class, method, or
 * field is annotated with a particular annotation; finding annotations on a
 * given class, method, or field; finding fields or methods annotated with
 * a particular annotation, etc.
 *
 * <p>{@link org.junit.platform.engine.TestEngine TestEngine} and extension
 * authors are encouraged to use these supported methods in order to align with
 * the behavior of the JUnit Platform.
 *
 * @since 1.0
 * @see ClassSupport
 * @see ModifierSupport
 * @see ReflectionSupport
 */
@API(status = MAINTAINED, since = "1.0")
public final class AnnotationSupport {

	private AnnotationSupport() {
		/* no-op */
	}

	/**
	 * Determine if an annotation of {@code annotationType} is either
	 * <em>present</em> or <em>meta-present</em> on the supplied optional
	 * {@code element}.
	 *
	 * <p><strong>Note:</strong> This method does not find repeatable annotations.
	 * To check for repeatable annotations, use {@link #findRepeatableAnnotations(Optional, Class)}
	 * and verify that the returned list is not empty.
	 *
	 * @param element an {@link Optional} containing the element on which to
	 * search for the annotation; may be {@code null} or <em>empty</em>
	 * @param annotationType the annotation type to search for; never {@code null}
	 * @return {@code true} if the annotation is present or meta-present
	 * @since 1.3
	 * @see #isAnnotated(AnnotatedElement, Class)
	 * @see #findAnnotation(Optional, Class)
	 * @see #findRepeatableAnnotations(Optional, Class)
	 */
	@API(status = MAINTAINED, since = "1.3")
	public static boolean isAnnotated(Optional<? extends AnnotatedElement> element,
			Class<? extends Annotation> annotationType) {

		return AnnotationUtils.isAnnotated(element, annotationType);
	}

	/**
	 * Determine if an annotation of {@code annotationType} is either
	 * <em>present</em> or <em>meta-present</em> on the supplied
	 * {@code element}.
	 *
	 * <p><strong>Note:</strong> This method does not find repeatable annotations.
	 * To check for repeatable annotations, use {@link #findRepeatableAnnotations(AnnotatedElement, Class)}
	 * and verify that the returned list is not empty.
	 *
	 * @param element the element on which to search for the annotation; may be
	 * {@code null}
	 * @param annotationType the annotation type to search for; never {@code null}
	 * @return {@code true} if the annotation is present or meta-present
	 * @see #isAnnotated(Optional, Class)
	 * @see #findAnnotation(AnnotatedElement, Class)
	 * @see #findRepeatableAnnotations(AnnotatedElement, Class)
	 */
	public static boolean isAnnotated(AnnotatedElement element, Class<? extends Annotation> annotationType) {
		return AnnotationUtils.isAnnotated(element, annotationType);
	}

	/**
	 * Find the first annotation of {@code annotationType} that is either
	 * <em>present</em> or <em>meta-present</em> on the supplied optional
	 * {@code element}.
	 *
	 * @param <A> the annotation type
	 * @param element an {@link Optional} containing the element on which to
	 * search for the annotation; may be {@code null} or <em>empty</em>
	 * @param annotationType the annotation type to search for; never {@code null}
	 * @return an {@code Optional} containing the annotation; never {@code null} but
	 * potentially empty
	 * @since 1.1
	 * @see #findAnnotation(AnnotatedElement, Class)
	 */
	@API(status = MAINTAINED, since = "1.1")
	public static <A extends Annotation> Optional<A> findAnnotation(Optional<? extends AnnotatedElement> element,
			Class<A> annotationType) {

		return AnnotationUtils.findAnnotation(element, annotationType);
	}

	/**
	 * Find the first annotation of {@code annotationType} that is either
	 * <em>directly present</em>, <em>meta-present</em>, or <em>indirectly
	 * present</em> on the supplied {@code element}.
	 *
	 * <p>If the element is a class and the annotation is neither <em>directly
	 * present</em> nor <em>meta-present</em> on the class, this method will
	 * additionally search on interfaces implemented by the class before
	 * finding an annotation that is <em>indirectly present</em> on the class.
	 *
	 * @param <A> the annotation type
	 * @param element the element on which to search for the annotation; may be
	 * {@code null}
	 * @param annotationType the annotation type to search for; never {@code null}
	 * @return an {@code Optional} containing the annotation; never {@code null} but
	 * potentially empty
	 */
	public static <A extends Annotation> Optional<A> findAnnotation(AnnotatedElement element, Class<A> annotationType) {
		return AnnotationUtils.findAnnotation(element, annotationType);
	}

	/**
	 * Find the first annotation of the specified type that is either
	 * <em>directly present</em>, <em>meta-present</em>, or <em>indirectly
	 * present</em> on the supplied class.
	 *
	 * <p>If the annotation is neither <em>directly present</em> nor <em>meta-present</em>
	 * on the class, this method will additionally search on interfaces implemented
	 * by the class before searching for an annotation that is <em>indirectly present</em>
	 * on the class (i.e., within the class inheritance hierarchy).
	 *
	 * <p>If the annotation still has not been found, this method will optionally
	 * search recursively through the enclosing class hierarchy if
	 * {@link SearchOption#INCLUDE_ENCLOSING_CLASSES} is specified.
	 *
	 * <p>If {@link SearchOption#DEFAULT} is specified, this method has the same
	 * semantics as {@link #findAnnotation(AnnotatedElement, Class)}.
	 *
	 * @param <A> the annotation type
	 * @param clazz the class on which to search for the annotation; may be {@code null}
	 * @param annotationType the annotation type to search for; never {@code null}
	 * @param searchOption the {@code SearchOption} to use; never {@code null}
	 * @return an {@code Optional} containing the annotation; never {@code null} but
	 * potentially empty
	 * @since 1.8
	 * @see SearchOption
	 * @see #findAnnotation(AnnotatedElement, Class)
	 * @deprecated Use {@link #findAnnotation(Class, Class, List)}
	 * (for {@code SearchOption.DEFAULT}) or
	 * {@link #findAnnotation(Class, Class, List)} (for
	 * {@code SearchOption.INCLUDE_ENCLOSING_CLASSES}) instead
	 */
	@Deprecated
	@API(status = DEPRECATED, since = "1.12")
	@SuppressWarnings("deprecation")
	public static <A extends Annotation> Optional<A> findAnnotation(Class<?> clazz, Class<A> annotationType,
			SearchOption searchOption) {

		Preconditions.notNull(searchOption, "SearchOption must not be null");

		return AnnotationUtils.findAnnotation(clazz, annotationType,
			searchOption == SearchOption.INCLUDE_ENCLOSING_CLASSES);
	}

	/**
	 * Find the first annotation of the specified type that is either
	 * <em>directly present</em>, <em>meta-present</em>, or <em>indirectly
	 * present</em> on the supplied class.
	 *
	 * <p>If the annotation is neither <em>directly present</em> nor <em>meta-present</em>
	 * on the class, this method will additionally search on interfaces implemented
	 * by the class before searching for an annotation that is <em>indirectly present</em>
	 * on the class (i.e., within the class inheritance hierarchy).
	 *
	 * <p>If the annotation still has not been found, this method will optionally
	 * search recursively through the supplied enclosing instance types, starting
	 * at the innermost enclosing class (the last one in the supplied list of
	 * {@code enclosingInstanceTypes}).
	 *
	 * @implNote The classes supplied as {@code enclosingInstanceTypes} may
	 * differ from the classes returned from invocations of
	 * {@link Class#getEnclosingClass()} &mdash; for example, when a nested test
	 * class is inherited from a superclass.
	 *
	 * @param <A> the annotation type
	 * @param clazz the class on which to search for the annotation; may be {@code null}
	 * @param annotationType the annotation type to search for; never {@code null}
	 * @param enclosingInstanceTypes the runtime types of the enclosing
	 * instances for the class, ordered from outermost to innermost,
	 * excluding {@code clazz}; never {@code null}
	 * @return an {@code Optional} containing the annotation; never {@code null} but
	 * potentially empty if {@code clazz} is not an inner class
	 * @since 1.12
	 * @see #findAnnotation(AnnotatedElement, Class)
	 */
	@API(status = EXPERIMENTAL, since = "1.12")
	public static <A extends Annotation> Optional<A> findAnnotation(Class<?> clazz, Class<A> annotationType,
			List<Class<?>> enclosingInstanceTypes) {

		Preconditions.notNull(enclosingInstanceTypes, "enclosingInstanceTypes must not be null");

		Optional<A> annotation = findAnnotation(clazz, annotationType);
		if (!annotation.isPresent()) {
			ListIterator<Class<?>> iterator = enclosingInstanceTypes.listIterator(enclosingInstanceTypes.size());
			while (iterator.hasPrevious()) {
				annotation = findAnnotation(iterator.previous(), annotationType);
				if (annotation.isPresent()) {
					break;
				}
			}
		}
		return annotation;
	}

	/**
	 * Find all <em>repeatable</em> {@linkplain Annotation annotations} of the
	 * supplied {@code annotationType} that are either <em>present</em>,
	 * <em>indirectly present</em>, or <em>meta-present</em> on the supplied
	 * optional {@code element}.
	 *
	 * <p>See {@link #findRepeatableAnnotations(AnnotatedElement, Class)} for
	 * details of the algorithm used.
	 *
	 * @param <A> the annotation type
	 * @param element an {@link Optional} containing the element on which to
	 * search for the annotation; may be {@code null} or <em>empty</em>
	 * @param annotationType the repeatable annotation type to search for; never {@code null}
	 * @return an immutable list of all such annotations found; never {@code null}
	 * @since 1.5
	 * @see java.lang.annotation.Repeatable
	 * @see java.lang.annotation.Inherited
	 * @see #findRepeatableAnnotations(AnnotatedElement, Class)
	 */
	@API(status = MAINTAINED, since = "1.5")
	public static <A extends Annotation> List<A> findRepeatableAnnotations(Optional<? extends AnnotatedElement> element,
			Class<A> annotationType) {

		return AnnotationUtils.findRepeatableAnnotations(element, annotationType);
	}

	/**
	 * Find all <em>repeatable</em> {@linkplain Annotation annotations} of the
	 * supplied {@code annotationType} that are either <em>present</em>,
	 * <em>indirectly present</em>, or <em>meta-present</em> on the supplied
	 * {@link AnnotatedElement}.
	 *
	 * <p>This method extends the functionality of
	 * {@link java.lang.reflect.AnnotatedElement#getAnnotationsByType(Class)}
	 * with additional support for meta-annotations.
	 *
	 * <p>In addition, if the element is a class and the repeatable annotation
	 * is {@link java.lang.annotation.Inherited @Inherited}, this method will
	 * search on superclasses first in order to support top-down semantics.
	 * The result is that this algorithm finds repeatable annotations that
	 * would be <em>shadowed</em> and therefore not visible according to Java's
	 * standard semantics for inherited, repeatable annotations, but most
	 * developers will naturally assume that all repeatable annotations in JUnit
	 * are discovered regardless of whether they are declared stand-alone, in a
	 * container, or as a meta-annotation (e.g., multiple declarations of
	 * {@code @ExtendWith} within a test class hierarchy).
	 *
	 * <p>If the element is a class and the repeatable annotation is not
	 * discovered within the class hierarchy, this method will additionally
	 * search on interfaces implemented by each class in the hierarchy.
	 *
	 * <p>If the supplied {@code element} is {@code null}, this method returns
	 * an empty list.
	 *
	 * <p>As of JUnit Platform 1.5, the search algorithm will also find
	 * repeatable annotations used as meta-annotations on other repeatable
	 * annotations.
	 *
	 * @param <A> the annotation type
	 * @param element the element to search on; may be {@code null}
	 * @param annotationType the repeatable annotation type to search for; never {@code null}
	 * @return an immutable list of all such annotations found; never {@code null}
	 * @see java.lang.annotation.Repeatable
	 * @see java.lang.annotation.Inherited
	 */
	public static <A extends Annotation> List<A> findRepeatableAnnotations(AnnotatedElement element,
			Class<A> annotationType) {

		return AnnotationUtils.findRepeatableAnnotations(element, annotationType);
	}

	/**
	 * Find all {@code public} {@linkplain Field fields} of the supplied class
	 * or interface that are declared to be of the specified {@code fieldType}
	 * and are annotated or <em>meta-annotated</em> with the specified
	 * {@code annotationType}.
	 *
	 * <p>Consult the Javadoc for {@link Class#getFields()} for details on
	 * inheritance and ordering.
	 *
	 * @param clazz the class or interface in which to find the fields; never {@code null}
	 * @param fieldType the declared type of fields to find; never {@code null}
	 * @param annotationType the annotation type to search for; never {@code null}
	 * @return the list of all such fields found; neither {@code null} nor mutable
	 * @see Class#getFields()
	 * @see Field#getType()
	 * @see #findAnnotatedFields(Class, Class)
	 * @see #findAnnotatedFields(Class, Class, Predicate, HierarchyTraversalMode)
	 * @see ReflectionSupport#findFields(Class, Predicate, HierarchyTraversalMode)
	 * @see ReflectionSupport#tryToReadFieldValue(Field, Object)
	 */
	public static List<Field> findPublicAnnotatedFields(Class<?> clazz, Class<?> fieldType,
			Class<? extends Annotation> annotationType) {

		return AnnotationUtils.findPublicAnnotatedFields(clazz, fieldType, annotationType);
	}

	/**
	 * Find all distinct {@linkplain Field fields} of the supplied class or
	 * interface that are annotated or <em>meta-annotated</em> with the specified
	 * {@code annotationType}, using top-down search semantics within the type
	 * hierarchy.
	 *
	 * <p>Fields declared in the same class or interface will be ordered using
	 * an algorithm that is deterministic but intentionally nonobvious.
	 *
	 * <p>The results will not contain fields that are <em>hidden</em> or
	 * {@linkplain Field#isSynthetic() synthetic}.
	 *
	 * @param clazz the class or interface in which to find the fields; never {@code null}
	 * @param annotationType the annotation type to search for; never {@code null}
	 * @return the list of all such fields found; neither {@code null} nor mutable
	 * @since 1.4
	 * @see Class#getDeclaredFields()
	 * @see #findPublicAnnotatedFields(Class, Class, Class)
	 * @see #findAnnotatedFields(Class, Class, Predicate, HierarchyTraversalMode)
	 * @see ReflectionSupport#findFields(Class, Predicate, HierarchyTraversalMode)
	 * @see ReflectionSupport#tryToReadFieldValue(Field, Object)
	 */
	@API(status = MAINTAINED, since = "1.4")
	public static List<Field> findAnnotatedFields(Class<?> clazz, Class<? extends Annotation> annotationType) {
		return findAnnotatedFields(clazz, annotationType, field -> true);
	}

	/**
	 * Find all distinct {@linkplain Field fields} of the supplied class or
	 * interface that are annotated or <em>meta-annotated</em> with the specified
	 * {@code annotationType} and match the specified {@code predicate}, using
	 * top-down search semantics within the type hierarchy.
	 *
	 * <p>Fields declared in the same class or interface will be ordered using
	 * an algorithm that is deterministic but intentionally nonobvious.
	 *
	 * <p>The results will not contain fields that are <em>hidden</em> or
	 * {@linkplain Field#isSynthetic() synthetic}.
	 *
	 * @param clazz the class or interface in which to find the fields; never {@code null}
	 * @param annotationType the annotation type to search for; never {@code null}
	 * @param predicate the field filter; never {@code null}
	 * @return the list of all such fields found; neither {@code null} nor mutable
	 * @since 1.10
	 * @see Class#getDeclaredFields()
	 * @see #findPublicAnnotatedFields(Class, Class, Class)
	 * @see #findAnnotatedFields(Class, Class, Predicate, HierarchyTraversalMode)
	 * @see ReflectionSupport#findFields(Class, Predicate, HierarchyTraversalMode)
	 * @see ReflectionSupport#tryToReadFieldValue(Field, Object)
	 */
	@API(status = MAINTAINED, since = "1.10")
	public static List<Field> findAnnotatedFields(Class<?> clazz, Class<? extends Annotation> annotationType,
			Predicate<Field> predicate) {
		return AnnotationUtils.findAnnotatedFields(clazz, annotationType, predicate);
	}

	/**
	 * Find all distinct {@linkplain Field fields} of the supplied class or
	 * interface that are annotated or <em>meta-annotated</em> with the specified
	 * {@code annotationType} and match the specified {@code predicate}, using
	 * the supplied hierarchy traversal mode.
	 *
	 * <p>Fields declared in the same class or interface will be ordered using
	 * an algorithm that is deterministic but intentionally nonobvious.
	 *
	 * <p>The results will not contain fields that are <em>hidden</em> or
	 * {@linkplain Field#isSynthetic() synthetic}.
	 *
	 * @param clazz the class or interface in which to find the fields; never {@code null}
	 * @param annotationType the annotation type to search for; never {@code null}
	 * @param predicate the field filter; never {@code null}
	 * @param traversalMode the hierarchy traversal mode; never {@code null}
	 * @return the list of all such fields found; neither {@code null} nor mutable
	 * @since 1.4
	 * @see Class#getDeclaredFields()
	 * @see #findAnnotatedFields(Class, Class)
	 * @see ReflectionSupport#findFields(Class, Predicate, HierarchyTraversalMode)
	 * @see ReflectionSupport#tryToReadFieldValue(Field, Object)
	 */
	@API(status = MAINTAINED, since = "1.4")
	public static List<Field> findAnnotatedFields(Class<?> clazz, Class<? extends Annotation> annotationType,
			Predicate<Field> predicate, HierarchyTraversalMode traversalMode) {

		Preconditions.notNull(traversalMode, "HierarchyTraversalMode must not be null");

		return AnnotationUtils.findAnnotatedFields(clazz, annotationType, predicate,
			ReflectionUtils.HierarchyTraversalMode.valueOf(traversalMode.name()));
	}

	/**
	 * Find the values of all non-static {@linkplain Field fields} of the supplied
	 * {@code instance} that are annotated or <em>meta-annotated</em> with the
	 * specified {@code annotationType}, using top-down search semantics within
	 * the type hierarchy.
	 *
	 * <p>Values from fields declared in the same class or interface will be
	 * ordered using an algorithm that is deterministic but intentionally
	 * nonobvious.
	 *
	 * <p>The results will not contain values from fields that are <em>hidden</em>
	 * or {@linkplain Field#isSynthetic() synthetic}.
	 *
	 * @param instance the instance in which to find the fields; never {@code null}
	 * @param annotationType the annotation type to search for; never {@code null}
	 * @return the list of all such field values found; neither {@code null} nor mutable
	 * @since 1.4
	 * @see #findAnnotatedFields(Class, Class)
	 * @see #findAnnotatedFields(Class, Class, Predicate, HierarchyTraversalMode)
	 * @see ReflectionSupport#findFields(Class, Predicate, HierarchyTraversalMode)
	 * @see ReflectionSupport#tryToReadFieldValue(Field, Object)
	 */
	@API(status = MAINTAINED, since = "1.4")
	public static List<Object> findAnnotatedFieldValues(Object instance, Class<? extends Annotation> annotationType) {
		Preconditions.notNull(instance, "instance must not be null");

		List<Field> fields = findAnnotatedFields(instance.getClass(), annotationType, ModifierSupport::isNotStatic,
			HierarchyTraversalMode.TOP_DOWN);

		return ReflectionUtils.readFieldValues(fields, instance);
	}

	/**
	 * Find the values of all static {@linkplain Field fields} of the supplied
	 * class or interface that are annotated or <em>meta-annotated</em> with the
	 * specified {@code annotationType}, using top-down search semantics within
	 * the type hierarchy.
	 *
	 * <p>Values from fields declared in the same class or interface will be
	 * ordered using an algorithm that is deterministic but intentionally
	 * nonobvious.
	 *
	 * <p>The results will not contain values from fields that are <em>hidden</em>
	 * or {@linkplain Field#isSynthetic() synthetic}.
	 *
	 * @param clazz the class or interface in which to find the fields; never {@code null}
	 * @param annotationType the annotation type to search for; never {@code null}
	 * @return the list of all such field values found; neither {@code null} nor mutable
	 * @since 1.4
	 * @see #findAnnotatedFields(Class, Class)
	 * @see #findAnnotatedFields(Class, Class, Predicate, HierarchyTraversalMode)
	 * @see ReflectionSupport#findFields(Class, Predicate, HierarchyTraversalMode)
	 * @see ReflectionSupport#tryToReadFieldValue(Field, Object)
	 */
	@API(status = MAINTAINED, since = "1.4")
	public static List<Object> findAnnotatedFieldValues(Class<?> clazz, Class<? extends Annotation> annotationType) {

		List<Field> fields = findAnnotatedFields(clazz, annotationType, ModifierSupport::isStatic,
			HierarchyTraversalMode.TOP_DOWN);

		return ReflectionUtils.readFieldValues(fields, null);
	}

	/**
	 * Find the values of all non-static {@linkplain Field fields} of the supplied
	 * {@code instance} that are declared to be of the specified {@code fieldType}
	 * and are annotated or <em>meta-annotated</em> with the specified
	 * {@code annotationType}, using top-down search semantics within the type
	 * hierarchy.
	 *
	 * <p>Values from fields declared in the same class or interface will be
	 * ordered using an algorithm that is deterministic but intentionally
	 * nonobvious.
	 *
	 * <p>The results will not contain values from fields that are <em>hidden</em>
	 * or {@linkplain Field#isSynthetic() synthetic}.
	 *
	 * @param instance the instance in which to find the fields; never {@code null}
	 * @param annotationType the annotation type to search for; never {@code null}
	 * @param fieldType the declared type of fields to find; never {@code null}
	 * @return the list of all such field values found; neither {@code null} nor mutable
	 * @since 1.4
	 * @see Field#getType()
	 * @see #findAnnotatedFields(Class, Class)
	 * @see #findAnnotatedFields(Class, Class, Predicate, HierarchyTraversalMode)
	 * @see ReflectionSupport#findFields(Class, Predicate, HierarchyTraversalMode)
	 * @see ReflectionSupport#tryToReadFieldValue(Field, Object)
	 */
	@SuppressWarnings("unchecked")
	@API(status = MAINTAINED, since = "1.4")
	public static <T> List<T> findAnnotatedFieldValues(Object instance, Class<? extends Annotation> annotationType,
			Class<T> fieldType) {

		Preconditions.notNull(instance, "instance must not be null");
		Preconditions.notNull(fieldType, "fieldType must not be null");

		Predicate<Field> predicate = //
			field -> ModifierSupport.isNotStatic(field) && fieldType.isAssignableFrom(field.getType());

		List<Field> fields = findAnnotatedFields(instance.getClass(), annotationType, predicate,
			HierarchyTraversalMode.TOP_DOWN);

		return (List<T>) ReflectionUtils.readFieldValues(fields, instance);
	}

	/**
	 * Find the values of all static {@linkplain Field fields} of the supplied
	 * class or interface that are declared to be of the specified
	 * {@code fieldType} and are annotated or <em>meta-annotated</em> with the
	 * specified {@code annotationType}, using top-down search semantics within
	 * the type hierarchy.
	 *
	 * <p>Values from fields declared in the same class or interface will be
	 * ordered using an algorithm that is deterministic but intentionally
	 * nonobvious.
	 *
	 * <p>The results will not contain values from fields that are <em>hidden</em>
	 * or {@linkplain Field#isSynthetic() synthetic}.
	 *
	 * @param clazz the class or interface in which to find the fields; never {@code null}
	 * @param annotationType the annotation type to search for; never {@code null}
	 * @param fieldType the declared type of fields to find; never {@code null}
	 * @return the list of all such field values found; neither {@code null} nor mutable
	 * @since 1.4
	 * @see Field#getType()
	 * @see #findAnnotatedFields(Class, Class)
	 * @see #findAnnotatedFields(Class, Class, Predicate, HierarchyTraversalMode)
	 * @see ReflectionSupport#findFields(Class, Predicate, HierarchyTraversalMode)
	 * @see ReflectionSupport#tryToReadFieldValue(Field, Object)
	 */
	@SuppressWarnings("unchecked")
	@API(status = MAINTAINED, since = "1.4")
	public static <T> List<T> findAnnotatedFieldValues(Class<?> clazz, Class<? extends Annotation> annotationType,
			Class<T> fieldType) {

		Preconditions.notNull(fieldType, "fieldType must not be null");

		Predicate<Field> predicate = //
			field -> ModifierSupport.isStatic(field) && fieldType.isAssignableFrom(field.getType());

		List<Field> fields = findAnnotatedFields(clazz, annotationType, predicate, HierarchyTraversalMode.TOP_DOWN);

		return (List<T>) ReflectionUtils.readFieldValues(fields, null);
	}

	/**
	 * Find all distinct {@linkplain Method methods} of the supplied class or
	 * interface that are annotated or <em>meta-annotated</em> with the specified
	 * {@code annotationType}.
	 *
	 * @param clazz the class or interface in which to find the methods; never {@code null}
	 * @param annotationType the annotation type to search for; never {@code null}
	 * @param traversalMode the hierarchy traversal mode; never {@code null}
	 * @return the list of all such methods found; neither {@code null} nor mutable
	 */
	public static List<Method> findAnnotatedMethods(Class<?> clazz, Class<? extends Annotation> annotationType,
			HierarchyTraversalMode traversalMode) {

		Preconditions.notNull(traversalMode, "HierarchyTraversalMode must not be null");

		return AnnotationUtils.findAnnotatedMethods(clazz, annotationType,
			ReflectionUtils.HierarchyTraversalMode.valueOf(traversalMode.name()));
	}

}
