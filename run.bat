@echo off
echo MotorPH Payroll System - Compile and Run
echo ==========================================

echo Compiling...
if not exist out mkdir out
javac -cp "lib/*" -d out -sourcepath src src\motorph\Main.java src\motorph\model\*.java src\motorph\dao\*.java src\motorph\service\*.java src\motorph\ui\*.java src\motorph\util\*.java

if %errorlevel% neq 0 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo Compilation successful!
echo.
echo Running MotorPH Payroll System...
java -cp "out;lib/*" motorph.Main
pause
