package com.appian.rpa.snippets.commons.queues.conversion;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.appian.rpa.snippets.commons.queues.annotations.AItemField;
import com.appian.rpa.snippets.commons.utils.annotations.AnnotationUtil;
import com.appian.rpa.snippets.commons.utils.conversion.ConversionUtils;
import com.novayre.jidoka.client.api.exceptions.JidokaException;

public class QueueConversionUtils {
	
	/** Private constructor */
	private QueueConversionUtils() {
		// Private constructor
	}
	
	/**
	 * Maps a {@link Map} into an {@link Object}
	 * 
	 * @param <T>   Returned object type
	 * @param map   Map to be mapped
	 * @param clazz Mapper class
	 * @return The mapped object
	 * 
	 * @throws {@link JidokaException}
	 */
	@SuppressWarnings("deprecation")
	public static <T> T map2Object(Map<String, String> map, Class<T> clazz) throws JidokaException {

		T object = null;

		try {

			object = clazz.newInstance();

			List<Field> fields = AnnotationUtil.getFieldsWithAnnotation(clazz, AItemField.class);

			for (Field field : fields) {

				AItemField annotation = field.getAnnotation(AItemField.class);

				Object contenido = ConversionUtils.jacksonToObject(map.get(annotation.fieldName()), field.getType());

				AnnotationUtil.setFieldValue(object, field.getName(), contenido);

			}

		} catch (Exception e) {
			throw new JidokaException("Error mapping map to object", e);
		}

		return object;
	}

	/**
	 * Maps an {@link Object} into a {@link Map}
	 * 
	 * 
	 * @param object Object to be mapped
	 * @return The mapped map
	 * 
	 * @throws {@link JidokaException}
	 */
	public static Map<String, String> object2Map(Object object) throws JidokaException {

		Map<String, String> map = new HashMap<>();

		try {

			List<Field> fields = AnnotationUtil.getFieldsWithAnnotation(object.getClass(), AItemField.class);

			for (Field field : fields) {

				AItemField annotation = field.getAnnotation(AItemField.class);

				Object value = AnnotationUtil.getFieldValue(object, field);

				map.put(annotation.fieldName(), ConversionUtils.objectToJackson(value));

			}
		} catch (Exception e) {
			throw new JidokaException("Error mapping object to map", e);
		}

		return map;
	}


}
