/*
 * Copyright 2025-2025 the original author or authors.
 */

package com.logaritex.mcp.method.prompt;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.BiFunction;

import com.logaritex.mcp.annotation.McpPrompt;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.GetPromptRequest;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 用于创建围绕提示方法的BiFunction回调的类。
 *
 * 该类提供了一种将使用{@link McpPrompt}注解的方法转换为可用于处理提示请求的回调函数的方式。
 * 它支持各种方法签名和返回类型。
 *
 * @author Christian Tzolov
 */
public final class SyncMcpPromptMethodCallback extends AbstractMcpPromptMethodCallback
		implements BiFunction<McpSyncServerExchange, GetPromptRequest, GetPromptResult> {

	Logger logger = LoggerFactory.getLogger(SyncMcpPromptMethodCallback.class);

	private SyncMcpPromptMethodCallback(Builder builder) {
		super(builder.method, builder.bean, builder.prompt);
	}

	/**
	 * Apply the callback to the given exchange and request.
	 * <p>
	 * This method builds the arguments for the method call, invokes the method, and
	 * converts the result to a GetPromptResult.
	 * @param exchange The server exchange, may be null if the method doesn't require it
	 * @param request The prompt request, must not be null
	 * @return The prompt result
	 * @throws McpPromptMethodException if there is an error invoking the prompt method
	 * @throws IllegalArgumentException if the request is null
	 */
	@Override
	public GetPromptResult apply(McpSyncServerExchange exchange, GetPromptRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("Request must not be null");
		}

		try {
			// Build arguments for the method call
			Object[] args = this.buildArgs(this.method, exchange, request);

			// Invoke the method
			this.method.setAccessible(true);
			Object result = this.method.invoke(this.bean, args);

			// Convert the result to a GetPromptResult
			GetPromptResult promptResult = this.convertToGetPromptResult(result);

			return promptResult;
		}
		catch (Exception e) {
			throw new McpPromptMethodException("Error invoking prompt method: " + this.method.getName(), e);
		}
	}

	/**
	 * Validates that the method return type is compatible with the prompt callback.
	 * @param method The method to validate
	 * @throws IllegalArgumentException if the return type is not compatible
	 */
	@Override
	protected boolean isExchangeType(Class<?> paramType) {
		return McpSyncServerExchange.class.isAssignableFrom(paramType);
	}

	@Override
	protected void validateReturnType(Method method) {
		Class<?> returnType = method.getReturnType();

		boolean validReturnType = GetPromptResult.class.isAssignableFrom(returnType)
				|| List.class.isAssignableFrom(returnType) || PromptMessage.class.isAssignableFrom(returnType)
				|| String.class.isAssignableFrom(returnType);

		if (!validReturnType) {
			throw new IllegalArgumentException("Method must return either GetPromptResult, List<PromptMessage>, "
					+ "List<String>, PromptMessage, or String: " + method.getName() + " in "
					+ method.getDeclaringClass().getName() + " returns " + returnType.getName());
		}
	}

	/**
	 * Builder for creating SyncMcpPromptMethodCallback instances.
	 * <p>
	 * This builder provides a fluent API for constructing SyncMcpPromptMethodCallback
	 * instances with the required parameters.
	 */
	public static class Builder extends AbstractBuilder<Builder, SyncMcpPromptMethodCallback> {

		/**
		 * Build the callback.
		 * @return A new SyncMcpPromptMethodCallback instance
		 */
		@Override
		public SyncMcpPromptMethodCallback build() {
			validate();
			return new SyncMcpPromptMethodCallback(this);
		}

	}

	/**
	 * Create a new builder.
	 * @return A new builder instance
	 */
	public static Builder builder() {
		return new Builder();
	}

}
