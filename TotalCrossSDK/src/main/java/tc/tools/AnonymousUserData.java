// Copyright (C) 2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import net.harawata.appdirs.AppDirsFactory;
import totalcross.io.ByteArrayStream;
import totalcross.io.Stream;
import totalcross.json.JSONObject;
import totalcross.net.HttpStream;
import totalcross.net.URI;
import totalcross.net.ssl.SSLSocketFactory;
import totalcross.sys.Settings;

public class AnonymousUserData {

    private static final String BASE_URL = "http://aqueous-plateau-93003.herokuapp.com/api/v1/";
    private static final String GET_UUID = BASE_URL + "users/get-anonymous-uuid";
    private static final String POST_LAUNCHER = BASE_URL + "launch";
    private static final String POST_DEPLOY = BASE_URL + "deploy";

    private static AnonymousUserData instance;
    private final String ID;

    private JSONObject config = new JSONObject();

    private SimpleDateFormat sdf;

    private AnonymousUserData() {
        ID = getStoredActivationKey();
        if (ID != null) {
            config.put("userUuid", ID);
        }
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
    }

    public static AnonymousUserData getInstance() {
        if (instance == null) {
            instance = new AnonymousUserData();
        }
        return instance;
    }

    public static String getStoredActivationKey() {
        String activationKey = null;
        final File configDir = new File(AppDirsFactory.getInstance().getUserConfigDir("TotalCross", null, null)
                .replace("Application Support", "Preferences")); // this replace only works on macos
        configDir.mkdirs();
        final File configFile = new File(configDir, "config.json");
        try (FileInputStream fis = new FileInputStream(configFile)) {
            ByteArrayStream bas = new ByteArrayStream(4096);
            bas.readFully(Stream.asStream(fis), 10, 4096);
            JSONObject o = new JSONObject(new String(bas.getBuffer(), 0, bas.available(), "UTF-8"));
            activationKey = o.optString("uuid");
        } catch (FileNotFoundException e) {
            HttpStream.Options options = new HttpStream.Options();
            // options.socketFactory = new SSLSocketFactory();
            try (HttpStream hs = new HttpStream(new URI(GET_UUID), options)) {
                ByteArrayStream bas = new ByteArrayStream(4096);
                bas.readFully(hs, 10, 4096);
                JSONObject o = new JSONObject(new String(bas.getBuffer(), 0, bas.available(), "UTF-8"));
                activationKey = o.optString("uuid");

                try (FileOutputStream fos = new FileOutputStream(configFile)) {
                    fos.write(bas.getBuffer(), 0, bas.available());
                }
            } catch (java.io.IOException e1) {
                e1.printStackTrace();
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        return activationKey;
    }

    public void launcher(String clazz, String... args) {
        HttpStream.Options options = new HttpStream.Options();
        // options.socketFactory = new SSLSocketFactory();
        options.httpType = HttpStream.POST;
        options.postHeaders.put("accept", "application/json");
        options.postHeaders.put("Content-Type", "application/json");
        JSONObject dataJson = new JSONObject(config, new String[] { "userUuid" });
        dataJson.put("os", System.getProperty("os.name"));
        dataJson.put("tc_version", Settings.versionStr);
        dataJson.put("date", sdf.format(new Date()));
        dataJson.put("args", clazz + " " + Arrays.toString(args));
        options.data = dataJson.toString();

        try (HttpStream hs = new HttpStream(new URI(POST_LAUNCHER), options)) {
        } catch (java.io.IOException e1) {
            e1.printStackTrace();
        }
    }

    public void deploy(String... args) {
        HttpStream.Options options = new HttpStream.Options();
        options.socketFactory = new SSLSocketFactory();
        options.httpType = HttpStream.POST;
        JSONObject dataJson = new JSONObject(config, new String[] { "userUuid" });
        dataJson.put("os", System.getProperty("os.name"));
        dataJson.put("tc_version", Settings.versionStr);
        dataJson.put("date", sdf.format(new Date()));
        dataJson.put("args", Arrays.toString(args));
        options.data = dataJson.toString();

        try (HttpStream hs = new HttpStream(new URI(POST_DEPLOY), options)) {
        } catch (java.io.IOException e1) {
            e1.printStackTrace();
        }
    }

}