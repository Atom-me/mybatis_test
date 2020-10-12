package com.sarming.mybatis_test;


import com.google.common.collect.Maps;
import com.sarming.mybatis_test.domain.User;
import org.apache.ibatis.annotations.Select;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

interface UserMapper {

    @Select("select * from user where id = #{id} and name = #{name}")
    List<User> selectUserList(Integer id, String name);
}

/**
 * @author Atom
 */
public class App {

    public static void main(String[] args) {
        UserMapper userMapper = (UserMapper) Proxy.newProxyInstance(App.class.getClassLoader(), new Class[]{UserMapper.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Map<String, Object> nameArgsMap = buildMethodArgumentNameMap(method, args);
                System.err.println(nameArgsMap);
                //get method
                System.err.println("methodName:::" + method.getName());
                //get args
                System.err.println("args:::" + Arrays.toString(args));
                //get annotation and sql
                Select annotation = method.getAnnotation(Select.class);
                if (Objects.nonNull(annotation)) {
                    String[] value = annotation.value();
                    String sql = value[0];
                    System.err.println("the original sql-> " + sql);
                    sql = parseSQL(sql, nameArgsMap);
                    System.err.println("the final sql-> " + sql);

                }
                return null;
            }
        });

        System.err.println(userMapper.getClass());

        userMapper.selectUserList(1, "ATom");
    }

    public static String parseSQL(String sql, Map<String, Object> nameArgMap) {
        StringBuilder finalSql = new StringBuilder();
        int length = sql.length();
        for (int i = 0; i < length; i++) {
            char c = sql.charAt(i);
            if (c == '#') {
                int nextIndex = i + 1;
                char nextChar = sql.charAt(nextIndex);
                if (nextChar != '{') {
                    throw new RuntimeException(String.format("这里应该为#{\nsql:%s\nindex:%d", finalSql.toString(), nextIndex));
                }
                StringBuilder argNameSB = new StringBuilder();
                i = parseSQLArg(sql, nextIndex, argNameSB);

                String argName = argNameSB.toString();
                System.err.println("argName:::::" + argName);
                Object argValue = nameArgMap.get(argName);
                if (Objects.isNull(argValue)) {
                    throw new RuntimeException(String.format("找不到参数值：%s", argName));
                }
                finalSql.append(argValue.toString());
                continue;
            }
            finalSql.append(c);
        }
        return finalSql.toString();
    }

    private static int parseSQLArg(String sql, int nextIndex, StringBuilder argSB) {
        //nextIndex 现在指向 '{'
        nextIndex++;
        for (; nextIndex < sql.length(); nextIndex++) {
            char c = sql.charAt(nextIndex);
            if (c != '}') {
                argSB.append(c);
                continue;
            }
            if (c == '}') {
                return nextIndex;
            }
        }
        throw new RuntimeException(String.format("缺少右括号\nindex:%d", nextIndex));
    }

    public static Map<String, Object> buildMethodArgumentNameMap(Method method, Object[] args) {
        Map<String, Object> nameArgMap = Maps.newHashMap();
        Parameter[] parameters = method.getParameters();

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            String name = parameter.getName();
            System.err.println("parameterName:::" + name);
            nameArgMap.put(name, args[i]);
        }
        return nameArgMap;
    }
}
