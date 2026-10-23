// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross;

import java.io.File;

import org.junit.Test;

import static org.junit.Assert.*;

public class ViewerFilePathTest
{
   @Test
   public void acceptsLocalPathsAndDecodesFileUris()
   {
      assertEquals(new File("/sdcard/My report.docx"),
          ViewerFilePath.fromArgument("/sdcard/My report.docx"));
      assertEquals(new File("relative/report.xlsx"),
          ViewerFilePath.fromArgument("relative/report.xlsx"));
      assertEquals(new File("/sdcard/My report#final.csv"),
          ViewerFilePath.fromArgument("file:///sdcard/My%20report%23final.csv"));
      assertEquals(new File("/sdcard/My report.pdf"),
          ViewerFilePath.fromArgument("file:///sdcard/My report.pdf"));
      assertEquals(new File("/sdcard/report.pdf"),
          ViewerFilePath.fromArgument("file://localhost/sdcard/report.pdf"));
   }

   @Test
   public void doesNotTreatWebAndContentUrisAsFiles()
   {
      assertNull(ViewerFilePath.fromArgument(null));
      assertNull(ViewerFilePath.fromArgument(""));
      assertNull(ViewerFilePath.fromArgument("https://example.com/a.pdf"));
      assertNull(ViewerFilePath.fromArgument("content://provider/document/123"));
      assertNull(ViewerFilePath.fromArgument("about:blank"));
      assertNull(ViewerFilePath.fromArgument("www.example.com/page"));
      assertNull(ViewerFilePath.fromArgument("youtube.com/watch?v=123"));
      assertNull(ViewerFilePath.fromArgument("youtu.be/abc"));
      assertNull(ViewerFilePath.fromArgument("//example.com/page"));
      assertNull(ViewerFilePath.fromArgument("file://example.com/sdcard/a.pdf"));
      assertNull(ViewerFilePath.fromArgument("file:relative.pdf"));
      assertNull(ViewerFilePath.fromArgument("file:///sdcard/a.pdf?query=1"));
   }

   @Test
   public void keepsWebViewerFormatsAndIdentifiesDocumentExtensions()
   {
      assertTrue(ViewerFilePath.isWebViewerFile("index.HTML"));
      assertTrue(ViewerFilePath.isWebViewerFile("notes.txt"));
      assertTrue(ViewerFilePath.isWebViewerFile("data.xml"));
      assertFalse(ViewerFilePath.isWebViewerFile("report.pdf"));
      assertFalse(ViewerFilePath.isWebViewerFile("report.docx"));
      assertFalse(ViewerFilePath.isWebViewerFile("data.csv"));
      assertEquals("xlsx", ViewerFilePath.extension("My report.XLSX"));
      assertNull(ViewerFilePath.extension("no-extension"));
   }
}
