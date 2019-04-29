@echo off
%1 mshta vbscript:CreateObject("Shell.Application").ShellExecute("cmd.exe","/c %~s0 ::","","runas",1)(window.close)&&exit
%~dp0${name}.exe ${batName}
echo The ${name} service current state:
%~dp0${name}.exe status
pause