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
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Sql(scripts = {"/sql/cleanup-db.sql", "/db/changelog/test-changelog.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public @interface DirtyTest {
}
