package com.github.bigdata.sql.parser.datax

import cn.hutool.json.JSONObject
import cn.hutool.json.JSONUtil
import com.github.bigdata.sql.parser.StatementData
import com.github.bigdata.sql.parser.StatementType
import com.github.bigdata.sql.parser.TableData
import com.github.bigdata.sql.parser.TableSource
import com.github.bigdata.sql.parser.datax.commons.DataxType
import com.github.bigdata.sql.parser.datax.reader.DBReader
import com.github.bigdata.sql.parser.datax.reader.S3Reader
import com.github.bigdata.sql.parser.datax.writer.DBWriter
import com.github.bigdata.sql.parser.datax.writer.S3Writer

/**
 *
 * Author: duhanmin
 * Description:
 * Date: 2021/12/28 15:55
 */
object DataxLHelper {

    @JvmStatic fun getStatementData(json: String): StatementData {

        // 解析json
        val reader: JSONObject
        val writer: JSONObject
        val readerName: String
        val writerName: String
        try {
            val content: JSONObject = JSONUtil.parseObj(json).getJSONObject("job").getJSONArray("content").getJSONObject(0)
            val readerJSONObject: JSONObject = content.getJSONObject("reader")
            reader = readerJSONObject.getJSONObject("parameter")
            readerName = readerJSONObject.getStr("name")

            val writerJSONObject: JSONObject = content.getJSONObject("writer")
            writer = writerJSONObject.getJSONObject("parameter")
            writerName = writerJSONObject.getStr("name")
        }catch (e: Exception){
            e.printStackTrace()
            return StatementData(StatementType.UNKOWN, null)
        }

        val statementData = TableData()

        var tableSourceReader:TableSource? = null
        //拿到reader
        if (readerName.equals(DataxType.Reader.pg.code) || readerName.equals(DataxType.Reader.mysql.code)){
            val dbReader = DBReader(readerName,reader)
            tableSourceReader = TableSource(dbReader.connectors,dbReader.db, dbReader.table,null,dbReader.columns)
        }else if (readerName.equals(DataxType.Reader.s3.code)){
            val s3Reader = S3Reader(readerName,reader)
            tableSourceReader = TableSource(null,s3Reader.db, s3Reader.table,null,s3Reader.columns)
        }
        tableSourceReader?.let { statementData.inputTables.add(it) }

        var tableSourceWriter:TableSource? = null
        //拿到writer
        if (writerName.equals(DataxType.Writer.pg.code) || writerName.equals(DataxType.Writer.mysql.code)){
            val dbWriter = DBWriter(writerName, writer)
            tableSourceWriter = TableSource(dbWriter.connectors,dbWriter.db, dbWriter.table,null,dbWriter.columns)
        }else if (writerName.equals(DataxType.Writer.s3.code)){
            val s3Writer = S3Writer(writerName, writer)
            tableSourceWriter = TableSource(s3Writer.connectors,s3Writer.db, s3Writer.table,null,s3Writer.columns)

        }
        tableSourceWriter?.let { statementData.outpuTables.add(it) }

        return StatementData(StatementType.UNKOWN, statementData)
    }
}
