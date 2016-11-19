/*******************************************************************************
 * Copyright 2016 Albert Shun-Dat Chan
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 *******************************************************************************/
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