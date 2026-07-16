@echo off
set "MAVEN_OPTS=-Xmx512m"
set "MVNW_VERBOSE=false"
set "MVNW_REPOURL=https://repo1.maven.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.2/"

set "BASE_DIR=%~dp0"
if exist "%BASE_DIR%.mvn\wrapper\maven-wrapper.jar" (
    java %MAVEN_OPTS% -jar "%BASE_DIR%.mvn\wrapper\maven-wrapper.jar" %*
) else (
    java %MAVEN_OPTS% -cp "%BASE_DIR%.mvn\wrapper\*" org.apache.maven.wrapper.MavenWrapperMain %*
)
