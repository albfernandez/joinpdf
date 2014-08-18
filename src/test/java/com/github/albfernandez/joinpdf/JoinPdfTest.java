package com.github.albfernandez.joinpdf;

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
