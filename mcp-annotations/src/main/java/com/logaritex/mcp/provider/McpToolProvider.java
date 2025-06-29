//package com.logaritex.mcp.provider;
//
//import com.logaritex.mcp.annotation.McpTool;
//import com.logaritex.mcp.method.tool.AsyncMcpToolMethodCallback;
//import com.logaritex.mcp.method.tool.DefaultMcpCallToolResultConverter;
//import com.logaritex.mcp.method.tool.McpCallToolResultConverter;
//import com.logaritex.mcp.method.tool.SyncMcpToolMethodCallback;
//import io.modelcontextprotocol.server.McpServerFeatures;
//import io.modelcontextprotocol.spec.McpSchema;
//import io.modelcontextprotocol.util.Assert;
//
//import java.lang.reflect.Method;
//import java.util.Arrays;
//import java.util.Comparator;
//import java.util.List;
//import java.util.stream.Stream;
//
///**
// * @author han
// * @time 2025/6/29 21:02
// */
//
//public class McpToolProvider {
//    private final List<Object> toolObjects;
//
//    public McpToolProvider(List<Object> toolObjects) {
//        Assert.notNull(toolObjects, "toolObjects cannot be null");
//        this.toolObjects = toolObjects;
//    }
//
//    /**
//     * Returns a list of tool specifications for async tool methods.
//     * @return List<McpServerFeatures.AsyncToolSpecification>
//     */
//    public List<McpServerFeatures.AsyncToolSpecification> getAsyncToolSpecifications() {
//
//        List<McpServerFeatures.AsyncToolSpecification> methodCallbacks = this.toolObjects.stream()
//                .map(toolObject -> Stream.of(doGetClassMethods(toolObject))
//                        .filter(toolMethod -> toolMethod.isAnnotationPresent(McpTool.class))
//                        .map(mcpToolMethod -> {
//                            McpTool mcpTool = mcpToolMethod.getAnnotation(McpTool.class);
//
//
//
//                            AsyncMcpToolMethodCallback methodCallback = AsyncMcpToolMethodCallback.builder()
//                                    .method(mcpToolMethod)
//                                    .bean(toolObject)
//                                    .tool(mcpTool)
//                                    .build();
//
//                            return new McpServerFeatures.AsyncToolSpecification(mcpTool, methodCallback);
//                        })
//                        .toList())
//                .flatMap(List::stream)
//                .toList();
//
//        return methodCallbacks;
//    }
//
//    /**
//     * Returns a list of tool specifications for sync tool methods.
//     * @return List<McpServerFeatures.SyncToolSpecification>
//     */
//    public List<McpServerFeatures.SyncToolSpecification> getSyncToolSpecifications() {
//
//        List<McpServerFeatures.SyncToolSpecification> methodCallbacks = this.toolObjects.stream()
//                .map(toolObject -> Stream.of(doGetClassMethods(toolObject))
//                        .filter(toolMethod -> toolMethod.isAnnotationPresent(McpTool.class))
//                        .map(mcpToolMethod -> {
//                            McpTool toolAnnotation = mcpToolMethod.getAnnotation(McpTool.class);
//
//                            String name = toolAnnotation.name();
//                            String title = toolAnnotation.title();
//                            String description = toolAnnotation.description();
//                            String mineType = toolAnnotation.mineType();
//                            boolean readOnlyHint = toolAnnotation.readOnlyHint();
//                            boolean destructiveHint = toolAnnotation.destructiveHint();
//                            boolean idempotentHint = toolAnnotation.idempotentHint();
//                            boolean openWorldHint = toolAnnotation.openWorldHint();
//                            boolean returnDirect = toolAnnotation.returnDirect();
//                            Class<? extends McpCallToolResultConverter> converter = toolAnnotation.converter();
//
//
//                            var mcpTool = new McpSchema.Tool(uri, name, description, mimeType,null, null);
//
//                            SyncMcpToolMethodCallback methodCallback = SyncMcpToolMethodCallback.builder()
//                                    .method(mcpToolMethod)
//                                    .bean(toolObject)
//                                    .tool(toolAnnotation)
//                                    .build();
//
//                            return new McpServerFeatures.SyncToolSpecification(mcpTool, methodCallback);
//                        })
//                        .toList())
//                .flatMap(List::stream)
//                .toList();
//
//        return methodCallbacks;
//    }
//
//    /**
//     * Returns the methods of the given bean class.
//     * @param bean the bean instance
//     * @return the methods of the bean class
//     */
//    protected Method[] doGetClassMethods(Object bean) {
//        Method[] methods = bean.getClass().getDeclaredMethods();
//        Arrays.sort(methods, Comparator
//                .comparing(Method::getName)
//                .thenComparing(method -> Arrays.toString(method.getParameterTypes())));
//        return methods;
//    }
//
//    private static String getName(Method method, McpTool tool) {
//        Assert.notNull(method, "method cannot be null");
//        if (tool == null || tool.name() == null || tool.name().isEmpty()) {
//            return method.getName();
//        }
//        return tool.name();
//	}
//}
