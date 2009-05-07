create table TABLE_STORE
(
                    TABLE_NAME  VARCHAR2(40),
                    CREATE_DATE DATE default sysdate
);

insert into TABLE_STORE 
 (select distinct t.table_name, sysdate from all_tab_partitions t
 where t.table_owner = upper('NDS3') and t.table_name not like 'BIN$%' );

exit


