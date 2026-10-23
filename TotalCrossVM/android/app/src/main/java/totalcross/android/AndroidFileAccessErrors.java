// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.android;

import android.system.ErrnoException;
import android.system.OsConstants;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.regex.Pattern;

/** Recognizes filesystem authorization failures without treating camera permissions as file access. */
public final class AndroidFileAccessErrors {
    private static final Pattern ACCESS_ERRNO = Pattern.compile("\\b(?:EACCES|EPERM)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern PERMISSION_DENIED = Pattern.compile("\\bpermission\\s+denied\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern CAMERA_OR_MIC_PERMISSION = Pattern.compile(
            "android\\.permission\\.(?:camera|record_audio)\\b"
                    + "|\\b(?:camera|microphone|record[ _]audio)\\s+(?:runtime\\s+)?permission\\b"
                    + "|\\bpermission\\s+(?:for|to)\\s+(?:use\\s+)?(?:the\\s+)?(?:camera|microphone|record[ _]audio)\\b"
                    + "|\\bpermission\\s+denied\\s+(?:for\\s+)?(?:the\\s+)?(?:camera|microphone|record[ _]audio)\\b",
            Pattern.CASE_INSENSITIVE);

    private AndroidFileAccessErrors() {
    }

    public static boolean isPermissionDenied(Throwable failure) {
        Set<Throwable> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Throwable current = failure; current != null && visited.add(current); current = current.getCause()) {
            if (current instanceof ErrnoException) {
                int errno = ((ErrnoException) current).errno;
                if (errno == OsConstants.EACCES || errno == OsConstants.EPERM) return true;
            }
            String message = current.getMessage();
            if (message == null) continue;
            if (ACCESS_ERRNO.matcher(message).find()) return true;
            if (PERMISSION_DENIED.matcher(message).find()
                    && !CAMERA_OR_MIC_PERMISSION.matcher(message).find()) return true;
        }
        return false;
    }
}
