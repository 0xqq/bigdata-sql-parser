package com.github.bigdata.sql.parser.datax.writer;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: duhanmin
 * Description:
 * Date: 2021/12/28 15:38
 */
public class S3Writer {
    private String name;
    private JSONObject writer;
    private String db;
    private String table;
    private List<String> columns = new ArrayList<>();

    public S3Writer(String name, JSONObject writer) throws Exception {
        this.name = name;
        this.writer = writer;
        init();
    }

    private void init() throws Exception {
        final String path = writer.getStr("path");
        final String[] split = path.split("/");
        if (!(split.length >= 3)) throw new RuntimeException("path 无法解析出库和表,path:"+ ArrayUtil.toString(split));
        db = split[1];
        table = split[2];

        final JSONArray column = writer.getJSONArray("column");
        for (int i = 0; i < column.size(); i++) {
            final String name = column.getJSONObject(i).getStr("name");
            columns.add(name);
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
}
