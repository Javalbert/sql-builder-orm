package chan.shundat.albert.utils.reflection;

import java.lang.reflect.Field;

public class FieldMemberAccess implements MemberAccess {
	private final Field field;
	
	public Field getField() { return field; }
	
	public FieldMemberAccess(Field field) {
		this.field = field;
	}
	
	@Override
	public Object get(Object instance) {
		try {
			return field.get(instance);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			return null;
		}
	}

	@Override
	public void set(Object instance, Object value) {
		try {
			field.set(instance, value);
		} catch (IllegalArgumentException | IllegalAccessException e) {}
	}
}