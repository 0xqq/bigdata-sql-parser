package com.github.bigdata.sql.parser.datax.writer

import cn.hutool.core.util.ArrayUtil
import cn.hutool.json.JSONObject

/**
 *
 * Author: duhanmin
 * Description:
 * Date: 2021/12/29 11:14
 */
class S3Writer {
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
        val path = writer!!.getStr("path")
        val split = path.split("/").toTypedArray()
        if (split.size < 3) throw RuntimeException("path 无法解析出库和表,path:" + ArrayUtil.toString(split))
        if (writer!!.containsKey("old")){
            db = split[2]
            table = split[3]
        }else{
            db = split[1]
            table = split[2]
            val column = writer!!.getJSONArray("column")
            for (i in column.indices) {
                val name = column.getJSONObject(i).getStr("name")
                columns.add(name)
            }
        }
    }
}
