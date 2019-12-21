package com.sarming.mybatis_test;

import com.sarming.mybatis_test.domain.User;
import com.sarming.mybatis_test.domain.User2;
import com.sarming.mybatis_test.persistence.User2Mapper;
import com.sarming.mybatis_test.persistence.UserMapper;
import com.sarming.mybatis_test.plugin.MyPlugin;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.decorators.LoggingCache;
import org.apache.ibatis.cache.decorators.LruCache;
import org.apache.ibatis.cache.decorators.SerializedCache;
import org.apache.ibatis.cache.decorators.SynchronizedCache;
import org.apache.ibatis.cache.impl.PerpetualCache;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.session.*;
import org.apache.ibatis.session.defaults.DefaultSqlSession;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.jdbc.JdbcTransaction;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class MybatisTest {

    private static SqlSessionFactory sqlSessionFactory;

    @BeforeClass
    public static void setUp() throws IOException, SQLException {
        String resource = "mybatis-config.xml";
        Reader resourceAsReader = Resources.getResourceAsReader(resource);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(resourceAsReader);
        Configuration configuration = sqlSessionFactory.getConfiguration();
        configuration.getTypeAliasRegistry().getTypeAliases().forEach((k, v) -> System.err.println(k + "::" + v.getName()));


        System.out.println("====================================================");
        boolean autoCommit = configuration.getEnvironment().getDataSource().getConnection().getAutoCommit();
        String environmentId = configuration.getEnvironment().getId();
        System.err.println("environmentId:" + environmentId);
        System.err.println("autoCommit:" + autoCommit);
        System.out.println("====================================================");
//        configuration.addInterceptor(new MyPlugin());
//        configuration.addInterceptor(new SqlInterceptor());
        configuration.getTypeHandlerRegistry().register("com.sarming.mybatis_test.typeHandler");


    }

    @Test
    public void testDateTypeHandler() {
        SqlSession sqlSession = sqlSessionFactory.openSession(true);
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);
        User user = new User();
        user.setName("sarming");
        user.setAge(20);
        user.setCreateTime(new Date());
        user.setHobby(new ArrayList<String>() {
            {
                add("java");
                add("ios");
                add("JavaScript");
            }
        });
        int i = mapper.insertWithCreateTime(user);
        System.out.println("==============iiiiiiiiii===========>" + i);
        System.out.println("==============iiiiiiiiii===========>" + user.getId());
        mapper.insertWithList2String(user);

//        mapper.insertUser(user);

        List<User> userList1 = mapper.getUserByName("sarming");
        userList1.forEach(System.err::println);

        String name = "sarming";
        int age = 20;
        List<User> userList2 = mapper.findUserByNameAndAge(name, age);
        userList2.forEach(System.err::println);


        User user2 = mapper.findUserById(2);
        System.err.println("user2::::::::::::::::" + user2);

        String test = "me";
        String endName = "ing";
        List<User> user1 = mapper.findByEndWith(test, endName);
        user1.forEach(System.out::println);

        sqlSession.close();
    }

    @Test
    public void testUseGenerateKeys() {
        SqlSession sqlSession = sqlSessionFactory.openSession(true);
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);
        User user = new User();
        user.setName("sarming");
        user.setAge(20);
        user.setCreateTime(new Date());
        user.setHobby(new ArrayList<String>() {
            {
                add("java");
                add("ios");
                add("JavaScript");
            }
        });
        int i = mapper.insertWithCreateTime(user);
        System.out.println("==============iiiiiiiiii===========>" + i);
        System.out.println("==============primarykey===========>" + user.getId());
        mapper.insertWithList2String(user);
        sqlSession.close();
    }

    @Test
    public void testSelectKey() {
        SqlSession sqlSession = sqlSessionFactory.openSession(true);
        User2Mapper mapper = sqlSession.getMapper(User2Mapper.class);
        User2 user = new User2();
        user.setName("Atom");
        user.setAge(21);
        int i = mapper.insertWithCreateTime(user);
        System.out.println("==============iiiiiiiiii===========>" + i);
        System.out.println("==============getUUID ===========>" + user.getId());
        sqlSession.close();
    }

    @Test
    public void testJavaConfig() throws Exception {
        // create configuration
        Configuration configuration = new Configuration();
        configuration.setCacheEnabled(true);
        configuration.setLazyLoadingEnabled(false);
        configuration.setAggressiveLazyLoading(true);
        // add plugin
        configuration.addInterceptor(new MyPlugin());
        //create datasource
        Properties properties = Resources.getResourceAsProperties("jdbc.properties");
        UnpooledDataSource dataSource = new UnpooledDataSource();
        dataSource.setDriver(properties.getProperty("driver"));
        dataSource.setUrl(properties.getProperty("url"));
        dataSource.setUsername(properties.getProperty("username"));
        dataSource.setPassword(properties.getProperty("password"));

        // crate Executor  <TransactionManager type="JDBC"/>
        Transaction transaction = new JdbcTransaction(dataSource, null, false);
        // configuration.newExecutor 会将符合条件的拦截器（插件） 添加到Executor代理链上
        Executor executor = configuration.newExecutor(transaction);

        //cache是一个多层代理【装饰模式】的缓存对象，通过一级一级代理使得一个简单的缓存拥有了复杂的功能
        //<cache/>
        final Cache userCache =
                new SynchronizedCache(//同步缓存
                        new SerializedCache(//序列化缓存
                                new LoggingCache(//日志缓存
                                        new LruCache(//最少使用缓存
                                                new PerpetualCache("user_cache")//持久缓存
                                        ))));

        //类型处理注册器
        //自己写TypeHandler的时候可以参考该注册器中已经存在的大量实现
        TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();

        //================== 下面的步骤相当于解析XML或者解析接口注解方法生成ms =====================

        //创建静态sqlSource
        //最简单的，相当于从xml或接口注解获取SQL，创建合适的sqlSource对象
        StaticSqlSource staticSqlSource = new StaticSqlSource(configuration, "select * from test_user where id =?");
        //由于上面的SQL有个参数id，这里需要提供ParameterMapping(参数映射)
        List<ParameterMapping> parameterMappings = new ArrayList<>();
        //通过ParameterMapping.Builder创建ParameterMapping
        parameterMappings.add(new ParameterMapping.Builder(configuration, "id", typeHandlerRegistry.getTypeHandler(int.class)).build());
        //通过ParameterMap.Builder创建ParameterMap
        ParameterMap.Builder parameterMapBuilder = new ParameterMap.Builder(configuration, "defaultParameterMap", User.class, parameterMappings);
        //创建ms
        MappedStatement.Builder msBuilder = new MappedStatement.Builder(configuration, "test_get_user", staticSqlSource, SqlCommandType.SELECT);
        msBuilder.parameterMap(parameterMapBuilder.build());
        //<resultMap>
        ResultMap resultMap = new ResultMap.Builder(configuration, "defaultResultMap", User.class, new ArrayList<ResultMapping>() {
            {
                add(new ResultMapping.Builder(configuration, "id", "id", int.class).build());
                add(new ResultMapping.Builder(configuration, "name", "name", String.class).build());
                add(new ResultMapping.Builder(configuration, "age", "age", typeHandlerRegistry.getTypeHandler(int.class)).build());
            }
        }).build();
        //2：不设置具体的映射，只是用类型，相当于只配置resultType="tk.mybatis.sample1.Country"
        //final ResultMap resultMap = new ResultMap.Builder(configuration, "defaultResultMap", User.class, new ArrayList<ResultMapping>()).build();

        List<ResultMap> resultMaps = new ArrayList<>();
        resultMaps.add(resultMap);
        //设置返回值的resultMap和resultType
        msBuilder.resultMaps(resultMaps);
        //设置缓存
        msBuilder.cache(userCache);
        //创建ms
        MappedStatement mappedStatement = msBuilder.build();
        //第一种使用executor执行
        List<User> users = executor.query(mappedStatement, 20, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER);
        users.forEach(user -> System.err.println("first query ==> " + user.toString()));
        //第二种
        //首先添加ms到config
        configuration.addMappedStatement(mappedStatement);
        //创建sqlSession
        SqlSession sqlSession = new DefaultSqlSession(configuration, executor, false);
        //查询
        User user = sqlSession.selectOne("test_get_user", 20);
        System.err.println("second query ==> " + user.toString());
        sqlSession.close();
    }
}
