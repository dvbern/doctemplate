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

package ch.dvbern.lib.doctemplate.xml;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.dvbern.lib.doctemplate.common.AbstractMergeEngine;
import ch.dvbern.lib.doctemplate.common.DocTemplateException;
import ch.dvbern.lib.doctemplate.common.Image;
import ch.dvbern.lib.doctemplate.common.MergeSource;
import ch.dvbern.lib.doctemplate.common.XmlBasedFieldMergeElement;
import ch.dvbern.lib.doctemplate.util.ImageHandler;
import ch.dvbern.lib.doctemplate.util.LdtConstants;

/**
 * Merged eine XML-Vorlage mit Informationen aus einer {@link MergeSource}, die mit darin enthaltener <field>-Elemente
 * gezielt referenziert werden.
 */
public class XmlMergeEngine extends AbstractMergeEngine<Image> {

	private static final Log log = LogFactory.getLog(XmlMergeEngine.class);

	private static final String XALAN_INDENTAMOUNT_PROPERTY = "{http://xml.apache.org/xslt}indent-amount";

	private static final String XML_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
	private static final String INTEGER_NUMBER_FORMAT = "###0";
	private static final String DECIMAL_NUMBER_FORMAT = "###0.0#_de_CH";
	private static final String NAMESPACE_URI = "http://www.dvbern.ch/lib/doctemplate/XmlMergeSchema";
	private static final String XML_FIELD = "FIELD";
	private static final String XML_FIELD_PATH = "PATH";
	private static final String XML_FIELD_FORMATTER = "formatter";
	private static final String CONDITION = "IF";
	protected static final String ITERATION = "WHILE";
	protected static final String SORT = "SORT";
	protected static final String ASC = "ASC";
	protected static final String DESC = "DESC";

	// ohne <>-Zeichen, damit XML Element-Attributen verwendbar
	protected static final String INTERNAL_BOOKMARK_XML_ATTR_START = INTERNAL_BOOKMARK_XML_START.replace('<', '[').replace('>', ']');
	protected static final String INTERNAL_BOOKMARK_XML_ATTR_END = INTERNAL_BOOKMARK_XML_END.replace('<', '[').replace('>', ']');

	private static final List<String> BLOCK_MARKERS = Arrays.<String> asList(new String[] { CONDITION, ITERATION });

	public XmlMergeEngine(String name, Map<String, String> keyTranslationTable) {
		this(name);
		this.keyTranslationTable.putAll(keyTranslationTable);
	}

	public XmlMergeEngine(String name) {
		super(name);
	}

