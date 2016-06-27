/*******************************************************************************
 * Copyright (c) 2016 Albert Shun-Dat Chan
 *
 * Licensed under MIT License
 * See LICENSE file at the root of the project or
 * https://github.com/Javalbert/sql-builder-orm/blob/master/LICENSE
 *******************************************************************************/
package chan.shundat.albert.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

@SuppressWarnings({ "rawtypes", "unchecked" })
public final class ClassUtils {
	public static final String NAME_BOOLEAN = "boolean";
	public static final String NAME_DOUBLE = "double";
	public static final String NAME_FLOAT = "float";
	public static final String NAME_INT = "int";
	public static final String NAME_JAVA_LANG_BOOLEAN = "java.lang.Boolean";
	public static final String NAME_JAVA_LANG_DOUBLE = "java.lang.Double";
	public static final String NAME_JAVA_LANG_FLOAT = "java.lang.Float";
	public static final String NAME_JAVA_LANG_INTEGER = "java.lang.Integer";
	public static final String NAME_JAVA_LANG_LONG = "java.lang.Long";
	public static final String NAME_JAVA_LANG_STRING = "java.lang.String";
	public static final String NAME_JAVA_MATH_BIG_DECIMAL = "java.math.BigDecimal";
	public static final String NAME_JAVA_SQL_TIMESTAMP = "java.sql.Timestamp";
	public static final String NAME_JAVA_UTIL_DATE = "java.util.Date";
	public static final String NAME_LONG = "long";
	
	// CREDIT: http://stackoverflow.com/a/520344
	public static List<Class> getClasses(String packageName) throws ClassNotFoundException, IOException {
        String path = packageName.replace('.', '/');
        
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = classLoader.getResources(path);
        
        List<File> dirs = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        
        List<Class> classes = new ArrayList<>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes;
    }
    
	private static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
        if (!directory.exists()) {
            return Collections.EMPTY_LIST;
        }
        
        List<Class> classes = new ArrayList<>();
        
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }
    
    private ClassUtils() {}
}