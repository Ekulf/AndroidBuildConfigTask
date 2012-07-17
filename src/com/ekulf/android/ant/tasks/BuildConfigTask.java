package com.ekulf.android.ant.tasks;

import java.io.File;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;

import com.ekulf.android.ant.tasks.BuildConfigGenerator.Market;

public class BuildConfigTask extends Task {

    private String mPreviousBuildType;
    private String mBuildType;
    private String mGenFolder;
    private String mAppPackage;
    private Market mMarket;

    public void setGenFolder(Path path) {
        mGenFolder = checkSinglePath("genFolder", path);
    }

    public void setPackage(String appPackage) {
        mAppPackage = appPackage;
    }

    public void setMarket(String market) {
        mMarket = Market.createFromString(market);
    }

    @Override
    public void execute() throws BuildException {
        if (mGenFolder == null) {
            throw new BuildException("Missing attribute genFolder");
        }
        if (mAppPackage == null) {
            throw new BuildException("Missing attribute package");
        }

        final BuildConfigGenerator generator =
                new BuildConfigGenerator(
                        mGenFolder,
                        mAppPackage,
                        Boolean.parseBoolean(getBuildType()),
                        mMarket);

        // first check if the file is missing.
        final File buildConfigFile = generator.getBuildConfigFile();
        final boolean missingFile = buildConfigFile.exists() == false;

        if (missingFile || hasBuildTypeChanged()) {
            if (isNewBuild()) {
                System.out.println("Generating BuildConfig class.");
            } else if (missingFile) {
                System.out.println("BuildConfig class missing: Generating new BuildConfig class.");
            } else {
                System.out.println("Build type changed: Generating new BuildConfig class.");
            }

            try {
                generator.generate();
            } catch (final IOException e) {
                throw new BuildException("Failed to create BuildConfig class", e);
            }
        } else {
            System.out.println("No need to generate new BuildConfig.");
        }
    }

    /** Sets the current build type */
    public void setBuildType(String buildType) {
        mBuildType = buildType;
    }

    /** Sets the previous build type */
    public void setPreviousBuildType(String previousBuildType) {
        mPreviousBuildType = previousBuildType;
    }

    private String getBuildType() {
        return mBuildType;
    }

    /**
     * Returns if it is a new build. If the build type is not input from the XML, this always returns true. A build type is
     * defined by having an empty previousBuildType.
     */
    private boolean isNewBuild() {
        return mBuildType == null || mPreviousBuildType.length() == 0;
    }

    /**
     * Returns true if the build type changed.
     */
    private boolean hasBuildTypeChanged() {
        // no build type? return false as the feature is simply not used
        if (mBuildType == null && mPreviousBuildType == null) {
            return false;
        }

        return mBuildType.equals(mPreviousBuildType) == false;
    }

    static String checkSinglePath(String attribute, Path path) {
        final String[] paths = path.list();
        if (paths.length != 1) {
            throw new BuildException(String.format(
                    "Value for '%1$s' is not valid. It must resolve to a single path", attribute));
        }

        return paths[0];
    }
}
