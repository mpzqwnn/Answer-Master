@echo off
set GRADLE_OPTS=-Xms64m -Xmx128m -XX:MaxMetaspaceSize=64m
call gradlew assembleDebug --no-daemon