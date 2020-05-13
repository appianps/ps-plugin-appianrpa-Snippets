package com.appian.rpa.snippets.commons.queues.generic.conversion;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.appian.rpa.snippets.commons.queues.generic.annotations.AItemField;
import com.appian.rpa.snippets.commons.utils.annotations.AnnotationUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
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

			List<Field> fields = AnnotationUtils.getFieldsWithAnnotation(clazz, AItemField.class);

			for (Field field : fields) {

				AItemField annotation = field.getAnnotation(AItemField.class);

				Object contenido = jacksonToObject(map.get(annotation.fieldName()), field.getType());

				AnnotationUtils.setFieldValue(object, field.getName(), contenido);

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

			List<Field> fields = AnnotationUtils.getFieldsWithAnnotation(object.getClass(), AItemField.class);

			for (Field field : fields) {

				AItemField annotation = field.getAnnotation(AItemField.class);

				Object value = AnnotationUtils.getFieldValue(object, field);

				map.put(annotation.fieldName(), objectToJackson(value));

			}
		} catch (Exception e) {
			throw new JidokaException("Error mapping object to map", e);
		}

		return map;
	}

	/**
	 * Object to Jackson.
	 *
	 * @param object The object to be mapped
	 * @return String with Jackson object
	 * 
	 * @throws {@link JsonProcessingException}
	 */
	public static String objectToJackson(Object object) throws JsonProcessingException {

		ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
				.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

		ObjectWriter ow = mapper.writer();

		return ow.writeValueAsString(object);
	}

	/**
	 * Jackson to object.
	 *
	 * @param <T>     Returned object type
	 * @param jackson Jackson to be mapped
	 * @param clazz   Mapper class
	 * @return The mapped object
	 * 
	 * @throws JsonProcessingException
	 */
	public static <T> T jacksonToObject(String jackson, Class<T> clazz) throws JsonProcessingException {

		ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
				.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

		ObjectReader or = mapper.readerFor(clazz);

		T readValue;
		try {
			readValue = or.readValue(jackson);
		} catch (JsonProcessingException e) {
			readValue = or.readValue(objectToJackson(jackson));
		}

		return readValue;
	}

}
