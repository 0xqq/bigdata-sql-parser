package com.github.bigdata.sql.parser.presto

import org.junit.Test

/**
 *
 * Author: duhanmin
 * Description:
 * Date: 2021/8/18 13:31
 */
class PrestoSqlParserTest {

    @Test
    fun sqlTest() {
        val sql = "with recursive t as (select * from a.x) select * from t"
        val statementData = PrestoSQLHelper.getStatementData(sql)
        println(statementData.statement)
    }

    @Test
    fun sql1Test() {
        val sql = "INSERT INTO adc.fsfd with recursive t as (select a,b,v from a.x)  select a,b,v from t"
        val statementData = PrestoSQLHelper.getStatementData(sql)
        println(statementData.statement)
    }
}
