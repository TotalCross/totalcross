/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 3.0    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-3.0.txt                                     *
 *                                                                               *
 *********************************************************************************/



package totalcross.net;

import java.io.EOFException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;

import net.rim.device.api.servicebook.ServiceBook;
import net.rim.device.api.servicebook.ServiceRecord;
import net.rim.device.api.synchronization.ConverterUtilities;
import net.rim.device.api.system.CoverageInfo;
import net.rim.device.api.system.RadioInfo;
import net.rim.device.api.system.WLANInfo;
import net.rim.device.api.util.DataBuffer;
import totalcross.Launcher4B;
import totalcross.io.IOException;
import totalcross.io.IllegalArgumentIOException;
import totalcross.util.Logger;

public class ConnectionManager4B
{
   static Object connRef;

   public static final int CRADLE = 1;
   public static final int WIFI = 2;
   public static final int CELLULAR = 3;
   public static final int GPRS = CELLULAR;
   
   private static final String[] CONNECTION_NAMES = new String[] { "", "CRADLE", "WIFI", "CELLULAR" };
   
   private static final int TRANSPORT_WIFI = 0;
   private static final int TRANSPORT_MDS = 1;
   private static final int TRANSPORT_BIS = 2;
   private static final int TRANSPORT_TCP = 3;
   private static final int TRANSPORT_WAP2 = 4;
   private static final int TRANSPORT_WAP1 = 5;
   
   private static final int TRANSPORT_COUNT = 6;
   private static final String[] TRANSPORT_NAMES = new String[] { "WIFI", "BES", "BIS", "TCP", "WAP2", "WAP" };

   private static String[] transportCfg = new String[TRANSPORT_NAMES.length];
   private static ServiceRecord[] serviceRecords = new ServiceRecord[TRANSPORT_NAMES.length];
   private static Object serviceBookLock = new Object();
   
   private static final int CONFIG_TYPE_WAP = 0;
   
   private static Logger logger = Logger.getLogger("totalcross.net");
   
   private ConnectionManager4B()
   {
   }

   public static void setDefaultConfiguration(int type, String cfg) throws IOException
   {
      cfg = parseParameters(cfg);
      switch (type)
      {
         case CRADLE:
            break;
         case WIFI:
            transportCfg[TRANSPORT_WIFI] = cfg;
            break;
         case CELLULAR:
            for (int transport = 0; transport < TRANSPORT_COUNT; transport++)
               transportCfg[transport] = cfg;
            break;
         default:
            throw new IllegalArgumentIOException("Invalid value for argument 'type'");
      }
   }

   public static boolean isAvailable(int type) throws IOException
   {
      loadServiceBook();
      
      switch (type)
      {
         case CRADLE:
            return false;
         case WIFI:
            return hasCoverage(TRANSPORT_WIFI);
         case CELLULAR:
            for (int transport = 0; transport < TRANSPORT_COUNT; transport++)
               if (hasCoverage(transport))
                  return true;
            return false;
         default:
            throw new IllegalArgumentIOException("Invalid value for argument 'type'");
      }
   }
   
   public static void open() throws IOException
   {
      int transport = open(null);
      if (transport == -1)
      {
         connRef = null;
         throw new IOException("No connections available");
      }
      else
         connRef = new Integer(transport);
   }

   private static int open(boolean[] ignore) throws IOException
   {
      StringBuffer sb = new StringBuffer();
      if (ignore != null)
      {
         for (int i = ignore.length - 1; i >= 0; i--)
         {
            if (ignore[i])
            {
               sb.append(sb.length() == 0 ? " ignoring " : ", ");
               sb.append(TRANSPORT_NAMES[i]);
            }
         }
      }
      
      sb.insert(0, "Trying to open first available network transport");
      logger.info(sb.toString());
      
      loadServiceBook();
      
      for (int transport = 0; transport < TRANSPORT_COUNT; transport++)
         if ((ignore == null || !ignore[transport]) && hasCoverage(transport))
            return transport;

      logger.warning("Could not find a network transport to use");
      return -1;
   }

   public static void open(int type) throws IOException
   {
      loadServiceBook();
      connRef = null;
      
      switch (type)
      {
         case CRADLE:
            break;
         case WIFI:
            if (hasCoverage(TRANSPORT_WIFI))
               connRef = new Integer(TRANSPORT_WIFI);
            break;
         case CELLULAR:
            for (int transport = 0; transport < TRANSPORT_COUNT && connRef == null; transport++)
               if (hasCoverage(transport))
                  connRef = new Integer(transport);
            break;
         default:
            throw new IllegalArgumentIOException("Invalid value for argument 'type'");
      }
      
      if (connRef == null)
         throw new IOException("Connection not available: " + CONNECTION_NAMES[type]);
   }

   public static void close() throws IOException
   {
      connRef = null;
   }

   public static String getHostAddress(String host) throws UnknownHostException
   {
      throw new UnknownHostException("Unsupported operation");
   }

