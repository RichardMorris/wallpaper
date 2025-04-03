@echo off
set image=chub.jpg
if NOT [%1]==[] set image=%1
java -jar wallpaper.jar %image% %2 %3
