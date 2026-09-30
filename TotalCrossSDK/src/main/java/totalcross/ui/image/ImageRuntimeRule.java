// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.ui.image;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import totalcross.sys.runtime.RuntimeWhen;

/** Declares a typed Image storage request for one runtime selector on an application entry class. */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
@Repeatable(ImageRuntimeRules.class)
public @interface ImageRuntimeRule {
  RuntimeWhen when();

  ImageStorageProfile storage();
}
