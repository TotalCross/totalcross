// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package axtls;

import totalcross.io.File;
import totalcross.crypto.provider.PBKDF2WithHmacSHA1Factory;
import totalcross.net.Socket;
import totalcross.net.ssl.SSLContext;
import totalcross.net.ssl.SSLSocket;
import totalcross.net.ssl.SSLSocketFactory;
import totalcross.ui.MainWindow;
import javax.crypto.SecretKey;
import javax.crypto.spec.PBEKeySpec;

public class AxTLSProviderSmoke extends MainWindow {
  private static final int CONNECT_TIMEOUT_MILLIS = 10000;
  private static final String TLS13_UNSUPPORTED_ERROR = "Error Code: FFFF8880";
  private String results = "";

  @Override
  public void initUI() {
    report("[START] provider matrix");
    String[] arguments = getCommandLine().trim().split("\\s+");
    if (arguments.length != 3) {
      report("[FAIL] expected TLSv1.2 RSA, TLSv1.3, and TLSv1.2 ECDSA ports in the command line");
      saveResults();
      exit(2);
      return;
    }

    int tls12Port;
    int tls13Port;
    int tls12EcdsaPort;
    try {
      tls12Port = Integer.parseInt(arguments[0]);
      tls13Port = Integer.parseInt(arguments[1]);
      tls12EcdsaPort = Integer.parseInt(arguments[2]);
    } catch (NumberFormatException e) {
      report("[FAIL] invalid TLS test port: " + e.getMessage());
      saveResults();
      exit(2);
      return;
    }

    int failures = 0;
    failures += expectPbkdf2Native();
    report("[INFO] creating default provider factory for TLSv1.2");
    failures += expectSuccess("default TLSv1.2", defaultFactory(), tls12Port);
    report("[INFO] creating default provider factory for TLSv1.3");
    failures += expectExpectedFailure("default TLSv1.3 unsupported (FFFF8880)", defaultFactory(), tls13Port,
        TLS13_UNSUPPORTED_ERROR);
    report("[INFO] creating default provider factory for TLSv1.2 ECDSA");
    failures += expectSuccess("default TLSv1.2 ECDSA", defaultFactory(), tls12EcdsaPort);
    report("[INFO] creating base provider factory for TLSv1.2");
    failures += expectSuccess("base TLSv1.2", baseFactory(), tls12Port);
    report("[INFO] creating base provider factory for TLSv1.3");
    failures += expectFailure("base TLSv1.3 rejected", baseFactory(), tls13Port);
    report("[INFO] creating base provider factory for TLSv1.2 ECDSA");
    failures += expectFailure("base TLSv1.2 ECDSA rejected", baseFactory(), tls12EcdsaPort);

    if (failures == 0) {
      report("[PASS] provider matrix complete");
      saveResults();
      exit(0);
    } else {
      report("[FAIL] provider matrix failures=" + failures);
      saveResults();
      exit(1);
    }
  }

  private int expectPbkdf2Native() {
    report("[INFO] PBKDF2 native: deriving RFC 6070 vector");
    try {
      SecretKey key = PBKDF2WithHmacSHA1Factory.generateSecret(
          new PBEKeySpec("password".toCharArray(), "salt".getBytes(), 1, 160));
      if (!"0c60c80f961f0e71f3a9b524af6012062fe037a6".equals(toHex(key.getEncoded()))) {
        report("[FAIL] PBKDF2 native: unexpected derived key");
        return 1;
      }
      report("[PASS] PBKDF2 native");
      return 0;
    } catch (Exception e) {
      report("[FAIL] PBKDF2 native: " + message(e));
      return 1;
    }
  }

  private String toHex(byte[] bytes) {
    char[] value = new char[bytes.length * 2];
    final char[] digits = "0123456789abcdef".toCharArray();
    for (int i = 0; i < bytes.length; i++) {
      value[i * 2] = digits[(bytes[i] >>> 4) & 15];
      value[i * 2 + 1] = digits[bytes[i] & 15];
    }
    return new String(value);
  }

  private SSLSocketFactory baseFactory() {
    try {
      return SSLContext.getInstance("", "base").getSocketFactory();
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  private SSLSocketFactory defaultFactory() {
    try {
      return SSLContext.getDefault().getSocketFactory();
    } catch (Exception e) {
      throw new RuntimeException(e.getMessage());
    }
  }

  private int expectSuccess(String name, SSLSocketFactory factory, int port) {
    report("[INFO] " + name + ": starting request");
    try {
      request(name, factory, port);
      report("[PASS] " + name);
      return 0;
    } catch (Exception e) {
      report("[FAIL] " + name + ": " + message(e));
      return 1;
    }
  }

  private int expectFailure(String name, SSLSocketFactory factory, int port) {
    report("[INFO] " + name + ": starting request expected to fail");
    try {
      request(name, factory, port);
      report("[FAIL] " + name + ": connection unexpectedly succeeded");
      return 1;
    } catch (Exception e) {
      report("[PASS] " + name);
      return 0;
    }
  }

  private int expectExpectedFailure(String name, SSLSocketFactory factory, int port, String expectedMessage) {
    report("[INFO] " + name + ": starting request expected to fail with " + expectedMessage);
    try {
      request(name, factory, port);
      report("[FAIL] " + name + ": connection unexpectedly succeeded");
      return 1;
    } catch (Exception e) {
      String actualMessage = message(e);
      if (actualMessage.indexOf(expectedMessage) < 0) {
        report("[FAIL] " + name + ": expected " + expectedMessage + ", got " + actualMessage);
        return 1;
      }
      report("[PASS] " + name);
      return 0;
    }
  }

  private void request(String name, SSLSocketFactory factory, int port) throws totalcross.io.IOException {
    report("[INFO] " + name + ": opening TCP socket on port " + port);
    Socket socket = factory.createSocket("127.0.0.1", port, CONNECT_TIMEOUT_MILLIS);
    try {
      report("[INFO] " + name + ": TCP socket opened");
      SSLSocket sslSocket = (SSLSocket) socket;
      report("[INFO] " + name + ": starting TLS handshake");
      sslSocket.startHandshake();
      report("[INFO] " + name + ": TLS handshake returned");
      byte[] request = "TLS smoke payload\\r\\n".getBytes();
      sslSocket.writeBytes(request, 0, request.length);
      report("[INFO] " + name + ": TLS payload sent");
    } finally {
      try {
        socket.close();
        report("[INFO] " + name + ": socket closed");
      } catch (Exception ignored) {
        report("[INFO] " + name + ": socket close ignored");
      }
    }
  }

  private String message(Exception e) {
    String value = e.getMessage();
    return value == null ? e.getClass().getName() : value;
  }

  private void report(String value) {
    results += value + "\n";
    System.out.println(value);
    saveResults();
  }

  private void saveResults() {
    try {
      byte[] contents = results.getBytes();
      File file = new File("axtls-provider-smoke-result.txt", File.CREATE_EMPTY);
      file.writeBytes(contents, 0, contents.length);
      file.close();
    } catch (Exception e) {
      System.out.println("[FAIL] unable to save provider smoke results: " + message(e));
    }
  }
}
