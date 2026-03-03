@REM ----------------------------------------------------------------------------
@REM Maven Wrapper startup batch script - Windows
@REM ----------------------------------------------------------------------------
@echo off
SET "BASEDIR=%~dp0"
SET "BASEDIR=%BASEDIR:~0,-1%"
SET "WRAPPER_JAR=%BASEDIR%\.mvn\wrapper\maven-wrapper.jar"

SET "JAVA_EXEC=java"
IF DEFINED JAVA_HOME SET "JAVA_EXEC=%JAVA_HOME%\bin\java"

@REM Try Android Studio JBR as fallback
IF NOT DEFINED JAVA_HOME (
  IF EXIST "C:\Program Files\Android\Android Studio\jbr\bin\java.exe" (
    SET "JAVA_EXEC=C:\Program Files\Android\Android Studio\jbr\bin\java.exe"
  )
)

"%JAVA_EXEC%" "-Dmaven.multiModuleProjectDirectory=%BASEDIR%" -cp "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*
