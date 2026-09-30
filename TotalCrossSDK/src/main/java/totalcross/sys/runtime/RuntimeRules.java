// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.sys.runtime;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Compiler container for repeatable {@link RuntimeRule} declarations. */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface RuntimeRules {
  RuntimeRule[] value();
}
