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
 * Kapselt ein Bild im PNG Format als byte array. Wenn die Methode getData(...) einer {@link MergeSource} eine Instanz
 * dieses Typs liefert, wird ein Bild in das zu erstellende Dokument eingefuegt.
 */
public class Image {

	private final byte[] bytes;
	private final int width, height;
	private final Format format;

	/**
	 * @param bytes
	 * @param width
	 * @param height
	 * @param format
	 */
	public Image(byte[] bytes, int width, int height, Format format) {
		super();
		this.bytes = java.util.Arrays.copyOf(bytes, bytes.length);
		this.width = width;
		this.height = height;
		this.format = format;
	}

	/**
	 * @return Returns the bytes.
	 */
	public byte[] getBytes() {

		return this.bytes;
	}

	/**
	 * @return Returns the width.
	 */
	public int getWidth() {

		return this.width;
	}

	/**
	 * @return Returns the height.
	 */
	public int getHeight() {

		return this.height;
	}

	/**
	 * @return Returns the format.
	 */
	public Format getFormat() {

		return this.format;
	}

	/**
	 * Bildformat
	 */
	public enum Format {

		/** Source of the picture is a PNG */
		PNG,

		/** Source of the picture is an EMF (enhanced metafile) */
		EMF,

		/** Source of the picture is a JPEG */
		JPEG;
	}

	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;
		result = prime * result + java.util.Arrays.hashCode(this.bytes);
		result = prime * result + (this.format == null ? 0 : this.format.hashCode());
		result = prime * result + this.height;
		result = prime * result + this.width;
		return result;
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Image other = (Image) obj;
		if (!java.util.Arrays.equals(this.bytes, other.bytes)) {
			return false;
		}
		if (this.format != other.format) {
			return false;
		}
		if (this.height != other.height) {
			return false;
		}
		if (this.width != other.width) {
			return false;
		}
		return true;
	}

}