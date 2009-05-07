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

