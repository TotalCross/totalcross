// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.media;

import totalcross.sys.Vm;
import totalcross.ui.event.Event;

/**
 * Events posted by a soundclip control.
 * @since SuperWaba 5.66
 */

public class MediaClipEvent extends Event<MediaClipHandler> {
  /** The event type when the play starts. */
  public static final int STARTED = 550;
  /** The event type when the play ends. */
  public static final int STOPPED = 551;
  /** The event type when the play pauses. */
  public static final int CLOSED = 552;
  public static final int ERROR = 553;
  public static final int END_OF_MEDIA = 554;

  /** Constructs a MediaClipEvent, setting the type and target to the given parameters. */
  public MediaClipEvent(int type, Object target) {
    super(type, target, Vm.getTimeStamp());
  }

  /** Constructs an empty MediaClipEvent. */
  public MediaClipEvent() {
  }

  @Override
  public void dispatch(MediaClipHandler listener) {
    // TODO Auto-generated method stub
    
  }
}
