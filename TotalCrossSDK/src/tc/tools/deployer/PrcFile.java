/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/

// $Id: PrcFile.java,v 1.22 2011-01-04 13:19:05 guich Exp $

package tc.tools.deployer;

import totalcross.io.File;

/** A launcher app for PalmOS devices. */

class PrcFile
{
   // Offsets into the byte structure which defines the stub applcation
   int attributeOffset    = 0x21;  // this is fixed - never modifies - used for copy protection
   int creatorOffset      = 0x40;  // this is fixed - never modifies
   int nameOffset=-1;              //      "
   int normalBitmapOffset=-1;      //      "
   int listBitmapOffset=-1;        //      "
   int versionOffset=-1;           //      "   guich@401_8
   int categoryOffset=-1;

   byte []launchString = "L2345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890".getBytes();
   byte []nameString = "APP4567890123456789012345678901".getBytes();
   byte []versionString = "versionstr012".getBytes();
   byte []categoryString = "CATEG6789012345".getBytes();

   // guich@200 - to make modifications easier, we now read the prc bytes from disk and modify it.
   byte bytes[];

   /** the path to this file */
   private String prcPath;
   String sEx = "The Stub.prc file has been modified. some of the placeholder strings could not be found. Reinstall TotalCross. - "; // string Exception - sEx - good name, isnt? ;-)
   /////////////////////////////////////////////////////////////////////////////////////
   /////////////////////////////////////////////////////////////////////////////////////
   /**
    * Constructs a new prcfile with the given path
    */
   public PrcFile(String path) throws Exception
   {
      prcPath=path;
      // guich@200 - read the file from stream

      // fdie@500 - add sdk.root option support
      String root = System.getProperty("sdk.root");
      if (root != null && !root.endsWith("/") && !root.endsWith("\\"))
      {
         root += '/';
      }
      String stub;
      if (root != null)
      {
         stub = (root + "launchers/palm/Launcher.prc").replace('/',java.io.File.separatorChar); // look in a path
         bytes = Utils.findAndLoadFile(stub,false);
      }
      if (bytes == null) // guich@570_121: look inside the jar even if sdk.root is defined.
      {
         stub = DeploySettings.etcDir+"launchers/palm/Launcher.prc".replace('/',java.io.File.separatorChar); // look inside tc.jar
         bytes = Utils.findAndLoadFile(stub,false);
      }
      if (bytes == null)
         throw new DeployerException("Launcher.prc not found. Please add a reference to TotalCrossSDK/lib/tc.jar to the classpath!\n" +
                        "or run the tool with the '-Dsdk.root=<totalcross_sdk_root>' option");
      findOffsets();
   }
   /////////////////////////////////////////////////////////////////////////////////////
   // guich@200 - dynamically find all entries
   private void findOffsets() throws Exception
   {
      nameOffset = Utils.indexOf(bytes, nameString,true);
      versionOffset = Utils.indexOf(bytes, versionString,true);
      categoryOffset = Utils.indexOf(bytes, categoryString,true);
      if (nameOffset == -1) throw new DeployerException(sEx+"3");
      if (versionOffset == -1) throw new DeployerException(sEx+"4");
      if (categoryOffset == -1) throw new DeployerException(sEx+"5");

      // search the prc for the bitmaps 1000 (prc icon) and 1001 (prc list icon)
      int headerLen = 0x4e;
      int numberOfResources = Utils.readInt(bytes,headerLen-4);

      // Read the resource map.
      // struct ResourceMapEntry {ulong fcType; ushort id; ulong ulOffset;};
      // struct PrcBitmap {ushort cx; ushort cy; ushort cbRow; ushort ff; ushort ausUnk[4]; byte *abBits; // word aligned bits

      int offset = headerLen;
      for (int i = 0; i < numberOfResources; i++)
      {
         String resType = new String(bytes, offset, 4);  offset += 4;
         int resId = Utils.readShort(bytes,offset);      offset += 2;
         int resOffset = Utils.readInt(bytes, offset);   offset += 4;
         if (resType.equals("tAIB")) // is it a bitmap?
         {
            if (resId == 1000) normalBitmapOffset = resOffset; else
            if (resId == 1001) listBitmapOffset = resOffset;
         }
      }

      if (normalBitmapOffset == -1) throw new Exception(sEx+"4");
      if (listBitmapOffset == -1) throw new Exception(sEx+"5");
   }
   /////////////////////////////////////////////////////////////////////////////////////
   /**
    * Creates the prcfile with the given settings
    */
   public void create() throws Exception
   {
      if (!"Launcher".equals(new String(bytes,0,8))) // guich@200 - better compare
         throw new Exception("ERROR: bad bytes 0");

      String internalName = DeploySettings.filePrefix;
      if (internalName.length() > nameString.length)
         internalName = internalName.substring(0,30);
      internalName += '_';
      Utils.writeString(bytes, internalName,0,false,nameString.length,(byte)0);

      if (!new String(bytes,creatorOffset,4).equals("TCAp")) // guich@200 - better compare
         throw new Exception("ERROR: bad bytes 1");
      Utils.writeString(bytes, DeploySettings.applicationId,creatorOffset,false);

      bytes[attributeOffset] |= 0x40; // guich@200 - copy protection

      // launch string
      /* guich@tc: there's no more launch string. Now the creator id if got with the Palm OS functions, and then the database
         Crid.TCAp is loaded. Still have to find a way to pass the /D to the application (maybe a bit in the unused attribute bits).

      String launch=DeploySettings.mainWindowClassName+" "+DeploySettings.applicationId;
      if (wants16bpp) // guich@421_50
         launch = "/16 "+launch;
      if (launch.length() > launchString.length) throw new Exception("ERROR: launch string too long");
      Utils.writeStringTakingCare(bytes, launch,launchOffset,true); // guich@200 - no more padding
      */

      DeploySettings.bitmaps.savePalmOSIcons(bytes, normalBitmapOffset, listBitmapOffset);
      // name is <= 31 chars
      if (DeploySettings.appTitle != null)
      Utils.writeString(bytes, DeploySettings.appTitle,nameOffset,true,DeploySettings.appTitle.length(),(byte)32);

      // guich@401_8: after the highres icons were added, the version information is no more at the end of the file
      if (DeploySettings.appVersion != null)
         Utils.writeString(bytes, DeploySettings.appVersion, versionOffset, true);

      // guich@tc100: store the new category
      String cat = totalcross.sys.Settings.appCategory == null ? "TotalCross Apps" : totalcross.sys.Settings.appCategory;
      if (cat.length() > 15) cat = cat.substring(0,15);
      Utils.writeString(bytes, cat, categoryOffset, true, 15, (byte)32);

      // write the file
      File fos = new File(prcPath, File.CREATE_EMPTY);
      fos.writeBytes(bytes,0,bytes.length);
      fos.close();
   }
}