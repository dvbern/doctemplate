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
package ch.dvbern.lib.doctemplate.common;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementiert eine MergeSource, die mit Java Reflection Informationen aus POJOs lesen kann
 */
public class BeanMergeSource implements MergeSource {

	private static final String BOOLEAN_NEGATION = "_NOT";
	private static final String BEAN_REFLECTION_PREFIX = "BRX_";

	private static final Log log = LogFactory.getLog(BeanMergeSource.class);

	protected final Object bean;

	private final String alternativePrefix;

	private static final Map<Class<?>, Map<String, Method>> beanAccessMethodCache = new HashMap<>();

	/**
	 * Konstruktor.
	 *
	 * @param bean POJO aus dem die Informationen gelesen werden sollen.
	 */
	public BeanMergeSource(Object bean) {

		this(bean, null);
	}

	/**
	 * Konstruktor mit Angabe eines alternativen Key-Prefixes.
	 *
	 * @param bean POJO aus dem die Informationen gelesen werden sollen.
	 * @param alternativePrefix alternativer Key-Praefix, auf den die MergeSource anspricht
	 */
	public BeanMergeSource(Object bean, String alternativePrefix) {

		this.bean = bean;
		this.alternativePrefix = alternativePrefix != null ? alternativePrefix.toUpperCase() : null;
	}

	/**
	 * @return identisch aber mit leerem Prefix
	 */
	public BeanMergeSource cloneWithEmptyPrefix() {

		return new BeanMergeSource(this.bean, "");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see ch.dvbern.lib.doctemplate.common.MergeSource#getData(ch.dvbern.lib.doctemplate. common.MergeContext,
	 * java.lang.String)
	 */
	@Override
	public Object getData(MergeContext ctx, String key) throws DocTemplateException {

		String fieldName = null;
		if (this.alternativePrefix != null && key.toUpperCase().startsWith(this.alternativePrefix)) {
			fieldName = key.substring(this.alternativePrefix.length());
		} else if (key.toUpperCase().startsWith(BEAN_REFLECTION_PREFIX)) {
			fieldName = key.substring(BEAN_REFLECTION_PREFIX.length());
		} else {
			return null;
		}
		try {
			return introspect(this.bean, fieldName);
		} catch (Exception e) {
			log.warn("getData", e);
			return e.getMessage();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see ch.dvbern.lib.doctemplate.common.MergeSource#ifStatement(ch.dvbern.lib.doctemplate. common.MergeContext,
	 * java.lang.String)
	 */
	@Override
	public Boolean ifStatement(MergeContext ctx, String key) throws DocTemplateException {

		boolean negate = key.endsWith(BOOLEAN_NEGATION);
		if (negate) {
			key = key.substring(0, key.length() - BOOLEAN_NEGATION.length());
		}
		Boolean result = null;
		Object o = getData(ctx, key);
		if (o == null) {
			return null;
		} else if (o instanceof Boolean) {
			result = (Boolean) o;
		} else if (o instanceof Collection) {
			@SuppressWarnings("rawtypes")
			Collection c = (Collection) o;
			result = c.size() > 0 ? Boolean.TRUE : Boolean.FALSE;
		} else if (o instanceof Number) {
			Number n = (Number) o;
			result = n.floatValue() != 0. ? Boolean.TRUE : Boolean.FALSE;
		} else {
			String s = o.toString();
			result = s != null && s.length() > 0 ? Boolean.TRUE : Boolean.FALSE;
		}
		if (negate) {
			result = Boolean.TRUE.equals(result) ? Boolean.FALSE : Boolean.TRUE;
		}
		return result;
	}

	private Object introspect(Object o, String fieldName) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {

		if (o == null) {
			return "";
		}
		int pPos = fieldName.indexOf(".");
		String s = pPos > 0 ? fieldName.substring(0, pPos) : fieldName;
		Method m = getAccessMethod(o, s);
		if (m == null) {
			return null;
		}
		Object result = m.invoke(o);
		if (pPos > 0) {
			return introspect(result, fieldName.substring(pPos + 1));
		}
		if (result == null) {
			return "";
		}
		return result;
	}

	private static Method getAccessMethod(Object o, String name) {

		Map<String, Method> methods = beanAccessMethodCache.get(o.getClass());
		if (methods == null) {
			methods = new HashMap<>();
			try {
				BeanInfo beanInfo = Introspector.getBeanInfo(o.getClass());
				PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
				for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
					Method readMethod = propertyDescriptor.getReadMethod();
					if (readMethod != null) {
						methods.put(propertyDescriptor.getName().toLowerCase(), readMethod);
					}
				}
			} catch (IntrospectionException e) {
				log.warn("Introspection Exception", e);
			}
			synchronized (beanAccessMethodCache) {
				beanAccessMethodCache.put(o.getClass(), methods);
			}
		}
		return methods.get(name.toLowerCase());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see ch.dvbern.lib.doctemplate.common.MergeSource#whileStatement(ch.dvbern.lib. doctemplate.common.MergeContext,
	 * java.lang.String)
	 */
	@Override
	public List<MergeSource> whileStatement(MergeContext ctx, String key) throws DocTemplateException {

		String fieldName = null;
		if (this.alternativePrefix != null && key.toUpperCase().startsWith(this.alternativePrefix)) {
			fieldName = key.substring(this.alternativePrefix.length());
		} else if (key.toUpperCase().startsWith(BEAN_REFLECTION_PREFIX)) {
			fieldName = key.substring(BEAN_REFLECTION_PREFIX.length());
		} else {
			return null;
		}
		try {
			Object o = introspect(this.bean, fieldName);
			if (o == null) {
				return new LinkedList<>();
			} else if (o instanceof Iterable) {
				List<MergeSource> result = new LinkedList<>();
				for (Object entry : (Iterable<?>) o) {
					result.add(new BeanMergeSource(entry, key + "."));
				}
				return result;
			} else {
				log.warn(o + " is not instance of Iterable");
				return new LinkedList<>();
			}
		} catch (Exception e) {
			log.warn("whileStatement", e);
			return new LinkedList<>();
		}
	}
}