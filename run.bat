@echo off
title LoadFlow - Minecraft Load Tester
echo Iniciando LoadFlow con JVM optimizada...
start javaw -XX:+UseG1GC -XX:+UseStringDeduplication -Dio.netty.allocator.type=pooled -Dio.netty.leakDetection.level=DISABLED -jar "%~dp0build\libs\LoadFlow-1.0.0-all.jar"
