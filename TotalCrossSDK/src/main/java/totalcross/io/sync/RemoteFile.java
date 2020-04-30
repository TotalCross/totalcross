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

package totalcross.io.sync;

import com.totalcross.annotations.ReplacedByNativeOnDeploy;
import totalcross.sys.Vm;

/**
 * Allows you to access a file on the device from the desktop during the conduit synchronization.<br>
 * It may only be used on files stored using a file system, therefore, it cannot be used to handle files on the PalmOS
 * internal memory. But you may use it to handle files on the device's built-in memory or an inserted external card.
 */
public final class RemoteFile {
  static {
    if (!Vm.attachNativeLibrary("TCSync")) {
      throw new RuntimeException("Could not load native library\nTCSync.dll.\n\nPlease add its location\nto the path.");
    }
  }

  /**
   * Lists the files contained in a folder.<br>
   * The strings returned are the names of the files and directories contained within this folder. This method returns
   * null if the folder can't be read or if the operation fails. Path names ends with /, so it is easy to distinguish
   * them from a file.<br>
   * On Palm OS, passing an empty string will return all installed cards. The flash memory slot will be named
   * <code>builtin</code> (which is usually slot 1). Then, to copy to/from a slot, you must use slot:/full_path_to_file
   * (E.G.: 1:/dbs/myapp/MyPDBFile.pdb).
   */
  @ReplacedByNativeOnDeploy
  public static String[] listFiles(String folder) {
    return null;
  }

  /**
   * Copies a file from the desktop to the remote device, creating the target folder if necessary.
   * 
   * @param srcFile
   *           The file on the desktop.
   * @param dstFile
   *           The target file on the remote device. Note: this CANNOT be a folder name, but the target file name
   *           instead.
   * @return true if the operation succeeds.
   */
  @ReplacedByNativeOnDeploy
  public static boolean copyToRemote(String srcFile, String dstFile) {
    return false;
  }

  /**
   * Copies a file from the remote device to the desktop.
   * 
   * @param srcFile
   *           The file on the remote device.
   * @param dstFile
   *           The target file on the desktop. Note: this CANNOT be a folder name, but the target file name instead.
   * @return true if the operation succeeds.
   */
  @ReplacedByNativeOnDeploy
  public static boolean copyFromRemote(String srcFile, String dstFile) {
    return false;
  }

  /**
   * Deletes a remote file or an empty folder.
   * 
   * @return true if the operation succeeds.
   */
  @ReplacedByNativeOnDeploy
  public static boolean delete(String fileOrFolder) {
    return false;
  }

  /**
   * Remotely executes the specified command on the connected device. <b>Works only on WinCE devices.</b><br>
   * The arguments launchCode and wait are not used and their value is ignored. However, they may be used in the future
   * so the usage of default values is recommended.
   * 
   * @param command
   *           the command to execute
   * @param args
   *           command arguments
   * @param launchCode
   *           ignored, use 0.
   * @param wait
   *           ignored, use false.
   * @return 0 if successful, otherwise a system specific error code is returned.
   * 
   * @since TotalCross 1.22
   */
  @ReplacedByNativeOnDeploy
  public static int exec(String command, String args, int launchCode, boolean wait) {
    return -1;
  }
}
