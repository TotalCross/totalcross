// Copyright (C) 2019-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

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
