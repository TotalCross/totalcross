package totalcross.ui.image;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import totalcross.io.File;
import totalcross.util.concurrent.Lock;

public class ImageLoader {

  private String path;

  private SimpleImageInfo sif;

  private HashMap<String, Image> mipmap = new HashMap<>();

  private static HashMap<String, ImageLoader> cache = new HashMap<>();

  private static Lock lock = new Lock();

  public static ImageLoader get(String path) throws IOException {
    synchronized (lock) {
      ImageLoader loader = cache.get(path);
      if (loader != null) {
        return loader;
      }
      loader = new ImageLoader(path);
      cache.put(path, loader);
      return loader;
    }
  }

  private ImageLoader(String path) throws java.io.IOException {
    this.path = path;

    //  try (InputStream is = Launcher.instance.openInputStream(path)) {
    try (File f = new File(path, File.READ_ONLY)) {
      InputStream is = f.asInputStream();
      sif = new SimpleImageInfo(is);
    }
  }

  private static final double F1_8 = 12.5;
  private static final double F1_4 = 25;
  private static final double F1_2 = 50;

  public Image getImage(int desiredWidth, int desiredHeight) throws IOException, ImageException {
    final String key = desiredWidth + "x" + desiredHeight;
    Image i = mipmap.get(key);
    if (i != null) {
      return i;
    }

    if ("image/jpeg".equals(sif.getMimeType())) {
      double p1 = desiredWidth * 100 / sif.getWidth();
      double p2 = desiredHeight * 100 / sif.getHeight();
      double p = Math.min(p1, p2);

      int scale_num = 1;
      int scale_denom;

      // comparing doubles is bad!
      if (p < F1_8) {
        // 1/8
        scale_denom = 8;
      } else if (p < F1_4) {
        // 1/4
        scale_denom = 4;
      } else if (p < F1_2) {
        // 1/2
        scale_denom = 2;
      } else {
        // original size
        scale_denom = 1;
      }

      //    try (InputStream is = Launcher.instance.openInputStream(path)) {
      try (File f = new File(path, File.READ_ONLY)) {
        //      Stream f = Stream.asStream(is);
        i = Image.getScaledJpeg(f, scale_num, scale_denom).smoothScaledFixedAspectRatio(
            desiredWidth < desiredHeight ? desiredWidth : desiredHeight, desiredHeight < desiredWidth);
//        mipmap.put(key, i);
        return i;
      }
    } else {
      // not jpeg, just rescale
      i = new Image(path).getSmoothScaledInstance(desiredWidth, desiredHeight);
//      mipmap.put(key, i);
      return i;
    }
  }
}
