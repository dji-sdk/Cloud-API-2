#!/bin/bash

# Flight Hub Java SDK 构建脚本
# 作者: DJI
# 版本: 1.0

# Exit immediately if a command exits with a non-zero status.
set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# BIN-P0-01: macOS 上必须使用系统自带的 strip 并 ad-hoc 重签, 否则:
#   1) PATH 中若存在 Homebrew binutils 的 GNU strip, 会破坏 Mach-O dylib
#      (丢失 LC_ID_DYLIB、损坏段布局), 导致 dyld 拒绝加载;
#   2) dylib 只能用 -x (仅移除本地符号) , 不能全 strip, 否则丢导出符号 JVM 无法解析;
#   3) strip 会使原有签名失效——arm64 内核对签名失效的 dylib 直接 SIGKILL,
#      因此 strip 后必须立即 ad-hoc 重签。
# 父脚本 build_java.sh 也有同名 override, 但 bash 函数默认不 export 到子进程,
# 所以本子脚本必须自带一份, 否则本脚本里的 `strip $file` 会走 PATH 里的默认 strip
# (没有 -x, 也没有 codesign) , 结果就是 dylib 里 100+ 万 local symbol 一个不掉。
if [ "$(uname)" = "Darwin" ]; then
    strip() { /usr/bin/strip -x "$@" && /usr/bin/codesign --force --sign - "${@: -1}"; }
fi

# --- Configuration ---
# 获取当前脚本所在目录的父目录（即platform/java目录）
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PLATFORM_JAVA_DIR="$(dirname "$SCRIPT_DIR")"
ROOT_DIR="$(dirname "$(dirname "$PLATFORM_JAVA_DIR")")"

# 并行度：上层 export 的 FHSDK_JOBS / FHSDK_MAX_JOBS，否则满核
fhsdk_detect_cores() {
    local c
    c="$(nproc 2>/dev/null || sysctl -n hw.ncpu 2>/dev/null || echo 4)"
    case "$c" in ""|*[!0-9]*) c=4 ;; esac
    echo "$c"
}
fhsdk_resolve_jobs() {
    if [ -n "${FHSDK_JOBS:-}" ] && [ "$FHSDK_JOBS" -gt 0 ] 2>/dev/null; then
        echo "$FHSDK_JOBS"
    elif [ -n "${FHSDK_MAX_JOBS:-}" ] && [ "$FHSDK_MAX_JOBS" -gt 0 ] 2>/dev/null; then
        echo "$FHSDK_MAX_JOBS"
    else
        fhsdk_detect_cores
    fi
}

BUILD_DIR_DEBUG="$PLATFORM_JAVA_DIR/build_java_debug"
BUILD_DIR_RELEASE="$PLATFORM_JAVA_DIR/build_java_release"
DIST_DIR="$PLATFORM_JAVA_DIR/dist"
BIN_DIR="$PLATFORM_JAVA_DIR/bin"
LIB_DIR="$PLATFORM_JAVA_DIR/lib"

THIRD_PARTIES_DIR="$ROOT_DIR/third_party"

# macOS 上必须使用系统自带的 strip：PATH 中若存在 Homebrew binutils 的 GNU strip，
# 它会破坏 Mach-O dylib（丢失 LC_ID_DYLIB、损坏段布局），导致 dyld 拒绝加载。
# dylib 需要 -x（仅移除本地符号）。
# strip 会使原有签名失效（arm64 内核对签名失效的 dylib 直接 SIGKILL），
# 因此 strip 后必须立即 ad-hoc 重签。
if [ "$(uname)" = "Darwin" ]; then
    strip() { /usr/bin/strip -x "$@" && /usr/bin/codesign --force --sign - "${@: -1}"; }
fi

# 从gradle.properties读取版本信息
get_gradle_property() {
    local property_name="$1"
    local gradle_props_file="$PLATFORM_JAVA_DIR/gradle.properties"
    
    if [ -f "$gradle_props_file" ]; then
        grep "^${property_name}=" "$gradle_props_file" | cut -d'=' -f2 | tr -d ' '
    else
        log_error "gradle.properties文件不存在: $gradle_props_file"
        return 1
    fi
}

# 获取当前系统平台
get_platform() {
    local OS=$(uname -s)
    local ARCH=$(uname -m)
    
    case "$OS" in
        Linux*)     
            case "$ARCH" in
                x86_64) echo "linux-x86_64" ;;
                *) echo "linux-unknown" ;;
            esac
            ;;
        Darwin*)    
            case "$ARCH" in
                arm64) echo "macosx-arm64" ;;
                *) echo "macosx-unknown" ;;
            esac
            ;;
        *) echo "unknown-platform" ;;
    esac
}

# 检查第三方库的可用性
check_third_party_libs() {
    local platform=$(get_platform)
    log_info "检查平台 $platform 的第三方库可用性..."
    
    case "$platform" in
        linux-x86_64)
            local libs=(
                "$THIRD_PARTIES_DIR/paho_mqtt_c/lib/linux/x86_64/libpaho-mqtt3as.so.1"
                "$THIRD_PARTIES_DIR/openssl_1_1_1/lib/linux/x86_64/libcrypto.so.1.1"
                "$THIRD_PARTIES_DIR/openssl_1_1_1/lib/linux/x86_64/libssl.so.1.1"
                "$THIRD_PARTIES_DIR/websockets/lib/linux/x86_64/libwebsockets.so.12"
            )
            ;;
            macosx-arm64)
            # macOS 使用系统 OpenSSL，不需要检查 OpenSSL 库
            local libs=(
                "$THIRD_PARTIES_DIR/paho_mqtt_c/lib/macosx/arm64/libpaho-mqtt3as.1.dylib"
                "$THIRD_PARTIES_DIR/websockets/lib/macosx/arm64/libwebsockets.12.dylib"
            )
            ;;
        *)
            log_warning "未知平台: $platform"
            local libs=()
            ;;
    esac
    
    if [ ${#libs[@]} -gt 0 ]; then
        for lib in "${libs[@]}"; do
            if [ -f "$lib" ]; then
                log_info "✓ 找到库: $lib"
            else
                log_warning "✗ 缺少库: $lib"
            fi
        done
    fi
}

# 检查必要的工具
check_dependencies() {
    log_info "检查构建依赖..."
    
    local required_tools=("cmake" "swig" "java" "javac")
    local missing_tools=()
    
    for tool in "${required_tools[@]}"; do
        if ! command -v "$tool" &> /dev/null; then
            missing_tools+=("$tool")
        fi
    done
    
    if [ ${#missing_tools[@]} -ne 0 ]; then
        log_error "缺少必要的构建工具: ${missing_tools[*]}"
        log_error "请安装以下工具:"
        for tool in "${missing_tools[@]}"; do
            case "$tool" in
                cmake) log_error "  - CMake: https://cmake.org/download/" ;;
                swig) log_error "  - SWIG: http://www.swig.org/download.html" ;;
                java|javac) log_error "  - Java JDK: https://openjdk.java.net/" ;;
            esac
        done
        return 1
    fi
    
    log_success "所有必要的构建依赖已满足"
}

