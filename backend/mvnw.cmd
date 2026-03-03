@REM ----------------------------------------------------------------------------
@REM Maven Wrapper startup batch script - Windows
@REM ----------------------------------------------------------------------------
@IF "%__MVNW_ARG0_NAME__%"=="" (SET "MVN_CMD=mvn.cmd") ELSE (SET "MVN_CMD=%__MVNW_ARG0_NAME__%")
@SET WRAPPER_JAR="%~dp0.mvn\wrapper\maven-wrapper.jar"
@SET WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

@SET WRAPPER_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"

@IF EXIST %~dp0mvnw.cmd (
  @SET "WRAPPER_DIR=%~dp0"
) ELSE (
  @SET "WRAPPER_DIR=%~dp0"
)

@SET JAVA_EXEC=java
@IF DEFINED JAVA_HOME @SET JAVA_EXEC="%JAVA_HOME%\bin\java"

@REM Try Android Studio JBR as fallback
@IF NOT EXIST "%JAVA_HOME%\bin\java.exe" (
  @IF EXIST "C:\Program Files\Android\Android Studio\jbr\bin\java.exe" (
    @SET JAVA_EXEC="C:\Program Files\Android\Android Studio\jbr\bin\java.exe"
  )
)

@IF NOT EXIST %WRAPPER_JAR% (
  @echo Downloading Maven Wrapper jar...
  @%JAVA_EXEC% -class path "%SYSTEMROOT%\system32" org.apache.maven.wrapper.Download %WRAPPER_URL% %WRAPPER_JAR% 2>NUL
  @IF ERRORLEVEL 1 (
    @powershell -Command "Invoke-WebRequest -Uri '%WRAPPER_URL%' -OutFile %WRAPPER_JAR%"
  )
)

@%JAVA_EXEC% -jar %WRAPPER_JAR% %WRAPPER_LAUNCHER% %*
