// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.android;

import android.content.ContentResolver;
import android.net.Uri;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/** Copies provider content to a destination atomically while distinguishing source and destination failures. */
final class ContentUriCopier {
    private ContentUriCopier() {
    }

  static final class Result {
      final boolean succeeded;
      final boolean destinationDenied;
      final IOException failure;

      Result(boolean succeeded, boolean destinationDenied, IOException failure) {
          this.succeeded = succeeded;
          this.destinationDenied = destinationDenied;
          this.failure = failure;
      }
  }

  private static final class UriSourceFailure extends IOException {
      UriSourceFailure(Throwable cause) {
          super("Unable to read the selected image content.", cause);
      }
  }

  private static final class UriDestinationFailure extends IOException {
      UriDestinationFailure(Throwable cause) {
          super("Unable to write the selected image to a temporary file.", cause);
      }
  }
  

    static Result copy(ContentResolver resolver, Uri sourceUri, String destinationPath) {
        if (sourceUri == null || !"content".equalsIgnoreCase(sourceUri.getScheme())
                || destinationPath == null || destinationPath.trim().isEmpty()) {
            return new Result(false, false, new IOException("The selected image URI or destination is missing."));
        }
        InputStream input;
        try {
            input = resolver.openInputStream(sourceUri);
        } catch (IOException | RuntimeException sourceFailure) {
            return new Result(false, false, new IOException("Unable to read the selected image URI.", sourceFailure));
        }
        if (input == null) {
            return new Result(false, false, new IOException("The selected image provider returned no stream."));
        }

        File destination;
        File parent;
        try {
            destination = new File(destinationPath).getAbsoluteFile();
            parent = destination.getParentFile();
            if (parent == null) {
                IOException closeFailure = closeSource(input);
                return new Result(false, false, closeFailure == null
                        ? new IOException("The selected image destination directory is unavailable.") : closeFailure);
            }
        } catch (SecurityException destinationFailure) {
            IOException closeFailure = closeSource(input);
            return closeFailure == null
                    ? new Result(false, true, null)
                    : new Result(false, false, closeFailure);
        }

        File temporaryFile;
        try {
            temporaryFile = File.createTempFile(".totalcross-image-", ".tmp", parent);
        } catch (SecurityException destinationFailure) {
            IOException closeFailure = closeSource(input);
            return closeFailure == null
                    ? new Result(false, true, null)
                    : new Result(false, false, closeFailure);
        } catch (IOException destinationFailure) {
            IOException closeFailure = closeSource(input);
            if (closeFailure != null) return new Result(false, false, closeFailure);
            return AndroidFileAccessErrors.isPermissionDenied(destinationFailure)
                    ? new Result(false, true, null)
                    : new Result(false, false, destinationFailure);
        }

        IOException failure = null;
        boolean destinationDenied = false;
        try {
            try (InputStream source = input) {
                try {
                    try (OutputStream output = new FileOutputStream(temporaryFile)) {
                        byte[] buffer = new byte[8192];
                        while (true) {
                            int count;
                            try {
                                count = source.read(buffer);
                            } catch (RuntimeException | IOException sourceFailure) {
                                throw new UriSourceFailure(sourceFailure);
                            }
                            if (count < 0) break;
                            try {
                                output.write(buffer, 0, count);
                            } catch (RuntimeException | IOException destinationFailure) {
                                throw new UriDestinationFailure(destinationFailure);
                            }
                        }
                        try {
                            output.flush();
                        } catch (RuntimeException | IOException destinationFailure) {
                            throw new UriDestinationFailure(destinationFailure);
                        }
                    }
                } catch (UriSourceFailure sourceFailure) {
                    failure = new IOException(sourceFailure.getMessage(), sourceFailure.getCause());
                } catch (UriDestinationFailure destinationFailure) {
                    Throwable cause = destinationFailure.getCause();
                    destinationDenied = cause instanceof SecurityException
                            || AndroidFileAccessErrors.isPermissionDenied(cause);
                    if (!destinationDenied) failure = destinationFailure;
                } catch (SecurityException destinationFailure) {
                    destinationDenied = true;
                } catch (IOException destinationFailure) {
                    destinationDenied = AndroidFileAccessErrors.isPermissionDenied(destinationFailure);
                    if (!destinationDenied) failure = destinationFailure;
                } catch (RuntimeException destinationFailure) {
                    failure = new IOException("Unable to write the selected image temporary file.", destinationFailure);
                }
            }
        } catch (IOException | RuntimeException sourceCloseFailure) {
            failure = new IOException("Unable to close the selected image content stream.", sourceCloseFailure);
            destinationDenied = false;
        }

        try {
            if (failure == null && !destinationDenied) {
                if (temporaryFile.length() == 0) {
                    failure = new IOException("The image provider returned an empty image.");
                }
            }

            if (failure == null && !destinationDenied) {
                Os.rename(temporaryFile.getAbsolutePath(), destination.getAbsolutePath());
                temporaryFile = null;
            }
        } catch (ErrnoException destinationFailure) {
            destinationDenied = destinationFailure.errno == OsConstants.EACCES
                    || destinationFailure.errno == OsConstants.EPERM;
            if (!destinationDenied) {
                failure = new IOException("Unable to replace the selected image destination.", destinationFailure);
            }
        } catch (SecurityException destinationFailure) {
            destinationDenied = true;
        } catch (RuntimeException destinationFailure) {
            failure = new IOException("Unable to replace the selected image destination.", destinationFailure);
        } finally {
            if (temporaryFile != null) {
                try {
                    if (temporaryFile.exists() && !temporaryFile.delete() && temporaryFile.exists()) {
                        if (!destinationDenied && failure == null) {
                            failure = new IOException("Unable to remove the temporary image file.");
                        }
                    }
                } catch (SecurityException cleanupFailure) {
                    if (!destinationDenied && failure == null) {
                        destinationDenied = true;
                    }
                } catch (RuntimeException cleanupFailure) {
                    if (!destinationDenied && failure == null) {
                        failure = new IOException("Unable to remove the temporary image file.", cleanupFailure);
                    }
                }
            }
        }
        return new Result(!destinationDenied && failure == null, destinationDenied, failure);
    }

    private static IOException closeSource(InputStream input) {
        try {
            input.close();
            return null;
        } catch (IOException | RuntimeException failure) {
            return new IOException("Unable to close the selected image content stream.", failure);
        }
    }


}
