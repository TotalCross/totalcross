/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



package tc.tools.deployer;

import totalcross.io.*;
import totalcross.sys.*;
import totalcross.util.*;

public class Deployer4Linux
{
   private static byte[] versionBytes = {52,0,36,0};

   public Deployer4Linux() throws Exception
   {
      String linuxArguments = DeploySettings.commandLine; // the name of the tcz will be the same as the executable
      byte[] args = linuxArguments.trim().getBytes();
      if (args.length > DeploySettings.defaultArgument.length)
         throw new IllegalArgumentException("Error: command line for Linux too long. It has " + args.length + ", while the maximum allowed is " + DeploySettings.defaultArgument.length);

      String targetDir = DeploySettings.targetDir + "linux/";
      // create the output folder
      File f = new File(targetDir);
      if (!f.exists())
         f.createDir();
      // copy the tcz file to there
      Utils.copyTCZFile(targetDir);
      // guich@tc123_22: process the pkg file
      Hashtable ht = new Hashtable(13);
      Utils.processInstallFile("linux.pkg", ht);
      Vector vLocals  = (Vector)ht.get("[L]"); 
      if (vLocals != null) 
      {
         Utils.preprocessPKG(vLocals,true);
         for (int i = vLocals.size(); --i >= 0;) 
            Utils.copyEntry((String)vLocals.items[i],targetDir);
      }
      Vector vGlobals = (Vector)ht.get("[G]"); 
      if (vGlobals != null)
      {
         Utils.preprocessPKG(vGlobals,true);
         for (int i = vGlobals.size(); --i >= 0;) 
            Utils.copyEntry((String)vGlobals.items[i],targetDir);
      }

      if (DeploySettings.mainClassName != null)
      {
         // copy the launcher for linux to there
         byte[] buf = Utils.findAndLoadFile(DeploySettings.etcDir+"launchers/linux/Launcher",false);
         if (buf == null)
            throw new DeployerException("File linux/Launcher not found!");

         // now find the offset of the argument
         int ofs = Utils.indexOf(buf, DeploySettings.defaultArgument, false);
         if (ofs < 0)
            throw new DeployerException("Can't find offset for command line on linux!");
         Vm.arrayCopy(args, 0, buf, ofs, args.length); // no "write taking care" needed
         buf[ofs+args.length] = 0;

         // find the offset of the version
         ofs = Utils.indexOf(buf, versionBytes, false);
         if (ofs > 0) // found version offset?
         {
            buf[ofs]   = (byte)DeploySettings.getAppVersionHi();
            buf[ofs+2] = (byte)DeploySettings.getAppVersionLo();
         }

         String out = targetDir+DeploySettings.filePrefix;
         File fout = new File(out, File.CREATE_EMPTY);
         fout.writeBytes(buf, 0, buf.length);
         fout.close();
         // set the executable flag
         java.io.File ff = new java.io.File(out);
         ff.setExecutable(true, false);         
      }
      System.out.println("... Files written to folder "+targetDir);
   }
}
