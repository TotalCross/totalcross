// Copyright (C) 2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import totalcross.json.JSONObject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

import tc.tools.AnonymousUserData;

public class AnonymousUserDataTest {
    static AnonymousUserData anonymousUserData;
    static String configDirPath;
    static String configPath;

    @BeforeEach
    void setup() {
        anonymousUserData = AnonymousUserData.instance();
        configDirPath = Paths.get(System.getProperty("user.home"), "tc-test").toAbsolutePath().toString();
        configPath = Paths.get(configDirPath, "config.json").toAbsolutePath().toString();
        anonymousUserData.setConfigDirPath(configDirPath);
        new File(configDirPath).delete();
        anonymousUserData.setResponseRequester(() -> {
            return true;
        });
        anonymousUserData.setBaseUrl("https://aqueous-plateau-93003.herokuapp.com/api/v1");
        anonymousUserData.loadConfiguration();
    }

    @AfterEach
    void finish() {
        new File(configDirPath).delete();
    }

    @Test
    void shouldCreateAConfigFile() throws IOException {
        
        anonymousUserData.launcher("/src", "200x800");
        assertEquals(true, new File(configPath).exists(), "file should be created");

        File file = new File(configPath);
        FileInputStream fis = new FileInputStream(file);
        byte[] data = new byte[(int) file.length()];
        fis.read(data);
        fis.close();

        String str = new String(data, "UTF-8");
        JSONObject object = new JSONObject(str);

        assertEquals(true, object.opt("userAcceptedToProvideAnonymousData"), "user accepted should be true");
        assertNotNull(object.opt("uuid"), "it should contains an uuid");

    }

    @Test
    void shouldUseInformationFromExistingConfigFile() throws IOException {
        // Create config file
        File f = new File(configPath);
        String existingToken = "cf871c5e-dd38-4ab8-9796-48b79d076d59"; // existing on stage server
        f.createNewFile();
        try {
            FileWriter myWriter = new FileWriter(f);
            myWriter.write("{\"userAcceptedToProvideAnonymousData\":true," +
                    "\"uuid\": \"" + existingToken + "\"}");
            myWriter.close();
          } catch (IOException e) {
            e.printStackTrace();
          }

        // Use config file
        anonymousUserData.loadConfiguration();
        anonymousUserData.launcher("/src", "200x800");
        assertEquals(true, 
            anonymousUserData.getConfig().get("userAcceptedToProvideAnonymousData"),
                "user agreement should come from config file");
        assertEquals(existingToken, anonymousUserData.getConfig().get("uuid"),
            "should use existing token");
    }
 
}
