package com.github.bigdata.sql.parser.datax

import org.junit.Test

/**
 *
 * Author: duhanmin
 * Description:
 * Date: 2021/12/28 15:53
 */
class DataxParserTest {
    fun readText(path:String): String = DataxParserTest::class.java.getResource(path).readText()

    fun run(path:String) {
        val json = readText(path)
        val statementData = DataxLHelper.getStatementData(json)
        println(statementData)
    }

    @Test
    fun test() {
        run("/datax/pg2s3.json")
        run("/datax/pg2s3_1.json")
        run("/datax/mysql2s3.json")
        run("/datax/mysql2s3_1.json")
        run("/datax/s32mysql.json")
        run("/datax/s32pg.json")
    }
}
