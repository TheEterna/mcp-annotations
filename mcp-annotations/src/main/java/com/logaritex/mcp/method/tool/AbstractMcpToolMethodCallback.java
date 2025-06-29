package com.logaritex.mcp.method.tool;

import com.logaritex.mcp.annotation.McpArg;
import com.logaritex.mcp.annotation.McpTool;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.util.Assert;
import reactor.util.annotation.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;

/**
 * Abstract base class for creating callbacks around tool methods.
 *
 * This class provides common functionality for both synchronous and asynchronous tool
 * method callbacks.
 *
 * @author han
 * @time 2025/6/25 11:39
 */
public abstract class AbstractMcpToolMethodCallback {
    protected final Method method;

    protected final Object bean;
    /**
     * The tool name. Unique within the tool set provided to a model.
     */
    protected final String name;

    /**
     * The tool description, used by the AI model to determine what the tool does.
     */
    protected final String description;

    /**
     * The schema of the parameters used to call the tool.
     */
    protected final String inputSchema;
    /**
     * The schema of the result returned by the tool.
     */
    protected final String outputSchema;
    /**
     * The mineType of the result returned by the tool.
     * <p>
     * The mineType is a string that describes the type of data returned by the tool.
     * It is used to determine how the data should be interpreted by the AI model.
     */
    protected final String mineType;
    /**
     * The annotations for the tool.
     */
    protected final McpSchema.ToolAnnotations annotations;

    /**
     *  The converter used to convert the tool method result to a CallToolResult.
     */
    protected final McpCallToolResultConverter converter;


    /**
     * Constructor for AbstractMcpToolMethodCallback.
     * @param method The method to create a callback for
     * @param bean The bean instance that contains the method
     */
    protected AbstractMcpToolMethodCallback(
            Method method,
            Object bean,
            String name,
            @Nullable String description,
            String inputSchema,
            @Nullable String outputSchema,
            @Nullable String mineType,
            @Nullable McpSchema.ToolAnnotations annotations,
            McpCallToolResultConverter converter
    ) {
        Assert.notNull(method, "Method can't be null!");
        Assert.notNull(bean, "Bean can't be null!");

        this.method = method;
        this.bean = bean;
        this.name = name;
        this.description = description;
        this.inputSchema = inputSchema;
        this.outputSchema = outputSchema;
        this.annotations = annotations;
        this.mineType = mineType;
        this.converter = converter;

        this.validateMethod(this.method);
    }

    /**
     * Validates that the method signature is compatible with the Tool callback.
     * <p>
     * This method checks that the return type is valid and that the parameters match the
     * expected pattern.
     * @param method The method to validate
     * @throws IllegalArgumentException if the method signature is not compatible
     */
    protected void validateMethod(Method method) {
        if (method == null) {
            throw new IllegalArgumentException("Method must not be null");
        }

        // 不需要校验, 也无法校验, 因为你无法确定用户的Result类
        // No need to validate, nor is it possible to validate, because you cannot determine the user's Result class
        this.validateParameters(method);
    }

    /**
     * Exception thrown when there is an error invoking a tool method.
     */
    public static class McpToolMethodException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        /**
         * Constructs a new exception with the specified detail message and cause.
         * @param message The detail message
         * @param cause The cause
         */
        public McpToolMethodException(String message, Throwable cause) {
            super(message, cause);
        }

