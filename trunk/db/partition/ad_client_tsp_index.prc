create or replace procedure ad_client_tsp_index(v_table_name in varchar2,
                                                v_column_name in varchar2,
                                                flag in varchar2,
                                                v_index_name in varchar2,
                                                p_user_name in varchar2) as

    v_crt_index_sql varchar2(4000);

begin
    v_crt_index_sql := '';
    if flag not in ('C', 'U') then
        raise_application_error(-20001, '请重新定义索引的类型!');
    end if;
    if v_table_name is null then
        raise_application_error(-20001, '请重新定义表名!');
    end if;
    if v_column_name is null then
        raise_application_error(-20001, '请重新定义索引的列名!');
    end if;
    if v_index_name is null then
        raise_application_error(-20001, '请重新定义索引的名称!');
    end if;

    for v in (select distinct t.partition_name
              from all_tab_partitions t
              where t.table_owner = upper(p_user_name) and
                    t.table_name = upper(v_table_name)) loop
    
        null;
    end loop;

    null;
end ad_client_tsp_index;
/

