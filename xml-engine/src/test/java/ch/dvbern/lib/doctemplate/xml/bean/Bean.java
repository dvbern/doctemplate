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

import java.util.ArrayList;
import java.util.List;


public class Bean {

	private AbsenderInkassostelle absender;

	private Refernz refernz;

	private List<Adresse> adressaten = new ArrayList<Adresse>();

	private BriefDatum briefDatum;

	private String verfuegendeGemeinde;

	private AbsenderInkassostelle inkassostelle;

	private DatenEntscheid datenEntscheid;

	private List<Adresse> esrOhneForderungAdressaten = new ArrayList<Adresse>();

	public AbsenderInkassostelle getAbsender() {
		return absender;
	}

	public void setAbsender(AbsenderInkassostelle absender) {
		this.absender = absender;
	}

	public Refernz getRefernz() {
		return refernz;
	}

	public void setRefernz(Refernz refernz) {
		this.refernz = refernz;
	}

	public List<Adresse> getAdressaten() {
		return adressaten;
	}

	public void setAdressaten(List<Adresse> adressaten) {
		this.adressaten = adressaten;
	}

	public void addAdressaten(Adresse adressat) {
		this.adressaten.add(adressat);
	}

	public BriefDatum getBriefDatum() {
		return briefDatum;
	}

	public void setBriefDatum(BriefDatum briefDatum) {
		this.briefDatum = briefDatum;
	}

	public String getVerfuegendeGemeinde() {
		return verfuegendeGemeinde;
	}

	public void setVerfuegendeGemeinde(String verfuegendeGemeinde) {
		this.verfuegendeGemeinde = verfuegendeGemeinde;
	}

	public AbsenderInkassostelle getInkassostelle() {
		return inkassostelle;
	}

	public void setInkassostelle(AbsenderInkassostelle inkassostelle) {
		this.inkassostelle = inkassostelle;
	}

	public DatenEntscheid getDatenEntscheid() {
		return datenEntscheid;
	}

	public void setDatenEntscheid(DatenEntscheid datenEntscheid) {
		this.datenEntscheid = datenEntscheid;
	}

	public List<Adresse> getEsrOhneForderungAdressaten() {
		return esrOhneForderungAdressaten;
	}

	public void setEsrOhneForderungAdressaten(
			List<Adresse> esrOhneForderungAdressaten) {
		this.esrOhneForderungAdressaten = esrOhneForderungAdressaten;
	}
	
	public void addEsrOhneForderungAdressaten(Adresse adressat) {
		this.esrOhneForderungAdressaten.add(adressat);
	}

}
