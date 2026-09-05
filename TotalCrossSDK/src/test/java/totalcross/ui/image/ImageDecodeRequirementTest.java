// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

class ImageDecodeRequirementTest {
  @Test
  void choosesConservativeThresholdTiers() throws Exception {
    EncodedImageSource source = source(800, 600);
    ImagePipeline root = new ImagePipeline(source);
    int[][] cases = {
        {100, 75, 8},
        {101, 75, 4},
        {200, 150, 4},
        {201, 150, 2},
        {400, 300, 2},
        {401, 300, 1}
    };
    for (int[] testCase : cases) {
      assertEquals(testCase[2], ImageDecodeRequirement.choose(source, root, testCase[0], testCase[1]));
    }
  }

  @Test
  void accountsForCropAnisotropyAndPhysicalDensity() throws Exception {
    EncodedImageSource source = source(800, 600);
    ImagePipeline root = new ImagePipeline(source);
    ImagePipeline cropped = append(root, ImagePipeline.CROP, 100, 50, 200, 160, 200, 160);
    assertEquals(8, ImageDecodeRequirement.choose(source, cropped, 25, 20));

    ImagePipeline anisotropic = append(root, ImagePipeline.SMOOTH_SCALE, 0, 0, 0, 0, 100, 300);
    assertEquals(2, ImageDecodeRequirement.choose(source, anisotropic, 100, 300));

    assertEquals(8, ImageDecodeRequirement.choose(source, root, 100, 75));
    assertEquals(4, ImageDecodeRequirement.choose(source, root, 200, 150));
    assertEquals(2, ImageDecodeRequirement.choose(source, root, 300, 225));
  }

  @Test
  void rejectsAmbiguousCropAfterTransformAndRotation() throws Exception {
    EncodedImageSource source = source(800, 600);
    ImagePipeline root = new ImagePipeline(source);
    ImagePipeline smooth = append(root, ImagePipeline.SMOOTH_SCALE, 0, 0, 0, 0, 400, 300);
    ImagePipeline cropAfterSmooth = append(smooth, ImagePipeline.CROP, 0, 0, 100, 100, 100, 100);
    assertEquals(1, ImageDecodeRequirement.choose(source, cropAfterSmooth, 25, 25));

    ImagePipeline rotation = append(root, ImagePipeline.ROTATE_SCALE, 100, 45, 0, 0, 700, 700);
    assertEquals(1, ImageDecodeRequirement.choose(source, rotation, 100, 100));
  }

  private static EncodedImageSource source(int width, int height) throws Exception {
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    ImageIO.write(image, "jpeg", bytes);
    return EncodedImageSource.fromBytes(bytes.toByteArray());
  }

  private static ImagePipeline append(ImagePipeline previous, int operation, int parameter1, int parameter2,
      int parameter3, int parameter4, int width, int height) {
    return previous.append(operation, parameter1, parameter2, parameter3, parameter4,
        width, height, width, height, 1, width);
  }
}
