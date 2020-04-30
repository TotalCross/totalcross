// Copyright (C) 2018-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.crypto.provider;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import com.totalcross.annotations.ReplacedByNativeOnDeploy;

public class PBKDF2WithHmacSHA1Factory //extends SecretKeyFactorySpi4D 
{
    public static SecretKey generateSecret(KeySpec keySpec) throws InvalidKeySpecException {
        return new PBKDF2WithHmacSHA1Factory().engineGenerateSecret(keySpec);
    }
    
//    @Override
    protected SecretKey engineGenerateSecret(KeySpec keySpec) throws InvalidKeySpecException {
        PBEKeySpec ks = (PBEKeySpec) keySpec;
        PBKDF2SecretKey secretKey = new PBKDF2SecretKey(ks, generateSecretImpl(ks.getPassword(), ks.getSalt(), ks.getIterationCount(), ks.getKeyLength()));
        return secretKey;
    }
    
    @ReplacedByNativeOnDeploy
    private byte[] generateSecretImpl(char[] password, byte[] salt, int iterations, int keyLength)
            throws InvalidKeySpecException {
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
            SecretKey key = skf.generateSecret(spec);
            return key.getEncoded();
        } catch (NoSuchAlgorithmException e) {
            throw new InvalidKeySpecException(e);
        }
    }

//    @Override
//    protected KeySpec engineGetKeySpec(SecretKey key, Class<?> keySpec) throws InvalidKeySpecException {
//        // TODO Auto-generated method stub
//        return null;
//    }
//
//    @Override
//    protected SecretKey engineTranslateKey(SecretKey key) throws InvalidKeyException {
//        // TODO Auto-generated method stub
//        return null;
//    }

}
