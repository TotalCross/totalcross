/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2012 SuperWaba Ltda.                                      *
 *  Copyright (C) 2012-2020 TotalCross Global Mobile Platform Ltda.              *
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

package tc.tools.deployer.ipa.blob;

public class BlobIndex {
  public static final int CSSLOT_CODEDIRECTORY = 0;
  public static final int CSSLOT_REQUIREMENTS = 2;
  public static final int CSSLOT_ENTITLEMENTS = 5;
  public static final int CSSLOT_SIGNATURE = 0x10000;

  public long blobType;
  public long offset;
  public BlobCore blob;

  BlobIndex(long blobType, BlobCore blob) {
    this.blobType = blobType;
    this.blob = blob;
  }
}
