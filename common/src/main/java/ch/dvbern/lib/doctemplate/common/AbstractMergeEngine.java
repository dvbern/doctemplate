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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import ch.dvbern.lib.doctemplate.util.ImageHandler;

/**
 * @author lsimon
 */
public abstract class AbstractMergeEngine<T extends Image> {

	private static final Log log = LogFactory.getLog(AbstractMergeEngine.class);

	protected Stack<BasicMergeElement> parseStack;
	protected Map<String, String> keyTranslationTable = null;
	protected Map<String, T> images = new LinkedHashMap<>();
	protected static final String CONDITION = "IF_";
	protected static final String ITERATION = "WHILE_";
	protected static final String CONDITION_END = "ENDIF_";
	protected static final String ITERATION_END = "ENDWHILE_";
	protected static final String SORTFIELD_PREFIX = "SORT_";
	private String fieldPrefix = "FIELD_";
	protected static final String INTERNAL_BOOKMARK_TAG = "doc-template-bookmark";
	protected static final String INTERNAL_BOOKMARK_XML_START = "<"
			+ INTERNAL_BOOKMARK_TAG + ">";
	protected static final String INTERNAL_BOOKMARK_XML_END = "</" + INTERNAL_BOOKMARK_TAG
			+ ">";

	protected String name;

	private static final Log LOG = LogFactory.getLog(AbstractMergeEngine.class);
	/**
	 * @param name
	 */
	public AbstractMergeEngine(String name) {
		this.name = name;
		initKeyTranslationTable();
	}

	public String getFieldPrefix() {
		return fieldPrefix;
	}

	public void setFieldPrefix(String fieldPrefix) {
		this.fieldPrefix = fieldPrefix;
	}

	private void initKeyTranslationTable() {
		this.keyTranslationTable = new HashMap<>();
		this.keyTranslationTable.put("_FMT0DP", "_FMT#,##0");
		this.keyTranslationTable.put("_FMT1DP", "_FMT#,##0.0");
		this.keyTranslationTable.put("_FMT2DP", "_FMT#,##0.00");
	}

	/**
	 * @param mergeSource
	 * @param input
	 * @param output
	 * @throws DocTemplateException
	 */
	protected void mergeContent(MergeSource mergeSource, InputStream input,
			OutputStream output) throws DocTemplateException {

		try (InputStreamRemainingOpen is = new InputStreamRemainingOpen(input)) {
			// XML Verarbeitung
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(is);
			Document result = builder.newDocument();
			result.setXmlStandalone(true);
			preProcess(result, doc, result);
			StringWriter writer = new StringWriter();
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.transform(new DOMSource(result), new StreamResult(writer));
			// Struktur parsen
			BasicMergeElement bme = parseInit();

			parseTemplate(new StringBuffer(writer.toString()));
			if (this.parseStack.size() > 1) {
				// Debug-Info about the failing element
				BasicMergeElement lastFailing = this.parseStack.peek();
				String errorMessage = "last failing tag is " + lastFailing.toString();
				LOG.error("There is an unmatched tag on the stack. The template has an invalid structure: " + errorMessage);
				throw new DocTemplateException("error.template.invalid.structure", errorMessage);
			}
			// Ergebnis erstellen
			bme.getContent(new MergeContext(mergeSource), mergeSource, output);
		}
		catch (DocTemplateException sfe) {
			throw sfe;
		}
		catch (IOException | SAXException | ParserConfigurationException
				| TransformerException | TransformerFactoryConfigurationError e) {
			throw new DocTemplateException(e);
		}
	}

