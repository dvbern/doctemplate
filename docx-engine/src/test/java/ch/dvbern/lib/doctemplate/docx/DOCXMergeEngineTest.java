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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Semaphore;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ch.dvbern.lib.doctemplate.common.BeanMergeSource;
import ch.dvbern.lib.doctemplate.common.DocTemplateException;
import ch.dvbern.lib.doctemplate.common.ExtendedBeanMergeSource;
import ch.dvbern.lib.doctemplate.common.Image;
import ch.dvbern.lib.doctemplate.common.MergeContext;
import ch.dvbern.lib.doctemplate.common.MergeSource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author METH
 */
public class DOCXMergeEngineTest {

	private static final String CHARSET = "ISO-8859-1";

	/**
	 * Sets the default locale.
	 */
	@Before
	public void setLocale() {

		Locale.setDefault(new Locale("de", "CH"));
	}

	/**
	 * Test 1: Textbaustein wird nicht ausgeblendet, Liste leer.
	 */
	@SuppressWarnings("unused")
	@Test
	public void test1() throws Exception {

		DOCXMergeEngine docxME = new DOCXMergeEngine("Test1");
		String s;
		try (InputStream is = this.getClass().getResourceAsStream("test1.docx")) {
			s = new String(docxME.getDocument(is, new RootMergeSource(true, true)), CHARSET);
		}

		if (false) { // switch to true to write the file
			try (FileOutputStream fos = new FileOutputStream("/tmp/result1.docx")) {
				fos.write(s.getBytes(CHARSET));
			}
		}

		String xml = getContent(new ByteArrayInputStream(s.getBytes(CHARSET)), "word/document.xml");
		String expected = getContent(new ByteArrayInputStream(getBytes("result1.docx")), "word/document.xml");
		Assert.assertEquals("merged docx does not match the expected result", xml, expected);
	}

	/**
	 * Test 2: Multithreading-Test.
	 */
	public void test2() throws Exception {

		String expected = new String(getBytes("result1.xml"));

		Semaphore semaphore = new Semaphore(100, true);
		for (int i = 0; i < 100; i++) {
			semaphore.acquire();
			new MultithreadingTest(i, semaphore, expected).start();
		}
		semaphore.acquire(100);
	}

	/**
	 * Test 3: Kopf- und Fusszeile
	 */
	@SuppressWarnings("unused")
	@Test
	public void test3() throws Exception {

		DOCXMergeEngine docxME = new DOCXMergeEngine("Test3");
		String s;
		try (InputStream is = this.getClass().getResourceAsStream("test3.docx")) {
			s = new String(docxME.getDocument(is, new RootMergeSource(true, true)), CHARSET);
		}

		if (false) { // switch to true to write the file
			try (FileOutputStream fos = new FileOutputStream("/tmp/result3.docx")) {
				fos.write(s.getBytes(CHARSET));
			}
		}

		// Header1
		String xml = getContent(new ByteArrayInputStream(s.getBytes(CHARSET)), "word/header1.xml");
		String expected = getContent(new ByteArrayInputStream(getBytes("result3.docx")), "word/header1.xml");
		Assert.assertEquals("merged docx of header1 does not match the expected result", xml, expected);

		// Footer1
		xml = getContent(new ByteArrayInputStream(s.getBytes(CHARSET)), "word/footer1.xml");
		expected = getContent(new ByteArrayInputStream(getBytes("result3.docx")), "word/footer1.xml");
		Assert.assertEquals("merged docx of footer1 does not match the expected result", xml, expected);
	}

	/**
	 * Test 3: Kopf- und Fusszeile
	 */
	@SuppressWarnings("unused")
	@Test
	public void testCellLineBreakProducesNoError() throws Exception {

		DOCXMergeEngine docxME = new DOCXMergeEngine("Test4");
		String s;
		try (InputStream is = this.getClass().getResourceAsStream("pagebreakDocvariable.docx")) {
			s = new String(docxME.getDocument(is, new RootMergeSource(true, true)), CHARSET);
		}

		if (false) { // switch to true to write the file
			try (FileOutputStream fos = new FileOutputStream("/tmp/resultPagebreakDocvariable.docx")) {
				fos.write(s.getBytes(CHARSET));
			}
		}

		String xml = getContent(new ByteArrayInputStream(s.getBytes(CHARSET)), "word/document.xml");
		String expected = getContent(new ByteArrayInputStream(getBytes("resultPagebreakDocvariable.docx")), "word/document.xml");
		Assert.assertEquals("merged docx does not match the expected result", xml, expected);

	}

