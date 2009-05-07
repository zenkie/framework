create or replace procedure AD_create_tsp_sql as
    /*
         2008.05.28 zjf add
         实现的功能是:生成创建Tablespace的语句

         需要注意的是:1个tablespace对应几个DBF文件
    */
    i integer;
begin
    update create_tsp_table t set t.content = null where id = 1;

    /* 生成一个tablespace对应1个DBF文件的创建tablespace语句
    */
    for v in (select *
              from dba_data_files g
              where g.tablespace_name in
                    (select t.tablespace_name
                     from dba_data_files t
                     where t.tablespace_name like 'G_%'
                     group by t.tablespace_name
                     having count(*) = 1)) loop
        if v.tablespace_name is not null then

            update create_tsp_table t
            set t.content = t.content || chr(13) || ' create tablespace ' ||
                             v.tablespace_name || ' datafile ' || '''' ||
                             v.file_name || '''' || ' size ' ||
                             to_char(trunc(v.bytes) / 1024 / 1024) || 'M ' ||
                             ' REUSE AUTOEXTEND OFF;'
            where id = 1;
        end if;
    end loop;

    /* 生成一个tablespace对应1个DBF文件的创建tablespace语句
    */
    for x in (select t.tablespace_name
              from dba_data_files t
              where t.tablespace_name like 'G_%'
              group by t.tablespace_name
              having count(*) >= 2) loop
        i := 1;

        for m in (select *
                  from dba_data_files g
                  where g.tablespace_name = x.tablespace_name) loop
            if i = 1 then
                update create_tsp_table t
                set t.content = t.content || chr(13) || ' create tablespace ' ||
                                 m.tablespace_name || ' datafile ' || '''' ||
                                 m.file_name || '''' || ' size ' ||
                                 to_char(trunc(m.bytes) / 1024 / 1024) || 'M ' ||
                                 ' REUSE AUTOEXTEND OFF;'
                where id = 1;
            else
                update create_tsp_table t
                set t.content = t.content || chr(13) || ' ALTER TABLESPACE ' ||
                                 m.tablespace_name || ' ADD datafile ' || '''' ||
                                 m.file_name || '''' || ' SIZE ' ||
                                 to_char(trunc(m.bytes) / 1024 / 1024) || 'M; ';
            end if;

            i := i + 1;
        end loop;

    end loop;

end AD_create_tsp_sql;
/

