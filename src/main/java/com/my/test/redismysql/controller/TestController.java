package com.my.test.redismysql.controller;

import com.my.test.redismysql.dao.DataDao;
import com.my.test.redismysql.entity.Data;
import com.my.test.redismysql.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@RestController
public class TestController {

    @Autowired
    private DataService dataService;
    @Autowired
    private DataDao dataDao;
    @Autowired
    private RedisConnectionFactory connectionFactory;


    /**
     * 多线程下cache与mysql混乱的情况
     * @param id
     */
    @GetMapping("/multy-thread")
    public void threadUpdate(@RequestParam("id")Integer id){

        if(id == null || id < 0){
            return;
        }

        for(int i = 0; i < 10; i++){
            int j = i;
            new Thread(() -> {
                String randomValue = String.valueOf((int)(Math.random() * 1000));
                System.out.println("第" + j + "次更新的值为: " + randomValue);
                dataService.commonUpdateText(id, randomValue);
                printlnText(j, id);
            }).start();
        }
    }

    /**
     * 尝试使用readWriteLock实现redis与Mysql缓存一致的情况
     * @param id
     */
    @GetMapping("/multy-thread-lock")
    public void threadLockUpdate(@RequestParam("id")Integer id){

        if(id == null || id < 0){
            return;
        }

        for(int i = 0; i < 10; i++){
            int j = i;
            new Thread(() -> {
                String randomValue = String.valueOf((int)(Math.random() * 1000));
                System.out.println("第" + j + "次更新的值为: " + randomValue);
                dataService.updateText(id, randomValue);
                // printlnText(j, id);
            }).start();
        }

        for(int i = 0; i < 10; i++){
            new Thread(() -> {
                dataService.getText(id);
                // printlnText(j, id);
            }).start();
        }

    }

    private void printlnText(int i, Integer id){
        if(connectionFactory.getConnection().exists(("DATA::DATA_" + id).getBytes())){
            String redisStr = new String(connectionFactory.getConnection().get(("DATA::DATA_" + id).getBytes()));
            String dbStr = dataService.getText(id);
            System.out.println("第" + i + "次数，redis: " + redisStr
                    + " mysql: " + dbStr);
        }else{
            System.out.println("redis: null"
                    + " mysql: " + dataDao.getData(id).getText());
        }
    }
}
