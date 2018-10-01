package totalcross.ui.image;

import java.io.IOException;
import java.util.HashMap;

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
  }

  public Image getImage(int desiredWidth, int desiredHeight) throws IOException, ImageException {
    final String key = desiredWidth + "x" + desiredHeight;
    Image i = mipmap.get(key);
    if (i != null) {
      return i;
    }

    try {
      i = Image.getJpegBestFit(path, desiredWidth, desiredHeight);
    } catch (Exception e) {
      // probably not a jpeg, we'll just ignore the exception and try again as a regular image.
      i = new Image(path);
    }
    int actualWidth = i.getWidth();
    int actualHeight = i.getHeight();

    if (actualWidth != desiredWidth || actualHeight != desiredHeight) {
      i =
          i.smoothScaledFixedAspectRatio(
              desiredWidth < desiredHeight ? desiredWidth : desiredHeight,
              desiredWidth > desiredHeight);
    }
    // disabled mimap for now
    // mipmap.put(key, i);
    return i;
  }
}
