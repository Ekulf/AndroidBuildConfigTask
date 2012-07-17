package com.ekulf.android.ant.tasks;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class BuildConfigGenerator {
    public final static String BUILD_CONFIG_NAME = "BuildConfig.java";

    private final static String PH_PACKAGE = "#PACKAGE#";
    private final static String PH_DEBUG = "#DEBUG#";
    private final static String PH_MARKET = "#MARKET#";

    private final String mGenFolder;
    private final String mAppPackage;
    private final boolean mDebug;
    private final Market mMarket;

    public BuildConfigGenerator(String genFolder, String appPackage, boolean debug, Market market) {
        mGenFolder = genFolder;
        mAppPackage = appPackage;
        mDebug = debug;
        mMarket = market;
    }

    public File getFolderPath() {
        final File genFolder = new File(mGenFolder);
        return new File(genFolder, mAppPackage.replace('.', File.separatorChar));
    }

    public File getBuildConfigFile() {
        final File folder = getFolderPath();
        return new File(folder, BUILD_CONFIG_NAME);
    }

    public void generate() throws IOException {
        final String template = readEmbeddedTextFile("BuildConfig.template");

        final Map<String, String> map = new HashMap<String, String>();
        map.put(PH_PACKAGE, mAppPackage);
        map.put(PH_DEBUG, Boolean.toString(mDebug));
        map.put(PH_MARKET, mMarket.name());

        final String content = replaceParameters(template, map);

        final File pkgFolder = getFolderPath();
        if (pkgFolder.isDirectory() == false) {
            pkgFolder.mkdirs();
        }

        final File buildConfigJava = new File(pkgFolder, BUILD_CONFIG_NAME);
        writeFile(buildConfigJava, content);
    }

    private String readEmbeddedTextFile(String filepath) throws IOException {
        final InputStream is = BuildConfigGenerator.class.getResourceAsStream(filepath);
        if (is != null) {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            String line;
            final StringBuilder total = new StringBuilder(reader.readLine());
            while ((line = reader.readLine()) != null) {
                total.append('\n');
                total.append(line);
            }

            return total.toString();
        }

        throw new IOException("BuildConfig template is missing!");
    }

    private void writeFile(File file, String content) throws IOException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            final InputStream source = new ByteArrayInputStream(content.getBytes("UTF-8"));

            final byte[] buffer = new byte[1024];
            int count = 0;
            while ((count = source.read(buffer)) != -1) {
                fos.write(buffer, 0, count);
            }
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    private String replaceParameters(String str, Map<String, String> parameters) {

        for (final Entry<String, String> entry : parameters.entrySet()) {
            final String value = entry.getValue();
            if (value != null) {
                str = str.replaceAll(entry.getKey(), value);
            }
        }

        return str;
    }

    public enum Market {
        PLAY, AMAZON, OUYA;

        public static Market createFromString(String name) {
            if (name == null || name.length() == 0) {
                return Market.PLAY;
            }

            return Enum.valueOf(Market.class, name.toUpperCase());
        }
    }
}
