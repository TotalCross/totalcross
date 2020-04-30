// Copyright (C) 1998, 1999 Wabasoft <www.wabasoft.com>
// Copyright (C) 2000 Matthias Ringwald
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

package totalcross.io.device;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import totalcross.io.Stream;
import totalcross.sys.Convert;

/**
 * PortConnector accesses the device ports.
 * <p>
 * This works on the devices and on JDK (in this case, it uses javax.comm, which must
 * be installed separately)
 * <p>
 * When a serial port is created, an attempt is made to open the port.
 * If the open attempt is successful, the port will remain open until close() is called.
 * If close() is never called, the port will be closed when the object
 * is garbage collected.
 * <p>
 *
 * Here is an example showing data being written and read from a serial port:
 *
 * <pre>
 * PortConnector port = new PortConnector(0, 9600);
 * DataStream ds = new DataStream(port);
 * ds.writeCString("Hello...\r\n");
 * port.close();
 * </pre>
 * On JDK, you can define a file called serial.properties with the number and ports
 * to be used:<br>
 *  DEFAULT=a<br>
 *  IRCOMM=b<br>
 *  SIR=c<br>
 *  USB=d<br>
 *  If the file is not found, it will use COM1, COM2, COM3, and COM4, respectively.
 */

public class PortConnector extends Stream {
  private Object portConnectorRef;
  Object _receiveBuffer; // Used only on PALMOS
  int _portNumber;

  /** Defines the write timeout, in milliseconds. The default is 6 seconds. */
  public int writeTimeout = 6000; // guich@570_67
  /** Defines the read timeout, in milliseconds. The default is 6 seconds. */
  public int readTimeout = 6000; // guich@570_67

  /** Set to false to not let the timeout error stop the write check loop. */
  public boolean stopWriteCheckOnTimeout = true; // guich@570_65

  // Logical Ports used in TotalCross
  // NOTE: The TotalCross VM indexes directly to these values

  /** Default Port (cradle) */
  public static final int DEFAULT = 0;

  /** IrCOMM Port (Serial Connection on top of IrDA Stack) */
  public static final int IRCOMM = 0x1000;

  /** SIR Port (Physical Layer of IrDA Stack). Important! In order
   * to access the SIR port on the Pocket PCs the following has to be
   * UNCHECKED: Settings/Connections/Beam/Receive all incoming beams and select
   * discoverable mode. Note that Palm OS OMAP-based devices do NOT support
   * SIR (aka raw IR); see this: http://www.alanjmcf.me.uk/comms/infrared/IrDA%20FAQ.html
   * and http://news.palmos.com/read/messages?id=146145#146145.
   * Note also that there's no handshaking support on SIR, and that not all devices work
   * with it. Use IRCOMM instead when possible.
   */
  public static final int SIR = 0x1001;

  /** USB Endpoint 2 Port */
  public static final int USB = 0x1002;

  /** Bluetooth: open the built-in Bluetooth discovery dialog,
   * then establish a Serial Port Profile (just serial emulation across
   * Bluetooth, using the RFComm BT layer) virtual serial port connection.
   * Note: on WinCE, this maps to the Serial (port 0). You must explicitly
   * map your BlueTooth outside the program.
   */
  public static final int BLUETOOTH = 0x1003; // guich@330_34

  /** Used in the constructor to define the parity */
  public static final int PARITY_NONE = 0;
  /** Used in the constructor to define the parity */
  public static final int PARITY_EVEN = 1;
  /** Used in the constructor to define the parity */
  public static final int PARITY_ODD = 2;

  // JDK implementation
  private Object thisInputStream;
  private Object thisOutputStream;

