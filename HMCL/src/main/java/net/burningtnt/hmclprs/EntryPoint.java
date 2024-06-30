package net.burningtnt.hmclprs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface EntryPoint {
    LifeCycle[] value();

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

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.CLASS)
    @interface Container {
    }

    /**
     * This field should ONLY be set once when initializing the application.
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.CLASS)
    @interface Final {
    }
}