   public static String getHostName(String host) throws UnknownHostException
   {
      throw new UnknownHostException("Unsupported operation");
   }

   public static String getLocalHost() throws UnknownHostException
   {
      throw new UnknownHostException("Unsupported operation");
   }

   static String parseParameters(String params)
   {
      if (params != null)
      {
         StringBuffer sb = new StringBuffer(params.trim());
         int length = sb.length();

         while (length > 0 && sb.charAt(0) == ';') // remove all leading ';'
         {
            sb.deleteCharAt(0);
            length--;
         }
         while (length > 0 && sb.charAt(length - 1) == ';') // remove all trailing ';'
         {
            sb.deleteCharAt(length - 1);
            length--;
         }

         Launcher4B.stringReplace(sb, "directtcp=", "deviceside=");
         Launcher4B.stringReplace(sb, "apnuser=", "tunnelauthusername=");
         Launcher4B.stringReplace(sb, "apnpass=", "tunnelauthpassword=");

         params = sb.toString();
      }

      return params;
   }

   static String getParameterValue(String params, String key, String def)
   {
      if (params == null)
         return def;
      else
      {
         params = ";" + params;
         String fullKey = ";" + key + "=";

         int i = params.indexOf(fullKey);
         if (i == -1)
            return def;
         else
         {
            i += fullKey.length();
            int j = params.indexOf(';', i);
            if (j == -1)
               j = params.length();

            return params.substring(i, j);
         }
      }
   }

   private static void loadServiceBook()
   {
      synchronized (serviceBookLock)
      {
         ServiceBook sb = ServiceBook.getSB();
         ServiceRecord[] records = sb.getRecords();

         for (int i = 0; i < serviceRecords.length; i++)
               serviceRecords[i] = null;

         for (int i = 0; i < records.length; i++)
         {
            ServiceRecord rec = records[i];
            String cid, name;

            if (rec.isValid() && !rec.isDisabled())
            {
               cid = rec.getCid().toLowerCase();
               name = rec.getName().toLowerCase();
               
               // MDS
               if (cid.equals("ippp") && (name.equals("bes") || name.equals("desktop") || name.equals("blackberry")))
                  serviceRecords[TRANSPORT_MDS] = rec;
               
               // BIS
               else if (cid.equals("ippp") && (name.equals("ippp for bibs") || name.equals("ippp for bis-b")))
                  serviceRecords[TRANSPORT_BIS] = rec;
               
               // WAP 1.0
               else if (cid.equals("wap") && getConfigType(rec) == CONFIG_TYPE_WAP)
                  serviceRecords[TRANSPORT_WAP1] = rec;

               // WAP 2.0
               else if (cid.equals("wptcp") && name.equals("wap2 transport"))
                  serviceRecords[TRANSPORT_WAP2] = rec;
            }
         }
      }
   }
   
   private static boolean hasCoverage(int transport)
   {
      boolean hasCoverage = false;
      
      switch (transport)
      {
         case TRANSPORT_WIFI:
            if (getParameterValue(transportCfg[transport], "allowwifi", "true").equalsIgnoreCase("true"))
               hasCoverage = RadioInfo.areWAFsSupported(RadioInfo.WAF_WLAN) && WLANInfo.getWLANState() == WLANInfo.WLAN_STATE_CONNECTED;
            break;
         case TRANSPORT_MDS:
            if (getParameterValue(transportCfg[transport], "allowbes", "true").equalsIgnoreCase("true"))
               hasCoverage = serviceRecords[TRANSPORT_MDS] != null && CoverageInfo.isCoverageSufficient(CoverageInfo.COVERAGE_MDS);
            break;
         case TRANSPORT_BIS:
            if (getParameterValue(transportCfg[transport], "allowbis", "true").equalsIgnoreCase("true"))
            {
               //$IF,OSVERSION,>=,460
               hasCoverage = serviceRecords[TRANSPORT_BIS] != null && CoverageInfo.isCoverageSufficient(CoverageInfo.COVERAGE_BIS_B);
               //$ELSE
               hasCoverage = serviceRecords[TRANSPORT_BIS] != null;
               //$END
            }
            break;
         case TRANSPORT_WAP1:
         case TRANSPORT_WAP2:
            if (getParameterValue(transportCfg[transport], "allowwap", "true").equalsIgnoreCase("true"))
            {
               //$IF,OSVERSION,>=,450
               hasCoverage = (transport == TRANSPORT_WAP1 || serviceRecords[TRANSPORT_WAP2] != null) && CoverageInfo.isCoverageSufficient(CoverageInfo.COVERAGE_DIRECT);
               //$ELSE
               hasCoverage = (transport == TRANSPORT_WAP1 || serviceRecords[TRANSPORT_WAP2] != null) && CoverageInfo.isCoverageSufficient(1 /*CoverageInfo.COVERAGE_CARRIER*/);
               //$END
            }
            break;
         case TRANSPORT_TCP:
            if (getParameterValue(transportCfg[transport], "allowtcp", "true").equalsIgnoreCase("true"))
            {
               //$IF,OSVERSION,>=,450
               hasCoverage = CoverageInfo.isCoverageSufficient(CoverageInfo.COVERAGE_DIRECT);
               //$ELSE
               hasCoverage = CoverageInfo.isCoverageSufficient(1 /*CoverageInfo.COVERAGE_CARRIER*/);
               //$END
            }
            break;
      }
      
      logger.fine("Network transport " + TRANSPORT_NAMES[transport] + (hasCoverage ? " has " : " does not have ") + "sufficient coverage");
      return hasCoverage;
   }
   
