package com.github.bigdata.sql.parser.job

import com.github.bigdata.sql.parser.JobData
import com.github.bigdata.sql.parser.StatementType
import com.github.bigdata.sql.parser.SetData
import com.github.bigdata.sql.parser.UnSetData
import org.junit.Assert
import org.junit.Test

/**
 *
 * Created by binsong.li on 2018/3/31 下午1:59
 */
class JobTaskParserTest {

    @Test
    fun setConfigTest1() {
        val sql = """
            set flink.test = 'hello world';
            set flink.test = setsd,sd,resr;
            set flink.test = hello world;
            set flink.test = hello-world;
            set flink.test = hello $\{usename} test;
            #set flink.test = hello comment;
            set flink.test = hello 'test' world;
            set flink.test = hello "test" world;
            set flink.test = hdfs://user/hive;
            set flink.test = 12,12;
            set flink.test = 3.45;
            set flink.test = ibdex.json;
            unset flink.test;
            set flink.test = dw.eset_sdfe_sd;
            set flink.test = demo.test;
            set flink.test = dsd(id)%=2;
            tet_test-demo_1.23-sdfd.jar cn.github.Demo1 param1  param2 'hello \n world'
             'hdfs://user/hive'
             '{"user": "binsong.li",
               address: "hangzhou"
             }'
             /user/jars/*
             --jars /user/jars/flink.jar
            """;

        val statementDatas = JobTaskHelper.getStatementData(sql)
        Assert.assertEquals(16, statementDatas.size)

        var statementData = statementDatas.get(0)
        var statement = statementData.statement
        if (statement is SetData) {
            Assert.assertEquals(StatementType.SET, statementData.type)
            Assert.assertEquals("flink.test", statement.key)
            Assert.assertEquals("hello world", statement.value)
        } else {
            Assert.fail()
        }

        var setCount = 0;
        statementDatas.filter { it.type == StatementType.SET }.forEach{
            setCount = setCount + 1;
        }

        Assert.assertEquals(14, setCount)

        statementData = statementDatas.get(11)
        statement = statementData.statement
        if (statement is UnSetData) {
            Assert.assertEquals(StatementType.UNSET, statementData.type)
            Assert.assertEquals("flink.test", statement.key)
        } else {
            Assert.fail()
        }

        statementData = statementDatas.get(15)
        statement = statementData.statement
        if (statement is JobData) {
            Assert.assertEquals(StatementType.JOB, statementData.type)
            Assert.assertEquals("tet_test-demo_1.23-sdfd.jar", statement.resourceName)
            Assert.assertEquals("cn.github.Demo1", statement.className)
            Assert.assertEquals(8, statement.params?.size)
            Assert.assertEquals("/user/jars/*", statement.params?.get(5))
        } else {
            Assert.fail()
        }
    }

    @Test
    fun setConfigTest2() {
        val sql =
                "demo.jar cn.github.Demo 'hello \"test\" world' param2 \n param3";

        val statementDatas = JobTaskHelper.getStatementData(sql)
        Assert.assertEquals(1, statementDatas.size)

        var statementData = statementDatas.get(0)
        var statement = statementData.statement
        if (statement is JobData) {
            Assert.assertEquals(StatementType.JOB, statementData.type)
            Assert.assertEquals("demo.jar", statement.resourceName)
            Assert.assertEquals("cn.github.Demo", statement.className)
            Assert.assertEquals(3, statement.params?.size)

            Assert.assertEquals("hello \"test\" world", statement.params?.get(0))
            Assert.assertEquals("param3", statement.params?.get(2))
        } else {
            Assert.fail()
        }
    }

    @Test
    fun setConfigTest3() {
        val sql = """
            set spark.yarn.queue=newoffline;
            set spark.sql.autoBroadcastJoinThreshold=40485760;
            set spark.executor.memory=8g;
            set spark.sql.hive.convertMetastoreParquet=true ;
            set spark.driver.maxResultSize=4g;
            set spark.driver.memory=10g;
            set spark.psi.ds=20180312;
            set spark.psi.dstTable=dw.index_psi_dt;
            set spark.psi.dims=idnumber;
            set spark.psi.num=0;
            set spark.psi.parNum=2;
            set spark.indexPSI.type=2;
            set spark.metrics.indexFile=index-mobile.json;
            psi_new_calculate_metrics-1.1-SNAPSHOT-jar-with-dependencies.jar  cn.github.dw.psi.StartDCJob 1,3,sd,qw
            """;

        val statementDatas = JobTaskHelper.getStatementData(sql)
        Assert.assertEquals(14, statementDatas.size)

        var statementData = statementDatas.get(13)
        var statement = statementData.statement
        if (statement is JobData) {
            Assert.assertEquals(StatementType.JOB, statementData.type)
            Assert.assertEquals("psi_new_calculate_metrics-1.1-SNAPSHOT-jar-with-dependencies.jar", statement.resourceName)
            Assert.assertEquals("cn.github.dw.psi.StartDCJob", statement.className)
            Assert.assertEquals("1,3,sd,qw", statement.params?.get(0))
        } else {
            Assert.fail()
        }
    }