	public byte[] getXml(MergeSource mergeSource, InputStream template) throws DocTemplateException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		mergeContent(mergeSource, template, baos);
		String s = new String(baos.toByteArray(), StandardCharsets.UTF_8);
		return formatXml(s).getBytes(StandardCharsets.UTF_8);
	}

	public String formatXml(String xml) throws DocTemplateException {

		try {
			Source xmlInput = new StreamSource(new StringReader(xml));
			StringWriter stringWriter = new StringWriter();
			StreamResult xmlOutput = new StreamResult(stringWriter);
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(XALAN_INDENTAMOUNT_PROPERTY, "2");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "true");
			transformer.transform(xmlInput, xmlOutput);
			return xmlOutput.getWriter().toString();
		} catch (Exception e) {
			throw new DocTemplateException(e); // simple exception handling,
			// please review it
		}
	}

	@Override
	protected String getStaticElementContent(String value) {

		return value.trim();
	}

	protected void preProcess(Document doc, Node src, Node dest) throws DocTemplateException {

		NodeList childElements = src.getChildNodes();
		for (int i = 0; i < childElements.getLength(); i++) {
			Node childElement = childElements.item(i);
			Node result = null;
			String nodeName = childElement.getNodeName();
			boolean field = nodeName.toUpperCase().endsWith(XML_FIELD);
			if (NAMESPACE_URI.equals(childElement.getNamespaceURI())) {
				String key = null;
				// In Fall des Fields key gleich mit dem "text:name" Eigenschaft
				if (field) {
					key = getValueOfAttribute(XML_FIELD_PATH, childElement);
					String formatter = getValueOfAttribute(XML_FIELD_FORMATTER, childElement);
					if (!StringUtils.isEmpty(formatter)) {
						String postFix = LdtConstants.FORMAT_SUFFIX + formatter;
						key = key + postFix;
						keyTranslationTable.put(postFix, postFix);
					}
				} else {
					for (String blockElement : BLOCK_MARKERS) {
						if (nodeName.toUpperCase().endsWith(blockElement)) {
							key = blockElement + "_" + childElement.getAttributes().item(0).getTextContent();
							break;
						}
					}
				}
				if (key != null) {
					result = doc.createElement(INTERNAL_BOOKMARK_TAG);
					result.setTextContent(field ? (getFieldPrefix() + key) : key);
					String sort = null;
					if ((sort = getValueOfAttribute(SORT, childElement)) != null) {
						if (!sort.equalsIgnoreCase(ASC) && !sort.equalsIgnoreCase(DESC)) {
							log.warn("Die Sortierung ist falsch: asc oder desc!");
						} else {
							dest.appendChild(result);
							result = doc.createElement(INTERNAL_BOOKMARK_TAG);
							String body = SORT.toUpperCase().concat("_").concat(getPfadOnly(key));
							if (sort.equalsIgnoreCase(DESC)) {
								body = body.concat("_").concat(sort.toUpperCase());
							}
							result.setTextContent(body);
						}
					}
					Node attr = childElement.getAttributes().getNamedItem("attribute");
					if (attr != null) {
						String s = attr.getNodeValue();
						Node srcAttr = src.getAttributes().getNamedItem(s);
						if (srcAttr != null) {
							srcAttr.setNodeValue(INTERNAL_BOOKMARK_XML_ATTR_START + result.getTextContent() + INTERNAL_BOOKMARK_XML_ATTR_END);
							dest.getAttributes().setNamedItem(doc.adoptNode(srcAttr.cloneNode(false)));
							continue;
						}
					}
					if (!field) {
						dest.appendChild(result);
						preProcess(doc, childElement, dest);
						result = doc.createElement(INTERNAL_BOOKMARK_TAG);
						result.setTextContent("END".concat(key));
						dest.appendChild(result);
						continue;
					}
				}
			}
			if (result == null) {
				result = doc.adoptNode(childElement.cloneNode(false));
			}
			dest.appendChild(result);
			preProcess(doc, childElement, result);
		}
	}

	@Override
	protected int getInternalBookmarkStart(StringBuffer sb) {

		int attr = sb.indexOf(INTERNAL_BOOKMARK_XML_ATTR_START);
		int tag = super.getInternalBookmarkStart(sb);
		return attr >= 0 && attr < tag ? attr : tag;
	}

	@Override
	protected int getInternalBookmarkEnd(StringBuffer sb) {

		int attr = sb.indexOf(INTERNAL_BOOKMARK_XML_ATTR_END);
		int tag = super.getInternalBookmarkEnd(sb);
		return attr >= 0 && attr < tag ? attr : tag;
	}

	private String getValueOfAttribute(String attr, Node nodeElement) {

		for (int i = 0; i < nodeElement.getAttributes().getLength(); i++) {
			if (nodeElement.getAttributes().item(i).getNodeName().equalsIgnoreCase(attr)) {
				return nodeElement.getAttributes().item(i).getNodeValue();
			}
		}
		return null;
	}

	private String getPfadOnly(final String key) {

		String result = key;
		if (keyTranslationTable != null) {
			for (Map.Entry<String, String> entry : keyTranslationTable.entrySet()) {
				if (result.endsWith(entry.getKey())) {
					return result.substring(0, result.length() - entry.getKey().length());
				}
			}
		}
		return result;
	}

	@Override
	public ImageHandler<Image> getImageHandler() {

		return null;
	}

	@Override
	protected XmlBasedFieldMergeElement getFieldMergeElement(String key, ImageHandler imageHandler) {

		return new XmlBasedFieldMergeElement(key, key, images, imageHandler) {

			@Override
			protected String getDefaultDateFormat() {

				return XML_DATE_FORMAT;
			}

			@Override
			protected String getDefaultIntFormat() {

				return INTEGER_NUMBER_FORMAT;
			}

			@Override
			protected String getDefaultFloatFormat() {

				return DECIMAL_NUMBER_FORMAT;
			}
		};
	}

}
