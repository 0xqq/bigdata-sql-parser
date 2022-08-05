package com.github.bigdata.sql.parser.pg

import cn.hutool.json.JSONUtil
import com.facebook.presto.sql.tree.Insert
import com.github.bigdata.sql.antlr4.presto.PrestoSqlParser.SelectItemContext
import com.github.bigdata.sql.parser.presto.PrestoSQLHelper
import org.apache.shardingsphere.sql.parser.SQLParseEngineFactory
import org.apache.shardingsphere.sql.parser.autogen.PostgreSQLStatementParser.SelectContext
import org.apache.shardingsphere.sql.parser.core.parser.SQLAST
import org.apache.shardingsphere.sql.parser.sql.segment.SQLSegment
import org.apache.shardingsphere.sql.parser.sql.segment.dml.item.SelectItemsSegment
import org.apache.shardingsphere.sql.parser.sql.segment.generic.SchemaSegment
import org.apache.shardingsphere.sql.parser.sql.segment.generic.TableSegment
import org.apache.shardingsphere.sql.parser.sql.statement.SQLStatement
import org.junit.Test


class PgTest {

    @Test
    fun test() {
        val sqlParseEngine = SQLParseEngineFactory.getSQLParseEngine("PostgreSQL")
        val allSQLSegments = sqlParseEngine.parse("select * from tbl.sss where id = 1", true).allSQLSegments
        for (segment in allSQLSegments) {
            when (segment) {
                is SelectItemsSegment -> {
                    segment.selectItems.forEach {
                        println(it)
                        println(it.text)
                    }
                }
                is TableSegment -> {
                    println(segment.owner.get().name)
                    println(segment.quoteCharacter)
                    println(segment.tableName)
                }
            }
            println("******" + segment)
        }


    }
}