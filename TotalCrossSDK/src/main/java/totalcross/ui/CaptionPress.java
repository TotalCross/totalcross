package totalcross.ui;

/** Implement this method and assign to the captionPress field if you want to handle press in the caption area or in the captionIcon.
 *  This handler is available for the ComboBox, MultiEdit and Edit classes. 
 */
public interface CaptionPress {
  public void onCaptionPress();

  public void onIconPress();
}
