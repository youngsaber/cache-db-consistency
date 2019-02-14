package com.my.test.redismysql.service.impl;

import com.my.test.redismysql.dao.DataDao;
import com.my.test.redismysql.entity.Data;
import com.my.test.redismysql.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class DataServiceImpl implements DataService {

    private static final String DATA_PREFIX = "DATA_";

    @Autowired
    private RedisConnectionFactory connectionFactory;
    @Autowired
    private DataDao dataDao;

    ReadWriteLock lock = new ReentrantReadWriteLock();
    // ReentrantLock lock = new ReentrantLock();


    /**
     * 更新data
     * @param text
     */
    @Override
    public void updateText(Integer id, String text) {

        Data data = dataDao.getData(id);
        data.setText(text);

        lock.writeLock().lock();
        // lock.lock();
        // 更新缓存，更新数据库
        try{
            getConnection().set(("DATA::" + DATA_PREFIX + data.getId()).getBytes(), data.getText().getBytes());
            dataDao.update(data);
        }catch (Exception e){
            // do something
        }finally {
            System.out.println("in write lock redis: " +
                    new String(getConnection().get(("DATA::" + DATA_PREFIX + data.getId()).getBytes()))
            + " mysql: " + dataDao.getData(id).getText());
            lock.writeLock().unlock();
            // lock.unlock();
        }

    }

    /**
     * 获取信息
     * @param id
     * @return
     */
    @Override
    public String getText(Integer id) {
        if(id == null || id < 0){
            return null;
        }
        lock.readLock().lock();
        // lock.lock();
        try{
            // 先读取缓存，未命中读取数据库，更新缓存
            if(getConnection().exists(("DATA::" + DATA_PREFIX + id).getBytes())){
                String redisStr = new String(getConnection().get(("DATA::" + DATA_PREFIX + id).getBytes()));

                // 输出
                Data data = dataDao.getData(id);
                System.out.println("in read lock redis: " +
                        new String(getConnection().get(("DATA::" + DATA_PREFIX + data.getId()).getBytes()))
                        + " mysql: " + dataDao.getData(id).getText());
                return redisStr;
            }

            // 数据库读取
            Data data = dataDao.getData(id);
            if(data != null){
                // 1.更新缓存 2.返回数据
                getConnection().set(("DATA::" + DATA_PREFIX + id).getBytes(), data.getText().getBytes());
                return data.getText();
            }

            return null;
        }catch (Exception e){
            // do something
            throw new RuntimeException("出现异常！");
        }finally {
            lock.readLock().unlock();
            // lock.unlock();
        }
    }

    @Override
    public String getDBText(Integer id) {
        return null;
    }

    @Override
    @CachePut(key = "\"DATA_\" + #p0", value = "DATA")
    // "DATA_" + #p0
    public String commonUpdateText(Integer id, String text) {
        Data data = dataDao.getData(id);
        data.setText(text);
        dataDao.update(data);
        return text;
    }

    @Override
    @Cacheable(key = "\"DATA_\" +#p0", value = "DATA")
    public String commonGetText(Integer id) {
        Data data = dataDao.getData(id);
        if(data != null){
            return data.getText();
        }
        return null;
    }

    private RedisConnection getConnection(){
        return connectionFactory.getConnection();
    }
}