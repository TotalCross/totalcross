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

package ras.comm.v1;

import totalcross.crypto.CryptoException;
import totalcross.crypto.signature.Signature;
import totalcross.io.ByteArrayStream;
import totalcross.io.DataStream;
import totalcross.io.IOException;
import totalcross.sys.Convert;

public class ActivationSuccess extends ActivationResponse {
  private int expireOn;
  private byte[] signature;

  public ActivationSuccess() {
  }

  public ActivationSuccess(ActivationRequest request, int expireOn) {
    super(request);
    this.expireOn = expireOn;
  }

  public int getExpireOn() {
    return expireOn;
  }

  public boolean verify(Signature engine) throws CryptoException {
    ByteArrayStream bas = new ByteArrayStream(128);
    DataStream ds = new DataStream(bas, true);

    try {
      request.write(ds);
      ds.writeInt(expireOn);
    } catch (IOException ex) {
    }

    engine.update(bas.toByteArray());
    return engine.verify(signature);
  }

  @Override
  protected void read(DataStream ds) throws IOException {
    super.read(ds);
    expireOn = ds.readInt();
    signature = Convert.hexStringToBytes(ds.readString());
  }

  @Override
  protected void write(DataStream ds) throws IOException {
    super.write(ds);
    ds.writeInt(expireOn);
    ds.writeString(Convert.bytesToHexString(signature));
  }
}
