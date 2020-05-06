package com.appian.rpa.snippets.commons.excel.annotations.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import com.novayre.jidoka.client.api.exceptions.JidokaException;

/**
 * ReflectionUtil class.
 */
public final class ReflectionUtil {

	/**
	 * Bean "get" method prefixes
	 */
	public static final String[] GETTERS_PREFIXES = new String[] { null, "get", "is" };

	/**
	 * Bean "set" method prefixes
	 */
	public static final String[] SETTERS_PREFIXES = new String[] { null, "set" };

	/**
	 * Private constructor
	 */
	private ReflectionUtil() {
	}

	/**
	 * Invokes the method of a bean
	 * 
	 * @param object          Bean object
	 * @param name            Method name
	 * @param throwExceptions Throw exceptions?
	 * @param prefixes        Bean method prefixes
	 * @param argumentsType   Method arguments type
	 * @param arguments       Method arguments
	 * @return
	 * @throws JidokaException
	 */
	public static Object invokeBeanMethod(Object object, String name, boolean throwExceptions, String[] prefixes,
			Class<?>[] argumentsType, Object[] arguments) throws JidokaException {

		Method m = getBeanMethod(object, name, prefixes, argumentsType);

		if (m == null) {
			return null;
		}

		try {
			return m.invoke(object, arguments);
		} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {

			if (throwExceptions) {
				throw new JidokaException("Error invoking the bean method", e);
			}

			return null;

		}
	}

	/**
	 * Returns the method of a bean
	 * 
	 * @param object    Bean object
	 * @param name      Method name
	 * @param prefixes  Bean prefixes
	 * @param arguments Method arguments
	 * @return The bean method
	 */
	public static Method getBeanMethod(Object object, String name, String[] prefixes, Class<?>[] arguments) {

		String[] methods = name.split("\\Q.\\E");

		if (methods.length == 1) {
			return internalGetBeanMethod(object, name, prefixes, arguments);
		}

		Object obj = object;

		for (int i = 0; i < methods.length - 1; i++) {

			obj = getBeanObject(obj, methods[i], prefixes, new Class<?>[] {});

			if (obj == null) {
				return null;
			}
		}

		return internalGetBeanMethod(obj, methods[methods.length - 1], prefixes, arguments);
	}

	/**
	 * Returns the method of a bean
	 * 
	 * @param object    Bean object
	 * @param name      Method name
	 * @param prefixes  Bean prefixes
	 * @param arguments Method arguments
	 * @return Given bean {@code objeto} method
	 */
	private static Method internalGetBeanMethod(Object object, String name, String[] prefixes, Class<?>[] arguments) {

		String m;

		for (int i = 0; i < prefixes.length; i++) {

			if (prefixes[i] == null) {
				m = name;
			} else {
				m = prefixes[i] + name.toUpperCase().charAt(0) + name.substring(1);
			}

			try {
				return object.getClass().getMethod(m, arguments);
			} catch (SecurityException | NoSuchMethodException e) {
				// continue
			}
		}

		return null;
	}

	/**
	 * Returns a bean object
	 * 
	 * @param object    Bean object
	 * @param name      Method name
	 * @param prefixes  Bean prefixes
	 * @param arguments Method arguments
	 * @return The bean object
	 */
	public static Object getBeanObject(Object object, String name, String[] prefixes, Class<?>[] arguments) {

		if (object == null) {
			return null;
		}

		String m;

		for (int i = 0; i < prefixes.length; i++) {

			if (prefixes[i] == null) {
				m = name;
			} else {
				m = prefixes[i] + name.toUpperCase().charAt(0) + name.substring(1);
			}

			try {
				Method method = object.getClass().getMethod(m, arguments);
				return method.invoke(object, (Object[]) arguments);
			} catch (Exception e) {
				// continue;
			}
		}

		return null;
	}

