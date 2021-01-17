package com.oliambot.inf;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Catch {
    String entry();
    int listen() default ON_GROUP;
    int permission() default MEMBER;

    int ON_GROUP = 0;
    int ON_FRIEND = 1;
    int BOTH = 2;

    int OWNER = 0;
    int ADMIN = 1;
    int MEMBER = 2;
}



