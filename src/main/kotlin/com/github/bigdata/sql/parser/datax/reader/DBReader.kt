package com.github.bigdata.sql.parser.datax.reader

import cn.hutool.core.util.StrUtil
import cn.hutool.json.JSONObject
import cn.hutool.json.JSONUtil
import com.github.bigdata.sql.parser.TableData
import com.github.bigdata.sql.parser.datax.commons.DataxType
import com.github.bigdata.sql.parser.pg.PostgreSQLHelper
import com.github.bigdata.sql.parser.tidb.TidbSQLHelper.getStatementData
import com.github.bigdata.sql.parser.util.SqlParserTool

/**
 *
 * Author: duhanmin
 * Description:
 * Date: 2021/12/29 11:13
 */
class DBReader {
    private var name: String? = null
    private var reader: JSONObject? = null
    var connectors: String? = null
    var db: String? = null
    var table: String? = null
    var columns: MutableList<String> = ArrayList()

    @Throws(Exception::class)
    constructor(name: String?, reader: JSONObject?) {
        this.name = name
        this.reader = reader
        init()
    }

    @Throws(Exception::class)
    private fun init() {
        val connection = reader!!.getJSONArray("connection").getJSONObject(0)
        var jdbcUrl = connection.getJSONArray("jdbcUrl").getStr(0)
        if (jdbcUrl.contains("?")) {
            jdbcUrl = jdbcUrl.split("?")[0]
        }
        val split = jdbcUrl.split("/").toTypedArray()
        db = split[split.size - 1]
        if (DataxType.Reader.pg.code == name) {
            pg(connection)
        } else {
            db(connection)
        }
        if (DataxType.Reader.mysql.code == name && StrUtil.isBlank(connectors)) {
            connectors = "mysql"
        }
    }

    @Throws(Exception::class)
    private fun pg(connection: JSONObject) {
        connectors = db
        if (!reader!!.containsKey("column") || connection.containsKey("querySql")) {
            var querySql = connection.getJSONArray("querySql").getStr(0)
            val statement = PostgreSQLHelper().parseSqlFieldLineage(querySql).getStatementData().statement
            val tableData: TableData = if (statement is TableData) {
                statement
            } else {
                throw RuntimeException("血缘无法解析 -> $statement")
            }
            val tableSource = tableData.inputTables[0]
            db = if (StrUtil.isBlank(tableSource.databaseName)) "public" else tableSource.databaseName
            table = tableSource.tableName
            columns = tableSource.columns as MutableList<String>
        } else {
            val table = connection.getJSONArray("table").getStr(0)
            if (!table.contains(".")) {
                db = "public"
                this.table = table
            } else {
                val tables = table.split(".").toTypedArray()
                db = tables[0]
                this.table = tables[1]
            }
            val column = reader!!.getJSONArray("column")
            columns.addAll(JSONUtil.toList(column, String::class.java))
        }
    }

    @Throws(Exception::class)
    private fun db(connection: JSONObject) {
        if (!reader!!.containsKey("column")) {
            val querySql = connection.getJSONArray("querySql").getStr(0)
            val statement = getStatementData(querySql).statement
            val tableData: TableData
            tableData = if (statement is TableData) {
                statement
            } else {
                throw RuntimeException("血缘无法解析 -> $statement")
            }
            val (_, _, tableName) = tableData.inputTables[0]
            table = tableName
            columns.addAll(SqlParserTool.getSelectColumnName(querySql))
        } else {
            table = connection.getJSONArray("table").getStr(0)
            val column = reader!!.getJSONArray("column")
            columns.addAll(JSONUtil.toList(column, String::class.java))
        }
    }
}
