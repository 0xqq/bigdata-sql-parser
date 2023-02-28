package com.github.bigdata.sql.parser.pg

import org.junit.Test

class Test {

    @Test
    fun setConfigTest1() {
        println("---------------")
        var querySql =  "select id,created_at,updated_at,counter_id,trade_day ,open,high,low,close,turnover,volume,suspension_indicator from quote_eod_private where trade_day = to_char(current_date - interval '1 day','YYYY-MM-DD') and CURRENT_DATE > date(trade_day) union select id,created_at,updated_at,counter_id,trade_day,open,high,low,close,turnover,volume,suspension_indicator from (select *,rank() over(partition by counter_id order by trade_day desc) as rownum from quote_eod_private where CURRENT_DATE > date(trade_day))t where rownum=1 "
        var statement = PostgreSQLHelper().parseSqlFieldLineage(querySql).getStatementData().statement
        println(statement)
    }
}