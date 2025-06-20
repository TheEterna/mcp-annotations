/*
* Copyright 2025 - 2025 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* https://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.logaritex.mcp.spring;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import com.logaritex.mcp.provider.*;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpServerFeatures.AsyncResourceSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.AsyncPromptSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncPromptSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.AsyncCompletionSpecification;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageRequest;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageResult;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import reactor.core.publisher.Mono;

import org.springframework.aop.support.AopUtils;
import org.springframework.util.ReflectionUtils;

/**
 * @author Christian Tzolov
 */
public class AsyncMcpAnnotationProvider {

	private static class SpringAiAsyncMcpLoggingConsumerProvider extends AsyncMcpLoggingConsumerProvider {

		public SpringAiAsyncMcpLoggingConsumerProvider(List<Object> loggingObjects) {
			super(loggingObjects);
		}

		@Override
		protected Method[] doGetClassMethods(Object bean) {
			return ReflectionUtils
				.getDeclaredMethods(AopUtils.isAopProxy(bean) ? AopUtils.getTargetClass(bean) : bean.getClass());
		}

	}

	private static class SpringAiAsyncMcpSamplingProvider extends AsyncMcpSamplingProvider {

		public SpringAiAsyncMcpSamplingProvider(List<Object> samplingObjects) {
			super(samplingObjects);
		}

		@Override
		protected Method[] doGetClassMethods(Object bean) {
			return ReflectionUtils
					.getDeclaredMethods(AopUtils.isAopProxy(bean) ? AopUtils.getTargetClass(bean) : bean.getClass());
		}

	}

	private static class SpringAiAsyncMcpResourceProvider extends McpResourceProvider {

		public SpringAiAsyncMcpResourceProvider(List<Object> resourceObjects) {
			super(resourceObjects);
		}

		@Override
		protected Method[] doGetClassMethods(Object bean) {
			Method[] methods = ReflectionUtils
					.getDeclaredMethods(AopUtils.isAopProxy(bean) ? AopUtils.getTargetClass(bean) : bean.getClass());
			Arrays.sort(methods, Comparator
					.comparing(Method::getName)
					.thenComparing(method -> Arrays.toString(method.getParameterTypes())));
			return methods;
		}

	}

	private static class SpringAiMcpPromptProvider extends McpPromptProvider {

		public SpringAiMcpPromptProvider(List<Object> promptObjects) {
			super(promptObjects);
		}

		@Override
		protected Method[] doGetClassMethods(Object bean) {
			Method[] methods = ReflectionUtils
					.getDeclaredMethods(AopUtils.isAopProxy(bean) ? AopUtils.getTargetClass(bean) : bean.getClass());
			Arrays.sort(methods, Comparator
					.comparing(Method::getName)
					.thenComparing(method -> Arrays.toString(method.getParameterTypes())));
			return methods;
		}

	}

	private static class SpringAiAsyncMcpCompletionProvider extends McpCompletionProvider {

		public SpringAiAsyncMcpCompletionProvider(List<Object> completionObjects) {
			super(completionObjects);
		}

		@Override
		protected Method[] doGetClassMethods(Object bean) {
			Method[] methods = ReflectionUtils
					.getDeclaredMethods(AopUtils.isAopProxy(bean) ? AopUtils.getTargetClass(bean) : bean.getClass());
			Arrays.sort(methods, Comparator
					.comparing(Method::getName)
					.thenComparing(method -> Arrays.toString(method.getParameterTypes())));
			return methods;
		}

	}

	/**
	 * fixme 未更新, 存在问题, 同步提供器无法解析Mono
	 * @param loggingObjects
	 * @return
	 */
	public static List<Function<LoggingMessageNotification, Mono<Void>>> createAsyncLoggingConsumers(
			List<Object> loggingObjects) {
		return new SpringAiAsyncMcpLoggingConsumerProvider(loggingObjects).getLoggingConsumers();
	}

	/**
	 * fixme 未更新, 存在问题, 同步提供器无法解析Mono
	 * @param samplingObjects
	 * @return
	 */

	public static Function<CreateMessageRequest, Mono<CreateMessageResult>> createAsyncSamplingHandler(
			List<Object> samplingObjects) {
		return new SpringAiAsyncMcpSamplingProvider(samplingObjects).getSamplingHandler();
	}


	public static List<AsyncResourceSpecification> createAsyncResourceSpecifications(List<Object> resourceObjects) {
		return new SpringAiAsyncMcpResourceProvider(resourceObjects).getAsyncResourceSpecifications();
	}

	public static List<AsyncCompletionSpecification> createAsyncCompletionSpecifications(List<Object> completionObjects) {
		return new SpringAiAsyncMcpCompletionProvider(completionObjects).getAsyncCompleteSpecifications();
	}

	public static List<AsyncPromptSpecification> createAsyncPromptSpecifications(List<Object> promptObjects) {
		return new SpringAiMcpPromptProvider(promptObjects).getAsyncPromptSpecifications();
	}




}
