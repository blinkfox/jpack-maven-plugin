@echo off
%1 mshta vbscript:CreateObject("Shell.Application").ShellExecute("cmd.exe","/c %~s0 ::","","runas",1)(window.close)&&exit
%~dp0${projectName}.exe ${batName}
echo The {projectName} service current state:
%~dp0{projectName}.exe status
pause