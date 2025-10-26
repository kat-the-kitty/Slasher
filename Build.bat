@echo off
echo Building Slasher APK...

REM Set your Android SDK path
set ANDROID_HOME=C:\Android
set BUILD_TOOLS=%ANDROID_HOME%\build-tools\34.0.0
set PLATFORM=%ANDROID_HOME%\platforms\android-33

REM Add tools to PATH
set PATH=%PATH%;%BUILD_TOOLS%;%ANDROID_HOME%\platform-tools

rmdir /s /q bin gen obj

REM Create output directories
if not exist bin mkdir bin
if not exist gen mkdir gen
if not exist obj mkdir obj

echo.
echo [1/7] Compiling resources...
call aapt2 compile --dir res -o obj\res.zip
if errorlevel 1 goto error

echo [2/7] Linking resources and generating R.java...
call aapt2 link -o bin\slasher.apk -I %PLATFORM%\android.jar --manifest AndroidManifest.xml obj\res.zip --java gen
if errorlevel 1 goto error

echo [3/7] Compiling Java source files...
javac --release 8 -d bin -classpath "%PLATFORM%\android.jar" -sourcepath src gen\kitty\kat\dev\slasher\R.java src\kitty\kat\dev\slasher\*.java
if errorlevel 1 goto error

echo.
echo Compiled class files:
dir /s /b bin\kitty\kat\dev\slasher\*.class
echo.

echo [4/7] Converting to DEX format...
echo Running: java -cp "%BUILD_TOOLS%\lib\d8.jar" com.android.tools.r8.D8 --lib %PLATFORM%\android.jar --release --output bin bin\kitty\kat\dev\slasher\*.class
java -cp "%BUILD_TOOLS%\lib\d8.jar" com.android.tools.r8.D8 --lib %PLATFORM%\android.jar --release --output bin bin\kitty\kat\dev\slasher\*.class
if errorlevel 1 goto error

echo [5/7] Adding DEX to APK...
cd bin
jar -uf slasher.apk classes.dex
cd ..
if errorlevel 1 goto error

echo [6/7] Aligning APK...
call zipalign -f 4 bin\slasher.apk bin\slasher-aligned.apk
if errorlevel 1 goto error

echo [7/7] Signing APK...
if not exist debug.keystore (
    echo Creating debug keystore...
    keytool -genkeypair -v -keystore debug.keystore -alias androiddebugkey -keyalg RSA -keysize 2048 -validity 10000 -storepass android -keypass android -dname "CN=Android Debug,O=Android,C=US"
)

call apksigner sign --ks debug.keystore --ks-pass pass:android --key-pass pass:android --out bin\slasher-debug.apk bin\slasher-aligned.apk
if errorlevel 1 goto error

echo.
echo ========================================
echo SUCCESS! APK created at: bin\slasher-debug.apk
echo ========================================
echo.
echo To install on device:
echo adb install bin\slasher-debug.apk
echo.
pause
goto end

:error
echo.
echo ========================================
echo ERROR: Build failed!
echo ========================================
pause
exit /b 1

:end