# 清理构建目录
clean_build() {
    log_info "清理构建目录..."
    
    if [ -d "$BUILD_DIR_DEBUG" ]; then
        rm -rf "$BUILD_DIR_DEBUG"
        log_info "已清理 $BUILD_DIR_DEBUG"
    fi
    
    if [ -d "$BUILD_DIR_RELEASE" ]; then
        rm -rf "$BUILD_DIR_RELEASE"
        log_info "已清理 $BUILD_DIR_RELEASE"
    fi
    
    if [ -d "$DIST_DIR" ]; then
        rm -rf "$DIST_DIR"
        log_info "已清理 $DIST_DIR"
    fi
    
    # 清理源码目录中的class文件
    clean_source_class_files
    
    # 注意：不清理SWIG生成的Java代码，因为它们是源码的一部分
    # 如果需要重新生成SWIG代码，请手动删除generated目录或使用CMake重新构建
    
    # 清理gradle生成的bin目录
    local gradle_bin_dir="$PLATFORM_JAVA_DIR/flight_hub_sdk/bin"
    if [ -d "$gradle_bin_dir" ]; then
        rm -rf "$gradle_bin_dir"
        log_info "已清理Gradle生成的bin目录: $gradle_bin_dir"
    fi
    
    # 清理gradle生成的build目录
    local gradle_build_dir="$PLATFORM_JAVA_DIR/flight_hub_sdk/build"
    if [ -d "$gradle_build_dir" ]; then
        rm -rf "$gradle_build_dir"
        log_info "已清理Gradle生成的build目录: $gradle_build_dir"
    fi
    
    # 清理demo项目的构建目录
    local demo_build_dir="$PLATFORM_JAVA_DIR/flight_hub_sdk_demo/build"
    if [ -d "$demo_build_dir" ]; then
        rm -rf "$demo_build_dir"
        log_info "已清理Demo项目的build目录: $demo_build_dir"
    fi
    
    log_success "构建目录清理完成"
}

# 清理源码目录中的class文件
clean_source_class_files() {
    log_info "清理源码目录中的class文件..."
    
    # 清理flight_hub_sdk源码目录中的class文件
    local sdk_src_dir="$PLATFORM_JAVA_DIR/flight_hub_sdk/src"
    if [ -d "$sdk_src_dir" ]; then
        find "$sdk_src_dir" -name "*.class" -type f -delete
        log_info "已清理SDK源码目录中的class文件"
    fi
    
    # 清理demo源码目录中的class文件
    local demo_src_dir="$PLATFORM_JAVA_DIR/flight_hub_sdk_demo/src"
    if [ -d "$demo_src_dir" ]; then
        find "$demo_src_dir" -name "*.class" -type f -delete
        log_info "已清理Demo源码目录中的class文件"
    fi
    
    log_success "源码目录class文件清理完成"
}

# 生成SWIG包装代码
generate_swig_wrapper() {
    log_info "生成SWIG包装代码..."
    
    local swig_file="$PLATFORM_JAVA_DIR/swig/flight_hub_sdk.i"
    # 将SWIG生成的Java文件放到flight_hub_sdk的源码目录下的generated子目录
    local java_generated_dir="$PLATFORM_JAVA_DIR/flight_hub_sdk/src/main/java/com/dji/flighthubsdk/generated"
    local java_package="com.dji.flighthubsdk.generated"
    
    if [ ! -f "$swig_file" ]; then
        log_error "SWIG接口文件不存在: $swig_file"
        return 1
    fi
    
    # 创建Java输出目录
    mkdir -p "$java_generated_dir"
    
    # 运行SWIG生成Java包装代码
    # 注意：C++文件将由CMake构建过程处理，这里只生成Java文件
    cd "$PLATFORM_JAVA_DIR"
    # Stage B B4: SWIG 优先仓内 deps/message_engine
    local me_api="$ROOT_DIR/deps/message_engine/src/api"
    if [ ! -d "$me_api" ]; then
        me_api="$(dirname "$ROOT_DIR")/dji_message_engine/src/api"
    fi
    swig -c++ -java -package "$java_package" \
         -outdir "$java_generated_dir" \
         -I"$ROOT_DIR/src/api" \
         -I"$ROOT_DIR/src" \
         -I"$me_api" \
         "$swig_file"
    
    if [ $? -eq 0 ]; then
        log_success "SWIG包装代码生成成功"
        log_info "Java文件生成在: $java_generated_dir"
        log_info "C++文件将由CMake构建过程处理"
    else
        log_error "SWIG包装代码生成失败"
        return 1
    fi
}

# 构建C++库
build_cpp_library() {
    local build_type="$1"  # Debug 或 Release
    local target_build_dir
    
    if [ "$build_type" = "Debug" ]; then
        target_build_dir="$BUILD_DIR_DEBUG"
    else
        target_build_dir="$BUILD_DIR_RELEASE"
    fi
    
    log_info "构建C++库 ($build_type)..."
    
    mkdir -p "$target_build_dir"
    cd "$target_build_dir"
    
    local platform_for_preset
    platform_for_preset=$(get_platform)
    local preset=""
    local bt_lower="release"
    if [ "$build_type" = "Debug" ]; then
        bt_lower="debug"
    fi
    case "$platform_for_preset" in
        macosx-arm64) preset="macos-arm64-${bt_lower}" ;;
        linux-x86_64) preset="linux-x86_64-${bt_lower}" ;;
        *)
            log_error "没有对应的 CMake preset: $platform_for_preset"
            return 1
            ;;
    esac

    local cmake_flags=(
        --preset "$preset"
        -DCMAKE_CXX_FLAGS=-w
    )
    if [ "$build_type" = "Debug" ]; then
        cmake_flags+=(
            -DCMAKE_CXX_FLAGS_DEBUG="-g -O0 -DDEBUG"
            -DCMAKE_C_FLAGS_DEBUG="-g -O0 -DDEBUG"
        )
    fi

    cmake -S "$ROOT_DIR" -B "$target_build_dir" "${cmake_flags[@]}"

    if [ $? -ne 0 ]; then
        log_error "CMake配置失败"
        return 1
    fi
    
    # 编译：默认满核；可用 FHSDK_JOBS / -j 覆盖
    PARALLEL_JOBS=$(fhsdk_resolve_jobs)
    log_info "编译并行度: $PARALLEL_JOBS"
    make -j"$PARALLEL_JOBS"
    
    if [ $? -eq 0 ]; then
        log_success "C++库构建成功 ($build_type)"
        
        # 复制库文件到lib目录
        local platform=$(get_platform)
        local lib_platform_dir="$LIB_DIR/$platform"
        mkdir -p "$lib_platform_dir"

        # C-01: 每次构建前清空 lib_platform_dir 里的 native 产物,
        # 避免历史遗留 so/dylib (含已关闭的 Volc/Agora native) 被后续 gradle 打进 jar.
        # 仅清 .so / .so.* / .dylib, 不动子目录或其他文件.
        log_info "清理 $lib_platform_dir 里的历史 native 产物..."
        find "$lib_platform_dir" -maxdepth 1 -type f \( -name "*.so" -o -name "*.so.*" -o -name "*.dylib" \) -delete 2>/dev/null || true
        
        # 复制生成的libflighthubsdk_jni共享库
        if [ -f "libflighthubsdk_jni.so" ]; then
            cp "libflighthubsdk_jni.so" "$lib_platform_dir/"
            # Debug构建时不strip，保留调试信息
            if [ "$build_type" != "Debug" ]; then
                strip "$lib_platform_dir/libflighthubsdk_jni.so"
            fi

            # BIN-P0-04: 清除嵌入的 CI/开发路径。-ffile-prefix-map 只重写本次编译新产生的
            # 调试段/rodata; 但 OpenSSL 1.1.1 等预编译静态库里已在 .rodata 段固化的 __FILE__
            # 路径（比如 "/home/jenkins/code_base/..."）无法通过编译选项清除,strip 也不动
            # rodata。用等长二进制替换直接把敏感前缀改成中性占位, 不影响段布局与运行时行为。
            if command -v python3 >/dev/null 2>&1; then
                python3 - "$lib_platform_dir/libflighthubsdk_jni.so" <<'PY'
