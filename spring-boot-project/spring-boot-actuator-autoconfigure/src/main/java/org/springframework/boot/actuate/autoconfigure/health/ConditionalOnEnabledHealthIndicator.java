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

package org.springframework.boot.actuate.autoconfigure.health;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Conditional;

/**
 * {@link Conditional @Conditional} that checks whether or not a default health indicator
 * is enabled. Matches if the value of the {@code management.health.<name>.enabled}
 * property is {@code true}. Otherwise, matches if the value of the
 * {@code management.health.defaults.enabled} property is {@code true} or if it is not
 * configured.
 *
 * @author Stephane Nicoll
 * @since 2.0.0
 */
//Conditional注解，只有当启用了 指定类型的健康指示器的时候才能匹配，查找顺序(优先级从高到低)：
//1.首先返回management.health.<name>.enabled这个配置属性的值；
//2.如果没有配置management.health.<name>.enabled，则返回management.health.defaults.enabled属性的值
//3.如果management.health.defaults.enabled属性也没有配置，则返回true
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Documented
@Conditional(OnEnabledHealthIndicatorCondition.class)
public @interface ConditionalOnEnabledHealthIndicator {

	/**
	 * The name of the health indicator.
	 * @return the name of the health indicator
	 */
	String value();

}
