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

import javax.swing.ImageIcon;
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

    private static String BASE_URL = "https://statistics.totalcross.com/api/v1";
    private static final String GET_UUID = "/users/get-anonymous-uuid";
    private static final String POST_LAUNCHER = "/launch";
    private static final String POST_DEPLOY = "/deploy";

    private static final String POPUP_TEXT = "We'd like to collect anonymous telemetry data to help us prioritize \n"
            + "improvements. This includes how often you deploy and launches \n"
            + "(on simulator) your app, which OS you deploy for, your timezone and \n"
            + "which totalcross version you're using. We do not collect any personal \n"
            + "data or sensitive information. Do you allow TotalCross to send us \n" + "anonymous report?";

    private static AnonymousUserData instance;

    private static ResponseRequester responseRequester;

    private static File configFile;

    private static JSONObject config;

    private SimpleDateFormat sdf;

    private static String configDirPath = AppDirsFactory.getInstance().getUserConfigDir("TotalCross", null, null)
            .replace("Application Support", "Preferences");

    private AnonymousUserData() {
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        responseRequester = new DefaultResponseRequester();
        loadConfiguration();
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
    public static void loadConfiguration() {
        config = null;
        File configDir = new File(configDirPath);
        configDir.mkdirs();
        configFile = new File(configDir, "config.json");
        if(configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                config = readJsonObject(Stream.asStream(fis));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = config == null ? new JSONObject() : config;
    }

    private static JSONObject readJsonObject(Stream stream) throws IOException {
        try (ByteArrayStream bas = new ByteArrayStream(4096)) {
            bas.readFully(stream, 10, 4096);
            JSONObject jsonObject = new JSONObject(new String(bas.getBuffer(), 0, bas.available(), "UTF-8"));
            return jsonObject;
        }
    }

    public void launcher(String... args) {
        if (!GraphicsEnvironment.isHeadless() && config.isNull("userAcceptedToProvideAnonymousData")) {
            try {
                boolean userAcceptedToContribute = responseRequester.ask();
                config.put("userAcceptedToProvideAnonymousData", userAcceptedToContribute);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try (PrintWriter writer = new PrintWriter(configFile)) {
                writer.write(config.toString());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        doPost(BASE_URL + POST_LAUNCHER, args);
    }

    public void deploy(String... args) {
        doPost(BASE_URL + POST_DEPLOY, args);
    }

    private void doGetUUID() {
        HttpStream.Options options = new HttpStream.Options();
        options.socketFactory = new SSLSocketFactory();
        try (HttpStream hs = new HttpStream(new URI(BASE_URL + GET_UUID), options)) {
            JSONObject ret = readJsonObject(hs);
            config.put("userUuid", ret.get("uuid"));

            try (PrintWriter writer = new PrintWriter(configFile)) {
                writer.write(config.toString());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    private void doPost(String url, String... args) {
        if (config.optBoolean("userAcceptedToProvideAnonymousData", false)) {
            if (!config.has("userUuid")) {
                doGetUUID();
            }

            HttpStream.Options options = new HttpStream.Options();
            options.httpType = HttpStream.POST;
            options.socketFactory = new SSLSocketFactory();
            options.postHeaders.put("accept", "application/json");
            options.postHeaders.put("Content-Type", "application/json");

            JSONObject dataJson = new JSONObject();
            dataJson.put("os", System.getProperty("os.name"));
            dataJson.put("tc_version", Settings.versionStr);
            dataJson.put("date", sdf.format(new Date()));
            dataJson.put("args", String.join(" ", args));
            dataJson.put("userUuid", config.opt("userUuid"));
            options.data = dataJson.toString();

            try (HttpStream hs = new HttpStream(new URI(url), options)) {
                try {
                    if(hs.badResponseCode) {
                        throw new Exception("Bad Code Response from server: " + hs.getStatus());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    public interface ResponseRequester {
        boolean ask() throws Exception;
    }

    private class DefaultResponseRequester implements ResponseRequester {
        @Override
        public boolean ask() throws Exception {
            final String[] options = new String[] { "Yes, send anonymous reports", "Don't send" };
            final ImageIcon icon = new ImageIcon(getClass().getClassLoader().getResource("tc/crossy.png"));
            int dialogResult = JOptionPane.showOptionDialog(null, POPUP_TEXT, "", JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE, icon, options, options[0]);
            if (dialogResult == JOptionPane.YES_OPTION) {
                return true;
            } else if (dialogResult == JOptionPane.NO_OPTION) {
                return false;
            }
            throw new Exception("Unexpected Response: user should provide a response.");
        }
    }

    public String getBaseUrl() {
        return BASE_URL;
    }

    public void setBaseUrl(String url) {
        BASE_URL = url;
    }

    public String getConfigDirPath() {
        return configDirPath;
    }

    public void setConfigDirPath(String configDirPath) {
        AnonymousUserData.configDirPath = configDirPath;
    }

    public ResponseRequester getResponseRequester() {
        return responseRequester;
    }

    public void setResponseRequester(ResponseRequester responseRequester) {
        AnonymousUserData.responseRequester = responseRequester;
    }

    public JSONObject getConfig() {
        return config;
    }

}