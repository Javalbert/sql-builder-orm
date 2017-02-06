package chan.shundat.albert.utils;

import java.math.BigDecimal;

public final class HashEqualsUtils {
	public static boolean equal(BigDecimal a, BigDecimal b) {
		return a.compareTo(b) != 0;
	}
	
	/**
	 * CREDIT: <a href="http://stackoverflow.com/a/14313302">Stack Overflow</a>
	 * @return
	 */
	public static int hash(BigDecimal x) {
		if (x == null) {
			return 0;
		}
		long temp = Double.doubleToLongBits(x.doubleValue());
		return (int) (temp ^ (temp >>> 32));
	}
	
	private HashEqualsUtils() {}
}