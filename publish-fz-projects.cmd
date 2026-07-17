@if (@CodeSection)==(@Batch) @then
@echo off
setlocal EnableExtensions EnableDelayedExpansion

set "MAVEN_HOME=D:\maven\apache-maven-3.9.14"
set "MAVEN_SETTINGS=D:\maven\settings.xml"
set "MAVEN_REPO=D:\maven\repository"

set "PROJECT_DEPENDENCIES=D:\workspace\fz-spring-boot-starter-dependencies"
set "PROJECT_STARTER=D:\workspace\fz-spring-boot-starter"

set "MVN=%MAVEN_HOME%\bin\mvn.cmd"

set "VERSION="
set "DRY_RUN=0"
set "SKIP_TESTS=1"
set "SKIP_GIT_PUSH=0"
set "SKIP_DEPLOY=0"
set "NO_PAUSE=0"
set "CURRENT_PROJECT="
set "CURRENT_STAGE="
set "BASE_STARTER_VERSION="
set "ROLLBACK_IN_PROGRESS=0"
set "SELF=%~f0"

:parse_args
if "%~1"=="" goto after_parse_args
if /i "%~1"=="/dryRun" set "DRY_RUN=1" & shift & goto parse_args
if /i "%~1"=="-dryRun" set "DRY_RUN=1" & shift & goto parse_args
if /i "%~1"=="/skipTests" set "SKIP_TESTS=1" & shift & goto parse_args
if /i "%~1"=="-skipTests" set "SKIP_TESTS=1" & shift & goto parse_args
if /i "%~1"=="/skipGitPush" set "SKIP_GIT_PUSH=1" & shift & goto parse_args
if /i "%~1"=="-skipGitPush" set "SKIP_GIT_PUSH=1" & shift & goto parse_args
if /i "%~1"=="/skipDeploy" set "SKIP_DEPLOY=1" & shift & goto parse_args
if /i "%~1"=="-skipDeploy" set "SKIP_DEPLOY=1" & shift & goto parse_args
if /i "%~1"=="/noPause" set "NO_PAUSE=1" & shift & goto parse_args
if /i "%~1"=="-noPause" set "NO_PAUSE=1" & shift & goto parse_args
if not defined VERSION (
    set "VERSION=%~1"
    shift
    goto parse_args
)
echo Unknown argument: %~1
goto fail

:after_parse_args
call :assert_path "%MVN%" "mvn.cmd" || goto fail
call :assert_path "%MAVEN_SETTINGS%" "Maven settings.xml" || goto fail
call :assert_path "%MAVEN_REPO%" "Maven local repository" || goto fail
call :assert_path "%PROJECT_DEPENDENCIES%" "Project directory" || goto fail
call :assert_path "%PROJECT_STARTER%" "Project directory" || goto fail

call :repo_has_changes "%PROJECT_STARTER%" STARTER_HAS_CHANGES || goto fail
if "%STARTER_HAS_CHANGES%"=="0" goto nothing_to_release

set "OLD_MAVEN_OPTS=%MAVEN_OPTS%"
set "MAVEN_OPTS=-Xms512m -Xmx2048m -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -Duser.language=zh -Duser.country=CN --add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.io=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED --add-opens=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED"
echo MAVEN_OPTS=%MAVEN_OPTS%

call :get_fz_version "%PROJECT_DEPENDENCIES%" DEP_VERSION || goto fail
call :get_fz_version "%PROJECT_STARTER%" STARTER_VERSION || goto fail
set "BASE_STARTER_VERSION=!STARTER_VERSION!"

if defined VERSION (
    set "RELEASE_VERSION=%VERSION%"
) else (
    if not "!DEP_VERSION!"=="!STARTER_VERSION!" (
        echo Project versions are inconsistent: !DEP_VERSION!, !STARTER_VERSION!
        echo Please run this cmd with an explicit version, for example:
        echo %~nx0 1.3.24
        goto fail
    )
    call :next_version "!DEP_VERSION!" RELEASE_VERSION || goto fail
    echo Auto release version: !DEP_VERSION! -^> !RELEASE_VERSION!
)

echo.
echo ========== release info ==========
echo dependencies version: !DEP_VERSION!
echo starter version: !STARTER_VERSION!
echo release version: !RELEASE_VERSION!

call :publish_project "fz-spring-boot-starter-dependencies" "%PROJECT_DEPENDENCIES%" "gitee" || goto fail
call :publish_project "fz-spring-boot-starter" "%PROJECT_STARTER%" "gitee github" || goto fail

