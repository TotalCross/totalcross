// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.sys.runtime;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import totalcross.sys.Architecture;
import totalcross.sys.GraphicsBackend;
import totalcross.sys.Platform;
import totalcross.sys.RuntimeFamily;

/** One conjunction of typed runtime dimensions; an array lists alternatives for that dimension. */
@Retention(RetentionPolicy.CLASS)
@Target({})
public @interface RuntimeCondition {
  Platform[] platform() default {};

  RuntimeFamily[] family() default {};

  GraphicsBackend[] backend() default {};

  Architecture[] architecture() default {};
}
