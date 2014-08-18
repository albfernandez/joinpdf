package com.github.albfernandez.joinpdf;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.io.RandomAccessSource;
import com.itextpdf.text.io.RandomAccessSourceFactory;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.RandomAccessFileOrArray;
import com.itextpdf.text.pdf.codec.TiffImage;

public class JoinPdf {
	
	public static final Locale LOCALE_ES = new Locale("es", "ES");
	private boolean extraCompression = false;
	private boolean crypt = false;
	private boolean printPageNumbers = false;

	private String metadataAuthor;
	private String metadataKeywords;
	private String metadataTitle;
	private String metadataSubject;
	
	private int actualPage = 0;
	private int totalPages = 0;
	
	private float margin = 25.0f;
	
	private List<File> files = new ArrayList<File>();
	
	private static boolean bouncyCastleLoaded = false;
    
	static {
		try {
			Class.forName("org.bouncycastle.LICENSE");
			Class.forName("org.bouncycastle.asn1.ASN1OctetString");
			bouncyCastleLoaded = true;
		} catch (ClassNotFoundException e) {
			bouncyCastleLoaded = false;
		}
	}     


	
	public JoinPdf() {
		super();
	}

	public JoinPdf(List<File> documentFilesFiles) {
		addFiles(documentFilesFiles);
	}

	public JoinPdf(File dir) {
		addDir(dir);
	}

	public final void addDir(File dir) {
		File[] documents = dir.listFiles(new JoinPdfFileFilter());
		addFiles(Arrays.asList(documents));
	}

	public final void addFile(File document) {
		this.files.add(document);
	}

	public final void addFiles(List<File> documents) {
		this.files.addAll(documents);
	}

