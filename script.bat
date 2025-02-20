@REM SET JAVAPATH = "C:\Program Files\Java\jdk1.8.0_202\bin\"
@REM %JAVAPATH%javac -d E:\Mr_Naina\sprint8\framework\3 "C:\Users\itu1\Desktop\Framework_2758\mg\itu\prom\*.java"
@REM jar cvf "E:\Mr_Naina\sprint8\framework\3\mg.jar" -C "E:\Mr_Naina\sprint8\framework\3" mg


@ECHO OFF

REM Define variables
    SET APP_DIR=%~dp0
    SET SRC_DIR=%APP_DIR%
    SET BIN_DIR=%APP_DIR%bin
    SET LIB_DIR=%APP_DIR%lib
    SET TEMP_JAVA_DIR=%APP_DIR%tempjava
    SET TEST_NAME_DIR=TestFramework

REM Copier les *.java dans un dossier temporaire tempjava
    MKDIR "%APP_DIR%\tempjava"
    for /R "%SRC_DIR%" %%G IN (*.java) DO (
        XCOPY /Y "%%G" "%APP_DIR%\tempjava"
    )

REM Compile Java classes
    javac -cp "%LIB_DIR%\*" -d "%BIN_DIR%" "%TEMP_JAVA_DIR%\*.java"

REM Supprimer le dossier temporaire apres compilation
    RD /S /Q "%TEMP_JAVA_DIR%"

REM Archiver en .jar
    jar cf mg.jar -C "%BIN_DIR%" .

REM Copy the .jar file to the lib directory of Project tomcat
move /Y mg.jar "..\%TEST_NAME_DIR%\lib\"
