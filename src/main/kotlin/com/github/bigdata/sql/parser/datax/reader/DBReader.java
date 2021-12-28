package com.github.bigdata.sql.parser.datax.reader;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.github.bigdata.sql.parser.Statement;
import com.github.bigdata.sql.parser.TableData;
import com.github.bigdata.sql.parser.TableSource;
import com.github.bigdata.sql.parser.datax.commons.DataxType;
import com.github.bigdata.sql.parser.tidb.TidbSQLHelper;
import com.github.bigdata.sql.parser.util.SqlParserTool;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: duhanmin
 * Description:
 * Date: 2021/12/28 15:38
 */
public class DBReader {

    private String name;
    private JSONObject reader;
    private String db;
    private String table;
    private List<String> columns = new ArrayList<>();

    public DBReader(String name,JSONObject reader) throws Exception {
        this.name = name;
        this.reader = reader;
        init();

    }

    private void init() throws Exception {
        final JSONObject connection = reader.getJSONArray("connection").getJSONObject(0);
        final String jdbcUrl = connection.getJSONArray("jdbcUrl").getStr(0);
        final String[] split = jdbcUrl.split("/");

        db = split[split.length-1];

        if (DataxType.Reader.pg.getCode().equals(name)){
            pg(connection);
        }else{
            db(connection);
        }

    }

    private void pg(JSONObject connection) throws Exception {
        if (!reader.containsKey("column")){
            final String querySql = connection.getJSONArray("querySql").getStr(0);
            final Statement statement = TidbSQLHelper.getStatementData(querySql).getStatement();

            TableData tableData;
            if (statement instanceof TableData) {
                tableData = (TableData) statement;
            }else {
                throw new RuntimeException("血缘无法解析 -> "+statement);
            }

            final TableSource tableSource = tableData.getInputTables().get(0);
            String databaseName = tableSource.getDatabaseName();
            if (StrUtil.isBlank(databaseName)) databaseName = "public";
            table = databaseName + "." + tableSource.getTableName();
            columns.addAll(SqlParserTool.getSelectColumnName(querySql));
        }else {
            final String table = connection.getJSONArray("table").getStr(0);
            if (!table.contains(".")){
                this.table = "public" + "." + table;
            }
            final JSONArray column = reader.getJSONArray("column");
            columns.addAll(JSONUtil.toList(column,String.class));
        }
    }

    private void db(JSONObject connection) throws Exception {
        if (!reader.containsKey("column")){
            final String querySql = connection.getJSONArray("querySql").getStr(0);
            final Statement statement = TidbSQLHelper.getStatementData(querySql).getStatement();

            TableData tableData;
            if (statement instanceof TableData) {
                tableData = (TableData) statement;
            }else {
                throw new RuntimeException("血缘无法解析 -> "+statement);
            }

            final TableSource tableSource = tableData.getInputTables().get(0);

            table = tableSource.getTableName();
            columns.addAll(SqlParserTool.getSelectColumnName(querySql));
        }else {
            table = connection.getJSONArray("table").getStr(0);
            final JSONArray column = reader.getJSONArray("column");
            columns.addAll(JSONUtil.toList(column,String.class));
        }
    }

    public String getDb() {
        return db;
    }

    public void setDb(String db) {
        this.db = db;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public List<String> getColumns() {
        return columns;
    }

    public void setColumns(List<String> columns) {
        this.columns = columns;
    }

    @Override
    public String toString() {
        return JSONUtil.toJsonStr(this);
    }
}
