package com.example.yupaobackend; // 请确保这个包名与您的项目匹配

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

// @SpringBootTest
public class RedisMemoryTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    // 1. 定义一个统一的、字段名更长的 User record，让两个测试共用
    private record User(
            int uniqueIdentifier,
            String displayName,
            String userRole,
            int experienceLevel
    ) {}

    private static final int NUM_USERS = 100000; // 测试10万个用户

    /**
     * 在每个测试方法运行前，清空当前 Redis 数据库，确保测试环境纯净。
     */
    @BeforeEach
    void setUp() {
        // 使用 getRequiredConnectionFactory() 以确保连接工厂一定存在
        try (RedisConnection connection = stringRedisTemplate.getRequiredConnectionFactory().getConnection()) {
            connection.serverCommands().flushDb();
        }
    }

    /**
     * 测试方法一：将用户对象序列化为包含长字段名的 JSON 字符串进行存储
     */
    @Test
    void testMemoryUsage_JsonStrings() throws JsonProcessingException {
        long memoryBefore = getUsedMemory();
        System.out.println("JSON 测试 - 存储前内存占用: " + memoryBefore + " bytes");

        for (int i = 0; i < NUM_USERS; i++) {
            // 2. 使用更新后的 User record 创建实例
            User user = new User(i, "user" + i, "player", i * 10);
            String userJson = objectMapper.writeValueAsString(user);
            stringRedisTemplate.opsForValue().set("user:json:" + i, userJson);
        }

        long memoryAfter = getUsedMemory();
        System.out.println("JSON 测试 - 存储后内存占用: " + memoryAfter + " bytes");
        System.out.printf(">>> 存储 %,d 个用户为 JSON 字符串，总共占用了 %,d bytes 内存。%n%n", NUM_USERS, (memoryAfter - memoryBefore));
    }

    /**
     * 测试方法二：将用户对象以包含长字段名的 Hash 结构进行存储
     */
    @Test
    void testMemoryUsage_Hashes() {
        long memoryBefore = getUsedMemory();
        System.out.println("Hash 测试 - 存储前内存占用: " + memoryBefore + " bytes");

        for (int i = 0; i < NUM_USERS; i++) {
            // 3. 使用更新后的 User record 创建实例
            User user = new User(i, "user" + i, "player", i * 10);

            Map<String, String> userMap = new HashMap<>();
            // 确保 Map 的 key 与 User record 的字段名一致
            userMap.put("uniqueIdentifier", String.valueOf(user.uniqueIdentifier()));
            userMap.put("displayName", user.displayName());
            userMap.put("userRole", user.userRole());
            userMap.put("experienceLevel", String.valueOf(user.experienceLevel()));

            stringRedisTemplate.opsForHash().putAll("user:hash:" + i, userMap);
        }

        long memoryAfter = getUsedMemory();
        System.out.println("Hash 测试 - 存储后内存占用: " + memoryAfter + " bytes");
        System.out.printf(">>> 存储 %,d 个用户为 Hash，总共占用了 %,d bytes 内存。%n%n", NUM_USERS, (memoryAfter - memoryBefore));
    }

    /**
     * 辅助方法，用于获取当前 Redis 数据库的已用内存大小
     * @return 已用内存字节数
     */
    private long getUsedMemory() {
        try (RedisConnection connection = stringRedisTemplate.getRequiredConnectionFactory().getConnection()) {
            Properties info = connection.serverCommands().info("memory");
            if (info != null) {
                return Long.parseLong(info.getProperty("used_memory"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1L;
    }
}