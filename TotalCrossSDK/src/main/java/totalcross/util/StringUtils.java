package totalcross.util;

import totalcross.ui.font.FontMetrics;

public class StringUtils {
	/** This method puts three dots (...) at the final of the text to fit in the size you passed in. */
	public static String shortText(String text, FontMetrics fm, int width) {
		if (text.equals("Testando 1")) {

		}
		int textSize = fm.stringWidth(text), dotSize = fm.charWidth('.');

		int endIndex = text.length() - 1;

		if (textSize > width) {
			text += "...";
		} else {
			return text;
		}

		textSize += dotSize * 3;

		while (textSize > width) {
			if (endIndex - 1 < 0) {
				return "";
			}
			char removed = text.charAt(endIndex - 1);
			int charSize = fm.charWidth(removed);

			text = text.substring(0, endIndex) + text.substring(endIndex + 1, text.length());
			textSize -= charSize;
			endIndex--;
		}
		return text;
	}
}
