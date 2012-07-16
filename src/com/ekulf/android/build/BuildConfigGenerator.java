package com.ekulf.android.build;

public class BuildConfigGenerator {
    public final static String BUILD_CONFIG_NAME = "BuildConfig.java";

    private final static String PH_PACKAGE = "#PACKAGE#";
    private final static String PH_DEBUG = "#DEBUG#";
    private final static String PH_MARKET = "#MARKET#";
    
    private final String mGenFolder;
    private final String mAppPackage;
    private final boolean mDebug;
    private final int mMarketRelase;
    
    public BuildConfigGenerator(String genFolder, String appPackage, boolean debug, int marketRelase) {
        mGenFolder = genFolder;
        mAppPackage = appPackage;
        mDebug = debug;
        mMarketRelase = marketRelase;
    }
}
