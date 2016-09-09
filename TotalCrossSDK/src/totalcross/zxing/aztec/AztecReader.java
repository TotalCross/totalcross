/*
 * Copyright 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package totalcross.zxing.aztec;

import totalcross.zxing.BarcodeFormat;
import totalcross.zxing.BinaryBitmap;
import totalcross.zxing.DecodeHintType;
import totalcross.zxing.FormatException;
import totalcross.zxing.NotFoundException;
import totalcross.zxing.Reader;
import totalcross.zxing.Result;
import totalcross.zxing.ResultMetadataType;
import totalcross.zxing.ResultPoint;
import totalcross.zxing.ResultPointCallback;
import totalcross.zxing.aztec.decoder.Decoder;
import totalcross.zxing.aztec.detector.Detector;
import totalcross.zxing.common.DecoderResult;

import java.util.List;
import java.util.Map;

/**
 * This implementation can detect and decode Aztec codes in an image.
 *
 * @author David Olivier
 */
public final class AztecReader implements Reader {

  /**
   * Locates and decodes a Data Matrix code in an image.
   *
   * @return a String representing the content encoded by the Data Matrix code
   * @throws NotFoundException if a Data Matrix code cannot be found
   * @throws FormatException if a Data Matrix code cannot be decoded
   */
  @Override
  public Result decode(BinaryBitmap image) throws NotFoundException, FormatException {
    return decode(image, null);
  }

  @Override
  public Result decode(BinaryBitmap image, Map<DecodeHintType,?> hints)
      throws NotFoundException, FormatException {

    NotFoundException notFoundException = null;
    FormatException formatException = null;
    Detector detector = new Detector(image.getBlackMatrix());
    ResultPoint[] points = null;
    DecoderResult decoderResult = null;
    try {
      AztecDetectorResult detectorResult = detector.detect(false);
      points = detectorResult.getPoints();
      decoderResult = new Decoder().decode(detectorResult);
    } catch (NotFoundException e) {
      notFoundException = e;
    } catch (FormatException e) {
      formatException = e;
    }
    if (decoderResult == null) {
      try {
        AztecDetectorResult detectorResult = detector.detect(true);
        points = detectorResult.getPoints();
        decoderResult = new Decoder().decode(detectorResult);
      } catch (NotFoundException | FormatException e) {
        if (notFoundException != null) {
          throw notFoundException;
        }
        if (formatException != null) {
          throw formatException;
        }
        throw e;
      }
    }

    if (hints != null) {
      ResultPointCallback rpcb = (ResultPointCallback) hints.get(DecodeHintType.NEED_RESULT_POINT_CALLBACK);
      if (rpcb != null) {
        for (ResultPoint point : points) {
          rpcb.foundPossibleResultPoint(point);
        }
      }
    }

    Result result = new Result(decoderResult.getText(), decoderResult.getRawBytes(), points, BarcodeFormat.AZTEC);
    
    List<byte[]> byteSegments = decoderResult.getByteSegments();
    if (byteSegments != null) {
      result.putMetadata(ResultMetadataType.BYTE_SEGMENTS, byteSegments);
    }
    String ecLevel = decoderResult.getECLevel();
    if (ecLevel != null) {
      result.putMetadata(ResultMetadataType.ERROR_CORRECTION_LEVEL, ecLevel);
    }
    
    return result;
  }

  @Override
  public void reset() {
    // do nothing
  }

}
