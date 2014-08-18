package com.github.albfernandez.joinpdf;

import java.io.File;
import java.io.FileFilter;

import org.apache.commons.lang3.StringUtils;

public class JoinPdfFileFilter implements FileFilter {
	
	public JoinPdfFileFilter () {
		super();
	}


	
	
	public boolean accept(File pathname) {
		if (pathname == null) {
			return false;
		}
		String fileName = StringUtils.lowerCase(pathname.getName(), JoinPdf.LOCALE_ES);
		return 
			fileName.endsWith("pdf") || 
			fileName.endsWith("png") ||
			fileName.endsWith("jpg") ||
			fileName.endsWith("jpeg") ||
			fileName.endsWith("tiff") ||
			fileName.endsWith("tif");

	}

}
