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

// $Id: UnCodIt.jav,v 1.3 2011-01-04 13:21:02 guich Exp $

package tc.tools.deployer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.compress.ZLibInputStream;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.system.EventLogger;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.util.StringUtilities;

public class UnCodIt extends UiApplication
{
   private boolean autoExtract;
   private boolean autoRemove;
   private boolean decompress;
   private String dstRoot;
   private Vector entries = new Vector();
   private long appGuid;
   
   private StringBuffer sbuf = new StringBuffer();
   private byte[] buf = new byte[512];
   private int bufPos = 0;
   private int bufLen = 0;
   
   public UnCodIt()
   {
      // Register itself with LogEvent
      appGuid = StringUtilities.stringHashToLong("UnCodIt");
      EventLogger.register(appGuid, "UnCodIt", EventLogger.VIEWER_STRING);
   }
   
   public boolean loadContent()
   {
      InputStream is = getClass().getResourceAsStream("package.content");
      if (is == null)
      {
         debug("Cannot retrieve package content; reason: file not found", EventLogger.ERROR);
         return false;
      }
      
      try
      {
         String line, field, value;
         int i;
         
         while ((line = readLine(is)) != null && ((i = line.indexOf('=')) >= 0))
         {
            field = line.substring(0, i).toLowerCase();
            value = line.substring(i + 1, line.length());
            
            if (field.equals("autoextract"))
               autoExtract = value.equalsIgnoreCase("true");
            else if (field.equals("autouninstall"))
               autoRemove = value.equalsIgnoreCase("true");
            else if (field.equals("compressed"))
               decompress = value.equalsIgnoreCase("true");
            else if (field.equals("root"))
               dstRoot = value;
            else
               debug("Unknown header field: " + field, EventLogger.WARNING);
         }
         
         while (line != null)
         {
            entries.addElement(line);
            line = readLine(is);
         }
        
        return true;
      }
      catch (IOException e)
      {
         debug("Cannot retrieve package content; reason: " + e.getMessage(), EventLogger.ERROR);
         return false;
      }
   }
   
   public int extract()
   {
      if (!autoExtract && Dialog.ask(Dialog.D_YES_NO, "Files will be extracted to '" + dstRoot + "'. Do you want to continue?", Dialog.YES) == Dialog.NO)
      {
         debug("Operation has been cancelled.", EventLogger.INFORMATION);
         return 0;
      }
      
      int count = 0;
      int len = entries.size();
      
      for (int i = 0; i < len; i ++)
      {
         String entry = (String) entries.elementAt(i);
         InputStream is = getClass().getResourceAsStream(entry + ".res");
         if (is == null)
            debug("Skipping file '" + entry + "'; reason: input resource not found", EventLogger.WARNING);
         else
         {
            if (decompress) // content is compressed
               is = new ZLibInputStream(is, true);
            
            String dir = dstRoot;
            int j = entry.lastIndexOf('/');
            if (j > 0)
               dir += entry.substring(0, j + 1);
            
            if (!mkdirs(dir))
               debug("Skipping file '" + entry + "'; reason: cannot create parent directory", EventLogger.WARNING);
            else
            {
               FileConnection conn = null;
               
               try
               {
                  conn = (FileConnection) Connector.open("file://" + dstRoot + entry, Connector.READ_WRITE);
                  if (conn.exists())
                  {
                     if (autoExtract)
                        debug("Skipping file '" + entry +  "'; reason: destination file already exists", EventLogger.WARNING);
                     else
                     {
                        if (Dialog.ask(Dialog.D_YES_NO, "File '" + (dstRoot + entry) + "' already exists. Do you want to overwrite?", Dialog.NO) == Dialog.NO)
                           conn.close();
                        else
                           conn.truncate(0);
                     }
                  }
                  else
                     conn.create();
                  
                  if (conn.isOpen()) // user has not cancelled
                  {
                     OutputStream os = conn.openOutputStream();
                     
                     int r;
                     while ((r = is.read(buf)) > 0)
                        os.write(buf, 0, r);
                     
                     os.close();
                     count ++;
                  }
               }
               catch (IOException e)
               {
                  debug("Skipping file '" + entry + "'; reason: " + e.getMessage(), EventLogger.ERROR);
                  
                  if (conn != null)
                  {
                     try
                     {
                        if (conn.isOpen())
                           conn.delete();
                        
                        conn.close();
                     }
                     catch (IOException e2) {}
                  }
               }
            }
            
            try
            {
               is.close();
            }
            catch (IOException e) {}
         }
      }
      
      debug(count + " file(s) successfully extracted", EventLogger.INFORMATION);
      
      if (autoRemove)
      {
         CodeModuleManager.deleteModuleEx(ApplicationDescriptor.currentApplicationDescriptor().getModuleHandle(), true);
         CodeModuleManager.promptForResetIfRequired();
      }
      
      return count;
   }
   
   /**
    * @param path
    * @return
    */
   private boolean mkdirs(String path)
   {
      try
      {
         FileConnection conn = (FileConnection) Connector.open("file://" + path);
         if (conn.exists())
            return true;
         
         path = path.substring(0, path.length() - 1); // remove last slash
         String parent = path.substring(0, path.lastIndexOf('/') + 1);
         
         if (!mkdirs(parent))
            return false;
         
         conn.mkdir();
         conn.close();
         
         return true;
      }
      catch (IOException e)
      {
         return false;
      }
   }
   
   /**
    * @param msg
    */
   private void debug(String msg, int severity)
   {
      if (!autoExtract)
      {
         switch (severity)
         {
            case EventLogger.WARNING:
            case EventLogger.ERROR: Dialog.alert(msg); break;
            
            case EventLogger.INFORMATION:
            default: Dialog.inform(msg); break;
         }
      }
      
      EventLogger.logEvent(appGuid, msg.getBytes(), severity);
   }
   
   /**
    * @param is
    * @return
    */
   private String readLine(InputStream is) throws IOException
   {
      if (bufPos == bufLen && !fillBuffer(is))
         return null;
      
      sbuf.setLength(0); // clear buffer
      
      while (bufPos < bufLen)
      {
         byte b = buf[bufPos ++];
         if (b == '\n')
            break;
         else if (b != '\r')
            sbuf.append((char) b);
         
         if (bufPos == bufLen && !fillBuffer(is))
            break;
      }
      
      String s = sbuf.toString();
      return s.length() == 0 ? null : s;
   }
   
   /**
    * @param is
    * @return
    * @throws IOException
    */
   private boolean fillBuffer(InputStream is) throws IOException
   {
      int r = is.read(buf);
      bufLen = r > 0 ? r : 0;
      bufPos = 0;
      
      return bufLen > 0;
   }
   
   /**
    * @param args
    */
   public static void main(String args[])
   {
      final UnCodIt uci = new UnCodIt();
      uci.invokeLater(new Runnable()
      {
         public void run()
         {
            if (uci.loadContent())
               uci.extract();
            
            System.exit(0);
         }
      });
      
      uci.enterEventDispatcher();
   }
}
