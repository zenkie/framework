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

