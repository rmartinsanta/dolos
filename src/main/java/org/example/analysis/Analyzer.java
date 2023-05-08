package org.example.analysis;

import me.tongfei.progressbar.ProgressBar;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.tools.ExtractImages;
import org.example.Args;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.*;
import java.util.function.Function;

public class Analyzer {

    private static final String EMPTY_SHA1 = "DA39A3EE5E6B4B0D3255BFEF95601890AFD80709";
    private Args args;

    private Map<String, String> excludedHashes = new HashMap<>();
    private Map<String, String> existingHashes = new HashMap<>();
    private Map<String, Set<String>> currentHashes = new HashMap<>();

    private String currentPath = new File("").getAbsolutePath();

    public Analyzer(Args args) {
        this.args = args;
    }

    public void analyze() throws IOException {
        if (args.getExcludedImages() != null) {
            extractImages(args.getExcludedImages(), "Extract excluded");
            this.excludedHashes = parseFolder(new File(args.getExcludedImages()), "Load excluded files");
            System.out.format("Loaded %s excluded files in %s %n", this.excludedHashes.size(), args.getExcludedImages());
        }

        if (args.getReferenceImages() != null) {
            extractImages(args.getReferenceImages(), "Extract reference");
            this.existingHashes = parseFolder(new File(args.getReferenceImages()), "Load reference files");
            System.out.format("Analyzed %s existing files in %s %n", this.existingHashes.size(), args.getReferenceImages());
        }

        Function<String, HashSet<String>> hashsetCreator = (s) -> new HashSet<>();
        var studentFolders = Files.list(Path.of(args.getPathToAnalyze())).map(Path::toFile).filter(File::isDirectory).toList();
        var students = new ArrayList<Student>(studentFolders.size());
        for (var folder : ProgressBar.wrap(studentFolders, "Load students")) {
            extractImages(folder, null);
            students.add(loadStudent(folder));
        }

        // Prepare dataset
        for (var student : ProgressBar.wrap(students, "Merge dataset")) {
            for (var e : student.files.entrySet()) {
                var hash = e.getKey();
                this.currentHashes.computeIfAbsent(hash, hashsetCreator);
                this.currentHashes.get(hash).add(e.getValue());
            }
        }

        List<Match> matches = new ArrayList<>();
        // Analysis
        for (var student : ProgressBar.wrap(students, "Find duplicat")) {
            for (var e : student.files.entrySet()) {
                var hash = e.getKey();
                if (this.excludedHashes.containsKey(hash)) {
                    continue;
                }
                if (this.existingHashes.containsKey(hash)) {
                    //System.out.printf("Student %s image %s found in existing images %s%n", student.path, hash, existingHashes.get(hash));
                    matches.add(new Match(student.path, hash, e.getValue(), Set.of(this.existingHashes.get(hash))));
                }

                if (this.currentHashes.get(hash).size() > 1) {
                    var otherNames = new HashSet<>(this.currentHashes.get(hash));
                    otherNames.remove(e.getValue());
                    matches.add(new Match(student.path, hash, e.getValue(), otherNames));
                    //System.out.printf("Student %s image %s found multiple times: %s%n", student.path, e.getValue(), otherNames);
                }
            }
        }
        export(matches);
    }

    private void extractImages(String path, String task) {
        extractImages(new File(path), task);
    }

    private void extractImages(File f, String task) {
        Iterable<File> files = FileUtils.listFiles(f, new String[]{"pdf"}, true);
        if (task != null) {
            files = ProgressBar.wrap(files, task);
        }
        for (var file : files) {
            try {
                ExtractImages.main(new String[]{file.getAbsolutePath()});
            } catch (IOException e) {
                System.err.format("%n---------------%n[ERROR] Failed to extract images from file %s, ignoring file, possibly corrupted. Exception message: %s%n---------------%n%n%n", file, e.getLocalizedMessage());
            }
        }
    }

    public void export(List<Match> matches) {
        System.out.println("--------------------------------");
        System.out.println("> RESULTS");
        System.out.println("--------------------------------");
        for (var m : matches) {
            System.out.println(m);
        }
        System.out.println("> End of results");

    }

    public HashedFile sha1File(File file) {
        try (InputStream fis = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            int n = 0;
            byte[] buffer = new byte[8192];
            while (n != -1) {
                n = fis.read(buffer);
                if (n > 0) {
                    digest.update(buffer, 0, n);
                }
            }
            var hash = digest.digest();
            var hashString = bytesToHex(hash);
            return new HashedFile(hashString, file.getAbsolutePath().replace(this.currentPath, ""));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public HashedFile sha1Img(File file) {
        var simplifiedPath = file.getAbsolutePath().replace(this.currentPath, "");
        try {
            BufferedImage img = ImageIO.read(file);
            if (img == null) {
                return new HashedFile(EMPTY_SHA1, simplifiedPath);
            }
            int w = img.getWidth(), h = img.getHeight();
            int[] data = img.getRaster().getPixels(0, 0, w, h, (int[]) null);
            byte[] bytes = new byte[data.length * 4];
            for (int i = 0; i < data.length; i+=4) {
                bytes[i+0] = (byte)(data[i] >> 0);
                bytes[i+1] = (byte)(data[i] >> 8);
                bytes[i+2] = (byte)(data[i] >> 16);
                bytes[i+3] = (byte)(data[i] >> 24);
            }
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.update(bytes);
            var hash = digest.digest();
            var hashString = bytesToHex(hash);
            return new HashedFile(hashString, simplifiedPath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, String> parseFolder(File f, String task) {
        Iterable<File> files = FileUtils.listFiles(f, new String[]{"png", "jpg", "jpeg"}, true);
        if (task != null) {
            files = ProgressBar.wrap(files, task);
        }
        Map<String, String> result = new HashMap<>();
        for (var file : files) {
            var hashedFile = args.isImgHash()? sha1Img(file): sha1File(file);
            if (!hashedFile.hash.equals(EMPTY_SHA1)) {
                result.put(hashedFile.hash(), hashedFile.path());
            }
        }
        return result;
    }

    private Student loadStudent(File file) {
        var hashes = parseFolder(file, null);
        return new Student(file.getName(), hashes);
    }

    public record Student(String path, Map<String, String> files) {
    }

    public record HashedFile(String hash, String path) {
    }

    public record Match(String student, String hash, String file, Set<String> otherMatches) {

    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

//    public ProgressBarBuilder getPbb(){
//        return new ProgressBarBuilder()
//                .setStyle(ProgressBarStyle.ASCII)
//                .showSpeed();
//    }
}