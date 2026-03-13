#!/bin/bash
# 服务器初始化脚本
# 在目标服务器上运行此脚本以安装必要的软件

set -e

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查是否为root用户
check_root() {
    if [ "$EUID" -ne 0 ]; then
        log_error "请使用root用户运行此脚本"
        exit 1
    fi
}

# 更新系统
update_system() {
    log_info "更新系统包..."
    
    # 检查系统类型
    if [ -f /etc/redhat-release ]; then
        # CentOS/RHEL
        yum update -y
        yum install -y epel-release
    elif [ -f /etc/debian_version ]; then
        # Debian/Ubuntu
        apt-get update
        apt-get upgrade -y
    elif [ -f /etc/alpine-release ]; then
        # Alpine
        apk update
        apk upgrade
    else
        log_warn "未知的系统类型，跳过系统更新"
    fi
}

# 安装必要软件
install_software() {
    log_info "安装必要软件..."
    
    if [ -f /etc/redhat-release ]; then
        # CentOS/RHEL
        yum install -y \
            git \
            curl \
            wget \
            vim \
            net-tools \
            htop \
            telnet \
            jq \
            tar \
            gzip \
            unzip
    elif [ -f /etc/debian_version ]; then
        # Debian/Ubuntu
        apt-get install -y \
            git \
            curl \
            wget \
            vim \
            net-tools \
            htop \
            telnet \
            jq \
            tar \
            gzip \
            unzip \
            ca-certificates
    elif [ -f /etc/alpine-release ]; then
        # Alpine
        apk add \
            git \
            curl \
            wget \
            vim \
            net-tools \
            htop \
            telnet \
            jq \
            tar \
            gzip \
            unzip \
            ca-certificates
    fi
}

# 安装Podman
install_podman() {
    log_info "安装Podman..."
    
    if command -v podman &> /dev/null; then
        log_info "Podman 已安装，版本: $(podman --version)"
        return
    fi
    
    if [ -f /etc/redhat-release ]; then
        # CentOS/RHEL 8+
        dnf install -y podman podman-compose
    elif [ -f /etc/debian_version ]; then
        # Debian/Ubuntu
        . /etc/os-release
        echo "deb https://download.opensuse.org/repositories/devel:/kubic:/libcontainers:/stable/xUbuntu_${VERSION_ID}/ /" | tee /etc/apt/sources.list.d/devel:kubic:libcontainers:stable.list
        curl -L "https://download.opensuse.org/repositories/devel:/kubic:/libcontainers:/stable/xUbuntu_${VERSION_ID}/Release.key" | apt-key add -
        apt-get update
        apt-get install -y podman podman-compose
    elif [ -f /etc/alpine-release ]; then
        # Alpine
        apk add podman podman-compose
    else
        log_error "不支持的系统类型，无法安装Podman"
        exit 1
    fi
    
    log_info "Podman 安装完成，版本: $(podman --version)"
}

# 配置Podman
configure_podman() {
    log_info "配置Podman..."
    
    # 创建配置目录
    mkdir -p /etc/containers
    
    # 配置镜像源（使用DaoCloud镜像加速）
    cat > /etc/containers/registries.conf << EOF
unqualified-search-registries = ["docker.io"]

[[registry]]
prefix = "docker.io"
location = "docker.m.daocloud.io"

[[registry]]
prefix = "registry-1.docker.io"
location = "docker.m.daocloud.io"
EOF
    
    # 配置存储
    cat > /etc/containers/storage.conf << EOF
[storage]
driver = "overlay"
runroot = "/var/run/containers/storage"
graphroot = "/var/lib/containers/storage"
EOF
    
    # 启动Podman服务
    systemctl enable podman
    systemctl start podman
    
    log_info "Podman 配置完成"
}

# 安装Java
install_java() {
    log_info "安装Java 17..."
    
    if command -v java &> /dev/null; then
        log_info "Java 已安装，版本: $(java -version 2>&1 | head -1)"
        return
    fi
    
    if [ -f /etc/redhat-release ]; then
        # CentOS/RHEL
        yum install -y java-17-openjdk-devel
    elif [ -f /etc/debian_version ]; then
        # Debian/Ubuntu
        apt-get install -y openjdk-17-jdk
    elif [ -f /etc/alpine-release ]; then
        # Alpine
        apk add openjdk17
    fi
    
    # 设置JAVA_HOME
    JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
    echo "export JAVA_HOME=$JAVA_HOME" >> /etc/profile
    echo "export PATH=\$PATH:\$JAVA_HOME/bin" >> /etc/profile
    source /etc/profile
    
    log_info "Java 安装完成，版本: $(java -version 2>&1 | head -1)"
}

