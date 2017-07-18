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

package ch.dvbern.lib.doctemplate.xml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ch.dvbern.lib.doctemplate.common.BeanMergeSource;
import ch.dvbern.lib.doctemplate.common.DocTemplateException;
import ch.dvbern.lib.doctemplate.xml.bean.AbsenderInkassostelle;
import ch.dvbern.lib.doctemplate.xml.bean.Adresse;
import ch.dvbern.lib.doctemplate.xml.bean.Bean;
import ch.dvbern.lib.doctemplate.xml.bean.BriefDatum;
import ch.dvbern.lib.doctemplate.xml.bean.Buchung;
import ch.dvbern.lib.doctemplate.xml.bean.DatenEntscheid;
import ch.dvbern.lib.doctemplate.xml.bean.Refernz;

public class XmlMergeEngineTest {

	private static final String CHARSET = "UTF-8";
	private static final String DATE_FORMAT = "dd.MM.yyyy";
	private Bean bean = new Bean();

	@Before
	public void createTestBean() throws ParseException, UnsupportedEncodingException {

		Locale.setDefault(new Locale("CH_de"));

		AbsenderInkassostelle absender = new AbsenderInkassostelle();
		absender.setEmail("info@3715.ch");
		absender.setTelefax("033 673 82 23");
		absender.setTelefon("033 673 82 23");

		absender.addAdresse(new Adresse("Steuerb?ro"));
		absender.addAdresse(new Adresse("Adelboden"));
		absender.addAdresse(new Adresse("Zelgstrasse 3"));
		absender.addAdresse(new Adresse("3715 Adelboden"));
		bean.setAbsender(absender);
		Refernz refernz = new Refernz();
		refernz.setFallLaufNummer("0002");
		refernz.setRegister("060");
		refernz.setSteuerJahr("2009");
		refernz.setZpvNummer("10000255");
		refernz.setGemeindeNummer("1101/0");
		bean.setRefernz(refernz);
		bean.addAdressaten(new Adresse("Pieren &amp; Co AG"));
		bean.addAdressaten(new Adresse("Alte Strasse 2"));
		bean.addAdressaten(new Adresse("3715 Adelboden"));
		bean.setBriefDatum(new BriefDatum("Adelboden", new SimpleDateFormat(DATE_FORMAT).parse("09.02.2012")));
		bean.setVerfuegendeGemeinde("Adelboden");
		AbsenderInkassostelle inkassostelle = new AbsenderInkassostelle();
		inkassostelle.setEmail("inkassotes1@fin.be.ch");
		inkassostelle.setTelefax("031 633 60 01");
		inkassostelle.setTelefon("031 633 94 02");
		inkassostelle.addAdresse(new Adresse("Inkassostelle Region Oberland"));
		inkassostelle.addAdresse(new Adresse("Allmendstrasse 18"));
		inkassostelle.addAdresse(new Adresse("3602 Thun"));
		bean.setInkassostelle(inkassostelle);
		DatenEntscheid datenEntscheid = new DatenEntscheid();
		datenEntscheid.setBankKonto("CH67 8082 0000 0035 7750 1, Raiffeisen, 3714, Frutigen");
		datenEntscheid.setBetragLst(1478.05f);
		datenEntscheid.setFaelligkeit(new SimpleDateFormat(DATE_FORMAT).parse("09.02.2012"));
		datenEntscheid.setSaldo("-2054.70");
		datenEntscheid.setSaldoInkasso("2054.70");
		datenEntscheid.setZahlungRueckzahlungFrist(30);
		datenEntscheid.addBuchung(new Buchung("Ihre Zahlung vom 31.12.2009", -3185.25f));
		datenEntscheid.addBuchung(new Buchung("Unsere R?ckzahlung vom 14.04.2010", 17.05f));
		datenEntscheid.addBuchung(new Buchung("Ihre Zahlung vom 07.12.2010", -59.30f));
		datenEntscheid.addBuchung(new Buchung("Ihre Zahlung vom 11.10.2011", -175.90f));
		datenEntscheid.addBuchung(new Buchung("Verg?tungszins", -129.35f));
		bean.setDatenEntscheid(datenEntscheid);
		bean.setEsrOhneForderungAdressaten(bean.getAdressaten());
	}

	@Test
	@Ignore
	public void test() throws DocTemplateException, IOException {

		InputStream is = this.getClass().getResourceAsStream("test.xml");
		Map<String, String> map = new HashMap<String, String>();
		map.put("_FMTSEP2DP", "_FMT####.00");
		XmlMergeEngine odtME = new XmlMergeEngine("bean", map);

		byte[] genXML = odtME.getXml(new BeanMergeSource(bean, ""), is);
		String xml = new String(genXML, CHARSET);
		is.close();

		// FileOutputStream fos = new FileOutputStream(
		// "/home/lsimon/workspaceLDT/dvbern-lib-doctemplate/xml-engine/src/test/resources/ch/dvbern/lib/doctemplate/xml/result.xml");
		// fos.write(xml.getBytes(CHARSET));
		// fos.close();

		// Assert.assertEquals("merged xml does not match the expected result", compareXml(new
		// ByteArrayInputStream(getBytes("test.result.xml")), new ByteArrayInputStream(xml.getBytes(CHARSET))));
		testXML(new String(getBytes("test.result.xml"), CHARSET), xml);
	}

