package totalcross.notification;

public class Notification {

  private String title;

  private String text;

  private Notification() {
  }
  
  public String title() {
    return title;
  }

  public String text() {
    return text;
  }
  
  public static class Builder {

    private CharSequence title;

    private CharSequence text;

    public Notification build() {
      Notification notification = new Notification();
      notification.title = title.toString();
      notification.text = text.toString();

      return notification;
    }

    public Builder title(CharSequence title) {
      this.title = title;
      return this;
    }

    public Builder text(CharSequence text) {
      this.text = text;
      return this;
    }
  }
}