  /**
   * Opens a port.
   *
   * @param number port number. In Windows, this is the number of the COM port.
   * @param baudRate baud rate
   * @param bits bits per char [5 to 8]
   * @param parity true for even parity, false for no parity
   * @param stopBits number of stop bits
   * @see #setFlowControl
   * @see #PortConnector(int, int, int, int, int)
   */
  public PortConnector(int number, int baudRate, int bits, boolean parity, int stopBits)
      throws totalcross.io.IllegalArgumentIOException, totalcross.io.IOException {
    this(number, baudRate, bits, parity ? PARITY_EVEN : PARITY_NONE, stopBits);
  }

  /**
   * Opens a port. The number passed is the number of the
   * port for devices with multiple ports. Port number
   * 0 defines the "default port" of a given type. For Windows CE
   * and Palm OS devices, you should pass 0 as the port number to
   * access the device's single serial or usb port.
   * <p>
   * On Windows devices, port numbers map to COM port numbers.
   * For example, serial port 2 maps to "COM2:".
   * <p>
   * Here is an example showing how to open the serial port of a
   * Palm OS device at 9600 baud with settings of 8 bits,
   * no partity and one stop bit (8/N/1):
   * <pre>
   * PortConnector port = new PortConnector(0, 9600, 8, false, 1);
   * </pre>
   * Here is an example of opening serial port COM2: on a Windows device:
   * <pre>
   * PortConnector port = new PortConnector(2, 57600, 8, false, 1);
   * </pre>
   * No serial XON/XOFF flow control (commonly called software flow control)
   * is used and RTS/CTS flow control (commonly called hardware flow control)
   * is turn on by default on all platforms but Windows CE. The parity setting
   * must be one of the constants PARITY_NONE, PARITY_EVEN, PARITY_ODD. For Palm OS,
   * PARITY_ODD turns on XON/XOFF mode.
   *
   * @param number port number. In Windows, this is the number of the COM port. On Windows and Palm OS, you can pass
   * a 4-letter that indicates the connection that will be used. For example, on Palm OS you could use <code>Convert.chars2int("rfcm")</code>
   * or in Windows CE use <code>Convert.chars2int("COM4")</code>.
   * @param baudRate baud rate
   * @param bits bits per char [5 to 8]
   * @param parity of the constants PARITY_NONE, PARITY_EVEN, PARITY_ODD. For Palm OS, PARITY_ODD turns on XonXoff mode.
   * @param stopBits number of stop bits
   * @see #setFlowControl
   * @since SuperWaba 4.5
   */
  public PortConnector(int number, int baudRate, int bits, int parity, int stopBits)
      throws totalcross.io.IllegalArgumentIOException, totalcross.io.IOException {
    if (bits < 5 || bits > 8) {
      throw new totalcross.io.IllegalArgumentIOException("bits", Convert.toString(bits));
    }

    _portNumber = number;
    create(number, baudRate, bits, parity, stopBits);
  }

  /**
   * Open a port with settings of 8 bits, no parity and 1 stop bit.
   * These are the most commonly used port settings.
   * @param number port number. On Windows, this is the number of the COM port.
   * @param baudRate baud rate
   * @see #PortConnector(int, int, int, int, int)
   */
  public PortConnector(int number, int baudRate)
      throws totalcross.io.IllegalArgumentIOException, totalcross.io.IOException {
    this(number, baudRate, 8, PARITY_NONE, 1);
  }