echo.
echo ========== done ==========
echo Release version: %RELEASE_VERSION%
goto done

:assert_path
if not exist "%~1" (
    echo %~2 does not exist: %~1
    exit /b 1
)
exit /b 0

:get_fz_version
for /f "usebackq delims=" %%V in (`cscript //nologo //E:JScript "!SELF!" getVersion "%~1"`) do set "%~2=%%V"
if not defined %~2 exit /b 1
exit /b 0

:next_version
set "CURRENT_VERSION=%~1"
for /f "tokens=1-3 delims=." %%A in ("%CURRENT_VERSION%") do (
    set "MAJOR=%%A"
    set "MINOR=%%B"
    set "PATCH=%%C"
)
if not defined MAJOR exit /b 1
if not defined MINOR exit /b 1
if not defined PATCH exit /b 1
set /a PATCH=PATCH + 1
if %PATCH% GTR 99 (
    set /a PATCH=0
    set /a MINOR=MINOR + 1
)
if %MINOR% GTR 99 (
    set /a MINOR=0
    set /a MAJOR=MAJOR + 1
)
set "%~2=%MAJOR%.%MINOR%.%PATCH%"
exit /b 0

:publish_project
set "PROJECT_NAME=%~1"
set "PROJECT_PATH=%~2"
set "PROJECT_REMOTES=%~3"
set "CURRENT_PROJECT=%PROJECT_NAME%"

echo.
echo ========== %PROJECT_NAME% prepare ==========
set "CURRENT_STAGE=prepare"
call :set_fz_version "%PROJECT_PATH%" "%RELEASE_VERSION%" || exit /b 1

echo.
echo ========== %PROJECT_NAME% clean install ==========
set "CURRENT_STAGE=clean install"
call :run_maven "%PROJECT_PATH%" clean install || exit /b 1

echo.
echo ========== %PROJECT_NAME% git push ==========
set "CURRENT_STAGE=git push"
call :commit_and_push "%PROJECT_NAME%" "%PROJECT_PATH%" "%PROJECT_REMOTES%" || exit /b 1

if "%SKIP_DEPLOY%"=="1" (
    echo Skipped deploy: %PROJECT_NAME%
) else (
    echo.
    echo ========== %PROJECT_NAME% clean deploy ==========
    set "CURRENT_STAGE=clean deploy"
    call :run_maven "%PROJECT_PATH%" clean deploy || exit /b 1
)
exit /b 0

:set_fz_version
if "%DRY_RUN%"=="1" (
    cscript //nologo //E:JScript "!SELF!" setVersion "%~1" "%~2" dryRun
) else (
    cscript //nologo //E:JScript "!SELF!" setVersion "%~1" "%~2"
)
exit /b %ERRORLEVEL%

:repo_has_changes
set "CHECK_STATUS_FILE=%TEMP%\fz_publish_precheck_%RANDOM%%RANDOM%.txt"
git -C "%~1" status --porcelain > "%CHECK_STATUS_FILE%"
if not "%ERRORLEVEL%"=="0" (
    del "%CHECK_STATUS_FILE%" >nul 2>nul
    echo Failed to check git status: %~1
    exit /b 1
)
for %%S in ("%CHECK_STATUS_FILE%") do set "CHECK_STATUS_SIZE=%%~zS"
del "%CHECK_STATUS_FILE%" >nul 2>nul
if "%CHECK_STATUS_SIZE%"=="0" (
    set "%~2=0"
) else (
    set "%~2=1"
)
exit /b 0

:run_maven
set "WORK_DIR=%~1"
set "TEST_ARG="
if "%SKIP_TESTS%"=="1" set "TEST_ARG=-DskipTests"
call :run_in_dir "%WORK_DIR%" "%MVN%" -B -U -ntp -s "%MAVEN_SETTINGS%" "-Dmaven.repo.local=%MAVEN_REPO%" %TEST_ARG% "%~2" "%~3"
exit /b %ERRORLEVEL%

:commit_and_push
set "COMMIT_PROJECT_NAME=%~1"
set "COMMIT_PROJECT_PATH=%~2"
set "COMMIT_REMOTES=%~3"
set "COMMIT_MESSAGE=%~4"
if not defined COMMIT_MESSAGE set "COMMIT_MESSAGE=chore: release %COMMIT_PROJECT_NAME% %RELEASE_VERSION%"

if "%SKIP_GIT_PUSH%"=="1" (
    echo Skipped git push: %COMMIT_PROJECT_NAME%
    exit /b 0
)

