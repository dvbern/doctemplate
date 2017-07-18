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

package ch.dvbern.lib.doctemplate.docx;

import ch.dvbern.lib.doctemplate.common.Image;

public class DocxImage extends Image {

	private int index = 0;
	private String id;

	public DocxImage(byte[] bytes, int width, int height, Format format) {
		super(bytes, width, height, format);
	}

	public int getIndex() {

		return this.index;
	}

	public void setIndex(int index) {

		this.index = index;
	}

	public String getId() {

		return this.id;
	}

	public String getRId() {

		return "rId" + this.id;
	}

	public void setId(String id) {

		this.id = id;
	}

	public void setId(int id) {

		setId(Integer.toString(id));
	}

	public String getPath() {

		return "word/media/" + getName();
	}

	public String getName() {

		return "image" + getIndex() + "." + getFormat().name().toLowerCase();
	}
}
