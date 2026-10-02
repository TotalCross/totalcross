// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.image;

import totalcross.io.ByteArrayStream;
import totalcross.io.File;
import totalcross.sys.Architecture;
import totalcross.sys.GraphicsBackend;
import totalcross.sys.Platform;
import totalcross.sys.RuntimeFamily;
import totalcross.sys.Settings;
import totalcross.sys.runtime.RuntimeCondition;
import totalcross.sys.runtime.RuntimeConfiguration;
import totalcross.sys.runtime.RuntimeWhen;
import totalcross.ui.ImageControl;
import totalcross.ui.MainWindow;
import totalcross.ui.ScrollContainer;
import totalcross.ui.gfx.Graphics;

/** Deployed macOS smoke for explicit async JPEG preparation and its lifecycle. */
@RuntimeConfiguration
@ImageRuntimeRule(when = @RuntimeWhen(allOf = {
    @RuntimeCondition(platform = Platform.MACOS),
    @RuntimeCondition(family = RuntimeFamily.DESKTOP),
    @RuntimeCondition(architecture = Architecture.ARM64),
    @RuntimeCondition(backend = GraphicsBackend.RASTER)
}), prefetchWorker = ImagePrefetchWorkerMode.LEGACY_PER_ENTRY_THREAD)
public class ImageAsyncPreparationSmokeApp extends MainWindow {
  private static final String SOURCE_PATH = "p8-async-source.jpg";
  private static final String CORRUPT_PATH = "p8-async-corrupt.jpg";

  private boolean explicitPreparation;
  private boolean explicitLegacyWorker;
  private boolean capturedPathDeleted;
  private boolean staleBatch;
  private boolean staleRequest;
  private boolean transientRetry;
  private boolean deterministicFailure;
  private boolean uiAdoption;
  private boolean drawReuse;
  private boolean finished;
  private String error = "";
  private String detail = "";
  private int oldBatchCallbacks;
  private int latestBatchCallbacks;

  @Override
  public void initUI() {
    try {
      Settings.fingerTouch = false;
      byte[] validJpeg = jpeg(0xFF2080D0);
      write(SOURCE_PATH, validJpeg);
      final Image initial = Image.getJpegScaled(SOURCE_PATH, 1, 4);
      delete(SOURCE_PATH);
      capturedPathDeleted = true;
      final double scale = getGraphics().getContentScale();
      final ImagePreparationRequest captured = initial.captureDisplayPreparationRequest(scale, 0L);
      explicitLegacyWorker = captured != null
          && captured.effectivePolicy.prefetchWorker() == ImagePrefetchWorkerMode.LEGACY_PER_ENTRY_THREAD;
      require(explicitLegacyWorker, "explicit LEGACY worker policy was not captured");
      final ScrollContainer initialScroll = addVisibleImage(initial);
      Image.resetImageOperationAccountingForTest();
      final int decodeCountBefore = Image.targetedDecodeInvocationCountForTest();
      initialScroll.prepareForDisplay(new Runnable() {
        @Override
        public void run() {
          oldBatchCallbacks++;
        }
      });
      initialScroll.prepareForDisplay(new Runnable() {
        @Override
        public void run() {
          runStage(new Stage() {
            @Override
            public void run() throws Exception {
              latestBatchCallbacks++;
              staleBatch = oldBatchCallbacks == 0 && latestBatchCallbacks == 1;
              explicitPreparation = initial.isDisplayPreparationReady(captured);
              uiAdoption = MainWindow.isMainThread();
              require(staleBatch, "latest batch callback only");
              require(explicitPreparation, "prepared image is ready");
              require(uiAdoption, "adoption callback is on UI thread");
              int decodeCountAfterPreparation = Image.targetedDecodeInvocationCountForTest();
              initial.resolveForDrawing(scale);
              int decodeCountAfter = Image.targetedDecodeInvocationCountForTest();
              drawReuse = decodeCountAfterPreparation > decodeCountBefore
                  && decodeCountAfter == decodeCountAfterPreparation;
              detail = "decodeBefore=" + decodeCountBefore + ",decodePrepared=" + decodeCountAfterPreparation
                  + ",decodeAfterDraw=" + decodeCountAfter;
              staleRequestCase(validJpeg, scale);
            }
          });
        }
      });
    } catch (Throwable failure) {
      finish(false, failure);
    }
  }

