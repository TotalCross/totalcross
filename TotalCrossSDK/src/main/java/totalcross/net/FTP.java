// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.net;

import totalcross.io.IOException;
import totalcross.io.LineReader;
import totalcross.io.Stream;
import totalcross.sys.Convert;
import totalcross.sys.InvalidNumberException;
import totalcross.sys.Settings;
import totalcross.sys.Vm;
import totalcross.ui.ComboBox;
import totalcross.ui.Control;
import totalcross.ui.ListBox;
import totalcross.util.Vector;

/**
 * This class implements the File Transfer Protocol.
 * <p>
 * Note: if you're experiencing slowness in the server, try to disable the reverse-ip resolution in your FTP server:
 * this will speedup everything.
 * </p>
 * Check the sample RemoteExplorer. Here is an example code:
 * 
 * <pre>
 &nbsp   ftp = new FTP(url, user, pass, cbLog);
 &nbsp   ftp.makeDir(&quot;tempdir&quot;);
 &nbsp   ftp.changeDir(&quot;tempdir&quot;);
 &nbsp   String textfile = &quot;Viva Verinha!&quot;;
 &nbsp   ByteArrayStream bas = new ByteArrayStream(textfile.getBytes());
 &nbsp   ftp.sendFile(bas, &quot;verinha.txt&quot;, false);
 &nbsp   String[] files = ftp.list(&quot;*.txt&quot;);
 &nbsp   if (files == null || files.length != 1)
 &nbsp      cbLog.add(&quot;Something went wrong in sending files...&quot;);
 &nbsp   ftp.rename(&quot;verinha.txt&quot;, &quot;vivaverinha.txt&quot;);
 &nbsp   bas = new ByteArrayStream(50);
 &nbsp   ftp.receiveFile(&quot;vivaverinha.txt&quot;, bas);
 &nbsp   cbLog.add(new String(bas.getCopy()));
 &nbsp   ftp.delete(&quot;vivaverinha.txt&quot;);
 &nbsp   ftp.changeDir(&quot;..&quot;);
 &nbsp   ftp.removeDir(&quot;tempdir&quot;);
 * </pre>
 * 
 * You can use the CompressedByteArrayStream to transfer and receive big files to/from the server (check the class for more
 * information and samples).
 * <p>
 * To transfer a File to the server, all you have to do is:
 * 
 * <pre>
 &nbsp   File f = new File(&quot;michelle.txt&quot;, File.READ_WRITE);
 &nbsp   ftp.sendFile(f, &quot;michelle.txt&quot;, false); // last parameter depends on what you want to do
 * </pre>
 * 
 * It is currently impossible to transfer a whole PDBFile to/from the server. The solution for this would be to send
 * each record in pieces, and store it in separate files in the server. Then a routine written in TotalCross in the server could
 * reassemble the records into a new PDBFile. Here is an idea:
 * 
 * <pre>
 &nbsp   // for the Client:
 &nbsp   // i assume that a ftp class is prepared to be used
 &nbsp   String name = &quot;myPDBFile&quot;;
 &nbsp   PDBFile cat = new PDBFile(name + &quot;.crtr.type&quot;, READ_WRITE);
 &nbsp   int n = cat.getRecordCount();
 &nbsp   for (int i = 0; i &lt; n; i++)
 &nbsp   {
 &nbsp      if (!cat.setRecordPos(i))
 &nbsp         throw new RuntimeException(&quot;PDBFile is in use elsewhere!&quot;);
 &nbsp      else
 &nbsp         ftp.sendFile(cat, name + &quot;#&quot; + i, false);
 &nbsp   }

 &nbsp   // for the server
 &nbsp   String name = &quot;myPDBFile&quot;;
 &nbsp   // for simplicity, I'll assume that the PDBFile does not exists
 &nbsp   byte[] buf = new byte[65536]; // in desktop this is possible
 &nbsp   PDBFile cat = new PDBFile(name + &quot;.crtr.type&quot;, PDBFile.CREATE);
 &nbsp   for (int i = 0;; i++)
 &nbsp   {
 &nbsp      File f = new File(name + &quot;#&quot; + i, File.READ_WRITE);
 &nbsp      if (!f.exists())
 &nbsp         break; // no more records
 &nbsp      int size = f.getSize();
 &nbsp      f.readBytes(buf, 0, size);
 &nbsp      f.delete(); // could be also: f.close();

 &nbsp      cat.addRecord(size);
 &nbsp      cat.writeBytes(buf, 0, size);
 &nbsp   }
 &nbsp   cat.close();
 * </pre>
 * 
 * The example above can be easily changed to add support for compression.
 * <p>
 * Here is a list of error codes that can thrown if an Exception occurs: <p>
      <TABLE width="100%" border=0 cellpadding="2" cellspacing="1" bgcolor="#FFFFFF">
        <TR bgcolor="#CCCCCC" align="CENTER">
          <TD><B>Code</B></TD>
          <TD><B>Description</B></TD>
        </TR>
        <TR bgcolor="#FFFFCC">
          <TD align=CENTER NOWRAP><B>100 Codes</B></TD>
          <TD align=left><B>The requested action is being taken. Expect a reply
              before proceeding with a new command.</B></TD>
        </TR>
        <TR VALIGN="TOP">
          <TD align="CENTER" class="text1"><B>110</B></TD>
          <TD>Restart marker reply.</TD>
        </TR>
        <TR VALIGN="TOP">
          <TD align="CENTER" class="text1"><B>120</B></TD>
          <TD>Service ready in (n) minutes.</TD>
        </TR>
        <TR VALIGN="TOP">
          <TD align="CENTER" class="text1"><B>125</B></TD>
          <TD>Data connection already open, transfer starting.</TD>
        </TR>
        <TR VALIGN="TOP">
          <TD align="CENTER" class="text1"><B>150</B></TD>
          <TD>File status okay, about to open data connection.</TD>
        </TR>
        <TR VALIGN="TOP" bgcolor="#FFFFCC">
          <TD align=CENTER><B>200 Codes</B></TD>
          <TD align=left><B>The requested action has been successfully completed.</B></TD>
        </TR>
        <TR VALIGN="TOP">
          <TD align="CENTER" class="text1"><B>200</B></TD>
          <TD>Command okay.</TD>
        </TR>
        <TR VALIGN="TOP">
          <TD align="CENTER" class="text1"><B>202</B></TD>
          <TD>Command not implemented</TD>
        </TR>
        <TR VALIGN="TOP">
          <TD align="CENTER" class="text1"><B>211</B></TD>
          <TD>System status, or system help reply.</TD>
        </TR>
        <TR VALIGN="TOP">
          <TD align="CENTER" class="text1"><B>212</B></TD>
          <TD>Directory status.</TD>
        </TR>
        <TR VALIGN="TOP">
          <TD align="CENTER" class="text1"><B>213</B></TD>
          <TD>File status.</TD>
        </TR>
        <TR VALIGN="TOP">
          <TD align="CENTER" class="text1"><B>214</B></TD>
          <TD>Help message.</TD>
        </TR>
        <TR VALIGN="TOP">
          <TD align="CENTER" class="text1"><B>215</B></TD>
          <TD>NAME system type. (NAME is an official system name from the list
            in the Assigned Numbers document.)</TD>
        </TR>
        <TR VALIGN="TOP">
          <TD align="CENTER" class="text1"><B>220</B></TD>
          <TD>Service ready for new user.</TD>
        </TR>
        <TR VALIGN="TOP">
          <TD align="CENTER" class="text1"><B>221</B></TD>
          <TD>Service closing control connection. (Logged out if appropriate.)</TD>
        </TR>
        <TR VALIGN="TOP">
          <TD align="CENTER" class="text1"><B>225</B></TD>
          <TD>Data connection open, no transfer in progress.</TD>
        </TR>
        <TR VALIGN="TOP">
          <TD align="CENTER" class="text1"><B>226</B></TD>
          <TD>Closing data connection. Requested file action successful (file
            transfer, abort, etc.).</TD>
        </TR>
        <TR VALIGN="TOP">
          <TD align="CENTER" class="text1"><B>227</B></TD>
          <TD>Entering Passive Mode</TD>
        </TR>
        <TR VALIGN="TOP">
          <TD align="CENTER" class="text1"><B>230</B></TD>
          <TD>User logged in, proceed.</TD>
        </TR>
        <TR VALIGN="TOP">
          <TD align="CENTER" class="text1"><B>250</B></TD>
          <TD>Requested file action okay, completed.</TD>
        </TR>
        <TR VALIGN="TOP">
          <TD align="CENTER" class="text1"><B>257</B></TD>
          <TD>"PATHNAME" created.</TD>
        </TR>
        <TR VALIGN="TOP" bgcolor="#FFFFCC">
          <TD align=CENTER><B>300 Codes</B></TD>
          <TD align=left><B>The command has been accepted, but the requested
              action is being held pending receipt of further information.</B></TD>
        </TR>
        <TR VALIGN="TOP">
          <TD align="CENTER" class="text1"><B>331</B></TD>
          <TD>User name okay, need password.</TD>
        </TR>
        <TR VALIGN="TOP">
          <TD align="CENTER" class="text1"><B>332</B></TD>
          <TD>Need account for login.</TD>
        </TR>
        <TR VALIGN="TOP">
          <TD align="CENTER" class="text1"><B>350</B></TD>
          <TD>Requested file action pending further information. <br>If you're using "list *.txt", try "list *.*" and filter localy.</TD>
        </TR>
        <TR VALIGN="TOP" bgcolor="#FFFFCC">
          <TD align=CENTER><B>400 Codes</B></TD>
          <TD align=left><B>The command was not accepted and the requested action
              did not take place. <BR>
            Tthe error condition is temporary, however, and the action may be
          requested again.</B></TD>
        </TR>
        <TR VALIGN="TOP">
          <TD align="CENTER" class="text1"><B>421</B></TD>
          <TD>Service not available, closing control connection. (May be a reply
            to any command if the service knows it must shut down.)'</TD>
        </TR>
        <TR VALIGN="TOP">
          <TD align="CENTER" class="text1"><B>425</B></TD>
          <TD>Can't open data connection.</TD>
        </TR>
        <TR VALIGN="TOP">
          <TD align="CENTER" class="text1"><B>426</B></TD>
          <TD>Connection closed, transfer aborted.</TD>
        </TR>
        <TR VALIGN="TOP">
          <TD align="CENTER" class="text1"><B>450</B></TD>
          <TD>Requested file action not taken. File unavailable (e.g., file busy).</TD>
        </TR>
        <TR VALIGN="TOP">
          <TD align="CENTER" class="text1"><B>451</B></TD>
          <TD>Requested action aborted, local error in processing.</TD>
        </TR>
        <TR VALIGN="TOP">
          <TD align="CENTER" class="text1"><B>452</B></TD>
          <TD>Requested action not taken. Insufficient storage space in system.</TD>
        </TR>
        <TR VALIGN="TOP" bgcolor="#FFFFCC">
          <TD align=CENTER><B>500 Codes</B></TD>
          <TD align=left><B>The command was not accepted and the requested action
              did not take place.</B></TD>
        </TR>
        <TR VALIGN="TOP">
          <TD align="CENTER" class="text1"><B>500</B></TD>
          <TD>Syntax error, command unrecognized. This may include errors such
            as command line too long.</TD>
        </TR>
        <TR VALIGN="TOP">
          <TD align="CENTER" class="text1"><B>501</B></TD>
          <TD>Syntax error in parameters or arguments.</TD>
        </TR>
        <TR VALIGN="TOP">
          <TD align="CENTER" class="text1"><B>502</B></TD>
          <TD>Command not implemented.</TD>
        </TR>
        <TR VALIGN="TOP">
          <TD align="CENTER" class="text1"><B>503</B></TD>
          <TD>Bad sequence of commands.</TD>
        </TR>
        <TR VALIGN="TOP">
          <TD align="CENTER" class="text1"><B>504</B></TD>
          <TD>Command not implemented for that parameter.</TD>
        </TR>
        <TR VALIGN="TOP">
          <TD align="CENTER" class="text1"><B>530</B></TD>
          <TD>User not logged in.</TD>
        </TR>
        <TR VALIGN="TOP">
          <TD align="CENTER" class="text1"><B>532</B></TD>
          <TD>Need account for storing files.</TD>
        </TR>
        <TR VALIGN="TOP">
          <TD align="CENTER" class="text1"><B>550</B></TD>
          <TD>Requested action not taken. File unavailable (e.g., file not found,
            no access).</TD>
        </TR>
        <TR VALIGN="TOP">
          <TD align="CENTER" class="text1"><B>552</B></TD>
          <TD>Requested file action aborted, storage allocation exceeded</TD>
        </TR>
        <TR VALIGN="TOP">
          <TD align="CENTER" class="text1"><B>553</B></TD>
          <TD>Requested action not taken. Illegal file name.</TD>
        </TR>
      </TABLE>
 * 
 * @since SuperWaba 5.6
 * @author Guilherme C. Hazan
 */
