package com.appian.rpa.snippets.queuemanager.utils.conversion;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.appian.rpa.snippets.queuemanager.annotations.AItemField;
import com.appian.rpa.snippets.queuemanager.annotations.AItemFile;
import com.appian.rpa.snippets.queuemanager.annotations.AItemKey;
import com.appian.rpa.snippets.queuemanager.utils.annotations.AnnotationUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.novayre.jidoka.client.api.JidokaFactory;
import com.novayre.jidoka.client.api.exceptions.JidokaFatalException;
import com.novayre.jidoka.client.api.queue.IQueueItem;

public class ConversionUtils {

	/** Constant with items files folder name */
	private static final String ITEMS_FILES_FOLDER = "itemsFiles";

	/** Items files folder name */
	private static File itemsFilesFolder;

	/** Private constructor */
	private ConversionUtils() {
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
	 */
	@SuppressWarnings("deprecation")
	public static <T> T map2Object(Map<String, String> map, Class<T> clazz) {

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
			throw new JidokaFatalException("Error mapping map to object", e);
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
	 */
	public static Map<String, String> object2Map(Object object) {

		Map<String, String> map = new HashMap<>();

		try {

			List<Field> fields = AnnotationUtils.getFieldsWithAnnotation(object.getClass(), AItemField.class);

			for (Field field : fields) {

				AItemField annotation = field.getAnnotation(AItemField.class);

				Object value = AnnotationUtils.getFieldValue(object, field);

				map.put(annotation.fieldName(), objectToJackson(value));

			}
		} catch (Exception e) {
			throw new JidokaFatalException("Error mapping object to map", e);
		}

		return map;
	}

	/**
	 * Object to Jackson.
	 *
	 * @param object The object to be mapped
	 * @return String with Jackson object
	 * 
	 */
	public static String objectToJackson(Object object) {

		try {
			ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
					.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

			ObjectWriter ow = mapper.writer();

			return ow.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			throw new JidokaFatalException("Error converting object to jackson", e);
		}
	}

	/**
	 * Jackson to object.
	 *
	 * @param <T>     Returned object type
	 * @param jackson Jackson to be mapped
	 * @param clazz   Mapper class
	 * @return The mapped object
	 * 
	 */
	public static <T> T jacksonToObject(String jackson, Class<T> clazz) {

		ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
				.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

		ObjectReader or = mapper.readerFor(clazz);

		T readValue;
		try {
			readValue = or.readValue(jackson);
		} catch (JsonProcessingException e) {
			try {
				readValue = or.readValue(objectToJackson(jackson));
			} catch (JsonProcessingException e1) {
				throw new JidokaFatalException("Error converting jackson to object", e);
			}
		}

		return readValue;
	}

	/**
	 * Sets the given {@code object} key with the given {@code keyValue}.
	 * 
	 * @param <T>      Generic type
	 * @param object   Object to set the key
	 * @param keyValue New key value
	 * @param clazz    Class of the model
	 * 
	 */
	public static <T> void setObjectKeyValue(T object, String keyValue, Class<?> clazz) {

		try {

			Field field = AnnotationUtils.getFirstFieldWithAnnotation(clazz, AItemKey.class);

			AnnotationUtils.setFieldValue(object, field.getName(), keyValue);

		} catch (Exception e) {
			throw new JidokaFatalException("Error mapping map to object", e);
		}

	}

	public static <T> void setObjectFiles(T object, IQueueItem currentQueueItem, Class<?> clazz) {
		try {

			List<Field> fields = AnnotationUtils.getFieldsWithAnnotation(clazz, AItemFile.class);

			Map<String, byte[]> filesMap = currentQueueItem.files();

			Map<String, String> functionalData = currentQueueItem.functionalData();

			Path folder = Paths.get(JidokaFactory.getServer().getCurrentDir(), ITEMS_FILES_FOLDER);

			itemsFilesFolder = folder.toFile();
			itemsFilesFolder.mkdirs();

			for (Field field : fields) {

				String fileName = functionalData.get(field.getName());

				byte[] fileContent = filesMap.get(fileName);

				File itemFile = Paths.get(itemsFilesFolder.getAbsolutePath(), fileName).toFile();

				OutputStream os = new FileOutputStream(itemFile);
				os.write(fileContent);
				os.close();

				if (Path.class.isAssignableFrom(field.getType())) {
					AnnotationUtils.setFieldValue(object, field.getName(), itemFile.toPath());

				} else if (File.class.isAssignableFrom(field.getType())) {
					AnnotationUtils.setFieldValue(object, field.getName(), itemFile);
				}
			}

		} catch (IOException e) {
			throw new JidokaFatalException("Error setting the object files", e);
		}
	}

}