set "BRANCH="
for /f "usebackq delims=" %%B in (`git -C "%COMMIT_PROJECT_PATH%" branch --show-current`) do set "BRANCH=%%B"
if not defined BRANCH (
    echo Cannot resolve current branch: %COMMIT_PROJECT_PATH%
    exit /b 1
)

set "STATUS_FILE=%TEMP%\fz_publish_status_%RANDOM%%RANDOM%.txt"
git -C "%COMMIT_PROJECT_PATH%" status --porcelain > "%STATUS_FILE%"
for %%S in ("%STATUS_FILE%") do set "STATUS_SIZE=%%~zS"

if not "%STATUS_SIZE%"=="0" (
    echo Changes to commit for %COMMIT_PROJECT_NAME%:
    type "%STATUS_FILE%"
    del "%STATUS_FILE%" >nul 2>nul
    call :run_in_dir "%COMMIT_PROJECT_PATH%" git -C "%COMMIT_PROJECT_PATH%" add -A || exit /b 1
    call :run_in_dir "%COMMIT_PROJECT_PATH%" git -C "%COMMIT_PROJECT_PATH%" commit -m "%COMMIT_MESSAGE%" || exit /b 1
) else (
    del "%STATUS_FILE%" >nul 2>nul
    echo No repository changes to commit: %COMMIT_PROJECT_NAME%
)

for %%R in (%COMMIT_REMOTES%) do (
    call :run_in_dir "%COMMIT_PROJECT_PATH%" git -C "%COMMIT_PROJECT_PATH%" push --force %%R HEAD:%BRANCH% || exit /b 1
)
exit /b 0

:align_failed_release_versions
if not defined BASE_STARTER_VERSION exit /b 0
if not defined RELEASE_VERSION exit /b 0

echo.
echo ========== failed release version alignment ==========
echo Align starter and dependencies to starter base version: %BASE_STARTER_VERSION%
echo Maven repository artifacts are not deleted or rolled back by this script.

if "%DRY_RUN%"=="1" (
    echo DryRun: skip failed release version alignment.
    exit /b 0
)

set "ROLLBACK_IN_PROGRESS=1"
call :align_project_version "fz-spring-boot-starter" "%PROJECT_STARTER%" "%BASE_STARTER_VERSION%" "gitee github"
set "ROLLBACK_STARTER_EXIT=%ERRORLEVEL%"
call :align_project_version "fz-spring-boot-starter-dependencies" "%PROJECT_DEPENDENCIES%" "%BASE_STARTER_VERSION%" "gitee"
set "ROLLBACK_DEP_EXIT=%ERRORLEVEL%"
set "ROLLBACK_IN_PROGRESS=0"

if not "%ROLLBACK_STARTER_EXIT%"=="0" exit /b %ROLLBACK_STARTER_EXIT%
if not "%ROLLBACK_DEP_EXIT%"=="0" exit /b %ROLLBACK_DEP_EXIT%
exit /b 0

:align_project_version
set "ALIGN_PROJECT_NAME=%~1"
set "ALIGN_PROJECT_PATH=%~2"
set "ALIGN_VERSION=%~3"
set "ALIGN_REMOTES=%~4"
set "ALIGN_CURRENT_VERSION="

call :get_fz_version "%ALIGN_PROJECT_PATH%" ALIGN_CURRENT_VERSION || exit /b 1
if "%ALIGN_CURRENT_VERSION%"=="%ALIGN_VERSION%" (
    echo Already aligned: %ALIGN_PROJECT_NAME% %ALIGN_VERSION%
    exit /b 0
)

call :set_fz_version "%ALIGN_PROJECT_PATH%" "%ALIGN_VERSION%" || exit /b 1
call :commit_and_push "%ALIGN_PROJECT_NAME%" "%ALIGN_PROJECT_PATH%" "%ALIGN_REMOTES%" "chore: align failed release version to starter %ALIGN_VERSION%" || exit /b 1
exit /b 0

:run_in_dir
set "WORK_DIR=%~1"
set "RUN_COMMAND="
:run_in_dir_args
shift
if "%~1"=="" goto run_in_dir_ready
if defined RUN_COMMAND (
    set "RUN_COMMAND=!RUN_COMMAND! "%~1""
) else (
    set "RUN_COMMAND="%~1""
)
goto run_in_dir_args
:run_in_dir_ready
echo [%WORK_DIR%] !RUN_COMMAND!
if "%DRY_RUN%"=="1" exit /b 0
pushd "%WORK_DIR%" || exit /b 1
call !RUN_COMMAND!
set "RUN_EXIT=%ERRORLEVEL%"
popd
if not "%RUN_EXIT%"=="0" (
    echo Command failed with exit code: %RUN_EXIT%
    exit /b %RUN_EXIT%
)
exit /b 0

