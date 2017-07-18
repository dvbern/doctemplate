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
package ch.dvbern.lib.doctemplate.rtf;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ch.dvbern.lib.doctemplate.common.BasicMergeElement;
import ch.dvbern.lib.doctemplate.common.ConditionMergeElement;
import ch.dvbern.lib.doctemplate.common.DocTemplateException;
import ch.dvbern.lib.doctemplate.common.Image;
import ch.dvbern.lib.doctemplate.common.IterationMergeElement;
import ch.dvbern.lib.doctemplate.common.MergeContext;
import ch.dvbern.lib.doctemplate.common.MergeSource;
import ch.dvbern.lib.doctemplate.common.StaticMergeElement;
import ch.dvbern.lib.doctemplate.util.FormatHelper;
import ch.dvbern.lib.doctemplate.util.LdtConstants;

import net.sourceforge.rtf.RTFTemplate;
import net.sourceforge.rtf.document.RTFDocument;
import net.sourceforge.rtf.document.RTFElement;
import net.sourceforge.rtf.document.RTFEndBookmark;
import net.sourceforge.rtf.document.RTFField;
import net.sourceforge.rtf.document.RTFStartBookmark;
import net.sourceforge.rtf.helper.RTFTemplateBuilder;

/**
 * Merged eine RTF-Vorlage mit Informationen, die Aufgrund der Bezeichnungen, die innerhalb der Vorlage als MERGE-Fields
 * enthalten sind zusammen. Die Komponente nutzt RTFTemplate (http://rtftemplate.sourceforge.net) als Basistechnologie.
 * Der Code in RTFTemplate ist in der Version 1.0.1-b13 nicht Thread-Safe!!!
 */
public class RTFMergeEngine {

	private static final String CONDITION_BEGIN = "IF_";
	private static final String CONDITION_END = "ENDIF_";
	private static final String ITERATION_BEGIN = "WHILE_";
	private static final String ITERATION_END = "ENDWHILE_";
	private static final String SORTFIELD_PREFIX = "SORT_";
	private static final String ALTERNATE_SUFFIX = "_ALT";
	private static final String FORMAT_SUFFIX = "_FMT";

	private static final String DOCUMENT_CONTENT_BEGIN = "{\\*\\bkmkend DOCUMENT_CONTENT_BEGIN}";
	private static final String DOCUMENT_CONTENT_END = "{\\*\\bkmkstart DOCUMENT_CONTENT_END}";

	private static final String syncPoint = new String("RTFMergeEngine::syncPoint");

	/** Logger */
	private static final Log log = LogFactory.getLog(RTFMergeEngine.class);

	private final String name;
	private Stack<BasicMergeElement> parseStack;
	private Map<String, String> keyTranslationTable = null;

	/**
	 * Initialisierung der Engine mit einem kennzeichnenden Namen.
	 *
	 * @param name Kennzeichnung
	 */
	public RTFMergeEngine(String name) {

		this.name = name;
	}

	/**
	 * Extrahiert den Inhalt eines Dokuments (ohne Header und Footer), damit dieser beispielsweise in ein Hauptdokument
	 * eingefuegt werden kann.
	 *
	 * @param originalContent Input-Dokument
	 * @return Input ohne Header und Footer (Inhalt zwischen den BEGIN/END-Bookmarks)
	 */
	public String extractDocumentContent(String originalContent) {

		StringBuffer sb = new StringBuffer(new String(originalContent));
		int pos = sb.indexOf(DOCUMENT_CONTENT_BEGIN);
		if (pos > 0) {
			sb.delete(0, pos + DOCUMENT_CONTENT_BEGIN.length());
		}
		pos = sb.indexOf(DOCUMENT_CONTENT_END);
		if (pos > 0) {
			sb.setLength(pos);
		}
		return sb.toString();
	}

