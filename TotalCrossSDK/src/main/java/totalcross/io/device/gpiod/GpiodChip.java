/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2019-2020 TotalCross Global Mobile Platform Ltda.              *
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

package totalcross.io.device.gpiod;

import java.util.HashMap;
import java.util.Map;

import com.totalcross.annotations.ReplacedByNativeOnDeploy;

public class GpiodChip {

    Object handle;

    Map<Integer, GpiodLine> linesMap = new HashMap<>();

    private GpiodChip() {
    }

    @ReplacedByNativeOnDeploy
    public static GpiodChip open(int bank) {
        return null;
    }

    public GpiodLine line(int line) {
        GpiodLine gline = linesMap.get(line);
        if (gline == null) {
            gline = GpiodLine.open(this, line);
            linesMap.put(line, gline);
        }
        return gline;
    }
}
