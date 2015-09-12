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

/**
 *
 */
public final class App {
    private App() {
        throw new AssertionError("No instances allowed");
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("Debe indicar archivos");
            return;
        }
        JoinPdf join = new JoinPdf();
        for (int i = 0; i <= args.length - 2; i++) {
            join.addFile(new File(args[i]));
        }
        join.export(new File(args[args.length - 1]));
    }
}
