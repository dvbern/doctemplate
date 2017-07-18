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
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Semaphore;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ch.dvbern.lib.doctemplate.common.BeanMergeSource;

/**
 * @author METH
 */
public class RTFMergeEngineAdditionalTest {

	/**
	 * Sets the default locale.
	 */
	@Before
	public void setLocale() {

		Locale.setDefault(new Locale("de", "CH"));
	}

	/**
	 * Test 10: BeanMergeSource.
	 *
	 * @throws Exception
	 */
	@Test
	@Ignore
	public void test10() throws Exception {

		RTFMergeEngine rtfME = new RTFMergeEngine("Test10");
		InputStream is = this.getClass().getResourceAsStream("test10.rtf");
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String s = new String(rtfME.getDocument(br, new BeanMergeSource(new SomeJavaBean(), "altPrefix_")));
		is.close();

		// FileOutputStream fos = new FileOutputStream("/temp/Result.rtf");
		// fos.write(s.getBytes());
		// fos.close();

		assert s.equals(new String(getBytes("test.result.10.rtf"))) : "merged rtf does not match the expected result";
	}

	/**
	 * Test 10: BeanMergeSource.
	 *
	 * @throws Exception
	 */
	@Test
	public void test10_100() throws Exception {

		Semaphore semaphore = new Semaphore(100, true);
		for (int i = 0; i < 100; i++) {
			semaphore.acquire();
			new Test10Multithreaded(i, semaphore).start();
		}
		semaphore.acquire(100);
	}

	private class Test10Multithreaded extends Thread {

		int number;
		Semaphore semaphore;

		Test10Multithreaded(int number, Semaphore semaphore) {

			this.number = number;
			this.semaphore = semaphore;
		}

		@Override
		public void run() {

			RTFMergeEngine rtfME = new RTFMergeEngine("Test10." + number);
			try {
				InputStream is = this.getClass().getResourceAsStream("test10.rtf");
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				rtfME.getDocument(br, new BeanMergeSource(new SomeJavaBean(), "altPrefix_"));
				is.close();
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

	/**
	 * Simple POJO
	 */
	public class SomeJavaBean extends Date {

		private static final long serialVersionUID = 1L;

		private final String attrib1 = "Value of Attrib1", attrib2 = "Value of Attrib2";
		private final Integer attrib3 = 3, attrib4 = 4;
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
