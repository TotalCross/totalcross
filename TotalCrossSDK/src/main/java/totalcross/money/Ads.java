package totalcross.money;

import totalcross.sys.Settings;
import totalcross.ui.gfx.Coord;

public class Ads
{
  public enum Size
  {
    ADMOB_BANNER,
    ADMOB_FULL,
    ADMOB_LARGE,
    ADMOB_LEADER,
    ADMOB_MEDIUM,
    ADMOB_SKY,
    ADMOB_SMART,
  };

  public enum Position
  {
    BOTTOM,
    TOP
  };

  private static Size size = Size.ADMOB_SMART;
  private static Position pos = Position.BOTTOM;

  public static boolean hasAds = Settings.platform.equals(Settings.ANDROID);

  public static Coord getSizeInPixels(Size size)
  {
    if (!hasAds){
      return new Coord(0,0);
    }
    int v = getHeightD(size.ordinal());
    int w = v % 1000000;
    int h = v / 1000000;
    return new Coord(w,h);
  }

  public static void configure(String id)
  {
    if (hasAds){
      configureD(id);
    }
  }

  public static void setSize(Size s)
  {
    size = s;
    if (hasAds){
      setSizeD(s.ordinal());
    }
  }

  public static Size getSize()
  {
    return size;
  }

  public static void setPosition(Position p)
  {
    pos = p;
    if (hasAds){
      setPositionD(p.ordinal());
    }
  }

  public static Position getPosition()
  {
    return pos;
  }

  public static void setVisible(boolean b)
  {
    if (hasAds){
      setVisibleD(b);
    }
  }

  public static boolean isVisible()
  {
    return hasAds && isVisibleD();
  }

  native static int getHeightD(int size);
  native static void configureD(String id);
  native static void setSizeD(int s);
  native static void setPositionD(int p);
  native static void setVisibleD(boolean b);
  native static boolean isVisibleD();
}
