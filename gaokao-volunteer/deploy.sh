#!/bin/bash
# 高考志愿填报平台 - 云主机一键部署脚本
# 用法: bash deploy.sh

set -e

echo "========================================="
echo "  高考志愿填报平台 部署脚本"
echo "========================================="

# 安装 Node.js (如果未安装)
if ! command -v node &> /dev/null; then
    echo ">>> 安装 Node.js..."
    curl -fsSL https://deb.nodesource.com/setup_18.x | bash -
    apt-get install -y nodejs
fi

# 安装 nginx (如果未安装)
if ! command -v nginx &> /dev/null; then
    echo ">>> 安装 Nginx..."
    apt-get update -qq && apt-get install -y -qq nginx
fi

# 克隆/更新代码
APP_DIR="/opt/gaokao-volunteer"
if [ -d "$APP_DIR" ]; then
    echo ">>> 更新代码..."
    cd "$APP_DIR" && git pull -q
else
    echo ">>> 克隆代码..."
    git clone https://github.com/luochan1028/gaokao-volunteer.git "$APP_DIR"
    cd "$APP_DIR"
fi

# 安装依赖并构建前端
echo ">>> 安装前端依赖..."
cd "$APP_DIR/frontend"
npm install --silent

echo ">>> 构建前端..."
npm run build

# 部署到 Nginx
echo ">>> 配置 Nginx..."
cat > /etc/nginx/sites-available/gaokao <<'EOF'
server {
    listen 80;
    server_name _;

    root /opt/gaokao-volunteer/frontend/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
EOF

ln -sf /etc/nginx/sites-available/gaokao /etc/nginx/sites-enabled/gaokao
rm -f /etc/nginx/sites-enabled/default

echo ">>> 重启 Nginx..."
nginx -t && systemctl restart nginx || (nginx -s reload 2>/dev/null || nginx)

echo "========================================="
echo "  部署完成!"
echo "========================================="
echo ""
echo "访问地址: http://$(curl -s ifconfig.me 2>/dev/null || echo 159.138.92.82)"
echo ""
echo "提示: 如无法访问,请在云主机安全组放行 80 端口"
echo "========================================="
