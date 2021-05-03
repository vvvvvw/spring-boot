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

package org.springframework.boot.actuate.health;

import org.springframework.boot.actuate.endpoint.SecurityContext;

/**
 * A logical grouping of {@link HealthContributor health contributors} that can be exposed
 * by the {@link HealthEndpoint}.
 *
 * @author Phillip Webb
 * @since 2.2.0
 */
/*HealthEndpointGroup集成了这个分组的分组名、所使用的StatusAggregator、HttpCodeStatusMapper*/
public interface HealthEndpointGroup {

	/**
	 * Returns {@code true} if the given contributor is a member of this group.
	 * @param name the contributor name
	 * @return {@code true} if the contributor is a member of this group
	 */
	//通过 给定的 健康指示器 name判断是否属于本group
	boolean isMember(String name);

	/**
	 * Returns if {@link CompositeHealth#getComponents() health components} should be
	 * shown in the response.
	 * @param securityContext the endpoint security context
	 * @return {@code true} to shown details or {@code false} to hide them
	 */
	//是否应该展示 本group中所有的健康指示器 分别的健康信息
	boolean showComponents(SecurityContext securityContext);

	/**
	 * Returns if {@link Health#getDetails() health details} should be shown in the
	 * response.
	 * @param securityContext the endpoint security context
	 * @return {@code true} to shown details or {@code false} to hide them
	 */
	//是否应该在返回响应的时候展示detail信息
	boolean showDetails(SecurityContext securityContext);

	/**
	 * Returns the status aggregator that should be used for this group.
	 * @return the status aggregator for this group
	 */
	//获取本group 对应的 状态聚合器
	StatusAggregator getStatusAggregator();

	/**
	 * Returns the {@link HttpCodeStatusMapper} that should be used for this group.
	 * @return the HTTP code status mapper
	 */
	//获取本group对应的 HttpCodeStatusMapper
	HttpCodeStatusMapper getHttpCodeStatusMapper();

}
