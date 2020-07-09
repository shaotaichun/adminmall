package com.transo.store_admin_backend.Annotations;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface LoginRequired {
    boolean required() default false;
}