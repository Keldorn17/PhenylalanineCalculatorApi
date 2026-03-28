package com.keldorn.phenylalaninecalculatorapi.it;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.test.context.jdbc.Sql;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Sql(scripts = "/sql/cleanup-db.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public @interface DirtyTest {
}
