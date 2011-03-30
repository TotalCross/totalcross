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



package tc.tools.deployer;

import totalcross.io.File;
import totalcross.io.FileNotFoundException;
import totalcross.util.Hashtable;

public class Deployer4Palm
{
   String targetDir;
   public Deployer4Palm() throws Exception
   {
      targetDir = DeploySettings.targetDir + "palm/";
      File f = new File(targetDir);
      if (!f.exists())
         f.createDir();

      if (DeploySettings.mainClassName != null)
         createPrc();
      createPdb();
      createInstall();
   }

   private void callMatchbox(String files) throws Exception
   {
      if (!DeploySettings.isWindows()) 
         return; // guich@tc111_26: ignore MacOS
      String installExe = targetDir+"install.exe";
      String installTxt = targetDir+"install.txt";
      String pathToMatchbox = DeploySettings.etcDir+"tools/matchbox/MatchBox.exe";
      String cmdline = pathToMatchbox+" "+"/palm /name \"Installation of "+DeploySettings.appTitle+"...\" /output \""+installExe+"\" /readme \""+installTxt+"\" /files "+files; // guich@tc100b5_57: added " in the parameters
      try
      {
         new File(installExe).delete(); // delete the old file, or we'll finish the process before the new file is written!
      }
      catch (FileNotFoundException e)
      {
      }
      Utils.waitForFile(installTxt);
      Runtime.getRuntime().exec("cmd /c start /B "+cmdline);
      // now we need to wait the process finish. For some reason, the Process.waitFor does not work.
      Utils.waitForFile(installExe);
   }

   private void createInstall() throws Exception
   {
      String prc = targetDir+DeploySettings.filePrefix+".prc";
      String pdb = targetDir+DeploySettings.filePrefix.replace(' ','_')+".pdb"; // guich@tc124_2: the prc contains spaces, but the pdb don't
      if (!new File(pdb).exists())
         throw new DeployerException("File "+pdb+" not found!");
      boolean hasPrc = DeploySettings.mainClassName != null;
      if (hasPrc && !new File(prc).exists())
      {
         Utils.println(prc+" not found. Install file not created.");
         return;
      }
      // check if the file has spaces, and surround with "" if necessary
      if (hasPrc && prc.indexOf(' ') > 0) prc = '"'+prc+'"';
      if (pdb.indexOf(' ') > 0) pdb = '"'+pdb+'"';
      // process the pak file
      Hashtable ht = new Hashtable(13);
      Utils.processInstallFile("palm.pkg", ht);
      String[] extras = Utils.joinGlobalWithLocals(ht, hasPrc ? new String[]{pdb,prc} : new String[]{pdb}, false);
      // create a default install.txt file if none exists
      String txt2delete = targetDir+"install.txt";
      if (new File(txt2delete).exists())
         txt2delete = null;
      else
      {
         String []instructions = {"The following files will be installed:"};
         Utils.writeFile(txt2delete,instructions,Utils.removePath(extras));
      }
      callMatchbox(Utils.toFullPath(extras));
      if (txt2delete != null) new File(txt2delete).delete();
      System.out.println("... Files written to folder "+targetDir);
   }

   private void createPdb() throws Exception
   {
      totalcross.sys.Settings.showDesktopMessages = false;
      String pdbName = DeploySettings.filePrefix.replace(' ','_')+"."+DeploySettings.applicationId+".TCZF";
      String olddp = totalcross.sys.Settings.dataPath;
      totalcross.sys.Settings.dataPath = targetDir;
      PDBStream fout = new PDBStream(pdbName,true);
      File fin = new File(DeploySettings.tczFileName,File.READ_WRITE);
      byte[] b = new byte[fin.getSize()];
      fin.readBytes(b,0,b.length);
      fout.growTo(b.length);
      fout.setPos(0);
      fout.writeBytes(b, 0, b.length);
      fin.close();
      fout.close();
      totalcross.sys.Settings.showDesktopMessages = true;
      totalcross.sys.Settings.dataPath = olddp;
   }

   private void createPrc() throws Exception
   {
      PrcFile prc=new PrcFile(targetDir+DeploySettings.filePrefix+".prc");
      prc.create();
   }

}
