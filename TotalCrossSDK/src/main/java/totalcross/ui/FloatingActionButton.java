package totalcross.ui;

import totalcross.res.Resources;
import totalcross.ui.event.DragEvent;
import totalcross.ui.event.PenEvent;
import totalcross.ui.event.PenListener;
import totalcross.ui.font.Font;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.image.Image;
import totalcross.ui.image.ImageException;
import totalcross.util.UnitsConverter;

public class FloatingActionButton extends Control {
	private Image backImg;
	private Image foreImg;
	private boolean pressed;

	public FloatingActionButton(Image foregroundImage) {
		this.foreImg = foregroundImage;
		backColor = Color.getRGB(253, 253, 253);
		foreColor = 0;

		addPenListener(new PenListener() {
			@Override
			public void penUp(PenEvent e) {
				e.consumed = true;
				pressed = false;
				backImg = null;
				Window.needsPaint = true;
				if ((e.x >= 0)&& (e.x <= width)&& (e.y >= 0)&& (e.y <= height)) {
					postPressedEvent();
				}
			}

			@Override
			public void penDragStart(DragEvent e) {
			}

			@Override
			public void penDragEnd(DragEvent e) {
			}

			@Override
			public void penDrag(DragEvent e) {
				if ((e.x < 0)|| (e.x > width)|| (e.y < 0)|| (e.y > height)) {
					e.consumed = true;
					pressed = false;
					backImg = null;
					Window.needsPaint = true;
				}
			}

			@Override
			public void penDown(PenEvent e) {
				e.consumed = true;
				pressed = true;
				backImg = null;
				Window.needsPaint = true;
			}
		});
		floating = true;
	}

	public FloatingActionButton() {
		this(Resources.floatingFrg);
	}

	/** Change the size of the icon. */
	public void setIconSize(int iconSize) {
		this.font = Font.getFont(font.name, false, iconSize);
		Window.needsPaint = true;
	}

	public int getIconSize() {
		return this.font.size;
	}

	public void setIcon(Image foregroundImage) {
		this.foreImg = foregroundImage;
		Window.needsPaint = true;
	}

	public Image getIcon() {
		return foreImg;
	}

	@Override
	protected void onBoundsChanged(boolean screenChanged) {
		backImg = null;
		Window.needsPaint = true;
	}

	@Override
	protected void onColorsChanged(boolean colorsChanged) {
		backImg = null;
		Window.needsPaint = true;
	}

	@Override
	public int getPreferredWidth() {
		return font.size + UnitsConverter.toPixels(36 + DP);
	}

	@Override
	public int getPreferredHeight() {
		return font.size + UnitsConverter.toPixels(36 + DP);
	}

	@Override
	public void onPaint(Graphics g) {
		super.onPaint(g);

		if (backImg == null) {
			try {
				backImg = Resources.floatingBkg.getPressedInstance(width, height, backColor,
				                                                   pressed ? Color.darker(backColor) : backColor, true);
			} catch (ImageException e) {
				e.printStackTrace();
			}
		}

		NinePatch.tryDrawImage(g, backImg, 0, 0);
		if (foreImg != null) {
			g.drawImage(foreImg, width / 2 - foreImg.getWidth() / 2, height / 2 - foreImg.getHeight() / 2);
		}
	}
}
