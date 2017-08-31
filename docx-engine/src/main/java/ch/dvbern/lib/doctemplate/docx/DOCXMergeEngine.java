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
package ch.dvbern.lib.doctemplate.docx;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import ch.dvbern.lib.doctemplate.common.AbstractMergeEngine;
import ch.dvbern.lib.doctemplate.common.DocTemplateException;
import ch.dvbern.lib.doctemplate.common.Image;
import ch.dvbern.lib.doctemplate.common.Image.Format;
import ch.dvbern.lib.doctemplate.common.MergeSource;
import ch.dvbern.lib.doctemplate.common.XmlBasedFieldMergeElement;
import ch.dvbern.lib.doctemplate.util.ImageHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Merged eine DOCX-Vorlage mit Informationen, die Aufgrund der Bezeichnungen, die innerhalb der Vorlage als
 * DocVariablen enthalten sind zusammen.
 */
public class DOCXMergeEngine extends AbstractMergeEngine<DocxImage> {

	private static final String UTF8 = "utf-8";

	private static final String IMAGE_PREFIX = "word/media/image";
	private static final String IMAGE_ID = "#IMG_ID#";
	private static final String RELATION = "<Relationship Id=\"" + IMAGE_ID
			+ "\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/image\" Target=\"media/" + ImageHandler.IMAGE_NAME_TAG + "\" />";
	private static final String IMAGE_EXTENSION = "#IMG_EXT#";
	private static final String EXTENSION_NODE = "<Default Extension=\"" + IMAGE_EXTENSION + "\" ContentType=\"image/" + IMAGE_EXTENSION + "\" />";
	private static final String CONTENT_XML_FILE_NAME = "word/document.xml";
	private static final String HEADER_XML_FILE_NAME = "word/header";
	private static final String FOOTER_XML_FILE_NAME = "word/footer";
	private static final String RELS_XML_FILE_NAME = "word/_rels/document.xml.rels";
	private static final String CONTENT_TYPES_FILE_NAME = "[Content_Types].xml";
	private static final String ALTERNATE_SUFFIX = "_ALT";
	private static final String CONDITION_BEGIN = "IF_";
	private static final String ITERATION_BEGIN = "WHILE_";
	private static final String DOCX_DOCVARIABLE_TAG = "w:instrText";
	// Spaces are important! DVLIB-136
	private String docxDocVariableStart = " DOCVARIABLE  ";
	private String docxDocVariableEnd ="  \\* MERGEFORMAT ";
	private static final String DOCX_FLDCHAR_TAG = "w:fldChar";
	private static final String DOCX_FLDCHARTYPE_ATTR = "w:fldCharType";
	private static final String DOCX_FLDCHARTYPE_BEGIN = "begin";
	private static final String DOCX_FLDCHARTYPE_END = "end";

	private ImageHandler<DocxImage> imageHandler;

	private int maxImgIdx = 0;
	private int maxRId = 0;

	private StringBuilder docVariable = null;
	private Node fldcharBeginParentNode = null, fldcharBeginNode = null;

	private static final Log LOG = LogFactory.getLog(DOCXMergeEngine.class);
	/**
	 * Initialisierung der Engine mit einem kennzeichnenden Namen.
	 *
	 * @param name Kennzeichnung
	 */
	public DOCXMergeEngine(String name) {
		super(name);
	}

	public String getDocxDocVariableStart() {

		return this.docxDocVariableStart;
	}

	public void setDocxDocVariableStart(String docxDocVariableStart) {

		this.docxDocVariableStart = docxDocVariableStart;
	}

	public String getDocxDocVariableEnd() {

		return this.docxDocVariableEnd;
	}

	public void setDocxDocVariableEnd(String docxDocVariableEnd) {

		this.docxDocVariableEnd = docxDocVariableEnd;
	}