public class FTP {
  /** Assign the progressInfo member to an instance of this interface to receive information of how many bytes were transfered.
   * Note that there's no way to get the total size of the file, but only what has been transfered so far.
   * @see #progressInfo 
   */
  public static interface ProgressInformation {
    /** Called to report the size of the file that will be transferred.
     * You can use it to compute the transfer's percentage.
     * @since TotalCross 1.2
     */
    public void reportSize(int size); // guich@tc120_40

    /** Called to inform how many bytes were transfered. */
    public void transfering(int current);
  }

  private static final byte WAITING_FOR_REPLY = 1;
  private static final byte COMPLETED = 2;
  private static final byte WAITING_FOR_MORE_INFO = 3;
  //   private static final byte NOT_PROCESSED = 4; 
  //   private static final byte NOT_ACCEPTED = 5;

  /** Default port: 21. */
  public static final int defaultPort = 21;
  /** Default open timeout: 15 seconds. */
  public static final int defaultOpenTimeout = 15000;
  /** Default read timeout: 5 seconds. */
  public static final int defaultReadTimeout = 5000;
  /** Default write timeout: 5 seconds. */
  public static final int defaultWriteTimeout = 5000;

  /** Defines an ASCII transfer type.
   * @see #setType(String) 
   */
  public static final String ASCII = "A";
  /** Defines a BINARY transfer type.
   * @see #setType(String) 
   */
  public static final String BINARY = "I";
  /** Defines an EBCDIC transfer type.
   * @see #setType(String) 
   */
  public static final String EBCDIC = "E";