# 安装Node.js
install_nodejs() {
    log_info "安装Node.js 18..."
    
    if command -v node &> /dev/null; then
        log_info "Node.js 已安装，版本: $(node --version)"
        return
    fi
    
    # 使用NodeSource安装
    curl -fsSL https://deb.nodesource.com/setup_18.x | bash -
    apt-get install -y nodejs
    
    log_info "Node.js 安装完成，版本: $(node --version)"
}

# 安装Maven
install_maven() {
    log_info "安装Maven..."
    
    if command -v mvn &> /dev/null; then
        log_info "Maven 已安装，版本: $(mvn --version | head -1)"
        return
    fi
    
    if [ -f /etc/redhat-release ]; then
        # CentOS/RHEL
        yum install -y maven
    elif [ -f /etc/debian_version ]; then
        # Debian/Ubuntu
        apt-get install -y maven
    elif [ -f /etc/alpine-release ]; then
        # Alpine
        apk add maven
    fi
    
    log_info "Maven 安装完成，版本: $(mvn --version | head -1)"
}

# 配置防火墙
configure_firewall() {
    log_info "配置防火墙..."
    
    # 检查防火墙状态
    if command -v ufw &> /dev/null; then
        # Ubuntu UFW
        ufw allow 22/tcp    # SSH
        ufw allow 80/tcp    # HTTP
        ufw allow 443/tcp   # HTTPS
        ufw allow 8080/tcp  # Backend API
        ufw allow 3101/tcp  # MiniApp H5
        ufw --force enable
        log_info "UFW 防火墙已配置"
    elif command -v firewall-cmd &> /dev/null; then
        # Firewalld
        firewall-cmd --permanent --add-port=22/tcp
        firewall-cmd --permanent --add-port=80/tcp
        firewall-cmd --permanent --add-port=443/tcp
        firewall-cmd --permanent --add-port=8080/tcp
        firewall-cmd --permanent --add-port=3101/tcp
        firewall-cmd --reload
        log_info "Firewalld 防火墙已配置"
    elif command -v iptables &> /dev/null; then
        # iptables
        iptables -A INPUT -p tcp --dport 22 -j ACCEPT
        iptables -A INPUT -p tcp --dport 80 -j ACCEPT
        iptables -A INPUT -p tcp --dport 443 -j ACCEPT
        iptables -A INPUT -p tcp --dport 8080 -j ACCEPT
        iptables -A INPUT -p tcp --dport 3101 -j ACCEPT
        iptables-save > /etc/iptables/rules.v4
        log_info "iptables 防火墙已配置"
    else
        log_warn "未找到防火墙工具，跳过防火墙配置"
    fi
}

# 创建部署目录
create_deploy_dir() {
    local deploy_dir="/opt/health-tracker"
    
    log_info "创建部署目录: $deploy_dir"
    mkdir -p "$deploy_dir"
    chmod 755 "$deploy_dir"
    
    # 创建必要的子目录
    mkdir -p "$deploy_dir/docker/mysql"
    mkdir -p "$deploy_dir/logs"
    mkdir -p "$deploy_dir/backups"
    
    log_info "部署目录结构创建完成"
}

# 显示安装摘要
show_summary() {
    echo ""
    echo "========================================="
    echo "        服务器初始化完成"
    echo "========================================="
    echo ""
    echo "📦 已安装软件:"
    echo "   Podman:      $(podman --version 2>/dev/null || echo '未安装')"
    echo "   Java:        $(java -version 2>&1 | head -1 | cut -d'"' -f2 2>/dev/null || echo '未安装')"
    echo "   Node.js:     $(node --version 2>/dev/null || echo '未安装')"
    echo "   Maven:       $(mvn --version 2>&1 | head -1 | cut -d' ' -f3 2>/dev/null || echo '未安装')"
    echo "   Git:         $(git --version 2>/dev/null || echo '未安装')"
    echo ""
    echo "🔧 系统配置:"
    echo "   部署目录:    /opt/health-tracker"
    echo "   开放端口:    22(SSH), 80(HTTP), 443(HTTPS), 8080(API), 3101(H5)"
    echo ""
    echo "🚀 下一步:"
    echo "   1. 将项目代码部署到 /opt/health-tracker"
    echo "   2. 运行: cd /opt/health-tracker && podman-compose up -d"
    echo "   3. 访问: http://服务器IP"
    echo ""
    echo "========================================="
}

# 主函数
main() {
    log_info "开始服务器初始化..."
    
    # 检查root权限
    check_root
    
    # 执行初始化步骤
    update_system
    install_software
    install_podman
    configure_podman
    install_java
    install_nodejs
    install_maven
    configure_firewall
    create_deploy_dir
    
    # 显示摘要
    show_summary
    
    log_info "服务器初始化完成！"
}

# 执行主函数
main "$@"