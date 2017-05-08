package jdkcompat.lang;

import java.io.IOException;

public interface Appendable4D {
	Appendable append(char c) throws IOException;
	Appendable append(CharSequence c) throws IOException;
	Appendable append(CharSequence c, int start, int end) throws IOException;
}
