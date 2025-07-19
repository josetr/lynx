# First follow all the steps from README.md

## Mac OS
export JAVA_HOME=/Library/Java/JavaVirtualMachines/zulu-11.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

## cd src/lynx
## source tools/envsetup.sh
## tools/hab sync .

# python3 -m venv venv
source venv/bin/activate
# pip3 install pyyaml

# 
# ./gradlew :LynxExplorer:assembleNoAsanDebug --no-daemon
# adb install lynx_explorer/build/outputs/apk/noasan/debug/LynxExplorer-noasan-debug.apk

./gradlew :LynxExplorer:assembleNoAsanRelease --no-daemon
./deploy-local.sh
