#!/bin/bash
set -e

echo "=== Installing Android SDK in Jenkins container ==="

# Install dependencies
apt-get update -qq
apt-get install -y -qq wget unzip 2>&1 | tail -3

# Download Android SDK commandline tools
cd /opt
wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O cmdline-tools.zip
unzip -q cmdline-tools.zip -d android-sdk
rm cmdline-tools.zip

# Move to expected directory structure
if [ -d "android-sdk/cmdline-tools" ]; then
    mkdir -p android-sdk/cmdline-tools/latest
    mv android-sdk/cmdline-tools/* android-sdk/cmdline-tools/latest/ 2>/dev/null || true
fi

# Accept licenses and install required components
export ANDROID_HOME=/opt/android-sdk
yes | /opt/android-sdk/cmdline-tools/latest/bin/sdkmanager --sdk_root=/opt/android-sdk "platforms;android-36" "build-tools;36.0.0" 2>&1 | tail -10

# Set ANDROID_HOME in profile
echo "export ANDROID_HOME=/opt/android-sdk" >> /etc/profile.d/android.sh
echo "export PATH=\$PATH:\$ANDROID_HOME/cmdline-tools/latest/bin:\$ANDROID_HOME/platform-tools" >> /etc/profile.d/android.sh
chmod +x /etc/profile.d/android.sh

# Give jenkins user ownership
chown -R jenkins:jenkins /opt/android-sdk

echo "=== Android SDK installation complete ==="
echo "ANDROID_HOME=/opt/android-sdk"
