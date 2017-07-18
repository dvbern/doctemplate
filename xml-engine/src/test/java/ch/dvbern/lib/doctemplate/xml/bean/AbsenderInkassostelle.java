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


public class AbsenderInkassostelle {
	private List<Adresse> adressen = new ArrayList<Adresse>();
	
	private String telefon;

	private String telefax;

	private String email;

	public List<Adresse> getAdressen() {
		return adressen;
	}

	public void setAdressen(List<Adresse> adressen) {
		this.adressen = adressen;
	}

	public void addAdresse(Adresse adress) {
		this.adressen.add(adress);
	}

	public String getTelefon() {
		return telefon;
	}

	public void setTelefon(String telefon) {
		this.telefon = telefon;
	}

	public String getTelefax() {
		return telefax;
	}

	public void setTelefax(String telefax) {
		this.telefax = telefax;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	
}
