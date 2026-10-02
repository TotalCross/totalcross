// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.ui.image;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import totalcross.sys.runtime.RuntimeFeatureState;
import totalcross.sys.runtime.RuntimeWhen;

/**
 * Declares typed Image runtime options for one selector on an application entry class.
 *
 * <p>Properties compose independently across matching rules. For example:
 *
 * <pre>
 * &#64;RuntimeConfiguration
 * &#64;ImageRuntimeRule(
 *     when = &#64;RuntimeWhen(platform = Platform.WINDOWS),
 *     scrollRasterReuse = RuntimeFeatureState.ENABLED,
 *     prefetchWorker = ImagePrefetchWorkerMode.SEMAPHORE_PROCESS_WORKER)
 * public class MyApp extends MainWindow { }
 * </pre>
 *
 * <p>Rules compose each property independently:
 *
 * <pre>
 * &#64;RuntimeConfiguration
 * &#64;ImageRuntimeRules({
 *     &#64;ImageRuntimeRule(
 *         when = &#64;RuntimeWhen(allOf = {@code @RuntimeCondition(family = RuntimeFamily.DESKTOP)}),
 *         storage = ImageStorageProfile.COMPACT),
 *     &#64;ImageRuntimeRule(
 *         when = &#64;RuntimeWhen(allOf = {@code @RuntimeCondition(platform = Platform.WINDOWS)}),
 *         scrollRasterReuse = RuntimeFeatureState.ENABLED)
 * })
 * public class MyApp extends MainWindow { }
 * </pre>
 *
 * <p>A rule must explicitly assign at least one option.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
@Repeatable(ImageRuntimeRules.class)
public @interface ImageRuntimeRule {
  RuntimeWhen when();

  ImageStorageProfile storage() default ImageStorageProfile.DEFAULT;

  RuntimeFeatureState targetColorConversion() default RuntimeFeatureState.DEFAULT;

  RuntimeFeatureState physicalVariantCache() default RuntimeFeatureState.DEFAULT;

  RuntimeFeatureState scrollRasterReuse() default RuntimeFeatureState.DEFAULT;

  ImagePrefetchWorkerMode prefetchWorker() default ImagePrefetchWorkerMode.DEFAULT;
}
