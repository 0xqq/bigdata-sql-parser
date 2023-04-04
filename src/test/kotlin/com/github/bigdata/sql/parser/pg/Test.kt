package com.github.bigdata.sql.parser.pg

import cn.hutool.json.JSONUtil
import org.junit.Test

class Test {

    @Test
    fun setConfigTest1() {
        println("---------------")
        var querySql = "select id,counter_id,alias,former_name,former_ticker from security_extend"
        var statement = PostgreSQLHelper().parseSqlFieldLineage(querySql).getStatementData().statement
        println(JSONUtil.parseObj(statement))
    }
}