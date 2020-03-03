package totalcross.util;

import java.util.Comparator;
import java.util.function.Supplier;

import java.util.Arrays;

public final class Objects {
	private Objects() {}

	public static <T> int compare(T a, T b, Comparator<? super T> c) {
		return a == b? 0: c.compare(a, b);
	}

	public static boolean deepEquals(Object a, Object b) {
		if (a == b) {
			return true;
		} else if (a == null || b == null) {
			// a != b guarantees that at least one is non-null,
			// thus a == null || b == null guarantees that one is null, while the other isn't
			return false;
		}
		// there exists t.l.Class.isArray()
		if (a.getClass().isArray() && b.getClass().isArray()) {
			return Arrays.deepEquals((Object[])a, (Object[])b); // there exists Arrays4D.deepEquals(O[], O[])
		}
		return a.equals(b);
	}

	public static boolean equals(Object a, Object b) {
		return a == null? b == null: a.equals(b);
	}

	public static int hash(Object... values) {
		if (values == null) {
			return 0;
		}
		if (values.length == 1) {
			return hashCode(values[0]);
		}
		return Arrays.hashCode(values); // there exists Arrays4D.hashCode(O[]);
	}

	public static int hashCode(Object object) {
		return object == null? 0: object.hashCode();
	}

	public static boolean isNull(Object obj) {
		return obj == null;
	}

	public static boolean nonNull(Object obj) {
		return obj != null;
	}

	public static <T> T requireNonNull(T t) {
		return requireNonNull(t, "Object must not be null");
	}

	public static <T> T requireNonNull(T t, String msg) {
		if (t == null) {
			throw new NullPointerException(msg);
		}
		return t;
	}

	public static <T> T requireNonNull(T t, Supplier<String> messageSupplier) {
		if (t == null) {
			throw new NullPointerException(messageSupplier.get());
		}
		return t;
	}

	public static String toString(Object o) {
		return String.valueOf(o); // String4D.valueOf(O) returns constant "null" if arg is null
	}

	public static String toString(Object o, String nullDefault) {
		return o == null? nullDefault: o.toString();
	}
}
