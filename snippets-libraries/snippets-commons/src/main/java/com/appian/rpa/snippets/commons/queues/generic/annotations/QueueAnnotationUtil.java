package com.appian.rpa.snippets.commons.queues.generic.annotations;

import java.lang.reflect.Field;

import com.appian.rpa.snippets.commons.utils.annotations.AnnotationUtils;
import com.novayre.jidoka.client.api.exceptions.JidokaException;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;

/**
 * Utility class for generic queues annotations.
 */
public class QueueAnnotationUtil {

	/** Private constructor */
	private QueueAnnotationUtil() {

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

		try {

			value = AnnotationUtils.getFieldValue(currentItem, fieldKey);

		} catch (JidokaException e) {
			throw new JidokaFatalException("Error getting the field key", e);
		}

		return value == null ? "" : value.toString();
	}

}
