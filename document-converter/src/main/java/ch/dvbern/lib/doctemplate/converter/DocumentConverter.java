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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.artofsolving.jodconverter.DefaultDocumentFormatRegistry;
import com.artofsolving.jodconverter.DocumentFormat;
import com.artofsolving.jodconverter.openoffice.connection.OpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.connection.SocketOpenOfficeConnection;
import com.artofsolving.jodconverter.openoffice.converter.OpenOfficeDocumentConverter;

import static java.util.Objects.requireNonNull;

/**
 * Konvertiert ein OpenOffice/LibreOffice Dokument (Writer/.odt) in ein PDF
 * via Libreoffice Headless-Server.
 *
 * Server/Port koennen via System-Property ({@link #SYSPROP_SERVER_HOST} bzw. {@link #SYSPROP_SERVER_PORT}) oder Properties-File angegeben werden.
 */
public class DocumentConverter {

	// Converter properties
	private static final String PDF = "pdf";
	private static final String ODT = "odt";
	private static final DocumentFormat ODT_FORMAT;
	private static final DocumentFormat PDF_FORMAT;

	private static final String PATH_TO_PROPERTIES = "/document_converter.properties";

	private static final String SERVER_HOST = "server.host";
	public static final String SYSPROP_SERVER_HOST = "documentconverter." + SERVER_HOST;
	private static final String DEFAULT_HOST = "localhost";

	private static final String SERVER_PORT = "server.port";
	public static final String SYSPROP_SERVER_PORT = "documentconverter." + SERVER_PORT;
	private static final int DEFAULT_PORT = 8100;

	private final ConnectionStrategy connectionStrategy;

	/** Logger */
	private static final Log LOG = LogFactory.getLog(DocumentConverter.class);

	private final String host;
	private final int port;

	private OpenOfficeConnection connection = null;

	private static final Properties PROPERTIES = new Properties();

	static {
		DefaultDocumentFormatRegistry registry = new DefaultDocumentFormatRegistry();
		ODT_FORMAT = registry.getFormatByFileExtension(ODT);
		PDF_FORMAT = registry.getFormatByFileExtension(PDF);

		InputStream is = Thread.currentThread().getContextClassLoader()
						.getResourceAsStream(PATH_TO_PROPERTIES);
		try {
			PROPERTIES.load(is);
		} catch (Exception e) {
			String msg = "Could not load the property file: "
							+ PATH_TO_PROPERTIES;
			LOG.error(msg, e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					LOG.warn("Could not close the property file: "
									+ PATH_TO_PROPERTIES, e);
				}
			}
		}
	}

	@SafeVarargs
	private static <T> T coalesce(T... args) {
		for (T arg : args) {
			if (arg != null) {
				return arg;
			}
		}
		throw new IllegalArgumentException("One arg must be != null");
	}

	private DocumentConverter(ConnectionStrategy connectionStrategy) throws ConnectException {
		this.connectionStrategy = requireNonNull(connectionStrategy);

		String portStr = coalesce(System.getProperty(SYSPROP_SERVER_PORT), PROPERTIES.getProperty(SERVER_PORT), String.valueOf(DEFAULT_PORT));
		int newPort;
		try {
			newPort = Integer.parseInt(portStr);
		} catch (NumberFormatException ignored) {
			LOG.warn("Incorrect " + SERVER_PORT + " value: " + portStr
							+ " Trying to use the default port: " + DEFAULT_PORT);
			newPort = DEFAULT_PORT;
		}
		port = newPort;
		host = coalesce(System.getProperty(SYSPROP_SERVER_HOST), PROPERTIES.getProperty(SERVER_HOST), DEFAULT_HOST);

		if (connectionStrategy.isConnectOnInit()) {
			connect();
		}
	}

	/**
	 * Create a DocumentConverter that establishes a connection to the libreoffice server immediately.
	 * @throws ConnectException Connection to the libreoffice service could not be established
	 */
	public static DocumentConverter createConnectImmediately() throws ConnectException {
		return new DocumentConverter(ConnectionStrategy.ON_INIT);
	}


	/**
	 * Create a DocumentConverter that establishes a connection to the libreoffice server only for the duration
	 * of the {@link #convertToPdf(InputStream)} call
	 */
	public static DocumentConverter createConnectPerRequest() {
		try {
			return new DocumentConverter(ConnectionStrategy.PER_REQUEST);
		} catch (ConnectException e) {
			throw new IllegalStateException("ConnectException not expected while calling the constructor for PER_REQUEST connection!", e);
		}
	}

	public synchronized ByteArrayOutputStream convertToPdf(InputStream inputStream) throws ConnectException {
		if (inputStream == null) {
			throw new RuntimeException("inputFile is null");
		}
		ByteArrayOutputStream baos;

		try {
			connect();

			OpenOfficeDocumentConverter converter = new OpenOfficeDocumentConverter(connection);
			baos = new ByteArrayOutputStream();
			//noinspection AccessToStaticFieldLockedOnInstance
			converter.convert(inputStream, ODT_FORMAT, baos, PDF_FORMAT);
		} finally {
			try {
				inputStream.close();
			} catch (IOException e) {
				LOG.warn("Could not close the FileInputStream.", e);
			}

			if (connectionStrategy.isDisconnectAfterRequest()) {
				disconnect();
			}
		}
		return baos;
	}

	private synchronized void connect() throws ConnectException {
		if (connection == null) {
			connection = new SocketOpenOfficeConnection(host, port);
		}

		if (!connection.isConnected()) {
			connection.connect();
		}
	}

	/**
	 *
	 */
	public synchronized void disconnect() {
		if (connection != null) {
			connection.disconnect();
		}
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		disconnect();
	}
}