	/**
	 * Returns a bean object instance
	 * 
	 * @param object    Bean object
	 * @param name      Method name
	 * @param prefixes  Bean prefixes
	 * @param arguments Method arguments
	 * @return The bean object instance
	 * @throws {@link JidokaException}
	 */
	@SuppressWarnings("deprecation")
	private static Object getInstanceBeanObject(Object object, String name, String[] prefixes, Class<?>[] arguments)
			throws JidokaException {

		try {

			Method m = getBeanMethod(object, name, prefixes, arguments);

			if (m == null) {
				throw new NullPointerException(
						"Getter not found for expression [" + name + "] on object [" + object + "]");
			}

			Object r;

			r = m.invoke(object, new Object[0]);

			if (r == null) {

				Class<?> returnType = m.getReturnType();

				if (returnType.isInterface()) {
					// return type is an interface, we need instance a class
					returnType = getInstanceTypeForInterface(returnType);
				}

				// es una propiedad con valor null la instanciamos
				r = returnType.newInstance();

				// y la incluimos en el objeto contenedor
				m = getBeanMethod(object, name, SETTERS_PREFIXES, new Class<?>[] { m.getReturnType() });

				m.invoke(object, r);
			}

			return r;

		} catch (Exception e) {
			throw new JidokaException("Error getting the instance object");
		}

	}

	/**
	 * Returns a class type for the interface {@code interfaceType} specified
	 * 
	 * @param interfaceType Interface class
	 * @return The interface instance type
	 * @throws {@link JidokaException}
	 */
	private static Class<?> getInstanceTypeForInterface(Class<?> interfaceType) throws JidokaException {

		if (interfaceType.isAssignableFrom(List.class)) {
			// list type, will instance a good type
			return ArrayList.class; // NOPMD
		}

		if (interfaceType.isAssignableFrom(Map.class)) {
			// map type, will instance a good type
			return HashMap.class; // NOPMD
		}

		throw new JidokaException("Cannot instance an object of interface type " + interfaceType);
	}

	/**
	 * Sets the value of a property
	 * 
	 * @param bean  Bean object
	 * @param name  Property name
	 * @param value Property new value
	 * @throws {@link JidokaException}
	 */
	public static void setPropertyBean(Object bean, String name, Object value) throws JidokaException {
		try {
			BeanUtils.setProperty(bean, name, value);
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new JidokaException(e);
		}
	}

	/**
	 * Returns the property value for the property specified. <br/>
	 * This method supports () and [] notation to Map and List types
	 * 
	 * @param bean     Bean object
	 * @param property Bean property
	 * @return The given property value
	 * @throws {@link JidokaException}
	 */
	private static Object getPropertyValue(Object bean, String property) throws JidokaException {

		String[] index = property.split("[\\(\\)]");

		if (index.length > 1) {
			// map access
			return getItemsMapValue(bean, index[0], index[1]);
		}

		index = property.split("[\\[\\]]");

		if (index.length > 1) {
			// list access
			return getItemsListValue(bean, index[0], Integer.parseInt(index[1]));
		}

		return getInstanceBeanObject(bean, property, GETTERS_PREFIXES, new Class<?>[0]);
	}

	/**
	 * Returns the items list from the specified index
	 * 
	 * @param bean     Bean object
	 * @param property Bean property
	 * @param index
	 * @return Item list
	 * @throws {@link JidokaException}
	 */
	@SuppressWarnings("deprecation")
	private static Object getItemsListValue(Object bean, String property, int index) throws JidokaException {

		@SuppressWarnings("unchecked")
		List<Object> list = (List<Object>) getPropertyValue(bean, property);

		if (list.size() > index) {
			return list.get(index);
		}

		ParameterizedType type = (ParameterizedType) getBeanMethod(bean, property, GETTERS_PREFIXES, new Class<?>[0])
				.getGenericReturnType();

		Object item;

		try {
			item = ((Class<?>) type.getActualTypeArguments()[0]).newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new JidokaException("Error getting the items list value");
		}

		list.add(index, item);

		return item;
	}

	/**
	 * Returns the items map for the specified key
	 * 
	 * @param bean     Bean object
	 * @param property Bean property
	 * @param key      Item map key
	 * @return The item object
	 * @throws {@link JidokaException}
	 */
	@SuppressWarnings("deprecation")
	private static Object getItemsMapValue(Object bean, String property, String key) throws JidokaException {

		@SuppressWarnings("unchecked")
		Map<Object, Object> map = (Map<Object, Object>) getPropertyValue(bean, property);

		if (map.containsKey(key)) {
			return map.get(key);
		}

		ParameterizedType type = (ParameterizedType) getBeanMethod(bean, property, GETTERS_PREFIXES, new Class<?>[0])
				.getGenericReturnType();

		Object item;

		try {
			// actual type 0 is the key type
			// actual type 1 is the value type
			item = ((Class<?>) type.getActualTypeArguments()[1]).newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new JidokaException("Error getting the items map value", e);
		}

		map.put(key, item);

		return item;
	}

}