  private void staleRequestCase(final byte[] validJpeg, final double scale) throws Exception {
    final Image changed = lazyImage(validJpeg, "p8-stale-request.jpg");
    final ImagePreparationRequest request = changed.captureDisplayPreparationRequest(scale, 0L);
    final ImagePipeline capturedPipeline = request.pipeline;
    ScrollContainer scroll = addVisibleImage(changed);
    scroll.prepareForDisplay(new Runnable() {
      @Override
      public void run() {
        runStage(new Stage() {
          @Override
          public void run() throws Exception {
            boolean pipelineChanged = changed.pipelineForSmoke() != capturedPipeline;
            boolean sourceHasBacking = request.source.decodedBackingForReuse(request.decodeDenominator) != null;
            staleRequest = pipelineChanged && !sourceHasBacking;
            detail += ",stalePipelineChanged=" + pipelineChanged + ",staleSourceHasBacking=" + sourceHasBacking
                + ",staleSourceGeneration=" + request.source.decodedGeneration();
            require(staleRequest, "stale pipeline result was rejected");
            transientRetryCase(validJpeg, scale);
          }
        });
      }
    });
    scroll.setVisible(false);
    changed.applyColor2(0xFF804020);
  }

  private void transientRetryCase(final byte[] validJpeg, final double scale) throws Exception {
    final Image retry = lazyImage(validJpeg, "p8-transient-retry.jpg");
    final EncodedImageSource source = (EncodedImageSource) retry.pipelineForSmoke().root();
    final ScrollContainer scroll = addVisibleImage(retry);
    Image.failNextTargetedDecodeInfrastructureForTest();
    scroll.prepareForDisplay(new Runnable() {
      @Override
      public void run() {
        runStage(new Stage() {
          @Override
          public void run() throws Exception {
            boolean noCachedFailure = source.decodeFailure() == null;
            boolean readyAfterFirstAttempt = retry.isDisplayPreparationReady(
                retry.captureDisplayPreparationRequest(scale, 0L));
            detail += ",transientGeneration=" + source.decodedGeneration()
                + ",transientReadyAfterFirst=" + readyAfterFirstAttempt;
            require(noCachedFailure && !readyAfterFirstAttempt, "transient failure remains retryable");
            scroll.setVisible(true);
            scroll.prepareForDisplay(new Runnable() {
              @Override
              public void run() {
                runStage(new Stage() {
                  @Override
                  public void run() throws Exception {
                    transientRetry = retry.isDisplayPreparationReady(
                        retry.captureDisplayPreparationRequest(scale, 0L));
                    require(transientRetry, "later explicit batch retries transient failure");
                    deterministicFailureCase(scale);
                  }
                });
              }
            });
            scroll.setVisible(false);
          }
        });
      }
    });
  }

  private void deterministicFailureCase(final double scale) throws Exception {
    byte[] corruptBytes = corruptJpegEntropy(jpeg(0xFFCC5030));
    write(CORRUPT_PATH, corruptBytes);
    final Image corrupt = Image.getJpegScaled(CORRUPT_PATH, 1, 4);
    final EncodedImageSource source = (EncodedImageSource) corrupt.pipelineForSmoke().root();
    delete(CORRUPT_PATH);
    ScrollContainer scroll = addVisibleImage(corrupt);
    scroll.prepareForDisplay(new Runnable() {
      @Override
      public void run() {
        runStage(new Stage() {
          @Override
          public void run() throws Exception {
            ImageException cached = source.decodeFailure();
            deterministicFailure = cached != null && materializationFailure(corrupt) == cached;
            require(deterministicFailure, "deterministic failure is cached on adoption");
            finish(true, null);
          }
        });
      }
    });
  }

