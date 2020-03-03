package totalcross.ui;

import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;
import totalcross.util.UnitsConverter;

public class Divider extends Control{
	
	public Divider() {
		backColor = Color.getRGB("c4c4c4");
	}
	
	@Override
	public int getPreferredWidth() {
		// TODO Auto-generated method stub
		return parent == null ? UnitsConverter.toPixels(20 + DP) : parent.width - 2;
	}
	
	@Override
	public int getPreferredHeight() {
		// TODO Auto-generated method stub
		return UnitsConverter.toPixels(1 + DP);
	}
	
	@Override
	public void onPaint(Graphics g) {
		super.onPaint(g);
		g.backColor = backColor;
		g.fillRect(0, 0, width, height);
	}
}
