// Copyright (C) 2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package tc.tools;

import java.awt.GraphicsEnvironment;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import net.harawata.appdirs.AppDirsFactory;
import totalcross.io.ByteArrayStream;
import totalcross.io.Stream;
import totalcross.json.JSONException;
import totalcross.json.JSONObject;
import totalcross.net.HttpStream;
import totalcross.net.ssl.SSLSocketFactory;
import totalcross.sys.Settings;

public class AnonymousUserData {

    private String BASE_URL = "https://statistics.totalcross.com/api/v1";
    private static final String GET_UUID = "/users/get-anonymous-uuid";
    private static final String POST_LAUNCHER = "/launch";
    private static final String POST_DEPLOY = "/deploy";
    private static final String CHECK_UUID = "/users/check-uuid";

    private static AnonymousUserData instance;

    private ResponseRequester responseRequester;

    private File configFile;

    private JSONObject config;

    private SimpleDateFormat sdf;

    private String configDirPath = AppDirsFactory.getInstance().getUserConfigDir("TotalCross", null, null)
            .replace("Application Support", "Preferences");

    private AnonymousUserData() throws IOException {
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        responseRequester = new DefaultResponseRequester();
        loadConfiguration();
    }

    public static AnonymousUserData instance() throws IOException {
        if (instance == null) {
            instance = new AnonymousUserData();
        }
        return instance;
    }

    /* package */ void setBaseUrl(String url) {
        this.BASE_URL = url;
    }

    /* package */ void setResponseRequester(ResponseRequester responseRequester) {
        this.responseRequester = responseRequester;
    }

    /* package */ void setConfigDirPath(String configDirPath) {
        this.configDirPath = configDirPath;
    }

    /* package */ JSONObject getConfig() {
        return this.config;
    }

    /**
     * Always returns a valid config object. An empty one is returned if anything
     * fails, not exception is thrown.
     * 
     * This must be transparent for the user, no exception or stack trace should
     * interrupt the usage of the SDK or pollute the standard output.
     * 
     * @throws IOException
     */
    public void loadConfiguration() throws IOException {
        config = null;
        File configDir = new File(configDirPath);
        configDir.mkdirs();
        configFile = new File(configDir, "config.json");
        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                config = readJsonObject(Stream.asStream(fis));
                if (config.has("uuid") && !checkUUID((String) config.get("uuid"))) {
                    config.remove("uuid");
                }
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

    public void launcher(String... args) throws JSONException, IOException {
        if (!GraphicsEnvironment.isHeadless() && config.isNull("userAcceptedToProvideAnonymousData")) {
            Boolean userAcceptedToContribute = responseRequester.ask();
            config.put("userAcceptedToProvideAnonymousData", userAcceptedToContribute);
            try (PrintWriter writer = new PrintWriter(configFile)) {
                writer.write(config.toString());
            }
        }
        doPost(BASE_URL + POST_LAUNCHER, args);
    }

    public void deploy(String... args) throws JSONException, IOException {
        doPost(BASE_URL + POST_DEPLOY, args);
    }

    private void doGetUUID() throws JSONException, IOException {
        JSONObject dataJson = new JSONObject();
        dataJson.put("os", System.getProperty("os.name"));
        dataJson.put("tc_version", Settings.versionStr);

        JSONObject ret = new HttpJsonConnection(BASE_URL + GET_UUID).doPost(dataJson).getResponse();
        config.put("uuid", ret.get("uuid"));

        try (PrintWriter writer = new PrintWriter(configFile)) {
            writer.write(config.toString());
        }
    }

    /**
     * Check for invalid uuid's which were generated on version 6.1.0. The uuid's
     * generated on such a version must be discarded from the user local
     * configuration.
     * 
     * @throws IOException
     * @throws JSONException
     */
    public boolean checkUUID(String uuid) throws JSONException, IOException {
        JSONObject ret = new HttpJsonConnection(BASE_URL + CHECK_UUID + "?uuid=" + uuid).doGet().getResponse();
        boolean isValid = (boolean) ret.get("isValid");
        if (!isValid) {
            config.remove("uuid");
        }
        return isValid;
    }

    private void doPost(String url, String... args) throws JSONException, IOException {
        if (config.optBoolean("userAcceptedToProvideAnonymousData", false)) {
            if (!config.has("uuid")) {
                doGetUUID();
            }

            JSONObject dataJson = new JSONObject();
            dataJson.put("tc_version", Settings.versionStr);
            dataJson.put("date", sdf.format(new Date()));
            dataJson.put("args", String.join(" ", args));
            dataJson.put("userUuid", config.opt("uuid"));

            final int status = new HttpJsonConnection(url).doPost(dataJson).responseCode();
            if (status >= 300) {
                throw new IOException("Bad Code Response from server: " + status);
            }
        }
    }

    public interface ResponseRequester {
        Boolean ask();
    }

    private class DefaultResponseRequester implements ResponseRequester {

        private static final String POPUP_TEXT = 
            "We'd like to collect anonymous telemetry data to help us prioritize \n"
                + "improvements. This includes how often you deploy and launches \n"
                + "(on simulator) your app, which OS you deploy for, your timezone and \n"
                + "which totalcross version you're using. We do not collect any personal \n"
            + "data or sensitive information. Do you allow TotalCross to send us \n" 
            + "anonymous report?";

        @Override
        public Boolean ask() {
            final String[] options = new String[] { "Yes, send anonymous reports", "Don't send" };
            final ImageIcon icon = new ImageIcon(getClass().getClassLoader().getResource("tc/crossy.png"));
            int dialogResult = JOptionPane.showOptionDialog(null, POPUP_TEXT, "", JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE, icon, options, options[0]);
            if (dialogResult == JOptionPane.YES_OPTION) {
                return true;
            } else if (dialogResult == JOptionPane.NO_OPTION) {
                return false;
            }
            return null;
        }
    }

    private static class HttpJsonConnection {

        private HttpURLConnection conn;

        class HttpJsonRequest {

            int responseCode() throws IOException {
                return conn.getResponseCode();
            }

            JSONObject getResponse() throws IOException {
                try (final InputStream inputStream = conn.getInputStream();
                        final ByteArrayOutputStream result = new ByteArrayOutputStream()) {
                    final byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) != -1) {
                        result.write(buffer, 0, length);
                    }
                    return new JSONObject(result.toString(StandardCharsets.UTF_8.name()));
                }
            }
        }

        HttpJsonConnection(String url) throws MalformedURLException, IOException {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setConnectTimeout(60_000);
            conn.setReadTimeout(60_000);
        }

        public HttpJsonRequest doGet() throws IOException {
            conn.setRequestMethod("GET");
            return new HttpJsonRequest();
        }

        public HttpJsonRequest doPost(JSONObject data) throws IOException {
            conn.setRequestMethod("POST");

            final byte[] bytes = data.toString().getBytes(StandardCharsets.UTF_8);
            conn.setFixedLengthStreamingMode(bytes.length);
            try (final OutputStream os = conn.getOutputStream()) {
                os.write(bytes);
            }

            return new HttpJsonRequest();
        }
    }
}