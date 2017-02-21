/*
 (C) Copyright 2014-2017 Alberto Fernández <infjaf@gmail.com>

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

package com.github.albfernandez.joinpdf;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.RandomAccessFileOrArray;
import com.lowagie.text.pdf.codec.TiffImage;


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
    
    private Rectangle pageSize = PageSize.A4;

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

    public JoinPdf(final List<File> documentFilesFiles) {
        addFiles(documentFilesFiles);
    }

    public JoinPdf(final File dir) {
        addDir(dir);
    }

    public final void addDir(final File dir) {
        if (dir != null && dir.isDirectory()) {
            File[] documents = dir.listFiles(new JoinPdfFileFilter());
            if (documents != null) {
                for (File document : documents) {
                    addFile(document);
                }
            }
        }
    }

    public final void addFile(final File document) {
        if (document != null && document.canRead() && document.isFile()) {
            this.files.add(document);
        }
    }

    public final void addFiles(final List<File> documents) {
        for (File document : documents) {
            addFile(document);
        }
    }

    
    public final synchronized void export(final OutputStream os) throws Exception {
        checkParameters();
        Document document = new Document();

        try {
            if (isPrintPageNumbers()) {
                this.totalPages = geTotalPageCount();
                this.actualPage = 0;
            }
            PdfWriter writer = PdfWriter.getInstance(document, os);
            setParametersAndHeaders(writer, document);
            document.open();
            for (File file : this.files) {
                add(file, document, writer);
            }
        } finally {
            ItextUtils.close(document);
        }
    }

    private void add(final File file, final Document document, final PdfWriter writer)
            throws Exception {
        String fileName = StringUtils.lowerCase(file.getName(), LOCALE_ES);
        if (fileName.endsWith(".tif") || fileName.endsWith(".tiff")) {
            addTiff(file, document, writer);
        } else if (fileName.endsWith(".pdf")) {
            addPdf(file, document, writer);
        } else {
            addImage(file, document, writer);
        }
    }

    private void addImage(final File file, final Document document, final PdfWriter writer)
            throws Exception {
        Image image = Image.getInstance(Files.readAllBytes(file.toPath()));
        addImage(image, document, writer);
    }

    private void addImage(final Image image, final Document document, final PdfWriter writer)
            throws Exception {
        if (image.getWidth() > image.getHeight()) {
            document.setPageSize(new Rectangle(this.pageSize.getHeight(), this.pageSize.getWidth()));
        } else {
            document.setPageSize(new Rectangle(this.pageSize.getWidth(), this.pageSize.getHeight()));
        }
        image.scaleToFit(document.getPageSize().getWidth() - this.margin * 2f,
                document.getPageSize().getHeight() - this.margin * 2f);
        float px = (document.getPageSize().getWidth() - image.getScaledWidth()) / 2f;
        float py = (document.getPageSize().getHeight() - image.getScaledHeight()) / 2f;
        image.setAbsolutePosition(px, py);
        document.newPage();
        document.add(image);
        writePageNumber(writer);
    }

    private void writePageNumber(final PdfWriter writer) throws Exception {
        if (isPrintPageNumbers()) {
            writePageNumber(writer.getDirectContent());
        }
    }

    private void writePageNumber(final PdfContentByte cb) throws DocumentException,
            IOException {
        if (isPrintPageNumbers()) {
            Rectangle size = cb.getPdfDocument().getPageSize();
            BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA,
                    BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            this.actualPage++;
            cb.beginText();
            cb.setFontAndSize(bf, 9);
            cb.showTextAligned(PdfContentByte.ALIGN_CENTER, this.actualPage
                    + " of " + this.totalPages, size.getWidth() / 2, 10, 0);
            cb.endText();
        }
    }

    private void addPdf(final File file, final Document document, final PdfWriter writer)
            throws Exception {
        PdfReader pdfReader = null;
        try (InputStream is = new FileInputStream(file)) {
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
        } finally {
            if (pdfReader != null) {
                writer.freeReader(pdfReader);
                ItextUtils.close(pdfReader);
            }
        }
    }

    private int geTotalPageCount() throws IOException {
        int pages = 0;
        for (File file : this.files) {
            pages += getPageCount(file);
        }
        return pages;
    }

    private int getPageCount(final File file) throws IOException {
        String fileName = StringUtils.lowerCase(file.getName(), LOCALE_ES);
        if (fileName.endsWith(".tif") || fileName.endsWith(".tiff")) {
            return getPageCountTif(file);
        }
        if (fileName.endsWith(".pdf")) {
            return getPageCountPdf(file);
        }
        return 1;

    }

    private void addTiff(final File file, final Document document, final PdfWriter writer)
            throws Exception {
        RandomAccessFileOrArray randomAccess = createRamdomAccessSource(file);
       
        int pages = getPageCount(file);
        for (int i = 1; i <= pages; i++) {
            Image image = TiffImage.getTiffImage(randomAccess, i);
            addImage(image, document, writer);
        }

    }

    private static RandomAccessFileOrArray createRamdomAccessSource(final File file)
            throws IOException {
        return new RandomAccessFileOrArray(file.getAbsolutePath(),false, Document.plainRandomAccess);        
    }

    public final void export(final File file) throws Exception {
        try (OutputStream os = new FileOutputStream(file)) {
            export(os);
        }
    }

    public final float getMargin() {
        return this.margin;
    }

    public final void setMargin(final float newMargin) {
        this.margin = newMargin;
    }

    public final boolean isExtraCompression() {
        return this.extraCompression;
    }

    public final void setExtraCompression(final boolean newExtraCompression) {
        this.extraCompression = newExtraCompression;
    }

    public final boolean isCrypt() {
        return this.crypt;
    }

    public final void setCrypt(final boolean newCrypt) {
        this.crypt = newCrypt;
    }

    public final boolean isPrintPageNumbers() {
        return this.printPageNumbers;
    }

    public final void setPrintPageNumbers(final boolean newPageNumbers) {
        this.printPageNumbers = newPageNumbers;
    }

    private void setParametersAndHeaders(final PdfWriter writer, final Document document)
            throws DocumentException {
        if (this.extraCompression) {
            writer.setFullCompression();
        }
        if (this.isCrypt() && bouncyCastleLoaded) {
            int permisos = PdfWriter.ALLOW_PRINTING;
            writer.setEncryption(null, null, permisos,
                    PdfWriter.ENCRYPTION_AES_128);
        }
        if (!StringUtils.isBlank(this.getMetadataAuthor())) {
            document.addAuthor(this.getMetadataAuthor());
        }
        if (!StringUtils.isBlank(this.getMetadataKeywords())) {
            document.addKeywords(this.getMetadataKeywords());
        }
        if (!StringUtils.isBlank(this.getMetadataTitle())) {
            document.addTitle(this.getMetadataTitle());
        }
        if (!StringUtils.isBlank(this.getMetadataSubject())) {
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

    public final String getMetadataAuthor() {
        return this.metadataAuthor;
    }

    public final void setMetadataAuthor(final String newMetadataAuthor) {
        this.metadataAuthor = newMetadataAuthor;
    }

    public final String getMetadataKeywords() {
        return this.metadataKeywords;
    }

    public final void setMetadataKeywords(final String newMetadataKeywords) {
        this.metadataKeywords = newMetadataKeywords;
    }

    public final String getMetadataTitle() {
        return this.metadataTitle;
    }

    public final void setMetadataTitle(final String newMetadataTitle) {
        this.metadataTitle = newMetadataTitle;
    }

    public final String getMetadataSubject() {
        return this.metadataSubject;
    }

    public final void setMetadataSubject(final String newMetadataSubject) {
        this.metadataSubject = newMetadataSubject;
    }

    public static int getPageCountTif(final File file) throws IOException {
        RandomAccessFileOrArray randomAccess = createRamdomAccessSource(file);
        return TiffImage.getNumberOfPages(randomAccess);
    }

    public static int getPageCountPdf(final byte[] contenido) {
        return getPageCountPdf(new ByteArrayInputStream(contenido));
    }

    public static int getPageCountPdf(final File file) throws IOException {
        try (InputStream is = new FileInputStream(file)) {
            return getPageCountPdf(is);
        }
    }

    private static int getPageCountPdf(final InputStream is) {
        PdfReader pdfReader = null;
        try {
            pdfReader = new PdfReader(is);
            return pdfReader.getNumberOfPages();
        } catch (Exception e) {
            return 0;
        } finally {
            ItextUtils.close(pdfReader);
        }
    }

	public Rectangle getPageSize() {
		return this.pageSize;
	}

	public void setPageSize(Rectangle pageSize) {
		this.pageSize = pageSize;
	}
    
    

}
