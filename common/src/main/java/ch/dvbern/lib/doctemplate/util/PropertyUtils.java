/*
 * Copyright 2017 DV Bern AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * limitations under the License.
 */

package ch.dvbern.lib.doctemplate.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.StringTokenizer;

public class PropertyUtils {

	public static Method getReadMethod(Object object, String property)
			throws IntrospectionException {
		return getPropertyDescriptor(object, property).getReadMethod();
	}

	public static Method getWriteMethod(Object object, String property)
			throws IntrospectionException {
		return getPropertyDescriptor(object, property).getWriteMethod();
	}

	public static Object getNestedProperty(Object bean, String nestedProperty)
			throws IllegalArgumentException, SecurityException, IllegalAccessException,
			InvocationTargetException, IntrospectionException, NoSuchMethodException {
		Object object = null;
		StringTokenizer st = new StringTokenizer(nestedProperty, ".", false);
		while (st.hasMoreElements() && bean != null) {
			String nam = (String) st.nextElement();
			if (st.hasMoreElements()) {
				bean = getProperty(bean, nam);
			}
			else {
				object = getProperty(bean, nam);
			}
		}
		return object;
	}

	public static Class<?> getNestedPropertyType(Class<?> clazz, String nestedProperty)
			throws IllegalArgumentException, SecurityException, IntrospectionException,
			NoSuchMethodException {
		Class<?> propertyType = null;
		StringTokenizer st = new StringTokenizer(nestedProperty, ".", false);
		while (st.hasMoreElements() && clazz != null) {
			String nam = (String) st.nextElement();
			Method readMethod = getReadMethod(clazz, nam);
			if (readMethod != null) {
				if (st.hasMoreElements()) {
					clazz = readMethod.getReturnType();
				}
				else {
					propertyType = readMethod.getReturnType();
				}
			}
			else {
				while (st.hasMoreElements()) {
					st.nextElement(); // use remaining tokens
				}
			}
		}
		return propertyType;
	}

	public static Object getProperty(Object bean, String property)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException, SecurityException, IntrospectionException,
			NoSuchMethodException {
		Object object = null;
		Method getter = getReadMethod(bean.getClass(), property);
		StringTokenizer st = new StringTokenizer(property, "[](,)", false);
		capitalize(st.nextToken());
		Object params[] = new Object[st.countTokens()];
		for (int j = 0; j < params.length; j++) {
			params[j] = st.nextToken();
			if (getter.getParameterTypes()[0] == int.class) {
				params[j] = new Integer((String) params[j]);
			}
		}
		object = getter == null ? null : getter.invoke(bean, params);
		return object;
	}

	public static PropertyDescriptor getPropertyDescriptor(Object object, String property)
			throws IntrospectionException {
		return getPropertyDescriptor(object.getClass(), property);
	}

	protected final static Hashtable<Class<?>, Hashtable<String, PropertyDescriptor>> propertyDescriptorss = new Hashtable<>();

	public static PropertyDescriptor getPropertyDescriptor(Class<?> clazz,
			String property) throws IntrospectionException {
		Hashtable<String, PropertyDescriptor> propertyDescriptors = propertyDescriptorss
				.get(clazz);
		if (propertyDescriptors == null) {
			BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
			if (beanInfo != null) {
				propertyDescriptors = new Hashtable<>();
				PropertyDescriptor[] propertyDescriptorArray = beanInfo
						.getPropertyDescriptors();
				for (PropertyDescriptor descriptor : propertyDescriptorArray) {
					propertyDescriptors.put(descriptor.getName(), descriptor);
				}
				propertyDescriptorss.put(clazz, propertyDescriptors);
			}
		}
		return propertyDescriptors == null ? null : propertyDescriptors.get(property);
	}

	public static Method getReadMethod(Class<?> clazz, String property)
			throws IntrospectionException, SecurityException, NoSuchMethodException {
		Method readMethod = null;
		if (property.indexOf('(') >= 0) {
			StringTokenizer st = new StringTokenizer(property, "(,)", false);
			String p = capitalize(st.nextToken());
			Class<?> types[] = new Class[st.countTokens()];
			for (int j = 0; j < types.length; j++) {
				types[j] = String.class;
			}
			try {
				readMethod = clazz.getMethod("is" + p, types);
			}
			catch (Exception e) {/**/
			}
			if (readMethod == null) {
				readMethod = clazz.getMethod("get" + p, types);
			}
		}
		else if (property.indexOf('[') >= 0) {
			StringTokenizer st = new StringTokenizer(property, "[,]", false);
			String p = capitalize(st.nextToken());
			Class<?> types[] = new Class[st.countTokens()];
			for (int j = 0; j < types.length; j++) {
				types[j] = int.class;
			}
			try {
				readMethod = clazz.getMethod("is" + p, types);
			}
			catch (Exception e) {/**/
			}
			if (readMethod == null) {
				readMethod = clazz.getMethod("get" + p, types);
			}
		}
		else {
			PropertyDescriptor propertyDescriptor = getPropertyDescriptor(clazz,
					property);
			readMethod = propertyDescriptor == null ? null
					: propertyDescriptor.getReadMethod();
		}
		return readMethod;
	}

	static String capitalize(String s) {
		if (s.length() == 0) {
			return s;
		}
		char chars[] = s.toCharArray();
		chars[0] = Character.toUpperCase(chars[0]);
		return new String(chars);
	}

}
