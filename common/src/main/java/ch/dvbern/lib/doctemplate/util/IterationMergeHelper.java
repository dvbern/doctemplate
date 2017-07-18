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

import java.util.Collections;
import java.util.List;

import ch.dvbern.lib.doctemplate.common.DocTemplateException;
import ch.dvbern.lib.doctemplate.common.IterationMergeSource;
import ch.dvbern.lib.doctemplate.common.MergeContext;
import ch.dvbern.lib.doctemplate.common.MergeSource;

/**
 * Hilfsklasse fuer die Implementierung von Iterationen.
 *
 * @author METH
 */
public class IterationMergeHelper {

	private static final String SUB_ITERATION_PATTERN_PREFIX = "_SUB";

	/**
	 * Erstellt eine {@link IterationMergeSource}-Instanz. Dabei werden Sortierungs- und Subiterationsdefinitionen
	 * beruecksichtigt.
	 *
	 * @param ctx aktueller Mergekontext
	 * @param mergeSource aktuelle Merge Source
	 * @param iterationKey key inkl. Subiterationsinformationen
	 * @param sortFieldKeys Sortierkriterien
	 * @return <code>null</code>, wenn unter <code>key</code> keine Daten geliefert werden
	 * @throws DocTemplateException
	 */
	public static IterationMergeSource getIterationMergeSource(MergeContext ctx, MergeSource mergeSource, String iterationKey, List<String> sortFieldKeys)
			throws DocTemplateException {

		String key = new String(iterationKey);
		// von-/bis-Bereichsangaben extrahieren ("_SUBvon]" bzw. "_SUBvon_bis")
		String vonBisBereich = null;
		int p = key.indexOf(SUB_ITERATION_PATTERN_PREFIX);
		if (p > 0) {
			vonBisBereich = key.substring(p);
			key = key.substring(0, p);
		}

		List<MergeSource> l = mergeSource.whileStatement(ctx, key);
		if (l != null) {
			if (sortFieldKeys != null) {
				// Sortierung innerhalb der Iteration bestimmen
				Collections.sort(l, new IterationMergeSource.IMSComparator(ctx, mergeSource, sortFieldKeys));
			}
			return new IterationMergeSource(l, vonBisBereich, mergeSource);
		}
		return null;
	}

}
