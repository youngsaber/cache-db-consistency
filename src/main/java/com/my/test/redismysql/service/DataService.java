package com.my.test.redismysql.service;

import com.my.test.redismysql.entity.Data;

public interface DataService {

    /**
     * 更新data
     * @param text
     */
    void updateText(Integer id, String text);

    /**
     * 获取信息
     * @param id
     * @return
     */
    String getText(Integer id);

    /**
     * 获取db信息
     * @param id
     * @return
     */
    String getDBText(Integer id);

    /**
     * 普通情况下更新
     * @param text
     */
    String commonUpdateText(Integer id, String text);

    String commonGetText(Integer id);
}
