package com.my.test.redismysql.dao;

import com.my.test.redismysql.entity.Data;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface DataDao {


    @Insert({"insert into `data` (text) values (#{text})"})
    void add(Data data);

    @Update({"update `data` set text = #{text} where id = #{id}"})
    void update(Data data);

    @Select({"select * from `data` where id = #{id}"})
    Data getData(Integer id);
}
