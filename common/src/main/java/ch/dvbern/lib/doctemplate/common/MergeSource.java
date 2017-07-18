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

import java.util.List;

/**
 * Liefert Informationen fuer den Merge einer Dokumentvorlage in das vorgesehene Resultat.
 */
public interface MergeSource {

	/**
	 * Liefert ein Objekt, welches in das Ergebnisdokument eingefuegt werden soll.
	 *
	 * @param ctx Kontext zum Austauschen von Informationen zwischen MergeSourcen
	 * @param key Identifikation des zu liefernden Objektes
	 * @return Resultat als Objekt (Comparable, damit sortiertbar)
	 * @throws DocTemplateException Exception als Basis fuer eine Meldung an den Benutzer
	 */
	Object getData(MergeContext ctx, String key) throws DocTemplateException;

	/**
	 * Entscheidet ob ein Textabschnitt im Ergebnisdokument enthalten sein soll.
	 *
	 * @param ctx Kontext zum Austauschen von Informationen zwischen MergeSourcen
	 * @param key Identifikation des Entscheidkriteriums
	 * @return <code>true</code> wenn der Textabschnitt enthalten sein soll, sonst <code>false</code>
	 * @throws DocTemplateException Exception als Basis fuer eine Meldung an den Benutzer
	 */
	Boolean ifStatement(MergeContext ctx, String key) throws DocTemplateException;

	/**
	 * Liefert eine Liste von Daten, die innerhalb einer Iteration in das Ergebnisdokument eingefuegt werden sollen.
	 *
	 * @param ctx Kontext zum Austauschen von Informationen zwischen MergeSourcen
	 * @param key Identifikation der zu liefernden Liste
	 * @return Liste mit Datenquellen fuer eine iterative Ermittlung von Daten (Auflistung)
	 * @throws DocTemplateException Exception als Basis fuer eine Meldung an den Benutzer
	 */
	List<MergeSource> whileStatement(MergeContext ctx, String key) throws DocTemplateException;

}