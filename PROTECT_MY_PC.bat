@echo off
title PC Addiction Protection Setup
echo ============================================================
echo  Requesting Administrator access to apply PC Adult Shield...
echo ============================================================
echo.
powershell -Command "Start-Process powershell -Verb RunAs -ArgumentList '-NoProfile -ExecutionPolicy Bypass -File \"%~dp0setup_pc_protection.ps1\"'"
