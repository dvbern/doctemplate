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
package ch.dvbern.lib.doctemplate.odt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ch.dvbern.lib.doctemplate.common.AbstractMergeEngine;
import ch.dvbern.lib.doctemplate.common.DocTemplateException;
import ch.dvbern.lib.doctemplate.common.Image;
import ch.dvbern.lib.doctemplate.common.MergeSource;
import ch.dvbern.lib.doctemplate.util.ImageHandler;

/**
 * Merged eine ODT-Vorlage mit Informationen, die Aufgrund der Bezeichnungen, die innerhalb der Vorlage als MERGE-Fields
 * enthalten sind zusammen.
 */
public class ODTMergeEngine extends AbstractMergeEngine<Image> {

	private static final String UTF8 = "utf-8";

	private static final String OO_FIELD_GET_TAG_POSTFIX = "variable-get";
	private static final String OO_FIELD_SET_TAG_POSTFIX = "variable-set";
	private static final String TEXT_NAME = "text:name";

	private static final String CONTENT_XML_FILE_NAME = "content.xml";
	private static final String STYLES_XML_FILE_NAME = "styles.xml";
	private static final String ALTERNATE_SUFFIX = "_ALT";
	private static final String CONDITION_BEGIN = "IF_";
	private static final String ITERATION_BEGIN = "WHILE_";
	private static final String ODT_BOOKMARK_TAG_SUFFIX = "bookmark";

	private static final String MANIFEST_FILE_NAME = "META-INF/manifest.xml";
	private static final String MANIFEST_END_TAG = "</manifest:manifest>";
	private static final String MANIFEST_ENTRY_1 = "<manifest:file-entry manifest:media-type=\"image/";
	private static final String MANIFEST_ENTRY_2 = "\" manifest:full-path=\"";
	private static final String MANIFEST_ENTRY_3 = "\"/>";
	private final ImageHandler<Image> imageHandler = new ODTImageHandler();

	/** Logger */
	private static final Log log = LogFactory.getLog(ODTMergeEngine.class);

	/**
	 * Initialisierung der Engine mit einem kennzeichnenden Namen.
	 *
	 * @param name Kennzeichnung
	 */
	public ODTMergeEngine(String name) {
		super(name);
	}

	/**
	 * Merged aus der Vorlage <code>reader</code> ein Dokument und gibt dieses als byte[] zurueck. Die Platzhalter
	 * innerhalb der Vorlage werden mit Hilfe der <code>mergeSource</code> ermittelt und abgefuellt.
	 *
	 * @param input Bezugsquelle fuer die Vorlage
	 * @param mergeSource Quelle fuer die Informationen zum abfuellen des Templates
	 * @param keyTranslationTable Uebersetzung von Keys, damit Einschraenkungen von Word umgangen werden koennen
	 * @return Ergebnisdokument
	 * @throws DocTemplateException Gatherer Fehler der eine Benutzerfehlermeldung erzeugen sollen
	 */
	public byte[] getDocument(InputStream input, MergeSource mergeSource, Map<String, String> keyTranslationTable) throws DocTemplateException {

		this.keyTranslationTable.putAll(keyTranslationTable);
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
		try {
			ZipEntry ze;
			ZipOutputStream zipout = new ZipOutputStream(baos);
			ZipInputStream zipin = new ZipInputStream(input);
			ByteArrayOutputStream manifest = new ByteArrayOutputStream();
			while ((ze = zipin.getNextEntry()) != null) {
				String zeName = ze.getName();
				// In styles.xml kann der Header and Footer gefunden werden.
				if (CONTENT_XML_FILE_NAME.equals(zeName) || STYLES_XML_FILE_NAME.equals(zeName)) {
					zipout.putNextEntry(new ZipEntry(zeName));
					mergeContent(mergeSource, zipin, zipout);
				} else if (MANIFEST_FILE_NAME.equals(zeName)) {
					transfer(zipin, manifest);
				} else {
					zipout.putNextEntry(new ZipEntry(zeName));
					transfer(zipin, zipout);
				}
			}
			addManifest(manifest, zipout);
			// Bilder einfuegen
			for (Map.Entry<String, Image> me : images.entrySet()) {
				zipout.putNextEntry(new ZipEntry(me.getKey()));
				zipout.write(me.getValue().getBytes());
			}
			zipin.close();
			zipout.close();
			byte[] result = baos.toByteArray();
			baos.close();
			return result;
		} catch (IOException e) {
			throw new DocTemplateException(e);
		}
	}