  private StringBuffer sbsc = new StringBuffer(100); // guich@562_4: now we use a string buffer to avoid gc
  private byte buffer[];
  private int openTimeout = defaultOpenTimeout;
  private int readTimeout = defaultReadTimeout;
  private int writeTimeout = defaultWriteTimeout;
  private ComboBox cbLog;
  private ListBox lbLog;

  /** Assign the ProgressInformation member to receive information about the file transfer. 
   * This way, you can show it in a ProgressBar.
   * @since TotalCross 1.14
   */
  public ProgressInformation progressInfo;

  /**
   * This makes a sleep during the send of a file. Important: when using softick, you must set this to 500(ms) or more,
   * or softick will starve to death.
   */
  public int sendSleep;

  /**
   * Set this to true to send the log to both the console and the combo.
   * 
   * @since SuperWaba 5.66
   */
  public static boolean log2console = Settings.onJavaSE; // guich@566_26

  private Socket myTCPConnection;

  private LineReader myTCPConnectionReader; //flsobral@tc123_54: FTP now uses LineReader to process the server responses - greatly improving its performance.

  private LineReader listReader; // used only for list and listNames.

  /**
   * Opens a socket to the given URL, user, password and the default timeouts.
   * @param url The url as an ip or a full address to the server. The connection port is always 21
   * @param user The user name for login
   * @param pass The password for login
   * 
   * @see #FTP(String, String, String, int, int, int, int, Control)
   */
  public FTP(String url, String user, String pass)
      throws UnknownHostException, IOException, FTPConnectionClosedException {
    this(url, user, pass, defaultPort, defaultOpenTimeout, defaultReadTimeout, defaultWriteTimeout, null);
  }

