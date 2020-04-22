package jdkcompat.lang;

import java.io.File;

public class Runtime4D {

    
    native public Process exec(String[] cmdarray, String[] envp, String dirPath);

}