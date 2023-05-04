package com.github.bigdata.sql.parser.presto

import cn.hutool.core.util.StrUtil
import cn.hutool.json.JSONUtil
import org.junit.Test

/**
 *
 * Author: duhanmin
 * Description:
 * Date: 2021/8/18 13:31
 */
class PrestoSqlParserTest {

    @Test
    fun sqlTest() {
        val sql = "with recursive t as (select * from a.x) select * from t"
        val statementData = PrestoSQLHelper.getStatementData(sql)
        println(statementData.statement)
    }

    @Test
    fun sql1Test() {
        val sql = "INSERT INTO adc.fsfd with recursive t as (select a,b,v from a.x)  select a,b,v from t"
        val statementData = PrestoSQLHelper.getStatementData(sql)
        println(statementData.statement)
    }

    @Test
    fun sql2Test() {
        val sql = "INSERT INTO a.adc.fsfd with recursive t as (select a,b,v from a.a.x)  select a,b,v from t.t.re"
        val statementData = PrestoSQLHelper.getStatementData(sql)
        println(statementData.statement)
    }


    @Test
    fun sql3Test() {
        val sql =
                "INSERT INTO hive.lb_bi_finance.dws_stock_profit_and_loss_flows_d\n" +
                "  WITH \n" +
                "  TABLE_DT AS (\n" +
                "    SELECT date_add('day', -1, Date(Date_parse('{flow_date}', '%Y%m%d'))) AS dt\n" +
                "  ),\n" +
                "  TABLE_IPO AS (\n" +
                "    -- 打新交易流水\n" +
                "    SELECT\n" +
                "        id,\n" +
                "        account_channel, -- 20210824临时修复\n" +
                "        aaid,\n" +
                "        counter_id,\n" +
                "        market,\n" +
                "        currency,\n" +
                "        lot_win_qty,\n" +
                "        luck_sub_amount,\n" +
                "        issue_price,\n" +
                "        sub_fee,\n" +
                "        luckly_fee,\n" +
                "        status,\n" +
                "        'BUY' AS direction,\n" +
                "        COALESCE(DATE_FORMAT(AT_TIMEZONE(luck_at, 'Asia/Shanghai'), '%Y%m%d%H%i%S%f'), CONCAT(pre_ipo_dt, '000000000000')) AS executed_time\n" +
                "    FROM hive.lb_bi_trade.dwb_ipo_trades_d\n" +
                "    WHERE pt = '{flow_date}'\n" +
                "        AND pre_ipo_dt = DATE_FORMAT((SELECT dt FROM TABLE_DT), '%Y%m%d') -- 暗盘日/公布中签日\n" +
                "        AND status IN (4, 5) -- 已公布中签，已公布未中签\n" +
                "  ),\n" +
                "  TABLE_LB_TRADE_FEE AS (\n" +
                "    -- lb股票交易费\n" +
                "    SELECT\n" +
                "        t2.account_channel,\n" +
                "        t2.aaid,\n" +
                "        t2.counter_id,\n" +
                "        t2.market,\n" +
                "        t2.currency,\n" +
                "        0 AS executed_qty,\n" +
                "        t1.amount AS executed_amount,\n" +
                "        0 AS executed_price,\n" +
                "        t2.direction,\n" +
                "        DATE_FORMAT((SELECT dt FROM TABLE_DT), '%Y%m%d%H%i%S%f') AS executed_time,\n" +
                "        IF(t1.biz_type = 1, 'lb_trade_fee', 'lb_option_trade_fee') AS flow_type,\n" +
                "        CONCAT('contract_id:', CAST(t2.id AS VARCHAR)) AS flow_desc,\n" +
                "        t1.biz_type,\n" +
                "        IF(t1.biz_type = 1, 'LONG', t4.position_side) AS position_side\n" +
                "    FROM\n" +
                "        (SELECT\n" +
                "            contract_id,\n" +
                "            amount,\n" +
                "            biz_type\n" +
                "        FROM hive.billing.contract_charges_di\n" +
                "        WHERE pt = '{flow_date}'\n" +
                "          AND state = 2 -- 1待冻结 2冻结中 3已经解冻\n" +
                "          AND (biz_type = 1 OR biz_type = 5) -- 1股票交易 2融资利息 3存款利息 4出金 5期权 6转仓\n" +
                "        ) t1\n" +
                "        LEFT JOIN\n" +
                "        (SELECT\n" +
                "            id,\n" +
                "            market,\n" +
                "            counter_id,\n" +
                "            IF(direction = 1, 'BUY', 'SELL') AS direction,\n" +
                "            currency,\n" +
                "            account_channel,\n" +
                "            aaid,\n" +
                "            biz_type\n" +
                "        FROM hive.billing.stock_exchange_contracts_di\n" +
                "        WHERE pt = '{flow_date}'\n" +
                "            AND biz_type IN (1, 5)-- 1 股票交易, 5 期权交易\n" +
                "        ) t2\n" +
                "        ON t1.contract_id = t2.id\n" +
                "        AND t1.biz_type=t2.biz_type\n" +
                "        LEFT JOIN \n" +
                "        (SELECT contract_id,\n" +
                "                MAX(order_id) AS order_id\n" +
                "         FROM hive.billing.stock_exchange_contract_orders_di\n" +
                "         WHERE pt = '{flow_date}'\n" +
                "         GROUP BY contract_id\n" +
                "        ) t3\n" +
                "        ON t2.id = t3.contract_id\n" +
                "        LEFT JOIN \n" +
                "        (SELECT order_id,position_side FROM hive.lb_bi_trade.dwb_lb_option_trade_done_d\n" +
                "         WHERE pt in(\n" +
                "            '{flow_date}',\n" +
                "            DATE_FORMAT(DATE_ADD('day', -1, DATE(DATE_PARSE('{flow_date}', '%Y%m%d'))), '%Y%m%d'),\n" +
                "            DATE_FORMAT(DATE_ADD('day', -2, DATE(DATE_PARSE('{flow_date}', '%Y%m%d'))), '%Y%m%d'),\n" +
                "            DATE_FORMAT(DATE_ADD('day', -3, DATE(DATE_PARSE('{flow_date}', '%Y%m%d'))), '%Y%m%d'),\n" +
                "            DATE_FORMAT(DATE_ADD('day', -4, DATE(DATE_PARSE('{flow_date}', '%Y%m%d'))), '%Y%m%d')\n" +
                "          )\n" +
                "        ) t4\n" +
                "        ON t3.order_id = t4.order_id\n" +
                "  )\n" +
                "  SELECT \n" +
                "      t1.dt,\n" +
                "      t1.member_id,\n" +
                "      t1.aaid,\n" +
                "      t1.counter_id,\n" +
                "      t1.market,\n" +
                "      t1.currency,\n" +
                "      t1.executed_qty,\n" +
                "      t1.executed_amount,\n" +
                "      t1.executed_price,\n" +
                "      t1.direction,\n" +
                "      t1.executed_time,\n" +
                "      t1.flow_type,\n" +
                "      t1.flow_desc,\n" +
                "      t1.position_side,\n" +
                "      t1.multiplier,\n" +
                "      (CASE WHEN split(t1.counter_id, '/')[1] = 'OP' AND t2.underlying_counter_id IS NOT NULL THEN t2.underlying_counter_id\n" +
                "            WHEN split(t1.counter_id, '/')[1] = 'WT' AND t3.underlying_counter_id IS NOT NULL THEN t3.underlying_counter_id\n" +
                "            ELSE t1.counter_id\n" +
                "            END\n" +
                "      ) AS underlying_counter_id,\n" +
                "      t1.account_channel,\n" +
                "      '{flow_date}' AS pt\n" +
                "  FROM    \n" +
                "      (SELECT\n" +
                "          (SELECT dt FROM TABLE_DT) AS dt,\n" +
                "          NULL AS member_id,\n" +
                "          account_channel,\n" +
                "          aaid,\n" +
                "          counter_id,\n" +
                "          market,\n" +
                "          currency,\n" +
                "          CAST(executed_qty AS DECIMAL(38, 10)) AS executed_qty,\n" +
                "          CAST(executed_amount AS DECIMAL(38, 10)) AS executed_amount,\n" +
                "          CAST(executed_price AS DECIMAL(38, 10)) AS executed_price,\n" +
                "          direction,\n" +
                "          executed_time,\n" +
                "          flow_type,\n" +
                "          flow_desc,\n" +
                "          position_side,\n" +
                "          multiplier     \n" +
                "      FROM\n" +
                "          (\n" +
                "          -- lb计算成本流水\n" +
                "          SELECT\n" +
                "              account_channel,\n" +
                "              aaid,\n" +
                "              counter_id,\n" +
                "              market,\n" +
                "              currency,\n" +
                "              executed_qty,\n" +
                "              executed_amount,\n" +
                "              executed_amount / (executed_qty * multiplier) AS executed_price,\n" +
                "              direction,\n" +
                "              executed_time,\n" +
                "              IF(flow_type = 'lb_corperate_action_diluted_avg_cost', 'lb_corperate_action', flow_type) AS flow_type,\n" +
                "              flow_desc,\n" +
                "              position_side,\n" +
                "              multiplier\n" +
                "          FROM hive.lb_bi_finance.dws_stock_flows_d\n" +
                "          WHERE pt = '{flow_date}' \n" +
                "              AND flow_type IN (\n" +
                "                'lb_stock_balance_change_ticket',\n" +
                "                'lb_trade_done',\n" +
                "                'lb_rollover',\n" +
                "                'lb_corperate_action_diluted_avg_cost',\n" +
                "                'stock_card',\n" +
                "                'lb_corperate_action',\n" +
                "                'lb_option_trade_done',\n" +
                "                'lb_option_giveup_exercise',\n" +
                "                'lb_option_due_exercise',\n" +
                "                'lb_cs_due_exercise',\n" +
                "                'lb_option_forced_liquidation_exercise',\n" +
                "                'lb_ipo_win'\n" +
                "              )\n" +
                "              AND executed_qty != 0-- 过滤后台脏数据\n" +
                "              \n" +
                "          UNION ALL\n" +
                "\n" +
                "          -- lb股票交易手续费\n" +
                "          SELECT\n" +
                "              account_channel,\n" +
                "              aaid,\n" +
                "              counter_id,\n" +
                "              market,\n" +
                "              currency,\n" +
                "              executed_qty,\n" +
                "              executed_amount,\n" +
                "              executed_price,\n" +
                "              direction,\n" +
                "              executed_time,\n" +
                "              flow_type,\n" +
                "              flow_desc,\n" +
                "              position_side,\n" +
                "              1 AS multiplier\n" +
                "          FROM TABLE_LB_TRADE_FEE\n" +
                "                \n" +
                "          UNION ALL\n" +
                "\n" +
                "          -- 打新认购费\n" +
                "          SELECT\n" +
                "              account_channel,\n" +
                "              aaid,\n" +
                "              counter_id,\n" +
                "              market,\n" +
                "              currency,\n" +
                "              0 AS executed_qty,\n" +
                "              sub_fee AS executed_amount,\n" +
                "              0 AS executed_price,\n" +
                "              direction,\n" +
                "              executed_time,\n" +
                "              'lb_ipo_sub_fee' AS flow_type,\n" +
                "              CONCAT('ipo order_id:', CAST(id AS VARCHAR)) AS flow_desc,\n" +
                "              'LONG' AS position_side,\n" +
                "              1 AS multiplier\n" +
                "          FROM TABLE_IPO\n" +
                "        \n" +
                "          UNION ALL\n" +
                "\n" +
                "          -- 打新中签费\n" +
                "          SELECT\n" +
                "              account_channel,\n" +
                "              aaid,\n" +
                "              counter_id,\n" +
                "              market,\n" +
                "              currency,\n" +
                "              0 AS executed_qty,\n" +
                "              luckly_fee AS executed_amount,\n" +
                "              0 AS executed_price,\n" +
                "              direction,\n" +
                "              executed_time,\n" +
                "              'lb_ipo_win_fee' AS flow_type,\n" +
                "              CONCAT('ipo order_id:', CAST(id AS VARCHAR)) AS flow_desc,\n" +
                "              'LONG' AS position_side,\n" +
                "              1 AS multiplier\n" +
                "          FROM TABLE_IPO\n" +
                "          WHERE status = 4 -- 已公布中签\n" +
                "\n" +
                "          UNION ALL\n" +
                "\n" +
                "          -- 打新融资利息\n" +
                "          SELECT\n" +
                "              account_channel,\n" +
                "              aaid,\n" +
                "              counter_id,\n" +
                "              market,\n" +
                "              currency,\n" +
                "              0 AS executed_qty,\n" +
                "              financing_interest_amount_accum_td AS executed_amount,\n" +
                "              0 AS executed_price,\n" +
                "              'BUY' AS direction,\n" +
                "              DATE_FORMAT(AT_TIMEZONE(issue_result_publ_date, 'Asia/Shanghai'), '%Y%m%d%H%i%S%f') AS executed_time,\n" +
                "              'lb_ipo_financing_interest' AS flow_type,\n" +
                "              CONCAT('ipo order_id:', CAST(id AS VARCHAR)) AS flow_desc,\n" +
                "              'LONG' AS position_side,\n" +
                "              1 AS multiplier\n" +
                "          FROM hive.lb_bi_trade.dwb_ipo_finance_d\n" +
                "          WHERE pt = '{flow_date}'\n" +
                "          ) all_flows\n" +
                "      ) t1\n" +
                "      \n" +
                "      LEFT JOIN\n" +
                "      (SELECT \n" +
                "          underlying_counter_id, \n" +
                "          counter_id\n" +
                "      FROM hive.lb_bi_product.dim_options_metadata\n" +
                "      ) t2\n" +
                "      ON t1.counter_id = t2.counter_id\n" +
                "      AND split(t1.counter_id, '/')[1] = 'OP'\n" +
                "\n" +
                "      LEFT JOIN\n" +
                "      (SELECT \n" +
                "          underlying_counter_id, \n" +
                "          counter_id \n" +
                "      FROM hive.lb_gemini.warrant_metadata_snapshot_6am\n" +
                "      WHERE pt = IF('{flow_date}' <= '20220119', '20220119', '{flow_date}')\n" +
                "      ) t3\n" +
                "      ON t1.counter_id = t3.counter_id \n" +
                "      AND split(t1.counter_id, '/')[1] = 'WT'\n"


        val statementData = PrestoSQLHelper.getStatementData(sql)
        println(JSONUtil.toJsonStr(statementData.statement))
        val putOnce = JSONUtil.createObj().putOnce("type", "presto").putOnce("script", sql)
        println(putOnce)
    }


}
