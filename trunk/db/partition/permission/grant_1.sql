GRANT CONNECT TO nds3;
GRANT RESOURCE TO nds3;
ALTER USER nds3 DEFAULT ROLE ALL;

GRANT CREATE ANY TABLE TO nds3;
GRANT CREATE ANY SEQUENCE TO nds3;
GRANT CREATE ANY View TO nds3;
GRANT UNLIMITED TABLESPACE TO nds3;
GRANT CREATE DATABASE LINK TO nds3;

grant select any dictionary to nds3;
grant select any table to nds3;
grant select_catalog_role to nds3;
grant create tablespace to nds3;


exit
