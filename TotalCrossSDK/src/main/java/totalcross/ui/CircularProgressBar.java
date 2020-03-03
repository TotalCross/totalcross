package totalcross.ui;

import totalcross.sys.Settings;
import totalcross.ui.event.Event;
import totalcross.ui.event.EventHandler;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.event.PenEvent;
import totalcross.ui.font.Font;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.icon.Icon;
import totalcross.ui.icon.IconType;
import totalcross.util.UnitsConverter;

public class CircularProgressBar extends Control {
	private int thickness;
	private int maxValue;
	private int value;
	private Icon icon;
	private int iconColor;
	private int ix, iy;// icon's x and y.
	
	/**
	 * 
	 * */
	public CircularProgressBar(int thickness, int maxValue, int value, IconType icon) {
		this.thickness = thickness;
		if(maxValue < 0) {
			this.maxValue = 100;
		} else {
			this.maxValue = maxValue;
		}
		if(value < 0) {
			this.value = 0;
		} else if (value > maxValue) {
			this.value = maxValue;
		} else {
			this.value = value;
		}
		if(icon != null) {
			this.icon = new Icon(icon);
		}
	}
	
	public CircularProgressBar(int thickness, int maxValue, IconType icon) {
		this(thickness, maxValue, 1, icon);
	}
	
	public CircularProgressBar(int thickness, IconType icon) {
		this(thickness, 100, 1, icon);
	}
	
	public CircularProgressBar(int thickness, int maxValue, int value) {
		this(thickness, maxValue, value, null);
	}
	
	public CircularProgressBar(int thickness, int maxValue) {
		this(thickness, maxValue, 1, null);
	}
	
	public CircularProgressBar(int thickness) {
		this(thickness, 100, 1, null);
	}
	
	@Override
	public void onPaint(Graphics g) {
		int outsideCircleRadius = width/2;
		int insideCircleRadius = outsideCircleRadius - thickness;
		g.backColor = value != maxValue ? backColor : foreColor;
		g.fillCircle(width/2, height/2, outsideCircleRadius - 1);
		if(value != maxValue) {
			g.foreColor = foreColor; 
			g.backColor = foreColor;
			g.fillPie(width/2, height/2, outsideCircleRadius - 1, 90 - (double) value/maxValue * 360 , 90);
		}
		g.foreColor = g.backColor = parent.backColor;
		g.fillCircle(width/2, height/2, insideCircleRadius - 1);
		if(icon != null) {
			icon.setForeColor(iconColor);
			int insideHeight = height - thickness * 2;
			int hGap = height/5;
			Font f = icon.font;
			while(insideHeight - hGap > f.fm.height) {
				f = f.adjustedBy(1);
			}
			while(insideHeight - hGap < f.fm.height) {
				f = f.adjustedBy(-1);
			}
			g.setFont(f);
			ix = (width - f.fm.height)/2;
			iy = (height - f.fm.height)/2;
			g.translate(ix, iy);
			icon.onPaint(g);
		}
	}
	
	@Override
	protected void onBoundsChanged(boolean screenChanged) {
		super.onBoundsChanged(screenChanged);
		if(icon != null) {
			ix = (width - icon.getPreferredWidth())/2;
			iy = (height - icon.getPreferredHeight())/2;
		}
	}
	
	@Override
	public <H extends EventHandler> void onEvent(Event<H> event) {
		if(event.target == this) {
			switch (event.type) {
			case KeyEvent.ACTION_KEY_PRESS:
				postPressedEvent();
				break;
			case PenEvent.PEN_UP:
				PenEvent pe = (PenEvent) event;
				if((!Settings.fingerTouch || !hadParentScrolled()) && isInsideOrNear(pe.x, pe.y)) {
					postPressedEvent();
				}
				break;
			}
		}
	}
	
	@Override
	public int getPreferredWidth() {
		return UnitsConverter.toPixels(DP + 50);
	}
	
	@Override
	public int getPreferredHeight() {
		return UnitsConverter.toPixels(DP + 50);
	}
	
	public int getMaxValue() {
		return maxValue;
	}
	
	public void setMaxValue(int maxValue) {
		if(maxValue == this.maxValue) {
			return;
		}
		this.maxValue = maxValue;
		Window.needsPaint = true;
	}
	
	public int getValue() {
		return value;
	}
	
	public void setValue(int value) {
		if(value == this.value) {
			return;
		}
		this.value = value > maxValue ? maxValue : value;
		Window.needsPaint = true;
	}

	public int getIconColor() {
		return iconColor;
	}

	public void setIconColor(int iconColor) {
		if(iconColor == this.iconColor) {
			return;
		}
		this.iconColor = iconColor;
		Window.needsPaint = true;
	}

	public Icon getIcon() {
		return icon;
	}

	public void setIcon(Icon icon) {
		if(icon == this.icon) {
			return;
		}
		this.icon = icon;
		Window.needsPaint = true;
	}

}
