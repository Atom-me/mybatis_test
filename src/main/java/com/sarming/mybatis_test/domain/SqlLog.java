package com.sarming.mybatis_test.domain;

import java.util.Date;

public class SqlLog {
    private int id;
    private String sqlClause;
    private int result;
    private Date createTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSqlClause() {
        return sqlClause;
    }

    public void setSqlClause(String sqlClause) {
        this.sqlClause = sqlClause;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "SqlLog{" +
                "id=" + id +
                ", sqlClause='" + sqlClause + '\'' +
                ", result=" + result +
                ", createTime=" + createTime +
                '}';
    }
}
