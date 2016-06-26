package chan.shundat.albert.utils.collections;

import java.util.Map;

public interface MapFactory {
	<K, V> Map<K, V> newInstance();
}