  /**
   * Opens a socket to the given URL, user, password, port and the default timeouts.
   * 
   * @param url  The url as an ip or a full address to the server.
   * @param user The user name for login
   * @param pass The password for login
   * @param port The port used to open the connection.
   * 
   * @see #FTP(String, String, String, int, int, int, int, Control)
   */
  public FTP(String url, String user, String pass, int port)
      throws UnknownHostException, IOException, FTPConnectionClosedException {
    this(url, user, pass, port, defaultOpenTimeout, defaultReadTimeout, defaultWriteTimeout, null);
  }

  /**
   * Opens a socket to the given URL, user, password, logging control and the default timeouts.
   * 
   * @param url  The url as an ip or a full address to the server. The connection port is always 21
   * @param user The user name for login
   * @param pass The password for login
   * @param loggingControl The ListBox or ComboBox where the logging will be sent to.
   * 
   * @see #FTP(String, String, String, int, int, int, int, Control)
   */
  public FTP(String url, String user, String pass, Control loggingControl)
      throws UnknownHostException, IOException, FTPConnectionClosedException {
    this(url, user, pass, defaultPort, defaultOpenTimeout, defaultReadTimeout, defaultWriteTimeout, loggingControl);
  }

  /**
   * Opens a socket to the given URL, user, password, and timeouts.
   * 
   * @param url  The url as an ip or a full address to the server. The connection port is always 21
   * @param user The user name for login
   * @param pass The password for login
   * @param openTimeout  The timeout used for the socket open
   * @param readTimeout  The timeout used for read operations
   * @param writeTimeout The timeout used for write operations
   * 
   * @see #FTP(String, String, String, int, int, int, int, Control)
   */
  public FTP(String url, String user, String pass, int openTimeout, int readTimeout, int writeTimeout)
      throws UnknownHostException, IOException, FTPConnectionClosedException {
    this(url, user, pass, defaultPort, openTimeout, readTimeout, writeTimeout, null);
  }

  /**
   * Opens a socket to the given URL, user, password, timeouts and logging control
   * 
   * @param url  The url as an ip or a full address to the server. The connection port is always 21
   * @param user The user name for login
   * @param pass The password for login
   * @param openTimeout  The timeout used for the socket open
   * @param readTimeout  The timeout used for read operations
   * @param writeTimeout The timeout used for write operations           
   * @param loggingControl The ListBox or ComboBox where the logging will be sent to.
   * 
   * @see #FTP(String, String, String, int, int, int, int, Control)
   */
  public FTP(String url, String user, String pass, int openTimeout, int readTimeout, int writeTimeout,
      Control loggingControl) throws UnknownHostException, IOException, FTPConnectionClosedException {
    this(url, user, pass, defaultPort, openTimeout, readTimeout, writeTimeout, loggingControl);
  }

  /**
   * Opens a socket to the given URL, user, password, port, timeouts and logging control.
   * 
   * @param url  The url as an ip or a full address to the server.
   * @param user The user name for login
   * @param pass The password for login
   * @param port The port used to open the connection.
   * @param openTimeout  The timeout used for the socket open
   * @param readTimeout  The timeout used for read operations
   * @param writeTimeout The timeout used for write operations           
   * @param loggingControl The ListBox or ComboBox where the logging will be sent to.
   */
  public FTP(String url, String user, String pass, int port, int openTimeout, int readTimeout, int writeTimeout,
      Control loggingControl) throws UnknownHostException, IOException, FTPConnectionClosedException {
    setLoggingControl(loggingControl);
    try {
      myTCPConnection = new Socket(url, port, openTimeout, true); // guich@561_14: use  for Palm OS
    } catch (totalcross.net.UnknownHostException e) {
      log("Not connected. Error: " + e.getMessage());
      throw e;
    } catch (totalcross.io.IOException e) {
      log("Not connected. Error: " + e.getMessage());
      throw e;
    }

    log("Connected.");
    myTCPConnectionReader = new LineReader(myTCPConnection);

    myTCPConnection.readTimeout = readTimeout; // guich@570_16
    myTCPConnection.writeTimeout = writeTimeout;
    this.openTimeout = openTimeout;
    this.readTimeout = readTimeout;
    checkResponse(COMPLETED);
    sendCommand("USER ", user, WAITING_FOR_MORE_INFO);
    sendCommand("PASS ", pass, COMPLETED);
  }

