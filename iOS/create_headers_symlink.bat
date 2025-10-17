@echo off
echo Creating AnyThinkSDK header symlinks...

REM Create AnyThinkSDK directory in Headers/Public
mkdir "Pods\Headers\Public\AnyThinkSDK"

REM Copy all header files from AnyThinkSDK.framework to Headers/Public/AnyThinkSDK
xcopy "Pods\AnyThinkiOS\core\AnyThinkSDK.xcframework\ios-arm64\AnyThinkSDK.framework\Headers\*" "Pods\Headers\Public\AnyThinkSDK\" /Y /I

echo Header symlinks created successfully!
pause