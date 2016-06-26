package chan.shundat.albert.utils.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PropertyMemberAccess implements MemberAccess {
	private final Method getter;
	private final Method setter;
	
	public Method getGetter() { return getter; }
	public Method getSetter() { return setter; }
	
	public PropertyMemberAccess(Method getter, Method setter) {
		this.getter = getter;
		this.setter = setter;
	}
	
	@Override
	public Object get(Object instance) {
		try {
			return getter.invoke(instance);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			return null;
		}
	}
	
	@Override
	public void set(Object instance, Object value) {
		try {
			setter.invoke(instance, value);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {}
	}
}