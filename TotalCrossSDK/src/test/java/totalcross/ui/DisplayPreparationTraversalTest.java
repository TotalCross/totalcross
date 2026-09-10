// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import totalcross.Launcher;
import totalcross.ui.image.Image;

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

    ArrayList<Image> requests = collect(root);

    assertEquals(3, requests.size());
    assertSame(shared, requests.get(0));
    assertSame(shared, requests.get(1));
    assertSame(background, requests.get(2));
  }

  @Test
  void emptyContainerProducesNoPreparationRequests() {
    ArrayList<Image> requests = collect(new Container());

    assertEquals(0, requests.size());
  }

  private static ArrayList<Image> collect(Container container) {
    final ArrayList<Image> requests = new ArrayList<Image>();
    container.prepareForDisplay(new DisplayPreparationContext(1.5, 7),
        new DisplayPreparationSink() {
          @Override
          public void request(Image image) {
            requests.add(image);
          }
        });
    return requests;
  }
}
