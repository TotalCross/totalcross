package totalcross.ui;

import totalcross.sys.Convert;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.FocusListener;
import totalcross.ui.event.FontChangeEvent;
import totalcross.ui.event.FontChangeHandler;
import totalcross.ui.event.UpdateListener;
import totalcross.ui.event.ValueChangeEvent;
import totalcross.ui.event.ValueChangeHandler;
import totalcross.ui.font.Font;
import totalcross.util.UnitsConverter;

public class FloatingLabel<T extends Control & HasValue<?>> {

	private T target;

	private Font captionAnimationFontTarget;

	private Font captionFontSmall;

	private Font fcap;

	public int xcap, ycap, ycap0, xcap0, inccap;

	public boolean isRunning;

	private boolean isExpanded = true;

	private int executedTime;

	private int topX, topY;

	public final UpdateListener updateListener = new UpdateListener() {

		@Override
		public void updateListenerTriggered(int elapsedMilliseconds) {
			singleStep(elapsedMilliseconds);
			Window.needsPaint = true;
			if (fcap.size == captionAnimationFontTarget.size) {
				isExpanded = !isExpanded;
				isRunning = false;
				executedTime = 0;
				MainWindow.getMainWindow().removeUpdateListener(updateListener);
			}
		}
	};

	private float testPercentage;

	public FloatingLabel(T target) {
		this.target = target;
		testPercentage = 0;
		executedTime = 0;
		captionFontSmall = Font.getFont(target.getFont().name, false, target.getFont().size * 75 / 100);
		fcap = target.getFont();

		target.addFontChangeHandler(new FontChangeHandler() {

			@Override
			public void onFontChange(FontChangeEvent event) {
				captionFontSmall = Font.getFont(event.font.name, false, event.font.size * 75 / 100);
				fcap = event.font;
			}
		});

		if (target instanceof TextControl) {
			target.addFocusListener(new FocusListener() {

				@Override
				public void focusOut(ControlEvent e) {
					if (target.getValue() != null ^ !isExpanded) {
						animateMaterial(true);
					}
				}

				@Override
				public void focusIn(ControlEvent e) {
					if (target.getValue() == null & isExpanded) {
						animateMaterial(true);
					}
				}
			});
		}

		target.addValueChangeHandler(
		new ValueChangeHandler<Object>() {

			@Override
			public void onValueChange(ValueChangeEvent<Object> event) {
				// pay attention to the ! (not) at the beginning
				if (!((isExpanded && target.getValue() == null)
					  || (!isExpanded && target.getValue() != null)
					  || (!isExpanded && target.getValue() == null && target instanceof TextControl))) {
					animateMaterial(target.isDisplayed());
				}
			}
		});
	}

	public FloatingLabel(T target, int variable1, int variable2) {
		this(target);
		this.topX = variable1;
		this.topY = variable2;
	}
	private void animateMaterial(boolean slow) {
		if (!isRunning) {
			final Font targetFont = target.getFont();
			captionAnimationFontTarget = isExpanded ? captionFontSmall : targetFont;
			float percentage = 0;
			if (target instanceof OutlinedEdit) {
				if (((OutlinedEdit) target).totalTime == 0) {
					percentage = -1;
				} else {
					percentage = (float) executedTime / ((OutlinedEdit) target).totalTime;
				}
			}
			if (target instanceof OutlinedEdit
				? percentage == -1 ? true
				: executedTime / ((OutlinedEdit) target).totalTime < 1
				: true) {
				if (fcap.size != captionAnimationFontTarget.size) {
					if (slow) {
						inccap = fcap.size == targetFont.size ? -1 : 1;
						isRunning = true;
						if (target instanceof OutlinedEdit) {
							testPercentage = isExpanded ? 0 : 1;
						}
						MainWindow.getMainWindow().addUpdateListener(updateListener);
					} else {
						fullStep();
						isExpanded = !isExpanded;
					}
				}
			}
		}
	}

