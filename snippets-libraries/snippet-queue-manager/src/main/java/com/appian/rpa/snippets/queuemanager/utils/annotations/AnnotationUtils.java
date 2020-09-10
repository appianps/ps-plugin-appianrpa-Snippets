package com.appian.rpa.snippets.queuemanager.utils.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;

import com.appian.rpa.snippets.commons.utils.reflection.ReflectionUtil;
import com.appian.rpa.snippets.queuemanager.annotations.AItemKey;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;

public class AnnotationUtils {

	/**
	 * AnnotationUtil private constructor
	 */
	private AnnotationUtils() {

	}

	/**
	 * Returns the value of the field <code>field</code> of the object
	 * <code>obj</code>
	 * 
	 * @param obj   Can't be <code>null</code>
	 * @param field Can't be <code>null</code>
	 * @return Object with the field value
	 */
	public static Object getFieldValue(Object obj, Field field) {

		return ReflectionUtil.invokeBeanMethod(obj, field.getName(), true, ReflectionUtil.GETTERS_PREFIXES,
				new Class<?>[] {}, new Object[] {});
	}

	/**
	 * Returns the list of fields of the class <code>clazz</code> annotated with
	 * <code>annotationType</code>
	 * 
	 * @param clazz          Can't be <code>null</code>
	 * @param annotationType Can't be <code>null</code>
	 * @return List with the fields annotated with the <code>annotationType</code>
	 *         annotation
	 */
	public static <A extends Annotation> List<Field> getFieldsWithAnnotation(Class<?> clazz,
			final Class<A> annotationType) {

		Field[] fields = clazz.getDeclaredFields();

		List<Field> fieldList = Arrays.asList(fields);

		List<Field> col = fieldList.stream().filter(f -> f.isAnnotationPresent(annotationType))
				.collect(Collectors.toList());

		return new LinkedList<>(col);
	}

	/**
	 * Returns the first field with the given annotation.
	 * 
	 * @param clazz
	 * @param annotationType
	 * @return
	 */
	public static <A extends Annotation> Field getFirstFieldWithAnnotation(Class<?> clazz,
			final Class<A> annotationType) {

		List<Field> fields = getFieldsWithAnnotation(clazz, annotationType);

		if (CollectionUtils.isEmpty(fields)) {
			return null;
		}

		return fields.get(0);
	}

	/**
	 * Gets the value of the key field given
	 * 
	 * @param <T>         Type of the value returned
	 * @param currentItem Object where to search the key field
	 * 
	 * @return The key field value
	 */
	public static <T> String getKeyFieldValue(T currentItem) {

		Field fieldKey = AnnotationUtils.getFirstFieldWithAnnotation(currentItem.getClass(), AItemKey.class);
		Object value = null;

		value = AnnotationUtils.getFieldValue(currentItem, fieldKey);

		return value == null ? "" : value.toString();
	}

	/**
	 * 
	 * Sets in the property <code>propertyName</code> the value <code>value</code>
	 * 
	 * @param obj          Can't be <code>null</code>
	 * @param propertyName Can't be <code>null</code>
	 * @param value
	 */
	public static void setFieldValue(Object obj, String propertyName, Object value) {

		try {

			BeanUtils.setProperty(obj, propertyName, value);

		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new JidokaFatalException(
					String.format("Error setting the property %s to the value %s", propertyName, value.toString()), e);
		}
	}

}
