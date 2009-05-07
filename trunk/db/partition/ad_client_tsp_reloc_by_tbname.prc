create or replace procedure Ad_Client_Tsp_reloc_by_TbName(p_table_name in varchar2,
                                                          p_username in varchar2) as
    /*2008.04.07 zjf add
      给一张特定的表增加全部分区
      如果分区已经存在，则不需增加，否则增加其他分区
      p_username 对应的登录oracle系统的用户名
    */
    v_table_name         varchar2(100); --需要操作的表名
    v_part_table         varchar2(20); --分区的名称
    v_addPart_table_name varchar2(20); --tablespace所对应的物理文件名称
    v_cnt                integer;
    --定义数组
    TYPE ObjectIDList IS TABLE OF NUMBER INDEX BY BINARY_INTEGER;
    v_invoices ObjectIDList;

    query varchar2(400);

begin
    --对输入参数的有效性的检查
    if p_username is null then
        raise_application_error(-20001, '未填写系统用户的名称!');
    end if;
    if p_table_name is null then
        raise_application_error(-20001, '未填写需要增加分区的表名称');
    end if;

    --获取需要增加分区的表的名称
    v_table_name := upper(p_table_name);
    query        := 'select distinct t.id from ' || p_username ||
                    '.ad_client t';
    execute immediate query BULK COLLECT
        INTO v_invoices;
    FOR i IN v_invoices.FIRST .. v_invoices.LAST LOOP
        --获取分区表中的分区
        v_addPart_table_name := upper('g_2000q') || to_char(v_invoices(i));
        --创建分区表的分区
        v_part_table := 'P' || to_char(v_invoices(i));

        select count(*)
        into v_cnt
        from all_tab_partitions t
        where t.table_name = v_table_name and t.partition_name = v_part_table;

        if v_cnt = 0 then
            --新增一个分区
            execute immediate 'alter table ' || p_username || '.' ||
                              v_table_name || ' add partition ' || v_part_table ||
                              ' values (' || v_invoices(i) || ')
              tablespace ' || v_addPart_table_name;
        end if;
    end loop;

    /*
        for v in (select distinct t.id from nds4.ad_client t) loop
            --获取分区表中的分区
            v_addPart_table_name := upper('g_2000q') || to_char(v.id);
            --创建分区表的分区
            v_part_table := 'P' || to_char(v.id);

            select count(*)
            into v_cnt
            from all_tab_partitions t
            where t.table_name = v_table_name and t.partition_name = v_part_table;

            if v_cnt = 0 then
                --新增一个分区
                execute immediate 'alter table nds4.' || v_table_name ||
                                  ' add partition ' || v_part_table || ' values (' || v.id || ')
                  tablespace ' || v_addPart_table_name;
            end if;

        end loop;
    */

end Ad_Client_Tsp_reloc_by_TbName;
/

