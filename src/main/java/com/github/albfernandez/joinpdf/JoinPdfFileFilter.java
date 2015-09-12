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
import java.io.File;
import java.io.FileFilter;

import org.apache.commons.lang3.StringUtils;

public class JoinPdfFileFilter implements FileFilter {

    public JoinPdfFileFilter() {
        super();
    }

    public final boolean accept(final File pathname) {
        if (pathname == null) {
            return false;
        }
        String fileName = StringUtils.lowerCase(pathname.getName(),
                JoinPdf.LOCALE_ES);
        return fileName.endsWith("pdf") || fileName.endsWith("png")
                || fileName.endsWith("jpg") || fileName.endsWith("jpeg")
                || fileName.endsWith("tiff") || fileName.endsWith("tif");

    }

}
