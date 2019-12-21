package com.sarming.mybatis_test.typeHandler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@MappedJdbcTypes({JdbcType.VARCHAR})
@MappedTypes({List.class})
public class MyListTypeHandler extends BaseTypeHandler<List<String>> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<String> parameter, JdbcType jdbcType) throws SQLException {
//        StringJoiner sj = new StringJoiner(",");
//        parameter.forEach(v -> sj.add(v));
//        ps.setString(i, sj.toString());

        String collect = parameter.stream().collect(Collectors.joining(","));
        ps.setString(i, collect);


    }

    @Override
    public List<String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        if (rs.getString(columnName)==null)
            return null;
        return Arrays.asList(rs.getString(columnName).split(","));
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        if (rs.getString(columnIndex)==null)
            return null;
        return Arrays.asList(rs.getString(columnIndex).split(","));
    }

    @Override
    public List<String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        if (cs.getString(columnIndex)==null)
            return null;
        return Arrays.asList(cs.getString(columnIndex).split(","));
    }
}
