32 位的是 JavaService 2.0.10
64 位的是 JavaService 2.0.7, 安装在x64平台上时将 JavaService_x64.exe 更名为 JavaService.exe


You must ensure following files in specified path

JDK 1.5    --> d:\jdk15
Portal422      --> e:\portal422


运行光盘目录service下install_service_portal.bat文件，运行成功后进入windows的管理工具下的服务，将Portal启动。（是否成功启动，可以查看E:\portal422\bin下的out.log文件，有信息写入文件，表示成功配置Portal为系统服务）
可以通过uninstall_service_portal.bat 可以解除portal作为服务。
