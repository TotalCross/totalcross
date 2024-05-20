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

package totalcross.net.ssl;

import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import totalcross.sys.Settings;

public class SSLContext {

    private static final Map<String, Class<? extends SSLContextSpi>> map = new HashMap<>();

    private static final Class<? extends SSLContextSpi> DEFAULT_PROVIDER;

    protected final SSLContextSpi sslContextSpi;

    static {
        map.put("base", SSLContextBase.class);
        if (!Settings.onJavaSE && !Settings.isWindowsCE()) {
            map.put("mbedtls", SSLContextMbedtls.class);
            DEFAULT_PROVIDER = map.get("mbedtls");
        } else {
            DEFAULT_PROVIDER = map.get("base");
        }
    }

    protected SSLContext(Class<? extends SSLContextSpi> sslContextSpi) throws NoSuchAlgorithmException {
        try {
            this.sslContextSpi = sslContextSpi.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            throw new NoSuchAlgorithmException(e);
        }
    }

    public static SSLContext getDefault() throws NoSuchAlgorithmException {
        return new SSLContext(DEFAULT_PROVIDER);
    }

    public static SSLContext getInstance(String protocol) throws NoSuchAlgorithmException {
        // check if protocol is supported
        return new SSLContext(DEFAULT_PROVIDER);
    }

    public static SSLContext getInstance(String protocol, String provider) throws NoSuchAlgorithmException {
        Class<? extends SSLContextSpi> spi = map.get(provider);
        // check if protocol is supported
        return new SSLContext(spi);
    }

    public SSLSocketFactory getSocketFactory() {
        return sslContextSpi.getSocketFactory();
    }
}