	private static String getContent(InputStream input, final String zipEntryName) {

		try (ZipInputStream zipin = new ZipInputStream(input)) {
			ZipEntry ze;
			while ((ze = zipin.getNextEntry()) != null) {
				String zeName = ze.getName();
				if (zipEntryName.equals(zeName)) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					int b = zipin.read();
					while (b >= 0) {
						baos.write(b);
						b = zipin.read();
					}
					zipin.close();
					return new String(baos.toByteArray());
				}
			}
			zipin.close();
			return null;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private class MultithreadingTest extends Thread {

		private final int number;
		private final Semaphore semaphore;
		private final String expected;

		MultithreadingTest(int number, Semaphore semaphore, String expected) {

			this.number = number;
			this.semaphore = semaphore;
			this.expected = expected;
		}

		@Override
		public void run() {

			DOCXMergeEngine odtME = new DOCXMergeEngine("Test2." + this.number);
			try (InputStream is = this.getClass().getResourceAsStream("test1.odt")) {
				String s = new String(odtME.getDocument(is, new RootMergeSource(true, true)), CHARSET);
				is.close();
				String xml = getContent(new ByteArrayInputStream(s.getBytes(CHARSET)), "word/document.xml");
				Assert.assertEquals("merged odt does not match the expected result", xml, this.expected);
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				this.semaphore.release();
			}
		}
	}

	private byte[] getBytes(String filename) throws IOException {

		try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); InputStream is = this.getClass().getResourceAsStream(filename)) {
			byte[] buf = new byte[1024];
			int numRead = 0;
			while ((numRead = is.read(buf)) != -1) {
				baos.write(buf, 0, numRead);
				buf = new byte[1024];
			}
			return baos.toByteArray();
		}
	}

	private class RootMergeSource implements MergeSource {

		private final boolean condition, loop;

		/**
		 * @param condition
		 * @param loop
		 */
		public RootMergeSource(boolean condition, boolean loop) {

			this.condition = condition;
			this.loop = loop;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see ch.dvbern.rodos.merger.shared.MergeSource#getData(ch.dvbern.rodos .merger.shared.MergeContext,
		 * java.lang.String)
		 */
		@Override
		public Object getData(MergeContext ctx, String key) throws DocTemplateException {

			if ("TestWert".equals(key)) {
				return "Dies ist der Inhalt für die Variable 'TestWert' mit ein paar <heiklen> Inhalten (&amp;).";
			}
			if ("TestImage".equals(key)) {
				try {
					return new DocxImage(getBytes("test.image.png"), 362, 74, Image.Format.PNG);
				} catch (IOException e) {
					throw new DocTemplateException("invalid image source", e);
				}
			}
			if ("TestImage2".equals(key)) {
				try {
					return new DocxImage(getBytes("test.image.jpg"), 200, 251, Image.Format.JPEG);
				} catch (IOException e) {
					throw new DocTemplateException("invalid image source", e);
				}
			}
			if ("TextWithNewlines".equals(key)) {
				return "Zeile1\nZeile2\n\nZeile mit einer Leerzeile dazwischen";
			}
			if ("TextWithPagebreak".equals(key)) {
				return "Zeile vor Pagebreak\fZeile nach Pagebreak";
			}
			if (key.startsWith("pojo.")) {
				return new BeanMergeSource(new Pojo(), "pojo.").getData(ctx, key);
			}
			if (key.startsWith("ext.pojo.")) {
				return new ExtendedBeanMergeSource(new Pojo(), "ext.pojo.").getData(ctx, key);
			}
			return "[" + key + "]";
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see ch.dvbern.rodos.merger.shared.MergeSource#ifStatement(ch.dvbern.rodos .merger.shared.MergeContext,
		 * java.lang.String)
		 */
		@Override
		public Boolean ifStatement(MergeContext ctx, String key) throws DocTemplateException {

			if ("testcondition".equals(key)) {
				return new Boolean(this.condition);
			}
			if (key.startsWith("ext.pojo.")) {
				return new ExtendedBeanMergeSource(new Pojo(), "ext.pojo.").ifStatement(ctx, key);
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see ch.dvbern.rodos.merger.shared.MergeSource#whileStatement(ch.dvbern .rodos.merger.shared.MergeContext,
		 * java.lang.String)
		 */
		@Override
		public List<MergeSource> whileStatement(MergeContext ctx, String key) throws DocTemplateException {

			if ("testloop".equals(key)) {
				List<MergeSource> l = new ArrayList<>();
				if (this.loop) {
					Date d = new Date(1225272612227l);
					l.add(new DetailMergeSource("One.1", "One.2", "One.3", "2", d, new Long(123456), new Double(123456)));
					d = new Date(d.getTime() + 1000000000);
					l.add(new DetailMergeSource("Two.1", "Two.2", "Two.3", "10", d, new Double(54321.12345), new Double(54321.12345)));
					d = new Date(d.getTime() - 2000000000);
					l.add(new DetailMergeSource("Three.1", "Three.2", "Three.3", "10", d, new BigDecimal("-12121"), new Double(-12121)));
				}
				return l;
			}
			if (key.startsWith("pojo.")) {
				return new BeanMergeSource(new Pojo(), "pojo.").whileStatement(ctx, key);
			}
			return null;
		}
	}

	private class DetailMergeSource implements MergeSource {

		private final String col1, col2, col3, col4;
		private final Date col5;
		private final Number col6, col7;

		/**
		 * @param col1
		 * @param col2
		 * @param col3
		 * @param col4
		 * @param col5
		 * @param col6
		 * @param col7
		 */
		public DetailMergeSource(String col1, String col2, String col3, String col4, Date col5, Number col6, Number col7) {

			this.col1 = col1;
			this.col2 = col2;
			this.col3 = col3;
			this.col4 = col4;
			this.col5 = col5;
			this.col6 = col6;
			this.col7 = col7;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see ch.dvbern.rodos.merger.shared.MergeSource#getData(ch.dvbern.rodos .merger.shared.MergeContext,
		 * java.lang.String)
		 */
		@Override
		public Object getData(MergeContext ctx, String key) throws DocTemplateException {

			if ("Col1".equals(key)) {
				return this.col1;
			}
			if ("Col2".equals(key)) {
				return this.col2;
			}
			if ("Col3".equals(key)) {
				return this.col3;
			}
			if ("Col4".equals(key)) {
				return this.col4;
			}
			if ("Col5".equals(key)) {
				return this.col5;
			}
			if ("Col6".equals(key)) {
				return this.col6;
			}
			if ("Col7".equals(key)) {
				return this.col7;
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see ch.dvbern.rodos.merger.shared.MergeSource#ifStatement(ch.dvbern.rodos .merger.shared.MergeContext,
		 * java.lang.String)
		 */
		@Override
		public Boolean ifStatement(MergeContext ctx, String key) throws DocTemplateException {

			return Boolean.FALSE;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see ch.dvbern.rodos.merger.shared.MergeSource#whileStatement(ch.dvbern .rodos.merger.shared.MergeContext,
		 * java.lang.String)
		 */
		@Override
		public List<MergeSource> whileStatement(MergeContext ctx, String key) throws DocTemplateException {

			return null;
		}
	}

	public static class Pojo {

		private String text = "lorem ipsum blabla";
		private int ganzzahl = 123;
		private double fliesskommazahl = 123.123;
		private Date datum = new Date(1407998846974l);
		private List<Pojo> liste;
		private Konstante konstante = Konstante.CONST2;

		private Pojo() {
			this.liste = new ArrayList<>();
			this.liste.add(this);
			this.liste.add(new Pojo(456));
			this.liste.add(new Pojo(789));
		}

		private Pojo(int ganzzahl) {
			this.ganzzahl = ganzzahl;
		}

		public Date getDatum() {

			return this.datum;
		}

		public void setDatum(Date datum) {

			this.datum = datum;
		}

		public double getFliesskommazahl() {

			return this.fliesskommazahl;
		}

		public void setFliesskommazahl(double fliesskommazahl) {

			this.fliesskommazahl = fliesskommazahl;
		}

		public int getGanzzahl() {

			return this.ganzzahl;
		}

		public void setGanzzahl(int ganzzahl) {

			this.ganzzahl = ganzzahl;
		}

		public List<Pojo> getListe() {

			return this.liste;
		}

		public void setListe(List<Pojo> liste) {

			this.liste = liste;
		}

		public String getText() {

			return this.text;
		}

		public void setText(String text) {

			this.text = text;
		}

		public Konstante getKonstante() {
			return konstante;
		}

		public void setKonstante(Konstante konstante) {
			this.konstante = konstante;
		}
	}

	public static enum Konstante {
		CONST1, CONST2;
	}

}
