package jdkcompatx.crypto;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import totalcross.crypto.provider.PBKDF2WithHmacSHA1Factory;
import totalcross.util.concurrent.Lock;

public class SecretKeyFactory4D {
    
    private static Map<String, SecretKeyFactory4D> services = new HashMap<>();
    
    private static final Lock lock = new Lock();

    // Store used provider
    private final Provider provider;

    // Store used spi implementation
    private final SecretKeyFactorySpi4D spiImpl;

    // Store used algorithm name
    private final String algorithm;
    
    /**
     * Creates a new {@code SecretKeyFactory}
     *
     * @param keyFacSpi
     *            the SPI delegate.
     * @param provider
     *            the provider providing this key factory.
     * @param algorithm
     *            the algorithm name for the secret key.
     */
    protected SecretKeyFactory4D(SecretKeyFactorySpi4D keyFacSpi,
            Provider provider, String algorithm) {
        this.provider = provider;
        this.algorithm = algorithm;
        this.spiImpl = keyFacSpi;
    }
    
    public static final SecretKeyFactory4D getInstance(String algorithm)
            throws NoSuchAlgorithmException {
        if (algorithm == null) {
            throw new NullPointerException(
//                    Messages.getString("crypto.02")
                    ); //$NON-NLS-1$
        }
        
        synchronized (lock) {
            SecretKeyFactory4D service = services.get(algorithm);
            if (service != null) {
                return service;
            } else if ("PBKDF2WithHmacSHA1".equals(algorithm)) {
//                service = new SecretKeyFactory4D(new PBKDF2WithHmacSHA1Factory(), null, algorithm);
//                services.put(algorithm, service);
            }
            
            return service;
        }
    }
    
    /**
     * Generate a secret key from the specified key specification.
     *
     * @param keySpec
     *            the key specification.
     * @return a secret key.
     * @throws InvalidKeySpecException
     *             if the specified key specification cannot be used to generate
     *             a secret key.
     */
    public final SecretKey generateSecret(KeySpec keySpec)
            throws InvalidKeySpecException {
        return spiImpl.engineGenerateSecret(keySpec);
    }

    /**
     * Returns the key specification of the specified secret key.
     *
     * @param key
     *            the secret key to get the specification from.
     * @param keySpec
     *            the target key specification class.
     * @return an instance of the specified key specification class.
     * @throws InvalidKeySpecException
     *             if the specified secret key cannot be transformed into the
     *             requested key specification.
     */
    public final KeySpec getKeySpec(SecretKey key, Class<?> keySpec)
            throws InvalidKeySpecException {
        return spiImpl.engineGetKeySpec(key, keySpec);
    }

    /**
     * Translates the specified secret key into an instance of the corresponding
     * key from the provider of this key factory.
     *
     * @param key
     *            the secret key to translate.
     * @return the corresponding translated key.
     * @throws InvalidKeyException
     *             if the specified key cannot be translated using this key
     *             factory.
     */
    public final SecretKey translateKey(SecretKey key)
            throws InvalidKeyException {
        return spiImpl.engineTranslateKey(key);

    }
}
