package com.github.albfernandez.joinpdf;
/*
(C) Copyright 2014-2015 Alberto Fernández <infjaf@gmail.com>

This file is part of joinpdf.

Foobar is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Foobar is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
*/
import java.io.ByteArrayOutputStream;
import java.io.File;

import org.junit.Assert;
import org.junit.Test;

public class JoinPdfTest {

	private static final String RESOURCES_DIR = "src/test/resources/";
	private static final String DEBIAN_SOCIAL_CONTRACT_PDF = RESOURCES_DIR + "debian.social.contract.es.pdf";
	private static final String DEBIAN_PNG = RESOURCES_DIR + "debian.png";
	private static final String MULTIPAGE_TIFF = RESOURCES_DIR + "multipage_tiff_example.tif";

	@Test
	public void testPdf() throws Exception {
		JoinPdf join = new JoinPdf();
		join.addFile(new File(DEBIAN_SOCIAL_CONTRACT_PDF));
		testExport(join, 9);
		join.addFile(new File(DEBIAN_SOCIAL_CONTRACT_PDF));
		testExport(join, 18);

	}

	@Test
	public void testPdfPng() throws Exception {
		JoinPdf join = new JoinPdf();
		join.addFile(new File(DEBIAN_PNG));
		join.addFile(new File(DEBIAN_SOCIAL_CONTRACT_PDF));
		testExport(join, 10);

	}

	@Test
	public void testPdfPngTif() throws Exception {
		JoinPdf join = new JoinPdf();
		join.addFile(new File(DEBIAN_PNG));
		join.addFile(new File(DEBIAN_SOCIAL_CONTRACT_PDF));
		join.addFile(new File(MULTIPAGE_TIFF));
		testExport(join, 13);
	}

	@Test
	public void testDir() throws Exception {
		JoinPdf join = new JoinPdf();
		join.addDir(new File(RESOURCES_DIR));
		testExport(join, 13);
	}

	private void testExport(JoinPdf join, int pages) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		join.export(baos);
		byte[] data = baos.toByteArray();
		Assert.assertEquals(pages, JoinPdf.getPageCountPdf(data));

	}
}
