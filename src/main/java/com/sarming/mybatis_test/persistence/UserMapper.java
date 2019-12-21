package com.sarming.mybatis_test.persistence;


import com.sarming.mybatis_test.domain.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface UserMapper {
    void insertUser(User user);

    int insertWithCreateTime(User user);

    void insertWithList2String(User user);

    List<User> getUserByName(String name);

    List<User> findUserByNameAndAge(@Param("name") String name, @Param("age") int age);

    @Select("select * from test_user u where u.id = #{id}")
    @ResultMap({"userResultMap"})
    User findUserById(int id);


    List<User> findByEndWith(@Param("test") String test, @Param("name") String name);
}