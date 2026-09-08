#!/usr/bin/env sh
# Lightweight repository launcher. Android Studio can also run the project directly.
# The source checkout intentionally does not carry a binary wrapper JAR.
set -eu
if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi
cat >&2 <<'EOF'
Gradle is not installed in this environment. Open the project in Android Studio with JDK 17,
or install Gradle 8.9 and run this command again. The expected distribution is recorded in
 gradle/wrapper/gradle-wrapper.properties.
EOF
exit 1
