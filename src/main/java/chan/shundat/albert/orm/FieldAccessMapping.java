package chan.shundat.albert.orm;

import java.lang.reflect.Field;

import chan.shundat.albert.utils.string.Strings;

public class FieldAccessMapping extends FieldColumnMapping {
	private static String initMapKeyName(String column, String alias, Field field) {
		MapKey mapKey = field.getAnnotation(MapKey.class);
		String mapKeyName = mapKey != null ? mapKey.value() : null;
		
		return !Strings.isNullOrEmpty(mapKeyName) ? mapKeyName 
				: !Strings.isNullOrEmpty(alias) ? alias 
				: !Strings.isNullOrEmpty(column) ? column 
				: field.getName();
	}
	
	private final Field field;
	
	public Field getField() { return field; }

	public FieldAccessMapping(String column, 
			String alias, 
			Field field, 
			int jdbcType, 
			boolean primaryKey, 
			GeneratedValue generatedValue, 
			boolean version) {
		super(column, alias, jdbcType, initMapKeyName(column, alias, field), primaryKey, generatedValue, version);
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