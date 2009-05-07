----------------------------------------------
-- Export file for user SYS                 --
-- Created by yfzhu on 2008-06-02, 19:13:31 --
----------------------------------------------

spool install.log

prompt
prompt Creating procedure AD_CLIENT_TSP_CREATE
prompt =======================================
prompt
@@ad_client_tsp_create.prc
prompt
prompt Creating procedure AD_CLIENT_TSP_CREATE_BY_TBNAME
prompt =================================================
prompt
@@ad_client_tsp_create_by_tbname.prc
prompt
prompt Creating procedure AD_CLIENT_TSP_INDEX
prompt ======================================
prompt
@@ad_client_tsp_index.prc
prompt
prompt Creating procedure AD_CLIENT_TSP_RELOC_BY_TBNAME
prompt ================================================
prompt
@@ad_client_tsp_reloc_by_tbname.prc
prompt
prompt Creating procedure AD_CLIENT_TSP_RESIZE
prompt =======================================
prompt
@@ad_client_tsp_resize.prc
prompt
prompt Creating procedure AD_CREATE_TSP_SQL
prompt ====================================
prompt
@@ad_create_tsp_sql.prc

spool off
