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

/**
 * @author lsimon
 */
public abstract class AbstractMergeElement extends BasicMergeElement {

	/**
	 *
	 */
	protected String key;
	/**
	 *
	 */
	protected final String name;
	/**
	 *
	 */
	protected static final String ALTERNATE_SUFFIX = "_ALT";

	/**
	 * Konstruktor eines MergeElements, dessen Ausgabe unterdrueckt werden kann.
	 *
	 * @param name
	 * @param key Identifikation der Bedingung, ob eine Ausgabe erfolgen soll
	 */
	public AbstractMergeElement(String name, String key) {

		this.key = key;
		this.name = name;
	}

	/**
	 *
	 */
	public void initFuerRtf() {

		int altPos = this.key.indexOf(ALTERNATE_SUFFIX);
		if (altPos > 0) {
			this.key = this.key.substring(0, altPos);
		}
	}

}
