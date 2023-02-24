package com.github.bigdata.sql.parser.pg

import org.junit.Test

class Test {

    @Test
    fun setConfigTest1() {
        val sql =
            "select id,code,pcode,counter_id,pcounter_id,product,name,name_locales::varchar as name_locales,company_name,company_name_locales::varchar as company_name_locales,profile,profile_locales::varchar as profile_locales,market,sip_status," +
                    "level,factset_product,factset_industry_code,isin,fds_md5,created_at,updated_at,fds_tr,ice_tr,ice_product,visible,exchange,locale_status," +
                    "plevel,currency,status,etf_type,jy_inner_code,jy_company_code,industry_id,intro::varchar as intro,rbics_l4_industry_id," +
                    "rbics_l4_secondary_industry_id,rbics_l3_industry_id,transaction_currency,security_code,factset_entity_id," +
                    "short_sell,intro->>'zh-CN' as intro_zh_cn from asdsadsa.securities"

    }
}