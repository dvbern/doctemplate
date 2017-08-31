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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author lsimon
 */
public class ConditionMergeElement extends AbstractMergeElement {

	private static final Log log = LogFactory.getLog(ConditionMergeElement.class);

	/**
	 * Konstruktor eines MergeElements, dessen Ausgabe unterdrueckt werden kann.
	 *
	 * @param name
	 * @param key Identifikation der Bedingung, ob eine Ausgabe erfolgen soll
	 */
	public ConditionMergeElement(String name, String key) {
		super(name, key);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see ch.dvbern.rodos.merger.server.util.RTFMergeEngine.BasicMergeElement#getContent(ch.
	 * dvbern.rodos.merger.shared .MergeContext, ch.dvbern.rodos.merger.shared.MergeSource)
	 */
	@Override
	public String getContent(MergeContext ctx, MergeSource mergeSource) throws DocTemplateException {

		Boolean b = mergeSource.ifStatement(ctx, this.key);
		log.debug(this.name + ": conditional output with key " + this.key + " is " + b);
		if (b != null) {
			if (b.booleanValue()) {
				return super.getContent(ctx, mergeSource);
			}
		} else {
			log.warn(this.name + ": no condition source with key " + this.key);
		}
		return "";
	}

	/**
	 * @see ch.dvbern.lib.doctemplate.common.BasicMergeElement#getContent(ch.dvbern.lib.doctemplate.common.MergeContext,
	 *      ch.dvbern.lib.doctemplate.common.MergeSource, java.io.OutputStream)
	 */
	@Override
	public void getContent(MergeContext ctx, MergeSource mergeSource, OutputStream output) throws DocTemplateException {

		Boolean b = mergeSource.ifStatement(ctx, this.key);
		log.debug(this.name + ": conditional output with key " + this.key + " is " + b);
		if (b != null) {
			if (b.booleanValue()) {
				super.getContent(ctx, mergeSource, output);
			}
		} else {
			log.warn(this.name + ": no condition source with key " + this.key);
		}
	}

	@Override
	public String toString() {
		return new org.apache.commons.lang.builder.ToStringBuilder(this)
				.append("key", key)
				.append("name", name)
				.toString();
	}
}
