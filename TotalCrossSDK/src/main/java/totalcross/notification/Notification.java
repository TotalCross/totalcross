// Copyright (C) 2019-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.notification;

/**
 * A class that represents how a persistent notification is to be presented to the user using the
 * {@link NotificationManager}.
 *
 * <p>Notifications can only be created by using the The {@link Notification.Builder}.
 *
 * @author Fábio Sobral
 * @since TotalCross 4.2.0
 */
public class Notification {

  private String title;

  private String text;

  private Notification() {}

  /**
   * The title of this notification
   *
   * @return
   */
  public String title() {
    return title;
  }

  /**
   * The content text of this notification
   *
   * @return
   */
  public String text() {
    return text;
  }

  /**
   * Builder class for Notification objects.
   *
   * <p>Example:
   *
   * <pre>
   * Notification notification = new Notification.Builder()
   * 		.setTitle("Here's an important notification!")
   * 		.setText(subject)
   * 		.build();
   * </pre>
   *
   * @author Fábio Sobral
   * @since TotalCross 4.2.0
   */
  public static class Builder {

    private CharSequence title;

    private CharSequence text;

    /**
     * Combine all of the options that have been set and return a new Notification object.
     *
     * @return a new Notification object
     */
    public Notification build() {
      Notification notification = new Notification();
      notification.title = title.toString();
      notification.text = text.toString();

      return notification;
    }

    /**
     * Set the first line of text (usually properly emphasized as a title) in the platform
     * notification template.
     *
     * @param title
     * @return
     */
    public Builder title(CharSequence title) {
      this.title = title;
      return this;
    }

    /**
     * Set the second line of text (the content of the notification) in the platform notification
     * template.
     *
     * @param text
     * @return
     */
    public Builder text(CharSequence text) {
      this.text = text;
      return this;
    }
  }
}