  private void create(int number, int baudRate, int bits, int parity, int stopBits) throws totalcross.io.IOException {
    try {
      // guich@330_38: use introspection to be able to compile everything without the comm.jar
      // javax.comm.CommPortIdentifier portId = javax.comm.CommPortIdentifier.getPortIdentifier(getPortName(number));
      Class<?> cpi = Class.forName("javax.comm.CommPortIdentifier"); // get the class

      String portName = getPortName(number);
      System.out.println("Port found: " + portName);

      Method mGetPortIdentifier = cpi.getMethod("getPortIdentifier", new Class[] { String.class }); // get the method getPortIdentifier(String)
      Object portId = mGetPortIdentifier.invoke(null, new Object[] { portName }); // invoke the getPortIdentifier(getPortName(number)) method

      // javax.comm.SerialPort port = (javax.comm.SerialPort)portId.open("TotalCross", 1000);
      Method mOpen = portId.getClass().getMethod("open", new Class[] { String.class, int.class }); // get the method getPortIdentifier(String)
      Object port = mOpen.invoke(portId, new Object[] { "TotalCross", new Integer(1000) }); // invoke the portId.open method

      // port.setSerialPortParams(baudRate, bits, stopBits, parity?javax.comm.SerialPort.PARITY_EVEN:javax.comm.SerialPort.PARITY_NONE);
      Class<?> cSerialPort = port.getClass().getSuperclass();
      Method mSetSerialPortParams = cSerialPort.getMethod("setSerialPortParams",
          new Class[] { int.class, int.class, int.class, int.class }); // get the method setSerialPortParams
      String[] parities = { "PARITY_NONE", "PARITY_EVEN", "PARITY_ODD" };
      Field parityField = cSerialPort.getField(parities[parity]);
      mSetSerialPortParams.invoke(port, new Object[] { new Integer(baudRate), new Integer(bits), new Integer(stopBits),
          new Integer(parityField.getInt(port)) });

      // port.enableReceiveTimeout(100);
      Method mEnableReceiveTimeout = cSerialPort.getMethod("enableReceiveTimeout", new Class[] { int.class });
      mEnableReceiveTimeout.invoke(port, new Object[] { new Integer(100) });

      // thisInputStream = port.getInputStream();
      Method mGetInputStream = cSerialPort.getMethod("getInputStream");
      thisInputStream = mGetInputStream.invoke(port);

      // thisOutputStream = port.getOutputStream();
      Method mGetOutputStream = cSerialPort.getMethod("getOutputStream");
      thisOutputStream = mGetOutputStream.invoke(port);

      portConnectorRef = port;
    } catch (java.lang.reflect.InvocationTargetException ule) {
      throw new totalcross.io.IOException(ule.getTargetException().getClass().getName() + " - "
          + ule.getTargetException().getMessage()
          + "\nThe probable cause is that the port is in use (You must kill HotSync)\nor that the win32com.dll is not accessible in your path. Please add '/TotalCross/etc/tools/commapi' to your path or add '-Djava.library.path=/TotalCross/etc/tools/commapi' to your java.exe command"); // guich@tc110_62:
    } catch (java.lang.NoClassDefFoundError e) {
      throw new java.lang.NoClassDefFoundError(e.getClass().getName() + " - " + e.getMessage()
          + "\nCannot find javax.comm.CommPortIdentifier. You must add the /TotalCross3/etc/tools/commapi/comm.jar file to your classpath. If you're not using Windows, then you must download the respective Comm api at http://java.sun.com/products/javacomm");
    } catch (Throwable t) {
      throw new totalcross.io.IOException(t.getClass().getName() + " - " + t.getMessage());
    }
  }

  /**
   * Closes the port.
   */
  @Override
  public void close() throws totalcross.io.IOException {
    if (portConnectorRef == null) {
      throw new totalcross.io.IOException("The port is not open");
    }

    try {
      nativeClose();
    } finally {
      portConnectorRef = null;
    }
  }

  final private void nativeClose() throws totalcross.io.IOException {
    try {
      // ((javax.comm.SerialPort)thisPortConnector).close();
      Method m = portConnectorRef.getClass().getSuperclass().getMethod("close");
      m.invoke(portConnectorRef);
    } catch (Throwable e) {
      throw new totalcross.io.IOException(e.getMessage());
    }
  }