	/**
	 * @param sb
	 * @throws DocTemplateException
	 */
	protected void parseTemplate(StringBuffer sb) throws DocTemplateException {

		int pos = getInternalBookmarkStart(sb);
		while (pos >= 0) {
			this.parseStack.peek().addMergeElement(new StaticMergeElement(
					getStaticElementContent(sb.substring(0, pos))));
			sb.delete(0, pos + getInternalBookmarkStartLength());
			int end = getInternalBookmarkEnd(sb);
			if (end < 0) {
				throw new DocTemplateException("error.template.invalid.structure");
			}
			String key = sb.substring(0, end);
			key = StringEscapeUtils.unescapeXml(key);
			if (key.startsWith(fieldPrefix)) {
				key = key.substring(fieldPrefix.length());
				XmlBasedFieldMergeElement<?> fme = getFieldMergeElement(translate(key),
						getImageHandler());
				this.parseStack.peek().addMergeElement(fme);
			}
			else if (key.startsWith(CONDITION)) {
				key = key.substring(CONDITION.length());
				ConditionMergeElement cme = new ConditionMergeElement(this.name,
						translate(key));
				this.parseStack.peek().addMergeElement(cme);
				this.parseStack.push(cme);
			}
			else if (key.startsWith(ITERATION)) {
				key = key.substring(ITERATION.length());
				IterationMergeElement ime = new IterationMergeElement(this.name,
						translate(key));
				this.parseStack.peek().addMergeElement(ime);
				this.parseStack.push(ime);
			}
			else if (key.startsWith(SORTFIELD_PREFIX)) {
				key = key.substring(SORTFIELD_PREFIX.length());
				Object ime = this.parseStack.peek();
				if (ime instanceof IterationMergeElement) {
					((IterationMergeElement) ime).addSortFieldKey(translate(key));
				}
				else {
					log.warn(
							"invalid structure: no IterationMergeElement on parse stack");
				}
			}
			else if (key.startsWith(CONDITION_END) || key.startsWith(ITERATION_END)) {
				if (this.parseStack.size() > 1) {
					this.parseStack.pop();
				}
				else {
					LOG.error("Encountered an ending if/whilte tag but there is no such open tag left on the stack " + key );
					throw new DocTemplateException("error.template.invalid.structure");
				}
			}
			else {
				throw new DocTemplateException("invalid merge command key: " + key);
			}
			sb.delete(0, end + getInternalBookmarkEndLength());
			pos = getInternalBookmarkStart(sb);
		}
		StaticMergeElement sme = new StaticMergeElement(sb.toString());
		this.parseStack.peek().addMergeElement(sme);
	}

	/**
	 * @param sb Dokumentvorlage als String
	 * @return position des aus dem Preprocessing eingefuegten Start-Tags
	 */
	protected int getInternalBookmarkStart(final StringBuffer sb) {
		return sb.indexOf(INTERNAL_BOOKMARK_XML_START);
	}

	/**
	 * @return Laenge des aus dem Preprocessing eingefuegten Start-Tags
	 */
	protected int getInternalBookmarkStartLength() {
		return INTERNAL_BOOKMARK_XML_START.length();
	}

	/**
	 * @param sb Dokumentvorlage als String
	 * @return position des aus dem Preprocessing eingefuegten End-Tags
	 */
	protected int getInternalBookmarkEnd(final StringBuffer sb) {
		return sb.indexOf(INTERNAL_BOOKMARK_XML_END);
	}

	/**
	 * @return Laenge des aus dem Preprocessing eingefuegten End-Tags
	 */
	protected int getInternalBookmarkEndLength() {
		return INTERNAL_BOOKMARK_XML_END.length();
	}

	/**
	 * @return
	 */
	public abstract ImageHandler<T> getImageHandler();

	protected String getStaticElementContent(String value) {
		return value;
	}

	/**
	 * @param key
	 * @param imageHandler
	 * @return
	 */
	protected XmlBasedFieldMergeElement<T> getFieldMergeElement(String key,
			ImageHandler<T> imageHandler) {
		return new XmlBasedFieldMergeElement<>(this.name, key, this.images, imageHandler);
	}

	/**
	 * @param doc
	 * @param src
	 * @param dest
	 * @throws DocTemplateException
	 */
	protected abstract void preProcess(Document doc, Node src, Node dest)
			throws DocTemplateException;

	/**
	 * @return
	 */
	protected BasicMergeElement parseInit() {

		BasicMergeElement bme = new BasicMergeElement();
		this.parseStack = new Stack<>();
		this.parseStack.push(bme);
		return bme;
	}

	/**
	 * @param key
	 * @return
	 */
	protected String translate(final String key) {

		String result = key;
		if (this.keyTranslationTable != null) {
			for (Map.Entry<String, String> entry : this.keyTranslationTable.entrySet()) {
				result = result.replace(entry.getKey(), entry.getValue());
			}
		}
		return result;
	}

}
