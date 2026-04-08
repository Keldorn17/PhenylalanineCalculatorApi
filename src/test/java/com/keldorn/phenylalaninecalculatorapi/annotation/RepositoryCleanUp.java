package com.keldorn.phenylalaninecalculatorapi.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.test.context.jdbc.Sql;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Sql(scripts = {"/sql/cleanup-db.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
public @interface RepositoryCleanUp {
}
