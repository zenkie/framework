@echo off
REM: update following parameters for new schema of esnds3 and eslportal account:t
REM:   replace all "eslportal" to new account name, password will be "lportal"
REM:   replace all "nds3" to new account name, password will be "abc123"
REM:   Note that in ln.sql, there's a sid named "orcl", please change if the server has differenct sid
@echo on

sqlplus "sys/oracle as SYSDBA" @grant_1.sql
sqlplus "nds3/abc123"  @create_table_2.sql 
sqlplus "nds3/abc123"  @create_pro_3.sql


