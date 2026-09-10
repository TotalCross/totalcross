// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

import totalcross.Launcher;
import totalcross.ui.ImageControl;
import totalcross.ui.MainWindow;
import totalcross.ui.ScrollContainer;

class ScrollContainerPreparationTest {
  @Test
  void replacingImageInvalidatesStaleBatchBeforeItCompletes() throws Exception {
    new Launcher();
    MainWindow.resetPreviewState();
    new MainWindow();
    ImagePreparation.setRunUiInlineForTest(true);
    ScrollContainer scroll = new ScrollContainer(false, false);
    ImageControl control = new ImageControl(new Image(jpeg(1024, 768)).getSmoothScaledInstance(256, 192));
    scroll.add(control);
    MainWindow.resetPreviewState();

    CountDownLatch adoptionStarted = new CountDownLatch(1);
    CountDownLatch releaseAdoption = new CountDownLatch(1);
    try {
      ImagePreparation.setBeforeAdoptionHookForTest(new Runnable() {
        @Override
        public void run() {
          adoptionStarted.countDown();
          try {
            releaseAdoption.await(5, TimeUnit.SECONDS);
          } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
          }
        }
      });
      CountDownLatch staleCompletion = new CountDownLatch(1);
      scroll.prepareForDisplay(staleCompletion::countDown);
      assertTrue(adoptionStarted.await(5, TimeUnit.SECONDS));

      control.setImage(new Image(jpeg(1024, 768)).getSmoothScaledInstance(256, 192));
      CountDownLatch currentCompletion = new CountDownLatch(1);
      scroll.prepareForDisplay(currentCompletion::countDown);
      releaseAdoption.countDown();

      assertTrue(currentCompletion.await(5, TimeUnit.SECONDS));
      assertFalse(staleCompletion.await(200, TimeUnit.MILLISECONDS));
      assertEquals(0, ImagePreparation.activeEntryCountForTest());
    } finally {
      ImagePreparation.setBeforeAdoptionHookForTest(null);
      ImagePreparation.setRunUiInlineForTest(false);
    }
  }

  private static byte[] jpeg(int width, int height) throws Exception {
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    ImageIO.write(image, "jpeg", bytes);
    return bytes.toByteArray();
  }
}
