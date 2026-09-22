#!/bin/bash
# ============================================================
#  高考志愿填报系统 - 一键部署脚本
#  用法: bash quick-deploy.sh
#  可选参数:
#    PORT=8081 bash quick-deploy.sh    # 指定前端端口（默认80）
#    APP_DIR=/opt/gaokao bash quick-deploy.sh  # 指定安装目录
# ============================================================

set -e

# ====== 配置 ======
GITHUB_REPO="luochan1028/gaokao-volunteer"
VERSION="v1.0.0"
APP_DIR="${APP_DIR:-/opt/gaokao-volunteer}"
NGINX_PORT="${PORT:-80}"
BACKEND_PORT=8080
JAR_NAME="gaokao-volunteer-1.0.0.jar"
FRONTEND_ZIP="frontend-dist.zip"
SERVICE_NAME="gaokao-backend"
# ==================

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info()  { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn()  { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

echo ""
echo "============================================================"
echo "  高考志愿填报系统 一键部署"
echo "  版本: $VERSION"
echo "  端口: 前端 $NGINX_PORT / 后端 $BACKEND_PORT"
echo "  目录: $APP_DIR"
echo "============================================================"
echo ""

# ====== Step 0: 检查是否为 root ======
if [ "$EUID" -ne 0 ]; then
    log_error "请使用 root 权限运行: sudo bash quick-deploy.sh"
    exit 1
fi

# ====== Step 1: 安装依赖 ======
log_info "Step 1/6: 检查并安装依赖..."

# 检测包管理器
if command -v apt-get &> /dev/null; then
    PKG="apt-get"
elif command -v yum &> /dev/null; then
    PKG="yum"
else
    log_error "不支持的系统（未找到 apt-get 或 yum）"
    exit 1
fi

# 安装 Java
if ! command -v java &> /dev/null; then
    log_info "  安装 JDK 17..."
    if [ "$PKG" = "apt-get" ]; then
        $PKG update -qq
        $PKG install -y -qq openjdk-17-jdk > /dev/null 2>&1
    else
        $PKG install -y java-17-openjdk-devel > /dev/null 2>&1
    fi
    log_info "  JDK 17 安装完成"
else
    log_info "  Java 已安装: $(java -version 2>&1 | head -1)"
fi

# 安装 Nginx
if ! command -v nginx &> /dev/null; then
    log_info "  安装 Nginx..."
    if [ "$PKG" = "apt-get" ]; then
        $PKG install -y -qq nginx > /dev/null 2>&1
    else
        $PKG install -y nginx > /dev/null 2>&1
    fi
    log_info "  Nginx 安装完成"
else
    log_info "  Nginx 已安装: $(nginx -v 2>&1)"
fi

# 安装 unzip
if ! command -v unzip &> /dev/null; then
    $PKG install -y -qq unzip > /dev/null 2>&1
fi

# ====== Step 2: 下载构建产物 ======
log_info "Step 2/6: 从 GitHub 下载构建产物..."

mkdir -p "$APP_DIR/backend" "$APP_DIR/frontend"
cd /tmp

DOWNLOAD_BASE="https://github.com/$GITHUB_REPO/releases/download/$VERSION"

# 下载后端 jar
if [ -f "$APP_DIR/backend/$JAR_NAME" ]; then
    log_warn "  后端 jar 已存在，跳过下载"
else
    log_info "  下载后端 jar..."
    curl -sL -o "$APP_DIR/backend/$JAR_NAME" "$DOWNLOAD_BASE/$JAR_NAME"
    if [ ! -f "$APP_DIR/backend/$JAR_NAME" ] || [ $(stat -c%s "$APP_DIR/backend/$JAR_NAME") -lt 1000000 ]; then
        log_error "  后端 jar 下载失败"
        exit 1
    fi
    log_info "  后端 jar 下载完成 ($(du -h "$APP_DIR/backend/$JAR_NAME" | cut -f1))"
fi

# 下载前端 dist
if [ -d "$APP_DIR/frontend/dist" ]; then
    log_warn "  前端 dist 已存在，跳过下载"
else
    log_info "  下载前端 dist..."
    curl -sL -o /tmp/$FRONTEND_ZIP "$DOWNLOAD_BASE/$FRONTEND_ZIP"
    if [ ! -f "/tmp/$FRONTEND_ZIP" ] || [ $(stat -c%s "/tmp/$FRONTEND_ZIP") -lt 10000 ]; then
        log_error "  前端 dist 下载失败"
        exit 1
    fi
    unzip -o -q /tmp/$FRONTEND_ZIP -d "$APP_DIR/frontend/"
    rm -f /tmp/$FRONTEND_ZIP
    log_info "  前端 dist 下载完成 ($(find "$APP_DIR/frontend/dist" -type f | wc -l) 个文件)"
fi

# ====== Step 3: 配置 Nginx ======
log_info "Step 3/6: 配置 Nginx..."

# 检测 Nginx 配置目录
if [ -d /etc/nginx/sites-available ]; then
    NGINX_CONF_DIR="/etc/nginx/sites-available"
    NGINX_ENABLED_DIR="/etc/nginx/sites-enabled"
elif [ -d /etc/nginx/conf.d ]; then
    NGINX_CONF_DIR="/etc/nginx/conf.d"
    NGINX_ENABLED_DIR="/etc/nginx/conf.d"
else
    log_error "  找不到 Nginx 配置目录"
    exit 1
fi

CONF_FILE="$NGINX_CONF_DIR/gaokao.conf"

cat > "$CONF_FILE" << NGINX_EOF
server {
    listen $NGINX_PORT;
    server_name _;
    root $APP_DIR/frontend/dist;
    index index.html;

    # SPA 路由回退
    location / {
        try_files \$uri \$uri/ /index.html;
    }

    # API 代理到后端
    location /api/ {
        proxy_pass http://127.0.0.1:$BACKEND_PORT;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_connect_timeout 30s;
        proxy_read_timeout 30s;
    }

    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 30d;
        add_header Cache-Control "public, immutable";
    }
}
NGINX_EOF

# 创建软链接（仅 Debian/Ubuntu 风格）
if [ "$NGINX_CONF_DIR" != "$NGINX_ENABLED_DIR" ]; then
    ln -sf "$CONF_FILE" "$NGINX_ENABLED_DIR/gaokao.conf"
    # 禁用 default，防止冲突
    rm -f "$NGINX_ENABLED_DIR/default"
fi

# 测试配置
if nginx -t -q 2>/dev/null; then
    systemctl reload nginx 2>/dev/null || nginx -s reload 2>/dev/null || nginx
    log_info "  Nginx 配置完成"
else
    log_error "  Nginx 配置测试失败"
    nginx -t 2>&1
    exit 1
fi

# ====== Step 4: 创建 systemd 服务 ======
log_info "Step 4/6: 创建后端服务..."

cat > /etc/systemd/system/$SERVICE_NAME.service << SVC_EOF
[Unit]
Description=Gaokao Volunteer Backend Service
After=network.target

[Service]
Type=simple
User=root
WorkingDirectory=$APP_DIR/backend
ExecStart=/usr/bin/java -jar -Xms256m -Xmx512m $JAR_NAME
Restart=always
RestartSec=10
StandardOutput=append:$APP_DIR/backend.log
StandardError=append:$APP_DIR/backend.log

[Install]
WantedBy=multi-user.target
SVC_EOF

systemctl daemon-reload
systemctl enable $SERVICE_NAME > /dev/null 2>&1
log_info "  服务创建完成"

# ====== Step 5: 启动服务 ======
log_info "Step 5/6: 启动服务..."

# 先杀掉可能占用端口的旧进程
fuser -k ${BACKEND_PORT}/tcp 2>/dev/null || true
sleep 1

systemctl restart $SERVICE_NAME

# 等待后端启动
log_info "  等待后端启动..."
for i in $(seq 1 30); do
    if curl -s "http://127.0.0.1:$BACKEND_PORT/api/public/colleges?page=0" > /dev/null 2>&1; then
        break
    fi
    echo -n "."
    sleep 1
done
echo ""

# ====== Step 6: 验证 ======
log_info "Step 6/6: 验证部署..."

BACKEND_OK=$(curl -s -o /dev/null -w "%{http_code}" "http://127.0.0.1:$BACKEND_PORT/api/public/colleges?page=0" 2>/dev/null || echo "000")
FRONTEND_OK=$(curl -s -o /dev/null -w "%{http_code}" "http://127.0.0.1:$NGINX_PORT/" 2>/dev/null || echo "000")
API_OK=$(curl -s -o /dev/null -w "%{http_code}" "http://127.0.0.1:$NGINX_PORT/api/public/colleges?page=0" 2>/dev/null || echo "000")

ALL_OK=true
if [ "$BACKEND_OK" = "200" ]; then
    log_info "  后端 API: 正常 (HTTP 200)"
else
    log_error "  后端 API: 异常 (HTTP $BACKEND_OK)"
    ALL_OK=false
fi

if [ "$FRONTEND_OK" = "200" ]; then
    log_info "  前端页面: 正常 (HTTP 200)"
else
    log_error "  前端页面: 异常 (HTTP $FRONTEND_OK)"
    ALL_OK=false
fi

if [ "$API_OK" = "200" ]; then
    log_info "  Nginx 代理: 正常 (HTTP 200)"
else
    log_error "  Nginx 代理: 异常 (HTTP $API_OK)"
    ALL_OK=false
fi

# ====== 完成 ======
echo ""
echo "============================================================"
if [ "$ALL_OK" = true ]; then
    echo -e "  ${GREEN}部署成功！${NC}"
else
    echo -e "  ${YELLOW}部署完成（部分异常）${NC}"
fi

# 获取公网 IP
PUBLIC_IP=$(curl -s ifconfig.me 2>/dev/null || curl -s ipinfo.io/ip 2>/dev/null || echo "你的服务器IP")

echo ""
echo "  访问地址: http://$PUBLIC_IP:$NGINX_PORT/"
echo ""
echo "  默认账号:"
echo "    手机号: 13900000001"
echo "    密码:   123456"
echo ""
echo "  常用命令:"
echo "    重启后端:  systemctl restart $SERVICE_NAME"
echo "    后端日志:  tail -f $APP_DIR/backend.log"
echo "    重启Nginx: systemctl reload nginx"
echo "    停止服务:  systemctl stop $SERVICE_NAME"
echo ""
echo "  文件位置:"
echo "    前端: $APP_DIR/frontend/dist/"
echo "    后端: $APP_DIR/backend/$JAR_NAME"
echo "    日志: $APP_DIR/backend.log"
echo "============================================================"
echo ""
