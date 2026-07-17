@echo off
set "MAVEN_OPTS=-Xmx512m -Dmaven.multiModuleProjectDirectory=%~dp0"
set "MVNW_VERBOSE=false"

set "BASE_DIR=%~dp0"
java %MAVEN_OPTS% -cp "%BASE_DIR%.mvn\wrapper\maven-wrapper.jar" org.apache.maven.wrapper.MavenWrapperMain %*
