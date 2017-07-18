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

public class Refernz {

	private String gemeindeNummer;

	private String zpvNummer;

	private String steuerJahr;

	private String register;

	private String fallLaufNummer;

	public String getGemeindeNummer() {
		return gemeindeNummer;
	}

	public void setGemeindeNummer(String gemeindeNummer) {
		this.gemeindeNummer = gemeindeNummer;
	}

	public String getZpvNummer() {
		return zpvNummer;
	}

	public void setZpvNummer(String zpvNummer) {
		this.zpvNummer = zpvNummer;
	}

	public String getSteuerJahr() {
		return steuerJahr;
	}

	public void setSteuerJahr(String steuerJahr) {
		this.steuerJahr = steuerJahr;
	}

	public String getRegister() {
		return register;
	}

	public void setRegister(String register) {
		this.register = register;
	}

	public String getFallLaufNummer() {
		return fallLaufNummer;
	}

	public void setFallLaufNummer(String fallLaufNummer) {
		this.fallLaufNummer = fallLaufNummer;
	}

}
