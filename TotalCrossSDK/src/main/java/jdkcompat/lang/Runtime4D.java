// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package jdkcompat.lang;

import java.io.File;
import java.util.ArrayList;

import totalcross.sys.Convert;

public class Runtime4D {

    private static Runtime4D instance;

    private Runtime4D() {

    }

    public static Runtime4D getRuntime() {
        if(instance == null) {
            instance = new Runtime4D();
        }
        return instance;
    }

    native private Process exec(String[] cmdarray, String[] envp, String dirPath);

    public Process exec(String[] cmdarray, String[] envp, File dir) {
        return exec(cmdarray, envp, dir.getPath());
    }

    public Process exec(String[] cmdarray, String[] envp) {
        String dirPath = null;
        return exec(cmdarray, envp, dirPath);
    }

    public Process exec(String[] cmdarray) {
        return exec(cmdarray, null);
    }

    public Process exec(String command, String[] envp, File dir) {
        return exec(Convert.tokenizeString(command, ' '), envp, dir.getPath());
    }

    public Process exec(String command, String[] envp) {
        String dirPath = null;
        return exec(Convert.tokenizeString(command, ' '), envp, dirPath);
    }

    public Process exec(String command) {
        return exec(command, null);
    }
}