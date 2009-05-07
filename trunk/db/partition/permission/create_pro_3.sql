


-----------------------------------------------
-- Export file for user SYS                  --
-- Created by user on 2008-4-23, 下午 05:08:59 --
-----------------------------------------------

spool start1111.log

prompt
prompt Creating procedure AD_CLIENT_TSP_CREATE
prompt =======================================
prompt
create or replace procedure Ad_Client_Tsp_Create(p_ad_client_id in number,
                                                 p_size in varchar2,
                                                 p_dirpath in varchar2,
                                                 p_username in varchar2) as
    /*2008.03.28 zjf edit
      p_dirpath 对应的tablespace的保存路径
      p_ad_client_id 是公司对应的ad_client_id的值
      p_size 对应的是定义Tablespace size的大小
      p_username 对应的是oracle的用户名 如：nds4,bidnds4
    */
    v_tablespace_path    varchar2(200); --tablespace的物理路径
    v_ad_client_id       number(10); --公司对应的id的值
    v_addPart_table_name varchar2(100); --tablespace的物理文件的名称
    v_part_table         varchar2(100); --根据ad_client_id新增一个分区
    v_size               varchar2(20); --tablespace的大小
    v_table_name         varchar2(100); --需要增加分区的表名
begin
    --对输入参数的有效性的检查
    if p_ad_client_id is null then
        raise_application_error(-20001, '未定义公司所对应的ad_client_id!');
    end if;
    if p_username is null then
        raise_application_error(-20001, '未填写系统用户的名称!');
    end if;
    if p_size is null then
        raise_application_error(-20001, '未定义数据库文件的大小!');
    end if;
    if p_dirpath is null then
        raise_application_error(-20001, '未定义数据文件保存的文件夹位置!');
    end if;

    v_ad_client_id := p_ad_client_id;
    --获取分区表中的分区
    v_addPart_table_name := 'g_2000q' || to_char(v_ad_client_id);
    --定义 Tablespace的物理路径
    v_tablespace_path := p_dirpath || '/g_2000q' || to_char(v_ad_client_id) ||
                         '.dbf';
    /*定义Tablespace的大小
    如果为空，则设置成默认100M的大小
    如果不为空，则设置成管理人员设置的大小*/
    if p_size is null then
        v_size := '100M';
    else
        v_size := p_size;
    end if;

    --创建 Tablespace
    /*  
     dbms_output.put_line('create tablespace ' || v_addPart_table_name ||
                          ' datafile ' || '''' || v_tablespace_path || '''' ||
                          ' size ' || v_size);
    */
    execute immediate 'create tablespace ' || v_addPart_table_name ||
                      ' datafile ' || '''' || v_tablespace_path || '''' ||
                      ' size ' || v_size;
    --||' default storage (initial 100k next 100k minextents 1 maxextents unlimited pctincrease 1)';
    --创建分区表的分区
    v_part_table := 'P' || to_char(v_ad_client_id);

    /*需要考虑做一个循环 循环的变量的值就是表名(v_table_name),
    向系统中所有含有ad_client_id的分区表均新增一个分区*/

    for v in (select distinct table_name from table_store) loop
        --新增一个分区
        v_table_name := upper(v.table_name);
        execute immediate 'alter table ' || p_username || '.' || v_table_name ||
                          ' add partition ' || v_part_table || ' values (' ||
                          v_ad_client_id || ')
          tablespace ' || v_addPart_table_name;
    end loop;
    --经过测试,动态执行语句不能绑定变量

    /*exception
    when others then
        v_code    := sqlcode;
        v_message := sqlerrm;
        rollback;*/
end Ad_Client_Tsp_Create;
/

prompt
prompt Creating procedure AD_CLIENT_TSP_CREATE_BY_TBNAME
prompt =================================================
prompt
create or replace procedure Ad_Client_Tsp_Create_by_TbName(p_table_name in varchar2) as
    /*2008.04.07 zjf add
      给一张特定的表增加一个分区
      p_table_name 对应的是需要操作的表
      p_ad_client_id 对应的是表中的ad_client_id的值
    */
    v_code               number(10);
    v_message            varchar2(200);
    v_table_name         varchar2(100);
    v_part_table         varchar2(20);
    v_addPart_table_name varchar2(20);
    v_cnt                integer;

begin
    --获取需要增加分区的表的名称
    v_table_name := upper(p_table_name); --'temp_ad_column';

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

    v_message := '操作成功';
    /*exception
    when others then
        v_code    := sqlcode;
        v_message := sqlerrm;
        rollback;*/
end Ad_Client_Tsp_Create_by_TbName;
/

prompt
prompt Creating procedure AD_CLIENT_TSP_RELOC_BY_TBNAME
prompt ================================================
prompt
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
        for v in (select distinct t.id from ad_client t) loop
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

prompt
prompt Creating procedure AD_CLIENT_TSP_RESIZE
prompt =======================================
prompt
create or replace procedure ad_client_tsp_resize(p_ad_client_id number,
                                                 p_re_size in varchar2,
                                                 p_flag in number,
                                                 p_dirpath in varchar2) as
    /*
       2008.03.28 zjf add
       重定义数据文件的大小
       p_tablespace tablespace的名称
       p_re_size tablespace重定义的大小
       p_dirpath 新增加一个dbf文件时需指定存放该dbf文件的路径
       p_flag 操作类型
          1:dbf文件进行改变容量操作
          2:在该tablespace下新增一个dbf文件(default:100M)
    */
    v_tablespace_path varchar2(100); --tablespace的物理位置
    v_partit          varchar2(20); --tablespace的文件名
    v_tbs_file_name   varchar2(200);

    v_ext_datafile varchar2(200); --新增的dbf的文件路径

    v_cnt integer;
