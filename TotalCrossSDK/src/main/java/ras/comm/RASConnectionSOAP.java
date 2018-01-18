package ras.comm;

import ras.ActivationClient;
import ras.ui.ActivationHtml;
import totalcross.io.ByteArrayStream;
import totalcross.io.DataStream;
import totalcross.io.IOException;
import totalcross.net.Base64;
import totalcross.sys.Convert;
import totalcross.sys.Registry;
import totalcross.sys.Settings;
import totalcross.ui.dialog.InputBox;
import totalcross.util.Hashtable;
import totalcross.xml.soap.SOAP;
import totalcross.xml.soap.SOAPException;

public class RASConnectionSOAP extends RASConnection {
  private String uri = ActivationClient.defaultServerURI;
  private String namespace;
  private int openTimeout;
  private int readWriteTimeout;

  private int helloHashCode;
  private int version;
  private String encodedResponse;

  private Hashtable userDefinedParams;

  RASConnectionSOAP(int openTimeout, int readWriteTimeout) throws IOException {
    if (Settings.activationServerURI != null) {
      this.uri = Settings.activationServerURI;
      this.namespace = Settings.activationServerNamespace;
      this.userDefinedParams = ActivationHtml.getUserDefinedParams();

      //flsobral@tc125: send fields for the user defined webservice.
      userDefinedParams.put("Settings.deviceId", Settings.deviceId);
      userDefinedParams.put("Settings.platform", Settings.platform);
      userDefinedParams.put("Settings.romVersion", Convert.toString(Settings.romVersion));
      userDefinedParams.put("Settings.version", Convert.toString(Settings.version));
      userDefinedParams.put("Settings.versionStr", Settings.versionStr);
      if (Settings.applicationId != null) {
        userDefinedParams.put("Settings.applicationId", Settings.applicationId);
      }
      if (Settings.appVersion != null) {
        userDefinedParams.put("Settings.appVersion", Settings.appVersion);
      }
      userDefinedParams.put("Settings.activationId", Settings.activationId);
      userDefinedParams.put("Settings.imei", Settings.imei != null ? Settings.imei : "");
    }
    this.openTimeout = openTimeout;
    this.readWriteTimeout = readWriteTimeout;
  }

  @Override
  public void sayHello() throws CommException {
    Hello hello = new Hello(ActivationClient.version);
    helloHashCode = hello.getClass().getName().hashCode();
    version = hello.getVersion();
  }

  @Override
  public void send(Packet packet) throws CommException {
    SOAP soap = new SOAP(packet.webServiceMethod, uri);
    soap.openTimeout = openTimeout;
    soap.readTimeout = readWriteTimeout;
    soap.writeTimeout = readWriteTimeout;
    if (namespace != null) {
      soap.namespace = namespace;
    }

    try {
      ByteArrayStream bas = new ByteArrayStream(256);
      DataStream ds = new DataStream(bas, true);

      // hello
      ds.writeInt(helloHashCode);
      ds.writeInt(version);
      ds.writeInt(packet.getClass().getName().hashCode());
      packet.write(ds);
      String encodedPacket = Base64.encode(bas.toByteArray());
      soap.setParam(encodedPacket, "request");

      if (uri != ActivationClient.defaultServerURI && userDefinedParams != null) {
        soap.setParam((String[]) userDefinedParams.getKeys().toObjectArray(), "keys");
        soap.setParam((String[]) userDefinedParams.getValues().toObjectArray(), "values");
      }

      try {
        soap.execute();
      } catch (SOAPException firstException) // on wince, if the first execute fails, check for proxy configuration and try again.
      {
        if (Settings.isWindowsCE()) //flsobral@tc126: check for proxy availability
        {
          try {
            String workNetworkKey = "Comm\\ConnMgr\\Providers\\{EF097F4C-DC4B-4c98-8FF6-AEF805DC0E8E}\\HTTP-{18AD9FBD-F716-ACB6-FD8A-1965DB95B814}";
            int enabled = Registry.getInt(Registry.HKEY_LOCAL_MACHINE, workNetworkKey, "Enable");
            String destination = Registry.getString(Registry.HKEY_LOCAL_MACHINE, workNetworkKey, "DestId");
            String username = Registry.getString(Registry.HKEY_LOCAL_MACHINE, workNetworkKey, "Username");
            String proxy = Registry.getString(Registry.HKEY_LOCAL_MACHINE, workNetworkKey, "Proxy");
            if (enabled == 1 && destination.equals("{436EF144-B4FB-4863-A041-8F905A62C572}")) {
              int portIdx = proxy.indexOf(':');
              int port = portIdx == -1 ? 80 : Convert.toInt(proxy.substring(portIdx + 1));
              InputBox input = new InputBox("Proxy", "Proxy password for " + username, null);
              input.getEdit().setMode(totalcross.ui.Edit.PASSWORD);
              input.popup();
              String password = input.getValue();
              if (password != null) {
                soap.useProxy(proxy.substring(0, portIdx), port, username, password);
              }
            }
            soap.execute();
          } catch (Exception e2) {
            throw firstException;
          }
        } else {
          throw firstException;
        }
      }
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new CommException("Cannot send packet", ex);
    }

    Object answer = soap.getAnswer();

    if (uri != ActivationClient.defaultServerURI && userDefinedParams != null) // user defined activation
    {
      if (answer instanceof String) {
        throw new CommException((String) answer);
      }
      String[] result = (String[]) answer;
      if (result.length == 1) {
        throw new CommException(result[0]);
      } else {
        encodedResponse = result[1];
      }
    } else {
      if (answer instanceof String) {
        encodedResponse = (String) answer;
      }
    }
  }

  @Override
  public Packet receive() throws CommException {
    byte[] decodedResponse = Base64.decode(encodedResponse);
    ByteArrayStream bas = new ByteArrayStream(decodedResponse);

    int id = 0;
    Class<?> packetClass;
    Packet packet = null;

    // Receive packet
    DataStream ds = new DataStream(bas);
    try {
      id = ds.readInt();
      packetClass = (Class<?>) Packet.packetClasses.get(id);
      if (packetClass == null) {
        throw new CommException("Unsupported packet received: " + id);
      }
      packet = (Packet) packetClass.newInstance();
      packet.read(ds);
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new CommException("Cannot receive packet", ex);
    }

    return packet;
  }
}