	/**
	 * Fuegt Subdokumente aus <code>includesMap</code> in das Hauptdokument <code>holderTemplate</code> ein.
	 *
	 * @param holderTemplate Hauptdokument
	 * @param includesMap Subdokumente
	 * @return zusammengefuegtes Dokument
	 */
	public String includeSubDocuments(String holderTemplate, Map<String, StringBuffer> includesMap) {

		StringBuffer sb = new StringBuffer(holderTemplate);
		String INCLUDE_BEGIN = "{\\*\\bkmkstart DOCUMENT_INCLUDE_";
		for (int start = sb.indexOf(INCLUDE_BEGIN); start > 0; start = sb.indexOf(INCLUDE_BEGIN)) {
			int end = sb.indexOf("}", start);
			String name = sb.substring(start + INCLUDE_BEGIN.length(), end);

			String INCLUDE_END = "{\\*\\bkmkend DOCUMENT_INCLUDE_" + name + "}";
			end = sb.indexOf(INCLUDE_END) + INCLUDE_END.length();
			sb.delete(start, end);

			StringBuffer include = includesMap.get(name);
			if (include != null) {
				sb.insert(start, include.toString());
			}
		}
		return sb.toString();
	}

	/**
	 * Merged aus der Vorlage <code>reader</code> ein Dokument und gibt dieses als byte[] zurueck. Die Platzhalter
	 * innerhalb der Vorlage werden mit Hilfe der <code>mergeSource</code> ermittelt und abgefuellt.
	 *
	 * @param reader Bezugsquelle fuer die Vorlage
	 * @param mergeSource Quelle fuer die Informationen zum abfuellen des Templates
	 * @param keyTranslationTable Uebersetzung von Keys, damit Einschraenkungen von Word umgangen werden koennen
	 * @return Ergebnisdokument
	 * @throws DocTemplateException Gatherer Fehler der eine Benutzerfehlermeldung erzeugen sollen
	 */
	public byte[] getDocument(Reader reader, MergeSource mergeSource, Map<String, String> keyTranslationTable) throws DocTemplateException {

		this.keyTranslationTable = keyTranslationTable;
		return getDocument(reader, mergeSource);
	}

