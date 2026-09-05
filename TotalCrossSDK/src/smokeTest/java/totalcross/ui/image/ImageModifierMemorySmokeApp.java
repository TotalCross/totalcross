// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.sys.Vm;
import totalcross.ui.MainWindow;
import totalcross.ui.gfx.Graphics;

/** macOS-only diagnosis smoke for the updated ImageModifiersSample workload. */
public class ImageModifierMemorySmokeApp extends MainWindow {
  private static final int EVENTS = 4000;
  private static final int NATIVE_LIVE_BOUND = 2;
  private static final String DEFAULT_SOURCE = "image-modifier-memory/lenna.png";

  @Override
  public void initUI() {
    Image root = null;
    Image leaf = null;
    boolean sourceDecode = false;
    boolean directDraw = false;
    boolean noMaterialization = false;
    boolean boundedNativeLive = false;
    String error = "";
    String fixture = fixtureFromCommandLine();
    int fixtureWidth = -1;
    int fixtureHeight = -1;
    int events = 0;

    try {
      Image.resetImageOperationAccountingForTest();
      byte[] encoded = Vm.getFile(fixture);
      require(encoded != null && encoded.length > 0, "encoded fixture");
      root = new Image(encoded);
      fixtureWidth = root.getPixelWidth();
      fixtureHeight = root.getPixelHeight();
      Graphics surface = getGraphics();
      require(surface != null, "main window graphics");

      for (int event = 1; event <= EVENTS; event++) {
        events = event;
        int sequence = (event - 1) / 2;
        int angle = -180 + ((sequence * 37) % 361);
        int scale = 1 + ((sequence * 53) % 400);
        byte brightness = (byte) (-128 + ((sequence * 17) & 255));
        byte contrast = (byte) (-128 + ((sequence * 29) & 255));
        if ((event & 1) == 0) {
          leaf = root.getRotatedScaledInstance(scale, angle, 0)
              .getTouchedUpInstance(brightness, contrast);
        } else {
          leaf = root.getTouchedUpInstance(brightness, contrast)
              .getRotatedScaledInstance(scale, angle, 0);
        }
        surface.drawImage(leaf, 0, 0, true);

        if (events == 100 || events == 500 || events == 1000 || events == 2000
            || events == 3000 || events == 4000) {
          checkpoint("events-" + events, events);
        }
      }

      checkpoint("after-stress", events);
      sourceDecode = Image.fullDecodeInvocationCountForTest() == 1
          && Image.targetedDecodeInvocationCountForTest() == 0;
      directDraw = Image.directDrawPlanExecutionCountForTest() >= EVENTS;
      noMaterialization = Image.nativeGeometryMaterializationCountForTest() == 0
          && Image.nativeColorReadbackCountForTest() == 0
          && Image.backingReadbackCountForTest() == 0;
      boundedNativeLive = NativeImageBacking.backingRecordsPeakLiveForTest() <= NATIVE_LIVE_BOUND;
      require(sourceDecode, "one reusable full decode");
      require(directDraw, "direct draw path");
      require(noMaterialization, "slider loop materialization/readback counters");
      require(boundedNativeLive, "bounded native live backings");

      leaf = null;
      checkpoint("before-leaf-gc", events);
      Vm.gc();
      Vm.sleep(300);
      checkpoint("after-leaf-gc-1", events);
      Vm.gc();
      Vm.sleep(300);
      checkpoint("after-leaf-gc-2", events);

      root = null;
      Vm.gc();
      Vm.sleep(300);
      checkpoint("after-root-gc", events);
    } catch (Throwable failure) {
      error = failure.getClass().getName() + ":" + String.valueOf(failure.getMessage()).replace(' ', '_');
    }

    boolean overallPass = events == EVENTS && sourceDecode && directDraw && noMaterialization
        && boundedNativeLive;
    String fixtureFormat = fixture.toLowerCase().endsWith(".png") ? "PNG" : "JPEG";
    System.out.println("fixture=ImageModifierMemorySmokeApp,fixturePath=" + fixture
        + ",fixtureFormat=" + fixtureFormat + ",fixtureWidth=" + fixtureWidth
        + ",fixtureHeight=" + fixtureHeight + ",events=" + events
        + ",sourceDecode=" + sourceDecode + ",directDraw=" + directDraw
        + ",drawPlanCacheHits=" + Image.imageDrawPlanCacheHitCountForTest()
        + ",noMaterialization=" + noMaterialization + ",boundedNativeLive=" + boundedNativeLive
        + ",overallPass=" + overallPass + (error.length() == 0 ? "" : ",error=" + error));
    exit(overallPass ? 0 : 1);
  }

  private static String fixtureFromCommandLine() {
    String commandLine = getCommandLine();
    return commandLine == null || commandLine.trim().length() == 0
        ? DEFAULT_SOURCE : commandLine.trim();
  }

  private static void checkpoint(String name, int events) {
    System.out.println("checkpoint=" + name + ",events=" + events + ",timestampMs=" + Vm.getTimeStamp()
        + ",imageCreated=" + Image.imageCreatedCountForTest()
        + ",imageFinalized=" + Image.imageFinalizedCountForTest()
        + ",imagePipelineCreated=" + Image.imagePipelineCreatedCountForTest()
        + ",imageDrawPlanCreated=" + Image.imageDrawPlanCreatedCountForTest()
        + ",drawPlanCacheHits=" + Image.imageDrawPlanCacheHitCountForTest()
        + ",nativeBackingCreated=" + NativeImageBacking.backingRecordsCreatedForTest()
        + ",nativeBackingReleased=" + NativeImageBacking.backingRecordsReleasedForTest()
        + ",nativeBackingLive=" + NativeImageBacking.backingRecordsLiveForTest()
        + ",nativeBackingPeakLive=" + NativeImageBacking.backingRecordsPeakLiveForTest()
        + ",nativeBackingBytesLive=" + NativeImageBacking.backingBytesLiveForTest()
        + ",nativeBackingBytesPeakLive=" + NativeImageBacking.backingBytesPeakLiveForTest()
        + ",fullDecode=" + Image.fullDecodeInvocationCountForTest()
        + ",targetedDecode=" + Image.targetedDecodeInvocationCountForTest()
        + ",geometryMaterialization=" + Image.nativeGeometryMaterializationCountForTest()
        + ",colorReadback=" + Image.nativeColorReadbackCountForTest()
        + ",backingReadback=" + Image.backingReadbackCountForTest()
        + ",directDraw=" + Image.directDrawPlanExecutionCountForTest());
    System.out.flush();
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException(message);
    }
  }
}
