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

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import ch.dvbern.lib.doctemplate.util.FormatHelper;
import junit.framework.Assert;

/**
 * @author WAMA
 */
public class FormatHelperTest extends Assert {

	/**
	 * Initialize Test
	 */
	@Before
	public void doBefore() {

		Locale.setDefault(new Locale("en", "GB"));
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testDate() throws Exception {

		Date date = newDate(1, 1, 2009);
		assertEquals("Format Datum stimmt nicht", "01. January 2009", FormatHelper.getDataAsString(date, "dd. MMMMM yyyy"));
		assertEquals("Format Datum stimmt nicht: nur _", "01. January 2009", FormatHelper.getDataAsString(date, "dd. MMMMM yyyy_"));
		assertEquals("Format Datum stimmt nicht: Schrott", "01. January 2009", FormatHelper.getDataAsString(date, "dd. MMMMM yyyy_schrott"));
		assertEquals("Format Datum stimmt nicht: f", "01. January 2009", FormatHelper.getDataAsString(date, "dd. MMMMM yyyy_f"));

		// Franz
		assertEquals("Format Datum stimmt nicht: Franz klein", "01. janvier 2009", FormatHelper.getDataAsString(date, "dd. MMMMM yyyy_fr"));
		assertEquals("Format Datum stimmt nicht: Franz Grossklein", "01. janvier 2009", FormatHelper.getDataAsString(date, "dd. MMMMM yyyy_FR"));
		assertEquals("Format Datum stimmt nicht: Franz klein", "01. janvier 2009", FormatHelper.getDataAsString(date, "dd. MMMMM yyyy_fr_"));
		assertEquals("Format Datum stimmt nicht: Franz klein", "01. janvier 2009", FormatHelper.getDataAsString(date, "dd. MMMMM yyyy_fr_CH"));
		assertEquals("Format Datum stimmt nicht: Franz klein", "le 01. janvier 2009", FormatHelper.getDataAsString(date, "'le 'dd. MMMMM yyyy_fr"));

		// Italy
		assertEquals("Format Datum stimmt nicht: Italy klein", "01. gennaio 2009", FormatHelper.getDataAsString(date, "dd. MMMMM yyyy_it"));

		// Standard format
		assertEquals("Format Datum stimmt nicht: Standard", "01.01.2009", FormatHelper.getDataAsString(date, "dd.MM.yyyy"));
		assertEquals("Format Datum stimmt nicht: Standard mit _", "01.01.2009", FormatHelper.getDataAsString(date, "dd.MM.yyyy_"));
		assertEquals("Format Datum stimmt nicht: Standard fr", "01.01.2009", FormatHelper.getDataAsString(date, "dd.MM.yyyy_fr"));

	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testBoolean() throws Exception {

		// Ohne Pattern
		assertEquals("Format Boolean stimmt nicht: null", "", FormatHelper.getDataAsString(null, null));
		assertEquals("Format Boolean stimmt nicht: Treu", "true", FormatHelper.getDataAsString(Boolean.TRUE, null));
		assertEquals("Format Boolean stimmt nicht: False", "false", FormatHelper.getDataAsString(Boolean.FALSE, null));

		// Mit Pattern
		assertEquals("Format Boolean stimmt nicht: null", "", FormatHelper.getDataAsString(null, "Ja_Nein"));

		// Mit True
		assertEquals("Format Boolean stimmt nicht: Treu", "Ja", FormatHelper.getDataAsString(Boolean.TRUE, "Ja_Nein"));
		assertEquals("Format Boolean stimmt nicht: Treu", "Ja", FormatHelper.getDataAsString(Boolean.TRUE, "Ja_"));
		assertEquals("Format Boolean stimmt nicht: Treu", "Ja", FormatHelper.getDataAsString(Boolean.TRUE, "Ja"));

		// Mit False
		assertEquals("Format Boolean stimmt nicht: False", "Nein", FormatHelper.getDataAsString(Boolean.FALSE, "Ja_Nein"));
		assertEquals("Format Boolean stimmt nicht: False", "Nein", FormatHelper.getDataAsString(Boolean.FALSE, "_Nein"));
		assertEquals("Format Boolean stimmt nicht: False", "", FormatHelper.getDataAsString(Boolean.FALSE, "_"));
	}

	@Test
	public void testFormatSuffixWithString() throws Exception {
		assertEquals("Formatierung mit Pattern 0000 stimmt nicht","", FormatHelper.getDataAsString("", "0000"));
		assertEquals("Formatierung mit Pattern 0000 stimmt nicht","0007", FormatHelper.getDataAsString(7, "0000"));
		assertEquals("Formatierung mit Pattern 0000 stimmt nicht","2017", FormatHelper.getDataAsString(2017, "0000"));
	}

	private Date newDate(int day, int month, int year) {

		Calendar calendar = Calendar.getInstance();
		calendar.setLenient(false);
		calendar.clear();
		if (year == 0) {
			calendar.set(1, month - 1, day);
		} else {
			calendar.set(year, month - 1, day);
		}
		return calendar.getTime();
	}

}
