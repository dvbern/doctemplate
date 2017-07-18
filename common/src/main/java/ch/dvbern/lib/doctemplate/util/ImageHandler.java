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
package ch.dvbern.lib.doctemplate.util;

import java.io.OutputStream;

import ch.dvbern.lib.doctemplate.common.DocTemplateException;
import ch.dvbern.lib.doctemplate.common.Image;

/**
 * @author lsimon
 */
public interface ImageHandler<T extends Image> {

	String IMAGE_NAME_TAG = "#IMGNAME#";

	/**
	 * @param image
	 * @param formatSuffix
	 * @param output
	 * @return
	 * @throws DocTemplateException
	 */
	String addImage(T image, String formatSuffix, OutputStream output, int imgIndex, boolean doublette) throws DocTemplateException;
}