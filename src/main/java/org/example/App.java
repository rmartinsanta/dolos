package org.example;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import org.example.analysis.Analyzer;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Extract files to analyze before executing, for example images from PDFs: java -jar pdfbox-app-3.0.0-alpha3.jar export:images -i=file.pdf
 *
 */
public class App {
    public static void main(String[] argv) throws IOException {
        var args = parseArgs(argv);
        // remove info and warning logs from pdfbox

        Logger.getLogger("org.apache.pdfbox").setLevel(Level.SEVERE);
        new Analyzer(args).analyze();
        //ExtractImages.main(args.getPathToAnalyze());
    }

    public static Args parseArgs(String[] argv) {
        var args = new Args();
        var jc = JCommander.newBuilder().addObject(args).build();
        try {
            jc.parse(argv);
            return args;
        } catch (ParameterException e) {
            System.err.println(e.getLocalizedMessage());
            jc.usage();
            System.exit(-1);
            return null; // Unreachable
        }
    }
}
