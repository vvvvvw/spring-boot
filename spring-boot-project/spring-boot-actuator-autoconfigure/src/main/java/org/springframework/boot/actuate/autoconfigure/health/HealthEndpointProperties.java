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

package org.springframework.boot.actuate.autoconfigure.health;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for {@link HealthEndpoint}.
 *
 * @author Phillip Webb
 * @author Leo Li
 * @since 2.0.0
 */
@ConfigurationProperties("management.endpoint.health")
public class HealthEndpointProperties extends HealthProperties {

	/**
	 * When to show full health details.
	 */
	//在返回响应的时候是否应该展示 详情，还是说只展示status
	private Show showDetails = Show.NEVER;

	/**
	 * Health endpoint groups.
	 */
	//分组  key:组名  value：每个组的展示属性
	private Map<String, Group> group = new LinkedHashMap<>();

	@Override
	public Show getShowDetails() {
		return this.showDetails;
	}

	public void setShowDetails(Show showDetails) {
		this.showDetails = showDetails;
	}

	public Map<String, Group> getGroup() {
		return this.group;
	}

	/**
	 * A health endpoint group.
	 */
	//每个 组的展示信息
	public static class Group extends HealthProperties {

		/**
		 * Health indicator IDs that should be included or '*' for all.
		 */
		//本组中包含哪些 健康指示器，value：set<健康指示器的名字，就是beanname>，如果是所有健康指示器的话，就用*
		private Set<String> include;

		/**
		 * Health indicator IDs that should be excluded or '*' for all.
		 */
		//本组中需要排除哪些 健康指示器，value：set<健康指示器的名字，就是beanname>，如果是所有健康指示器的话，就用*
		private Set<String> exclude;

		/**
		 * When to show full health details. Defaults to the value of
		 * 'management.endpoint.health.show-details'.
		 */
		//是否展示详情
		private Show showDetails;

		public Set<String> getInclude() {
			return this.include;
		}

		public void setInclude(Set<String> include) {
			this.include = include;
		}

		public Set<String> getExclude() {
			return this.exclude;
		}

		public void setExclude(Set<String> exclude) {
			this.exclude = exclude;
		}

		@Override
		public Show getShowDetails() {
			return this.showDetails;
		}

		public void setShowDetails(Show showDetails) {
			this.showDetails = showDetails;
		}

	}

}
