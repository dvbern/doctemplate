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
 * Informationen ueber Fehler beim Aufbereiten von Dokumenten aus dem Template und {@link MergeSource}-Quellen.
 */
public class DocTemplateException extends Exception {

	private static final long serialVersionUID = 1L;

	private final String errorCode;
	private final Object[] args;

	/**
	 * Konstruktor.
	 *
	 * @param errorCode Fehlercode z.B. als i18n key
	 * @param args zusaetzliche Informationen fuer die Fehlermeldung
	 */
	public DocTemplateException(String errorCode, Object... args) {

		super(errorCode);
		this.errorCode = errorCode;
		this.args = args;
	}

	/**
	 * Konstruktor.
	 *
	 * @param nestedException verursachender Fehler
	 */
	public DocTemplateException(Throwable nestedException) {
		super(nestedException);
		this.errorCode = nestedException.getMessage();
		this.args = new Object[] {};
	}

	/**
	 * @return Returns the errorCode.
	 */
	public String getErrorCode() {

		return this.errorCode;
	}

	/**
	 * @return Returns the args.
	 */
	public Object[] getArgs() {

		return this.args;
	}
}