	private void testXML(String xml1, String xml2) {

		// Zeilenumbrueche koennen je nach Plattform unterschiedlich sein
		xml1 = xml1.replaceAll("\n", "").replace("\r", "");
		xml2 = xml2.replaceAll("\n", "").replace("\r", "");
		Assert.assertEquals("merged xml does not match the expected result", xml1, xml2);
	}

	@Test
	@Ignore
	public void sortTest() throws DocTemplateException, IOException {

		InputStream is = this.getClass().getResourceAsStream("sort_test.xml");
		Map<String, String> map = new HashMap<String, String>();
		map.put("_FMTSEP2DP", "_FMT####.00");
		XmlMergeEngine odtME = new XmlMergeEngine("bean", map);

		byte[] genXML = odtME.getXml(new BeanMergeSource(bean, ""), is);
		String xml = new String(genXML, CHARSET);
		is.close();

		testXML(new String(getBytes("sort.test.result.xml"), CHARSET), xml);
	}

	@Test
	@Ignore
	public void sortTest2() throws DocTemplateException, IOException {

		testXML(new String(getBytes("sort.test.result.2.xml"), CHARSET), getGenXml("sort_test.2.xml"));
	}

	@Test
	@Ignore
	public void sortTest3() throws DocTemplateException, IOException {

		testXML(new String(getBytes("sort.test.result.3.xml"), CHARSET), getGenXml("sort_test.3.xml"));
	}

	@Test
	@Ignore
	public void attrTest() throws DocTemplateException, IOException {

		InputStream is = this.getClass().getResourceAsStream("attr_test.xml");
		Map<String, String> map = new HashMap<String, String>();
		map.put("_FMTSEP2DP", "_FMT####.00");
		XmlMergeEngine odtME = new XmlMergeEngine("bean", map);

		byte[] genXML = odtME.getXml(new BeanMergeSource(bean, ""), is);
		String xml = new String(genXML, CHARSET);
		is.close();
		testXML(new String(getBytes("attr.test.result.xml"), CHARSET), xml);
	}

	@Test
	@Ignore
	public void sortAttrTest() throws DocTemplateException, IOException {

		InputStream is = this.getClass().getResourceAsStream("sort_attr_test.xml");
		Map<String, String> map = new HashMap<String, String>();
		map.put("_FMTSEP2DP", "_FMT####.00");
		XmlMergeEngine odtME = new XmlMergeEngine("bean", map);

		byte[] genXML = odtME.getXml(new BeanMergeSource(bean, ""), is);
		String xml = new String(genXML, CHARSET);
		is.close();
		testXML(new String(getBytes("sort.attr.test.result.xml"), CHARSET), xml);
	}

	private String getGenXml(String fileName) throws DocTemplateException, IOException {

		InputStream is = this.getClass().getResourceAsStream(fileName);
		Map<String, String> map = new HashMap<String, String>();
		map.put("_FMTSEP2DP", "_FMT####.00");
		XmlMergeEngine odtME = new XmlMergeEngine("bean", map);
		float var = -12.25f;
		int counter = 1;
		float prevValue = 0;
		for (Buchung buchung : bean.getDatenEntscheid().getBuchungen()) {
			buchung.setBetragBuchungTest((var * counter++) * (counter % 2 == 1 ? -1 : 1));
			if ((counter % 2) == 0) {
				prevValue = buchung.getBetragBuchung();
			} else {
				buchung.setBetragBuchung(prevValue);
			}
		}
		byte[] genXML = odtME.getXml(new BeanMergeSource(bean, ""), is);
		is.close();
		return new String(genXML, CHARSET);
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

	@Test
	public void test2() throws Exception {

		String expected = new String(getBytes("test.result.xml"), CHARSET);
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

			try {
				Map<String, String> map = new HashMap<String, String>();
				map.put("_FMTSEP2DP", "_FMT####.00");
				XmlMergeEngine xmlME = new XmlMergeEngine("bean " + number, map);

				InputStream is = this.getClass().getResourceAsStream("test.xml");
				String s = new String(xmlME.getXml(new BeanMergeSource(bean, ""), is), CHARSET);
				is.close();
				Assert.assertEquals("merged xml does not match the expected result", s, expected);
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				semaphore.release();
			}
		}
	}

}