  private ScrollContainer addVisibleImage(Image image) {
    ScrollContainer scroll = new ScrollContainer(false, false);
    scroll.setRect(0, 0, 220, 160);
    ImageControl control = new ImageControl(image);
    scroll.add(control);
    control.setRect(4, 4, Math.max(24, image.getWidth()), Math.max(20, image.getHeight()));
    return scroll;
  }

  private static Image lazyImage(byte[] encoded, String path) throws Exception {
    write(path, encoded);
    Image image = Image.getJpegScaled(path, 1, 4);
    delete(path);
    return image;
  }

  private static byte[] jpeg(int color) throws Exception {
    Image image = new Image(64, 32);
    Graphics graphics = image.getGraphics();
    graphics.foreColor = color;
    graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
    ByteArrayStream output = new ByteArrayStream(8192);
    image.createJpg(output, 90);
    byte[] encoded = new byte[output.getPos()];
    System.arraycopy(output.getBuffer(), 0, encoded, 0, encoded.length);
    return encoded;
  }

  private static void write(String path, byte[] bytes) throws Exception {
    File file = new File(path, File.CREATE_EMPTY);
    file.writeBytes(bytes, 0, bytes.length);
    file.close();
  }

  private static void delete(String path) throws Exception {
    File file = new File(path, File.DONT_OPEN);
    file.delete();
  }

  private static byte[] corruptJpegEntropy(byte[] source) {
    int sos = -1;
    for (int i = 0; i + 1 < source.length; i++) {
      if ((source[i] & 0xFF) == 0xFF && (source[i + 1] & 0xFF) == 0xDA) {
        sos = i;
        break;
      }
    }
    if (sos < 0) {
      throw new IllegalStateException("JPEG SOS marker missing");
    }
    int segmentLength = ((source[sos + 2] & 0xFF) << 8) | (source[sos + 3] & 0xFF);
    int entropy = sos + 2 + segmentLength;
    byte[] invalidTail = {
        (byte) 0xFF, (byte) 0xC3, 0, 8, 8, 0, 1, 0, 1, 1, (byte) 0xFF, (byte) 0xD9
    };
    byte[] result = new byte[entropy + invalidTail.length];
    System.arraycopy(source, 0, result, 0, entropy);
    System.arraycopy(invalidTail, 0, result, entropy, invalidTail.length);
    return result;
  }

  private static ImageException materializationFailure(Image image) {
    try {
      image.resolveForDrawing(1.0);
      return null;
    } catch (ImageException failure) {
      return failure;
    }
  }

  private void runStage(Stage stage) {
    if (finished) {
      return;
    }
    try {
      stage.run();
    } catch (Throwable failure) {
      finish(false, failure);
    }
  }

  private interface Stage {
    void run() throws Exception;
  }

  private void finish(boolean passed, Throwable failure) {
    if (finished) {
      return;
    }
    finished = true;
    if (failure != null) {
      error = failure.getClass().getName() + ":" + String.valueOf(failure.getMessage()).replace(' ', '_');
    }
    boolean overall = passed && explicitPreparation && explicitLegacyWorker && capturedPathDeleted && staleBatch
        && staleRequest && transientRetry && deterministicFailure && uiAdoption && drawReuse;
    System.out.println("fixture=ImageAsyncPreparationSmokeApp,explicitPreparation=" + explicitPreparation
        + ",explicitLegacyWorker=" + explicitLegacyWorker + ",capturedPathDeleted=" + capturedPathDeleted
        + ",staleBatch=" + staleBatch
        + ",staleRequest=" + staleRequest + ",transientRetry=" + transientRetry
        + ",deterministicFailure=" + deterministicFailure + ",uiAdoption=" + uiAdoption
        + ",drawReuse=" + drawReuse + ",overallPass=" + overall
        + (detail.length() == 0 ? "" : ",detail=" + detail)
        + (error.length() == 0 ? "" : ",error=" + error));
    System.out.flush();
    exit(overall ? 0 : 1);
  }

  private void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalStateException(message);
    }
  }
}
