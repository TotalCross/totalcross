// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.sys.runtime;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Declares one immutable runtime selector rule on an application entry class. */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
@Repeatable(RuntimeRules.class)
public @interface RuntimeRule {
  RuntimeWhen when();
}