        /**
         * Constructs a new exception with the specified detail message.
         * @param message The detail message
         */
        public McpToolMethodException(String message) {
            super(message);
        }

    }

    /**
     * Validates method parameters. This method provides common validation logic and
     * delegates exchange type checking to subclasses.
     * @param method The method to validate
     * @throws IllegalArgumentException if the parameters are not compatible
     */
    protected void validateParameters(Method method) {
        Parameter[] parameters = method.getParameters();

        // Check for duplicate parameter types
        boolean hasExchangeParam = false;

        for (java.lang.reflect.Parameter param : parameters) {
            Class<?> paramType = param.getType();

            if (isExchangeType(paramType)) {
                if (hasExchangeParam) {
                    throw new IllegalArgumentException("Method cannot have more than one exchange parameter: "
                            + method.getName() + " in " + method.getDeclaringClass().getName());
                }
                hasExchangeParam = true;
            }

        }
    }

    /**
     * Builds the arguments array for invoking the method.
     * <p>
     * This method constructs an array of arguments based on the method's parameter types
     * and the available values (exchange, request).
     * @param method The method to build arguments for
     * @param exchange The server exchange
     * @param arguments The arguments provided by the client
     * @return An array of arguments for the method invocation
     */
    protected Object[] buildArgs(Method method, Object exchange, Map<String, Object> arguments) {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            java.lang.reflect.Parameter param = parameters[i];
            Class<?> paramType = param.getType();

            if (isExchangeType(paramType)) {
                args[i] = exchange;
            }
            else {
                McpArg arg = param.getAnnotation(McpArg.class);
                String paramName = arg != null && !arg.name().isBlank() ? arg.name() : param.getName();

                if (arguments != null && arguments.containsKey(paramName)) {
                    Object argValue = arguments.get(paramName);
                    args[i] = convertArgumentValue(argValue, paramType);
                }
                else {
                    args[i] = null;
                }
            }
        }

        return args;
    }
    /**
     * Converts an argument value to the expected parameter type.
     * @param value The value to convert
     * @param targetType The target type
     * @return The converted value
     */
    protected Object convertArgumentValue(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }

        // Handle primitive types and their wrappers
        if (targetType == String.class) {
            return value.toString();
        }
        else if (targetType == Integer.class || targetType == int.class) {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            else {
                return Integer.parseInt(value.toString());
            }
        }
        else if (targetType == Long.class || targetType == long.class) {
            if (value instanceof Number) {
                return ((Number) value).longValue();
            }
            else {
                return Long.parseLong(value.toString());
            }
        }
        else if (targetType == Double.class || targetType == double.class) {
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            }
            else {
                return Double.parseDouble(value.toString());
            }
        }
        else if (targetType == Boolean.class || targetType == boolean.class) {
            if (value instanceof Boolean) {
                return value;
            }
            else {
                return Boolean.parseBoolean(value.toString());
            }
        }

        // For other types, return as is and hope for the best
        return value;
    }

    /**
     * Checks if a parameter type is compatible with the exchange type. This method should
     * be implemented by subclasses to handle specific exchange type checking.
     * @param paramType The parameter type to check
     * @return true if the parameter type is compatible with the exchange type, false
     * otherwise
     */
    protected abstract boolean isExchangeType(Class<?> paramType);


    /**
     * Abstract builder for creating McpToolMethodCallback instances.
     * <p>
     * This builder provides a base for constructing callback instances with the required
     * parameters.
     *
     * @param <T> The type of the builder
     * @param <R> The type of the callback
     */
    @SuppressWarnings("unchecked")
    protected abstract static class AbstractBuilder<T extends AbstractMcpToolMethodCallback.AbstractBuilder<T, R>, R> {

        protected Method method;

        protected Object bean;

        protected String name;
        protected String description;
        protected String inputSchema;
        protected String mineType;
        protected String outputSchema;
        protected McpSchema.ToolAnnotations annotations;
        protected McpCallToolResultConverter converter;
        /**
         * Set the method to create a callback for.
         * @param method The method to create a callback for
         * @return This builder
         */
        public T method(Method method) {
            this.method = method;
            return (T) this;
        }

        public T inputSchema(String inputSchema) {
            this.inputSchema = inputSchema;
            return (T) this;
        }

        public T outputSchema(String outputSchema) {
            this.outputSchema = outputSchema;
            return (T) this;
        }
        public T description(String description) {
            this.description = description;
            return (T) this;
        }

        public T name(String name) {
            this.name = name;
            return (T) this;
        }

        public T mineType(String mineType) {
            this.mineType = mineType;
            return (T) this;
        }

        public T converter(McpCallToolResultConverter converter) {
            this.converter = converter;
            return (T) this;
        }

        public T annotations(McpSchema.ToolAnnotations annotations) {
            this.annotations = annotations;
            return (T) this;
        }

        /**
         * Set the bean instance that contains the method.
         * @param bean The bean instance
         * @return This builder
         */
        public T bean(Object bean) {
            this.bean = bean;
            return (T) this;
        }

        /**
         * Set the Tool annotation.
         * @param tool The Tool annotation
         * @return This builder
         */
        public T tool(McpTool tool) {
            // No additional configuration needed from the annotation at this time
            this.name = tool.name();
            this.description = tool.description();
            this.mineType = tool.mineType();
            this.annotations = new McpSchema.ToolAnnotations(tool.title(),
                    tool.readOnlyHint(),
                    tool.destructiveHint(),
                    tool.idempotentHint(),
                    tool.openWorldHint(),
                    tool.returnDirect());
            try {
                // 使用 Constructor.newInstance() 替代 Class.newInstance()
                this.converter = tool.converter().getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("无法实例化转换器", e);
            }

            return (T) this;
        }

        /**
         * Validate the builder state.
         * @throws IllegalArgumentException if the builder state is invalid
         */
        protected void validate() {
            if (method == null) {
                throw new IllegalArgumentException("Method must not be null");
            }
            if (bean == null) {
                throw new IllegalArgumentException("Bean must not be null");
            }
        }

        /**
         * Build the callback.
         * @return A new callback instance
         */
        public abstract R build();

	}

}
