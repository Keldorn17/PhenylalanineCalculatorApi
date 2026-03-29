package com.keldorn.phenylalaninecalculatorapi.it.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.test.context.jdbc.Sql;

/**
 * This annotation is used for marking tests that make changes in the database.
 * Use this instead of {@link org.springframework.test.annotation.DirtiesContext @DirtiesContext}
 * to avoid full Spring reload and test container restart.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Sql(scripts = "/sql/cleanup-db.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public @interface DirtyTest {
}
