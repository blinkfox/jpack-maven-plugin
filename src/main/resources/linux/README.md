一款将 SpringBoot 项目做成Windows Service 的 Maven 插件


注意：

1. 5个 bat 文件，请右键以管理员的身份运行
2. 各文件的文件名无特殊情况，不需要修改
3. 一旦安装成了 Windows 服务，目录下的文件就不要移动了
4. 命令运行时，可能会提示安装.NET,安装完成就可运行命令了，不过现在大部分的 Windows 服务器或者个人电脑都会默认安装了.NET,没有的话启用一下就好了
5. 运行各个命令是注意提示信息，例如卸载完服务都的状态为NonExistent，刚安装完服务后的状态为Stopped，服务成功启动的状态为Started...等等


更为详细使用说明见 ： https://github.com/JoyLau/joylau-springboot-daemon-windows