  /**
   * Sends a quit command to the server and closes the socket.
   * 
   * @throws FTPConnectionClosedException
   * @throws totalcross.io.IOException
   */
  public void quit() throws FTPConnectionClosedException, totalcross.io.IOException {
    if (myTCPConnection != null) {
      try {
        sendCommand("QUIT", null, COMPLETED);
      } catch (totalcross.io.IOException e) {
        // The server might close the connection without sending an acknowledgement,
        // so we catch and ignore IOExceptions at this point.
      } finally {
        myTCPConnection.close();
        myTCPConnection = null;
        myTCPConnectionReader = null;
      }
    }
  }

  /**
   * Returns the name of the current working directory.
   * 
   * @return The name of the current working directory.
   * @throws FTPConnectionClosedException
   * @throws totalcross.io.IOException
   */
  public String getCurrentDir() throws FTPConnectionClosedException, totalcross.io.IOException {
    String s = sendCommand("PWD", null, COMPLETED);
    return s.substring(s.indexOf('"') + 1, s.lastIndexOf('"'));
  }

  /**
   * Changes the user's current working directory to the given one.
   * 
   * This command allows the user to work with a different directory or dataset for file storage or retrieval without
   * altering his login or accounting information. Transfer parameters are similarly unchanged. The argument is a
   * pathname specifying a directory or other system dependent file group designator.
   * 
   * @param pathName Specifies a directory or other system dependent file group designator.
   */
  public void setCurrentDir(String pathName) throws FTPConnectionClosedException, totalcross.io.IOException {
    sendCommand("CWD ", pathName, COMPLETED);
  }

  /**
   * Sets the data representation type.
   * 
   * @param type specifies the data representation type.
   * @throws NullPointerException if type is null.
   * @see #ASCII
   * @see #BINARY
   * @see #EBCDIC
   */
  public void setType(String type) throws FTPConnectionClosedException, totalcross.io.IOException {
    if (type == null) {
      throw new NullPointerException("Argument 'type' cannot have a null value.");
    }
    sendCommand("TYPE ", type, COMPLETED);
  }

  /**
   * The argument is a HOST-PORT specification for the data port to be used in data connection. There are defaults for
   * both the user and server data ports, and under normal circumstances this command and its reply are not needed. If
   * this command is used, the argument is the concatenation of a 32-bit internet host address and a 16-bit TCP port
   * address. This address information is broken into 8-bit fields and the value of each field is transmitted as a
   * decimal number (in character string representation). The fields are separated by commas. A port command would be:
   * <pre>
   * PORT h1,h2,h3,h4,p1,p2
   * </pre>
   * where h1 is the high order 8 bits of the internet host address.
   * 
   * @param port port to be used in data connection
   * @throws NullPointerException if port is null.
   */
  public void setPort(String port) throws FTPConnectionClosedException, totalcross.io.IOException {
    if (port == null) {
      throw new NullPointerException("Argument 'port' cannot have a null value.");
    }
    sendCommand("PORT ", port, COMPLETED);
  }

  /**
   * Deletes the file specified at the server site.
   * 
   * @param pathName the file to be deleted.
   * @throws FTPConnectionClosedException
   * @throws totalcross.io.IOException
   * @throws NullPointerException
   *            if pathName is null.
   */
  public void delete(String pathName) throws FTPConnectionClosedException, totalcross.io.IOException {
    if (pathName == null) {
      throw new NullPointerException("Argument 'pathName' cannot have a null value.");
    }
    sendCommand("DELE ", pathName, COMPLETED);
  }

  /**
   * Creates a directory at the server site.
   * 
   * This command causes the directory specified in the pathname to be created as a directory (if the pathname is
   * absolute) or as a subdirectory of the current working directory (if the pathname is relative).
   * 
   * @param pathName Specifies the directory to be created.
   * @throws NullPointerException if pathName is null.
   */
  public void createDir(String pathName) throws FTPConnectionClosedException, totalcross.io.IOException {
    if (pathName == null) {
      throw new NullPointerException("Argument 'pathName' cannot have a null value");
    }
    sendCommand("MKD ", pathName, COMPLETED);
  }

  /**
   * Causes the directory specified in the pathname to be removed as a directory (if the pathname is absolute) or as a
   * subdirectory of the current working directory (if the pathname is relative).
   * 
   * @param pathName Specifies the directory to be removed.
   * @throws NullPointerException if pathName is null.
   */
  public void removeDir(String pathName) throws FTPConnectionClosedException, totalcross.io.IOException {
    if (pathName == null) {
      throw new NullPointerException("Argument 'pathName' cannot have a null value");
    }
    sendCommand("RMD ", pathName, COMPLETED);
  }

  /**
   * This command may help to keep the connection open.
   * 
   * This command does not affect any parameters or previously entered commands. It specifies no action other than that
   * the server send an OK reply.
   */
  public void noop() throws FTPConnectionClosedException, totalcross.io.IOException {
    sendCommand("NOOP", null, COMPLETED);
  }

