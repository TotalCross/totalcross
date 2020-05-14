// Copyright (C) 2018-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.crypto.provider;

import javax.crypto.interfaces.PBEKey;
import javax.crypto.spec.PBEKeySpec;

public class PBKDF2SecretKey implements PBEKey {

    private final PBEKeySpec keySpec;
    
    private final byte[] encoded;

    PBKDF2SecretKey(PBEKeySpec keySpec, byte[] encoded) {
        this.keySpec = keySpec;
        this.encoded = encoded;
    }

    @Override
    public String getAlgorithm() {
        return "PBKDF2WithHmacSHA1";
    }

    @Override
    public String getFormat() {
        return "RAW";
    }

    @Override
    public byte[] getEncoded() {
        return encoded;
    }

    @Override
    public int getIterationCount() {
        return keySpec.getIterationCount();
    }

    @Override
    public char[] getPassword() {
        return keySpec.getPassword();
    }

    @Override
    public byte[] getSalt() {
        return keySpec.getSalt();
    }

}
