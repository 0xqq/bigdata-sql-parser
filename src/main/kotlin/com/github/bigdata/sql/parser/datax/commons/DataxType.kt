package com.github.bigdata.sql.parser.datax.commons

/**
 *
 * Author: duhanmin
 * Description:
 * Date: 2021/12/29 11:12
 */
class DataxType {
    enum class Reader(val code: String) {
        mysql("mysqlreader"), pg("postgresqlreader"), s3("s3reader");
    }

    enum class Writer(val code: String) {
        mysql("mysqlwriter"), pg("postgresqlwriter"), s3("s3writer");
    }
}