begin
    --对输入参数的有效性的检查
    if p_flag is null then
        raise_application_error(-20001, '未定义对tablespace的操作类型!');
    end if;
    if p_ad_client_id is null then
        raise_application_error(-20001, '未定义公司所对应的ad_client_id!');
    end if;

    if p_flag = 2 then
        if p_dirpath is null then
            raise_application_error(-20001, '未定义存放新增加dbf文件的路径');
        end if;
        if p_re_size is not null then
            raise_application_error(-20001, '不需要定义数据文件的大小!');
        end if;
    elsif p_flag = 1 then
        if p_dirpath is not null then
            raise_application_error(-20001,
                                    '不需要定义存放新增加dbf文件的路径,该操作是改变容量的大小!');
        end if;
        if p_re_size is null then
            raise_application_error(-20001, '未定义数据文件的大小!');
        end if;
    else
        raise_application_error(-20001, '定义对tablespace的操作类型不正确!');
    end if;

    --分区表中分区的名称
    --获取分区表的名称有问题,需修改
    v_partit := 'g_2000q' || to_char(p_ad_client_id);
    --获取Tablespace的物理位置
    /*v_tablespace_path := '''' || 'D:/oracle/product/10.2.0/oradata/orcl/' ||
    v_partit || '.dbf' || '''';*/

    /*显示扩展数据库空间操作语句
    dbms_output.put_line(' ALTER DATABASE
                         DATAFILE ' ||
                         v_tablespace_path || ' RESIZE ' || p_re_size);
    */
    case p_flag
        when 1 then
            select count(*)
            into v_cnt
            from dba_data_files t
            where t.tablespace_name = upper(v_partit);
            --获取最新的分区的名称        
            select g.file_name
            into v_tbs_file_name
            from (select rownum as num, t.*
                   --into v_tbs_file_name
                   from dba_data_files t
                   where t.tablespace_name = upper(v_partit)
                   order by t.file_id desc) g
            where g.num = v_cnt;
            /*
                      select t.file_name
                      into v_tbs_file_name
                      from dba_data_files t
                      where t.tablespace_name = upper(v_partit) and rownum = 1
                      order by t.relative_fno desc;
            */
            --Tablespace的物理位置
            v_tablespace_path := '''' || v_tbs_file_name || '''';
            --不能绑定动态变量
            execute immediate ' ALTER DATABASE
                         DATAFILE ' ||
                              v_tablespace_path || ' RESIZE ' || p_re_size;
        when 2 then
            v_ext_datafile := '''' || p_dirpath || '/ext' ||
                              to_char(sysdate, 'yymmddhhmmss') || v_partit || '''';
            --添加一个datafile到tablespace下
            execute immediate ' ALTER TABLESPACE ' || v_partit ||
                              ' ADD datafile' || v_ext_datafile ||
                              ' SIZE 100M ';
            --alter tablespace G_2000Q6 add datafile 'D:\oracle\product\10.2.0\oradata\orcl\g_20001112.dbf' SIZE 50M;
        else
            raise_application_error(-20001,
                                    '对tablespace的操作类型不正确 ' || p_flag);
    end case;
/*exception
    when others then
        v_code    := sqlcode;
        v_message := sqlerrm;
        rollback;*/
end ad_client_tsp_resize;
/




create or replace procedure ad_client_tsp_calc_size(p_ad_client_id in number,
                                          p_size out number,
                                          p_str_size out varchar2) as
    /*
       @2008.04.30 zjf add
       统计每个ad_client_id（公司）所占用的空间
    */
    v_tablespace_name varchar2(100);
    v_mbytes_alloc    varchar2(50);
    v_mbytes_free     varchar2(50);
    v_partit          varchar2(20); --tablespace的文件名
    v_cnt             int;
    v_init_alloc      number(10);
    v_init_free       number(10);
begin
    --分区表中分区的名称
    --获取分区表的名称有问题,需修改
    v_partit := 'g_2000q' || to_char(p_ad_client_id);

    --判断该ad_client_id所对应的公司是否存在
    select count(*)
    into v_cnt
    from all_tab_partitions t
    where t.tablespace_name = upper(v_partit) and rownum = 1;

    if v_cnt = 0 then
        raise_application_error(-20001,
                                p_ad_client_id || '所对应的用户不存在,请检查!');
    end if;

    --统计该ad_client_id所对应的公司所占用的数据库空间
    select b.tablespace_name, mbytes_alloc, mbytes_free
    into v_tablespace_name, v_mbytes_alloc, v_mbytes_free
    from (select round(sum(bytes) / 1024 / 1024) mbytes_free, tablespace_name
           from dba_free_space
           group by tablespace_name) a,
         (select round(sum(bytes) / 1024 / 1024) mbytes_alloc, tablespace_name
           from dba_data_files
           group by tablespace_name) b
    where a.tablespace_name(+) = b.tablespace_name and
          lower(b.tablespace_name) = v_partit;
    --统计模板('P0')所占用的数据库空间
    select mbytes_alloc, mbytes_free
    into v_init_alloc, v_init_free
    from (select round(sum(bytes) / 1024 / 1024) mbytes_free, tablespace_name
           from dba_free_space
           group by tablespace_name) a,
         (select round(sum(bytes) / 1024 / 1024) mbytes_alloc, tablespace_name
           from dba_data_files
           group by tablespace_name) b
    where a.tablespace_name(+) = b.tablespace_name and
          lower(b.tablespace_name) = 'g_2000q0';
    --获取该公司的数据所占用的数据库空间大小(以'M'为单位)
    p_size := v_mbytes_alloc - (v_init_alloc - v_init_free) - v_mbytes_free;

    p_str_size := '该用户的数据已经占用的空间为' || p_size || 'M';

end ad_client_tsp_calc_size;
/

spool off