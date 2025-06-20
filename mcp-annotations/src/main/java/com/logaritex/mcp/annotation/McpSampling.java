/*
 * Copyright 2025-2025 the original author or authors.
 */

package com.logaritex.mcp.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于处理来自 MCP 服务器的采样请求的方法注解。
 *
 * <p>
 * 被此注解标记的方法可用于处理来自 MCP 服务器的采样请求。方法可以具有以下两种签名之一：
 * <ul>
 * <li>一个单独的 {@code CreateMessageRequest} 类型参数
 * <li>多个参数，分别对应 {@code CreateMessageRequest} 的字段
 * </ul>
 *
 * <p>
 * 对于同步处理器，方法必须返回 {@code CreateMessageResult}。对于异步处理器，
 * 方法必须返回 {@code Mono<CreateMessageResult>}。
 *
 * <p>
 * 使用示例：<pre>{@code
 * &#64;McpSampling
 * public CreateMessageResult handleSamplingRequest(CreateMessageRequest request) {
 *     // 处理请求并返回结果
 *     return CreateMessageResult.builder()
 *         .message("生成的响应")
 *         .build();
 * }
 *
 * &#64;McpSampling
 * public Mono<CreateMessageResult> handleAsyncSamplingRequest(CreateMessageRequest request) {
 *     // 异步处理请求并返回结果
 *     return Mono.just(CreateMessageResult.builder()
 *         .message("生成的响应")
 *         .build());
 * }
 * }</pre>
 *
 * @author Christian Tzolov
 * @see io.modelcontextprotocol.spec.McpSchema.CreateMessageRequest
 * @see io.modelcontextprotocol.spec.McpSchema.CreateMessageResult
 */
@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface McpSampling {

}