  /**
   * Renames a file at the server site.
   * 
   * @param oldPathName Old pathname of the file which is to be renamed.
   * @param newPathName New pathname of the file specified to be renamed.
   * @throws NullPointerException if any of the arguments, oldPathName and newPathName, is null.
   */
  public void rename(String oldPathName, String newPathName)
      throws FTPConnectionClosedException, totalcross.io.IOException {
    if (oldPathName == null) {
      throw new NullPointerException("Argument 'oldPathName' cannot have a null value.");
    }
    if (newPathName == null) {
      throw new NullPointerException("Argument 'newPathName' cannot have a null value.");
    }

    sendCommand("RNFR ", oldPathName, WAITING_FOR_MORE_INFO);
    sendCommand("RNTO ", newPathName, COMPLETED);
  }

  /**
   * Sends a file to be stored at the server site. If the file specified in the pathname exists at the server site,
   * then its contents shall be replaced by the data being transferred. A new file is created at the server site if the
   * file specified in the pathname does not already exist.
   * 
   * @param inputStream The Stream from where the data will be read (using readBytes method)
   * @param pathName The name of the destination file in the server
   * @return Returns the total bytes sent. Its up to the user to check if it was the desired amount.
   * @throws NullPointerException if any of the arguments, inputStream or pathName, is null.
   */
  public int sendFile(Stream inputStream, String pathName)
      throws FTPConnectionClosedException, totalcross.io.IOException {
    if (inputStream == null) {
      throw new NullPointerException("Argument 'inputStream' cannot have a null value.");
    }
    if (pathName == null) {
      throw new NullPointerException("Argument 'pathName' cannot have a null value.");
    }

    int count, total = 0;
    setType(BINARY);
    /*
     * Important: linger must be set to true. in a gprs connection, the data is not completely sent until the
     * socket.close is sent. In this case, we MUST wait for the acknowledge from the server, otherwise the file will
     * not be flushed.
     */
    Socket socket = openReturningPath(false);
    sendCommand("STOR ", pathName, WAITING_FOR_REPLY);
    log("0 bytes sent");
    if (progressInfo != null) {
      progressInfo.transfering(0);
    }
    int tries = 3;
    if (buffer == null) {
      buffer = new byte[2048];
    }
    while ((count = inputStream.readBytes(buffer, 0, buffer.length)) > 0) {
      int totalSent = 0;
      while (totalSent < count) // this may happen when using softick
      {
        int sent = socket.writeBytes(buffer, totalSent, count - totalSent);
        if (sendSleep > 0) {
          Vm.sleep(sendSleep); // this is needed for softick
        }
        if (sent == 0 && tries-- == 0) {
          throw new totalcross.io.IOException("Cannot write to server.|Transmitted " + total + " bytes");
        }
        if (sent > 0) {
          totalSent += sent;
        }
      }
      tries = 3;
      total += totalSent;
      logReplace(total + " bytes sent");
      if (progressInfo != null) {
        progressInfo.transfering(total);
      }
    }
    log("Flushing file...");
    try {
      socket.close();

      // if the socket was correctly closed, we can safely ignore any timeout exceptions
      log("Flushed. Receiving ack...");
      try {
        checkResponse(COMPLETED);
      } catch (totalcross.io.IOException e) {
        String msg = e.getMessage();
        if (msg != null && (msg.startsWith("No response") || msg.indexOf("450") >= 0)) {
          /*
           * This is a case where, given to the long wait for file flushing, the ack from the server was lost.
           * Since the socket had closed fine, we can ensure that the file was transfered and just ignore the ack.
           */
          log("Ack was lost, but file transfer succeed.");
        } else {
          throw e; // something else happened, dispatch the exception
        }
      }

    } catch (totalcross.io.IOException e) {
      // if the socket could not be closed, let the user handle
      // the exception because the file was not transfered
      checkResponse(COMPLETED);
    }

    return total;
  }

  /**
   * Transfers a copy of the file specified in the pathname from the server. The status and contents of the file at the
   * server site shall be unaffected.
   * 
   * @param pathName Specifies the file to be retrieved.
   * @param outputStream The stream to where the file will be written.
   * @return number of bytes read.
   * @throws NullPointerException if any of the arguments, pathName or outputStream, is null.
   */
  public int receiveFile(String pathName, Stream outputStream)
      throws FTPConnectionClosedException, totalcross.io.IOException {
    if (pathName == null) {
      throw new NullPointerException("Argument 'pathName' cannot have a null value.");
    }
    if (outputStream == null) {
      throw new NullPointerException("Argument 'outputStream' cannot have a null value.");
    }

    setType(BINARY);
    Socket socket = openReturningPath(true);

    if (progressInfo != null) {
      try {
        String fsize = sendCommand("SIZE ", pathName, COMPLETED);
        progressInfo.reportSize(Convert.toInt(fsize.substring(fsize.lastIndexOf(' ') + 1)));
      } catch (Exception e) {
      }
    }

    sendCommand("RETR ", pathName, WAITING_FOR_REPLY);
    int count;
    int total = 0;
    log("0 bytes received");
    if (progressInfo != null) {
      progressInfo.transfering(0);
    }
    int tries = 3;
    if (buffer == null) {
      buffer = new byte[2048];
    }
    while ((count = socket.readBytes(buffer)) >= 0) // guich@561_8: != -1 -> > 0
    {
      if (count > 0) {
        tries = 3;
        total += count;
        logReplace(total + " bytes received");
        if (progressInfo != null) {
          progressInfo.transfering(total);
        }
        outputStream.writeBytes(buffer, 0, count);
      } else if (tries-- == 0) {
        break;
      }
    }
    try {
      checkResponse(COMPLETED); //flsobral@tc123_50: always check for the server response, even when count is -1 (EOF).
    } catch (totalcross.io.IOException e) {
      log("Cannot read from server.|Received " + total + " bytes. Error: " + e.getMessage());
      throw e;
    }
    socket.close();
    return total;
  }

