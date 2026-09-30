// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import totalcross.Launcher;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageDrawingBridge;

class DisplayPreparationTraversalTest {
  @BeforeAll
  static void initializeUi() {
    new Launcher();
  }

  @Test
  void traversesNestedContainersAndDeduplicatesImageControlBackground() throws Exception {
    Image shared = new Image(2, 2);
    Image background = new Image(2, 2);
    Container root = new Container();
    Container row = new Container();
    ImageControl first = new ImageControl(shared);
    first.setBackground(shared);
    ImageControl second = new ImageControl(shared);
    second.setBackground(background);
    row.add(first);
    row.add(second);
    root.add(row);

    ArrayList<Integer> requirements = new ArrayList<Integer>();
    ArrayList<Image> requests = collect(root, requirements);

    assertEquals(3, requests.size());
    assertEquals(ImageDrawingBridge.COPY_READY, requirements.get(0).intValue());
    assertEquals(ImageDrawingBridge.COPY_READY, requirements.get(1).intValue());
    assertEquals(ImageDrawingBridge.COPY_READY, requirements.get(2).intValue());
    assertSame(shared, requests.get(0));
    assertSame(shared, requests.get(1));
    assertSame(background, requests.get(2));
  }

  @Test
  void emptyContainerProducesNoPreparationRequests() {
    ArrayList<Image> requests = collect(new Container(), new ArrayList<Integer>());

    assertEquals(0, requests.size());
  }

  @Test
  void imageControlAllowsDrawReadyWhenItCanExceedBounds() throws Exception {
    ImageControl imageControl = new ImageControl(new Image(2, 2));
    imageControl.allowBeyondLimits = true;
    Container root = new Container();
    root.add(imageControl);
    ArrayList<Integer> requirements = new ArrayList<Integer>();

    collect(root, requirements);

    assertEquals(1, requirements.size());
    assertEquals(ImageDrawingBridge.DRAW_READY, requirements.get(0).intValue());
  }

  @Test
  void imageControlChangesInvalidateEveryAncestorPreparationGeneration() throws Exception {
    MainWindow.resetPreviewState();
    new MainWindow();
    try {
      ScrollContainer scroll = new ScrollContainer(false, false);
      ImageControl imageControl = new ImageControl(new Image(2, 2));
      scroll.add(imageControl);
      long generation = scroll.displayPreparationGenerationForTest();

      imageControl.setBackground(new Image(2, 2));
      long afterBackground = scroll.displayPreparationGenerationForTest();
      assertTrue(afterBackground > generation);

      imageControl.setImage(new Image(3, 3));
      long afterImage = scroll.displayPreparationGenerationForTest();
      assertTrue(afterImage > afterBackground);

      imageControl.scaleToFit = true;
      imageControl.setRect(0, 0, 8, 8);
      assertTrue(scroll.displayPreparationGenerationForTest() > afterImage);
    } finally {
      MainWindow.resetPreviewState();
    }
  }

  private static ArrayList<Image> collect(Container container, final ArrayList<Integer> requirements) {
    final ArrayList<Image> requests = new ArrayList<Image>();
    container.prepareForDisplay(new DisplayPreparationContext(1.5, 7),
        new DisplayPreparationSink() {
          @Override
          public void request(Image image, int requirement) {
            requests.add(image);
            requirements.add(Integer.valueOf(requirement));
          }
        });
    return requests;
  }
}
