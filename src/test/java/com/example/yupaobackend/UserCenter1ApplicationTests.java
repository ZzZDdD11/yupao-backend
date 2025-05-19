package com.example.yupaobackend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import redis.clients.jedis.Jedis;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@SpringBootTest
class yupaobackendApplicationTests {


    @Test
    void contextLoads() {
        // 连接redis服务
        Jedis redis = new Jedis("127.0.0.1", 6379);
        try{
            System.out.println(redis.ping());
            redis.set("testkey", "hello");
            System.out.println(redis.get("testkey"));
        }finally {
            if(redis != null){
                redis.close();
            }
        }
    }

    public String fetchUserFromDB(String userId) {
        // 这里的 String 是返回类型，表示这个方法会返回一个 String 类型的值
        // fetchUserFromDB 是方法名，你可以叫它 getUserFromDatabase 等，名字要有意义
        // String userId 是参数列表，表示这个方法需要接收一个 String 类型的 userId

        System.out.println("--- 模拟从数据库获取用户: " + userId + " ---");
        // 这里模拟从数据库查询，并返回一个模拟的用户数据字符串 (比如简单的 JSON)
        String userData = "{\"id\":\"" + userId + "\", \"name\":\"模拟用户" + userId + "\", \"email\":\"user" + userId + "@example.com\"}";

        System.out.println("--- 数据库获取完成 ---");
        return userData; // 使用 return 关键字将结果返回
    }

    public String getUserByIdFromRedis(Jedis jedis, String userId) {
        String cacheKey = "user:" + userId;
        String cachedata = jedis.get(cacheKey);
        if(jedis.exists(cacheKey)){
            return cachedata;
        }else {
            System.out.println("缓存未命中");
            String data = fetchUserFromDB(userId);
            jedis.setex(cacheKey,600, data);
            return data;
        }
    }

    @Test
    void redisTest() {

        Jedis jedis = null;
        try {
            jedis = new Jedis("127.0.0.1", 6379);
            System.out.println(jedis.ping());
            System.out.println("第一次获取用户1001 ---");
            String user1001Data = fetchUserFromDB("1001");
            System.out.println("结果：" + user1001Data);

        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if(jedis != null){
                jedis.close();
                System.out.println("\nRedis Test 连接已关闭");
            }
        }


    }

}
