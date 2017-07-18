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

import java.util.HashMap;
import java.util.Map;

/**
 * Ermoeglich den Austausch von Informationen zwischen verschiedenen {@link MergeSource}-Instanzen.
 */
public class MergeContext {

	MergeSource currentMergeSource;
	private Map<Object, Object> attributes;

	/**
	 * Konstruktor
	 *
	 * @param currentMergeSource aktuell verwendete Merge-Quelle
	 */
	public MergeContext(MergeSource currentMergeSource) {

		this.currentMergeSource = currentMergeSource;
	}

	/**
	 * @return Returns the currentMergeSource.
	 */
	public MergeSource getCurrentMergeSource() {

		return this.currentMergeSource;
	}

	/**
	 * @param currentMergeSource The currentMergeSource to set.
	 */
	public void setCurrentMergeSource(MergeSource currentMergeSource) {

		this.currentMergeSource = currentMergeSource;
	}

	/**
	 * Lesen eines Kontext-Attributes.
	 *
	 * @param key Identifikation des Attributes
	 * @return unter <code>key</code> gespeichertes Attribut oder <code>null</code> wenn nicht vorhanden
	 */
	public Object getAttribute(Object key) {

		if (this.attributes == null) {
			return null;
		}
		return this.attributes.get(key);
	}

	/**
	 * Setzen eines Kontext-Attributes.
	 *
	 * @param key Identifikation des Attributes
	 * @param value zu setzender Wert
	 */
	public void setAttribute(Object key, Object value) {

		if (this.attributes == null) {
			this.attributes = new HashMap<>();
		}
		this.attributes.put(key, value);
	}

}