	private void singleStep(int ellapsedMilliseconds) {
		int xA;
		int xB;
		int yA;
		int yB;
		executedTime += ellapsedMilliseconds;
		if (target instanceof OutlinedEdit) {
			if (((Edit) target).chars.length() > 0) {
				fullStep();
				return;
			}
			int totalTime = ((OutlinedEdit) target).totalTime;
			float timeT = 1.0f / ellapsedMilliseconds + (float) executedTime / totalTime;
			if (timeT > 1) {
				timeT = 1;
			} else if (timeT < 0) {
				timeT = 0;
			}

			// Label Font size Animation
			int textSizeDifference = target.getFont().size - captionFontSmall.size;
			if (isExpanded ? testPercentage < timeT : testPercentage > 1 - timeT) {
				fcap = fcap.adjustedBy(inccap);
				testPercentage += -inccap / (float) textSizeDifference;
				float lastStep = (isExpanded ? (textSizeDifference - 1) : 1) / (float) textSizeDifference;
				float lastStepLastFortyPercent = isExpanded ? lastStep + 6 / 10 / textSizeDifference : 4 / 10 *
												 lastStep;
				if (isExpanded ? timeT < lastStepLastFortyPercent
					: 1 - timeT > lastStepLastFortyPercent && testPercentage == lastStep) {
					fcap = fcap.adjustedBy(-inccap);
					testPercentage -= -inccap / (float) textSizeDifference;
				}
			}

			// Label Position Animation
			if (fcap.size == (isExpanded ? captionFontSmall.size : target.font.size)) {
				timeT = 1;
			}
			xA = isExpanded ? xcap0 : topX;
			yA = isExpanded ? ycap0 : topY;
			xB = !isExpanded ? xcap0 : topX;
			yB = !isExpanded ? ycap0 : topY;
			xcap = (int)(xA * (1 - timeT) + xB * timeT);
			ycap = (int)(yA * (1 - timeT) + yB * timeT);
		} else if (target instanceof MultiEdit) {
			if (((MultiEdit) target).chars.length() > 0) {
				fullStep();
				return;
			}
			int fmHmin = captionFontSmall.fm.height;
			// Label Font size Animation
			fcap = fcap.adjustedBy(inccap);
			// Label Position Animation
			ycap = ycap0 * (fcap.fm.height - fmHmin) / (target.getFont().fm.height - fmHmin);
			xcap = xcap0 * (fcap.fm.height - fmHmin) / (target.getFont().fm.height - fmHmin);
		} else {
			if (target instanceof Edit && ((Edit) target).chars.length() > 0) {
				fullStep();
				return;
			}
			final int animationTime = 80;
			float timeT = 1.0f / ellapsedMilliseconds + (float) executedTime / animationTime;
			if (timeT > 1) {
				timeT = 1;
			} else if (timeT < 0) {
				timeT = 0;
			}
			// Label Font size Animation
			int textSizeDifference = target.getFont().size - captionFontSmall.size;
			if (isExpanded ? testPercentage < timeT : testPercentage > 1 - timeT) {
				fcap = fcap.adjustedBy(inccap);
				testPercentage += -inccap / (float) textSizeDifference;
				float lastStep = (isExpanded ? (textSizeDifference - 1) : 1) / (float) textSizeDifference;
				float lastStepLastFortyPercent = isExpanded ? lastStep + 6 / 10 / textSizeDifference : 4 / 10 *
												 lastStep;
				if (isExpanded ? timeT < lastStepLastFortyPercent
					: 1 - timeT > lastStepLastFortyPercent && testPercentage == lastStep) {
					fcap = fcap.adjustedBy(-inccap);
					testPercentage -= -inccap / (float) textSizeDifference;
				}
				if (fcap.size == (isExpanded ? captionFontSmall.size : target.font.size)) {
					timeT = 1;
				}
			}
			// Label Position Animation
			yA = isExpanded ? ycap0  : UnitsConverter.toPixels(Control.DP + 20) - captionFontSmall.fm.ascent -
				 captionFontSmall.fm.descent;
			yB = !isExpanded ? ycap0 : UnitsConverter.toPixels(Control.DP + 20) - captionFontSmall.fm.ascent -
				 captionFontSmall.fm.descent;
			ycap = (int)(yA * (1 - timeT) + yB * timeT);
		}
	}

	private void fullStep() {
		// Label Font size Animation
		fcap = fcap.adjustedBy(isExpanded ? captionFontSmall.size - target.getFont().size :
							   target.getFont().size - captionFontSmall.size);
		// Label Position Animation
		if (target instanceof OutlinedEdit) {
			xcap = !isExpanded ? xcap0 : topX;
			ycap = !isExpanded ? ycap0 : topY;
		} else if (target instanceof MultiEdit) {
			int fmHmin = captionFontSmall.fm.height;
			ycap = ycap0 * (fcap.fm.height - fmHmin) / (target.getFont().fm.height - fmHmin);
			xcap = xcap0 * (fcap.fm.height - fmHmin) / (target.getFont().fm.height - fmHmin);
		} else {
			ycap = !isExpanded ? ycap0 : UnitsConverter.toPixels(Control.DP + 20) - captionFontSmall.fm.ascent -
				   captionFontSmall.fm.descent;
		}
		testPercentage = -inccap;
	}

	public int getExtraHeight() {
		return captionFontSmall.fm.height;
	}

	public Font getCaptionFontSmall() {
		return captionFontSmall;
	}

	public Font getFcap() {
		return fcap;
	}

	public boolean isBig() {
		return isExpanded;
	}

	public int getTopX() {
		return topX;
	}
	public void setTopY(int variable1) {
		this.topX = variable1;
	}
	public int getTopY() {
		return topY;
	}
	public void setTopX(int variable2) {
		this.topY = variable2;
	}
}