import sys
p = sys.argv[1]
mapping = [
    # BUILD-01: 映射目标统一为 /opt/vendor__(13 字符等长)。
    # 原目标 /build/vendor 会使本清理"制造"出与上游流水线相同的伪路径前缀,
    # 与随包 4 个 .so 的清理映射冲突, 故一并统一。
    (b'/home/jenkins', b'/opt/vendor__'),  # 等长 13 字符, 清 CI 前缀
    (b'/code_base',    b'/opensrc__'),     # 等长 10 字符, 清 CI 项目根
    (b'/build/vendor', b'/opt/vendor__'),  # 等长 13 字符, 清 cmake prefix-map 产生的第三方前缀
]
with open(p, 'rb') as f:
    data = f.read()
for old, new in mapping:
    assert len(old) == len(new), f'长度不等: {old!r} vs {new!r}'
    n = data.count(old)
    if n:
        data = data.replace(old, new)
        print(f'BIN-P0-04: 替换 {old.decode()} -> {new.decode()}: {n} 处')
with open(p, 'wb') as f:
    f.write(data)
PY
            else
                log_warning "BIN-P0-04: 未找到 python3, 跳过嵌入路径清理"
            fi

            log_info "已复制libflighthubsdk_jni.so到 $lib_platform_dir/"
        elif [ -f "libflighthubsdk_jni.dylib" ]; then
            cp "libflighthubsdk_jni.dylib" "$lib_platform_dir/"
            
            # macOS: 修复主 JNI 库的依赖关系，将 @rpath 改为 @loader_path
            # 辅助函数：将 Framework 路径转换为 dylib 路径
            convert_framework_to_dylib() {
                local dep="$1"
                if echo "$dep" | grep -q "RTCFFmpeg.framework"; then
                    echo "@loader_path/libRTCFFmpeg.dylib"
                elif echo "$dep" | grep -q "VolcEngineRTC.framework"; then
                    echo "@loader_path/libVolcEngineRTC.dylib"
                elif echo "$dep" | grep -q "ByteRTCVp8CodecExtension.framework"; then
                    echo "@loader_path/libbytertc_vp8codec_extension.dylib"
                elif echo "$dep" | grep -q "ByteRTCNICOExtension.framework"; then
                    echo "@loader_path/libbytertc_nico_extension.dylib"
                elif echo "$dep" | grep -q "ByteRTCFFmpegAudioExtension.framework"; then
                    echo "@loader_path/libbytertc_ffmpeg_audio_extension.dylib"
                elif echo "$dep" | grep -q "ByteRTCFDK-AACExtension.framework"; then
                    echo "@loader_path/libbytertc_fdk-aac_extension.dylib"
                else
                    # 通用转换：移除 @rpath，将 Framework 路径转换为 dylib
                    echo "$dep" | sed 's|@rpath/||' | sed 's|\.framework/.*|.dylib|' | sed 's|^|@loader_path/|'
                fi
            }
            
            # 检查是否为 macOS 平台
            if [[ "$platform" == macosx-* ]]; then
                log_info "修复主 JNI 库的依赖关系..."
                # 修改 install_name
                install_name_tool -id "@loader_path/libflighthubsdk_jni.dylib" "$lib_platform_dir/libflighthubsdk_jni.dylib" 2>/dev/null || true
                
                # 修改所有 @rpath 依赖为 @loader_path
                for dep in $(otool -L "$lib_platform_dir/libflighthubsdk_jni.dylib" 2>/dev/null | grep "@rpath" | awk '{print $1}'); do
                    # Java 运行时库，保持 @rpath（Java 会设置正确的 rpath）
                    if echo "$dep" | grep -q "libjawt.dylib\|libjvm.dylib"; then
                        continue
                    fi
                    
                    # 处理 Framework 路径
                    if echo "$dep" | grep -q "\.framework"; then
                        new_dep=$(convert_framework_to_dylib "$dep")
                    else
                        # 其他库：移除 @rpath，使用 @loader_path
                        new_dep=$(echo "$dep" | sed 's|@rpath/||' | sed 's|^|@loader_path/|')
                    fi
                    if [ -n "$new_dep" ]; then
                        install_name_tool -change "$dep" "$new_dep" "$lib_platform_dir/libflighthubsdk_jni.dylib" 2>/dev/null || true
                    fi
                done
                
                # BIN-P0-02: 修复 OpenSSL 绝对路径依赖, 并把 OpenSSL dylib 打进产物目录。
                # 目标: 消除 /opt/homebrew/lib/libssl.3.dylib 等 Homebrew 绝对路径, 使 jar
                # 可在任意 macOS (无 Homebrew) 上加载。方案: 依赖改为 @loader_path/, 且把
                # OpenSSL 实体 dylib cp 到 $lib_platform_dir/, 与 jni.dylib 同目录一起入 jar。
                _openssl_copied=""
                for dep in $(otool -L "$lib_platform_dir/libflighthubsdk_jni.dylib" 2>/dev/null | grep -E "/opt/homebrew|/usr/local" | grep -E "libssl|libcrypto" | awk '{print $1}'); do
                    # 取文件名 (libssl.3.dylib / libcrypto.3.dylib) , 依赖改为 @loader_path/<basename>
                    local dep_base
                    dep_base=$(basename "$dep")
                    install_name_tool -change "$dep" "@loader_path/$dep_base" "$lib_platform_dir/libflighthubsdk_jni.dylib" 2>/dev/null || true
                    log_info "改写 OpenSSL 依赖: $dep -> @loader_path/$dep_base"

                    # 复制实体 dylib 到 lib 目录 (dep 通常是软链, cp -L 跟随到 Cellar 实际文件) ,
                    # 若 dep 路径已经不存在 (只是 install_name) , 尝试直接从 /opt/homebrew/lib 找。
                    if [ -z "$(echo "$_openssl_copied" | grep -w "$dep_base")" ]; then
                        local src=""
                        if [ -e "$dep" ]; then
                            src="$dep"
                        elif [ -e "/opt/homebrew/lib/$dep_base" ]; then
                            src="/opt/homebrew/lib/$dep_base"
                        elif [ -e "/usr/local/lib/$dep_base" ]; then
                            src="/usr/local/lib/$dep_base"
                        fi
                        if [ -n "$src" ]; then
                            cp -L "$src" "$lib_platform_dir/$dep_base"
                            chmod +w "$lib_platform_dir/$dep_base" 2>/dev/null || true
                            # 把 OpenSSL 自身的 LC_ID_DYLIB 也改成 @loader_path, 保持自洽。
                            install_name_tool -id "@loader_path/$dep_base" "$lib_platform_dir/$dep_base" 2>/dev/null || true
                            # OpenSSL 之间可能相互依赖 (libssl → libcrypto) , 把这些依赖也改为 @loader_path。
                            for inner in $(otool -L "$lib_platform_dir/$dep_base" 2>/dev/null | tail -n +2 | grep -E "/opt/homebrew|/usr/local" | grep -E "libssl|libcrypto" | awk '{print $1}'); do
                                local inner_base
                                inner_base=$(basename "$inner")
                                install_name_tool -change "$inner" "@loader_path/$inner_base" "$lib_platform_dir/$dep_base" 2>/dev/null || true
                            done
                            codesign --force --sign - "$lib_platform_dir/$dep_base" 2>/dev/null || true
                            _openssl_copied="$_openssl_copied $dep_base"
                            log_info "已随包 OpenSSL: $dep_base (源: $src)"
                        else
                            log_warning "未找到 $dep_base 的实体, 请确保运行机上安装了 openssl@3"
                        fi
                    fi
                done
                
                # 重新签名（使用 adhoc 签名）
                codesign --force --sign - "$lib_platform_dir/libflighthubsdk_jni.dylib" 2>/dev/null || true
            fi
            
            # Debug构建时不strip，保留调试信息
            if [ "$build_type" != "Debug" ]; then
                strip "$lib_platform_dir/libflighthubsdk_jni.dylib"
                # strip 后需要重新签名（macOS）
                if [[ "$platform" == macosx-* ]]; then
                    codesign --force --sign - "$lib_platform_dir/libflighthubsdk_jni.dylib" 2>/dev/null || true
                fi
            fi
            log_info "已复制libflighthubsdk_jni.dylib到 $lib_platform_dir/"
        else
            log_warning "未找到JNI库文件，请检查构建是否成功"
            log_info "当前目录文件列表:"
            ls -la *.so *.dylib 2>/dev/null || log_info "  没有找到库文件"
        fi

        # 根据平台复制相应的第三方库
        local platform=$(get_platform)
        case "$platform" in
            linux-x86_64)
                # Linux x86_64 平台
                if [ -f "$THIRD_PARTIES_DIR/paho_mqtt_c/lib/linux/x86_64/libpaho-mqtt3as.so.1" ]; then
                    cp "$THIRD_PARTIES_DIR/paho_mqtt_c/lib/linux/x86_64/libpaho-mqtt3as.so.1" "$lib_platform_dir/"
                    strip "$lib_platform_dir/libpaho-mqtt3as.so.1" || true
                    log_info "已复制libpaho-mqtt3as.so.1到 $lib_platform_dir/"
                fi
                
                if [ -f "$THIRD_PARTIES_DIR/openssl_1_1_1/lib/linux/x86_64/libcrypto.so.1.1" ]; then
                    cp "$THIRD_PARTIES_DIR/openssl_1_1_1/lib/linux/x86_64/libcrypto.so.1.1" "$lib_platform_dir/"
                    strip "$lib_platform_dir/libcrypto.so.1.1" || true
                    log_info "已复制libcrypto.so.1.1到 $lib_platform_dir/"
                else
                    log_warning "缺少libcrypto.so.1.1: $THIRD_PARTIES_DIR/openssl_1_1_1/lib/linux/x86_64/libcrypto.so.1.1"
                fi
                
                if [ -f "$THIRD_PARTIES_DIR/openssl_1_1_1/lib/linux/x86_64/libssl.so.1.1" ]; then
                    cp "$THIRD_PARTIES_DIR/openssl_1_1_1/lib/linux/x86_64/libssl.so.1.1" "$lib_platform_dir/"
                    strip "$lib_platform_dir/libssl.so.1.1" || true
                    log_info "已复制libssl.so.1.1到 $lib_platform_dir/"
                else
                    log_warning "缺少libssl.so.1.1: $THIRD_PARTIES_DIR/openssl_1_1_1/lib/linux/x86_64/libssl.so.1.1"
                fi
                
                if [ -f "$THIRD_PARTIES_DIR/websockets/lib/linux/x86_64/libwebsockets.so.12" ]; then
                    cp "$THIRD_PARTIES_DIR/websockets/lib/linux/x86_64/libwebsockets.so.12" "$lib_platform_dir/"
                    strip "$lib_platform_dir/libwebsockets.so.12" || true
                    log_info "已复制libwebsockets.so.12到 $lib_platform_dir/"
                else
                    log_warning "缺少libwebsockets.so.12: $THIRD_PARTIES_DIR/websockets/lib/linux/x86_64/libwebsockets.so.12"
                fi
                
                # 火山引擎 RTC SDK 动态库 (Linux)
                # C-01 (ES320 v3.6): Volc RTC 已在 CMake 关闭 (BUILD_WITH_VOLC_USER_FULL=OFF);
                # 本段保留代码结构以便未来重启, 打包时通过 gate 跳过. 恢复: 把 `false` 改回 `[ -d "$volc_lib_dir" ]`.
                local volc_lib_dir="$THIRD_PARTIES_DIR/volc_engine_rtc_full/lib/linux-x86_64"
                if false; then
                    log_info "复制火山引擎 RTC SDK 动态库 (Linux)..."
                    if [ -f "$volc_lib_dir/libVolcEngineRTC.so" ]; then
                        cp "$volc_lib_dir/libVolcEngineRTC.so" "$lib_platform_dir/"
                        strip "$lib_platform_dir/libVolcEngineRTC.so" || true
                        log_info "已复制libVolcEngineRTC.so到 $lib_platform_dir/"
                    fi
                    if [ -f "$volc_lib_dir/libRTCFFmpeg.so" ]; then
                        cp "$volc_lib_dir/libRTCFFmpeg.so" "$lib_platform_dir/"
                        strip "$lib_platform_dir/libRTCFFmpeg.so" || true
                        log_info "已复制libRTCFFmpeg.so到 $lib_platform_dir/"
                    fi
                    if [ -f "$volc_lib_dir/libbytertc_vp8codec_extension.so" ]; then
                        cp "$volc_lib_dir/libbytertc_vp8codec_extension.so" "$lib_platform_dir/"
                        strip "$lib_platform_dir/libbytertc_vp8codec_extension.so" || true
                        log_info "已复制libbytertc_vp8codec_extension.so到 $lib_platform_dir/"
                    fi
                    if [ -f "$volc_lib_dir/libbytertc_nico_extension.so" ]; then
                        cp "$volc_lib_dir/libbytertc_nico_extension.so" "$lib_platform_dir/"
                        strip "$lib_platform_dir/libbytertc_nico_extension.so" || true
                        log_info "已复制libbytertc_nico_extension.so到 $lib_platform_dir/"
                    fi
                    if [ -f "$volc_lib_dir/libbytertc_ffmpeg_audio_extension.so" ]; then
                        cp "$volc_lib_dir/libbytertc_ffmpeg_audio_extension.so" "$lib_platform_dir/"
                        strip "$lib_platform_dir/libbytertc_ffmpeg_audio_extension.so" || true
                        log_info "已复制libbytertc_ffmpeg_audio_extension.so到 $lib_platform_dir/"
                    fi
                    if [ -f "$volc_lib_dir/libbytertc_fdk-aac_extension.so" ]; then
                        cp "$volc_lib_dir/libbytertc_fdk-aac_extension.so" "$lib_platform_dir/"
                        strip "$lib_platform_dir/libbytertc_fdk-aac_extension.so" || true
                        log_info "已复制libbytertc_fdk-aac_extension.so到 $lib_platform_dir/"
                    fi
                    if [ -f "$volc_lib_dir/libbytenn.so" ]; then
                        cp "$volc_lib_dir/libbytenn.so" "$lib_platform_dir/"
                        strip "$lib_platform_dir/libbytenn.so" || true
                        log_info "已复制libbytenn.so到 $lib_platform_dir/"
                    fi
                else
                    log_info "Volc RTC 已在 CMake 关闭 (BUILD_WITH_VOLC_USER_FULL=OFF), 跳过 native so 复制"
                fi

                # 声网 Agora RTC SDK 动态库 (Linux x86_64)
                # C-01: Agora RTC 已在 CMake 关闭 (BUILD_WITH_AGORA_USER=OFF); 恢复方式同上.
                local agora_lib_dir="$THIRD_PARTIES_DIR/agora/linux/x86_64/agora_sdk"
                if false; then
                    log_info "复制声网 Agora RTC SDK 动态库 (Linux x86_64)..."
                    if [ -f "$agora_lib_dir/libaosl.so" ]; then
                        cp "$agora_lib_dir/libaosl.so" "$lib_platform_dir/"
                        strip "$lib_platform_dir/libaosl.so" || true
                        log_info "已复制libaosl.so到 $lib_platform_dir/"
                    fi
                    if [ -f "$agora_lib_dir/libagora-fdkaac.so" ]; then
                        cp "$agora_lib_dir/libagora-fdkaac.so" "$lib_platform_dir/"
                        strip "$lib_platform_dir/libagora-fdkaac.so" || true
                        log_info "已复制libagora-fdkaac.so到 $lib_platform_dir/"
                    fi
                    if [ -f "$agora_lib_dir/libagora_rtc_sdk.so" ]; then
                        cp "$agora_lib_dir/libagora_rtc_sdk.so" "$lib_platform_dir/"
                        strip "$lib_platform_dir/libagora_rtc_sdk.so" || true
                        log_info "已复制libagora_rtc_sdk.so到 $lib_platform_dir/"
                    fi
                    
                    # 为声网库设置 RPATH，使其能找到依赖的库
                    if command -v patchelf >/dev/null 2>&1; then
                        log_info "为声网 Agora RTC SDK 动态库设置 RPATH..."
                        if [ -f "$lib_platform_dir/libaosl.so" ]; then
                            patchelf --set-rpath '$ORIGIN' "$lib_platform_dir/libaosl.so" 2>/dev/null || true
                            log_info "已设置libaosl.so的RPATH"
                        fi
                        if [ -f "$lib_platform_dir/libagora-fdkaac.so" ]; then
                            patchelf --set-rpath '$ORIGIN' "$lib_platform_dir/libagora-fdkaac.so" 2>/dev/null || true
                            log_info "已设置libagora-fdkaac.so的RPATH"
                        fi
                        if [ -f "$lib_platform_dir/libagora_rtc_sdk.so" ]; then
                            patchelf --set-rpath '$ORIGIN' "$lib_platform_dir/libagora_rtc_sdk.so" 2>/dev/null || true
                            log_info "已设置libagora_rtc_sdk.so的RPATH"
                        fi
                    else
                        log_warning "patchelf 未安装，无法设置 RPATH。可能需要设置 LD_LIBRARY_PATH"
                    fi
                else
                    log_info "Agora RTC 已在 CMake 关闭 (BUILD_WITH_AGORA_USER=OFF), 跳过 native so 复制"
                fi

                # DRTC user SDK 动态库 (Linux x86_64)
                local drtc_lib_dir="$THIRD_PARTIES_DIR/libdrtc/lib/linux/x86_64"
                if [ -d "$drtc_lib_dir" ]; then
                    log_info "复制 DRTC user SDK 动态库 (Linux x86_64)..."
                    if [ -f "$drtc_lib_dir/libdrpc.so" ]; then
                        cp "$drtc_lib_dir/libdrpc.so" "$lib_platform_dir/"
                        strip "$lib_platform_dir/libdrpc.so" || true
                        log_info "已复制libdrpc.so到 $lib_platform_dir/"
                    fi
                    if [ -f "$drtc_lib_dir/libdrtc_device_sdk.so" ]; then
                        cp "$drtc_lib_dir/libdrtc_device_sdk.so" "$lib_platform_dir/"
                        strip "$lib_platform_dir/libdrtc_device_sdk.so" || true
                        log_info "已复制libdrtc_device_sdk.so到 $lib_platform_dir/"
                    fi

                    # libdrtc_device_sdk.so 依赖 libdrpc.so；须设 $ORIGIN
                    if command -v patchelf >/dev/null 2>&1; then
                        log_info "为 DRTC user SDK 动态库设置 RPATH..."
                        if [ -f "$lib_platform_dir/libdrpc.so" ]; then
                            patchelf --set-rpath '$ORIGIN' "$lib_platform_dir/libdrpc.so" 2>/dev/null || true
                            log_info "已设置libdrpc.so的RPATH"
                        fi
                        if [ -f "$lib_platform_dir/libdrtc_device_sdk.so" ]; then
                            patchelf --set-rpath '$ORIGIN' "$lib_platform_dir/libdrtc_device_sdk.so" 2>/dev/null || true
                            log_info "已设置libdrtc_device_sdk.so的RPATH"
                        fi
                    else
                        log_warning "patchelf 未安装，无法设置 RPATH。可能需要设置 LD_LIBRARY_PATH"
                    fi
                else
                    log_warning "未找到 DRTC SDK 动态库目录: $drtc_lib_dir"
                fi
                ;;
        macosx-arm64)
            # macOS ARM64 平台
            # macOS 使用系统 OpenSSL，不需要复制 OpenSSL 库
            if [ -f "$THIRD_PARTIES_DIR/paho_mqtt_c/lib/macosx/arm64/libpaho-mqtt3as.1.dylib" ]; then
                cp "$THIRD_PARTIES_DIR/paho_mqtt_c/lib/macosx/arm64/libpaho-mqtt3as.1.dylib" "$lib_platform_dir/"
                strip "$lib_platform_dir/libpaho-mqtt3as.1.dylib" || true
                log_info "已复制libpaho-mqtt3as.1.dylib到 $lib_platform_dir/"
            fi
            
            if [ -f "$THIRD_PARTIES_DIR/websockets/lib/macosx/arm64/libwebsockets.12.dylib" ]; then
                cp "$THIRD_PARTIES_DIR/websockets/lib/macosx/arm64/libwebsockets.12.dylib" "$lib_platform_dir/"
                strip "$lib_platform_dir/libwebsockets.12.dylib" || true
                log_info "已复制libwebsockets.12.dylib到 $lib_platform_dir/"
            fi

            # BIN-P0-02 续: 仓内 paho/lws 是 @rpath/libssl.3.dylib（给 C++ 用）。
            # Java 解到临时目录后没有 LC_RPATH，@rpath 会 dlopen 失败。打 jar 改成 @loader_path。
            for _lib in "$lib_platform_dir"/libpaho-mqtt3as*.dylib "$lib_platform_dir"/libwebsockets*.dylib; do
                [ -f "$_lib" ] || continue
                install_name_tool -id "@loader_path/$(basename "$_lib")" "$_lib" 2>/dev/null || true
                for dep in $(otool -L "$_lib" 2>/dev/null | awk '/libssl|libcrypto/ {print $1}'); do
                    case "$dep" in
                        @loader_path/*|/usr/lib/*) continue ;;
                    esac
                    dep_base=$(basename "$dep")
                    install_name_tool -change "$dep" "@loader_path/$dep_base" "$_lib" 2>/dev/null || true
                    log_info "改写 $(basename "$_lib") OpenSSL 依赖: $dep -> @loader_path/$dep_base"
                done
                codesign --force --sign - "$_lib" 2>/dev/null || true
            done
            
            # 火山引擎 RTC SDK (macOS ARM64)
            # C-01: Volc RTC 已关闭; 恢复方式同上.
            # 从 Framework 中提取二进制文件，创建 .dylib 供 Java 加载
            # 注意：不需要复制整个 Framework 目录，只需要提取 dylib 文件即可
            local volc_lib_dir="$THIRD_PARTIES_DIR/volc_engine_rtc_full/lib/macosx/arm64"
            if false; then
                log_info "从 Framework 中提取火山引擎 RTC SDK dylib (macOS ARM64)..."
                
                # 从 Framework 中提取二进制文件，创建 .dylib 供 Java 加载
                # Java 的 JniLibraryLoader 期望 .dylib 文件，但 Framework 使用 @rpath
                # 需要修改 install_name，将 @rpath 改为 @loader_path，以便在临时目录中能找到依赖
                # 注意：需要先复制所有库，再修改依赖关系，因为修改依赖时需要目标库已存在
                
                # 先复制所有二进制文件
                if [ -f "$volc_lib_dir/VolcEngineRTC.framework/Versions/3/VolcEngineRTC" ]; then
                    # 使用 cp -L 来复制实际文件，而不是符号链接
                    cp -L "$volc_lib_dir/VolcEngineRTC.framework/Versions/3/VolcEngineRTC" "$lib_platform_dir/libVolcEngineRTC.dylib"
                fi
                if [ -f "$volc_lib_dir/RTCFFmpeg.framework/Versions/A/RTCFFmpeg" ]; then
                    # 使用 cp -L 来复制实际文件，而不是符号链接
                    cp -L "$volc_lib_dir/RTCFFmpeg.framework/Versions/A/RTCFFmpeg" "$lib_platform_dir/libRTCFFmpeg.dylib"
                fi
                if [ -f "$volc_lib_dir/ByteRTCVp8CodecExtension.framework/Versions/A/ByteRTCVp8CodecExtension" ]; then
                    # 使用 cp -L 来复制实际文件，而不是符号链接
                    cp -L "$volc_lib_dir/ByteRTCVp8CodecExtension.framework/Versions/A/ByteRTCVp8CodecExtension" "$lib_platform_dir/libbytertc_vp8codec_extension.dylib"
                fi
                if [ -f "$volc_lib_dir/ByteRTCNICOExtension.framework/Versions/A/ByteRTCNICOExtension" ]; then
                    cp -L "$volc_lib_dir/ByteRTCNICOExtension.framework/Versions/A/ByteRTCNICOExtension" "$lib_platform_dir/libbytertc_nico_extension.dylib"
                fi
                if [ -f "$volc_lib_dir/ByteRTCFFmpegAudioExtension.framework/Versions/A/ByteRTCFFmpegAudioExtension" ]; then
                    cp -L "$volc_lib_dir/ByteRTCFFmpegAudioExtension.framework/Versions/A/ByteRTCFFmpegAudioExtension" "$lib_platform_dir/libbytertc_ffmpeg_audio_extension.dylib"
                fi
                if [ -f "$volc_lib_dir/ByteRTCFDK-AACExtension.framework/Versions/A/ByteRTCFDK-AACExtension" ]; then
                    cp -L "$volc_lib_dir/ByteRTCFDK-AACExtension.framework/Versions/A/ByteRTCFDK-AACExtension" "$lib_platform_dir/libbytertc_fdk-aac_extension.dylib"
                fi
                
                # 然后修改所有库的 install_name 和依赖路径
                if [ -f "$lib_platform_dir/libVolcEngineRTC.dylib" ]; then
                    # 先修改 install_name 和依赖关系
                    install_name_tool -id "@loader_path/libVolcEngineRTC.dylib" "$lib_platform_dir/libVolcEngineRTC.dylib" 2>/dev/null || true
                    install_name_tool -change "@rpath/VolcEngineRTC.framework/Versions/3/VolcEngineRTC" "@loader_path/libVolcEngineRTC.dylib" "$lib_platform_dir/libVolcEngineRTC.dylib" 2>/dev/null || true
                    install_name_tool -change "@rpath/libbytenn.dylib" "@loader_path/libbytenn.dylib" "$lib_platform_dir/libVolcEngineRTC.dylib" 2>/dev/null || true
                    install_name_tool -change "@rpath/RTCFFmpeg.framework/RTCFFmpeg" "@loader_path/libRTCFFmpeg.dylib" "$lib_platform_dir/libVolcEngineRTC.dylib" 2>/dev/null || true
                    install_name_tool -change "@rpath/libbytevc0.dylib" "@loader_path/libbytevc0.dylib" "$lib_platform_dir/libVolcEngineRTC.dylib" 2>/dev/null || true
                    # 重新签名（使用 adhoc 签名）
                    codesign --force --sign - "$lib_platform_dir/libVolcEngineRTC.dylib" 2>/dev/null || true
                    if [ "$build_type" != "Debug" ]; then
                        strip "$lib_platform_dir/libVolcEngineRTC.dylib" || true
                        # strip 后需要重新签名
                        codesign --force --sign - "$lib_platform_dir/libVolcEngineRTC.dylib" 2>/dev/null || true
                    fi
                    log_info "已创建并修改libVolcEngineRTC.dylib到 $lib_platform_dir/"
                fi
                # 辅助函数：将 Framework 路径转换为 dylib 路径
                # RTCFFmpeg.framework/RTCFFmpeg -> libRTCFFmpeg.dylib
                # VolcEngineRTC.framework/Versions/3/VolcEngineRTC -> libVolcEngineRTC.dylib
                # ByteRTC*.framework/... -> libbytertc_*.dylib
                convert_framework_to_dylib() {
                    local dep="$1"
                    if echo "$dep" | grep -q "RTCFFmpeg.framework"; then
                        echo "@loader_path/libRTCFFmpeg.dylib"
                    elif echo "$dep" | grep -q "VolcEngineRTC.framework"; then
                        echo "@loader_path/libVolcEngineRTC.dylib"
                    elif echo "$dep" | grep -q "ByteRTCVp8CodecExtension.framework"; then
                        echo "@loader_path/libbytertc_vp8codec_extension.dylib"
                    elif echo "$dep" | grep -q "ByteRTCNICOExtension.framework"; then
                        echo "@loader_path/libbytertc_nico_extension.dylib"
                    elif echo "$dep" | grep -q "ByteRTCFFmpegAudioExtension.framework"; then
                        echo "@loader_path/libbytertc_ffmpeg_audio_extension.dylib"
                    elif echo "$dep" | grep -q "ByteRTCFDK-AACExtension.framework"; then
                        echo "@loader_path/libbytertc_fdk-aac_extension.dylib"
                    else
                        # 通用转换：移除 @rpath，将 Framework 路径转换为 dylib
                        echo "$dep" | sed 's|@rpath/||' | sed 's|\.framework/.*|.dylib|' | sed 's|^|@loader_path/|'
                    fi
                }
                
                if [ -f "$lib_platform_dir/libRTCFFmpeg.dylib" ]; then
                    # 先修改 install_name 和依赖关系（修改后签名会失效，但先修改可以保留更多信息）
                    install_name_tool -id "@loader_path/libRTCFFmpeg.dylib" "$lib_platform_dir/libRTCFFmpeg.dylib" 2>/dev/null || true
                    # 检查并修改 RTCFFmpeg 的依赖
                    for dep in $(otool -L "$lib_platform_dir/libRTCFFmpeg.dylib" 2>/dev/null | grep "@rpath" | awk '{print $1}'); do
                        new_dep=$(convert_framework_to_dylib "$dep")
                        install_name_tool -change "$dep" "$new_dep" "$lib_platform_dir/libRTCFFmpeg.dylib" 2>/dev/null || true
                    done
                    # 重新签名（使用 adhoc 签名，允许修改后的库加载）
                    codesign --force --sign - "$lib_platform_dir/libRTCFFmpeg.dylib" 2>/dev/null || true
                    if [ "$build_type" != "Debug" ]; then
                        strip "$lib_platform_dir/libRTCFFmpeg.dylib" || true
                        # strip 后需要重新签名
                        codesign --force --sign - "$lib_platform_dir/libRTCFFmpeg.dylib" 2>/dev/null || true
                    fi
                    log_info "已创建并修改libRTCFFmpeg.dylib到 $lib_platform_dir/"
                fi
                # 修复扩展库的依赖关系
                for ext_lib in libbytertc_vp8codec_extension.dylib libbytertc_nico_extension.dylib libbytertc_ffmpeg_audio_extension.dylib libbytertc_fdk-aac_extension.dylib; do
                    if [ -f "$lib_platform_dir/$ext_lib" ]; then
                        # 先修改 install_name 和依赖关系
                        install_name_tool -id "@loader_path/$ext_lib" "$lib_platform_dir/$ext_lib" 2>/dev/null || true
                        # 修改所有 @rpath 依赖为 @loader_path
                        for dep in $(otool -L "$lib_platform_dir/$ext_lib" 2>/dev/null | grep "@rpath" | awk '{print $1}'); do
                            new_dep=$(convert_framework_to_dylib "$dep")
                            install_name_tool -change "$dep" "$new_dep" "$lib_platform_dir/$ext_lib" 2>/dev/null || true
                        done
                        # 修复已转换但路径错误的依赖（RTCFFmpeg.dylib -> libRTCFFmpeg.dylib）
                        install_name_tool -change "@loader_path/RTCFFmpeg.dylib" "@loader_path/libRTCFFmpeg.dylib" "$lib_platform_dir/$ext_lib" 2>/dev/null || true
                        # 重新签名（使用 adhoc 签名）
                        codesign --force --sign - "$lib_platform_dir/$ext_lib" 2>/dev/null || true
                        if [ "$build_type" != "Debug" ]; then
                            strip "$lib_platform_dir/$ext_lib" || true
                            # strip 后需要重新签名
                            codesign --force --sign - "$lib_platform_dir/$ext_lib" 2>/dev/null || true
                        fi
                        log_info "已创建并修改$ext_lib到 $lib_platform_dir/"
                    fi
                done
                
                # 修改 libbytenn.dylib 的 install_name（如果它也有 @rpath 依赖）
                if [ -f "$lib_platform_dir/libbytenn.dylib" ]; then
                    install_name_tool -id "@loader_path/libbytenn.dylib" "$lib_platform_dir/libbytenn.dylib" 2>/dev/null || true
                    for dep in $(otool -L "$lib_platform_dir/libbytenn.dylib" 2>/dev/null | grep "@rpath" | awk '{print $1}'); do
                        new_dep=$(echo "$dep" | sed 's|@rpath/||' | sed 's|^|@loader_path/|')
                        install_name_tool -change "$dep" "$new_dep" "$lib_platform_dir/libbytenn.dylib" 2>/dev/null || true
                    done
                fi
                
                # 修改其他 dylib 的 install_name
                for dylib in libbytevc0.dylib libbdaudioeffect.dylib libeffect.dylib; do
                    if [ -f "$lib_platform_dir/$dylib" ]; then
                        install_name_tool -id "@loader_path/$dylib" "$lib_platform_dir/$dylib" 2>/dev/null || true
                        for dep in $(otool -L "$lib_platform_dir/$dylib" 2>/dev/null | grep "@rpath" | awk '{print $1}'); do
                            new_dep=$(echo "$dep" | sed 's|@rpath/||' | sed 's|^|@loader_path/|')
                            install_name_tool -change "$dep" "$new_dep" "$lib_platform_dir/$dylib" 2>/dev/null || true
                        done
                    fi
                done
                
                # 复制独立的 dylib 文件
                if [ -f "$volc_lib_dir/libbytenn.dylib" ]; then
                    cp "$volc_lib_dir/libbytenn.dylib" "$lib_platform_dir/"
                    strip "$lib_platform_dir/libbytenn.dylib" || true
                    log_info "已复制libbytenn.dylib到 $lib_platform_dir/"
                fi
                if [ -f "$volc_lib_dir/libbdaudioeffect.dylib" ]; then
                    cp "$volc_lib_dir/libbdaudioeffect.dylib" "$lib_platform_dir/"
                    strip "$lib_platform_dir/libbdaudioeffect.dylib" || true
                    log_info "已复制libbdaudioeffect.dylib到 $lib_platform_dir/"
                fi
                if [ -f "$volc_lib_dir/libbytevc0.dylib" ]; then
                    cp "$volc_lib_dir/libbytevc0.dylib" "$lib_platform_dir/"
                    strip "$lib_platform_dir/libbytevc0.dylib" || true
                    log_info "已复制libbytevc0.dylib到 $lib_platform_dir/"
                fi
                if [ -f "$volc_lib_dir/libeffect.dylib" ]; then
                    cp "$volc_lib_dir/libeffect.dylib" "$lib_platform_dir/"
                    strip "$lib_platform_dir/libeffect.dylib" || true
                    log_info "已复制libeffect.dylib到 $lib_platform_dir/"
                fi
            else
                log_warning "未找到火山引擎 RTC SDK 目录: $volc_lib_dir"
            fi
            ;;
            *)
                log_warning "未知平台: $platform，跳过第三方库复制"
                ;;
        esac

        # BUILD-01 止血: 清除随包 .so 内嵌的构建机路径(等长二进制替换, 不改段布局)。
        # 残留来源: libdrtc 上游预编译流水线未加 -ffile-prefix-map(/build/vendor
        # 共 241 处 + libcrypto 残留 /home/jenkins 1 处 + PPE 库 /home/jenkins 3 处)。
        # 上游根治已转 4.3 登记, 仓库侧以本清理冻结恶化; 发布后以 strings 复核应归零。
        # 扫描 lib_platform_dir 下所有 .so（含 ppe/ 子目录），避免新增库遗漏。
        if [ "$platform" = "linux-x86_64" ] && command -v python3 >/dev/null 2>&1; then
            python3 - "$lib_platform_dir" <<'PY'
import os, sys, glob
lib_dir = sys.argv[1]
mapping = [
    (b'/build/vendor', b'/opt/vendor__'),  # 等长 13 字符, 上游流水线前缀
    (b'/home/jenkins', b'/opt/vendor__'),  # 等长 13 字符, libcrypto/PPE 残留
    (b'/code_base',    b'/opensrc__'),     # 等长 10 字符
]
so_files = glob.glob(os.path.join(lib_dir, '*.so*')) + \
           glob.glob(os.path.join(lib_dir, '**', '*.so*'), recursive=True)
for p in sorted(set(so_files)):
    if not os.path.isfile(p) or os.path.islink(p):
        continue
    name = os.path.relpath(p, lib_dir)
    with open(p, 'rb') as f:
        data = f.read()
    size_before = len(data)
    changed = False
    for old, new in mapping:
        assert len(old) == len(new), f'长度不等: {old!r} vs {new!r}'
        n = data.count(old)
        if n:
            data = data.replace(old, new)
            print(f'BUILD-01: {name}: {old.decode()} -> {new.decode()}: {n} 处')
            changed = True
    assert len(data) == size_before, f'{name}: 替换后长度变化!'
    if changed:
        with open(p, 'wb') as f:
            f.write(data)
PY
        fi
    else
        log_error "C++库构建失败 ($build_type)"
        return 1
    fi
    
    cd "$PLATFORM_JAVA_DIR"
}

# 构建Java SDK
build_java_sdk() {
    log_info "构建Java SDK..."
    
    cd "$PLATFORM_JAVA_DIR"
    
    # 构建前清理源码目录中的class文件
    clean_source_class_files
    
    # 构建SDK项目
    log_info "构建SDK项目..."
    ./gradlew :flight_hub_sdk:clean :flight_hub_sdk:build :flight_hub_sdk:publishToMavenLocal \
        --max-workers="$(fhsdk_resolve_jobs)"
    
    if [ $? -eq 0 ]; then
        log_success "Java SDK构建成功"
        
        # 复制SDK JAR到dist目录
        mkdir -p "$DIST_DIR"
        cp flight_hub_sdk/build/libs/flight-hub-sdk-*.jar "$DIST_DIR/flight-hub-sdk.jar" 2>/dev/null || true
        
        log_info "SDK JAR文件已复制到 $DIST_DIR/"
        ls -la "$DIST_DIR/"
        
        # 构建完成后清理源码目录中的class文件
        clean_source_class_files
    else
        log_error "SDK项目构建失败"
        return 1
    fi
}

# 显示帮助信息
show_help() {
    echo "Flight Hub Java SDK 构建脚本"
    echo ""
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  -h, --help          显示此帮助信息"
    echo "  -c, --clean         只清理构建目录"
    echo "  --clean-class       只清理源码目录中的class文件"
    echo "  -d, --debug         构建Debug版本"
    echo "  -r, --release       构建Release版本"
    echo "  -a, --all           构建Debug和Release版本"
    echo "  --swig-only         显示SWIG代码生成说明（现由CMake自动处理）"
    echo "  --cpp-only          只构建C++库"
    echo "  --java-only         只构建Java SDK"
    echo ""
    echo "示例:"
    echo "  $0 --clean          # 清理构建目录"
    echo "  $0 --clean-class    # 清理源码目录中的class文件"
    echo "  $0 --debug          # 构建Debug版本"
    echo "  $0 --release        # 构建Release版本"
    echo "  $0 --all            # 构建所有版本"
}

# 主函数
main() {
    local build_debug=false
    local build_release=false
    local clean_only=false
    local swig_only=false
    local cpp_only=false
    local java_only=false
    
    # 解析命令行参数
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_help
                exit 0
                ;;
            -c|--clean)
                clean_only=true
                shift
                ;;
            --clean-class)
                clean_source_class_files
                log_success "源码目录中的class文件已清理"
                exit 0
                ;;
            -d|--debug)
                build_debug=true
                shift
                ;;
            -r|--release)
                build_release=true
                shift
                ;;
            -a|--all)
                build_debug=true
                build_release=true
                shift
                ;;
            --swig-only)
                swig_only=true
                shift
                ;;
            --cpp-only)
                cpp_only=true
                shift
                ;;
            --java-only)
                java_only=true
                shift
                ;;
            *)
                log_error "未知选项: $1"
                show_help
                exit 1
                ;;
        esac
    done
    
    # 如果没有指定任何构建选项，显示帮助信息
    if [ "$build_debug" = false ] && [ "$build_release" = false ] && \
       [ "$clean_only" = false ] && [ "$swig_only" = false ] && \
       [ "$cpp_only" = false ] && [ "$java_only" = false ]; then
        log_warning "未指定任何构建选项"
        echo ""
        show_help
        exit 0
    fi
    
    log_info "开始构建Flight Hub Java SDK..."
    log_info "平台: $(get_platform)"
    
    # 检查依赖
    if [ "$clean_only" = false ]; then
        check_dependencies || exit 1
        check_third_party_libs
    fi
    
    # 执行清理
    if [ "$clean_only" = true ]; then
        clean_build
        exit 0
    fi
    
    # SWIG包装代码现在由CMake自动生成，无需手动调用
    # 注释掉手动SWIG生成，改为由CMake在构建C++库时自动处理
    # if [ "$cpp_only" = false ] && [ "$java_only" = false ]; then
    #     generate_swig_wrapper || exit 1
    # fi
    
    if [ "$swig_only" = true ]; then
        log_info "SWIG包装代码将由CMake在构建C++库时自动生成"
        log_info "请使用 --cpp-only 选项来生成SWIG包装代码"
        exit 0
    fi
    
    # 构建C++库
    if [ "$java_only" = false ]; then
        if [ "$build_debug" = true ]; then
            build_cpp_library "Debug" || exit 1
        fi
        
        if [ "$build_release" = true ]; then
            build_cpp_library "Release" || exit 1
        fi
    fi
    
    if [ "$cpp_only" = true ]; then
        log_success "C++库构建完成"
        exit 0
    fi
    
    # 构建Java SDK
    if [ "$cpp_only" = false ]; then
        build_java_sdk || exit 1
    fi
    
    log_success "Flight Hub Java SDK构建完成！"
    log_info "构建产物位置:"
    log_info "  - JNI库: $LIB_DIR/"
    log_info "  - JAR包: $DIST_DIR/"
}

# 运行主函数
main "$@"