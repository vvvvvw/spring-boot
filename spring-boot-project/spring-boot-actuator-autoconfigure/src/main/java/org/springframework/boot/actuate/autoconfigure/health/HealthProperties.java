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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Properties used to configure the health endpoint and endpoint groups.
 *
 * @author Stephane Nicoll
 * @author Phillip Webb
 * @since 2.2.0
 */
public abstract class HealthProperties {

	@NestedConfigurationProperty
	private final Status status = new Status();

	/**
	 * When to show components. If not specified the 'show-details' setting will be used.
	 */
	//在返回响应的时候是否应该展示 本group中所有的健康指示器 分别的健康信息
	private Show showComponents;

	/**
	 * Roles used to determine whether or not a user is authorized to be shown details.
	 * When empty, all authenticated users are authorized.
	 */
	//当showComponents或者showDetails的认证方式是Show.WHEN_AUTHORIZED(认证用户才能查看details或者各个子健康信息的时候)
	//roles就用来表示 符合条件的认证用户
	private Set<String> roles = new HashSet<>();

	public Status getStatus() {
		return this.status;
	}

	public Show getShowComponents() {
		return this.showComponents;
	}

	public void setShowComponents(Show showComponents) {
		this.showComponents = showComponents;
	}

	public abstract Show getShowDetails();

	public Set<String> getRoles() {
		return this.roles;
	}

	public void setRoles(Set<String> roles) {
		this.roles = roles;
	}

	/**
	 * Status properties for the group.
	 */
	public static class Status {

		/**
		 * Comma-separated list of health statuses in order of severity.
		 */
		private List<String> order = new ArrayList<>();

		/**
		 * Mapping of health statuses to HTTP status codes. By default, registered health
		 * statuses map to sensible defaults (for example, UP maps to 200).
		 */
		private final Map<String, Integer> httpMapping = new HashMap<>();

		public List<String> getOrder() {
			return this.order;
		}

		public void setOrder(List<String> statusOrder) {
			if (statusOrder != null && !statusOrder.isEmpty()) {
				this.order = statusOrder;
			}
		}

		public Map<String, Integer> getHttpMapping() {
			return this.httpMapping;
		}

	}

	/**
	 * Options for showing items in responses from the {@link HealthEndpoint} web
	 * extensions.
	 */
	public enum Show {

		/**
		 * Never show the item in the response.
		 */
		NEVER,

		/**
		 * Show the item in the response when accessed by an authorized user.
		 */
		WHEN_AUTHORIZED,

		/**
		 * Always show the item in the response.
		 */
		ALWAYS

	}

}