  /**
   * List the contents on the specified pathname.
   * 
   * This command causes a list to be sent from the server passive DTP. If the pathname specifies a directory or other
   * group of files, the server should transfer a list of files in the specified directory. If the pathname specifies a
   * file then the server should send current information on the file. A null argument implies the user's current
   * working or default directory. The data transfer is over the data connection in type ASCII or type EBCDIC. (The
   * user must ensure that the TYPE is appropriately ASCII or EBCDIC). Since the information on a file may vary widely
   * from system to system, this information may be hard to use automatically in a program, but may be quite useful to
   * a human user.
   * 
   * @param pathName Specifies a directory, a group of files or a single file. A null value lists the user's current
   *           directory or the default directory.
   * @return The contents of the given pathname in the server
   * @throws FTPConnectionClosedException
   * @throws IOException
   */
  public String[] list(String pathName) throws FTPConnectionClosedException, totalcross.io.IOException {
    Socket socket = openReturningPath(true);
    sendCommand("LIST ", pathName, WAITING_FOR_REPLY);
    Vector v = new Vector(50);
    String s;

    if (listReader == null) {
      listReader = new LineReader(socket);
    } else {
      listReader.setStream(socket);
    }
    while (true) {
      if ((s = listReader.readLine()) != null) {
        v.addElement(s);
      } else {
        break;
      }
    }
    if (v.size() > 0 && v.items[v.size() - 1].toString().startsWith("total")) {
      v.removeElementAt(v.size() - 1); // remove the "total" information
    }

    socket.close();
    checkResponse(COMPLETED);
    return (String[]) v.toObjectArray();
  }

  /**
   * List the names of the files on the specified pathname.
   * 
   * This command causes a directory listing to be sent from server to user site. The pathname should specify a
   * directory or other system-specific file group descriptor; a null argument implies the current directory. The
   * server will return a stream of names of files and no other information. The data will be transferred in ASCII or
   * EBCDIC type over the data connection as valid pathname strings separated by <CRLF> or
   * <NL>. (The user must ensure that the TYPE is correct.) This command is intended to return information that can be
   * used by a program to further process the files automatically. For example, in the implementation of a "multiple
   * get" function.
   * 
   * @param pathName Specifies a directory or a file group descriptor. A null value implies the user's current directory.
   * @return The files of the given pathname in the server
   * @throws FTPConnectionClosedException
   * @throws totalcross.io.IOException
   */
  public String[] listNames(String pathName) throws FTPConnectionClosedException, totalcross.io.IOException {
    Socket socket = openReturningPath(true);
    sendCommand("NLST ", pathName, WAITING_FOR_REPLY);
    Vector v = new Vector(50);
    String s;
    if (listReader == null) {
      listReader = new LineReader(socket);
    } else {
      listReader.setStream(socket);
    }
    while (true) {
      if ((s = listReader.readLine()) != null) {
        v.addElement(s);
      } else {
        break;
      }
    }
    if (v.size() > 0 && v.items[v.size() - 1].toString().startsWith("total")) {
      v.removeElementAt(v.size() - 1); // remove the "total" information
    }

    socket.close();
    checkResponse(COMPLETED);
    return (String[]) v.toObjectArray();
  }

  /////////////////////////////////////////////////////////////////////////////////////////

  private Socket openReturningPath(boolean linger)
      throws FTPConnectionClosedException, totalcross.net.UnknownHostException, totalcross.io.IOException {
    String line = sendCommand("PASV", "", COMPLETED); // 227 Entering Passive Mode (219,112,241,81,68,33)
    // get the host and port
    if (line == null || line.length() == 0) {
      throw new totalcross.io.IOException("Nothing read from PASV|connection closed?");
    }
    line = line.substring(line.indexOf('(') + 1);
    line = line.substring(0, line.lastIndexOf(')')); // guich@561_6: some servers return "33)" and others "33)."
    String[] p = Convert.tokenizeString(line, ',');
    if (p == null || p.length != 6) {
      throw new totalcross.io.IOException("Error! Invalid answer: " + line);
    }
    String host = p[0] + '.' + p[1] + '.' + p[2] + '.' + p[3];
    int port = 21;
    try {
      port = (Convert.toInt(p[4]) << 8) | Convert.toInt(p[5]);
    } catch (InvalidNumberException ine) {
      log(ine.getMessage());
      throw new totalcross.io.IOException("Invalid port number: " + p[4] + p[5]);
    }
    log("Connecting to " + host + ":" + port);

    Socket socket = null;
    for (int i = 0; i < 5; i++) // guich@561_8: try more than one time
    {
      try {
        socket = new Socket(host, port, openTimeout, linger); // guich@561_14: 15: use nolinger for Palm OS
        socket.readTimeout = readTimeout;
        socket.writeTimeout = writeTimeout;
        break;
      } catch (totalcross.net.UnknownHostException e) {
        throw e;
      } catch (totalcross.io.IOException e) {
        if (i < 4) {
          Vm.sleep(500);
        } else {
          throw e; // flsobral@tc100b3: failed all 5 times, time to give up and throw the exception caught.
        }
      }
    }
    return socket;
  }