  /**
   * Turns RTS/CTS flow control (hardware flow control) on or off.
   * @param on pass true to set flow control on and false to set it off
   */
  final public void setFlowControl(boolean on) throws totalcross.io.IOException {
    try {
      //((javax.comm.SerialPort)thisPortConnector).setFlowControlMode(on?javax.comm.SerialPort.FLOWCONTROL_RTSCTS_IN|javax.comm.SerialPort.FLOWCONTROL_RTSCTS_OUT:javax.comm.SerialPort.FLOWCONTROL_NONE);
      Field fRTSCTS_IN = portConnectorRef.getClass().getSuperclass().getField("FLOWCONTROL_RTSCTS_IN");
      Field fRTSCTS_OUT = portConnectorRef.getClass().getSuperclass().getField("FLOWCONTROL_RTSCTS_OUT");
      Field fNONE = portConnectorRef.getClass().getSuperclass().getField("FLOWCONTROL_NONE");
      Method m = portConnectorRef.getClass().getSuperclass().getMethod("setFlowControlMode", new Class[] { int.class });
      m.invoke(portConnectorRef,
          new Object[] { on ? new Integer(fRTSCTS_IN.getInt(portConnectorRef) | fRTSCTS_OUT.getInt(portConnectorRef))
              : new Integer(fNONE.getInt(portConnectorRef)) });
    } catch (Throwable t) {
      throw new totalcross.io.IOException(t.getMessage() + "\nNot Supported?");
    }
  }

  @Override
  public int readBytes(byte buf[], int start, int count) throws totalcross.io.IOException {
    if (portConnectorRef == null) {
      throw new totalcross.io.IOException("The port is not open");
    }
    if (buf == null) {
      throw new java.lang.NullPointerException("Argument 'buf' cannot have a null value.");
    }
    if (start < 0 || count < 0 || start + count > buf.length) {
      throw new ArrayIndexOutOfBoundsException();
    }
    if (count == 0) {
      return 0;
    }

    return readWriteBytes(buf, start, count, true);
  }

  /**
   * Returns the number of bytes currently available to be read from the
   * port's queue. */
  final public int readCheck() throws totalcross.io.IOException {
    try {
      //return ((java.io.InputStream)thisInputStream).available();
      Method m = thisInputStream.getClass().getSuperclass().getMethod("available");
      Integer i = (Integer) m.invoke(thisInputStream);
      return i.intValue();
    } catch (Throwable t) {
      throw new totalcross.io.IOException(t.getMessage());
    }
  }

  /**
   * Writes to the port. Returns the number of bytes written or throws an <code>IOException</code> if an error prevented the write operation from occurring. If data
   * can't be written to the port and flow control is on, the write
   * operation will time out and fail after approximately 2 seconds.
   * @param buf the byte array to write data from
   * @param start the start position in the byte array
   * @param count the number of bytes to write
   */
  @Override
  public int writeBytes(byte buf[], int start, int count) throws totalcross.io.IOException {
    if (portConnectorRef == null) {
      throw new totalcross.io.IOException("The port is not open");
    }
    if (buf == null) {
      throw new java.lang.NullPointerException("Argument 'buf' cannot have a null value.");
    }
    if (start < 0 || count < 0 || start + count > buf.length) {
      throw new ArrayIndexOutOfBoundsException();
    }
    if (count == 0) {
      return 0;
    }

    return readWriteBytes(buf, start, count, false);
  }

  final private int readWriteBytes(byte buf[], int start, int count, boolean isRead) throws totalcross.io.IOException {
    if (isRead) {
      try {
        // guich@tc: now the readTimeout is a field
        Method mEnableReceiveTimeout = portConnectorRef.getClass().getSuperclass().getMethod("enableReceiveTimeout",
            new Class[] { int.class });
        mEnableReceiveTimeout.invoke(portConnectorRef, new Object[] { new Integer(readTimeout) });

        int tries = readTries;
        Method m = thisInputStream.getClass().getSuperclass().getMethod("read",
            new Class[] { byte[].class, int.class, int.class });
        // guich@330_42: make sure we have read everything
        int total = count;
        do {
          //return ((java.io.InputStream)thisInputStream).read(buf, start, count);
          Integer i = (Integer) m.invoke(thisInputStream, new Object[] { buf, new Integer(start), new Integer(count) });
          int read = i.intValue();
          if (read <= 0) {
            break;
          }
          start += read;
          count -= read;
        } while (count > 0 && tries-- > 0);
        return total;
      } catch (Throwable t) {
        throw new totalcross.io.IOException(t.getMessage() + "\nNot Supported?");
      }
    } else {
      try {
        // guich@330_42: make sure we have wrote everything
        //((java.io.OutputStream)thisOutputStream).write(buf, start, count);
        // note: write returns void
        Method m = thisOutputStream.getClass().getSuperclass().getMethod("write",
            new Class[] { byte[].class, int.class, int.class });
        m.invoke(thisOutputStream, new Object[] { buf, new Integer(start), new Integer(count) });
        return count;
      } catch (Throwable t) {
        throw new totalcross.io.IOException(t.getMessage() + "\nNot Supported?");
      }
    }
  }

