/*
 * Copyright 2012-2020 the original author or authors.
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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.actuate.autoconfigure.endpoint.expose.IncludeExcludeEndpointFilter;
import org.springframework.boot.actuate.autoconfigure.endpoint.expose.IncludeExcludeEndpointFilter.DefaultIncludes;
import org.springframework.boot.actuate.endpoint.EndpointId;
import org.springframework.boot.actuate.endpoint.ExposableEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionMessage;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.cloud.CloudPlatform;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.ConcurrentReferenceHashMap;

/**
 * A condition that checks if an endpoint is available (i.e. enabled and exposed).
 *
 * @author Brian Clozel
 * @author Stephane Nicoll
 * @author Phillip Webb
 * @see ConditionalOnAvailableEndpoint
 */
//只有当指定类型的endpoint 可用的时候才能匹配，可用包括enabled和在端点暴露，
// enabled 查找顺序（从高到低）：
//1.首先返回management.endpoint.<name>.enabled这个配置属性的值；
//2.如果没有配置management.endpoint.<name>.enabled，则返回management.endpoints.enabled-by-default属性的值
//3.如果management.endpoints.enabled-by-default属性也没有配置，则返回标注在endpoint类上的@Endpoint或者
// @EndpointExtension的enableByDefault属性（默认为true）
//在指定端点暴露：获取management.endpoints.web.exposure.include、management.endpoints.web.exposure.exclude、
//		//management.endpoints.jmx.exposure.include、management.endpoints.jmx.exposure.exclude属性判断本endpoint是否暴露
//（web和jmx只要有一个暴露就暴露）
class OnAvailableEndpointCondition extends AbstractEndpointCondition {

	private static final String JMX_ENABLED_KEY = "spring.jmx.enabled";

	private static final Map<Environment, Set<Exposure>> exposuresCache = new ConcurrentReferenceHashMap<>();

	@Override
	public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
		//判断 端点是否enable
		//查找顺序（从高到低）：
//1.首先返回management.endpoint.<name>.enabled这个配置属性的值；
//2.如果没有配置management.endpoint.<name>.enabled，则返回management.endpoints.enabled-by-default属性的值
//3.如果management.endpoints.enabled-by-default属性也没有配置，则返回标注在endpoint类上的@Endpoint或者
		ConditionOutcome enablementOutcome = getEnablementOutcome(context, metadata,
				ConditionalOnAvailableEndpoint.class);
		if (!enablementOutcome.isMatch()) {
			return enablementOutcome;
		}
		ConditionMessage message = enablementOutcome.getConditionMessage();
		Environment environment = context.getEnvironment();
		//CLOUD_FOUNDRY云环境 特殊处理
		if (CloudPlatform.CLOUD_FOUNDRY.isActive(environment)) {
			return new ConditionOutcome(true, message.andCondition(ConditionalOnAvailableEndpoint.class)
					.because("application is running on Cloud Foundry"));
		}
		EndpointId id = EndpointId.of(environment,
				getEndpointAttributes(ConditionalOnAvailableEndpoint.class, context, metadata).getString("id"));
		//获取management.endpoints.web.exposure.include、management.endpoints.web.exposure.exclude、
		//management.endpoints.jmx.exposure.include、management.endpoints.jmx.exposure.exclude属性判断本endpoint是否暴露
		Set<Exposure> exposures = getExposures(environment);
		for (Exposure exposure : exposures) {
			if (exposure.isExposed(id)) {
				return new ConditionOutcome(true,
						message.andCondition(ConditionalOnAvailableEndpoint.class)
								.because("marked as exposed by a 'management.endpoints." + exposure.getPrefix()
										+ ".exposure' property"));
			}
		}
		return new ConditionOutcome(false, message.andCondition(ConditionalOnAvailableEndpoint.class)
				.because("no 'management.endpoints' property marked it as exposed"));
	}

	private Set<Exposure> getExposures(Environment environment) {
		Set<Exposure> exposures = exposuresCache.get(environment);
		if (exposures == null) {
			exposures = new HashSet<>(2);
			//如果 spring.jmx.enabled为true
			if (environment.getProperty(JMX_ENABLED_KEY, Boolean.class, false)) {
				//management.endpoints.web.exposure.include 和 management.endpoints.web.exposure.exclude属性
				exposures.add(new Exposure(environment, "jmx", DefaultIncludes.JMX));
			}
			//management.endpoints.jmx.exposure.include 和 management.endpoints.jmx.exposure.exclude属性
			exposures.add(new Exposure(environment, "web", DefaultIncludes.WEB));
			exposuresCache.put(environment, exposures);
		}
		return exposures;
	}

	static class Exposure extends IncludeExcludeEndpointFilter<ExposableEndpoint<?>> {

		private final String prefix;

		@SuppressWarnings({ "rawtypes", "unchecked" })
		Exposure(Environment environment, String prefix, DefaultIncludes defaultIncludes) {
			super((Class) ExposableEndpoint.class, environment, "management.endpoints." + prefix + ".exposure",
					defaultIncludes);
			this.prefix = prefix;
		}

		String getPrefix() {
			return this.prefix;
		}

		boolean isExposed(EndpointId id) {
			return super.match(id);
		}

	}

}
