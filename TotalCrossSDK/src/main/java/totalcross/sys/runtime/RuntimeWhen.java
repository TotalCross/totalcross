// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.sys.runtime;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Combines required conditions with conjunction and optional alternatives with disjunction. */
@Retention(RetentionPolicy.CLASS)
@Target({})
public @interface RuntimeWhen {
  RuntimeCondition[] allOf() default {};

  RuntimeCondition[] anyOf() default {};
}