:fail
echo.
echo ========== failed ==========
if defined CURRENT_PROJECT echo Failed project: %CURRENT_PROJECT%
if defined CURRENT_STAGE echo Failed stage: %CURRENT_STAGE%
if "%ROLLBACK_IN_PROGRESS%"=="0" (
    call :align_failed_release_versions
    if not "!ERRORLEVEL!"=="0" echo Failed to align versions after failed release.
)
set "MAVEN_OPTS=%OLD_MAVEN_OPTS%"
if not "%NO_PAUSE%"=="1" pause
exit /b 1

:nothing_to_release
echo.
echo ========== nothing to release ==========
echo fz-spring-boot-starter has no git changes.
echo Release stopped before version update. fz-spring-boot-starter-dependencies will not be upgraded.
if not "%NO_PAUSE%"=="1" pause
exit /b 0

:done
set "MAVEN_OPTS=%OLD_MAVEN_OPTS%"
if not "%NO_PAUSE%"=="1" pause
exit /b 0

@end

var fso = new ActiveXObject("Scripting.FileSystemObject");

function fail(message) {
    WScript.StdErr.WriteLine(message);
    WScript.Quit(1);
}

function trim(value) {
    return String(value).replace(/^\s+|\s+$/g, "");
}

function pomPath(projectPath) {
    return fso.BuildPath(projectPath, "pom.xml");
}

function readUtf8(path) {
    var stream = new ActiveXObject("ADODB.Stream");
    stream.Type = 2;
    stream.Charset = "utf-8";
    stream.Open();
    stream.LoadFromFile(path);
    var text = stream.ReadText();
    stream.Close();
    return text;
}

function writeUtf8NoBom(path, text) {
    var textStream = new ActiveXObject("ADODB.Stream");
    textStream.Type = 2;
    textStream.Charset = "utf-8";
    textStream.Open();
    textStream.WriteText(text);
    textStream.Position = 3;

    var binaryStream = new ActiveXObject("ADODB.Stream");
    binaryStream.Type = 1;
    binaryStream.Open();
    textStream.CopyTo(binaryStream);
    binaryStream.SaveToFile(path, 2);
    binaryStream.Close();
    textStream.Close();
}

function getVersion(projectPath) {
    var path = pomPath(projectPath);
    if (!fso.FileExists(path)) {
        fail("pom.xml does not exist: " + path);
    }

    var text = readUtf8(path);
    var pattern = /<fz\.version>([\s\S]*?)<\/fz\.version>/g;
    var match;
    var count = 0;
    var version = "";

    while ((match = pattern.exec(text)) !== null) {
        count++;
        version = trim(match[1]);
    }

    if (count !== 1) {
        fail("Expected exactly one <fz.version> in pom.xml, but found " + count + ": " + path);
    }

    WScript.Echo(version);
}

function setVersion(projectPath, newVersion, dryRun) {
    var path = pomPath(projectPath);
    if (!fso.FileExists(path)) {
        fail("pom.xml does not exist: " + path);
    }

    var text = readUtf8(path);
    var pattern = /(<fz\.version>)([\s\S]*?)(<\/fz\.version>)/g;
    var matches = text.match(pattern);
    if (!matches || matches.length !== 1) {
        fail("Expected exactly one <fz.version> in pom.xml, but found " + (matches ? matches.length : 0) + ": " + path);
    }

    var oldVersion = trim(/<fz\.version>([\s\S]*?)<\/fz\.version>/.exec(matches[0])[1]);
    if (oldVersion === newVersion) {
        WScript.Echo("Version unchanged: " + projectPath + " -> " + newVersion);
        return;
    }

    if (dryRun) {
        WScript.Echo("DryRun: " + path + " <fz.version> " + oldVersion + " -> " + newVersion);
        return;
    }

    var newText = text.replace(pattern, "$1" + newVersion + "$3");
    writeUtf8NoBom(path, newText);
    WScript.Echo("Updated version: " + projectPath + " " + oldVersion + " -> " + newVersion);
}

if (WScript.Arguments.length < 1) {
    fail("Missing helper command.");
}

var command = WScript.Arguments(0);
if (command === "getVersion") {
    getVersion(WScript.Arguments(1));
} else if (command === "setVersion") {
    setVersion(WScript.Arguments(1), WScript.Arguments(2), WScript.Arguments.length > 3 && WScript.Arguments(3) === "dryRun");
} else {
    fail("Unknown helper command: " + command);
}

