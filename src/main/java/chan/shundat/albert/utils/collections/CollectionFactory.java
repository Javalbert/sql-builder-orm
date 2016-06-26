package chan.shundat.albert.utils.collections;

import java.util.Collection;

public interface CollectionFactory {
	<T> Collection<T> newInstance();
}