package com.logaritex.mcp.provider;

import com.logaritex.mcp.annotation.CompleteAdapter;
import com.logaritex.mcp.annotation.McpComplete;
import com.logaritex.mcp.method.complete.AsyncMcpCompleteMethodCallback;
import com.logaritex.mcp.method.complete.SyncMcpCompleteMethodCallback;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.util.Assert;
import reactor.core.publisher.Mono;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author han
 * @time 2025/6/17 17:42
 */

public class AsyncMcpCompletionProvider {
    private final List<Object> completeObjects;

    public AsyncMcpCompletionProvider(List<Object> completeObjects) {
        Assert.notNull(completeObjects, "completeObjects cannot be null");
        this.completeObjects = completeObjects;
    }

    public List<McpServerFeatures.AsyncCompletionSpecification> getCompleteSpecifications() {

        List<McpServerFeatures.AsyncCompletionSpecification> syncCompleteSpecification = this.completeObjects.stream()
                .map(completeObject -> Stream.of(doGetClassMethods(completeObject))
                        .filter(method -> method.isAnnotationPresent(McpComplete.class))
                        .map(mcpCompleteMethod -> {
                            var completeAnnotation = mcpCompleteMethod.getAnnotation(McpComplete.class);
                            var completeRef = CompleteAdapter.asCompleteReference(completeAnnotation, mcpCompleteMethod);

                            var methodCallback = AsyncMcpCompleteMethodCallback.builder()
                                    .method(mcpCompleteMethod)
                                    .bean(completeObject)
                                    .reference(completeRef)
                                    .build();

                            return new McpServerFeatures.AsyncCompletionSpecification(completeRef, methodCallback);
                        })
                        .toList())
                .flatMap(List::stream)
                .toList();

        return syncCompleteSpecification;
    }

    /**
     * Returns the methods of the given bean class.
     * @param bean the bean instance
     * @return the methods of the bean class
     */
    protected Method[] doGetClassMethods(Object bean) {
        return bean.getClass().getDeclaredMethods();
    }

}