    @Test
    fun setConfigTest4() {
        val sql = """
            set spark.yarn.queue=newoffline;
set spark.dynamicAllocation.maxExecutors=100;
set spark.driver.maxResultSize=30g;
set spark.driver.memory=15g;
set spark.executor.instances=80;
set spark.executor.cores=5;
set spark.executor.memory=30g;
set spark.sql.shuffle.partitions=5000;
set spark.shuffle.io.maxRetries=60;
set spark.shuffle.io.retryWait=60s;
set spark.metrics.indexFile=index-mobile.json;
set spark.metrics.indexInputTable=dw.dwa_mobile_model_dt;
set spark.metrics.indexOutputTable=dw.app_mdl_mobile_index_dt;
set spark.metrics.indexWaitFullPartition=true;
set spark.metrics.indexHashWhere=abs(hash(mobile))%4=3;
new_calculate_metrics-100-SNAPSHOT-jar-with-dependencies.jar cn.github.dw.index.StartDCJob IndexOffline 2018-03-18
            """;

        val statementDatas = JobTaskHelper.getStatementData(sql)
        Assert.assertEquals(16, statementDatas.size)

        var statementData = statementDatas.get(15)
        var statement = statementData.statement
        if (statement is JobData) {
            Assert.assertEquals(StatementType.JOB, statementData.type)
            Assert.assertEquals("new_calculate_metrics-100-SNAPSHOT-jar-with-dependencies.jar", statement.resourceName)
            Assert.assertEquals("cn.github.dw.index.StartDCJob", statement.className)
            Assert.assertEquals("2018-03-18", statement.params?.get(1))
        } else {
            Assert.fail()
        }
    }

    @Test
    fun setConfigTest5() {
        val sql = """
            set spark.shuffle.compress=true;set spark.rdd.compress=true;
set spark.driver.maxResultSize=3g;
set spark.serializer=org.apache.spark.serializer.KryoSerializer;
set spark.kryoserializer.buffer.max=1024m;
set spark.kryoserializer.buffer=256m;
set spark.network.timeout=300s;
createHfile-1.2-SNAPSHOT-jar-with-dependencies.jar imei_test.euSaveHBase gaea_offline:account_mobile sh md shda.interest_radar_mobile_score_dt 20180318 /xxxx/sh/mobile/loan 400 '%7B%22job_type%22=' --jar
            """;

        val statementDatas = JobTaskHelper.getStatementData(sql)
        Assert.assertEquals(8, statementDatas.size)

        var statementData = statementDatas.get(7)
        var statement = statementData.statement
        if (statement is JobData) {
            Assert.assertEquals(StatementType.JOB, statementData.type)
            Assert.assertEquals("createHfile-1.2-SNAPSHOT-jar-with-dependencies.jar", statement.resourceName)
            Assert.assertEquals("imei_test.euSaveHBase", statement.className)
            Assert.assertEquals("/xxxx/sh/mobile/loan", statement.params?.get(5))
            Assert.assertEquals("400", statement.params?.get(6))
            Assert.assertEquals("%7B%22job_type%22=", statement.params?.get(7))
            Assert.assertEquals("--jar", statement.params?.get(8))
        } else {
            Assert.fail()
        }
    }

    @Test
    fun setConfigTest6() {
        val sql = """
            set spark.toMysql.url=jdbc:mysql://192.168.40.110:3306/data_quality;set spark.toMysql.user=dq;
set spark.toMysql.password=0nlpSvpgC5leeKuw;
set spark.screenJob.screenType=3;
set spark.screenJob.test=true;
set spark.screenTool.srcTable=default.activity_flat;
set spark.toMysql.tableName=province;
set spark.toMysql.field=creditProvinceAmountJson,creditForeignAmount,creditCityAmountJson,zhejiangFraud,fraudProvinceAmountJson;
province-1.0-SNAPSHOT-jar-with-dependencies.jar com.github.screen_dc.ScreenJob /xxxx/2017-22-03/sh/mobile/loan/ --write-private-test;
province-1.0-SNAPSHOT-jar-with-dependencies.jar com.github.screen_dc.ScreenJob /xxxx/2017-22-03/sh/mobile/loan/ --write-private-test
            """;

        val statementDatas = JobTaskHelper.getStatementData(sql)
        Assert.assertEquals(10, statementDatas.size)

        var statementData = statementDatas.get(9)
        var statement = statementData.statement
        if (statement is JobData) {
            Assert.assertEquals(StatementType.JOB, statementData.type)
            Assert.assertEquals("province-1.0-SNAPSHOT-jar-with-dependencies.jar", statement.resourceName)
            Assert.assertEquals("com.github.screen_dc.ScreenJob", statement.className)
            Assert.assertEquals("/xxxx/2017-22-03/sh/mobile/loan/", statement.params?.get(0))
            Assert.assertEquals("--write-private-test", statement.params?.get(1))
        } else {
            Assert.fail()
        }
    }

