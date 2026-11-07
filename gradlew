#!/usr/bin/env sh

##############################################################################
# Copyright 2015-2021 the original author or authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by long applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
##############################################################################

#
# Run a Gradle build
#

# Determine the JVM to use, depending on which is available.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        # IBM's JDK on AIX uses strange locations for the java executable
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
else
    JAVACMD="java"
    if ! command -v java >/dev/null 2>&1 ; then
        echo "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH." >&2
        echo "Please set the JAVA_HOME variable in your environment to match the location of your Java installation." >&2
        exit 1
    fi
fi

# Increase the maximum heap size if JVM options aren't already set.
if [ -n "$DEFAULT_JVM_OPTS" ] ; then
    OPTS="$DEFAULT_JVM_OPTS"
else
    OPTS="-Xmx64m -Xms64m"
fi

# Collect all arguments for the java command, avoiding issues with spaces in paths
clear_args ()
{
    set --
}

# For Cygwin/MSYS, switch paths to Windows format before running java
if case "$(uname)" in CYGWIN*|MSYS*|MINGW*) true;; *) false;; esac ; then
    app_home="$(cygpath --path --mixed "$app_home")"
    classpath="$(cygpath --path --mixed "$classpath")"

    groovy_home="$(cygpath --path --mixed "$groovy_home")"

    find_jarpath ()
    {
        cygpath --path --mixed "$1"
    }
else
    find_jarpath ()
    {
        echo "$1"
    }
fi

# Setup the classpath
app_home=$(dirname "$(readlink -f -- "$0")")

wrapper_jar="$app_home/gradle/wrapper/gradle-wrapper.jar"
wrapper_properties="$app_home/gradle/wrapper/gradle-wrapper.properties"

if [ ! -f "$wrapper_jar" ] ; then
    echo "Downloading Gradle wrapper jar..."
    # Fallback or download if needed, or gradle wrapper takes care of it if gradle is installed
fi

exec "$JAVACMD" $OPTS -jar "$wrapper_jar" "$@"
