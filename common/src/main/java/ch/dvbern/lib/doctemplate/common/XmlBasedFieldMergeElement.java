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

import static ch.dvbern.lib.doctemplate.util.LdtConstants.FORMAT_SUFFIX;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ch.dvbern.lib.doctemplate.util.FormatHelper;
import ch.dvbern.lib.doctemplate.util.ImageHandler;
import ch.dvbern.lib.doctemplate.util.LdtConstants;

/**
 * @author lsimon
 */
public class XmlBasedFieldMergeElement<T extends Image> extends BasicMergeElement {

	private static final Log log = LogFactory.getLog(XmlBasedFieldMergeElement.class);
	private final String key;
	private final String name;
	private Map<String, T> images = null;
	private ImageHandler<T> imageHandler = null;

	/**
	 * Konstruktor eines MergeElements, dessen Ausgabe dynamisch ermittelt wird
	 *
	 * @param name
	 * @param key Identifikation des Inhaltes, der fuer die Ausgabe ermittelt werden soll
	 * @param images
	 * @param imageHandler
	 */
	public XmlBasedFieldMergeElement(String name, String key, Map<String, T> images, ImageHandler<T> imageHandler) {

		this.key = key;
		this.name = name;
		this.images = images;
		this.imageHandler = imageHandler;
	}

	/**
	 * @see ch.dvbern.lib.doctemplate.common.BasicMergeElement#getContent(ch.dvbern.lib.doctemplate.common.MergeContext,
	 *      ch.dvbern.lib.doctemplate.common.MergeSource, java.io.OutputStream)
	 */
	@Override
	public void getContent(MergeContext ctx, MergeSource mergeSource, OutputStream output) throws DocTemplateException {

		// Format-Suffix aus key extrahieren
		String keyWithoutFormatSuffix = this.key, formatSuffix = null;
		int i = this.key.indexOf(FORMAT_SUFFIX);
		if (i > 0) {
			formatSuffix = this.key.substring(i + FORMAT_SUFFIX.length());
			keyWithoutFormatSuffix = this.key.substring(0, i);
		}

		Object data = mergeSource.getData(ctx, keyWithoutFormatSuffix);
		if (data instanceof Image) {
			addImage((T) data, formatSuffix, output);
		} else if (data != null) {
			String dataAsString = FormatHelper.getDataAsString(data, StringUtils.isEmpty(formatSuffix) ? getDefaultFormatter(data) : formatSuffix);
			dataAsString = StringEscapeUtils.escapeXml(dataAsString);
			writeText(output, dataAsString);
		} else {
			log.warn(this.name + ": no template source with key " + this.key);
		}
	}

	/**
	 * @param data
	 * @return
	 */
	protected String getDefaultFormatter(Object data) {

		String formatter = null;
		if (data == null) {
			return formatter;
		}
		if (data instanceof Number) {
			if (data instanceof Float || data instanceof Double || data instanceof BigDecimal) {
				formatter = getDefaultFloatFormat();
			} else {
				formatter = getDefaultIntFormat();
			}
		} else if (data instanceof Date) {
			formatter = getDefaultDateFormat();
		}
		return formatter;
	}

	/**
	 * @return
	 */
	protected String getDefaultDateFormat() {

		return LdtConstants.DEFAULT_DATE_FORMAT;
	}

	/**
	 * @return
	 */
	protected String getDefaultIntFormat() {

		return LdtConstants.DEFAULT_INT_FORMAT;
	}

	/**
	 * @return
	 */
	protected String getDefaultFloatFormat() {

		return LdtConstants.DEFAULT_FLOAT_FORMAT;
	}

	/**
	 * @param output
	 * @param dataAsString
	 * @throws DocTemplateException
	 */
	protected void writeText(OutputStream output, String dataAsString) throws DocTemplateException {

		try {
			output.write(dataAsString.getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw new DocTemplateException(e);
		}
	}

	private void addImage(T image, String formatSuffix, OutputStream output) throws DocTemplateException {

		if (this.imageHandler != null) {
			boolean doublette = true;
			if (!(doublette = this.images.containsValue(image))) {
				this.images.put(this.imageHandler.addImage(image, formatSuffix, output, this.images.size(), doublette), image);
				return;
			}
			int counter = 0;
			for (T imItem : this.images.values()) {
				if (imItem.equals(image)) {
					this.imageHandler.addImage(imItem, formatSuffix, output, counter, doublette);
					break;
				}
				counter++;
			}
		}
	}
}
