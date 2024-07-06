package net.burningtnt.hmclprs.hooks;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface EntryPoint {
    LifeCycle when();

    Type type();

    enum LifeCycle {
        /**
         * Invoked before Application launches. Be careful while using packages from HMCL project.
         */
        BOOTSTRAP,
        /**
         * Invoked after Application launches. All packages from HMCL project is available.
         */
        RUNTIME
    }

    enum Type {
        INJECT,
        VALUE_MUTATION,
        REDIRECT
    }
}
