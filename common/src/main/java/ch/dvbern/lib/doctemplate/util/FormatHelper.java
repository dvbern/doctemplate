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
package ch.dvbern.lib.doctemplate.util;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;

/**
 * Hilfsklasse fuer die Ausgabe von Ganz-, Fliesskommazahlen oder Datumswerten.
 *
 * @author METH
 */
public class FormatHelper {

	private static final DecimalFormat defaultIntDF = getDecimalFormat(LdtConstants.DEFAULT_INT_FORMAT, null);
	private static final DecimalFormat defaultFloatDF = getDecimalFormat(LdtConstants.DEFAULT_FLOAT_FORMAT, null);
	private static final SimpleDateFormat defaultSDF = new SimpleDateFormat(LdtConstants.DEFAULT_DATE_FORMAT);

	/**
	 * @param o das in ein {@link String} zu konvertierende Objekt
	 * @param formatPattern {@link SimpleDateFormat} oder {@link DecimalFormat} Pattern
	 * @return <code>o</code> als String
	 */
	public synchronized static String getDataAsString(Object o, String formatPattern) {

		if (o == null) {
			return "";
		}

		if (StringUtils.isNotBlank(formatPattern)) {

			if (o instanceof Number) {
				// Ausgabe einer Ganz- oder Fliesskommazahl
				Number n = (Number) o;
				DecimalFormat df = null;
				if (formatPattern != null && formatPattern.length() > 0) {
					// Die Sprache kann mittels eines Delimiter "_" im Pattern
					// erfasst werden
					// z.B. "#'##0.00_de"
					int i = formatPattern.indexOf("_");
					Locale locale = i > 0 ? getLocale(formatPattern.substring(i + 1)) : null;
					formatPattern = i > 0 ? formatPattern.substring(0, i) : formatPattern;
					df = getDecimalFormat(formatPattern, locale);
				}
				if (o instanceof Float || o instanceof Double || o instanceof BigDecimal) {
					if (df == null) {
						df = defaultFloatDF;
					}
					return df.format(n.doubleValue());
				}
				if (df == null) {
					df = defaultIntDF;
				}
				return df.format(n.longValue());
			} else if (o instanceof Date) {
				// Ausgabe eines Datums mit oder ohne Zeitangabe
				Date d = (Date) o;
				SimpleDateFormat sdf = null;
				if (formatPattern != null && formatPattern.length() > 0) {
					// Die Sprache kann mittels eines Delimiter "_" im Pattern
					// erfasst werden
					// z.B. "dd. MMMMM yyyy_de"
					int i = formatPattern.indexOf("_");
					Locale locale = i > 0 ? getLocale(formatPattern.substring(i + 1)) : null;
					formatPattern = i > 0 ? formatPattern.substring(0, i) : formatPattern;
					sdf = getSimpleDateFormat(formatPattern, locale);
				}
				if (sdf == null) {
					sdf = defaultSDF;
				}
				return sdf.format(d);
			} else if (o instanceof Boolean) {
				Boolean b = (Boolean) o;
				if (formatPattern != null && formatPattern.length() > 0) {
					String trueString;
					String falseString = "";
					int trueIndex = formatPattern.indexOf("_");
					if (trueIndex >= 0) {
						trueString = formatPattern.substring(0, trueIndex);
						falseString = formatPattern.substring(trueIndex + 1);
					} else {
						trueString = formatPattern;
					}
					if (b) {
						return trueString;
					}
					return falseString;
				}
			} else if (o instanceof String) {
				String str = (String) o;
				if (str.length() > 0 && formatPattern != null && formatPattern.length() > 0) {
					return String.format("%" + formatPattern + "s", str);
				}
				return str;
			} else if (o instanceof Collection) {
				String[] splitted = formatPattern.split(";");
				String separator = ", ";
				if (splitted.length > 1) {
					separator = splitted[1].substring(1, separator.length() - 1);
				}

				String result = ((Collection<?>) o).stream().map(r -> {
					try {
						Object value = PropertyUtils.getNestedProperty(o, splitted[0]);
						if (value != null) {
							return value.toString();
						}
					} catch (IllegalArgumentException | SecurityException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | IntrospectionException e) {
						LogFactory.getLog(FormatHelper.class).error("getDataAsString", e);
					}
					return null;
				}).filter(Objects::nonNull).collect(Collectors.joining(separator));

				if (splitted.length > 2) {
					int maxLen = Integer.parseInt(splitted[2]);
					if (maxLen > 3) {
						return StringUtils.abbreviate(result, maxLen);
					}
				}
				return result;
			}
		}
		return o.toString();
	}

	private static Locale getLocale(final String pattern) {

		if (pattern != null && pattern.length() > 0) {
			int j = pattern.indexOf("_");
			if (j > 0) {
				String country = pattern.substring(j + 1);
				return new Locale(pattern.substring(0, j), country);
			}
			return new Locale(pattern);
		}
		return null;
	}

	private static DecimalFormat getDecimalFormat(final String pattern, final Locale locale) {

		Locale i18n = locale == null ? getLocale() : locale;
		if (i18n != null) {
			return new DecimalFormat(pattern, new DecimalFormatSymbols(i18n));
		}
		return new DecimalFormat(pattern);
	}

	private static SimpleDateFormat getSimpleDateFormat(String pattern, Locale locale) {

		SimpleDateFormat result = null;
		if (locale == null) {
			locale = getLocale();
		}
		if (locale != null) {
			result = new SimpleDateFormat(pattern, locale);
		} else {
			result = new SimpleDateFormat(pattern);
		}
		return result;
	}

	private static Locale getLocale() {

		Locale result = null;
		String localeOverwriteVMArg = System.getProperty("default.locale.overwrite");
		if (localeOverwriteVMArg != null) {
			StringTokenizer st = new StringTokenizer(localeOverwriteVMArg, "_");
			if (st.countTokens() == 2) {
				result = new Locale(st.nextToken(), st.nextToken());
			}
			if (st.countTokens() == 1) {
				result = new Locale(st.nextToken());
			}
		}
		return result;
	}

}
