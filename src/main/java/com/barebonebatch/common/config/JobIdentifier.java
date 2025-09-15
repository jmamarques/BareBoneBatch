package com.barebonebatch.common.config;

import org.springframework.context.annotation.Configuration;

import java.lang.annotation.*;

/**
 * A custom annotation to identify a specific Spring Batch job.
 * This can be used for descriptive purposes or to be processed by other components.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JobIdentifier {
    /**
     * The unique identifier for the job.
     *
     * @return The job ID as a String.
     */
    String value();

    /**
     * An optional description for the job.
     *
     * @return The job description.
     */
    String description() default "";

    String uatIdf();

    String prodIdf();
}
