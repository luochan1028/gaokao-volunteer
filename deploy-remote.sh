#!/bin/bash
set -e

echo "=== Gaokao Volunteer System Deployment ==="

# Step 1: Install JDK 17
echo "Step 1: Installing JDK 17..."
if ! command -v java &> /dev/null; then
    apt-get update -qq
    apt-get install -y -qq openjdk-17-jdk > /dev/null 2>&1
    echo "  JDK 17 installed"
else
    echo "  Java already installed: $(java -version 2>&1 | head -1)"
fi

# Step 2: Install Maven
echo "Step 2: Installing Maven..."
if ! command -v mvn &> /dev/null; then
    apt-get install -y -qq maven > /dev/null 2>&1
    echo "  Maven installed"
else
    echo "  Maven already installed"
fi

# Step 3: Clone or pull code
echo "Step 3: Getting code..."
DEPLOY_DIR="/opt/gaokao-volunteer"
if [ -d "$DEPLOY_DIR" ]; then
    echo "  Directory exists, pulling latest..."
    cd "$DEPLOY_DIR"
    git pull origin main 2>/dev/null || true
else
    echo "  Cloning from GitHub..."
    git clone https://github.com/luochan1028/gaokao-volunteer.git "$DEPLOY_DIR" 2>/dev/null || {
        echo "  GitHub clone failed, trying API download..."
        curl -sL "https://api.github.com/repos/luochan1028/gaokao-volunteer/tarball/main" -o /tmp/gaokao.tar.gz
        mkdir -p "$DEPLOY_DIR"
        tar xzf /tmp/gaokao.tar.gz -C "$DEPLOY_DIR" --strip-components=1
        rm -f /tmp/gaokao.tar.gz
    }
fi

cd "$DEPLOY_DIR"
echo "  Code at: $DEPLOY_DIR"

# Step 4: Build backend
echo "Step 4: Building backend..."
cd "$DEPLOY_DIR/gaokao-volunteer/backend"
mvn clean package -DskipTests -q 2>&1 | tail -5
echo "  Backend built: $(ls target/*.jar)"

# Step 5: Build frontend
echo "Step 5: Building frontend..."
cd "$DEPLOY_DIR/gaokao-volunteer/frontend"
npm install --silent 2>&1 | tail -3
# Update vite config for production - API proxy to backend
npx vite build 2>&1 | tail -5
echo "  Frontend built: dist/"

# Step 6: Configure Nginx
echo "Step 6: Configuring Nginx..."
cat > /etc/nginx/sites-available/gaokao << 'NGINX_EOF'
server {
    listen 80;
    server_name _;

    # Frontend
    location / {
        root /opt/gaokao-volunteer/gaokao-volunteer/frontend/dist;
        try_files $uri $uri/ /index.html;
        index index.html;
    }

    # API proxy to backend
    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # Static assets caching
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 30d;
        add_header Cache-Control "public, immutable";
    }
}
NGINX_EOF

ln -sf /etc/nginx/sites-available/gaokao /etc/nginx/sites-enabled/gaokao
rm -f /etc/nginx/sites-enabled/default
nginx -t 2>&1
systemctl reload nginx
echo "  Nginx configured"

# Step 7: Create systemd service for backend
echo "Step 7: Creating backend service..."
cat > /etc/systemd/system/gaokao-backend.service << 'SVC_EOF'
[Unit]
Description=Gaokao Volunteer Backend Service
After=network.target

[Service]
Type=simple
User=root
WorkingDirectory=/opt/gaokao-volunteer/gaokao-volunteer/backend
ExecStart=/usr/bin/java -jar -Xms256m -Xmx512m target/gaokao-volunteer-1.0.0.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
SVC_EOF

systemctl daemon-reload
systemctl enable gaokao-backend
systemctl restart gaokao-backend
echo "  Backend service started"

# Step 8: Wait and verify
echo "Step 8: Verifying..."
sleep 10
if curl -s http://localhost:8080/api/public/colleges?page=0&size=1 | grep -q "data"; then
    echo "  Backend: OK"
else
    echo "  Backend: FAILED - checking logs..."
    journalctl -u gaokao-backend --no-pager -n 20
fi

if curl -s http://localhost/ | grep -q "html"; then
    echo "  Frontend: OK"
else
    echo "  Frontend: FAILED"
fi

echo "=== Deployment completed! ==="
echo "  Frontend: http://116.63.203.1/"
echo "  Backend API: http://116.63.203.1/api/"
