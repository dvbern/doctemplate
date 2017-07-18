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
import java.util.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementiert eine MergeSource, die in if-Statements mit fixen Stringwerten vergleichen kann.
 * Vgl. dazu den Unit-Test
 */
public class ExtendedBeanMergeSource extends BeanMergeSource {

	private static final String EQUALS_PATTERN = "_EQ_";
	private static final String NOT_EQUALS_PATTERN = "_NEQ_";

	/**
	 * Konstruktor.
	 *
	 * @param bean POJO aus dem die Informationen gelesen werden sollen.
	 */
	public ExtendedBeanMergeSource(Object bean) {

		super(bean);
	}

	/**
	 * Konstruktor mit Angabe eines alternativen Key-Prefixes.
	 *
	 * @param bean POJO aus dem die Informationen gelesen werden sollen.
	 * @param alternativePrefix alternativer Key-Praefix, auf den die MergeSource anspricht
	 */
	public ExtendedBeanMergeSource(Object bean, String alternativePrefix) {

		super(bean, alternativePrefix);
	}

	/**
	 * @return identisch aber mit leerem Prefix
	 */
	public ExtendedBeanMergeSource cloneWithEmptyPrefix() {

		return new ExtendedBeanMergeSource(this.bean, "");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see ch.dvbern.lib.doctemplate.common.MergeSource#getData(ch.dvbern.lib.doctemplate. common.MergeContext,
	 * java.lang.String)
	 */
	@Override
	public Object getData(MergeContext ctx, String key) throws DocTemplateException {

		return super.getData(ctx, key);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see ch.dvbern.lib.doctemplate.common.MergeSource#ifStatement(ch.dvbern.lib.doctemplate. common.MergeContext,
	 * java.lang.String)
	 */
	@Override
	public Boolean ifStatement(MergeContext ctx, String key) throws DocTemplateException {

		int equalsPosition = key.indexOf(EQUALS_PATTERN);
		if (equalsPosition > 0) {
			String equalsExpression = key.substring(equalsPosition + EQUALS_PATTERN.length());
			Object result = ctx.getCurrentMergeSource().getData(ctx, key.substring(0, equalsPosition));
			return result == null ? false : equalsExpression.equals(result.toString());
		}
		int notEqualsPosition = key.indexOf(NOT_EQUALS_PATTERN);
		if (notEqualsPosition > 0) {
			String equalsExpression = key.substring(notEqualsPosition + NOT_EQUALS_PATTERN.length());
			Object result = ctx.getCurrentMergeSource().getData(ctx, key.substring(0, notEqualsPosition));
			return result == null ? true : !equalsExpression.equals(result.toString());
		}
		return super.ifStatement(ctx, key);
	}

	private String getFirstPart(String s) {
		int p = s.indexOf('.');
		return p >= 0 ? s.substring(0, p) : s;
	}

	private String cutFirstPart(String s) {
		int p = s.indexOf('.');
		return p >= 0 ? s.substring(p + 1) : s;
	}

}