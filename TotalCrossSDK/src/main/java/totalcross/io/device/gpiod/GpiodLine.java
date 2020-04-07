/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2019-2020 TotalCross Global Mobile Platform Ltda.              *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 2.1    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-2.1.txt                                     *
 *                                                                               *
 *********************************************************************************/

package totalcross.io.device.gpiod;

import com.totalcross.annotations.ReplacedByNativeOnDeploy;

public class GpiodLine {

    Object handle;

    private GpiodLine() {
    }

    @ReplacedByNativeOnDeploy
    static GpiodLine open(GpiodChip chip, int line) {
        return null;
    }

    @ReplacedByNativeOnDeploy
    public int requestOutput(String consumer, int defaultValue) {
        return -1;
    }
    
    @ReplacedByNativeOnDeploy
    public int setValue(int value) {
        return -1;
    }

    @ReplacedByNativeOnDeploy
    public int requestInput(String consumer) {
        return -1;
    }
    
    @ReplacedByNativeOnDeploy
    public int getValue() {
        return -1;
    }
}
