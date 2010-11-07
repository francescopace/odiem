package org.odiem.sdk.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.scannotation.AnnotationDB;
import org.scannotation.ClasspathUrlFinder;

public class AnnotationUtils {

	public static Set<Class<?>> listAnnotatedSubClasses(

	Class<?> pojoClass) throws Exception {

		HashSet<Class<?>> set = new HashSet<Class<?>>();

		URL url = ClasspathUrlFinder.findClassBase(pojoClass);
		if (url.getProtocol().equals("vfszip")) {
			url = new URL(url.toString().replaceFirst("vfszip", "file"));
		}
		AnnotationDB db = new AnnotationDB();
		db.scanArchives(url);

		Set<String> ldapObjectClassAnnotatedClasses = db.getAnnotationIndex()
				.get(ObjectClass.class.getName());

		for (String annotatedClassName : ldapObjectClassAnnotatedClasses) {
			Class<?> annotatedClass = Class.forName(annotatedClassName);
			if (pojoClass != annotatedClass
					&& pojoClass.isAssignableFrom(annotatedClass)
					&& !Modifier.isAbstract(annotatedClass.getModifiers())) {

				set.add(annotatedClass);
			}
		}
		return set;
	}

	public static Attribute copyAttribute(final Attribute attribute,
			final String value) {

		return new Attribute() {
			public Class<? extends Annotation> annotationType() {
				return attribute.annotationType();
			}

			public String value() {
				return value;
			}

			public boolean isId() {
				return attribute.isId();
			}
		};
	}

	public static ObjectClass copyObjectClass(final ObjectClass objectClass,
			final String value) {
		return new ObjectClass() {

			public Class<? extends Annotation> annotationType() {
				return objectClass.annotationType();
			}

			public String value() {
				return value;
			}
		};
	}
}