	/**
	 * Merged aus der Vorlage <code>reader</code> ein Dokument und gibt dieses als byte[] zurueck. Die Platzhalter
	 * innerhalb der Vorlage werden mit Hilfe der <code>mergeSource</code> ermittelt und abgefuellt.
	 *
	 * @param reader Bezugsquelle fuer die Vorlage
	 * @param mergeSource Quelle fuer die Informationen zum abfuellen des Templates
	 * @return Ergebnisdokument
	 * @throws DocTemplateException Gatherer Fehler der eine Benutzerfehlermeldung erzeugen sollen
	 */
	public byte[] getDocument(Reader reader, MergeSource mergeSource) throws DocTemplateException {

		synchronized (syncPoint) {
			try {
				RTFTemplate rtfTemplate = RTFTemplateBuilder.newRTFTemplateBuilder().newRTFTemplate();
				rtfTemplate.setTemplate(reader);

				// Template mit MergeSourcen aufbereiten
				log.debug(name + ": RTF Template in Substrukturen transformieren");
				RTFDocument rtfDoc = rtfTemplate.transform();
				BasicMergeElement bme = new BasicMergeElement();
				parseStack = new Stack<BasicMergeElement>();
				parseStack.push(bme);
				log.debug(name + ": RTF Template parsen");
				parseTemplate(rtfDoc);
				if (parseStack.size() > 1) {
					throw new DocTemplateException("error.rtftemplate.invalid.structure");
				}
				return bme.getContent(new MergeContext(mergeSource), mergeSource).getBytes();
			} catch (DocTemplateException sfe) {
				throw sfe;
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void parseTemplate(RTFElement rtfElement) throws DocTemplateException {

		for (Object o : rtfElement.getElementList()) {
			if (o instanceof RTFElement) {
				if (o instanceof RTFField) {
					String key = ((RTFField) o).getName();
					String rtfCode = ((RTFField) o).getRTFContentOfSimpleElement();
					if (key == null || key.startsWith("$")) {
						StaticMergeElement sme = new StaticMergeElement(rtfCode);
						parseStack.peek().addMergeElement(sme);
					} else {
						FieldMergeElement fme = new FieldMergeElement(translate(key), rtfCode);
						parseStack.peek().addMergeElement(fme);
					}
				} else if (o instanceof RTFStartBookmark) {
					RTFStartBookmark bm = (RTFStartBookmark) o;
					String bmName = bm.getName();
					if (bmName.startsWith(CONDITION_BEGIN)) {
						String key = bmName.substring(CONDITION_BEGIN.length());
						ConditionMergeElement cme = new ConditionMergeElement(name, translate(key));
						cme.initFuerRtf();
						parseStack.peek().addMergeElement(cme);
						parseStack.push(cme);
					} else if (bmName.startsWith(ITERATION_BEGIN)) {
						String key = bmName.substring(ITERATION_BEGIN.length());
						IterationMergeElement ime = new IterationMergeElement(name, translate(key));
						ime.initFuerRtf();
						parseStack.peek().addMergeElement(ime);
						parseStack.push(ime);
					} else if (bmName.startsWith(SORTFIELD_PREFIX)) {
						String key = bmName.substring(SORTFIELD_PREFIX.length());
						// mehrere gleiche Textmarken mit ALT-Suffix
						// intern ohne ALT-Suffix anwenden
						int altPos = key.indexOf(ALTERNATE_SUFFIX);
						if (altPos > 0) {
							key = key.substring(0, altPos);
						}
						Object ime = parseStack.peek();
						if (ime instanceof IterationMergeElement) {
							((IterationMergeElement) ime).addSortFieldKey(key);
						} else {
							log.warn("invalid structure: no IterationMergeElement on parse stack");
						}
					} else if (!bmName.startsWith(CONDITION_END) && !bmName.startsWith(ITERATION_END)) {
						String s = bm.getRTFContentOfSimpleElement();
						StaticMergeElement sme = new StaticMergeElement(s);
						parseStack.peek().addMergeElement(sme);
					}
				} else if (o instanceof RTFEndBookmark) {
					RTFEndBookmark bm = (RTFEndBookmark) o;
					String bmName = bm.getName();
					if (bmName.startsWith(CONDITION_END) || bmName.startsWith(ITERATION_END)) {
						if (parseStack.size() > 1) {
							parseStack.pop();
						} else {
							throw new DocTemplateException("error.rtftemplate.invalid.structure");
						}
					} else if (!bmName.startsWith(CONDITION_BEGIN) && !bmName.startsWith(ITERATION_BEGIN) && !bmName.startsWith(SORTFIELD_PREFIX)) {
						String s = bm.getRTFContentOfSimpleElement();
						StaticMergeElement sme = new StaticMergeElement(s);
						parseStack.peek().addMergeElement(sme);
					}
				} else {
					parseTemplate((RTFElement) o);
				}
			} else {
				String s = o.toString();
				StaticMergeElement sme = new StaticMergeElement(s);
				parseStack.peek().addMergeElement(sme);
			}
		}
	}

	private String translate(String key) {

		String result = key;
		if (keyTranslationTable != null) {
			for (Map.Entry<String, String> entry : keyTranslationTable.entrySet()) {
				result = result.replace(entry.getKey(), entry.getValue());
			}
		}
		return result;
	}

	private class FieldMergeElement extends BasicMergeElement {

		private final String key;
		private final String rtfCode;

		/**
		 * Konstruktor eines MergeElements, dessen Ausgabe dynamisch ermittelt wird
		 *
		 * @param key Identifikation des Inhaltes, der fuer die Ausgabe ermittelt werden soll
		 * @param rtfCode RTF Code mit dem original Merge Field
		 */
		public FieldMergeElement(String key, String rtfCode) {

			this.key = key;
			this.rtfCode = rtfCode;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * ch.dvbern.rodos.merger.server.util.RTFMergeEngine.BasicMergeElement#getContent(ch.dvbern.rodos.merger.shared
		 * .MergeContext, ch.dvbern.rodos.merger.shared.MergeSource)
		 */
		@Override
		public String getContent(MergeContext ctx, MergeSource mergeSource) throws DocTemplateException {

			log.debug(name + ": evaluate template source with key " + key);

			// Format-Suffix aus key extrahieren
			String keyWithoutFormatSuffix = key, formatSuffix = null;
			int i = key.indexOf(FORMAT_SUFFIX);
			if (i > 0) {
				formatSuffix = key.substring(i + FORMAT_SUFFIX.length());
				keyWithoutFormatSuffix = key.substring(0, i);
			}

			Object data = mergeSource.getData(ctx, keyWithoutFormatSuffix);
			if (data instanceof Image) {
				return getImageAsRTF((Image) data, formatSuffix);
			}
			if (data != null) {
				String dataAsString = FormatHelper.getDataAsString(data, StringUtils.isEmpty(formatSuffix) ? getDefaultFormatter(data) : formatSuffix);
				dataAsString = convertRtfEncodings(dataAsString);
				int rtlchPos = rtfCode.indexOf("{\\rtlch\\fcs1");
				if (rtlchPos >= 0) {
					int mfldPos = rtfCode.indexOf(" MERGEFIELD", rtlchPos);
					if (mfldPos > 0 && mfldPos < rtfCode.indexOf("}", rtlchPos)) {
						StringBuffer replacement = new StringBuffer();
						replacement.append(rtfCode.substring(rtlchPos, mfldPos));
						replacement.append(dataAsString);
						replacement.append("}");
						return replacement.toString();
					}
				}
				return dataAsString;
			} else {
				log.warn(name + ": no template source with key " + key);
				return rtfCode;
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

		private String getImageAsRTF(Image image, String formatSuffix) throws DocTemplateException {

			StringBuilder result = new StringBuilder("{\\*\\shppict {\\pict");
			if (Image.Format.PNG == image.getFormat()) {
				result.append("\\pngblip");
			} else if (Image.Format.JPEG == image.getFormat()) {
				result.append("\\jpegblip");
			} else if (Image.Format.EMF == image.getFormat()) {
				result.append("\\emfblip");
			} else {
				throw new DocTemplateException("unknown image format: " + image.getFormat());
			}
			long w = image.getWidth(), h = image.getHeight();
			try {
				if (formatSuffix != null && formatSuffix.length() > 0) {
					StringTokenizer st = new StringTokenizer(formatSuffix, "_");
					if (st.hasMoreTokens()) {
						w = Integer.parseInt(st.nextToken());
					}
					if (st.hasMoreTokens()) {
						h = Integer.parseInt(st.nextToken());
					}
				}
				result.append("\\picw").append(w).append("\\pich").append(h);
				result.append("\\picwgoal").append(w * 15).append("\\pichgoal").append(h * 15);
				result.append(" ");
				ByteArrayInputStream bais = new ByteArrayInputStream(image.getBytes());
				DataInputStream dis = new DataInputStream(bais);
				while (dis.available() > 0) {
					String s = Integer.toHexString(dis.read());
					if (s.length() % 2 != 0) {
						result.append("0");
					}
					result.append(s);
				}
			} catch (IOException ioe) {
				log.warn("error reading image: " + formatSuffix, ioe);
			} catch (NumberFormatException nfe) {
				log.warn("invalid image format suffix: " + formatSuffix, nfe);
			}
			result.append("}}");
			return result.toString();
		}

		private String convertRtfEncodings(String data) {

			// special character encodings
			StringBuffer sb = new StringBuffer();
			int length = data.length();
			for (int i = 0; i < length; i++) {
				char c = data.charAt(i);
				if (c == '\n') {
					sb.append("\\line "); // LF einfuegen
				} else if (c == '\r') {
					sb.append(""); // CRs NICHT einfuegen
				} else if (c > 0xFF || c < 0x00) {
					sb.append("\\u");
					sb.append(Short.toString((short) c).toCharArray());
					sb.append("?");
				} else if (c >= 0x80 || c < 0x20 || c == 0x5C || c == 0x7B || c == 0x7D) {
					sb.append("\\'");
					sb.append(Integer.toHexString(c));
				} else {
					sb.append(c);
				}
			}
			return sb.toString();
		}
	}

}