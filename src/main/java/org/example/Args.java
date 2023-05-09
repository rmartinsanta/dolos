package org.example;

import com.beust.jcommander.Parameter;

public class Args {
    @Parameter(names = "-i", description = "Folder containing one folder for each student. Each student folder contains either a PDF or a set of images, will be recursively enumerated if it contains directories.", validateWith = PathValidator.class, required = true)
    private String pathToAnalyze;

    @Parameter(names = "-e", description = "Path containing excluded images that should be ignored if found inside the folder to analyze.", validateWith = PathValidator.class)
    private String excludedImages;

    @Parameter(names = "-r", description = "Path containing PDFs from which images will be extracted and used to compare against, but they themselves should not compared against the rest. Useful for example to provide submissions from other years. Will be recursively enumerated.", validateWith = PathValidator.class)
    private String referenceImages;

    @Parameter(names = "-pixelmatch", description = "Match image content using pixel data or match whole file. By default, if this parameter is not provided, matches whole files.")
    private boolean imgHash;
    public Args() {}

    public String getPathToAnalyze() {
        return pathToAnalyze;
    }

    public void setPathToAnalyze(String pathToAnalyze) {
        this.pathToAnalyze = pathToAnalyze;
    }

    public String getExcludedImages() {
        return excludedImages;
    }

    public void setExcludedImages(String excludedImages) {
        this.excludedImages = excludedImages;
    }

    public String getReferenceImages() {
        return referenceImages;
    }

    public void setReferenceImages(String referenceImages) {
        this.referenceImages = referenceImages;
    }

    public boolean isImgHash() {
        return imgHash;
    }

    public void setImgHash(boolean imgHash) {
        this.imgHash = imgHash;
    }
}
