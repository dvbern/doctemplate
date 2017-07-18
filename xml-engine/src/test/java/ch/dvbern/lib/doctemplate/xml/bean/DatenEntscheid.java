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
import java.util.Date;
import java.util.List;

public class DatenEntscheid {

	private Float betragLst;

	private Date faelligkeit;

	private Integer zahlungRueckzahlungFrist;

	private String saldo;

	private List<Buchung> buchungen = new ArrayList<Buchung>();

	private String saldoInkasso;

	private String bankKonto;
	
	private boolean showBankKonto = true;

	public Float getBetragLst() {
		return betragLst;
	}

	public void setBetragLst(Float betragLst) {
		this.betragLst = betragLst;
	}

	public Date getFaelligkeit() {
		return faelligkeit;
	}

	public void setFaelligkeit(Date faelligkeit) {
		this.faelligkeit = faelligkeit;
	}

	public Integer getZahlungRueckzahlungFrist() {
		return zahlungRueckzahlungFrist;
	}

	public void setZahlungRueckzahlungFrist(Integer zahlungRueckzahlungFrist) {
		this.zahlungRueckzahlungFrist = zahlungRueckzahlungFrist;
	}

	public String getSaldo() {
		return saldo;
	}

	public void setSaldo(String saldo) {
		this.saldo = saldo;
	}

	public List<Buchung> getBuchungen() {
		return buchungen;
	}

	public void setBuchungen(List<Buchung> buchungen) {
		this.buchungen = buchungen;
	}

	public void addBuchung(Buchung buchung) {
		this.buchungen.add(buchung);
	}

	public String getSaldoInkasso() {
		return saldoInkasso;
	}

	public void setSaldoInkasso(String saldoInkasso) {
		this.saldoInkasso = saldoInkasso;
	}

	public DatenEntscheid getKonto(){
		return this;
	}
	
	public String getBankKonto() {
		return bankKonto;
	}

	public void setBankKonto(String bankKonto) {
		this.bankKonto = bankKonto;
	}

	public boolean isShowBankKonto() {
		return showBankKonto;
	}

	public void setShowBankKonto(boolean showBankKonto) {
		this.showBankKonto = showBankKonto;
	}

}
