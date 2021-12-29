package com.github.bigdata.sql.parser.util

import net.sf.jsqlparser.JSQLParserException
import net.sf.jsqlparser.expression.*
import net.sf.jsqlparser.expression.Function
import net.sf.jsqlparser.parser.CCJSqlParserUtil
import net.sf.jsqlparser.schema.Column
import net.sf.jsqlparser.schema.Table
import net.sf.jsqlparser.statement.Statement
import net.sf.jsqlparser.statement.alter.Alter
import net.sf.jsqlparser.statement.create.index.CreateIndex
import net.sf.jsqlparser.statement.create.table.CreateTable
import net.sf.jsqlparser.statement.create.view.CreateView
import net.sf.jsqlparser.statement.delete.Delete
import net.sf.jsqlparser.statement.drop.Drop
import net.sf.jsqlparser.statement.execute.Execute
import net.sf.jsqlparser.statement.insert.Insert
import net.sf.jsqlparser.statement.merge.Merge
import net.sf.jsqlparser.statement.replace.Replace
import net.sf.jsqlparser.statement.select.*
import net.sf.jsqlparser.statement.truncate.Truncate
import net.sf.jsqlparser.statement.update.Update
import net.sf.jsqlparser.statement.upsert.Upsert
import net.sf.jsqlparser.util.TablesNamesFinder
import java.io.StringReader
import java.util.*

/**
 *
 * Author: duhanmin
 * Description:
 * Date: 2021/12/29 11:21
 */
object SqlParserTool {

    /**
     * 由于jsqlparser没有获取SQL类型的原始工具，并且在下面操作时需要知道SQL类型，所以编写此工具方法
     *
     * @param sql sql语句
     * @return sql类型，
     * @throws JSQLParserException
     */
    @Throws(JSQLParserException::class)
    fun getSqlType(sql: String?): SqlType {
        val sqlStmt = CCJSqlParserUtil.parse(StringReader(sql))
        return if (sqlStmt is Alter) {
            SqlType.ALTER
        } else if (sqlStmt is CreateIndex) {
            SqlType.CREATEINDEX
        } else if (sqlStmt is CreateTable) {
            SqlType.CREATETABLE
        } else if (sqlStmt is CreateView) {
            SqlType.CREATEVIEW
        } else if (sqlStmt is Delete) {
            SqlType.DELETE
        } else if (sqlStmt is Drop) {
            SqlType.DROP
        } else if (sqlStmt is Execute) {
            SqlType.EXECUTE
        } else if (sqlStmt is Insert) {
            SqlType.INSERT
        } else if (sqlStmt is Merge) {
            SqlType.MERGE
        } else if (sqlStmt is Replace) {
            SqlType.REPLACE
        } else if (sqlStmt is Select) {
            SqlType.SELECT
        } else if (sqlStmt is Truncate) {
            SqlType.TRUNCATE
        } else if (sqlStmt is Update) {
            SqlType.UPDATE
        } else if (sqlStmt is Upsert) {
            SqlType.UPSERT
        } else {
            SqlType.NONE
        }
    }

    /**
     * 获取sql操作接口,与上面类型判断结合使用
     * example:
     * String sql = "create table a(a string)";
     * SqlType sqlType = SqlParserTool.getSqlType(sql);
     * if(sqlType.equals(SqlType.SELECT)){
     * Select statement = (Select) SqlParserTool.getStatement(sql);
     * }
     *
     * @param sql
     * @return
     * @throws JSQLParserException
     */
    @Throws(JSQLParserException::class)
    fun getStatement(sql: String?): Statement? {
        return CCJSqlParserUtil.parse(StringReader(sql))
    }

    /**
     * 获取tables的表名
     *
     * @param statement
     * @return
     */
    fun getTableList(statement: Select?): List<String?>? {
        val tablesNamesFinder = TablesNamesFinder()
        return tablesNamesFinder.getTableList(statement)
    }

    /**
     * 获取join层级
     *
     * @param selectBody
     * @return
     */
    fun getJoins(selectBody: SelectBody?): List<Join?>? {
        return if (selectBody is PlainSelect) {
            selectBody.joins
        } else ArrayList()
    }

    /**
     * @param selectBody
     * @return
     */
    fun getIntoTables(selectBody: SelectBody?): List<Table?>? {
        return if (selectBody is PlainSelect) {
            selectBody.intoTables
        } else ArrayList()
    }

    /**
     * @param selectBody
     * @return
     */
    fun setIntoTables(selectBody: SelectBody?, tables: List<Table?>?) {
        if (selectBody is PlainSelect) {
            selectBody.intoTables = tables
        }
    }

    /**
     * 获取limit值
     *
     * @param selectBody
     * @return
     */
    fun getLimit(selectBody: SelectBody?): Limit? {
        return if (selectBody is PlainSelect) {
            selectBody.limit
        } else null
    }

    /**
     * 为SQL增加limit值
     *
     * @param selectBody
     * @param l
     */
    fun setLimit(selectBody: SelectBody?, l: Long) {
        if (selectBody is PlainSelect) {
            val limit = Limit()
            limit.rowCount = LongValue(l.toString())
            selectBody.limit = limit
        }
    }

