package dev.nolij.zumegradle.proguard;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
public @interface ProGuardKeep {
	
	@Retention(RetentionPolicy.CLASS)
	@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
	@interface WithObfuscation {}
	
	@Retention(RetentionPolicy.CLASS)
	@Target({ElementType.TYPE})
	@interface Enum {}
	
}
