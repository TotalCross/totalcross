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

package totalcross.ui.dialog;

import totalcross.io.File;
import totalcross.io.IOException;
import totalcross.net.URI;
import totalcross.sys.Settings;

public class FilePicker {

    public URI present() {
        if (Settings.isIOS() || Settings.ANDROID.equals(Settings.platform)) {
            return nativePresent();
        }
        FileChooserBox fcb = new FileChooserBox(new FileChooserBox.Filter() {

            @Override
            public boolean accept(File f) throws IOException {
                return true;
            }

        });
        fcb.popup();
        return new URI("file://" + fcb.getAnswer());
    }

    public FilePicker() {

    }

    native private URI nativePresent();
}
