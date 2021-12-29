package com.github.bigdata.sql.parser.datax.writer

import cn.hutool.json.JSONObject
import cn.hutool.json.JSONUtil
import com.github.bigdata.sql.parser.datax.commons.DataxType

/**
 *
 * Author: duhanmin
 * Description:
 * Date: 2021/12/29 11:14
 */
class DBWriter {
    private var name: String? = null
    private var writer: JSONObject? = null
    var db: String? = null
    var table: String? = null
    var connectors: String? = null
    var columns: MutableList<String> = ArrayList()

    @Throws(Exception::class)
    constructor(name: String?, writer: JSONObject?) {
        this.name = name
        this.writer = writer
        init()
    }

    @Throws(Exception::class)
    private fun init() {
        val connection = writer!!.getJSONArray("connection").getJSONObject(0)
        var jdbcUrl = connection.getStr("jdbcUrl")
        if (jdbcUrl.contains("?")){
            jdbcUrl = jdbcUrl.split("?")[0]
        }
        val split = jdbcUrl.split("/").toTypedArray()

        db = split[split.size - 1]
        val table = connection.getJSONArray("table").getStr(0)
        this.table = table

        if (DataxType.Writer.pg.code == name) {
            connectors = db
            if (!table.contains(".")) {
                db = "public"
                this.table = table
            } else {
                val tables = table.split(".").toTypedArray()
                db = tables[0]
                this.table = tables[1]
            }
        }
        val column = writer!!.getJSONArray("column")
        columns.addAll(JSONUtil.toList(column, String::class.java))
    }
}
