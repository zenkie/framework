md E:\oracle\oradata\cc\
md e:\bk
sqlplus "sys/oracle@210 as SYSDBA" @al_log.sql

sqlplus "sys/rman888@rman as SYSDBA" @begin.sql
Rman_Backup_Full.bat
 
 

