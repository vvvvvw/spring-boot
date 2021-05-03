/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.actuate.endpoint.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A {@code @Selector} can be used on a parameter of an {@link Endpoint @Endpoint} method
 * to indicate that the parameter is used to select a subset of the endpoint's data.
 * <p>
 * A {@code @Selector} may change the way that the endpoint is exposed to the user. For
 * example, HTTP mapped endpoints will map select parameters to path variables.
 *
 * @author Andy Wilkinson
 * @since 2.0.0
 */
//使用@Selector注解操作方法的一个或多个参数，可以进一步endpoint定制路径，这样的参数作为路径变量添加到路径谓词，
// 当调用端点操作时，将变量的值传递给操作方法
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Selector {

	/**
	 * The match type that should be used for the selection.
	 * @return the match type
	 * @since 2.2.0
	 */
	Match match() default Match.SINGLE;

	/**
	 * Match types that can be used with the {@code @Selector}.
	 */
	enum Match {

		/**
		 * Capture a single item. For example, in the case of a web application a single
		 * path segment. The parameter value be converted from a {@code String} source.
		 */
		//只提取一个 path中的一段路径作为 参数值
		SINGLE,

		/**
		 * Capture all remaining times. For example, in the case of a web application all
		 * remaining path segments. The parameter value be converted from a
		 * {@code String[]} source.
		 */
		//将所有未解析的剩余path都提取出来 作为参数值（string[]），注意，注解值为Match.ALL_REMAINING的@Selector 参数必须放在
		//所有 @Selector 的最后一个
		ALL_REMAINING

	}

}
