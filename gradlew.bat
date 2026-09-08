@echo off
where gradle >nul 2>nul
if %ERRORLEVEL% EQU 0 (
  gradle %*
  exit /b %ERRORLEVEL%
)
echo Gradle is not installed. Open the project in Android Studio with JDK 17 or install Gradle 8.9.
exit /b 1
