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

package org.springframework.boot.actuate.autoconfigure.endpoint.condition;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.EndpointExtension;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.env.Environment;

/**
 * {@link Conditional @Conditional} that checks whether an endpoint is available. An
 * endpoint is considered available if it is both enabled and exposed. Matches enablement
 * according to the endpoints specific {@link Environment} property, falling back to
 * {@code management.endpoints.enabled-by-default} or failing that
 * {@link Endpoint#enableByDefault()}. Matches exposure according to any of the
 * {@code management.endpoints.web.exposure.<id>} or
 * {@code management.endpoints.jmx.exposure.<id>} specific properties or failing that to
 * whether the application runs on
 * {@link org.springframework.boot.cloud.CloudPlatform#CLOUD_FOUNDRY}. Both those
 * conditions should match for the endpoint to be considered available.
 * <p>
 * When placed on a {@code @Bean} method, the endpoint defaults to the return type of the
 * factory method:
 *
 * <pre class="code">
 * &#064;Configuration
 * public class MyConfiguration {
 *
 *     &#064;ConditionalOnAvailableEndpoint
 *     &#064;Bean
 *     public MyEndpoint myEndpoint() {
 *         ...
 *     }
 *
 * }</pre>
 * <p>
 * It is also possible to use the same mechanism for extensions:
 *
 * <pre class="code">
 * &#064;Configuration
 * public class MyConfiguration {
 *
 *     &#064;ConditionalOnAvailableEndpoint
 *     &#064;Bean
 *     public MyEndpointWebExtension myEndpointWebExtension() {
 *         ...
 *     }
 *
 * }</pre>
 * <p>
 * In the sample above, {@code MyEndpointWebExtension} will be created if the endpoint is
 * available as defined by the rules above. {@code MyEndpointWebExtension} must be a
 * regular extension that refers to an endpoint, something like:
 *
 * <pre class="code">
 * &#064;EndpointWebExtension(endpoint = MyEndpoint.class)
 * public class MyEndpointWebExtension {
 *
 * }</pre>
 * <p>
 * Alternatively, the target endpoint can be manually specified for components that should
 * only be created when a given endpoint is available:
 *
 * <pre class="code">
 * &#064;Configuration
 * public class MyConfiguration {
 *
 *     &#064;ConditionalOnAvailableEndpoint(endpoint = MyEndpoint.class)
 *     &#064;Bean
 *     public MyComponent myComponent() {
 *         ...
 *     }
 *
 * }</pre>
 *
 * @author Brian Clozel
 * @author Stephane Nicoll
 * @since 2.2.0
 * @see Endpoint
 */
//Conditional注解，只有当指定类型的endpoint 可用的时候才能匹配，可用包括enabled和在端点暴露，
// enabled 查找顺序（从高到低）：
//1.首先返回management.endpoint.<name>.enabled这个配置属性的值；
//2.如果没有配置management.endpoint.<name>.enabled，则返回management.endpoints.enabled-by-default属性的值
//3.如果management.endpoints.enabled-by-default属性也没有配置，则返回标注在endpoint类上的@Endpoint或者
// @EndpointExtension的enableByDefault属性（默认为true）
//本端点是否暴露：获取management.endpoints.web.exposure.include、management.endpoints.web.exposure.exclude、
//management.endpoints.jmx.exposure.include、management.endpoints.jmx.exposure.exclude属性判断本endpoint是否暴露
//（web和jmx只要有一个暴露就暴露）

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@Documented
@Conditional(OnAvailableEndpointCondition.class)
public @interface ConditionalOnAvailableEndpoint {

	/**
	 * The endpoint type that should be checked. Inferred when the return type of the
	 * {@code @Bean} method is either an {@link Endpoint @Endpoint} or an
	 * {@link EndpointExtension @EndpointExtension}.
	 * @return the endpoint type to check
	 */
	Class<?> endpoint() default Void.class;

}
