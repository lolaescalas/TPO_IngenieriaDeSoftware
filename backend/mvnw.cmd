@echo off
rem Maven Wrapper simplificado: descarga Maven (una sola vez) en %USERPROFILE%\.m2\wrapper
rem y lo ejecuta con los argumentos recibidos. Uso: mvnw.cmd test
setlocal
set "MAVEN_VERSION=3.9.9"
set "WRAPPER_HOME=%USERPROFILE%\.m2\wrapper\dists"
set "MAVEN_HOME=%WRAPPER_HOME%\apache-maven-%MAVEN_VERSION%"
set "MAVEN_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MAVEN_VERSION%/apache-maven-%MAVEN_VERSION%-bin.zip"

if not exist "%MAVEN_HOME%\bin\mvn.cmd" (
  echo Descargando Maven %MAVEN_VERSION%...
  if not exist "%WRAPPER_HOME%" mkdir "%WRAPPER_HOME%"
  powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "$ErrorActionPreference='Stop'; $zip=Join-Path $env:TEMP 'maven-%MAVEN_VERSION%.zip'; Invoke-WebRequest -UseBasicParsing -Uri '%MAVEN_URL%' -OutFile $zip; Expand-Archive -Force -Path $zip -DestinationPath '%WRAPPER_HOME%'; Remove-Item $zip"
  if errorlevel 1 (
    echo Error descargando Maven.
    exit /b 1
  )
)

call "%MAVEN_HOME%\bin\mvn.cmd" -f "%~dp0pom.xml" %*
exit /b %ERRORLEVEL%
