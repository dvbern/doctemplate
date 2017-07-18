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

package ch.dvbern.lib.doctemplate.converter;

/**
 * Legt fest, wie sich der DocumentConverter zum Libreoffice-Server verbindet.
 */
enum ConnectionStrategy {
	/**
	 * Versucht, sich sofort beim Initialisieren zu connecten. Die Verbindung wird - wenn moeglich - gehalten.
	 */
	ON_INIT(true, false),
	/**
	 * Connected bei jedem Request und disconnected danach sofort
	 */
	PER_REQUEST(false, true);

	private final boolean connectOnInit;
	private final boolean disconnectAfterRequest;

	ConnectionStrategy(boolean connectOnInit, boolean disconnectAfterRequest) {
		this.connectOnInit = connectOnInit;
		this.disconnectAfterRequest = disconnectAfterRequest;
	}

	public boolean isConnectOnInit() {
		return connectOnInit;
	}

	public boolean isDisconnectAfterRequest() {
		return disconnectAfterRequest;
	}
}
