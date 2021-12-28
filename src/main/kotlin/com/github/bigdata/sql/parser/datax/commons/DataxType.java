package com.github.bigdata.sql.parser.datax.commons;

/**
 * Author: duhanmin
 * Description:
 * Date: 2021/12/28 16:09
 */
public interface DataxType {

    enum Reader {
        mysql("mysqlreader"),pg("postgresqlreader"),s3("s3reader");
        private String code;

        Reader(String name) {
            this.code = name;
        }

        public String getCode() {
            return code;
        }
    }

    enum Writer {
        mysql("mysqlwriter"),pg("postgresqlwriter"),s3("s3writer");
        private String code;

        Writer(String name) {
            this.code = name;
        }

        public String getCode() {
            return code;
        }
    }
}
