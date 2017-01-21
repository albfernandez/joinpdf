package com.github.albfernandez.joinpdf;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfReader;

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


public final class ItextUtils {

    private ItextUtils() {
        throw new AssertionError("No instances allowed");
    }

    public static void close(final Document document) {
        try {
            if (document != null) {
                document.close();
            }
        } catch (Exception e) { // NOPMD
            //
        }
    }

    public static void close(final PdfReader reader) {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (Exception e) { // NOPMD
            //
        }
    }
}
