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
package ch.dvbern.lib.doctemplate.odt;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;

import ch.dvbern.lib.doctemplate.common.DocTemplateException;
import ch.dvbern.lib.doctemplate.common.Image;
import ch.dvbern.lib.doctemplate.common.Image.Format;
import ch.dvbern.lib.doctemplate.util.ImageHandler;

/**
 * @author lsimon
 */
public class ODTImageHandler implements ImageHandler<Image> {

	private static final String IMAGE_TAG_TEMPLATE = "<draw:frame draw:style-name=\"fr1\" draw:name=\"graphics#INDEX1#\" "
			+ "text:anchor-type=\"as-char\" svg:width=\"#WIDTH#cm\" svg:height=\"#HEIGHT#cm\" draw:z-index=\"#INDEX0#\">"
			+ "<draw:image xlink:href=\"#IMGNAME#\" xlink:type=\"simple\" " + "xlink:show=\"embed\" xlink:actuate=\"onLoad\"/></draw:frame>";
	private static final String IMAGE_INDEX0_TAG = "#INDEX0#", IMAGE_INDEX1_TAG = "#INDEX1#";
	private static final String IMAGE_WIDTH_TAG = "#WIDTH#", IMAGE_HEIGHT_TAG = "#HEIGHT#";
	private static final double IMAGE_FACTOR_2CM = 0.0265;

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.dvbern.lib.doctemplate.util.ImageHandler#addImage(ch.dvbern.lib.doctemplate.common.Image,
	 * java.lang.String, java.io.OutputStream)
	 */
	@Override
	public String addImage(Image image, String formatSuffix, OutputStream output, int imgIndex, boolean doublette) throws DocTemplateException {

		if (image.getFormat() != Format.PNG && image.getFormat() != Format.JPEG) {
			throw new DocTemplateException("image format not supported: " + image.getFormat());
		}
		long w = image.getWidth(), h = image.getHeight();
		if (formatSuffix != null && formatSuffix.length() > 0) {
			StringTokenizer st = new StringTokenizer(formatSuffix, "_");
			if (st.hasMoreTokens()) {
				w = Integer.parseInt(st.nextToken());
			}
			if (st.hasMoreTokens()) {
				h = Integer.parseInt(st.nextToken());
			}
		}
		StringBuffer sb = new StringBuffer(IMAGE_TAG_TEMPLATE);
		replace(sb, IMAGE_INDEX0_TAG, Integer.toString(imgIndex));
		replace(sb, IMAGE_INDEX1_TAG, Integer.toString(imgIndex + 1));
		replace(sb, IMAGE_WIDTH_TAG, Double.toString(w * IMAGE_FACTOR_2CM));
		replace(sb, IMAGE_HEIGHT_TAG, Double.toString(h * IMAGE_FACTOR_2CM));
		String imgName = "Pictures/img" + imgIndex + "." + image.getFormat().toString().toLowerCase();
		replace(sb, IMAGE_NAME_TAG, imgName);
		try {
			output.write(sb.toString().getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw new DocTemplateException(e);
		}
		return imgName;
	}

	private void replace(StringBuffer sb, String token, String value) {

		int i = sb.indexOf(token);
		if (i >= 0) {
			sb.delete(i, i + token.length());
			sb.insert(i, value);
		}
	}

}
