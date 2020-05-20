package com.appian.rpa.snippets.queuemanager.check;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import com.appian.rpa.snippets.commons.utils.reflection.ReflectionUtil;
import com.appian.rpa.snippets.queuemanager.annotations.AItemField;
import com.appian.rpa.snippets.queuemanager.annotations.AItemKey;
import com.appian.rpa.snippets.queuemanager.utils.annotations.AnnotationUtils;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;

/**
 * Class to check if the given class meets the requirements for the library to
 * work
 *
 */
public class CheckModelUtils {

	/**
	 * Private no arguments constructor
	 */
	private CheckModelUtils() {

	}

	/**
	 * Checks if the given class has no arguments public constructor
	 * 
	 * @param modelClass Given model class
	 */
	public static void hasNoArgumentsPublicConstructor(Class<?> modelClass) {
		boolean hasNoArgumentsConstructor = false;
		for (Constructor<?> constructor : modelClass.getConstructors()) {
			if (constructor.getParameterCount() == 0) {
				hasNoArgumentsConstructor = true;
			}
		}
		if (!hasNoArgumentsConstructor) {
			throw new JidokaFatalException("The model hasn't got a no arguments public constructor");
		}
	}

	/**
	 * Checks if the class has all the needed get and set methods
	 * 
	 * @param modelClass Given model class
	 */
	public static void checkGettersSetters(Class<?> modelClass) {
		try {
			List<Field> fields = AnnotationUtils.getFieldsWithAnnotation(modelClass, AItemField.class);

			for (Field field : fields) {
				Method m = ReflectionUtil.getBeanMethod(modelClass.getDeclaredConstructor().newInstance(),
						field.getName(), ReflectionUtil.GETTERS_PREFIXES, new Class<?>[] {});
				if (m == null) {
					throw new JidokaFatalException(String.format(
							"Get method must be created to returns the value of the field %s with the format (get/is)FieldName()",
							field.getName()));
				}

				Method m2 = ReflectionUtil.getBeanMethod(modelClass.getDeclaredConstructor().newInstance(),
						field.getName(), ReflectionUtil.SETTERS_PREFIXES, new Class<?>[] { m.getReturnType() });
				if (m2 == null) {
					throw new JidokaFatalException(String.format(
							"Set method must be created to returns the value of the field %s with the format setFieldName()",
							field.getName()));
				}
			}
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new JidokaFatalException("Error checking if get/set methods exists");
		}

	}

	/**
	 * Checks if the class has any field with the {@linkplain AItemField} annotation
	 * and at least one and only one field with the {@linkplain AItemKey} annotation
	 * 
	 * @param modelClass Given model class
	 */
	public static void checkFieldsWithAnnotations(Class<?> modelClass) {

		List<Field> fields = AnnotationUtils.getFieldsWithAnnotation(modelClass, AItemField.class);

		if (fields.isEmpty()) {
			throw new JidokaFatalException(
					"There is no fields with the annotation @" + AItemField.class.getSimpleName());
		}

		List<Field> fieldsKey = AnnotationUtils.getFieldsWithAnnotation(modelClass, AItemKey.class);

		if (fieldsKey.isEmpty()) {
			throw new JidokaFatalException("There is no fields with the annotation @" + AItemKey.class.getSimpleName());
		} else if (fieldsKey.size() > 1) {
			throw new JidokaFatalException(
					"The model must have only one field with the annotation @" + AItemKey.class.getSimpleName());
		}

	}

	/**
	 * Checks if the model class implements to the interface
	 * {@linkplain Serializable}.
	 * 
	 * @param modelClass Given model class
	 */
	public static void checkExtendsSerializable(Class<?> modelClass) {
		boolean extendsSerializable = Serializable.class.isAssignableFrom(modelClass);

		if (!extendsSerializable) {
			throw new JidokaFatalException("The model class must implement to the interface java.io.Serializable");
		}
	}

}
