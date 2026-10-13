#!/bin/bash
# Java 进程资源监控脚本
# 用法: ./monitor_java_process.sh <PID>

PID=${1:-32724}

if [ ! -d "/proc/$PID" ]; then
    echo "错误: 进程 $PID 不存在"
    exit 1
fi

echo "=========================================="
echo "Java 进程资源监控报告 (PID: $PID)"
echo "时间: $(date '+%Y-%m-%d %H:%M:%S')"
echo "=========================================="
echo ""

# 1. 基本信息
echo "【1. 进程基本信息】"
ps -p $PID -o pid,ppid,user,%mem,%cpu,rss,vsz,etime,cmd --no-headers | awk '{
    printf "  PID: %s\n", $1
    printf "  PPID: %s\n", $2
    printf "  用户: %s\n", $3
    printf "  内存占用: %s%%\n", $4
    printf "  CPU占用: %s%%\n", $5
    printf "  物理内存(RSS): %s KB (%.2f MB)\n", $6, $6/1024
    printf "  虚拟内存(VSZ): %s KB (%.2f MB)\n", $7, $7/1024
    printf "  运行时间: %s\n", $8
}'
echo ""

# 2. 详细状态信息
echo "【2. 内存详细状态】"
cat /proc/$PID/status | grep -E "(VmSize|VmRSS|VmPeak|VmHWM|VmData|VmStk|VmExe|VmLib)" | awk '{
    key = $1
    value = $2
    unit = $3
    value_mb = value / 1024
    printf "  %-15s: %10s %-3s (%.2f MB)\n", key, value, unit, value_mb
}'
echo ""

# 3. 文件描述符
echo "【3. 文件描述符 (FD)】"
FD_COUNT=$(ls -1 /proc/$PID/fd 2>/dev/null | wc -l)
FD_LIMIT=$(cat /proc/$PID/limits 2>/dev/null | grep "Max open files" | awk '{print $4}')
FD_SIZE=$(cat /proc/$PID/status 2>/dev/null | grep "FDSize" | awk '{print $2}')
if [ "$FD_LIMIT" != "unlimited" ] && [ ! -z "$FD_LIMIT" ]; then
    FD_USAGE=$(awk "BEGIN {printf \"%.2f\", $FD_COUNT*100/$FD_LIMIT}")
else
    FD_USAGE="N/A"
fi

echo "  当前打开的文件描述符数: $FD_COUNT"
echo "  FD 数组大小: $FD_SIZE"
echo "  系统限制: $FD_LIMIT"
if [ "$FD_USAGE" != "N/A" ]; then
    echo "  使用率: ${FD_USAGE}%"
fi

# 统计 FD 类型
echo ""
echo "  FD 类型统计:"
SOCKET_COUNT=$(ls -l /proc/$PID/fd 2>/dev/null | grep -c "socket")
PIPE_COUNT=$(ls -l /proc/$PID/fd 2>/dev/null | grep -c "pipe")
ANON_COUNT=$(ls -l /proc/$PID/fd 2>/dev/null | grep -c "anon_inode")
FILE_COUNT=$(ls -l /proc/$PID/fd 2>/dev/null | grep -c "  -> /" | head -1)
echo "    套接字(socket): $SOCKET_COUNT"
echo "    管道(pipe): $PIPE_COUNT"
echo "    匿名inode: $ANON_COUNT"
echo "    文件: $((FD_COUNT - SOCKET_COUNT - PIPE_COUNT - ANON_COUNT))"
echo ""

# 4. 线程信息
echo "【4. 线程信息】"
THREAD_COUNT=$(ls -1 /proc/$PID/task 2>/dev/null | wc -l)
THREAD_LIMIT=$(cat /proc/$PID/limits 2>/dev/null | grep "Max processes" | awk '{print $4}')
if [ "$THREAD_LIMIT" != "unlimited" ] && [ ! -z "$THREAD_LIMIT" ]; then
    THREAD_USAGE=$(awk "BEGIN {printf \"%.2f\", $THREAD_COUNT*100/$THREAD_LIMIT}")
