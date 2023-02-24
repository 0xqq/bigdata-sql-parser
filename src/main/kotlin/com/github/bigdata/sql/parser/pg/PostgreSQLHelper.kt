package com.github.bigdata.sql.parser.pg

import cn.hutool.core.collection.CollUtil
import com.github.bigdata.sql.antlr4.pg.PostgreSQLLexer
import com.github.bigdata.sql.antlr4.pg.PostgreSQLParser
import com.github.bigdata.sql.antlr4.pg.PostgreSQLParser.ExprContext
import com.github.bigdata.sql.antlr4.pg.PostgreSQLParser.Select_stmtContext
import com.github.bigdata.sql.antlr4.pg.PostgreSQLParserBaseVisitor
import com.github.bigdata.sql.parser.StatementData
import com.github.bigdata.sql.parser.StatementType
import com.github.bigdata.sql.parser.TableData
import com.github.bigdata.sql.parser.TableSource
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTree
import java.util.function.Consumer


class PostgreSQLHelper : PostgreSQLParserBaseVisitor<Any?>() {

    private var currentOptType: StatementType = StatementType.PG
    private val statementData = TableData()

    override fun visitSelect_stmt(ctx: Select_stmtContext): Any? {
        val fromItemcontext = ctx.from_clause().from_item().get(0)
        val dbTable = fromItemcontext.text.split(".")
        var databaseName = "public"
        var tableName = ""
        if (dbTable.size == 1) {
            tableName = dbTable.get(0)
        } else {
            databaseName = dbTable.get(0)
            tableName = dbTable.get(1)
        }

        val columns: MutableList<String> = CollUtil.newArrayList()
        ctx.selector_clause().column_list().expr().forEach(Consumer { exprContext: ExprContext ->
            exprContext.identifier().forEach(
                Consumer { identifierContext: PostgreSQLParser.IdentifierContext ->
                    columns.add(identifierContext.ruleContext.text)
                })
        })
        var tableSource = TableSource(databaseName, tableName, columns)
        this.statementData.inputTables.add(tableSource)
        return super.visitSelect_stmt(ctx)
    }

    fun parseSqlFieldLineage(sql: String): PostgreSQLHelper {
        val visitor = PostgreSQLHelper()
        visitor.visit(getParseTree(sql))
        return visitor
    }

    fun getStatementData(): StatementData {
        return StatementData(currentOptType, statementData)
    }

    private fun getParseTree(sql: String): ParseTree? {
        val input: CharStream = CharStreams.fromString(sql)
        val lexer = PostgreSQLLexer(input)
        val tokenStream = CommonTokenStream(lexer)
        val parser = PostgreSQLParser(tokenStream)
        return parser.root()
    }

}