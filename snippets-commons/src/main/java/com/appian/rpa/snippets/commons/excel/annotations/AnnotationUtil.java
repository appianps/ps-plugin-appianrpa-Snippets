package com.appian.rpa.snippets.commons.excel.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.collections.CollectionUtils;

import com.appian.rpa.snippets.commons.excel.annotations.utils.ReflectionUtil;
import com.novayre.jidoka.client.api.exceptions.JidokaException;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;

/**
 * Utility class for annotations.
 */
public final class AnnotationUtil {

	/**
	 * AnnotationUtil private constructor
	 */
	private AnnotationUtil() {

	}

	/**
	 * Returns the value of the field <code>field</code> of the object
	 * <code>obj</code>
	 * 
	 * @param obj   Can't be <code>null</code>
	 * @param field Can't be <code>null</code>
	 * @return Object with the field value
	 * @throws JidokaException
	 */
	public static Object getFieldValue(Object obj, Field field) throws JidokaException {

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
	 * 
	 * Sets in the property <code>propertyName</code> the value <code>value</code>
	 * 
	 * @param obj          Can't be <code>null</code>
	 * @param propertyName Can't be <code>null</code>
	 * @param value
	 * @throws JidokaException
	 */
	public static void setFieldValue(Object obj, String propertyName, Object value) throws JidokaException {

		try {

			BeanUtils.setProperty(obj, propertyName, value);

		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new JidokaException(
					String.format("Error setting the property %s to the value %s", propertyName, value.toString()), e);
		}
	}

	public static <T> String getKeyFieldValue(T currentItem) {

		Field fieldKey = getFirstFieldWithAnnotation(currentItem.getClass(), AExcelFieldKey.class);
		Object value = null;

		try {

			value = getFieldValue(currentItem, fieldKey);

		} catch (JidokaException e) {
			throw new JidokaFatalException("Error getting the field key", e);
		}

		return value == null ? "" : value.toString();
	}

}
