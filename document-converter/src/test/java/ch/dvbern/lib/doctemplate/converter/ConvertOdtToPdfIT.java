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
package ch.dvbern.lib.doctemplate.converter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Semaphore;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author lsimon
 */
public class ConvertOdtToPdfIT {

	private static final String PATH_TO_ODT = "/ch/dvbern/lib/doctemplate/odt/result.odt";
	private static final int MAX_THREAD_COUNT = 100000;

	private byte[] input;
	private byte[] expected;
	private volatile int counter = 0;

	/**
	 * @throws IOException
	 */
	@Before
	public void init() throws IOException {
		input = getBytes(PATH_TO_ODT);
		expected = getBytes("/ch/dvbern/lib/doctemplate/converter/test_lo.pdf");
	}

	/**
	 * @throws Throwable
	 * @throws Exception
	 */
	@Test
	public void test1() throws Throwable {
		DocumentConverter converter = DocumentConverter.createConnectImmediately();
		ByteArrayOutputStream baous = null;
		try {
			baous = converter.convertToPdf(new ByteArrayInputStream(input));
			// FileOutputStream fos = new FileOutputStream(
			// "/home/lsimon/workspace2/dvbern-lib-doctemplate/document-converter/src/test/resources/ch/dvbern/lib/doctemplate/converter/test_lo.pdf");
			// fos.write(baous.toByteArray());
			// fos.close();
			Assert.assertEquals(expected.length, baous.size());
			Assert.assertTrue(isEqualPdf(new String(expected), new String(baous.toByteArray())));
		} catch (Exception e) {
			dumpToFile(baous.toByteArray(), "test_lo_f.pdf");
			throw e;
		} finally {
			if (baous != null) {
				baous.close();
			}
			if (converter != null) {
				converter.disconnect();
			}
		}
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void test2() throws Exception {
		Semaphore semaphore = new Semaphore(100, true);
		for (int i = 0; i < MAX_THREAD_COUNT; i++) {
			new MultithreadingTest(i, semaphore, this).start();
			if (i > 0 && i % 100 == 0) {
				synchronized (this) {
					while (counter != i) {
						wait();
					}
				}
			}
		}
		synchronized (this) {
			while (counter != MAX_THREAD_COUNT) {
				wait();
			}
		}
	}

	private boolean isEqualPdf(String s, String resultFile) {
		s = testSafePdf(s);
		resultFile = testSafePdf(resultFile);
		return s.equals(resultFile);
	}

	private String testSafePdf(String s) {

		StringBuffer sb = new StringBuffer(s);
		int i = sb.indexOf("13 0 obj");
		sb.replace(i, sb.indexOf("startxref", i), "");
		return sb.toString();
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

	private class MultithreadingTest extends Thread {

		private final int number;
		private final Semaphore semaphore;
		private final ConvertOdtToPdfIT c_lo;

		MultithreadingTest(int number, Semaphore semaphore, ConvertOdtToPdfIT c_lo) {
			this.number = number;
			this.semaphore = semaphore;
			this.c_lo = c_lo;
		}

		@Override
		public void run() {
			ByteArrayOutputStream baous = null;
			DocumentConverter converter = null;
			try {
				semaphore.acquire();
				converter = DocumentConverter.createConnectImmediately();
				baous = converter.convertToPdf(new ByteArrayInputStream(input));
				Assert.assertEquals(expected.length, baous.size());
				Assert.assertTrue(isEqualPdf(new String(expected), new String(baous.toByteArray())));
				System.out.println(number + ". successfully finished");
			} catch (Exception e1) {
				e1.printStackTrace();
			} catch (AssertionError e) {
				dumpToFile(baous.toByteArray(), "test_lo_" + number + ".pdf");
				throw e;
			} finally {
				synchronized (c_lo) {
					c_lo.inc();
					c_lo.notifyAll();
				}
				semaphore.release();
				if (baous != null) {
					try {
						baous.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (converter != null) {
					converter.disconnect();
				}
			}
		}
	}

	private void dumpToFile(byte[] in, String filename) {
		OutputStream fos = null;
		try {
			fos = new FileOutputStream(new File(System.getProperty("java.io.tmpdir", filename)));
			InputStream ins = new ByteArrayInputStream(in);
			IOUtils.copy(ins, fos);
			IOUtils.closeQuietly(ins);
			IOUtils.closeQuietly(fos);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public synchronized void inc() {
		counter++;
	}
}
