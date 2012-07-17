/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Modified to include market build info.

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
    private Market mPreviousMarket;

    public void setGenFolder(Path path) {
        mGenFolder = checkSinglePath("genFolder", path);
    }

    public void setPackage(String appPackage) {
        mAppPackage = appPackage;
    }

    public void setMarket(String market) {
        mMarket = Market.createFromString(market);
    }

    public void setPreviousMarket(String market) {
        if (market != null && market.length() > 0) {
            mPreviousBuildType = market;
        }
    }

    public void setBuildType(String buildType) {
        mBuildType = buildType;
    }

    public void setPreviousBuildType(String previousBuildType) {
        mPreviousBuildType = previousBuildType;
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
                System.out.println("BuildConfig class error: " + e.toString());
                throw new BuildException("Failed to create BuildConfig class", e);
            }
        } else {
            System.out.println("No need to generate new BuildConfig.");
        }
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

        if (mPreviousMarket == null || mMarket == null) {
            return true;
        }

        return (!mBuildType.equals(mPreviousBuildType) || !mMarket.equals(mPreviousMarket));
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
