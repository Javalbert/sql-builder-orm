package chan.shundat.albert.utils.reflection;

public interface MemberAccess {
	Object get(Object instance);
	void set(Object instance, Object value);
}