	/**
	 * Merged aus der Vorlage <code>reader</code> ein Dokument und gibt dieses als byte[] zurueck. Die Platzhalter
	 * innerhalb der Vorlage werden mit Hilfe der <code>mergeSource</code> ermittelt und abgefuellt.
	 *
	 * @param input Bezugsquelle fuer die Vorlage
	 * @param mergeSource Quelle fuer die Informationen zum abfuellen des Templates
	 * @param keyTranslationTbl Uebersetzung von Keys, damit Einschraenkungen von Word umgangen werden koennen
	 * @return Ergebnisdokument
	 * @throws DocTemplateException Gatherer Fehler der eine Benutzerfehlermeldung erzeugen sollen
	 */
	public byte[] getDocument(InputStream input, MergeSource mergeSource, Map<String, String> keyTranslationTbl) throws DocTemplateException {

		this.keyTranslationTable.putAll(keyTranslationTbl);
		return getDocument(input, mergeSource);
	}

	/**
	 * Merged aus der Vorlage <code>reader</code> ein Dokument und gibt dieses als byte[] zurueck. Die Platzhalter
	 * innerhalb der Vorlage werden mit Hilfe der <code>mergeSource</code> ermittelt und abgefuellt.
	 *
	 * @param input Bezugsquelle fuer die Vorlage
	 * @param mergeSource Quelle fuer die Informationen zum abfuellen des Templates
	 * @return Ergebnisdokument
	 * @throws DocTemplateException Gatherer Fehler der eine Benutzerfehlermeldung erzeugen sollen
	 */
	public byte[] getDocument(InputStream input, MergeSource mergeSource) throws DocTemplateException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try (ZipOutputStream zipout = new ZipOutputStream(baos); ZipInputStream zipin = new ZipInputStream(input)) {
			ZipEntry ze;

			ByteArrayOutputStream rels = new ByteArrayOutputStream();
			ByteArrayOutputStream contentTypes = new ByteArrayOutputStream();
			ByteArrayOutputStream content = new ByteArrayOutputStream();
			while ((ze = zipin.getNextEntry()) != null) {
				String zeName = ze.getName();
				// In Header und Footer ebenfalls Variablen ersetzen
				if (zeName != null //
						&& (zeName.startsWith(HEADER_XML_FILE_NAME) || zeName.startsWith(FOOTER_XML_FILE_NAME))) {
					zipout.putNextEntry(new ZipEntry(zeName));
					mergeContent(mergeSource, zipin, zipout);
				} else if (CONTENT_XML_FILE_NAME.equals(zeName)) {
					transfer(zipin, content);
				} else if (RELS_XML_FILE_NAME.equals(zeName)) {
					transfer(zipin, rels);
					getMaxRId(rels);
				} else if (CONTENT_TYPES_FILE_NAME.equals(zeName)) {
					transfer(zipin, contentTypes);
				} else if (zeName.startsWith(IMAGE_PREFIX)) {
					// wir nehmen die hoechste Nummer aus dem Ordner media
					int idx = Integer.parseInt(zeName.substring(IMAGE_PREFIX.length(), zeName.indexOf('.', IMAGE_PREFIX.length())));
					this.maxImgIdx = idx > this.maxImgIdx ? idx : this.maxImgIdx;
					zipout.putNextEntry(new ZipEntry(zeName));
					transfer(zipin, zipout);
				} else {
					zipout.putNextEntry(new ZipEntry(zeName));
					transfer(zipin, zipout);
				}
			}
			this.imageHandler = new DocxImageHandler(this.maxImgIdx, this.maxRId);
			updateContent(content, zipout, mergeSource);
			updateRels(rels, zipout);
			updateContentTypes(contentTypes, zipout);
			// Bilder einfuegen
			if (this.images != null) {
				for (Map.Entry<String, DocxImage> me : this.images.entrySet()) {
					zipout.putNextEntry(new ZipEntry(me.getKey()));
					zipout.write(me.getValue().getBytes());
				}
			}
			zipin.close();
			zipout.close();
			byte[] result = baos.toByteArray();
			baos.close();
			return result;
		} catch (IOException e) {
			throw new DocTemplateException(e);
		} catch (XPathExpressionException e) {
			throw new DocTemplateException(e);
		} catch (ParserConfigurationException e) {
			throw new DocTemplateException(e);
		} catch (SAXException e) {
			throw new DocTemplateException(e);
		}
	}

	private static void transfer(InputStream input, OutputStream output) throws IOException {

		int read = 0;
		byte[] buf = new byte[1024];
		while ((read = input.read(buf, 0, 1024)) != -1) {
			output.write(buf, 0, read);
		}
	}

	private void updateContent(ByteArrayOutputStream content, ZipOutputStream zipout, MergeSource mergeSource) throws IOException, DocTemplateException {

		if (content.size() > 0) {
			zipout.putNextEntry(new ZipEntry(CONTENT_XML_FILE_NAME));
			mergeContent(mergeSource, new ByteArrayInputStream(content.toByteArray()), zipout);
		}
	}

	private void updateRels(ByteArrayOutputStream rels, ZipOutputStream zipout) throws IOException {

		if (rels.size() > 0) {
			zipout.putNextEntry(new ZipEntry(RELS_XML_FILE_NAME));
			String xml = new String(rels.toByteArray(), UTF8);
			if (this.images != null && this.images.size() > 0) {
				StringBuffer xmlSB = new StringBuffer(xml.substring(0, xml.length() - "</Relationships>".length()));
				for (DocxImage image : this.images.values()) {
					image.setId(++this.maxRId);
					xmlSB.append(RELATION.replace(IMAGE_ID, image.getRId()).replace(ImageHandler.IMAGE_NAME_TAG, image.getName()));
				}
				xmlSB.append("</Relationships>");
				xml = xmlSB.toString();
			}
			zipout.write(xml.getBytes());
		}
	}

	private int getMaxRId(ByteArrayOutputStream xmlStream) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(new ByteArrayInputStream(xmlStream.toByteArray()));
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression expr = xpath.compile("Relationships/*");
		NodeList nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
		for (int i = 0; i < nodeList.getLength(); i++) {
			String id = nodeList.item(i).getAttributes().getNamedItem("Id").getTextContent();
			int idNum = Integer.parseInt(id.substring("rId".length()));
			this.maxRId = idNum > this.maxRId ? idNum : this.maxRId;
		}
		return this.maxRId;
	}

	private void updateContentTypes(ByteArrayOutputStream contentTypes, ZipOutputStream zipout) throws IOException {

		Set<Format> formatSet = new HashSet<>();
		if (contentTypes.size() > 0) {
			zipout.putNextEntry(new ZipEntry(CONTENT_TYPES_FILE_NAME));
			String xml = new String(contentTypes.toByteArray(), UTF8);
			if (this.images != null && this.images.size() > 0) {
				StringBuffer xmlSB = new StringBuffer(xml.substring(0, xml.length() - "</Types>".length()));
				for (Image image : this.images.values()) {
					if (formatSet.add(image.getFormat()) && xml.indexOf("image/" + image.getFormat().name().toLowerCase()) < 0) {
						xmlSB.append(EXTENSION_NODE.replaceAll(IMAGE_EXTENSION, image.getFormat().name().toLowerCase()));
					}
				}
				xmlSB.append("</Types>");
				xml = xmlSB.toString();
			}
			zipout.write(xml.getBytes());
		}
	}

	@Override
	protected void preProcess(Document doc, Node src, Node dest) throws DocTemplateException {

		NodeList childElements = src.getChildNodes();
		for (int i = 0; i < childElements.getLength(); i++) {
			Node childElement = childElements.item(i);
			if (childElement.getNodeName().equals(DOCX_DOCVARIABLE_TAG)) {
				String s = childElement.getTextContent();
				if (this.docVariable != null || s != null && s.startsWith(this.docxDocVariableStart)) {
					if (this.fldcharBeginNode != null && this.fldcharBeginParentNode != null) {
						// vorherigen DocVariable-Begin-Tag entfernen
						this.fldcharBeginParentNode.removeChild(this.fldcharBeginNode);
						this.fldcharBeginNode = null;
						this.fldcharBeginParentNode = null;
					}
					if (s != null && s.startsWith(this.docxDocVariableStart)) {
						s = s.substring(this.docxDocVariableStart.length());
					}
					if (this.docVariable == null) {
						this.docVariable = new StringBuilder();
					}
					this.docVariable.append(s);
					int endMarker = this.docVariable.indexOf(this.docxDocVariableEnd);
					if (endMarker < 0) {
						LOG.debug("'" + this.docxDocVariableEnd +"' was not immediatly found in tag, this can happen if the Docvariable"
								+ "is broken up over multiple instr tags. Continuing search...");
						continue; // DocVariable noch unvollstaendig
					}
					this.docVariable.delete(endMarker, this.docVariable.length());
					String dv = removeDoubleQuotes(this.docVariable.toString());
					int altPos = dv.indexOf(ALTERNATE_SUFFIX);
					if (altPos > 0) {
						// mehrere gleiche Textmarken mit ALT-Suffix: ab hier ohne
						// ALT-Suffix
						dv = dv.substring(0, altPos);
					}
					if (dv.startsWith(getFieldPrefix()) || dv.startsWith(SORTFIELD_PREFIX) || dv.startsWith(CONDITION_BEGIN) || dv.startsWith(CONDITION_END)
							|| dv.startsWith(ITERATION_BEGIN) || dv.startsWith(ITERATION_END)) {
						Node n = doc.createElement(INTERNAL_BOOKMARK_TAG);
						n.setTextContent(dv);
						dest.appendChild(n);
					}
					this.docVariable = null;
					continue;
				}
			}
			Node adoptedNode = doc.adoptNode(childElement.cloneNode(false));
			if (childElement.getNodeName().equals(DOCX_FLDCHAR_TAG)) {
				Node n = childElement.getAttributes().getNamedItem(DOCX_FLDCHARTYPE_ATTR);
				String fldCharType = n == null ? null : n.getTextContent();
				if (DOCX_FLDCHARTYPE_BEGIN.equals(fldCharType)) {
					this.fldcharBeginNode = adoptedNode;
					this.fldcharBeginParentNode = dest;
				} else if (DOCX_FLDCHARTYPE_END.equals(fldCharType)) {
					if (this.fldcharBeginNode == null || this.fldcharBeginParentNode == null) {
						continue;
					}
				}
			}
			dest.appendChild(adoptedNode);
			preProcess(doc, childElement, adoptedNode);
		}
	}

	private static String removeDoubleQuotes(String value) {

		// Word setzt Docvariablen in Anfuehrungszeichen, wenn Spaces enthalten sind
		String s = value.startsWith("\"") ? value.substring(1) : value;
		s = s.endsWith("\"") ? s.substring(0, s.length() - 1) : s;
		return s;
	}

	private class FieldMergeElement extends XmlBasedFieldMergeElement<DocxImage> {

		public FieldMergeElement(String name, String key, Map<String, DocxImage> images) {
			super(name, key, images, DOCXMergeEngine.this.imageHandler);
		}

		@Override
		protected void writeText(OutputStream output, String dataAsString) throws DocTemplateException {

			String dataToMerge = convertNewlinesAndPagebreaks(dataAsString);
			super.writeText(output, dataToMerge);
		}

	}

	/**
	 * This method converts \n characters in the appropriate format for docx. In essence it replaces \n with
	 * </w:t><w:br/><w:t>" Note that these are linebreaks and not new paragraphs Also it converts \f (formfeed)
	 * characters to pagebreaks.
	 * 
	 * @param dataAsString
	 * @return
	 */
	private static String convertNewlinesAndPagebreaks(final String dataAsString) {

		String stringWithTags = "<w:t>" + dataAsString + "</w:t>"; // wrap in text tags
		String stringWithLinebreaks = stringWithTags.replace("\n", "</w:t><w:br/><w:t>");// the
		// Linebreaks could also  be  entered with just a br tag but this is more correct
		return stringWithLinebreaks.replace("\f", "</w:t><w:br w:type=\"page\" /><w:t>");// the
		// \f marker is expected to  be at  the end of the variable inputtext.
	}

	@Override
	protected XmlBasedFieldMergeElement<DocxImage> getFieldMergeElement(String key, ImageHandler<DocxImage> imageHdlr) {

		return new FieldMergeElement(this.name, translate(key), this.images);
	}

	@Override
	public ImageHandler<DocxImage> getImageHandler() {

		return this.imageHandler;
	}

}
