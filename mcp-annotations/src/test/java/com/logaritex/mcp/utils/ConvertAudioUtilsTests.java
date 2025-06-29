package com.logaritex.mcp.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author han
 * @time 2025/6/28 11:14
 */

public class ConvertAudioUtilsTests {

    private static final Logger log = LoggerFactory.getLogger(ConvertAudioUtilsTests.class);
    @Test
    public void testAudio() {
        try {
            String audio = ConvertAudioUtils.audioToBase64("D:\\下载\\O400001Sc4XZ4cqpFN.ogg");
            log.info("audio:{}", audio);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    public void testMineType() {
        Type returnType = new TypeReference<List<McpSchema.Content>>() {}.getType();
        log.info("returnType:{}", returnType.getTypeName());
        log.info("actual:{}", List.class.getTypeName());
    }
}
