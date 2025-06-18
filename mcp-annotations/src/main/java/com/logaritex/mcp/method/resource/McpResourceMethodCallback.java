package com.logaritex.mcp.method.resource;

import io.modelcontextprotocol.server.McpAsyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * @author han
 * @time 2025/6/18 1:56
 */

public class McpResourceMethodCallback extends AbstractMcpResourceMethodCallback
        implements BiFunction<McpAsyncServerExchange, McpSchema.ReadResourceRequest, Mono<McpSchema.ReadResourceResult>> {

    private McpResourceMethodCallback(McpResourceMethodCallback.Builder builder) {
        super(builder.method, builder.bean, builder.uri, builder.name, builder.description, builder.mimeType,
                builder.resultConverter, builder.uriTemplateManagerFactory, builder.contentType);
        this.validateMethod(this.method);
    }

    /**
     * Validates that the method return type is compatible with the resource callback.
     * This method should be implemented by subclasses to handle specific return type
     * validation.
     *
     * @param method The method to validate
     * @throws IllegalArgumentException if the return type is not compatible
     */
    @Override
    protected void validateReturnType(Method method) {
        Class<?> returnType = method.getReturnType();

        boolean validReturnType = McpSchema.ReadResourceResult.class.isAssignableFrom(returnType)
                || List.class.isAssignableFrom(returnType) || McpSchema.ResourceContents.class.isAssignableFrom(returnType)
                || String.class.isAssignableFrom(returnType) || Mono.class.isAssignableFrom(returnType);

        if (!validReturnType) {
            throw new IllegalArgumentException(
                    "Method must return either ReadResourceResult, List<ResourceContents>, List<String>, "
                            + "ResourceContents, String, or Mono<T>: " + method.getName() + " in "
                            + method.getDeclaringClass().getName() + " returns " + returnType.getName());
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
        return false;
    }
    /**
     * Builder for creating AsyncMcpResourceMethodCallback instances.
     * <p>
     * This builder provides a fluent API for constructing AsyncMcpResourceMethodCallback
     * instances with the required parameters.
     */
    public static class Builder extends AbstractBuilder<McpResourceMethodCallback.Builder, McpResourceMethodCallback> {

        /**
         * Constructor for Builder.
         */
        public Builder() {
            this.resultConverter = new DefaultMcpReadResourceResultConverter();
        }

        /**
         * Build the callback.
         * @return A new AsyncMcpResourceMethodCallback instance
         */
        @Override
        public McpResourceMethodCallback build() {
            validate();
            return new McpResourceMethodCallback(this);
        }

    }

    /**
     * Create a new builder.
     * @return A new builder instance
     */
    public static McpResourceMethodCallback.Builder builder() {
        return new McpResourceMethodCallback.Builder();
    }

    /**
     * Applies this function to the given arguments.
     *
     * @param exchange the first function argument
     * @param request    the second function argument
     * @return the function result
     */
    @Override
    public Mono<McpSchema.ReadResourceResult> apply(McpAsyncServerExchange exchange, McpSchema.ReadResourceRequest request) {
        if (request == null) {
            return Mono.error(new IllegalArgumentException("Request must not be null"));
        }

        return Mono.defer(() -> {
            try {
                // Extract URI variable values from the request URI
                Map<String, String> uriVariableValues = this.uriTemplateManager.extractVariableValues(request.uri());

                // Verify all URI variables were extracted if URI variables are expected
                if (!this.uriVariables.isEmpty() && uriVariableValues.size() != this.uriVariables.size()) {
                    return Mono
                            .error(new IllegalArgumentException("Failed to extract all URI variables from request URI: "
                                    + request.uri() + ". Expected variables: " + this.uriVariables + ", but found: "
                                    + uriVariableValues.keySet()));
                }

                // Build arguments for the method call
                Object[] args = this.buildArgs(this.method, exchange, request, uriVariableValues);

                // Invoke the method
                this.method.setAccessible(true);
                Object result = this.method.invoke(this.bean, args);

                // Handle the result based on its type
                if (result instanceof Mono<?>) {
                    // If the result is already a Mono, use it
                    return ((Mono<?>) result).map(r -> this.resultConverter.convertToReadResourceResult(r,
                            request.uri(), this.mimeType, this.contentType));
                }
                else {
                    // Otherwise, convert the result to a ReadResourceResult and wrap in a
                    // Mono
                    return Mono.just(this.resultConverter.convertToReadResourceResult(result, request.uri(),
                            this.mimeType, this.contentType));
                }
            }
            catch (Exception e) {
                return Mono.error(
                        new McpResourceMethodException("Error invoking resource method: " + this.method.getName(), e));
            }
        });
    }

}
