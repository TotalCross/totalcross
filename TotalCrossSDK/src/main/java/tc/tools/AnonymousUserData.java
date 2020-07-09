// Copyright (C) 2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools;

import java.awt.GraphicsEnvironment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JOptionPane;

import net.harawata.appdirs.AppDirsFactory;
import totalcross.io.ByteArrayStream;
import totalcross.io.Stream;
import totalcross.json.JSONObject;
import totalcross.net.HttpStream;
import totalcross.net.URI;
import totalcross.net.ssl.SSLSocketFactory;
import totalcross.sys.Settings;

public class AnonymousUserData {

    private static final String BASE_URL = "https://aqueous-plateau-93003.herokuapp.com/api/v1/";
    private static final String GET_UUID = BASE_URL + "users/get-anonymous-uuid";
    private static final String POST_LAUNCHER = BASE_URL + "launch";
    private static final String POST_DEPLOY = BASE_URL + "deploy";

    private static final String POPUP_TEXT = "We'd like to collect anonymous telemetry data to help us prioritize \n"
            + "improvements. This includes how often you deploy and launches \n"
            + "(on simulator) your app, witch OS you deploy for, your timezone and \n"
            + "which totalcross version you're using. We do not collect any personal \n"
            + "data or sensitive information. Do you allow TotalCross to send us \n" + "anonymous report?";

    private static AnonymousUserData instance;

    private static File configFile;

    private JSONObject config;

    private SimpleDateFormat sdf;

    private AnonymousUserData() {
        config = loadConfiguration();
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
    }

    public static AnonymousUserData instance() {
        if (instance == null) {
            instance = new AnonymousUserData();
        }
        return instance;
    }

    /**
     * Always returns a valid config object. An empty one is returned if anything
     * fails, not exception is thrown.
     * 
     * This must be transparent for the user, no exception or stack trace should
     * interrupt the usage of the SDK or pollute the standard output.
     */
    public static JSONObject loadConfiguration() {
        JSONObject config = null;
        final File configDir = new File(AppDirsFactory.getInstance().getUserConfigDir("TotalCross", null, null)
                .replace("Application Support", "Preferences")); // this replace only works on macos
        configDir.mkdirs();
        configFile = new File(configDir, "config.json");
        try (FileInputStream fis = new FileInputStream(configFile)) {
            config = readJsonObject(Stream.asStream(fis));
        } catch (FileNotFoundException e) {
            HttpStream.Options options = new HttpStream.Options();
            options.socketFactory = new SSLSocketFactory();
            try (HttpStream hs = new HttpStream(new URI(GET_UUID), options)) {
                config = readJsonObject(hs);
                config.put("userUuid", config.remove("uuid"));

                try (PrintWriter writer = new PrintWriter(configFile)) {
                    writer.write(config.toString());
                }
            } catch (java.io.IOException e1) {
                e1.printStackTrace();
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        return config == null ? new JSONObject() : config;
    }

    private static JSONObject readJsonObject(Stream stream) throws IOException {
        try (ByteArrayStream bas = new ByteArrayStream(4096)) {
            bas.readFully(stream, 10, 4096);
            JSONObject jsonObject = new JSONObject(new String(bas.getBuffer(), 0, bas.available(), "UTF-8"));
            return jsonObject;
        }
    }

    public void launcher(String clazz, String... args) {
        if (!GraphicsEnvironment.isHeadless() && config.isNull("userAcceptedToProvideAnonymousData")) {
            final String[] options = new String[] { "Yes, send anonymous reports", "Don't send" };
            int dialogResult = JOptionPane.showOptionDialog(null, POPUP_TEXT, "", JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
            if (dialogResult == JOptionPane.YES_OPTION) {
                config.put("userAcceptedToProvideAnonymousData", true);
            } else if (dialogResult == JOptionPane.NO_OPTION) {
                config.put("userAcceptedToProvideAnonymousData", false);
            }
            try (PrintWriter writer = new PrintWriter(configFile)) {
                writer.write(config.toString());
            } catch (FileNotFoundException e) {
            }
        }
        doPost(POST_LAUNCHER, clazz + " " + String.join(" ", args));
    }

    public void deploy(String... args) {
        doPost(POST_DEPLOY, String.join(" ", args));
    }

    private void doPost(String url, String args) {
        if (config.optBoolean("userAcceptedToProvideAnonymousData", false)) {
            HttpStream.Options options = new HttpStream.Options();
            options.httpType = HttpStream.POST;
            options.socketFactory = new SSLSocketFactory();
            options.postHeaders.put("accept", "application/json");
            options.postHeaders.put("Content-Type", "application/json");

            JSONObject dataJson = new JSONObject(config, new String[] { "userUuid" });
            dataJson.put("os", System.getProperty("os.name"));
            dataJson.put("tc_version", Settings.versionStr);
            dataJson.put("date", sdf.format(new Date()));
            dataJson.put("args", args);
            options.data = dataJson.toString();

            try (HttpStream hs = new HttpStream(new URI(url), options)) {
            } catch (java.io.IOException e1) {
                e1.printStackTrace();
            }
        }
    }

}