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
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ch.dvbern.lib.doctemplate.util.IterationMergeHelper;

/**
 * @author lsimon
 */
public class IterationMergeElement extends AbstractMergeElement {

	private static final Log log = LogFactory.getLog(IterationMergeElement.class);
	private List<String> sortFieldKeys;

	/**
	 * Konstruktor eines MergeElements, dessen Ausgabe iterativ erfolgen kann.
	 *
	 * @param name
	 * @param key Identifikation der Liste, ueber die iteriert wird
	 */
	public IterationMergeElement(String name, String key) {
		super(name, key);
	}

	/**
	 * @param key1 MergeField Key fuer die Sortierung innerhalb der Iteration
	 */
	public void addSortFieldKey(String key1) {

		if (this.sortFieldKeys == null) {
			this.sortFieldKeys = new LinkedList<>();
		}
		this.sortFieldKeys.add(key1);
	}

	@Override
	public String getContent(MergeContext ctx, MergeSource mergeSource) throws DocTemplateException {

		StringBuffer result = new StringBuffer();
		IterationMergeSource ims = IterationMergeHelper.getIterationMergeSource(ctx, mergeSource, this.key, this.sortFieldKeys);
		log.debug(this.name + ": iterative output with key " + this.key);
		if (ims != null) {
			MergeSource previousMergeSource = ctx.getCurrentMergeSource();
			ctx.setCurrentMergeSource(ims);
			while (ims.hasNext()) {
				ims.next();
				result.append(super.getContent(ctx, ims));
			}
			ctx.setCurrentMergeSource(previousMergeSource);
		} else {
			log.warn(this.name + ": no iteration source with key " + this.key);
		}
		return result.toString();
	}

	@Override
	public void getContent(MergeContext ctx, MergeSource mergeSource, OutputStream output) throws DocTemplateException {

		IterationMergeSource ims = IterationMergeHelper.getIterationMergeSource(ctx, mergeSource, this.key, this.sortFieldKeys);
		log.debug(this.name + ": iterative output with key " + this.key);
		if (ims != null) {
			MergeSource previousMergeSource = ctx.getCurrentMergeSource();
			ctx.setCurrentMergeSource(ims);
			while (ims.hasNext()) {
				ims.next();
				super.getContent(ctx, ims, output);
			}
			ctx.setCurrentMergeSource(previousMergeSource);
		} else {
			log.warn(this.name + ": no iteration source with key " + this.key);
		}
	}

}
