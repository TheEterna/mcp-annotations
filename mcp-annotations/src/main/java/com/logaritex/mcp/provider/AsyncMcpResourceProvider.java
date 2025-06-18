/*
 * Copyright 2025-2025 the original author or authors.
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

package com.logaritex.mcp.provider;

import com.logaritex.mcp.annotation.McpResource;
import com.logaritex.mcp.method.resource.AsyncMcpResourceMethodCallback;
import com.logaritex.mcp.method.resource.McpResourceMethodCallback;
import io.modelcontextprotocol.server.McpServerFeatures.AsyncResourceSpecification;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.util.Assert;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author han
 */

public class AsyncMcpResourceProvider {

	private final List<Object> resourceObjects;

	public AsyncMcpResourceProvider(List<Object> resourceObjects) {
		Assert.notNull(resourceObjects, "resourceObjects cannot be null");
		this.resourceObjects = resourceObjects;
	}

	public List<AsyncResourceSpecification> getResourceSpecifications() {

		List<AsyncResourceSpecification> methodCallbacks = this.resourceObjects.stream()
			.map(resourceObject -> Stream.of(doGetClassMethods(resourceObject))
				.filter(resourceMethod -> resourceMethod.isAnnotationPresent(McpResource.class))
				.map(mcpResourceMethod -> {
					McpResource resourceAnnotation = mcpResourceMethod.getAnnotation(McpResource.class);

					String uri = resourceAnnotation.uri();
					String name = getName(mcpResourceMethod, resourceAnnotation);
					String description = resourceAnnotation.description();
					String mimeType = resourceAnnotation.mimeType();
					var mcpResource = new McpSchema.Resource(uri, name, description, mimeType, null);

					McpResourceMethodCallback methodCallback = McpResourceMethodCallback.builder()
						.method(mcpResourceMethod)
						.bean(resourceObject)
						.resource(mcpResource)
						.build();

					return new AsyncResourceSpecification(mcpResource, methodCallback);
				})
				.toList())
			.flatMap(List::stream)
			.toList();

		return methodCallbacks;
	}

	/**
	 * Returns the methods of the given bean class.
	 * @param bean the bean instance
	 * @return the methods of the bean class
	 */
	protected Method[] doGetClassMethods(Object bean) {
		return bean.getClass().getDeclaredMethods();
	}

	private static String getName(Method method, McpResource resource) {
		Assert.notNull(method, "method cannot be null");
		if (resource == null || resource.name() == null || resource.name().isEmpty()) {
			return method.getName();
		}
		return resource.name();
	}

}
