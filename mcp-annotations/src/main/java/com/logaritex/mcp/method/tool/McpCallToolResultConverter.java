package com.logaritex.mcp.method.tool;

import com.logaritex.mcp.method.tool.AbstractMcpToolMethodCallback;
import io.modelcontextprotocol.spec.McpSchema;

import java.lang.reflect.Type;

/**
 * @author han
 * @time 2025/6/27 8:24
 */

public interface McpCallToolResultConverter {
    McpSchema.CallToolResult convertToCallToolResult(Object result, Type returnType, AbstractMcpToolMethodCallback callback);
}
