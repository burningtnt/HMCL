#!/usr/bin/env bash

set -e

# Switch message language
if [ -z "${LANG##zh_*}" ]; then
  _HMCL_USE_CHINESE=true
else
  _HMCL_USE_CHINESE=false
fi

# _HMCL_OS
case "$OSTYPE" in
  linux*)
    _HMCL_OS="linux";;
  darwin*)
    _HMCL_OS="osx";;
  freebsd*)
    _HMCL_OS="freebsd";;
  msys*|cygwin*)
    _HMCL_OS="windows";;
  *)
    _HMCL_OS="unknown";;
esac

# Normalize _HMCL_ARCH
case "$(uname -m)" in
  x86_64|x86-64|amd64|em64t|x64)
    _HMCL_ARCH="x86_64";;
  x86_32|x86-32|x86|ia32|i386|i486|i586|i686|i86pc|x32)
    _HMCL_ARCH="x86";;
  arm64|aarch64|armv8*|armv9*)
    _HMCL_ARCH="arm64";;
  arm|arm32|aarch32|armv7*)
    _HMCL_ARCH="arm32";;
  loongarch64)
    _HMCL_ARCH="loongarch64";;
  *)
    _HMCL_ARCH="unknown";;
esac

# Self path
_HMCL_PATH="${BASH_SOURCE[0]}"
_HMCL_DIR=$(dirname "$_HMCL_PATH")

if [ "$_HMCL_OS" == "windows" ]; then
  _HMCL_JAVA_EXE_NAME="java.exe"
else
  _HMCL_JAVA_EXE_NAME="java"
fi

# _HMCL_VM_OPTIONS
if [ -n "${HMCL_JAVA_OPTS+x}" ]; then
  _HMCL_VM_OPTIONS=${HMCL_JAVA_OPTS}
else
  _HMCL_VM_OPTIONS="-XX:MinHeapFreeRatio=5 -XX:MaxHeapFreeRatio=15"
fi

# First, find Java in HMCL_JAVA_HOME
if [ -n "${HMCL_JAVA_HOME+x}" ]; then
  if [ -x "$HMCL_JAVA_HOME/bin/$_HMCL_JAVA_EXE_NAME" ]; then
    exec "$HMCL_JAVA_HOME/bin/$_HMCL_JAVA_EXE_NAME" $_HMCL_VM_OPTIONS -jar "$_HMCL_PATH"
  else
    if [ "$_HMCL_USE_CHINESE" == true ]; then
      show_message "错误" "环境变量 HMCL_JAVA_HOME 的值无效，请设置为合法的 Java 路径。\n你可以访问 https://docs.hmcl.net/help.html 页面寻求帮助。"
    else
      show_message "Error" "The value of the environment variable HMCL_JAVA_HOME is invalid, please set it to a valid Java path.\nYou can visit the https://docs.hmcl.net/help.html page for help."
    fi
    exit 1
  fi
fi

# Find Java in HMCL_DIR
case "$_HMCL_ARCH" in
  x86_64)
    if [ -x "$_HMCL_DIR/jre-x64/bin/$_HMCL_JAVA_EXE_NAME" ]; then
      exec "$_HMCL_DIR/jre-x64/bin/$_HMCL_JAVA_EXE_NAME" $_HMCL_VM_OPTIONS -jar "$_HMCL_PATH"
    fi
    if [ -x "$_HMCL_DIR/jre-x86/bin/$_HMCL_JAVA_EXE_NAME" ]; then
      exec "$_HMCL_DIR/jre-x86/bin/$_HMCL_JAVA_EXE_NAME" $_HMCL_VM_OPTIONS -jar "$_HMCL_PATH"
    fi
    ;;
  x86)
    if [ -x "$_HMCL_DIR/jre-x86/bin/$_HMCL_JAVA_EXE_NAME" ]; then
      exec "$_HMCL_DIR/jre-x86/bin/$_HMCL_JAVA_EXE_NAME" $_HMCL_VM_OPTIONS -jar "$_HMCL_PATH"
    fi
    ;;
  arm64)
    if [ -x "$_HMCL_DIR/jre-arm64/bin/$_HMCL_JAVA_EXE_NAME" ]; then
      exec "$_HMCL_DIR/jre-arm64/bin/$_HMCL_JAVA_EXE_NAME" $_HMCL_VM_OPTIONS -jar "$_HMCL_PATH"
    fi
    ;;
  arm32)
    if [ -x "$_HMCL_DIR/jre-arm32/bin/$_HMCL_JAVA_EXE_NAME" ]; then
      exec "$_HMCL_DIR/jre-arm32/bin/$_HMCL_JAVA_EXE_NAME" $_HMCL_VM_OPTIONS -jar "$_HMCL_PATH"
    fi
    ;;
  loongarch64)
    if [ -x "$_HMCL_DIR/jre-loongarch64/bin/$_HMCL_JAVA_EXE_NAME" ]; then
      exec "$_HMCL_DIR/jre-loongarch64/bin/$_HMCL_JAVA_EXE_NAME" $_HMCL_VM_OPTIONS -jar "$_HMCL_PATH"
    fi
    ;;