   private static String getURL(int transport, String url, int timeout)
   {
      switch (transport)
      {
         case TRANSPORT_WIFI:
            url += ";interface=wifi";
            break;
         case TRANSPORT_MDS:
            url += ";deviceside=false;ConnectionTimeout=" + timeout;
            break;
         case TRANSPORT_BIS:
            url += ";deviceside=false;ConnectionType=mds-public;ConnectionTimeout=" + timeout + ";ConnectionUID=" + serviceRecords[TRANSPORT_BIS].getUid();
            break;
         case TRANSPORT_WAP2:
            url += ";deviceside=true;ConnectionUID=" + serviceRecords[TRANSPORT_WAP2].getUid();
            break;
         case TRANSPORT_TCP:
         case TRANSPORT_WAP1:
            url += ";deviceside=true";
            break;
      }
      
      String params = transportCfg[transport];
      if (params != null && params.length() > 0)
         url += ";" + params;
      
      return url;
   }
   
   static SocketConnectionHolder getConnection(String url, int timeout) throws IOException
   {
      SocketConnectionHolder connHolder = null;
      boolean[] ignore = new boolean[TRANSPORT_COUNT];
      int transport = connRef == null ? -1 : ((Integer)connRef).intValue();
      
      while (connHolder == null)
      {
         try
         {
            if (transport != -1 && !hasCoverage(transport))
               ignore[transport] = true;
            if ((transport == -1 || ignore[transport]) && (transport = open(ignore)) == -1)
               throw new IOException("No connections available");
            
            connHolder = new SocketConnectionHolder(transport, getURL(transport, url, timeout));
         }
         catch (IOException ex)
         {
            if (transport == -1) // no more transports available
            {
               logger.warning("Failed to open a network transport; " + ex.getMessage());
               throw ex;
            }
            else // error trying to connect to this specific transport, so try to open again ignoring it
            {
               logger.warning("Failed to open network transport " + TRANSPORT_NAMES[transport] + "; " + ex.getMessage());
               ignore[transport] = true;
            }
         }
      }
      
      return connHolder;
   }
   
   private static int getConfigType(ServiceRecord record)
   {
      return getDataInt(record, 12);
   }

   private static int getDataInt(ServiceRecord record, int type)
   {
      DataBuffer buffer = null;
      buffer = getDataBuffer(record, type);

      if (buffer != null)
      {
         try
         {
            return ConverterUtilities.readInt(buffer);
         }
         catch (EOFException e)
         {
            return -1;
         }
      }
      return -1;
   }

   private static DataBuffer getDataBuffer(ServiceRecord record, int type)
   {
      byte[] data = record.getApplicationData();
      if (data != null)
      {
         DataBuffer buffer = new DataBuffer(data, 0, data.length, true);
         try
         {
            buffer.readByte();
         }
         catch (EOFException e1)
         {
            return null;
         }
         
         if (ConverterUtilities.findType(buffer, type))
            return buffer;
      }
      return null;
   }
   
   static class SocketConnectionHolder
   {
      private String url;
      private SocketConnection conn;
      private InputStream is;
      private OutputStream os;
      private int transport;
      
      public SocketConnectionHolder(int transport, String url) throws IOException
      {
         this.transport = transport;
         this.url = url;
         
         try
         {
            logger.info("Opening connection using network transport " + TRANSPORT_NAMES[transport] + "; " + url);
            conn = (SocketConnection)Connector.open(url, Connector.READ_WRITE, false);
            is = conn.openInputStream();
            os = conn.openOutputStream();
         }
         catch (java.io.IOException ex)
         {
            try
            {
               if (conn != null)
               {
                  conn.close();
                  conn = null;
               }
            }
            catch (java.io.IOException ex2) {}
            
            try
            {
               if (is != null)
               {
                  is.close();
                  is = null;
               }
            }
            catch (java.io.IOException ex2) {}
            
            logger.warning("Error opening connection: " + ex.getMessage());
            throw new IOException(ex.getMessage());
         }
      }
      
      public String getURL()
      {
         return url;
      }
      
      public int getTransport()
      {
         return transport;
      }
      
      public String getTransportName()
      {
         return TRANSPORT_NAMES[transport];
      }
      
      public SocketConnection getConnection()
      {
         return conn;
      }
      
      public InputStream getInputStream()
      {
         return is;
      }
      
      public OutputStream getOutputStream()
      {
         return os;
      }
   }
}