    /**
     * 获取FromItem不支持子查询操作
     *
     * @param selectBody
     * @return
     */
    fun getFromItem(selectBody: SelectBody?): FromItem? {
        if (selectBody is PlainSelect) {
            return selectBody.fromItem
        } else if (selectBody is WithItem) {
            getFromItem(selectBody.selectBody)
        }
        return null
    }

    /**
     * 获取子查询
     *
     * @param selectBody
     * @return
     */
    fun getSubSelect(selectBody: SelectBody?): SubSelect? {
        if (selectBody is PlainSelect) {
            val fromItem = selectBody.fromItem
            if (fromItem is SubSelect) {
                return fromItem
            }
        } else if (selectBody is WithItem) {
            getSubSelect(selectBody.selectBody)
        }
        return null
    }

    /**
     * 判断是否为多级子查询
     *
     * @param selectBody
     * @return
     */
    fun isMultiSubSelect(selectBody: SelectBody?): Boolean {
        if (selectBody is PlainSelect) {
            val fromItem = selectBody.fromItem
            if (fromItem is SubSelect) {
                val subBody = fromItem.selectBody
                if (subBody is PlainSelect) {
                    val subFromItem = subBody.fromItem
                    if (subFromItem is SubSelect) {
                        return true
                    }
                }
            }
        }
        return false
    }

    /**
     * 获取查询字段
     *
     * @param selectBody
     * @return
     */
    fun getSelectItems(selectBody: SelectBody?): List<SelectItem?>? {
        return if (selectBody is PlainSelect) {
            selectBody.selectItems
        } else null
    }

    @Throws(JSQLParserException::class)
    public fun getSelectColumnName(selectSql: String?): List<String> {
        val sqlType = getSqlType(selectSql)
        val items: MutableList<String> = ArrayList()
        if (sqlType == SqlType.SELECT) {
            val select = getStatement(selectSql) as Select
            val plain = select.selectBody as PlainSelect
            val selectItems = plain.selectItems
            if (selectItems != null) {
                for (selectItem in selectItems) {
                    if (selectItem is SelectExpressionItem) {
                        val selectExpressionItem = selectItem
                        var columnName = ""
                        val alias = selectExpressionItem.alias
                        val expression = selectExpressionItem.expression
                        if (expression is CaseExpression) {
                            // case表达式
                            columnName = alias!!.name
                        } else if (expression is LongValue || expression is StringValue || expression is DateValue || expression is DoubleValue) {
                            // 值表达式
                            columnName =
                                if (Objects.nonNull(alias!!.name)) alias.name else expression.astNode.jjtGetValue()
                                    .toString()
                        } else if (expression is TimeKeyExpression) {
                            // 日期
                            columnName = alias!!.name
                        } else {
                            if (alias != null) {
                                columnName = alias.name
                            } else {
                                val node = expression.astNode
                                val value = node.jjtGetValue()
                                if (value is Column) {
                                    columnName = value.columnName
                                } else if (value is Function) {
                                    columnName = value.toString()
                                } else {
                                    // 增加对select 'aaa' from table; 的支持
                                    columnName = value.toString()
                                    columnName = columnName.replace("'", "")
                                    columnName = columnName.replace("\"", "")
                                    columnName = columnName.replace("`", "")
                                }
                            }
                        }
                        columnName = columnName.replace("'", "")
                        columnName = columnName.replace("\"", "")
                        columnName = columnName.replace("`", "")
                        items.add(columnName)
                    } else if (selectItem is AllTableColumns) {
                        items.add(selectItem.toString())
                    } else {
                        items.add(selectItem.toString())
                    }
                }
            }
        }
        return items
    }

    /**
     * @description: 定义sql返回类型
     * @author: ZhiWen
     * @create: 2021-02-23
     */
    enum class SqlType {
        ALTER, CREATEINDEX, CREATETABLE, CREATEVIEW, DELETE, DROP, EXECUTE, INSERT, MERGE, REPLACE, SELECT, TRUNCATE, UPDATE, UPSERT, NONE
    }

    /*不需要引入依赖
    * <dependency>
            <groupId>com.github.jsqlparser</groupId>
            <artifactId>jsqlparser</artifactId>
            <version>1.4</version>
        </dependency>
    * */

    @Throws(JSQLParserException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val sql =
            "select userid,username as name,CASE WHEN age < 18 THEN 0 WHEN age >= 18 AND age < 60 THEN 1 ELSE 2 END AS age, sex  from student"
        /*SqlType sqlType = SqlParserTool.getSqlType(sql);
        if (sqlType.equals(SqlType.SELECT)) {
            Select statement = (Select) SqlParserTool.getStatement(sql);
            SelectBody selectBody = statement.getSelectBody();
            SubSelect subSelect = SqlParserTool.getSubSelect(selectBody);
            System.out.println(subSelect);
            List<SelectItem> selectItems = getSelectItems(subSelect.getSelectBody());
            selectItems.forEach(selectItem -> System.out.println(selectItem.getASTNode()));

        }*/

        val columnName = SqlParserTool.getSelectColumnName(sql)
        for (s in columnName) {
            println(s)
        }
    }
}