else
    THREAD_USAGE="N/A"
fi

echo "  当前线程数: $THREAD_COUNT"
echo "  系统限制: $THREAD_LIMIT"
if [ "$THREAD_USAGE" != "N/A" ]; then
    echo "  使用率: ${THREAD_USAGE}%"
fi
echo ""

# 5. Java 堆内存 (如果 jstat 可用)
if command -v jstat &> /dev/null; then
    echo "【5. Java 堆内存 (GC 统计)】"
    jstat -gc $PID | awk 'NR==1 {
        printf "  %-15s %12s %12s %12s %12s %12s %12s %12s %12s\n", "区域", "S0C", "S1C", "S0U", "S1U", "EC", "EU", "OC", "OU"
    } NR==2 {
        printf "  %-15s %12s %12s %12s %12s %12s %12s %12s %12s\n", "堆内存(KB)", $1, $2, $3, $4, $5, $6, $7, $8
        s0c=$1; s1c=$2; s0u=$3; s1u=$4; ec=$5; eu=$6; oc=$7; ou=$8
        total_used = s0u + s1u + eu + ou
        total_cap = s0c + s1c + ec + oc
        printf "  已用: %.2f MB\n", total_used/1024
        printf "  容量: %.2f MB\n", total_cap/1024
        printf "  使用率: %.2f%%\n", (total_used*100/total_cap)
    }'
    echo ""
    
    echo "【6. Java 堆内存容量】"
    jstat -gccapacity $PID | awk 'NR==1 {
        printf "  %-15s %12s %12s %12s %12s\n", "区域", "NGCMN", "NGCMX", "NGC", "S0C"
    } NR==2 {
        printf "  %-15s %12s %12s %12s %12s\n", "容量(KB)", $1, $2, $3, $4
    }'
    echo ""
fi

# 6. 内存映射统计
echo "【7. 内存映射统计】"
if command -v pmap &> /dev/null; then
    PMAP_OUTPUT=$(pmap -x $PID 2>/dev/null | tail -1)
    if [ ! -z "$PMAP_OUTPUT" ]; then
        echo "$PMAP_OUTPUT" | awk '{
            total_kb = $3
            rss_kb = $4
            dirty_kb = $5
            printf "  总虚拟内存: %.2f MB\n", total_kb/1024
            printf "  物理内存(RSS): %.2f MB\n", rss_kb/1024
            printf "  脏页: %.2f MB\n", dirty_kb/1024
        }'
    fi
fi
echo ""

# 7. 上下文切换
echo "【8. 上下文切换统计】"
cat /proc/$PID/status | grep -E "(voluntary_ctxt_switches|nonvoluntary_ctxt_switches)" | while read line; do
    key=$(echo "$line" | awk '{print $1}')
    value=$(echo "$line" | awk '{print $2}')
    printf "  %-30s: %s\n" "$key" "$value"
done
echo ""

# 8. 资源限制
echo "【9. 系统资源限制】"
cat /proc/$PID/limits | grep -E "(Max open files|Max processes|Max file size|Max locked memory|Max address space)" | while read line; do
    resource=$(echo "$line" | awk '{print $1, $2, $3}')
    soft=$(echo "$line" | awk '{print $4}')
    hard=$(echo "$line" | awk '{print $5}')
    unit=$(echo "$line" | awk '{print $6}')
    printf "  %-25s: soft=%s %s, hard=%s %s\n" "$resource" "$soft" "$unit" "$hard" "$unit"
done
echo ""

echo "=========================================="
echo "监控完成"
echo "=========================================="
echo ""
echo "提示:"
echo "  - 如果 FD 数量持续增长，可能存在文件描述符泄露"
echo "  - 如果线程数持续增长，可能存在线程泄露"
echo "  - 如果 RSS/堆内存持续增长且不下降，可能存在内存泄露"
echo "  - 建议定期运行此脚本进行对比: watch -n 5 ./monitor_java_process.sh $PID"

