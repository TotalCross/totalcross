// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

/** Android-only helpers for the file arguments accepted by Vm.exec("viewer"). */
public final class ViewerFilePath
{
   private ViewerFilePath()
   {
   }

   /** Returns null for URLs, content URIs, and other non-file arguments. */
   public static File fromArgument(String argument)
   {
      if (argument == null || argument.isEmpty())
         return null;

      if (argument.regionMatches(true, 0, "file:", 0, 5))
      {
         try
         {
            // Accept legacy file:// arguments containing literal spaces.
            URI uri = new URI(argument.replace(" ", "%20"));
            String authority = uri.getAuthority();
            if (!"file".equalsIgnoreCase(uri.getScheme())
                || (authority != null && !authority.isEmpty()
                    && !"localhost".equalsIgnoreCase(authority))
                || uri.getRawQuery() != null || uri.getRawFragment() != null)
               return null;

            String path = uri.getPath();
            return path != null && path.startsWith("/") ? new File(path) : null;
         }
         catch (URISyntaxException e)
         {
            return null;
         }
      }

      // Retain the historical handling of web locations, even without http://.
      if (argument.startsWith("//")
          || argument.regionMatches(true, 0, "www.", 0, 4)
          || argument.regionMatches(true, 0, "youtube.com", 0, 11)
          || argument.regionMatches(true, 0, "youtu.be", 0, 8))
         return null;

      int colon = argument.indexOf(':');
      if (colon > 0 && Character.isLetter(argument.charAt(0)))
      {
         boolean scheme = true;
         for (int i = 1; i < colon; i++)
         {
            char c = argument.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != '+' && c != '-' && c != '.')
            {
               scheme = false;
               break;
            }
         }
         if (scheme)
            return null;
      }

      return new File(argument);
   }

   /** Files already handled by the built-in Android WebViewer. */
   public static boolean isWebViewerFile(String name)
   {
      String extension = extension(name);
      return "htm".equals(extension) || "html".equals(extension)
          || "xhtml".equals(extension) || "txt".equals(extension)
          || "xml".equals(extension);
   }

   public static String extension(String name)
   {
      if (name == null)
         return null;

      int dot = name.lastIndexOf('.');
      return dot > 0 && dot < name.length() - 1
          ? name.substring(dot + 1).toLowerCase(Locale.ROOT) : null;
   }
}
