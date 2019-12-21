package com.sarming.mybatis_test.persistence;


import com.sarming.mybatis_test.domain.User2;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface User2Mapper {
    void insertUser(User2 user);

    int insertWithCreateTime(User2 user);


    List<User2> getUserByName(String name);

    List<User2> findUserByNameAndAge(@Param("name") String name, @Param("age") int age);

    @Select("select * from test_user u where u.id = #{id}")
    User2 findUserById(int id);


    List<User2> findByEndWith(@Param("test") String test, @Param("name") String name);
}