	private void transfer(InputStream input, OutputStream output) throws IOException {

		int read = 0;
		byte[] buf = new byte[1024];
		while ((read = input.read(buf, 0, 1024)) != -1) {
			output.write(buf, 0, read);
		}
	}

	private void addManifest(ByteArrayOutputStream manifest, ZipOutputStream zipout) throws IOException {

		if (manifest.size() > 0) {
			zipout.putNextEntry(new ZipEntry(MANIFEST_FILE_NAME));
			byte[] manifestBytes = manifest.toByteArray();
			if (images != null && images.size() > 0) {
				StringBuilder manifestBuilder = new StringBuilder(new String(manifestBytes, UTF8));
				for (Map.Entry<String, Image> img : images.entrySet()) {
					int p = manifestBuilder.indexOf(MANIFEST_END_TAG);
					StringBuilder sb = new StringBuilder(MANIFEST_ENTRY_1);
					sb.append(img.getValue().getFormat().toString().toLowerCase());
					sb.append(MANIFEST_ENTRY_2).append(img.getKey()).append(MANIFEST_ENTRY_3);
					manifestBuilder.insert(p, sb.toString());
				}
				manifestBytes = manifestBuilder.toString().getBytes(UTF8);
			}
			zipout.write(manifestBytes);
		}
	}

	@Override
	protected void preProcess(Document doc, Node src, Node dest) throws DocTemplateException {

		NodeList childElements = src.getChildNodes();
		for (int i = 0; i < childElements.getLength(); i++) {
			Node childElement = childElements.item(i);
			Node result = null;
			boolean field = false;
			// OO Field Eigenschaft kann mit "variable-get" oder "variable-set" Knotename behandlen werden.
			if (childElement.getNodeName().endsWith(ODT_BOOKMARK_TAG_SUFFIX)
					|| (field = childElement.getNodeName().endsWith(OO_FIELD_GET_TAG_POSTFIX) || childElement.getNodeName().endsWith(OO_FIELD_SET_TAG_POSTFIX))) {
				String key = "";
				// In Fall des Fields key gleich mit dem "text:name" Eigenschaft
				if (field) {
					key = childElement.getAttributes().getNamedItem(TEXT_NAME).getTextContent();
				} else {
					key = childElement.getAttributes().item(0).getTextContent();
				}
				// mehrere gleiche Textmarken mit ALT-Suffix intern ohne ALT-Suffix anwenden
				int altPos = key.indexOf(ALTERNATE_SUFFIX);
				if (altPos > 0) {
					key = key.substring(0, altPos);
				}
				if (key != null && (key.startsWith(getFieldPrefix()) || key.startsWith(SORTFIELD_PREFIX) || key.startsWith(CONDITION_BEGIN) || key.startsWith(CONDITION_END)
						|| key.startsWith(ITERATION_BEGIN) || key.startsWith(ITERATION_END)) || field) {
					result = doc.createElement(INTERNAL_BOOKMARK_TAG);
					// In Fall des Fields wird ein Feld mit Prefix "FIELD_" generiert. Das bedautet, wir behandlen den
					// OO Field ebenso, als "FIELD_" Bookmark
					result.setTextContent(field ? (getFieldPrefix() + key) : key);
				}
			}
			if (result == null) {
				result = doc.adoptNode(childElement.cloneNode(false));
			}
			dest.appendChild(result);
			preProcess(doc, childElement, result);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.dvbern.lib.doctemplate.common.AbstractMergeEngine#getImageHandler()
	 */
	@Override
	public ImageHandler<Image> getImageHandler() {

		return imageHandler;
	}

}