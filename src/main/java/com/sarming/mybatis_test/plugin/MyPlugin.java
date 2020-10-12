package com.sarming.mybatis_test.plugin;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

@Intercepts({
        @Signature(
                type = Executor.class,
                method = "update",
                args = {
                        MappedStatement.class, Object.class
                }
        ),
        @Signature(
                type = Executor.class,
                method = "query",
                args = {
                        MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class
                }
        )
})
public class MyPlugin implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {

        Class<? extends Invocation> aClass = invocation.getClass();
        System.err.println("className : " + aClass.getName());
        Object[] args = invocation.getArgs();
        MappedStatement mappedStatement = MappedStatement.class.cast(args[0]);
        SqlCommandType sqlCommandType = mappedStatement.getSqlCommandType();
        System.err.println("sqlCommandType:::" + sqlCommandType);

        Object arg = args[1];
        BoundSql boundSql = mappedStatement.getBoundSql(args[1]);
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        parameterMappings.forEach(parameterMapping -> {
            String property = parameterMapping.getProperty();
            System.err.println("parameterMapping property:::" + property);
        });
        String sql = boundSql.getSql();
        System.err.println("sql:" + sql);
        Arrays.stream(args).forEach(a -> System.err.println("args:" + a));
        Method method = invocation.getMethod();

        String name = method.getName();
        System.err.println("method: " + name);
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }
}
