#!/bin/bash
set -e

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
BACKEND_DIR="$PROJECT_DIR/backend"
FRONTEND_DIR="$PROJECT_DIR/frontend"
JAR_FILE="$BACKEND_DIR/target/gaokao-volunteer-1.0.0.jar"

echo "============================================"
echo "  高考志愿填报系统 - 一键部署脚本"
echo "============================================"
echo ""

# 检查 Java
if ! command -v java &> /dev/null; then
    echo "[INFO] 未检测到 Java，开始安装 JDK 17..."
    if command -v apt-get &> /dev/null; then
        apt-get update -qq
        apt-get install -y -qq openjdk-17-jdk
    elif command -v yum &> /dev/null; then
        yum install -y java-17-openjdk-devel
    else
        echo "[ERROR] 无法自动安装 Java，请手动安装 JDK 17"
        exit 1
    fi
fi
JAVA_VERSION=$(java -version 2>&1 | head -1 | awk -F '"' '{print $2}')
echo "[OK] Java 版本: $JAVA_VERSION"

# 检查 Maven
if ! command -v mvn &> /dev/null; then
    echo "[INFO] 未检测到 Maven，开始安装 Maven..."
    if command -v apt-get &> /dev/null; then
        apt-get install -y -qq maven
    elif command -v yum &> /dev/null; then
        yum install -y maven
    else
        echo "[ERROR] 无法自动安装 Maven，请手动安装"
        exit 1
    fi
fi
echo "[OK] Maven: $(mvn -v 2>&1 | head -1)"

# 检查 Node.js
if ! command -v node &> /dev/null; then
    echo "[INFO] 未检测到 Node.js，开始安装 Node.js 18..."
    if command -v apt-get &> /dev/null; then
        curl -fsSL https://deb.nodesource.com/setup_18.x | bash -
        apt-get install -y -qq nodejs
    elif command -v yum &> /dev/null; then
        curl -fsSL https://rpm.nodesource.com/setup_18.x | bash
        yum install -y nodejs
    else
        echo "[ERROR] 无法自动安装 Node.js，请手动安装"
        exit 1
    fi
fi
echo "[OK] Node.js: $(node -v)"

# 构建后端
echo ""
echo "[STEP 1/4] 构建后端..."
if [ ! -f "$JAR_FILE" ]; then
    cd "$BACKEND_DIR"
    mvn clean package -DskipTests -q
    echo "[OK] 后端构建完成"
else
    echo "[SKIP] 后端 jar 已存在，跳过构建"
fi

# 构建前端
echo ""
echo "[STEP 2/4] 构建前端..."
if [ ! -d "$FRONTEND_DIR/dist" ]; then
    cd "$FRONTEND_DIR"
    npm install --silent 2>/dev/null || npm install
    npm run build
    echo "[OK] 前端构建完成"
else
    echo "[SKIP] 前端 dist 已存在，跳过构建"
fi

# 停止旧进程
echo ""
echo "[STEP 3/4] 停止旧进程..."
pkill -f "gaokao-volunteer-1.0.0.jar" 2>/dev/null && echo "[OK] 已停止旧后端进程" || echo "[OK] 无旧后端进程"
pkill -f "vite.*3000" 2>/dev/null && echo "[OK] 已停止旧前端进程" || echo "[OK] 无旧前端进程"
sleep 2

# 启动后端
echo ""
echo "[STEP 4/4] 启动服务..."
nohup java -jar "$JAR_FILE" > "$PROJECT_DIR/backend.log" 2>&1 &
BACKEND_PID=$!
echo "[OK] 后端已启动 (PID: $BACKEND_PID)"

# 等待后端就绪
echo -n "[INFO] 等待后端启动"
for i in $(seq 1 30); do
    if curl -s http://localhost:8080/api/colleges > /dev/null 2>&1; then
        echo " 完成"
        break
    fi
    echo -n "."
    sleep 1
done

# 启动前端
cd "$FRONTEND_DIR"
nohup npm run preview -- --host 0.0.0.0 --port 3000 > "$PROJECT_DIR/frontend.log" 2>&1 &
FRONTEND_PID=$!
echo "[OK] 前端已启动 (PID: $FRONTEND_PID)"

sleep 3
echo ""
echo "============================================"
echo "  部署完成！"
echo "============================================"
echo "  后端 API:  http://localhost:8080"
echo "  前端页面:  http://localhost:3000"
echo "  H2控制台:  http://localhost:8080/h2-console"
echo "  测试账号:  13900000000 / 123456"
echo "  管理账号:  13800000000 / 123456"
echo "============================================"
echo ""
echo "后端日志: tail -f $PROJECT_DIR/backend.log"
echo "前端日志: tail -f $PROJECT_DIR/frontend.log"
echo "停止服务: kill $BACKEND_PID $FRONTEND_PID"