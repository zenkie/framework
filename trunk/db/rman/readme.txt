1.用DBCA建立备份数据库"RMAN"。修改目标数据库为“归档模式”也可直接运行 “备份.BAT”
 
2.  建议正式启用RMAN备份计划之前先清一下RMAN备份记录，否则RMAN备份时可能会报找不到已手工删除的归档日志：
          rman>crosscheck archivelog all;
          rman>delete expired archivelog all;
          rman>crosscheck backup;
          rman>delete expired backup;
          可直接运行  
                     清除文本记录.bat  需修改实际连接
   
3.在控制面板--任务计划中添加相应的任务计划，如：
	每月最后一个周日晚12:00数据库全备        （利用Rman_Backup_Full.bat）
	每周日晚8:30做数据库0级备份              （利用Rman_Backup_Level0.bat）
	每周一、二、四、五、六晚8:30做2级差异备份（利用Rman_Backup_Level2.bat）
        每周三晚8:30做数据库1级差异备份          （利用Rman_Backup_Level1.bat）

4.利用文本编辑工具修改备份.bat,还原.bat和部分rcv中的实际数据库连接
（sys/oracle@orcl部分,如果使用catalog目录，可加上catalog参数）
         PWD@标示
re_back.rcv
rman_backup_1.rcv
Rman_Backup_Full.rcv
Rman_Backup_Full0.rcv
Rman_Backup_Full1.rcv
Rman_Backup_Full2.rcv
还原.bat
备份.bat
5.存储目录为
E:\oracle\oradata\cc\   虚拟表目录
E:\bk                    备份存储目录