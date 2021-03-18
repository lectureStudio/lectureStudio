/*
 * Copyright (C) 2020 TU Darmstadt, Department of Computer Science,
 * Embedded Systems and Applications Group.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.lecturestudio.core.pdf;

import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.lecturestudio.core.util.FontFileLocator;

import org.apache.fontbox.ttf.TTFParser;
import org.apache.fontbox.ttf.TrueTypeFont;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

/**
 * This class manages font name to font file mappings.
 * 
 * @author Alex Andres
 */
public class PdfFontManager {

	private static final Logger LOG = LogManager.getLogger(PdfFontManager.class);
	
	private static final class InstanceHolder {
		static final PdfFontManager INSTANCE = new PdfFontManager();
	}

	private final TTFParser parser = new TTFParser(false, true);
	
	private final Map<PDDocument, Map<String, PDFont>> docFontMap = new WeakHashMap<>();
	
	
	public static PdfFontManager getInstance() {
		return InstanceHolder.INSTANCE;
	}
	
	/** Font name to font file mapping. */
	private final Map<String, String> fontFileMap = new HashMap<>();
	
	
	private PdfFontManager() {
		loadSystemFonts();
	}
	
	public String getFontFile(String fontPsName) {
		return fontFileMap.get(fontPsName);
	}
	
	public void addFontFile(String fontPath) throws IOException {
		registerFontFile(fontPath);
	}
	
	public PDFont getPdfFont(Font font, PDDocument document) throws IOException {
		Map<String, PDFont> docFonts = docFontMap.get(document);
		PDFont pdFont = null;

		if (docFonts == null) {
			// No fonts loaded for this document so far.
			docFonts = new HashMap<>();

			docFontMap.put(document, docFonts);
		}

		String fontName = font.getPSName();

		pdFont = docFonts.get(fontName);

		if (pdFont == null) {
			pdFont = loadPdfFont(font, document);

			if (pdFont != null) {
				docFonts.put(fontName, pdFont);
			}
		}

		return pdFont;
	}
	
	private PDFont loadPdfFont(Font font, PDDocument document) throws IOException {
		String fontPath = getFontFile(font.getPSName());

		if (fontPath == null) {
			return null;
		}

		PDFont pdFont;
		File fontFile = new File(fontPath);

		if (fontFile.exists()) {
			pdFont = PDType0Font.load(document, fontFile);
		}
		else {
			try (InputStream inStream = PdfFontManager.class.getClassLoader()
					.getResourceAsStream(fontPath)) {
				pdFont = PDType0Font.load(document, inStream);
			}
		}

		return pdFont;
	}
	
	private void loadSystemFonts() {
		// Map font names to font files.
		FontFileLocator.find((path) -> {
			try {
				registerFontFile(path.toString());
			}
			catch (IOException e) {
				LOG.error("Loading font failed.", e);
			}
		}, ".ttf");
	}
	
	private void registerFontFile(String fontPath) throws IOException {
		TrueTypeFont font;

		File file = new File(fontPath);

		if (file.exists()) {
			font = parser.parse(fontPath);
		}
		else {
			InputStream inStream = PdfFontManager.class.getClassLoader().getResourceAsStream(fontPath);
			font = parser.parse(inStream);
			inStream.close();
		}

		if (font != null) {
			fontFileMap.put(font.getName(), fontPath);
		}
	}
	
}
