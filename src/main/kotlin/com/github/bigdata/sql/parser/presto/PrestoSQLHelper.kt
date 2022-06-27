package com.github.bigdata.sql.parser.presto

import cn.hutool.core.collection.CollUtil
import com.facebook.presto.sql.parser.ParsingOptions
import com.facebook.presto.sql.parser.SqlParser
import com.facebook.presto.sql.tree.Insert
import com.facebook.presto.sql.tree.Node
import com.facebook.presto.sql.tree.QuerySpecification
import com.facebook.presto.sql.tree.Table
import com.github.bigdata.sql.parser.StatementData
import com.github.bigdata.sql.parser.StatementType
import com.github.bigdata.sql.parser.TableData
import com.github.bigdata.sql.parser.TableSource
import org.apache.commons.lang.StringUtils


/**
 *
 * Author: duhanmin
 * Description:
 * Date: 2021/8/17 15:26
 */
object PrestoSQLHelper {
    private val SQL_PARSER = SqlParser()

    @JvmStatic fun getStatementData(sql: String): StatementData{
        val statementData = TableData()
        val parsingOptions = ParsingOptions(ParsingOptions.DecimalLiteralTreatment.AS_DOUBLE)
        val statement = SQL_PARSER.createStatement(sql, parsingOptions)

        when (statement) {
            is Insert -> {
                statementData.outpuTables.add(tableSource(statement.target.toString()))
            }
        }
        maxDepthLeaf(statement.children,statementData)

        val hashSet = HashSet(statementData.inputTables)
        statementData.inputTables.clear()
        statementData.inputTables.addAll(hashSet)
        return StatementData(StatementType.PRESTO, statementData)
    }

    private fun tableSource(str: String): TableSource {
        val split = StringUtils.split(str, ".")
        var tableSource: TableSource = when (split.size) {
            2 -> {
                TableSource(split[0], split[1])
            }
            3 -> {
                TableSource(split[0], split[1], split[2],null,null)
            }
            1 -> {
                TableSource("", split[0])
            }
            else -> {
                TableSource("", "")
            }
        }
        return tableSource
    }

    private fun maxDepthLeaf(treeList: List<Node>, statement:TableData) {
        if (CollUtil.isNotEmpty(treeList)) {
            for (node in treeList) {

                val children = node.children

                if (node is QuerySpecification){
                    if (node.from.isPresent){
                        val from = node.from.get()
                        if(from is Table){
                            statement.inputTables.add(tableSource(from.name.toString()))
                        }
                    }
                }

                if(node is Table){
                    statement.inputTables.add(tableSource(node.name.toString()))
                }

                if (CollUtil.isNotEmpty(children)) {
                    maxDepthLeaf(children,statement)
                }
            }
        }
    }
}
