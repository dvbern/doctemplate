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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author lsimon
 */
public class StaticMergeElement extends BasicMergeElement {

	private final String staticContent;

	/**
	 * Konstruktor fuer ein MergeElement mit statischem Inhalt.
	 *
	 * @param staticContent statischer Bestandteil der Vorlage
	 */
	public StaticMergeElement(String staticContent) {

		this.staticContent = staticContent;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see ch.dvbern.rodos.merger.server.util.RTFMergeEngine.BasicMergeElement#getContent(ch.
	 * dvbern.rodos.merger.shared .MergeContext, ch.dvbern.rodos.merger.shared.MergeSource)
	 */
	@Override
	public String getContent(MergeContext ctx, MergeSource mergeSource) throws DocTemplateException {

		return this.staticContent;
	}

	@Override
	public void getContent(MergeContext ctx, MergeSource mergeSource, OutputStream output) throws DocTemplateException {

		try {
			output.write(this.staticContent.getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw new DocTemplateException(e);
		}
	}
}