  private boolean isDigit(char c) {
    return '0' <= c && c <= '9';
  }

  private String readAck() throws totalcross.io.IOException {
    try {
      String s;
      do {
        s = myTCPConnectionReader.readLine();
        if (s == null) {
          break;
        }
      } while (!isDigit(s.charAt(0)) || !isDigit(s.charAt(1)) || !isDigit(s.charAt(2)) || s.charAt(3) != ' ');
      return s;
    } catch (ArrayIndexOutOfBoundsException aioobe) {
      return null;
    }
  }

  private String checkResponse(byte expectedReturnType) throws FTPConnectionClosedException, totalcross.io.IOException {
    String line = readAck();
    if (line != null) {
      int ack = -1;
      try {
        ack = Convert.toInt(line.substring(0, 3));
      } catch (InvalidNumberException ine) {
        // This exception will NEVER be thrown by Conver.toInt because readAck() ALWAYS returns strings starting with 3 digits.
        Vm.warning("FTP.checkResponse unexpected error: " + ine.getMessage());
      }
      int type = line.charAt(0) - '0';
      log(line);
      if (type == expectedReturnType) {
        return line;
      }
      if (ack == 421 || ack == 425 || ack == 426) // guich@561_8
      {
        myTCPConnection.close();
        myTCPConnection = null;
        throw new FTPConnectionClosedException("Connection timed out. This FTP|instance is no longer valid.");
      }
      throw new totalcross.io.IOException(
          "Not acknowledged|Expected of type " + expectedReturnType + "|Received: " + ack);
    }
    // Cleans the socket's incoming buffer
    if (buffer == null) {
      buffer = new byte[2048];
    }
    while (myTCPConnection.readBytes(buffer, 0, buffer.length) > 0) {
      ;
    }
    myTCPConnectionReader.setStream(myTCPConnection);
    throw new totalcross.io.IOException("No response|Maybe the ftp was closed?");
  }

  private String sendCommand(String s1, String s2, byte expectedReturnType)
      throws FTPConnectionClosedException, totalcross.io.IOException // guich@562_4: now the 2nd command is passed separately
  {
    if (s1 != null) {
      StringBuffer sb = sbsc;
      sb.setLength(0);
      sb.append(s1);
      if (s2 != null) {
        sb.append(s2);
      }
      sb.append(Convert.CRLF); // guich@562_4: seems that we must send all the string at once
      // Let's avoid a few method calls and call directly the writeBytes with 3 arguments.
      byte[] sbBytes = Convert.getBytes(sb);
      myTCPConnection.writeBytes(sbBytes, 0, sbBytes.length);
      sb.setLength(sb.length() - 2); // remove the \r\n - we don't want to log it
      log(s1.equals("PASS ") ? "PASS xxxxx" : sb.toString());
    }
    return checkResponse(expectedReturnType);
  }

  private void logReplace(String s) {
    // remove the last
    if (cbLog != null) {
      cbLog.remove(cbLog.size() - 1);
    } else if (lbLog != null) {
      lbLog.remove(lbLog.size() - 1);
    }
    // and add the new
    log(s);
  }

  private void log(String s) {
    if (s != null) {
      if (log2console) {
        Vm.debug(s);
      }
      // display in a combobox or listbox if any were assigned
      if (cbLog != null) {
        cbLog.add(s);
        cbLog.selectLast();
      } else if (lbLog != null) {
        lbLog.add(s);
        lbLog.selectLast();
      }
    }
  }

  /**
   * Sets the control where the log will be displayed. Only ComboBox and ListBox are allowed.
   */
  public void setLoggingControl(Control c) {
    if (c == null) //rnovais@571_13: the control can be null
    {
      cbLog = null;
      lbLog = null;
    } else if (c instanceof ComboBox) {
      cbLog = (ComboBox) c;
    } else if (c instanceof ListBox) {
      lbLog = (ListBox) c;
    } else {
      Vm.warning("Only ListBox and ComboBox are alowed in FTP.setLogControl");
    }
  }

  /**
   * Closes the control connection without issuing any quit or abort command to the server.
   * Avoid using this method unless strictly necessary, use the quit method instead. 
   */
  public void forceClose() throws totalcross.io.IOException {
    myTCPConnection.close();
    myTCPConnection = null;
    myTCPConnectionReader = null;
  }
}
