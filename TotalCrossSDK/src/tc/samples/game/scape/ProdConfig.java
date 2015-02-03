/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
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



package tc.samples.game.scape;

/**
 * ProdConfig Production configuration.
 *
 * See <a href='http://www.javaworld.com/javaworld/javatips/jw-javatip5.html'>this</a>.
 */

interface ProdConfig
{
   static final String PC_VERSION = "0.9";
   static final String PC_CREATOR_ID = "Scpe";

   static final boolean PC_COLOR = true;
   static final boolean PC_WAVES = true;

   final static boolean NEVER_LOSE = false;
}
