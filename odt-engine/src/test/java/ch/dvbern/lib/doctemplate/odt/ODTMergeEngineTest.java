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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.dvbern.lib.doctemplate.common.DocTemplateException;
import ch.dvbern.lib.doctemplate.common.Image;
import ch.dvbern.lib.doctemplate.common.MergeContext;
import ch.dvbern.lib.doctemplate.common.MergeSource;

/**
 * @author METH
 */
public class ODTMergeEngineTest {

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
	 *
	 * @throws Exception
	 */
	@Test
	public void test1() throws Exception {

		ODTMergeEngine odtME = new ODTMergeEngine("Test1");

		InputStream is = this.getClass().getResourceAsStream("test1.odt");
		String s = new String(odtME.getDocument(is, new RootMergeSource(true, true)), CHARSET);
		is.close();

		// FileOutputStream fos = new FileOutputStream(
		// "/home/lsimon/workspaceLDT/dvbern-lib-doctemplate/odt-engine/src/test/resources/ch/dvbern/lib/doctemplate/odt/result.odt");
		// fos.write(s.getBytes(CHARSET));
		// fos.close();
		// fos = new FileOutputStream(
		// "/home/lsimon/workspaceLDT/dvbern-lib-doctemplate/odt-engine/src/test/resources/ch/dvbern/lib/doctemplate/odt/result2.xml");
		String xmlContent = getContent(new ByteArrayInputStream(s.getBytes(CHARSET)));
		// byte[] xml = xmlContent.getBytes();
		// fos.write(xml);
		// fos.close();

		String expected = new String(getBytes("result1.xml"));
		Assert.assertEquals("merged odt does not match the expected result", xmlContent, expected);
	}

	private static String getContent(InputStream input) {

		try {
			ZipEntry ze;
			ZipInputStream zipin = new ZipInputStream(input);
			while ((ze = zipin.getNextEntry()) != null) {
				String zeName = ze.getName();
				if ("content.xml".equals(zeName)) {
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

	/**
	 * Test 4: Multithreading-Test.
	 *
	 * @throws Exception
	 */
	@Test
	public void test2() throws Exception {

		String expected = new String(getBytes("result1.xml"));

		Semaphore semaphore = new Semaphore(100, true);
		for (int i = 0; i < 100; i++) {
			semaphore.acquire();
			new MultithreadingTest(i, semaphore, expected).start();
		}
		semaphore.acquire(100);
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

			ODTMergeEngine odtME = new ODTMergeEngine("Test2." + number);
			try {
				InputStream is = this.getClass().getResourceAsStream("test1.odt");
				String s = new String(odtME.getDocument(is, new RootMergeSource(true, true)), CHARSET);
				is.close();
				String xml = getContent(new ByteArrayInputStream(s.getBytes(CHARSET)));
				Assert.assertEquals("merged odt does not match the expected result", xml, expected);
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				semaphore.release();
			}
		}
	}

	private byte[] getBytes(String filename) throws IOException {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		InputStream is = this.getClass().getResourceAsStream(filename);
		byte[] buf = new byte[1024];
		int numRead = 0;
		while ((numRead = is.read(buf)) != -1) {
			baos.write(buf, 0, numRead);
			buf = new byte[1024];
		}
		return baos.toByteArray();
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
		public Object getData(MergeContext ctx, String key) throws DocTemplateException {

			if ("TestWert".equals(key)) {
				return "Dies ist der Inhalt für die Variable 'TestWert' mit ein paar <heiklen> Inhalten (&).";
			}
			if ("TestImage".equals(key)) {
				try {
					return new Image(getBytes("test.image.png"), 362, 74, Image.Format.PNG);
				} catch (IOException e) {
					throw new DocTemplateException("invalid image source", e);
				}
			}
			if ("TestImage2".equals(key)) {
				try {
					return new Image(getBytes("test.image.jpg"), 200, 251, Image.Format.JPEG);
				} catch (IOException e) {
					throw new DocTemplateException("invalid image source", e);
				}
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see ch.dvbern.rodos.merger.shared.MergeSource#ifStatement(ch.dvbern.rodos .merger.shared.MergeContext,
		 * java.lang.String)
		 */
		public Boolean ifStatement(MergeContext ctx, String key) throws DocTemplateException {

			if ("testcondition".equals(key)) {
				return new Boolean(condition);
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see ch.dvbern.rodos.merger.shared.MergeSource#whileStatement(ch.dvbern .rodos.merger.shared.MergeContext,
		 * java.lang.String)
		 */
		public List<MergeSource> whileStatement(MergeContext ctx, String key) throws DocTemplateException {

			if ("testloop".equals(key)) {
				List<MergeSource> l = new ArrayList<MergeSource>();
				if (loop) {
					Date d = new Date(1225272612227l);
					l.add(new DetailMergeSource("One.1", "One.2", "One.3", "2", d, new Long(123456), new Double(123456)));
					d = new Date(d.getTime() + 1000000000);
					l.add(new DetailMergeSource("Two.1", "Two.2", "Two.3", "10", d, new Double(54321.12345), new Double(54321.12345)));
					d = new Date(d.getTime() - 2000000000);
					l.add(new DetailMergeSource("Three.1", "Three.2", "Three.3", "10", d, new BigDecimal("-12121"), new Double(-12121)));
				}
				return l;
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
		public Object getData(MergeContext ctx, String key) throws DocTemplateException {

			if ("Col1".equals(key)) {
				return col1;
			}
			if ("Col2".equals(key)) {
				return col2;
			}
			if ("Col3".equals(key)) {
				return col3;
			}
			if ("Col4".equals(key)) {
				return col4;
			}
			if ("Col5".equals(key)) {
				return col5;
			}
			if ("Col6".equals(key)) {
				return col6;
			}
			if ("Col7".equals(key)) {
				return col7;
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see ch.dvbern.rodos.merger.shared.MergeSource#ifStatement(ch.dvbern.rodos .merger.shared.MergeContext,
		 * java.lang.String)
		 */
		public Boolean ifStatement(MergeContext ctx, String key) throws DocTemplateException {

			return Boolean.FALSE;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see ch.dvbern.rodos.merger.shared.MergeSource#whileStatement(ch.dvbern .rodos.merger.shared.MergeContext,
		 * java.lang.String)
		 */
		public List<MergeSource> whileStatement(MergeContext ctx, String key) throws DocTemplateException {

			return null;
		}
	}

}
