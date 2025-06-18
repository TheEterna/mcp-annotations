package com.logaritex.mcp.method.prompt;

import io.modelcontextprotocol.server.McpAsyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.BiFunction;

/**
 * @author han
 * @time 2025/6/18 1:56
 */

public class McpPromptMethodCallback extends AbstractMcpPromptMethodCallback
        implements BiFunction<McpAsyncServerExchange, McpSchema.GetPromptRequest, Mono<McpSchema.GetPromptResult>> {

    /**
     * Constructor for AbstractMcpPromptMethodCallback.
     *
     * @param builder The builder to use for building the callback
     */
    private McpPromptMethodCallback(McpPromptMethodCallback.Builder builder) {
        super(builder.method, builder.bean, builder.prompt);
    }

    /**
     * Validates that the method return type is compatible with the prompt callback.
     *
     * @param method The method to validate
     * @throws IllegalArgumentException if the return type is not compatible
     */
    @Override
    protected void validateReturnType(Method method) {
        // Merge logic for synchronous and asynchronous methods
        // 合并异步和同步的逻辑
        Class<?> returnType = method.getReturnType();

        boolean validReturnType = McpSchema.GetPromptResult.class.isAssignableFrom(returnType)
                || List.class.isAssignableFrom(returnType) || McpSchema.PromptMessage.class.isAssignableFrom(returnType)
                || String.class.isAssignableFrom(returnType)
                || Mono.class.isAssignableFrom(returnType);

        if (!validReturnType) {
            throw new IllegalArgumentException(
                    "Method must return either GetPromptResult, PromptMessage, List<String>, "
                            + "String, or Mono<T>: " + method.getName() + " in " + method.getDeclaringClass().getName()
                            + " returns " + returnType.getName());
        }
    }

    /**
     * Checks if a parameter type is compatible with the exchange type.
     *
     * @param paramType The parameter type to check
     * @return true if the parameter type is compatible with the exchange type, false
     * otherwise
     */
    @Override
    protected boolean isExchangeType(Class<?> paramType) {
        return false;
    }

    /**
     * Apply the callback to the given exchange and request.
     * <p>
     * This method builds the arguments for the method call, invokes the method, and
     * converts the result to a GetPromptResult.
     * @param exchange The server exchange, may be null if the method doesn't require it
     * @param request The prompt request, must not be null
     * @return A Mono that emits the prompt result
     * @throws McpPromptMethodException if there is an error invoking the prompt method
     * @throws IllegalArgumentException if the request is null
     */
    @Override
    public Mono<McpSchema.GetPromptResult> apply(McpAsyncServerExchange exchange, McpSchema.GetPromptRequest request) {
        if (request == null) {
            return Mono.error(new IllegalArgumentException("Request must not be null"));
        }

        return Mono.defer(() -> {
            try {
                // Build arguments for the method call
                Object[] args = this.buildArgs(this.method, exchange, request);

                // Invoke the method
                this.method.setAccessible(true);
                Object result = this.method.invoke(this.bean, args);

                // Handle the result based on its type
                if (result instanceof Mono<?>) {
                    // If the result is already a Mono, map it to a GetPromptResult
                    return ((Mono<?>) result).map(this::convertToGetPromptResult);
                }
                else {
                    // Otherwise, convert the result to a GetPromptResult and wrap in a
                    // Mono
                    return Mono.just(convertToGetPromptResult(result));
                }
            }
            catch (Exception e) {
                return Mono
                        .error(new McpPromptMethodException("Error invoking prompt method: " + this.method.getName(), e));
            }
        });
    }

    /**
     * Builder for creating SyncMcpPromptMethodCallback instances.
     * <p>
     * This builder provides a fluent API for constructing SyncMcpPromptMethodCallback
     * instances with the required parameters.
     */
    public static class Builder extends AbstractBuilder<McpPromptMethodCallback.Builder, McpPromptMethodCallback> {

        /**
         * Build the callback.
         * @return A new SyncMcpPromptMethodCallback instance
         */
        @Override
        public McpPromptMethodCallback build() {
            validate();
            return new McpPromptMethodCallback(this);
        }

    }

    /**
     * Create a new builder.
     * @return A new builder instance
     */
    public static McpPromptMethodCallback.Builder builder() {
        return new McpPromptMethodCallback.Builder();
    }
}
