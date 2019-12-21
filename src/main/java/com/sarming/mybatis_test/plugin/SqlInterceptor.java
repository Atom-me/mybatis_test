package com.sarming.mybatis_test.plugin;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Date;
import java.util.List;
import java.util.Properties;

@Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
public class SqlInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement mappedStatement = MappedStatement.class.cast(args[0]);
        Object parameter = args[1];
//        SqlLog sqlLog = new SqlLog();
        Configuration configuration = mappedStatement.getConfiguration();
        Executor executor = Executor.class.cast(invocation.getTarget());
        Connection connection = executor.getTransaction().getConnection();
        String databaseProductVersion = connection.getMetaData().getDatabaseProductVersion();
        System.err.println("databaseVersion####" + databaseProductVersion);

        BoundSql boundSql = mappedStatement.getBoundSql(parameter);

//        String sql = boundSql.getSql();
//        System.err.println("sql########" + sql);
//        sql = sql.replaceAll("\\s", " ");
//        System.err.println("sql########" + sql);
//        sql = sql.replaceAll("[ ]{2,}", " ");
//
//        System.err.println("sql########" + sql);
//        boundSql.getParameterMappings().forEach(parameterMapping -> System.err.println("parameterMapping.getProperty####" + parameterMapping.getProperty()));
//        Object parameterObject = boundSql.getParameterObject();
//        System.err.println("parameterObject####" + parameterObject.toString());



        String sql = this.getSql(configuration, boundSql);



//   boundSql.getParameterMappings().stream().filter()


//
//        StatementHandler statementHandler = configuration.newStatementHandler(executor, mappedStatement, parameter, RowBounds.DEFAULT, null, null);
//        BoundSql boundSql = statementHandler.getBoundSql();
//        String sql = boundSql.getSql();
//        boundSql.getParameterMappings().forEach(parameterMapping -> System.err.println("parameterMapping.getProperty####"+parameterMapping.getProperty()));
//        Object parameterObject = boundSql.getParameterObject();
//        System.err.println("parameterObject####"+parameterObject.toString());
//
//
        System.err.println("sqlInterceptor ======>sql:::::::::::::" + sql);
//
//
//        ParameterHandler parameterHandler = configuration.newParameterHandler(mappedStatement, parameterObject, boundSql);
//        PreparedStatement preparedStatement = connection.prepareStatement(sql);
//        parameterHandler.setParameters(preparedStatement);
//


        Object result = invocation.proceed();


        return result;
    }

    /**
     * 获取完整的sql语句
     *
     * @param configuration
     * @param boundSql
     * @return
     */
    private String getSql(Configuration configuration, BoundSql boundSql) {
        // 输入sql字符串空判断
        String sql = boundSql.getSql();
        if (!StringUtils.hasText(sql)) {
            return "";
        }

        //美化sql
        sql = this.beautifySql(sql);

        //填充占位符, 目前基本不用mybatis存储过程调用,故此处不做考虑
        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        if (!parameterMappings.isEmpty() && parameterObject != null) {
            TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
            if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                sql = this.replacePlaceholder(sql, parameterObject);
            } else {
                MetaObject metaObject = configuration.newMetaObject(parameterObject);
                for (ParameterMapping parameterMapping : parameterMappings) {
                    String propertyName = parameterMapping.getProperty();
                    if (metaObject.hasGetter(propertyName)) {
                        Object obj = metaObject.getValue(propertyName);
                        sql = this.replacePlaceholder(sql, obj);
                    } else if (boundSql.hasAdditionalParameter(propertyName)) {
                        Object obj = boundSql.getAdditionalParameter(propertyName);
                        sql = this.replacePlaceholder(sql, obj);
                    }
                }
            }
        }
        return sql;
    }

    /**
     * 美化Sql
     */
    private String beautifySql(String sql) {
        return sql.replaceAll("[\\s\n ]+", " ");
    }

    /**
     * 填充占位符?
     *
     * @param sql
     * @param parameterObject
     * @return
     */
    private String replacePlaceholder(String sql, Object parameterObject) {
        String result;
        if (parameterObject instanceof String) {
            result = "'" + parameterObject.toString() + "'";
        } else if (parameterObject instanceof Date) {
            result = "'" + this.getDate2String((Date) parameterObject) + "'";
        } else {
            result = parameterObject.toString();
        }
        return sql.replaceFirst("\\?", result);
    }

    private String getDate2String(Date parameterObject) {
        return parameterObject.toLocaleString();
    }

    /**
     * 格式化sql日志
     *
     * @param sqlCommandType
     * @param sqlId
     * @param sql
     * @param costTime
     * @return
     */
    private void formatSqlLog(SqlCommandType sqlCommandType, String sqlId, String sql, long costTime, Object obj) {
        String sqlLog = "Mapper Method ===> [" + sqlId + "]\n, " + sql + "\n\n, " + "Spend Time ===> " + costTime + " ms\n\n";
        if (sqlCommandType == SqlCommandType.UPDATE || sqlCommandType == SqlCommandType.INSERT
                || sqlCommandType == SqlCommandType.DELETE) {
            sqlLog += ", Affect Count ===> " +Integer.getInteger(String.valueOf(obj));
        }
    }
    @Override
    public Object plugin(Object target) {
        if (target instanceof Executor) {
            return Plugin.wrap(target, this);
        }
        return target;
    }

    @Override
    public void setProperties(Properties properties) {

    }
}
