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

package ch.dvbern.lib.doctemplate.xml.bean;

public class Buchung {

	private String textBuchung;

	private Float betragBuchung;

	private Float betragBuchungTest;

	public Buchung(String textBuchung, Float betragBuchung, Float betragBuchungTest) {
		this(textBuchung, betragBuchung);
		this.betragBuchungTest = betragBuchungTest;
	}

	public Buchung(String textBuchung, Float betragBuchung) {
		super();
		this.textBuchung = textBuchung;
		this.betragBuchung = betragBuchung;
	}

	public String getTextBuchung() {
		return textBuchung;
	}

	public void setTextBuchung(String textBuchung) {
		this.textBuchung = textBuchung;
	}

	public Float getBetragBuchung() {
		return betragBuchung;
	}

	public void setBetragBuchung(Float betragBuchung) {
		this.betragBuchung = betragBuchung;
	}

	public Float getBetragBuchungTest() {
		return betragBuchungTest;
	}

	public void setBetragBuchungTest(Float betragBuchungTest) {
		this.betragBuchungTest = betragBuchungTest;
	}

}