esac

# Find Java in JAVA_HOME
if [ -f "$JAVA_HOME/bin/$_HMCL_JAVA_EXE_NAME" ]; then
  exec "$JAVA_HOME/bin/$_HMCL_JAVA_EXE_NAME" $_HMCL_VM_OPTIONS -jar "$_HMCL_PATH"
fi

# Find Java in PATH
if [ -x "$(command -v $_HMCL_JAVA_EXE_NAME)" ]; then
  exec $_HMCL_JAVA_EXE_NAME $_HMCL_VM_OPTIONS -jar "$_HMCL_PATH"
fi

# Java not found

if [ "$_HMCL_OS" == "osx" ]; then
    _HMCL_DOWNLOAD_PAGE_OS="macos"
else
    _HMCL_DOWNLOAD_PAGE_OS="$_HMCL_OS"
fi

case "$_HMCL_OS-$_HMCL_ARCH" in
  windows-x86|windows-x86_64|windows-arm64|linux-x86|linux-x86_64|linux-arm32|linux-arm64|linux-loongarch64|macos-x86_64|macos-arm64)
    _HMCL_JAVA_DOWNLOAD_PAGE="https://docs.hmcl.net/downloads/$_HMCL_DOWNLOAD_PAGE_OS/$_HMCL_ARCH.html"
    ;;
esac

if [ "$_HMCL_USE_CHINESE" == true ]; then
  _HMCL_WARNING_TITLE="错误"
  _HMCL_WARNING_MESSAGE_BASE="运行 HMCL 需要 Java 运行时环境，请安装 Java 并设置环境变量后重试。"
  _HMCL_WARNING_MESSAGE1="$_HMCL_WARNING_MESSAGE_BASE\n你可以前往此处下载:\n$_HMCL_JAVA_DOWNLOAD_PAGE"
  _HMCL_WARNING_MESSAGE2="$_HMCL_WARNING_MESSAGE_BASE\n\n是否要前往 Java 下载页面？"
  _HMCL_WARNING_MESSAGE3="$_HMCL_WARNING_MESSAGE_BASE\n你可以访问 https://docs.hmcl.net/help.html 页面寻求帮助。"
else
  _HMCL_WARNING_TITLE="Error"
  _HMCL_WARNING_MESSAGE_BASE="The Java runtime environment is required to run HMCL.\nPlease install Java and set the environment variables and try again."
  _HMCL_WARNING_MESSAGE1="$_HMCL_WARNING_MESSAGE_BASE\nYou can download it from here:\n$_HMCL_JAVA_DOWNLOAD_PAGE"
  _HMCL_WARNING_MESSAGE2="$_HMCL_WARNING_MESSAGE_BASE\n\nDo you want to go to the Java download page?"
  _HMCL_WARNING_MESSAGE3="$_HMCL_WARNING_MESSAGE_BASE\nYou can visit the https://docs.hmcl.net/help.html page for help."
fi

if [ -n "$_HMCL_JAVA_DOWNLOAD_PAGE" ]; then
  echo -e "$_HMCL_WARNING_TITLE: $_HMCL_WARNING_MESSAGE1"

  if [ -n "$(command -v xdg-open)" ]; then
    if [ -n "$(command -v zenity)" ]; then
      zenity --question --title="$_HMCL_WARNING_TITLE" --text="$_HMCL_WARNING_MESSAGE2"
    elif [ -n "$(command -v kdialog)" ]; then
      kdialog --title "$_HMCL_WARNING_TITLE" --warningyesno "$_HMCL_WARNING_MESSAGE2"
    fi
    xdg-open "$_HMCL_JAVA_DOWNLOAD_PAGE"
  fi
else
  echo -e "$_HMCL_WARNING_TITLE: $_HMCL_WARNING_MESSAGE3"
  if [ -n "$(command -v zenity)" ]; then
    zenity --info --title="$_HMCL_WARNING_TITLE" --text="$_HMCL_WARNING_MESSAGE3"
  elif [ -n "$(command -v kdialog)" ]; then
    kdialog --title "$_HMCL_WARNING_TITLE" --msgbox "$_HMCL_WARNING_MESSAGE3"
  fi
fi

exit 1
