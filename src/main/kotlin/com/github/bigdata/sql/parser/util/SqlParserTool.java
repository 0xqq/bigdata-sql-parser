package com.github.bigdata.sql.parser.util;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.SimpleNode;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.index.CreateIndex;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.execute.Execute;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.merge.Merge;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import net.sf.jsqlparser.statement.upsert.Upsert;
import net.sf.jsqlparser.util.TablesNamesFinder;
import org.apache.calcite.avatica.SqlType;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @description: jsqlparser解析SQL工具类
 * PlainSelect类不支持union、union all等请使用SetOperationList接口
 * Author: duhanmin
 * Description:
 * Date: 2021/12/28 17:56
 */
public class SqlParserTool {


    /**
     * 由于jsqlparser没有获取SQL类型的原始工具，并且在下面操作时需要知道SQL类型，所以编写此工具方法
     *
     * @param sql sql语句
     * @return sql类型，
     * @throws JSQLParserException
     */
    public static SqlType getSqlType(String sql) throws JSQLParserException {
        Statement sqlStmt = CCJSqlParserUtil.parse(new StringReader(sql));
        if (sqlStmt instanceof Alter) {
            return SqlType.ALTER;
        } else if (sqlStmt instanceof CreateIndex) {
            return SqlType.CREATEINDEX;
        } else if (sqlStmt instanceof CreateTable) {
            return SqlType.CREATETABLE;
        } else if (sqlStmt instanceof CreateView) {
            return SqlType.CREATEVIEW;
        } else if (sqlStmt instanceof Delete) {
            return SqlType.DELETE;
        } else if (sqlStmt instanceof Drop) {
            return SqlType.DROP;
        } else if (sqlStmt instanceof Execute) {
            return SqlType.EXECUTE;
        } else if (sqlStmt instanceof Insert) {
            return SqlType.INSERT;
        } else if (sqlStmt instanceof Merge) {
            return SqlType.MERGE;
        } else if (sqlStmt instanceof Replace) {
            return SqlType.REPLACE;
        } else if (sqlStmt instanceof Select) {
            return SqlType.SELECT;
        } else if (sqlStmt instanceof Truncate) {
            return SqlType.TRUNCATE;
        } else if (sqlStmt instanceof Update) {
            return SqlType.UPDATE;
        } else if (sqlStmt instanceof Upsert) {
            return SqlType.UPSERT;
        } else {
            return SqlType.NONE;
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
    public static Statement getStatement(String sql) throws JSQLParserException {
        Statement sqlStmt = CCJSqlParserUtil.parse(new StringReader(sql));
        return sqlStmt;
    }

    /**
     * 获取tables的表名
     *
     * @param statement
     * @return
     */
    public static List<String> getTableList(Select statement) {
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tableList = tablesNamesFinder.getTableList(statement);
        return tableList;
    }

    /**
     * 获取join层级
     *
     * @param selectBody
     * @return
     */
    public static List<Join> getJoins(SelectBody selectBody) {
        if (selectBody instanceof PlainSelect) {
            List<Join> joins = ((PlainSelect) selectBody).getJoins();
            return joins;
        }
        return new ArrayList<Join>();
    }

    /**
     * @param selectBody
     * @return
     */
    public static List<Table> getIntoTables(SelectBody selectBody) {
        if (selectBody instanceof PlainSelect) {
            List<Table> tables = ((PlainSelect) selectBody).getIntoTables();
            return tables;
        }
        return new ArrayList<Table>();
    }

    /**
     * @param selectBody
     * @return
     */
    public static void setIntoTables(SelectBody selectBody, List<Table> tables) {
        if (selectBody instanceof PlainSelect) {
            ((PlainSelect) selectBody).setIntoTables(tables);
        }
    }

    /**
     * 获取limit值
     *
     * @param selectBody
     * @return
     */
    public static Limit getLimit(SelectBody selectBody) {
        if (selectBody instanceof PlainSelect) {
            Limit limit = ((PlainSelect) selectBody).getLimit();
            return limit;
        }
        return null;
    }

    /**
     * 为SQL增加limit值
     *
     * @param selectBody
     * @param l
     */
    public static void setLimit(SelectBody selectBody, long l) {
        if (selectBody instanceof PlainSelect) {
            Limit limit = new Limit();
            limit.setRowCount(new LongValue(String.valueOf(l)));
            ((PlainSelect) selectBody).setLimit(limit);
        }
    }

    /**
     * 获取FromItem不支持子查询操作
     *
     * @param selectBody
     * @return
     */
    public static FromItem getFromItem(SelectBody selectBody) {
        if (selectBody instanceof PlainSelect) {
            FromItem fromItem = ((PlainSelect) selectBody).getFromItem();
            return fromItem;
        } else if (selectBody instanceof WithItem) {
            SqlParserTool.getFromItem(((WithItem) selectBody).getSelectBody());
        }
        return null;
    }

    /**
     * 获取子查询
     *
     * @param selectBody
     * @return
     */
    public static SubSelect getSubSelect(SelectBody selectBody) {
        if (selectBody instanceof PlainSelect) {
            FromItem fromItem = ((PlainSelect) selectBody).getFromItem();
            if (fromItem instanceof SubSelect) {
                return ((SubSelect) fromItem);
            }
        } else if (selectBody instanceof WithItem) {
            SqlParserTool.getSubSelect(((WithItem) selectBody).getSelectBody());
        }
        return null;
    }

    /**
     * 判断是否为多级子查询
     *
     * @param selectBody
     * @return
     */
    public static boolean isMultiSubSelect(SelectBody selectBody) {
        if (selectBody instanceof PlainSelect) {
            FromItem fromItem = ((PlainSelect) selectBody).getFromItem();
            if (fromItem instanceof SubSelect) {
                SelectBody subBody = ((SubSelect) fromItem).getSelectBody();
                if (subBody instanceof PlainSelect) {
                    FromItem subFromItem = ((PlainSelect) subBody).getFromItem();
                    if (subFromItem instanceof SubSelect) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 获取查询字段
     *
     * @param selectBody
     * @return
     */
    public static List<SelectItem> getSelectItems(SelectBody selectBody) {
        if (selectBody instanceof PlainSelect) {
            List<SelectItem> selectItems = ((PlainSelect) selectBody).getSelectItems();
            return selectItems;
        }
        return null;
    }

    public static List<String> getSelectColumnName(String selectSql) throws JSQLParserException {

        SqlType sqlType = getSqlType(selectSql);
        List<String> items = new ArrayList<>();

        if (sqlType.equals(SqlType.SELECT)) {
            Select select = (Select) SqlParserTool.getStatement(selectSql);
            PlainSelect plain = (PlainSelect) select.getSelectBody();
            List<SelectItem> selectItems = plain.getSelectItems();

            if (selectItems != null) {
                for (SelectItem selectItem : selectItems) {
                    if (selectItem instanceof SelectExpressionItem) {
                        SelectExpressionItem selectExpressionItem = (SelectExpressionItem) selectItem;

                        String columnName = "";
                        Alias alias = selectExpressionItem.getAlias();

                        Expression expression = selectExpressionItem.getExpression();
                        if (expression instanceof CaseExpression) {
                            // case表达式
                            columnName = alias.getName();
                        } else if (expression instanceof LongValue || expression instanceof StringValue || expression instanceof DateValue || expression instanceof DoubleValue) {
                            // 值表达式
                            columnName = Objects.nonNull(alias.getName()) ? alias.getName() : expression.getASTNode().jjtGetValue().toString();
                        } else if (expression instanceof TimeKeyExpression) {
                            // 日期
                            columnName = alias.getName();
                        } else {
                            if (alias != null) {
                                columnName = alias.getName();
                            } else {
                                SimpleNode node = expression.getASTNode();
                                Object value = node.jjtGetValue();
                                if (value instanceof Column) {
                                    columnName = ((Column) value).getColumnName();
                                } else if (value instanceof Function) {
                                    columnName = value.toString();
                                } else {
                                    // 增加对select 'aaa' from table; 的支持
                                    columnName = String.valueOf(value);
                                    columnName = columnName.replace("'", "");
                                    columnName = columnName.replace("\"", "");
                                    columnName = columnName.replace("`", "");
                                }
                            }
                        }

                        columnName = columnName.replace("'", "");
                        columnName = columnName.replace("\"", "");
                        columnName = columnName.replace("`", "");

                        items.add(columnName);
                    } else if (selectItem instanceof AllTableColumns) {
                        AllTableColumns allTableColumns = (AllTableColumns) selectItem;
                        items.add(allTableColumns.toString());
                    } else {
                        items.add(selectItem.toString());
                    }
                }
            }
        }
        return items;
    }

    public static void main(String[] args) throws JSQLParserException {
        String sql = "select userid,username as name,CASE WHEN age < 18 THEN 0 WHEN age >= 18 AND age < 60 THEN 1 ELSE 2 END AS age, sex  from student";
        /*SqlType sqlType = SqlParserTool.getSqlType(sql);
        if (sqlType.equals(SqlType.SELECT)) {
            Select statement = (Select) SqlParserTool.getStatement(sql);
            SelectBody selectBody = statement.getSelectBody();
            SubSelect subSelect = SqlParserTool.getSubSelect(selectBody);
            System.out.println(subSelect);
            List<SelectItem> selectItems = getSelectItems(subSelect.getSelectBody());
            selectItems.forEach(selectItem -> System.out.println(selectItem.getASTNode()));

        }*/
        List<String> columnName = getSelectColumnName(sql);
        for (String s : columnName) {
            System.out.println(s);
        }
    }

    /**
     * @description: 定义sql返回类型
     * @author: ZhiWen
     * @create: 2021-02-23
     **/
    public enum SqlType {
        ALTER,
        CREATEINDEX,
        CREATETABLE,
        CREATEVIEW,
        DELETE,
        DROP,
        EXECUTE,
        INSERT,
        MERGE,
        REPLACE,
        SELECT,
        TRUNCATE,
        UPDATE,
        UPSERT,
        NONE
    }

    /*不需要引入依赖
    * <dependency>
            <groupId>com.github.jsqlparser</groupId>
            <artifactId>jsqlparser</artifactId>
            <version>1.4</version>
        </dependency>
    * */

}