    @Test
    fun setConfigTest7() {
        val sql = """
            raph.edgesSNAPSHOT.eventType.jar cn.github.graph.PhoenixCSVWriterJob graph_csv_s_2 /user/admin/bigdata/data/xxx/graph_csv_s_2/2018/12/day_12
            /user/admin/bigdata/data/xxx/graph_csv_s_new_2/Loan/2018/12/day_12 500 Loan hdfs://192.168.40.37,hdfs://192.168.39.133 hdfs://192.168.40.37,hdfs://192.168.39.130;
            """;

        val statementDatas = JobTaskHelper.getStatementData(sql)
        Assert.assertEquals(1, statementDatas.size)

        var statementData = statementDatas.get(0)
        var statement = statementData.statement
        if (statement is JobData) {
            Assert.assertEquals(StatementType.JOB, statementData.type)
            Assert.assertEquals("raph.edgesSNAPSHOT.eventType.jar", statement.resourceName)
            Assert.assertEquals("cn.github.graph.PhoenixCSVWriterJob", statement.className)
            Assert.assertEquals("hdfs://192.168.40.37,hdfs://192.168.39.130", statement.params?.get(6))
        } else {
            Assert.fail()
        }
    }

    @Test
    fun setConfigTest8() {
        val sql = """
            set spark.app.name=sparkAppName;set spark.memory.storageFraction=0.1;set spark.memory.fraction=0.95;set spark.memory.useLegacyMode=true;set master=yarn-cluster;/user/pontus_2.1/pontus-core-2.1.0-SNAPSHOT-fat.jar cn.github.pontus.core.Engine customCmd "-j{'readerFields':[{'field':'uuid','type':'string'},{'field':'rule_detail','type':'string'}],'resourceSetting':{'spark.driver.memory':'2g','spark.pontus.writer.mapper':'2'},'reader':{'databaseName':'afraudtech','connectionType':'hive','table':'antifraud_rule_result'},'writerFields':[{'transform':'uuid','field':'uuid','type':'varchar(32)'},{'filter':'where id=\'test\'','transform':'rule_detail','field':'policy_recommendation','type':'text'}],'writer':{'dataSourceId':'364','connectionAttr':'jdbc:mysql://192.168.74.136:3306/athena','password':'6ydJDezPBLBuco+sCV6QL6XsdTN/ShtYIz1Gi3TVusw=','writeMode':'UPSERT','userName':'athena','connectionType':'mysql','table':'edison_warning_result'}}"  --jars /user/pontus_2.1/*
            """;

        val statementDatas = JobTaskHelper.getStatementData(sql)
        Assert.assertEquals(6, statementDatas.size)

        var statementData = statementDatas.get(5)
        var statement = statementData.statement
        if (statement is JobData) {
            Assert.assertEquals(StatementType.JOB, statementData.type)
            Assert.assertEquals("/user/pontus_2.1/pontus-core-2.1.0-SNAPSHOT-fat.jar", statement.resourceName)
            Assert.assertEquals("cn.github.pontus.core.Engine", statement.className)
            Assert.assertEquals(4, statement.params?.size)
            val config = """
                -j{'readerFields':[{'field':'uuid','type':'string'},{'field':'rule_detail','type':'string'}],'resourceSetting':{'spark.driver.memory':'2g','spark.pontus.writer.mapper':'2'},'reader':{'databaseName':'afraudtech','connectionType':'hive','table':'antifraud_rule_result'},'writerFields':[{'transform':'uuid','field':'uuid','type':'varchar(32)'},{'filter':'where id=\'test\'','transform':'rule_detail','field':'policy_recommendation','type':'text'}],'writer':{'dataSourceId':'364','connectionAttr':'jdbc:mysql://192.168.74.136:3306/athena','password':'6ydJDezPBLBuco+sCV6QL6XsdTN/ShtYIz1Gi3TVusw=','writeMode':'UPSERT','userName':'athena','connectionType':'mysql','table':'edison_warning_result'}}
                """
            Assert.assertEquals(config.trim(), statement.params?.get(1))
        } else {
            Assert.fail()
        }
    }
}
