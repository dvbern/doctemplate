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
package ch.dvbern.lib.doctemplate.common;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lsimon
 */
public class BasicMergeElement {

	private List<BasicMergeElement> mergeElements;

	/**
	 * Hinzufuegen eines weiteren MergeElements.
	 *
	 * @param mergeElement
	 */
	public void addMergeElement(BasicMergeElement mergeElement) {

		if (this.mergeElements == null) {
			this.mergeElements = new ArrayList<>();
		}
		this.mergeElements.add(mergeElement);
	}

	/**
	 * Liefert das Resultat eines Merge-Vorgangs.
	 *
	 * @param ctx Kontext zum Austauschen von Informationen zwischen MergeSourcen
	 * @param mergeSource Informationsquelle
	 * @return Zeichenkette als Resultat des Merge-Vorgangs.
	 * @throws DocTemplateException
	 */
	public String getContent(MergeContext ctx, MergeSource mergeSource) throws DocTemplateException {

		StringBuffer result = new StringBuffer();
		for (BasicMergeElement bme : this.mergeElements) {
			result.append(bme.getContent(ctx, mergeSource));
		}
		return result.toString();
	}

	/**
	 * Liefert das Resultat eines Merge-Vorgangs.
	 *
	 * @param ctx Kontext zum Austauschen von Informationen zwischen MergeSourcen
	 * @param mergeSource Informationsquelle
	 * @param output resulting document output stream
	 * @throws DocTemplateException
	 */
	public void getContent(MergeContext ctx, MergeSource mergeSource, OutputStream output) throws DocTemplateException {

		if (this.mergeElements != null) {
			for (BasicMergeElement bme : this.mergeElements) {
				bme.getContent(ctx, mergeSource, output);
			}
		}
	}

}
