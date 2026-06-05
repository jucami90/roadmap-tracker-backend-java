#!/usr/bin/env bash
# Creates a macOS .dmg installer for Roadmap Tracker.
# Requirements: Temurin 21 JDK, Maven on PATH.
set -euo pipefail

# ── Configuration ─────────────────────────────────────────────────────────────
APP_NAME="Roadmap Tracker"
APP_VERSION="1.0.0"
MAIN_CLASS="com.roadmap.app.MainApp"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
TEMURIN_HOME="/Users/jucami90/Library/Java/JavaVirtualMachines/temurin-21.0.10/Contents/Home"
JPACKAGE="$TEMURIN_HOME/bin/jpackage"
JAVAC="$TEMURIN_HOME/bin/javac"
JAVA="$TEMURIN_HOME/bin/java"
JAR_CMD="$TEMURIN_HOME/bin/jar"
PACKAGING_DIR="$SCRIPT_DIR/packaging"
STAGING_DIR="$SCRIPT_DIR/packaging/staging"
DIST_DIR="$SCRIPT_DIR/dist"
M2="$HOME/.m2/repository"
FX_VERSION="21.0.2"
FX_DIR="$M2/org/openjfx"

# Derived names
SHADE_JAR="$SCRIPT_DIR/target/roadmap-app-${APP_VERSION}.jar"
MAIN_JAR="roadmap-app-${APP_VERSION}.jar"

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo " Roadmap Tracker — macOS package builder"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# ── Step 1: Generate icon ──────────────────────────────────────────────────────
echo ""
echo "[1/5] Generating icon..."
ICONSET="$PACKAGING_DIR/icon.iconset"

if [ ! -f "$PACKAGING_DIR/icon.icns" ]; then
    cd "$PACKAGING_DIR"
    "$JAVAC" IconGenerator.java
    "$JAVA" IconGenerator "$ICONSET"
    iconutil -c icns "$ICONSET" -o icon.icns
    echo "     icon.icns created."
else
    echo "     icon.icns already exists, skipping."
fi

# ── Step 2: Build fat JAR ──────────────────────────────────────────────────────
echo ""
echo "[2/5] Building fat JAR (mvn package)..."
cd "$SCRIPT_DIR"
mvn clean package -DskipTests -q
echo "     Built: $SHADE_JAR"

# ── Step 3: Prepare staging directory ─────────────────────────────────────────
echo ""
echo "[3/5] Preparing staging directory..."
rm -rf "$STAGING_DIR"
mkdir -p "$STAGING_DIR"

# Copy fat JAR and strip ALL module-info.class files so it runs as an unnamed module.
# The shade plugin places them under META-INF/versions/*/module-info.class in multi-release JARs.
cp "$SHADE_JAR" "$STAGING_DIR/$MAIN_JAR"
MODULE_INFOS=$("$JAR_CMD" tf "$STAGING_DIR/$MAIN_JAR" 2>/dev/null | grep "module-info.class" || true)
if [ -n "$MODULE_INFOS" ]; then
    while IFS= read -r entry; do
        zip -q -d "$STAGING_DIR/$MAIN_JAR" "$entry" 2>/dev/null || true
    done <<< "$MODULE_INFOS"
    echo "     Removed module-info entries: $(echo "$MODULE_INFOS" | wc -l | tr -d ' ')"
fi

# Copy JavaFX mac-aarch64 JARs (needed as module-path inside the .app)
for MOD in javafx-base javafx-controls javafx-fxml javafx-graphics; do
    FX_JAR="$FX_DIR/$MOD/$FX_VERSION/${MOD}-${FX_VERSION}-mac-aarch64.jar"
    if [ -f "$FX_JAR" ]; then
        cp "$FX_JAR" "$STAGING_DIR/"
        echo "     + $MOD"
    else
        echo "     WARN: not found: $FX_JAR"
    fi
done

echo "     Staging directory ready."

# ── Step 4: Run jpackage ───────────────────────────────────────────────────────
echo ""
echo "[4/5] Running jpackage..."
rm -rf "$DIST_DIR"
mkdir -p "$DIST_DIR"

"$JPACKAGE" \
    --type dmg \
    --name "$APP_NAME" \
    --app-version "$APP_VERSION" \
    --input "$STAGING_DIR" \
    --main-jar "$MAIN_JAR" \
    --main-class "$MAIN_CLASS" \
    --icon "$PACKAGING_DIR/icon.icns" \
    --dest "$DIST_DIR" \
    --java-options "--module-path \$APPDIR" \
    --java-options "--add-modules javafx.controls,javafx.fxml" \
    --java-options "--add-opens=java.base/java.lang=ALL-UNNAMED" \
    --java-options "--add-opens=java.base/java.util=ALL-UNNAMED" \
    --java-options "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED" \
    --java-options "--add-opens=java.base/java.io=ALL-UNNAMED" \
    --java-options "-Djava.awt.headless=false" \
    --java-options "-Xmx512m" \
    --mac-package-identifier "com.roadmap.app" \
    --description "Backend Roadmap Tracker" \
    --vendor "jucami90"

# ── Step 5: Done ───────────────────────────────────────────────────────────────
echo ""
echo "[5/5] Done!"
echo ""
DMG_FILE=$(ls "$DIST_DIR"/*.dmg 2>/dev/null | head -1)
if [ -n "$DMG_FILE" ]; then
    echo "     Installer: $DMG_FILE"
    echo "     Size:      $(du -sh "$DMG_FILE" | cut -f1)"
else
    echo "     WARN: No .dmg found in $DIST_DIR"
    ls "$DIST_DIR"
fi
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
