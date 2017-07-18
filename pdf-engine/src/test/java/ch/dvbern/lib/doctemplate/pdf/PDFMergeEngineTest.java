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
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ch.dvbern.lib.doctemplate.common.BeanMergeSource;
import ch.dvbern.lib.doctemplate.common.DocTemplateException;
import ch.dvbern.lib.doctemplate.common.Image;
import ch.dvbern.lib.doctemplate.common.MergeContext;
import ch.dvbern.lib.doctemplate.common.MergeSource;
import junit.framework.Assert;

/**
 * @author METH
 */
public class PDFMergeEngineTest {

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
	@Ignore
	public void test1() throws Exception {

		PDFMergeEngine pdfME = new PDFMergeEngine("Test1");

		InputStream is = this.getClass().getResourceAsStream("test1.pdf");
		String s = new String(pdfME.getDocument(is, new RootMergeSource(true, true)));
		is.close();

		// FileOutputStream fos = new FileOutputStream("/home/meth/Desktop/test1.result.pdf");
		// fos.write(s.getBytes());
		// fos.close();
		Assert.assertTrue(isEqualPdf(s, "test1.result.pdf"));
	}

	/**
	 * Test 2: Mehrfachaufruf multithreaded
	 *
	 * @throws Exception
	 */
	@Test
	@Ignore
	public void test2() throws Exception {

		ExecutorService executor = Executors.newFixedThreadPool(10);
		List<Future<Long>> list = new ArrayList<Future<Long>>();
		for (int i = 0; i < 10; i++) {
			Callable<Long> worker = new Hundertfach();
			Future<Long> submit = executor.submit(worker);
			list.add(submit);
		}
		long calls = 0;
		for (Future<Long> future : list) {
			calls += future.get();
		}
		Assert.assertEquals(100, calls);
	}

	private class Hundertfach implements Callable<Long> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.concurrent.Callable#call()
		 */
		public Long call() throws Exception {

			long l = 0;
			for (; l < 10; l++) {
				test1();
			}
			return Long.valueOf(l);
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

	/**
	 * Test 3: Merge mit BeanMergeSource
	 *
	 * @throws Exception
	 */
	@Test
	@Ignore
	public void test3() throws Exception {

		PDFMergeEngine pdfME = new PDFMergeEngine("Test1");

		InputStream is = this.getClass().getResourceAsStream("test3.pdf");
		String s = new String(pdfME.getDocument(is, new BeanMergeSource(new SomeJavaBean(), "bean.")));
		is.close();

		// FileOutputStream fos = new FileOutputStream("/home/meth/Desktop/test3.result.pdf");
		// fos.write(s.getBytes());
		// fos.close();
		Assert.assertTrue(isEqualPdf(s, "test3.result.pdf"));
	}

	private boolean isEqualPdf(String s, String resultFile) throws IOException {

		return testSafePdf(s).equals(testSafePdf(new String(getBytes(resultFile))));
	}

	private String testSafePdf(String s) {

		StringBuffer sb = new StringBuffer(s);
		int i = sb.indexOf("<</Creator");
		sb.replace(i, sb.indexOf("endobj", i), "");
		i = sb.indexOf("<</Root");
		sb.replace(i, sb.length(), "");
		return sb.toString();
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
		 * @see ch.dvbern.rodos.merger.shared.MergeSource#getData(ch.dvbern.rodos.merger.shared.MergeContext,
		 * java.lang.String)
		 */
		public Object getData(MergeContext ctx, String key) throws DocTemplateException {

			if ("QST_name".equals(key)) {
				return "Meister";
			}
			if ("QST_vorname".equals(key)) {
				return "Thomas";
			}
			if ("QST_plz".equals(key)) {
				return "3250";
			}
			if ("QST_ort".equals(key)) {
				return "Lyss";
			}
			if ("TestImage.PNG".equals(key)) {
				try {
					return new Image(getBytes("test.image.png"), 362, 74, Image.Format.PNG);
				} catch (IOException e) {
					throw new DocTemplateException("invalid image source", e);
				}
			}
			if ("TestImage.JPG".equals(key)) {
				try {
					return new Image(getBytes("test.image.jpg"), 362, 74, Image.Format.JPEG);
				} catch (IOException e) {
					throw new DocTemplateException("invalid image source", e);
				}
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see ch.dvbern.rodos.merger.shared.MergeSource#ifStatement(ch.dvbern.rodos.merger.shared.MergeContext,
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
		 * @see ch.dvbern.rodos.merger.shared.MergeSource#whileStatement(ch.dvbern.rodos.merger.shared.MergeContext,
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
		 * @see ch.dvbern.rodos.merger.shared.MergeSource#getData(ch.dvbern.rodos.merger.shared.MergeContext,
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
		 * @see ch.dvbern.rodos.merger.shared.MergeSource#ifStatement(ch.dvbern.rodos.merger.shared.MergeContext,
		 * java.lang.String)
		 */
		public Boolean ifStatement(MergeContext ctx, String key) throws DocTemplateException {

			return Boolean.FALSE;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see ch.dvbern.rodos.merger.shared.MergeSource#whileStatement(ch.dvbern.rodos.merger.shared.MergeContext,
		 * java.lang.String)
		 */
		public List<MergeSource> whileStatement(MergeContext ctx, String key) throws DocTemplateException {

			return null;
		}
	}

	/**
	 * Simple POJO
	 */
	public class SomeJavaBean extends Date {

		private static final long serialVersionUID = 1L;

		private final String attrib1 = "Value of Attrib1", attrib2 = "Value of Attrib2 הצüיט";
		private final Integer attrib3 = 3, attrib4 = 4444;
		private SomeJavaBean attrib5 = null;
		private final List<SomeJavaBean> attrib6 = new LinkedList<SomeJavaBean>();

		SomeJavaBean() {
			super(123456789);
			attrib5 = this;
			attrib6.add(this);
			attrib6.add(this);
			attrib6.add(this);
		}

		/** @return */
		public String getAttrib1() {

			return attrib1;
		}

		/** @return */
		public String getAttrib2() {

			return attrib2;
		}

		/** @return */
		public Integer getAttrib3() {

			return attrib3;
		}

		/** @return */
		public Integer getAttrib4() {

			return attrib4;
		}

		/** @return */
		public SomeJavaBean getAttrib5() {

			return attrib5;
		}

		/** @return */
		public List<SomeJavaBean> getAttrib6() {

			return attrib6;
		}

		/** @return */
		public boolean isGreat() {

			return true;
		}
	}

}
