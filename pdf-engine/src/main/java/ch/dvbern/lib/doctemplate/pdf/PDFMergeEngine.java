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
package ch.dvbern.lib.doctemplate.pdf;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;

import ch.dvbern.lib.doctemplate.common.BeanMergeSource;
import ch.dvbern.lib.doctemplate.common.DocTemplateException;
import ch.dvbern.lib.doctemplate.common.Image;
import ch.dvbern.lib.doctemplate.common.MergeContext;
import ch.dvbern.lib.doctemplate.common.MergeSource;
import ch.dvbern.lib.doctemplate.util.FormatHelper;

/**
 * Merged eine PDF-Vorlage mit Informationen, die Aufgrund der Namen der Formularfelder, die innerhalb der Vorlage als
 * MERGE-Fields enthalten sind zusammen.
 */
public class PDFMergeEngine {

	private static final String FORMAT_SUFFIX = "_FMT";
	private static final String ALTERNATE_SUFFIX = "_ALT";

	/** Logger */
	private static final Log log = LogFactory.getLog(PDFMergeEngine.class);

	private final String name;
	private Map<String, String> keyTranslationTable = null;

	/**
	 * Initialisierung der Engine mit einem kennzeichnenden Namen.
	 *
	 * @param name Kennzeichnung
	 */
	public PDFMergeEngine(String name) {

		this.name = name;
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

		this.keyTranslationTable = keyTranslationTable;
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

		FlattenMergeSource flattenMergeSource = new FlattenMergeSource(mergeSource);
		MergeContext ctx = new MergeContext(flattenMergeSource);

		PdfReader pdfTemplate;
		ByteArrayOutputStream out;
		try {
			pdfTemplate = new PdfReader(input);

			out = new ByteArrayOutputStream();
			PdfStamper stamper = new PdfStamper(pdfTemplate, out);

			stamper.setFormFlattening(true);
			AcroFields fields = stamper.getAcroFields();
			for (Object o : fields.getFields().entrySet()) {
				@SuppressWarnings("rawtypes")
				String key = ((Map.Entry) o).getKey().toString();

				// PdfForm-Zeichenuebersetzung (* -> .)
				String translated = translate(key);

				// Alternative-Suffix vom key entfernen
				String ohneAltSuffix = translated;
				int i = ohneAltSuffix.indexOf(ALTERNATE_SUFFIX);
				if (i > 0) {
					ohneAltSuffix = ohneAltSuffix.substring(0, i);
				}

				// Format-Suffix aus key extrahieren
				String ohneFormatSuffix = ohneAltSuffix, formatSuffix = null;
				i = ohneFormatSuffix.indexOf(FORMAT_SUFFIX);
				if (i > 0) {
					formatSuffix = ohneFormatSuffix.substring(i + FORMAT_SUFFIX.length());
					ohneFormatSuffix = ohneFormatSuffix.substring(0, i);
				}

				Object data = flattenMergeSource.getData(ctx, ohneFormatSuffix);
				if (data instanceof Image) {
					Image img = (Image) data;
					float[] imgPosition = stamper.getAcroFields().getFieldPositions(key);
					int page = Float.valueOf(imgPosition[0]).intValue();
					PdfContentByte canvas = stamper.getOverContent(page);
					insertImage(img, canvas, imgPosition);
				} else {
					String s = FormatHelper.getDataAsString(data, formatSuffix);
					stamper.getAcroFields().setField(key, s);
				}
			}
			stamper.close();
			pdfTemplate.close();
		} catch (DocTemplateException e) {
			throw e;
		} catch (Exception e) {
			throw new DocTemplateException(e);
		}
		return out.toByteArray();
	}

	private void insertImage(Image img, PdfContentByte canvas, float[] imgPosition) throws Exception {

		com.lowagie.text.Image image = com.lowagie.text.Image.getInstance(img.getBytes());

		float fieldLx = imgPosition[1];
		float fieldLy = imgPosition[2];
		float fieldUx = imgPosition[3];
		float fieldUy = imgPosition[4];
		Rectangle rect = new Rectangle(fieldLx, fieldLy, fieldUx, fieldUy);
		float absPosWidth = fieldLx;
		float absPosHeight = fieldLy;
		image.scaleToFit(rect.getWidth(), rect.getHeight());
		image.setAbsolutePosition(absPosWidth, absPosHeight);
		canvas.addImage(image, rect.getWidth(), 0, 0, rect.getHeight(), absPosWidth, absPosHeight);
	}

	private String translate(String key) {

		String result = key.replace('*', '.');
		if (keyTranslationTable != null) {
			for (Map.Entry<String, String> entry : keyTranslationTable.entrySet()) {
				result = result.replace(entry.getKey(), entry.getValue());
			}
		}
		return result;
	}

	/**
	 * MergeSource Implementierung fuer einen direkten Zugriff in hierarchische MergeSource-Strukturen mit
	 * Mehrfachvorkommen.
	 *
	 * @author METH
	 */
	public class FlattenMergeSource implements MergeSource {

		private final MergeSource nestedMergeSource;

		/**
		 * @param nestedMergeSource initiale MergeSource
		 */
		public FlattenMergeSource(MergeSource nestedMergeSource) {

			super();

			this.nestedMergeSource = nestedMergeSource;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see ch.dvbern.rodos.merger.shared.MergeSource#getData(ch.dvbern.rodos.merger.shared.MergeContext,
		 * java.lang.String)
		 */
		public Object getData(MergeContext ctx, String key) throws DocTemplateException {

			MergeSource currentSource = nestedMergeSource;
			String s = key;
			int indexPos = s.indexOf("_[");
			while (indexPos > 0) {
				List<MergeSource> l = currentSource.whileStatement(ctx, s.substring(0, indexPos));
				int endPos = s.indexOf("].", indexPos);
				Integer i = null;
				try {
					if (endPos > 0) {
						i = Integer.valueOf(s.substring(indexPos + 2, endPos));
					}
				} catch (Exception e) {
					log.warn(name + ": invalid iteration index (" + s.substring(indexPos + 2, endPos) + ")");
				}
				if (l != null && i != null && l.size() > i.intValue()) {
					s = s.substring(endPos + 2);
					currentSource = beanMergeSourceWithEmptyPrefix(l.get(i));
				}
				indexPos = s.indexOf("_[", indexPos + 1);
			}
			return currentSource.getData(ctx, s);
		}

		private MergeSource beanMergeSourceWithEmptyPrefix(MergeSource ms) {

			if (ms instanceof BeanMergeSource) {
				return ((BeanMergeSource) ms).cloneWithEmptyPrefix();
			}
			return ms;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see ch.dvbern.rodos.merger.shared.MergeSource#ifStatement(ch.dvbern.rodos.merger.shared.MergeContext,
		 * java.lang.String)
		 */
		public Boolean ifStatement(MergeContext ctx, String key) throws DocTemplateException {

			throw new DocTemplateException(new IllegalAccessException());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see ch.dvbern.rodos.merger.shared.MergeSource#whileStatement(ch.dvbern.rodos.merger.shared.MergeContext,
		 * java.lang.String)
		 */
		public List<MergeSource> whileStatement(MergeContext ctx, String key) throws DocTemplateException {

			throw new DocTemplateException(new IllegalAccessException());
		}

	}

}