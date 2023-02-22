package com.github.bigdata.sql.parser.datax.reader

import cn.hutool.core.util.ArrayUtil
import cn.hutool.json.JSONObject

/**
 *
 * Author: duhanmin
 * Description:
 * Date: 2021/12/29 11:14
 */
class S3Reader {
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
        val path = reader!!.getJSONArray("path").getStr(0)
        val split = path.split("/").toTypedArray()
        if (split.size < 3) throw RuntimeException("path 无法解析出库和表,path:" + ArrayUtil.toString(split))
        if (path.startsWith("account/")) {
            db = split[3]
            table = split[4]
        } else {
            db = split[1]
            table = split[2]
        }
        val columns = reader!!.getJSONArray("column")
        for (i in columns.indices) {
            val column = columns.getJSONObject(i)
            if (column.containsKey("name"))
                this.columns.add(column.getStr("name"))
        }
        connectors = "hive"
    }
}