  // JDK implementation

  private static String portDEFAULT, portIRCOMM, portSIR, portUSB;
  /** Set this to a value that will be used to keep trying to read further while something is available.
   * If a series of bytes is returned in very few steps, this will make the read stop after readTries has reached zero.
   * @since SuperWaba 5.72
   */
  public static int readTries = 10; // guich@572_8

  private static void initSerial() {
    java.util.ResourceBundle serial;
    try {
      serial = java.util.ResourceBundle.getBundle("serial");
      portDEFAULT = serial.getString("DEFAULT");
      portIRCOMM = serial.getString("IRCOMM");
      portSIR = serial.getString("SIR");
      portUSB = serial.getString("USB");
    } catch (java.util.MissingResourceException e) {
      System.out.println("Error: missing resource bundle: " + e.getClassName());
      System.out.println("Using defaults..."); // guich@400_20
      portDEFAULT = "COM1";
      portIRCOMM = "COM2";
      portSIR = "COM3";
      portUSB = "COM4";
    }
  }

  /**
   * @return When number == Logical port number defined in totalcross.io.device.PortConnector -
   * name of port assigned in serial.properties file.
   * For other values - returns first port name with given number inside of it.
   */
  static private String getPortName(int number) {
    if (portDEFAULT == null) {
      initSerial();
    }

    switch (number) {
    case PortConnector.DEFAULT:
      return portDEFAULT;
    case PortConnector.IRCOMM:
      return portIRCOMM;
    case PortConnector.SIR:
      return portSIR;
    case PortConnector.USB:
      return portUSB;
    }

    try {
      //java.util.Enumeration portIds = javax.comm.CommPortIdentifier.getPortIdentifiers();
      Class<?> cpi = Class.forName("javax.comm.CommPortIdentifier"); // get the class
      Method mGetPortIdentifiers = cpi.getMethod("getPortIdentifiers");
      java.util.Enumeration<?> portIds = (java.util.Enumeration<?>) mGetPortIdentifiers.invoke(null);

      if (number == 0) {
        if (portIds.hasMoreElements()) {
          // return ((javax.comm.CommPortIdentifier)portIds.nextElement()).getName();
          Object oCommPortIdentifier = portIds.nextElement();
          Method mGetName = oCommPortIdentifier.getClass().getMethod("getName");
          String portName = (String) mGetName.invoke(oCommPortIdentifier);
          return portName;
        }
      } else {
        while (portIds.hasMoreElements()) {
          // portName = ((javax.comm.CommPortIdentifier)portIds.nextElement()).getName();
          Object oCommPortIdentifier = portIds.nextElement();
          Method mGetName = oCommPortIdentifier.getClass().getMethod("getName");
          String portName = (String) mGetName.invoke(oCommPortIdentifier);
          if (portName.indexOf(number + '0') != -1) {
            return portName;
          }
        }
      }
    } catch (Exception ee) {
      ee.printStackTrace();
    }
    return null;
  }

  @Override
  protected void finalize() {
    try {
      close();
    } catch (totalcross.io.IOException e) {
    }
  }
}