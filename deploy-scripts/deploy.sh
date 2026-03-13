#!/bin/bash
# 健康管理系统部署脚本
# 使用方法: ./deploy.sh [环境]
# 环境: dev(默认), prod

set -e

# 配置
ENV=${1:-dev}
SERVER_IP="139.196.155.234"
SSH_USER="root"
DEPLOY_DIR="/opt/health-tracker"
SSH_KEY="$HOME/.ssh/health_tracker_deploy"

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

check_ssh() {
    log_info "检查SSH连接..."
    if ssh -o StrictHostKeyChecking=no -i "$SSH_KEY" "${SSH_USER}@${SERVER_IP}" "echo 'SSH连接成功'"; then
        log_info "SSH连接正常"
    else
        log_error "SSH连接失败，请检查密钥和网络"
        exit 1
    fi
}

deploy_to_server() {
    log_info "开始部署到服务器..."
    
    # 1. 创建部署目录
    ssh -o StrictHostKeyChecking=no -i "$SSH_KEY" "${SSH_USER}@${SERVER_IP}" "
        mkdir -p $DEPLOY_DIR
        cd $DEPLOY_DIR
        echo '当前目录: \$(pwd)'
    "
    
    # 2. 备份现有数据
    log_info "备份现有数据..."
    ssh -o StrictHostKeyChecking=no -i "$SSH_KEY" "${SSH_USER}@${SERVER_IP}" "
        cd $DEPLOY_DIR
        if [ -d 'docker/mysql' ]; then
            tar -czf mysql-backup-\$(date +%Y%m%d%H%M%S).tar.gz docker/mysql
            log_info 'MySQL数据备份完成'
        fi
    "
    
    # 3. 停止旧服务
    log_info "停止旧服务..."
    ssh -o StrictHostKeyChecking=no -i "$SSH_KEY" "${SSH_USER}@${SERVER_IP}" "
        cd $DEPLOY_DIR
        if [ -f 'docker-compose.yml' ]; then
            podman-compose down || true
            log_info '旧服务已停止'
        fi
    "
    
    # 4. 传输项目文件
    log_info "传输项目文件到服务器..."
    
    # 创建临时目录
    TEMP_DIR="/tmp/health-tracker-$(date +%s)"
    mkdir -p "$TEMP_DIR"
    
    # 复制项目文件（排除不必要的文件）
    rsync -avz -e "ssh -o StrictHostKeyChecking=no -i $SSH_KEY" \
        --exclude='.git' \
        --exclude='node_modules' \
        --exclude='target' \
        --exclude='dist' \
        --exclude='.DS_Store' \
        --exclude='.idea' \
        /Users/shuaiqideguigegegegege/conproject/health-tracker/ \
        "${SSH_USER}@${SERVER_IP}:${DEPLOY_DIR}/"
    
    # 5. 设置权限
    ssh -o StrictHostKeyChecking=no -i "$SSH_KEY" "${SSH_USER}@${SERVER_IP}" "
        cd $DEPLOY_DIR
        chmod +x deploy-scripts/*.sh
    "
    
    # 6. 启动服务
    log_info "启动服务..."
    ssh -o StrictHostKeyChecking=no -i "$SSH_KEY" "${SSH_USER}@${SERVER_IP}" "
        cd $DEPLOY_DIR
        podman-compose up -d --build
    "
    
    # 7. 等待服务启动
    log_info "等待服务启动..."
    sleep 30
    
    # 8. 检查服务状态
    log_info "检查服务状态..."
    ssh -o StrictHostKeyChecking=no -i "$SSH_KEY" "${SSH_USER}@${SERVER_IP}" "
        cd $DEPLOY_DIR
        echo '=== 容器状态 ==='
        podman-compose ps
        echo ''
        echo '=== 容器日志 ==='
        podman-compose logs --tail=10
    "
    
    # 清理临时目录
    rm -rf "$TEMP_DIR"
}

health_check() {
    log_info "进行健康检查..."
    
    local max_retries=10
    local retry_count=0
    
    while [ $retry_count -lt $max_retries ]; do
        log_info "尝试 $((retry_count + 1))/$max_retries..."
        
        # 检查后端
        if curl -f -s "http://${SERVER_IP}:8080/api/health" > /dev/null 2>&1; then
            log_info "✅ 后端服务正常"
        else
            log_warn "后端服务尚未就绪"
            sleep 10
            ((retry_count++))
            continue
        fi
        
        # 检查Web前端
        if curl -f -s "http://${SERVER_IP}" > /dev/null 2>&1; then
            log_info "✅ Web服务正常"
        else
            log_warn "Web服务尚未就绪"
            sleep 10
            ((retry_count++))
            continue
        fi
        
        # 检查小程序
        if curl -f -s "http://${SERVER_IP}:3101" > /dev/null 2>&1; then
            log_info "✅ 小程序服务正常"
            log_info "🎉 所有服务健康检查通过！"
            return 0
        else
            log_warn "小程序服务尚未就绪"
            sleep 10
            ((retry_count++))
            continue
        fi
    done
    
    log_error "健康检查失败，服务未在预期时间内启动"
    return 1
}

show_access_info() {
    echo ""
    echo "========================================="
    echo "        健康管理系统部署完成"
    echo "========================================="
    echo ""
    echo "📱 访问地址:"
    echo "   后端API:    http://${SERVER_IP}:8080"
    echo "   PC Web端:   http://${SERVER_IP}"
    echo "   小程序H5:   http://${SERVER_IP}:3101"
    echo ""
    echo "🔧 管理命令:"
    echo "   查看状态:   ssh ${SSH_USER}@${SERVER_IP} 'cd ${DEPLOY_DIR} && podman-compose ps'"
    echo "   查看日志:   ssh ${SSH_USER}@${SERVER_IP} 'cd ${DEPLOY_DIR} && podman-compose logs'"
    echo "   重启服务:   ssh ${SSH_USER}@${SERVER_IP} 'cd ${DEPLOY_DIR} && podman-compose restart'"
    echo "   停止服务:   ssh ${SSH_USER}@${SERVER_IP} 'cd ${DEPLOY_DIR} && podman-compose down'"
    echo ""
    echo "📊 监控命令:"
    echo "   资源使用:   ssh ${SSH_USER}@${SERVER_IP} 'podman stats'"
    echo "   磁盘空间:   ssh ${SSH_USER}@${SERVER_IP} 'df -h'"
    echo "   内存使用:   ssh ${SSH_USER}@${SERVER_IP} 'free -h'"
    echo ""
    echo "========================================="
}

# 主函数
main() {
    log_info "开始部署健康管理系统 (环境: $ENV)"
    log_info "服务器: ${SSH_USER}@${SERVER_IP}"
    log_info "部署目录: $DEPLOY_DIR"
    
    # 检查SSH连接
    check_ssh
    
    # 部署到服务器
    deploy_to_server
    
    # 健康检查
    if health_check; then
        show_access_info
        log_info "部署成功完成！"
    else
        log_error "部署过程中出现问题，请检查服务日志"
        exit 1
    fi
}

# 执行主函数
main "$@"