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

// $Id: DigestTest.java,v 1.4 2011-01-04 13:19:28 guich Exp $

package tc.samples.crypto.digest;

import totalcross.crypto.digest.Digest;
import totalcross.crypto.digest.MD5Digest;
import totalcross.crypto.digest.SHA1Digest;
import totalcross.crypto.digest.SHA256Digest;
import totalcross.sys.Convert;
import totalcross.ui.Button;
import totalcross.ui.ComboBox;
import totalcross.ui.Edit;
import totalcross.ui.ListBox;
import totalcross.ui.MainWindow;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;

public class DigestTest extends MainWindow
{
   private Edit edtInput;
   private ComboBox cboDigests;
   private Button btnGo;
   private ListBox lboResults;
   
   public DigestTest()
   {
      super("Digest Test", TAB_ONLY_BORDER);
   }
   
   public void initUI()
   {
      edtInput = new Edit();
      edtInput.setText("0123456789ABCDEF");
      cboDigests = new ComboBox(new Object[] {new MD5Digest(), new SHA1Digest(), new SHA256Digest()});
      cboDigests.setSelectedIndex(0);
      btnGo = new Button("Go!");
      lboResults = new ListBox();
      lboResults.enableHorizontalScroll();
      
      add(edtInput, LEFT + 2, TOP + 2, FILL - (btnGo.getPreferredWidth() + cboDigests.getPreferredWidth() + 6), PREFERRED);
      add(cboDigests, AFTER + 2, SAME, PREFERRED, PREFERRED);
      add(btnGo, AFTER + 2, SAME, PREFERRED, PREFERRED);
      add(lboResults, LEFT + 2, AFTER + 2, FILL - 2, FILL - 2);
   }
   
   public void onEvent(Event e)
   {
      switch (e.type)
      {
         case ControlEvent.PRESSED:
            if (e.target == btnGo)
            {
               Digest alg = (Digest)cboDigests.getSelectedItem();
               String message = edtInput.getText();
               
               alg.reset();
               alg.update(message.getBytes());
               byte[] digest = alg.getDigest();
               
               lboResults.add("Message: " + message);
               lboResults.add("Digest: " + Convert.bytesToHexString(digest) + " (" + digest.length + " bytes)");
               lboResults.add("=========================");
               lboResults.repaintNow();
            }
            break;
      }
   }
}
