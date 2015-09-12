package com.github.albfernandez.joinpdf;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfReader;

public final class ItextUtils {

	private ItextUtils() {
		throw new AssertionError("No instances allowed");
	}
	public static void close(Document document) {
		try{
			if (document != null) {
				document.close();
			}
		}
		catch (Exception e) {  //NOPMD
			//
		}
	}
	public static void close(PdfReader reader) {
		try {
			if (reader != null) {
				reader.close();
			}
		}
		catch (Exception e) {  //NOPMD
			//
		}
	}
}
