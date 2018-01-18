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

package tc.samples.api.crypto;

import tc.samples.api.BaseContainer;
import totalcross.crypto.NoSuchAlgorithmException;
import totalcross.crypto.digest.Digest;
import totalcross.crypto.digest.MD5Digest;
import totalcross.crypto.digest.SHA1Digest;
import totalcross.crypto.digest.SHA256Digest;
import totalcross.sys.Convert;
import totalcross.ui.Button;
import totalcross.ui.ComboBox;
import totalcross.ui.Edit;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;

public class DigestSample extends BaseContainer {
  private Edit edtInput;
  private ComboBox cboDigests;
  private Button btnGo;
  private Object[] comboItems;

  public DigestSample() {
    try {
      comboItems = new Object[] { new MD5Digest(), new SHA1Digest(), new SHA256Digest() };
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void initUI() {
    super.initUI();
    edtInput = new Edit();
    edtInput.setText("0123456789ABCDEF");
    cboDigests = new ComboBox(comboItems);
    cboDigests.setSelectedIndex(0);
    btnGo = new Button(" Go! ");

    add(edtInput, LEFT + 2, TOP + fmH / 4, FILL - (btnGo.getPreferredWidth() + cboDigests.getPreferredWidth() + 6),
        PREFERRED);
    add(cboDigests, AFTER + 2, SAME, PREFERRED, PREFERRED);
    add(btnGo, AFTER + 2, SAME, PREFERRED, PREFERRED);
    addLog(LEFT + 2, AFTER + 2, FILL - 2, FILL - 2, null);
  }

  @Override
  public void onEvent(Event e) {
    switch (e.type) {
    case ControlEvent.PRESSED:
      if (e.target == btnGo) {
        Digest alg = (Digest) cboDigests.getSelectedItem();
        String message = edtInput.getText();

        alg.reset();
        alg.update(message.getBytes());
        byte[] digest = alg.getDigest();

        log("Message: " + message);
        log("Digest: " + Convert.bytesToHexString(digest) + " (" + digest.length + " bytes)");
        log("=========================");
      }
      break;
    }
  }
}
