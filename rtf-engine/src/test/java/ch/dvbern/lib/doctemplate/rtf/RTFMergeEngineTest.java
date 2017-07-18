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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Semaphore;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ch.dvbern.lib.doctemplate.common.DocTemplateException;
import ch.dvbern.lib.doctemplate.common.Image;
import ch.dvbern.lib.doctemplate.common.MergeContext;
import ch.dvbern.lib.doctemplate.common.MergeSource;

/**
 * @author METH
 */
public class RTFMergeEngineTest {

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

		RTFMergeEngine rtfME = new RTFMergeEngine("Test1");

		InputStream is = this.getClass().getResourceAsStream("test.rtf");
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String s = new String(rtfME.getDocument(br, new RootMergeSource(true, false)));
		is.close();

		// FileOutputStream fos = new FileOutputStream("/home/meth/Desktop/Result.rtf");
		// fos.write(s.getBytes());
		// fos.close();
		assert s.equals(new String(getBytes("test.result.1.rtf"))) : "merged rtf does not match the expected result";
	}

	/**
	 * Test 2: Textbaustein wird nicht ausgeblendet, Liste mit Inhalt.
	 *
	 * @throws Exception
	 */
	@Test
	public void test2() throws Exception {

		RTFMergeEngine rtfME = new RTFMergeEngine("Test2");

		InputStream is = this.getClass().getResourceAsStream("test.rtf");
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String s = new String(rtfME.getDocument(br, new RootMergeSource(true, true)));
		is.close();

		assert s.equals(new String(getBytes("test.result.2.rtf"))) : "merged rtf does not match the expected result";
	}

	/**
	 * Test 3: Textbaustein wird ausgeblendet.
	 *
	 * @throws Exception
	 */
	@Test
	public void test3() throws Exception {

		RTFMergeEngine rtfME = new RTFMergeEngine("Test3");

		InputStream is = this.getClass().getResourceAsStream("test.rtf");
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String s = new String(rtfME.getDocument(br, new RootMergeSource(false, false)));
		is.close();

		assert s.equals(new String(getBytes("test.result.3.rtf"))) : "merged rtf does not match the expected result";
	}

	/**
	 * Test 4: Multithreading-Test.
	 *
	 * @throws Exception
	 */
	@Test
	public void test4() throws Exception {

		Semaphore semaphore = new Semaphore(100, true);
		for (int i = 0; i < 100; i++) {
			semaphore.acquire();
			new MultithreadingTest(i, semaphore).start();
		}
		semaphore.acquire(100);
	}

	private class MultithreadingTest extends Thread {

		int number;
		Semaphore semaphore;

		MultithreadingTest(int number, Semaphore semaphore) {

			this.number = number;
			this.semaphore = semaphore;
		}

		@Override
		public void run() {

			RTFMergeEngine rtfME = new RTFMergeEngine("Test4." + number);
			try {
				InputStream is = this.getClass().getResourceAsStream("test.rtf");
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				String s = new String(rtfME.getDocument(br, new RootMergeSource(false, false)));
				is.close();
				assert s.equals(new String(getBytes("test.result.3.rtf"))) : "merged rtf does not match the expected result";
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				semaphore.release();
			}
		}
	}

	/**
	 * Test 5: Iteration mit NEXT.
	 *
	 * @throws Exception
	 */
	@Test
	public void test5() throws Exception {

		RTFMergeEngine rtfME = new RTFMergeEngine("Test5");

		InputStream is = this.getClass().getResourceAsStream("test5.rtf");
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String s = new String(rtfME.getDocument(br, new RootMergeSource(true, true)));
		is.close();

		assert s.equals(new String(getBytes("test.result.5.rtf"))) : "merged rtf does not match the expected result";
	}

	/**
	 * Test 6: Sortierung innerhalb Iteration.
	 *
	 * @throws Exception
	 */
	@Test
	public void test6() throws Exception {

		RTFMergeEngine rtfME = new RTFMergeEngine("Test6");

		InputStream is = this.getClass().getResourceAsStream("test6.rtf");
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String s = new String(rtfME.getDocument(br, new RootMergeSource(true, true)));
		is.close();

		assert s.equals(new String(getBytes("test.result.6.rtf"))) : "merged rtf does not match the expected result";
	}

	/**
	 * Test 7: Sortierung DESC innerhalb Iteration.
	 *
	 * @throws Exception
	 */
	@Test
	public void test7() throws Exception {

		RTFMergeEngine rtfME = new RTFMergeEngine("Test7");

		InputStream is = this.getClass().getResourceAsStream("test7.rtf");
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String s = new String(rtfME.getDocument(br, new RootMergeSource(true, true)));
		is.close();

		assert s.equals(new String(getBytes("test.result.7.rtf"))) : "merged rtf does not match the expected result";
	}

	/**
	 * Test 8: Sortierung innerhalb Iteration mit Datums- und Zahlenkolonnen inkl. Formatierung.
	 *
	 * @throws Exception
	 */
	@Test
	@Ignore
	public void test8() throws Exception {

		RTFMergeEngine rtfME = new RTFMergeEngine("Test8");

		InputStream is = this.getClass().getResourceAsStream("test8.rtf");
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String s = new String(rtfME.getDocument(br, new RootMergeSource(true, true)));
		is.close();

		assert s.equals(new String(getBytes("test.result.8.rtf"))) : "merged rtf does not match the expected result";
	}

	/**
	 * Test 9: Bild einfuegen.
	 *
	 * @throws Exception
	 */
	@Test
	public void test9() throws Exception {

		RTFMergeEngine rtfME = new RTFMergeEngine("Test9");

		InputStream is = this.getClass().getResourceAsStream("test9.rtf");
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String s = new String(rtfME.getDocument(br, new RootMergeSource(true, true)));
		is.close();

		// FileOutputStream fos = new FileOutputStream("/temp/Result.rtf");
		// fos.write(s.getBytes());
		// fos.close();

		assert s.equals(new String(getBytes("test.result.9.rtf"))) : "merged rtf does not match the expected result";
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
		 * @see ch.dvbern.rodos.merger.shared.MergeSource#getData(ch.dvbern.rodos.merger.shared.MergeContext,
		 * java.lang.String)
		 */
		public Object getData(MergeContext ctx, String key) throws DocTemplateException {

			if ("TestWert".equals(key)) {
				return "Dies ist der Inhalt fuer die Variable 'TestWert'.";
			}
			if ("TestImage".equals(key)) {
				try {
					return new Image(getBytes("test.image.png"), 362, 74, Image.Format.PNG);
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

}
