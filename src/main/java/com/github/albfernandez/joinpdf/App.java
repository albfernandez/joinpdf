package com.github.albfernandez.joinpdf;

import java.io.File;

/**
 *
 */
public class App {
    public static void main( String[] args ) throws Exception {
    	if (args.length < 2) {
    		System.out.println("Debe indicar archivos");
    		return;
    	}
    	JoinPdf join = new JoinPdf();
    	for (int i = 0; i <= args.length -2; i++){
    		join.addFile(new File(args[i]));
    	}
    	join.export(new File(args[args.length-1]));
    }
}
