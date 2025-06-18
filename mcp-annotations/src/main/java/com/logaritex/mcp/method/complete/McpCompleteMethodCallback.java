package com.logaritex.mcp.method.complete;

import com.logaritex.mcp.method.logging.AbstractMcpLoggingConsumerMethodCallback;
import com.logaritex.mcp.method.logging.SyncMcpLoggingConsumerMethodCallback;
import io.modelcontextprotocol.server.McpAsyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.util.DeafaultMcpUriTemplateManagerFactory;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * @author han
 * @time 2025/6/18 1:55
 */

public class McpCompleteMethodCallback extends AbstractMcpCompleteMethodCallback
        implements BiFunction<McpAsyncServerExchange, McpSchema.CompleteRequest, Mono<McpSchema.CompleteResult>> {

    private McpCompleteMethodCallback(McpCompleteMethodCallback.Builder builder) {
        super(builder.method, builder.bean, builder.prompt, builder.uri, builder.uriTemplateManagerFactory);
        this.validateMethod(this.method);
    }


    /**
     * Validates that the method return type is compatible with the complete callback.
     * Merge synchronous and asynchronous verification logic
     *
     * @param method The method to validate
     * @see SyncMcpCompleteMethodCallback
     * @see AsyncMcpCompleteMethodCallback
     * @throws IllegalArgumentException if the return type is not compatible
     */
    @Override
    protected void validateReturnType(Method method) {
        // Merge logic for synchronous and asynchronous methods
        // 合并异步和同步的逻辑
        Class<?> returnType = method.getReturnType();

        boolean validReturnType = McpSchema.CompleteResult.class.isAssignableFrom(returnType)
                || McpSchema.CompleteResult.CompleteCompletion.class.isAssignableFrom(returnType) || List.class.isAssignableFrom(returnType)
                || String.class.isAssignableFrom(returnType) || Mono.class.isAssignableFrom(returnType)
                || McpSchema.CompleteResult.class.isAssignableFrom(returnType)
                || McpSchema.CompleteResult.CompleteCompletion.class.isAssignableFrom(returnType) || List.class.isAssignableFrom(returnType)
                || String.class.isAssignableFrom(returnType);

        if (!validReturnType) {
            throw new IllegalArgumentException(
                    "Method must return either CompleteResult, CompleteCompletion, List<String>, "
                            + "String, or Mono<T>: " + method.getName() + " in " + method.getDeclaringClass().getName()
                            + " returns " + returnType.getName());
		}
    }

    /**
     * Checks if a parameter type is compatible with the exchange type. This method should
     * be implemented by subclasses to handle specific exchange type checking.
     *
     * @param paramType The parameter type to check
     * @return true if the parameter type is compatible with the exchange type, false
     * otherwise
     */
    @Override
    protected boolean isExchangeType(Class<?> paramType) {
        // No exchange type for logging consumer methods
        return false;
    }

    /**
     * Applies this function to the given arguments.
     *
     * @param mcpAsyncServerExchange the first function argument
     * @param completeRequest        the second function argument
     * @return the function result
     */
    @Override
    public Mono<McpSchema.CompleteResult> apply(McpAsyncServerExchange exchange, McpSchema.CompleteRequest request) {
        if (request == null) {
            return Mono.error(new IllegalArgumentException("Request must not be null"));
        }

        try {
            // Build arguments for the method call
            Object[] args = this.buildArgs(this.method, exchange, request);

            // Invoke the method
            this.method.setAccessible(true);
            Object result = this.method.invoke(this.bean, args);

            // Convert the result to a CompleteResult
            return convertToCompleteResultMono(result);
        }
        catch (Exception e) {
            return Mono
                    .error(new McpCompleteMethodException("Error invoking complete method: " + this.method.getName(), e));
        }
    }


    /**
     * Converts the method result to a Mono<CompleteResult>.
     * @param result The method result
     * @return A Mono that emits the CompleteResult
     */
    private Mono<McpSchema.CompleteResult> convertToCompleteResultMono(Object result) {
        if (result == null) {
            return Mono.just(new McpSchema.CompleteResult(new McpSchema.CompleteResult.CompleteCompletion(List.of(), 0, false)));
        }

        if (result instanceof Mono) {
            return ((Mono<?>) result).map(this::convertToCompleteResult);
        }

        return Mono.just(convertToCompleteResult(result));
    }

    /**
     * Converts the method result to a Mono<CompleteResult>.
     * @param result The method result
     * @return A Mono that emits the CompleteResult
     */
    private McpSchema.CompleteResult convertToCompleteResult(Object result) {

        if (result == null) {
            return new McpSchema.CompleteResult(new McpSchema.CompleteResult.CompleteCompletion(List.of(), 0, false));
        }

        if (result instanceof McpSchema.CompleteResult) {
            return (McpSchema.CompleteResult) result;
        }

        if (result instanceof McpSchema.CompleteResult.CompleteCompletion) {
            return new McpSchema.CompleteResult((McpSchema.CompleteResult.CompleteCompletion) result);
        }

        if (result instanceof List) {
            List<?> list = (List<?>) result;
            List<String> values = new ArrayList<>();

            for (Object item : list) {
                if (item instanceof String) {
                    values.add((String) item);
                }
                else {
                    throw new IllegalArgumentException("List items must be of type String");
                }
            }

            return new McpSchema.CompleteResult(new McpSchema.CompleteResult.CompleteCompletion(values, values.size(), false));
        }

        if (result instanceof String) {
            return new McpSchema.CompleteResult(new McpSchema.CompleteResult.CompleteCompletion(List.of((String) result), 1, false));
        }

        throw new IllegalArgumentException("Unsupported return type: " + result.getClass().getName());
    }



    /**
     * Create a new builder.
     * @return A new builder instance
     */
    public static McpCompleteMethodCallback.Builder builder() {
        return new McpCompleteMethodCallback.Builder();
    }


    /**
     * Builder for creating SyncMcpCompleteMethodCallback instances.
     * <p>
     * This builder provides a fluent API for constructing SyncMcpCompleteMethodCallback
     * instances with the required parameters.
     */
    public static class Builder extends AbstractBuilder<McpCompleteMethodCallback.Builder, McpCompleteMethodCallback> {

        /**
         * Constructor for Builder.
         */
        public Builder() {
            this.uriTemplateManagerFactory = new DeafaultMcpUriTemplateManagerFactory();
        }

        /**
         * Build the callback.
         * @return A new SyncMcpCompleteMethodCallback instance
         */
        @Override
        public McpCompleteMethodCallback build() {
            validate();
            return new McpCompleteMethodCallback(this);
        }

    }



}