	public synchronized void export(OutputStream os) throws Exception {
		checkParameters();
		Document document = new Document();

		try {
			if (isPrintPageNumbers()) {
				this.totalPages = gePageCount();
				this.actualPage = 0;
			}
			PdfWriter writer = PdfWriter.getInstance(document, os);
			setParametersAndHeaders(writer, document);
			document.open();
			for (File file: this.files) {
				add(file, document, writer);
			}
		}
		finally {
			try {
			document.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void add(File file, Document document, PdfWriter writer) throws Exception {
		String fileName = StringUtils.lowerCase(file.getName(), LOCALE_ES);
		if (fileName.endsWith(".tif") || fileName.endsWith(".tiff")){
			addTiff(file, document, writer);
		}
		else if (fileName.endsWith(".pdf")){
			addPdf(file, document, writer);
		}
		else {
			addImage(file, document,writer);
		}		
	}

	private void addImage(File file, Document document, PdfWriter writer) throws Exception {
		Image image = Image.getInstance(fileToByteArray(file));
		addImage(image, document, writer);
		

	}

	private byte[] fileToByteArray(File file) throws IOException {
		byte[] retVal = null;
		long longitud = file.length();

		try (FileInputStream fis = new FileInputStream(file)) {
			retVal = new byte[(int) longitud];
			int totalReaded = 0;
			int readed = 0;
			while ((readed = fis.available()) > 0) {
				totalReaded += fis.read(retVal, totalReaded, readed);
			}
			return retVal;
		}

	}

	private void addImage(Image image, Document document, PdfWriter writer) throws Exception {
		if (image.getWidth() > image.getHeight()) {
			document.setPageSize(new Rectangle(PageSize.A4.getHeight(), PageSize.A4.getWidth()));
			image.setAbsolutePosition(0.0f + this.margin, 0.0f + this.margin);
			image.scaleToFit(PageSize.A4.getHeight() - this.margin * 2f, PageSize.A4.getWidth() - this.margin * 2f);
		} else {
			document.setPageSize(new Rectangle(PageSize.A4.getWidth(), PageSize.A4.getHeight()));
			image.setAbsolutePosition(0.0f + this.margin, 0.0f + this.margin);
			image.scaleToFit(PageSize.A4.getWidth() - this.margin * 2f, PageSize.A4.getHeight() - this.margin * 2f);
		}
		document.newPage();
		document.add(image);
		writePageNumber(writer);
	}

	private void writePageNumber(PdfWriter writer) throws Exception {
		
		if (isPrintPageNumbers()){			
			writePageNumber(writer.getDirectContent());			
		}
	}

	private void writePageNumber(PdfContentByte cb) throws DocumentException, IOException {
		if (isPrintPageNumbers()) {
			Rectangle size = cb.getPdfDocument().getPageSize();
			BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);			
			this.actualPage++;
			cb.beginText();
			cb.setFontAndSize(bf, 9);
			cb.showTextAligned(PdfContentByte.ALIGN_CENTER, this.actualPage + " of " + this.totalPages, size.getWidth() / 2, 10, 0);
			cb.endText();
		}
	}
	private void addPdf(File file, Document document, PdfWriter writer) throws Exception {
		
		PdfReader pdfReader = null;
		try (InputStream is = new FileInputStream(file)){
			pdfReader = new PdfReader(is);
			PdfContentByte cb = writer.getDirectContent();
			for (int currentPage = 1; currentPage <= pdfReader.getNumberOfPages(); currentPage++) {
				Rectangle currentPageSize = pdfReader.getPageSize(currentPage);
				document.setPageSize(currentPageSize);
				document.newPage();
				PdfImportedPage page = writer.getImportedPage(pdfReader, currentPage);
				cb.addTemplate(page, 0, 0);
				writePageNumber(cb);
				
			}			
			writer.flush();		
		}
		finally {		
			if (pdfReader != null) {
				writer.freeReader(pdfReader);
				pdfReader.close();
			}
		}
	}

	private int gePageCount() throws IOException {
		int pages = 0;
		for (File file: this.files) {
			pages += getPageCount(file);
		}
		return pages;
	}
	private int getPageCount(File file) throws IOException {
		String fileName = StringUtils.lowerCase(file.getName(), LOCALE_ES);
		if (fileName.endsWith(".tif") || fileName.endsWith(".tiff")){
			return getPageCountTif(file);
		}
		if (fileName.endsWith(".pdf")){
			return getPageCountPdf(file);
		}
		return 1;
			
	}
	private void addTiff(File file, Document document, PdfWriter writer) throws Exception {
		RandomAccessSource source = createRamdomAccessSource(file);
		RandomAccessFileOrArray ramdomAccess = new RandomAccessFileOrArray(source);
		int pages = getPageCount(file);
		for (int i = 1; i <= pages; i++) {
			Image image = TiffImage.getTiffImage(ramdomAccess, i);
			addImage(image, document, writer);
		}
		
	}

	private static RandomAccessSource createRamdomAccessSource(File file) throws IOException {
		RandomAccessSource source = new RandomAccessSourceFactory()
		.setForceRead(false)
		.setUsePlainRandomAccess(Document.plainRandomAccess)
		.createBestSource(file.getAbsolutePath());
		return source;
	}

	public void export(File file) throws Exception {
		try (OutputStream os = new FileOutputStream(file)){
			export(os);
		}
	}

	

	public float getMargin() {
		return margin;
	}

	public void setMargin(float margin) {
		this.margin = margin;
	}

	public boolean isExtraCompression() {
		return this.extraCompression;
	}

	public void setExtraCompression(boolean extraCompression) {
		this.extraCompression = extraCompression;
	}

	public boolean isCrypt() {
		return this.crypt;
	}

	public void setCrypt(boolean crypt) {
		this.crypt = crypt;
	}

	public boolean isPrintPageNumbers() {
		return this.printPageNumbers;
	}

	public void setPrintPageNumbers(boolean pageNumbers) {
		this.printPageNumbers = pageNumbers;
	}
	private void setParametersAndHeaders(PdfWriter writer, Document document) throws DocumentException {
		if (this.extraCompression) {
			writer.setFullCompression();
		}
		if (this.isCrypt() && bouncyCastleLoaded) {
			int permisos = PdfWriter.ALLOW_PRINTING;
			writer.setEncryption(null, null, permisos, PdfWriter.ENCRYPTION_AES_128);
		}
		if (!StringUtils.isBlank(this.getMetadataAuthor())){
			document.addAuthor(this.getMetadataAuthor());
		}
		if (!StringUtils.isBlank(this.getMetadataKeywords())){
			document.addKeywords(this.getMetadataKeywords());
		}
		if (!StringUtils.isBlank(this.getMetadataTitle())){
			document.addTitle(this.getMetadataTitle());
		}
		if (!StringUtils.isBlank(this.getMetadataSubject())){
			document.addSubject(this.getMetadataSubject());
		}
	}
	private void checkParameters() throws Exception {
		if (this.files.isEmpty()) {
			throw new Exception("You must provide some files to join");
		}
		if (this.isCrypt() && !bouncyCastleLoaded) {			
			throw new Exception("Bouncycastle not found!");
		}
	}

	
	public String getMetadataAuthor() {
		return this.metadataAuthor;
	}

	
	public void setMetadataAuthor(String metadataAuthor) {
		this.metadataAuthor = metadataAuthor;
	}

	
	public String getMetadataKeywords() {
		return this.metadataKeywords;
	}

	
	public void setMetadataKeywords(String metadataKeywords) {
		this.metadataKeywords = metadataKeywords;
	}

	
	public String getMetadataTitle() {
		return this.metadataTitle;
	}

	
	public void setMetadataTitle(String metadataTitle) {
		this.metadataTitle = metadataTitle;
	}

	
	public String getMetadataSubject() {
		return this.metadataSubject;
	}

	
	public void setMetadataSubject(String metadataSubject) {
		this.metadataSubject = metadataSubject;
	}
	
	public static int getPageCountTif(File file) throws IOException {
		RandomAccessSource source = createRamdomAccessSource(file);
		RandomAccessFileOrArray ramdomAccess = new RandomAccessFileOrArray(source);
		return TiffImage.getNumberOfPages(ramdomAccess);
	}
	
	public static int getPageCountPdf(byte[] contenido) {
		return getPageCountPdf(new ByteArrayInputStream(contenido));
	}
	
	public static int getPageCountPdf(File file) throws IOException {
		try (InputStream is = new FileInputStream(file)){
			return getPageCountPdf(is);
		}
	}
	
	private static int getPageCountPdf(InputStream is) {
		PdfReader pdfReader = null;
		try {
			pdfReader = new PdfReader(is);
		
			return pdfReader.getNumberOfPages();
		}
		catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		finally {
			if (pdfReader!= null) {
				pdfReader.close();
			}
		}
	}
	 
}