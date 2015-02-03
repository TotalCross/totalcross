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

import tc.samples.api.*;

import totalcross.crypto.*;
import totalcross.crypto.cipher.*;
import totalcross.crypto.digest.*;
import totalcross.crypto.signature.*;
import totalcross.sys.*;
import totalcross.ui.*;
import totalcross.ui.event.*;

public class SignatureSample extends BaseContainer
{
   private Object[] signatures;
   private Key[] sigKeys;
   private Key[] verKeys;
   
   private Edit edtInput;
   private ComboBox cboSignatures;
   private Button btnGo;
   
   private static final byte[] RSA_N = new byte[]
   {
      (byte)0, (byte)-60, (byte)-106, (byte)-118, (byte)-19, (byte)57, (byte)-63, (byte)-18,
      (byte)102, (byte)111, (byte)-56, (byte)1, (byte)50, (byte)-101, (byte)-90, (byte)-85,
      (byte)-96, (byte)-66, (byte)-70, (byte)-49, (byte)-52, (byte)-3, (byte)70, (byte)-120,
      (byte)63, (byte)-76, (byte)-34, (byte)-114, (byte)13, (byte)8, (byte)45, (byte)-124,
      (byte)-12, (byte)-6, (byte)87, (byte)90, (byte)61, (byte)-124, (byte)-42, (byte)34,
      (byte)21, (byte)14, (byte)-73, (byte)21, (byte)-104, (byte)70, (byte)11, (byte)-59,
      (byte)58, (byte)-72, (byte)-55, (byte)-98, (byte)68, (byte)123, (byte)-63, (byte)-11,
      (byte)-7, (byte)-115, (byte)32, (byte)57, (byte)-38, (byte)-41, (byte)-9, (byte)-108,
      (byte)79
   };
   
   private static final byte[] RSA_D = new byte[]
   {
      (byte)122, (byte)-69, (byte)13, (byte)-94, (byte)-54, (byte)-61, (byte)67, (byte)37,
      (byte)-38, (byte)-75, (byte)127, (byte)-31, (byte)-21, (byte)-128, (byte)-29, (byte)119,
      (byte)104, (byte)123, (byte)-46, (byte)-115, (byte)-60, (byte)-75, (byte)-53, (byte)12,
      (byte)18, (byte)-52, (byte)58, (byte)-36, (byte)-15, (byte)-11, (byte)17, (byte)34,
      (byte)-109, (byte)-121, (byte)5, (byte)117, (byte)109, (byte)-72, (byte)-27, (byte)-103,
      (byte)-85, (byte)-1, (byte)37, (byte)-30, (byte)38, (byte)-86, (byte)88, (byte)-28,
      (byte)-26, (byte)-102, (byte)-10, (byte)124, (byte)-97, (byte)-18, (byte)-118, (byte)2,
      (byte)36, (byte)40, (byte)-47, (byte)-75, (byte)-44, (byte)69, (byte)10, (byte)1
   };
   
   private static final byte[] RSA_E = new byte[]
   {
      (byte)1, (byte)0, (byte)1
   };
   
   public void initUI()
   {
      super.initUI();
      signatures = new Object[3];
      try
      {
         signatures[0] = new PKCS1Signature(new MD5Digest());
         signatures[1] = new PKCS1Signature(new SHA1Digest());
         signatures[2] = new PKCS1Signature(new SHA256Digest());
      }
      catch (CryptoException e)
      {
         throw new RuntimeException(e.getMessage());
      }
      
      verKeys = new Key[3];
      verKeys[0] = verKeys[1] = verKeys[2] = new RSAPublicKey(RSA_E, RSA_N);
      
      sigKeys = new Key[3];
      sigKeys[0] = sigKeys[1] = sigKeys[2] = new RSAPrivateKey(RSA_E, RSA_D, RSA_N);
      
      edtInput = new Edit();
      edtInput.setText("0123456789ABCDEF");
      
      cboSignatures = new ComboBox(signatures);
      cboSignatures.setSelectedIndex(0);
      
      btnGo = new Button(" Go! ");
      
      add(edtInput, LEFT + 2, TOP + fmH/4, FILL - 2, PREFERRED);
      add(cboSignatures, LEFT + 2, AFTER + fmH/4, PREFERRED, PREFERRED);
      add(btnGo, AFTER + 2, SAME, PREFERRED, PREFERRED);
      addLog(LEFT + 2, AFTER + fmH/4, FILL - 2, FILL - 2,null);
   }
   
   public void onEvent(Event e)
   {
      switch (e.type)
      {
         case ControlEvent.PRESSED:
            if (e.target == btnGo)
            {
               int index = cboSignatures.getSelectedIndex();
               String message = edtInput.getText();
               byte[] data = message.getBytes();
               
               Signature signer = (Signature)signatures[index];
               try
               {
                  log("Message: '" + message + "'");
                  signer.reset(Signature.OPERATION_SIGN, sigKeys[index]);
                  signer.update(data);
                  byte[] signature = signer.sign();
                  
                  log("Signature: " + Convert.bytesToHexString(signature) + " (" + signature.length + " bytes)");
                  
                  signer.reset(Signature.OPERATION_VERIFY, verKeys[index]);
                  signer.update(data);
                  
                  log(signer.verify(signature) ? "Signature verified!" : "Invalid signature!");
               }
               catch (CryptoException ex)
               {
                  log("Exception: " + ex.toString());
               }
               log("=========================");
            }
            break;
      }
   }
}
