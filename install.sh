#!/bin/bash
set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}Starting Kotlin and Android development environment setup...${NC}"

# Check if running as root
if [ "$EUID" -ne 0 ]; then 
    echo -e "${RED}Please run as root (use sudo)${NC}"
    exit 1
fi

# Get real user
REAL_USER=$(logname || echo $SUDO_USER)
REAL_HOME=$(getent passwd $REAL_USER | cut -d: -f6)

# 1. Install OpenJDK 11 and tools
echo -e "${GREEN}Installing OpenJDK 11 and required tools...${NC}"
apt update
apt install -y openjdk-11-jdk curl zip unzip wget

# 2. Download and install Kotlin compiler
echo -e "${GREEN}Installing Kotlin compiler...${NC}"
KOTLIN_VERSION="1.9.22"  # Update this version as needed
KOTLIN_DIR="/opt/kotlin"
mkdir -p $KOTLIN_DIR
wget -q "https://github.com/JetBrains/kotlin/releases/download/v${KOTLIN_VERSION}/kotlin-compiler-${KOTLIN_VERSION}.zip"
unzip -q "kotlin-compiler-${KOTLIN_VERSION}.zip" -d $KOTLIN_DIR
rm "kotlin-compiler-${KOTLIN_VERSION}.zip"
ln -sf "$KOTLIN_DIR/kotlinc/bin/kotlin" /usr/local/bin/kotlin
ln -sf "$KOTLIN_DIR/kotlinc/bin/kotlinc" /usr/local/bin/kotlinc

# 3. Install Gradle directly
echo -e "${GREEN}Installing Gradle...${NC}"
GRADLE_VERSION="8.5"  # Update this version as needed
wget -q "https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"
unzip -q "gradle-${GRADLE_VERSION}-bin.zip" -d /opt
rm "gradle-${GRADLE_VERSION}-bin.zip"
ln -sf "/opt/gradle-${GRADLE_VERSION}/bin/gradle" /usr/local/bin/gradle

# 4. Create Android SDK directory
ANDROID_HOME=$REAL_HOME/Android/Sdk
mkdir -p $ANDROID_HOME
chown $REAL_USER:$REAL_USER $ANDROID_HOME
chmod 755 $ANDROID_HOME

# 5. Download and install Android command line tools
echo -e "${GREEN}Downloading Android command line tools...${NC}"
CMDLINE_TOOLS_URL="https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip"
wget -q $CMDLINE_TOOLS_URL
unzip -q commandlinetools-linux-*_latest.zip -d $ANDROID_HOME/cmdline-tools/
mv $ANDROID_HOME/cmdline-tools/cmdline-tools $ANDROID_HOME/cmdline-tools/latest
rm commandlinetools-linux-*_latest.zip
chown -R $REAL_USER:$REAL_USER $ANDROID_HOME

# 6. Set up environment variables
echo -e "${GREEN}Setting up environment variables...${NC}"
ENV_FILE="$REAL_HOME/.bashrc"
if [ -f "$REAL_HOME/.zshrc" ]; then
    ENV_FILE="$REAL_HOME/.zshrc"
fi

# Remove any existing entries
sed -i '/export ANDROID_HOME/d' $ENV_FILE
sed -i '/export PATH=.*android/d' $ENV_FILE
sed -i '/export KOTLIN_HOME/d' $ENV_FILE

# Add new environment variables
cat << EOF >> $ENV_FILE

# Android SDK
export ANDROID_HOME=$ANDROID_HOME
export PATH=\$PATH:\$ANDROID_HOME/cmdline-tools/latest/bin:\$ANDROID_HOME/platform-tools

# Kotlin
export KOTLIN_HOME=/opt/kotlin/kotlinc
export PATH=\$PATH:\$KOTLIN_HOME/bin
EOF

chown $REAL_USER:$REAL_USER $ENV_FILE

# 7. Install required SDK packages
echo -e "${GREEN}Installing SDK packages...${NC}"
su - $REAL_USER -c "yes | $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --licenses"
su - $REAL_USER -c "$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager 'platform-tools' 'platforms;android-33' 'build-tools;33.0.1'"

echo -e "${GREEN}Installation complete! Please restart your terminal or run:${NC}"
echo -e "${BLUE}source $ENV_FILE${NC}"

# Print verification commands
echo -e "\n${GREEN}To verify installation, run these commands:${NC}"
echo -e "java -version"
echo -e "kotlin -version"
echo -e "kotlinc -version"
echo -e "gradle -version"
echo -e "adb --version"

# Create a simple test script
cat << 'EOF' > /tmp/test_kotlin.kt
fun main() {
    println("Kotlin setup successful!")
}
EOF

echo -e "\n${BLUE}Test your Kotlin installation:${NC}"
echo -e "kotlinc /tmp/test_kotlin.kt -include-runtime -d test.jar"
echo -e "java -jar test.jar"

# Cleanup
rm /tmp/test_kotlin.kt
