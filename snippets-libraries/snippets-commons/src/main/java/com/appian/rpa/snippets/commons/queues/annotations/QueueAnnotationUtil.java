package com.appian.rpa.snippets.commons.queues.annotations;

import java.lang.reflect.Field;

import com.appian.rpa.snippets.commons.utils.annotations.AnnotationUtil;
import com.novayre.jidoka.client.api.exceptions.JidokaException;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;

public class QueueAnnotationUtil {
	
	/** Private constructor */
	private QueueAnnotationUtil() {
		
	}
	
	/**
	 * Gets the value of the key field given
	 * 
	 * @param <T> Type of the value returned
	 * @param currentItem Object where to search the key field
	 * 
	 * @return The key field value
	 */
	public static <T> String getKeyFieldValue(T currentItem) {

		Field fieldKey = AnnotationUtil.getFirstFieldWithAnnotation(currentItem.getClass(), AItemKey.class);
		Object value = null;

		try {

			value = AnnotationUtil.getFieldValue(currentItem, fieldKey);

		} catch (JidokaException e) {
			throw new JidokaFatalException("Error getting the field key", e);
		}

		return value == null ? "" : value.toString();
	}

}
