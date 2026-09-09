#!/bin/bash
# ==============================================================================
# 高考志愿填报系统 - Ubuntu 一键安装运行脚本
# 用法: chmod +x run.sh && ./run.sh
# ==============================================================================
set -e

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
BACKEND_DIR="$PROJECT_DIR/backend"
FRONTEND_DIR="$PROJECT_DIR/frontend"
LOG_DIR="$PROJECT_DIR/logs"
PID_DIR="$PROJECT_DIR/.pids"

mkdir -p "$LOG_DIR" "$PID_DIR"

# 颜色输出
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

info()  { echo -e "${GREEN}[INFO]${NC} $1"; }
warn()  { echo -e "${YELLOW}[WARN]${NC} $1"; }
error() { echo -e "${RED}[ERROR]${NC} $1"; }

# ==============================================================================
# 1. 检查并安装 Java 17
# ==============================================================================
install_java() {
    info "检查 Java 环境..."
    if command -v java &>/dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | head -1 | awk -F'"' '{print $2}' | cut -d'.' -f1)
        info "当前 Java 版本: $JAVA_VERSION"
        if [ "$JAVA_VERSION" -ge 17 ] 2>/dev/null; then
            info "Java 版本满足要求(>=17)"
            return 0
        fi
    fi
    warn "未找到 Java 17+，开始安装..."
    sudo apt-get update -qq
    sudo apt-get install -y -qq openjdk-17-jdk
    info "Java 17 安装完成"
}

# ==============================================================================
# 2. 检查并安装 Maven
# ==============================================================================
install_maven() {
    info "检查 Maven 环境..."
    if command -v mvn &>/dev/null; then
        info "Maven 已安装: $(mvn -version 2>&1 | head -1)"
        return 0
    fi
    warn "未找到 Maven，开始安装..."
    sudo apt-get install -y -qq maven
    info "Maven 安装完成"
}

# ==============================================================================
# 3. 检查并安装 Node.js 18+
# ==============================================================================
install_node() {
    info "检查 Node.js 环境..."
    if command -v node &>/dev/null; then
        NODE_VERSION=$(node -v | sed 's/v//' | cut -d'.' -f1)
        info "当前 Node.js 版本: $(node -v)"
        if [ "$NODE_VERSION" -ge 18 ] 2>/dev/null; then
            info "Node.js 版本满足要求(>=18)"
            return 0
        fi
    fi
    warn "未找到 Node.js 18+，开始安装..."
    if ! command -v curl &>/dev/null; then
        sudo apt-get install -y -qq curl
    fi
    curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
    sudo apt-get install -y -qq nodejs
    info "Node.js 安装完成: $(node -v)"
}

# ==============================================================================
# 4. 构建后端
# ==============================================================================
build_backend() {
    info "构建后端..."
    cd "$BACKEND_DIR"
    mvn -q -DskipTests clean package
    JAR_FILE=$(find target -name "*.jar" -not -name "*.original" | head -1)
    if [ -z "$JAR_FILE" ]; then
        error "后端构建失败，未找到 jar 文件"
        exit 1
    fi
    info "后端构建成功: $JAR_FILE"
}

# ==============================================================================
# 5. 安装前端依赖
# ==============================================================================
install_frontend() {
    info "安装前端依赖..."
    cd "$FRONTEND_DIR"
    if [ -d node_modules ]; then
        info "node_modules 已存在，跳过安装"
    else
        npm install --silent 2>/dev/null || npm install
    fi
    info "前端依赖安装完成"
}

# ==============================================================================
# 6. 启动后端
# ==============================================================================
start_backend() {
    info "启动后端服务(端口8080)..."
    if [ -f "$PID_DIR/backend.pid" ]; then
        OLD_PID=$(cat "$PID_DIR/backend.pid")
        if kill -0 "$OLD_PID" 2>/dev/null; then
            warn "后端已在运行(PID=$OLD_PID)，先停止..."
            kill "$OLD_PID" 2>/dev/null || true
            sleep 2
        fi
    fi
    cd "$BACKEND_DIR"
    JAR_FILE=$(find target -name "*.jar" -not -name "*.original" | head -1)
    nohup java -jar "$JAR_FILE" > "$LOG_DIR/backend.log" 2>&1 &
    echo $! > "$PID_DIR/backend.pid"
    info "后端 PID=$(cat "$PID_DIR/backend.pid")"

    # 等待后端就绪
    info "等待后端启动..."
    for i in $(seq 1 60); do
        if curl -s "http://localhost:8080/api/public/colleges?page=0&size=1" | grep -q "code"; then
            info "后端就绪 (${i}s)"
            return 0
        fi
        sleep 1
    done
    error "后端启动超时"
    tail -20 "$LOG_DIR/backend.log"
    exit 1
}

