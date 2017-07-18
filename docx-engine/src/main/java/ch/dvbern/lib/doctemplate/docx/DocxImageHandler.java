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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;

import ch.dvbern.lib.doctemplate.common.DocTemplateException;
import ch.dvbern.lib.doctemplate.common.Image.Format;
import ch.dvbern.lib.doctemplate.util.ImageHandler;

/**
 * @author lsimon
 */
public class DocxImageHandler implements ImageHandler<DocxImage> {

	private final long px = 9525;
	private static final String ID = "#ID#";
	private static final String IMAGE_WIDTH_TAG = "#WIDTH#", IMAGE_HEIGHT_TAG = "#HEIGHT#";
	private static final String IMAGE_TAG_TEMPLATE = "<w:drawing><wp:inline distB=\"0\" distL=\"0\" distR=\"0\" distT=\"0\"><wp:extent cx=\"" + IMAGE_WIDTH_TAG + "\" cy=\""
			+ IMAGE_HEIGHT_TAG + "\"/><wp:effectExtent b=\"0\" l=\"0\" r=\"0\" t=\"0\"/><wp:docPr id=\"" + ID + "\" name=\"Grafik " + ID
			+ "\"/><wp:cNvGraphicFramePr/><a:graphic xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\"><a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/picture\"><pic:pic xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\"><pic:nvPicPr><pic:cNvPr id=\"0\" name=\"\"/><pic:cNvPicPr/></pic:nvPicPr><pic:blipFill><a:blip cstate=\"print\" r:embed=\"#IMGNAME#\"/><a:stretch><a:fillRect/></a:stretch></pic:blipFill><pic:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\""
			+ IMAGE_WIDTH_TAG + "\" cy=\"" + IMAGE_HEIGHT_TAG
			+ "\"/></a:xfrm><a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></pic:spPr></pic:pic></a:graphicData></a:graphic></wp:inline></w:drawing>";
	private int maxImgIdx;
	private int maxRId;

	public DocxImageHandler(int maxImgIdx, int maxRId) {
		this.maxImgIdx = maxImgIdx;
		this.maxRId = maxRId;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see ch.dvbern.lib.doctemplate.util.ImageHandler#addImage(ch.dvbern.lib.doctemplate. common.Image,
	 * java.lang.String, java.io.OutputStream)
	 */
	@Override
	public String addImage(DocxImage image, String formatSuffix, OutputStream output, int imgIndex, boolean doublette) throws DocTemplateException {

		if (!doublette) {
			image.setIndex(++this.maxImgIdx);
			image.setId(++this.maxRId);
		}
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
		String imgName = image.getPath();
		try {
			StringBuffer sb = new StringBuffer(IMAGE_TAG_TEMPLATE);
			replace(sb, IMAGE_WIDTH_TAG, Long.toString(w * this.px));
			replace(sb, IMAGE_HEIGHT_TAG, Long.toString(h * this.px));
			replace(sb, IMAGE_WIDTH_TAG, Long.toString(w * this.px));
			replace(sb, IMAGE_HEIGHT_TAG, Long.toString(h * this.px));
			replace(sb, IMAGE_NAME_TAG, image.getRId());
			replace(sb, ID, image.getId());
			replace(sb, ID, image.getId());
			output.write(sb.toString().getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw new DocTemplateException(e);
		}
		return imgName;
	}

	private static void replace(StringBuffer sb, String token, String value) {

		int i = sb.indexOf(token);
		if (i >= 0) {
			sb.delete(i, i + token.length());
			sb.insert(i, value);
		}
	}

}