# ==============================================================================
# 7. 启动前端
# ==============================================================================
start_frontend() {
    info "启动前端服务(端口3000)..."
    if [ -f "$PID_DIR/frontend.pid" ]; then
        OLD_PID=$(cat "$PID_DIR/frontend.pid")
        if kill -0 "$OLD_PID" 2>/dev/null; then
            warn "前端已在运行(PID=$OLD_PID)，先停止..."
            kill "$OLD_PID" 2>/dev/null || true
            sleep 2
        fi
    fi
    cd "$FRONTEND_DIR"
    nohup npm run dev > "$LOG_DIR/frontend.log" 2>&1 &
    echo $! > "$PID_DIR/frontend.pid"
    info "前端 PID=$(cat "$PID_DIR/frontend.pid")"

    # 等待前端就绪
    info "等待前端启动..."
    for i in $(seq 1 40); do
        if curl -s "http://localhost:3000" | grep -q "html"; then
            info "前端就绪 (${i}s)"
            return 0
        fi
        sleep 1
    done
    error "前端启动超时"
    tail -20 "$LOG_DIR/frontend.log"
    exit 1
}

# ==============================================================================
# 8. 停止服务
# ==============================================================================
stop_services() {
    info "停止服务..."
    for name in frontend backend; do
        if [ -f "$PID_DIR/$name.pid" ]; then
            PID=$(cat "$PID_DIR/$name.pid")
            if kill -0 "$PID" 2>/dev/null; then
                kill "$PID" 2>/dev/null
                info "已停止 $name (PID=$PID)"
            fi
            rm -f "$PID_DIR/$name.pid"
        fi
    done
    # 清理残留进程
    pkill -f "gaokao-volunteer.*jar" 2>/dev/null || true
    pkill -f "vite.*dev" 2>/dev/null || true
}

# ==============================================================================
# 主逻辑
# ==============================================================================
case "${1:-start}" in
    start)
        echo "=============================================="
        echo "  高考志愿填报系统 - 一键启动"
        echo "=============================================="
        install_java
        install_maven
        install_node
        build_backend
        install_frontend
        start_backend
        start_frontend
        echo ""
        echo "=============================================="
        echo -e "${GREEN}  启动成功!${NC}"
        echo "  前端: http://localhost:3000"
        echo "  后端: http://localhost:8080"
        echo "  H2控制台: http://localhost:8080/h2-console"
        echo "  测试账号: 13900000000 / 123456"
        echo "  管理账号: 13800000000 / 123456"
        echo "=============================================="
        echo ""
        echo "停止服务: ./run.sh stop"
        echo "查看日志: tail -f logs/backend.log | tail -f logs/frontend.log"
        echo ""
        # 尝试打开浏览器
        if command -v xdg-open &>/dev/null; then
            xdg-open "http://localhost:3000" 2>/dev/null || true
        elif command -v sensible-browser &>/dev/null; then
            sensible-browser "http://localhost:3000" 2>/dev/null || true
        fi
        info "服务运行中，按 Ctrl+C 退出本脚本(服务继续运行)"
        ;;
    stop)
        stop_services
        info "所有服务已停止"
        ;;
    restart)
        stop_services
        sleep 2
        "$0" start
        ;;
    status)
        BACKEND_OK="否"
        FRONTEND_OK="否"
        if [ -f "$PID_DIR/backend.pid" ] && kill -0 "$(cat "$PID_DIR/backend.pid")" 2>/dev/null; then
            BACKEND_OK="是(PID=$(cat "$PID_DIR/backend.pid"))"
        fi
        if [ -f "$PID_DIR/frontend.pid" ] && kill -0 "$(cat "$PID_DIR/frontend.pid")" 2>/dev/null; then
            FRONTEND_OK="是(PID=$(cat "$PID_DIR/frontend.pid"))"
        fi
        echo "后端运行中: $BACKEND_OK"
        echo "前端运行中: $FRONTEND_OK"
        ;;
    *)
        echo "用法: ./run.sh [start|stop|restart|status]"
        echo "  start   - 安装环境、构建并启动服务(默认)"
        echo "  stop    - 停止所有服务"
        echo "  restart - 重启服务"
        echo "  status  - 查看服务状态"
        exit 1
        ;;
esac