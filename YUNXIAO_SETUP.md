# 云效流水线配置指南

本文档指导如何将健康管理系统部署到阿里云效平台。

## 一、准备工作

### 1.1 代码仓库配置
1. 确保代码已推送到GitHub仓库：https://github.com/Mrwu-gui/health-tracker
2. 在云效平台关联GitHub账号

### 1.2 服务器准备
- 服务器IP: 139.196.155.234
- SSH用户: root
- SSH私钥: 已保存在本地 `/Users/shuaiqideguigegegegege/.ssh/health_tracker_deploy`

### 1.3 云效配置
1. 登录云效平台：https://flow.aliyun.com
2. 创建企业（如果还没有）
3. 创建项目

## 二、创建流水线

### 2.1 新建流水线
1. 进入云效控制台
2. 点击"新建流水线"
3. 选择"空白流水线"

### 2.2 配置代码源
1. 选择"GitHub"作为代码源
2. 授权GitHub账号
3. 选择仓库：`Mrwu-gui/health-tracker`
4. 分支：`main`

### 2.3 添加流水线阶段

#### 阶段1：代码检查
- 名称：代码检查
- 添加步骤：
  1. Java代码编译检查
  2. Node.js依赖安装检查

#### 阶段2：构建
- 名称：构建
- 添加步骤：
  1. Java后端构建
  2. Web前端构建
  3. 小程序H5构建

#### 阶段3：部署
- 名称：部署到服务器
- 添加步骤：SSH部署

#### 阶段4：验证
- 名称：服务验证
- 添加步骤：健康检查

### 2.4 配置环境变量
在流水线设置中添加以下环境变量：

| 变量名 | 值 | 描述 |
|--------|-----|------|
| SERVER_IP | 139.196.155.234 | 服务器公网IP |
| SSH_USER | root | SSH用户名 |
| DEPLOY_DIR | /opt/health-tracker | 部署目录 |

### 2.5 配置SSH密钥
1. 进入云效"证书管理"
2. 添加SSH私钥证书
3. 名称：`health-tracker-server`
4. 私钥内容：复制 `/Users/shuaiqideguigegegegege/.ssh/health_tracker_deploy` 文件内容
5. 在流水线步骤中引用此证书

## 三、流水线步骤详细配置

### 3.1 Java后端构建步骤
```yaml
步骤类型：构建
语言：Java
JDK版本：Open JDK 17
Maven版本：Maven 3.9
构建命令：
cd health-tracker-java
mvn clean package -DskipTests
```

### 3.2 Web前端构建步骤
```yaml
步骤类型：构建
语言：Node.js
Node版本：Node.js 18
构建命令：
cd health-tracker-web
npm install
npm run build
```

### 3.3 小程序H5构建步骤
```yaml
步骤类型：构建
语言：Node.js
Node版本：Node.js 18
构建命令：
cd health-tracker-miniapp
npm install
npm run build:h5
```

### 3.4 SSH部署步骤
```yaml
步骤类型：SSH
主机：${SERVER_IP}
用户名：${SSH_USER}
证书：health-tracker-server
执行命令：
#!/bin/bash
set -e

echo "开始部署健康管理系统..."

# 进入部署目录
cd ${DEPLOY_DIR}

# 停止旧服务
if [ -f "docker-compose.yml" ]; then
    podman-compose down || true
    echo "旧服务已停止"
fi

# 备份数据
if [ -d "docker/mysql" ]; then
    tar -czf backups/mysql-$(date +%Y%m%d%H%M%S).tar.gz docker/mysql
    echo "MySQL数据已备份"
fi

# 拉取最新代码
if [ -d ".git" ]; then
    git pull origin main
else
    git clone https://github.com/Mrwu-gui/health-tracker.git .
fi

# 启动服务
podman-compose up -d --build

# 等待服务启动
sleep 30

# 检查服务状态
podman-compose ps

echo "部署完成！"
```

### 3.5 健康检查步骤
```yaml
步骤类型：命令
执行命令：
#!/bin/bash
echo "进行服务健康检查..."
sleep 60

# 检查后端服务
if curl -f http://${SERVER_IP}:8080/api/health; then
    echo "✅ 后端服务正常"
else
    echo "❌ 后端服务异常"
    exit 1
fi

# 检查Web服务
if curl -f http://${SERVER_IP}; then
    echo "✅ Web服务正常"
else
    echo "❌ Web服务异常"
    exit 1
fi

# 检查小程序服务
if curl -f http://${SERVER_IP}:3101; then
    echo "✅ 小程序服务正常"
else
    echo "❌ 小程序服务异常"
    exit 1
fi

echo "🎉 所有服务验证通过！"
```

## 四、触发器配置

### 4.1 自动触发
- 触发条件：代码推送到main分支
- 触发路径：所有文件变更

### 4.2 手动触发
- 支持手动运行流水线
- 支持参数化构建

## 五、通知配置

### 5.1 钉钉通知
1. 在钉钉群添加机器人
2. 获取Webhook地址
3. 在流水线通知配置中添加：
   - 成功通知
   - 失败通知

### 5.2 邮件通知
1. 配置发件人邮箱
2. 添加收件人列表
3. 设置通知条件

## 六、监控与维护

### 6.1 流水线监控
- 查看流水线运行历史
- 查看构建日志
- 查看部署状态

### 6.2 服务器监控
```bash
# 查看容器状态
ssh root@139.196.155.234 "cd /opt/health-tracker && podman-compose ps"

# 查看容器日志
ssh root@139.196.155.234 "cd /opt/health-tracker && podman-compose logs"

# 查看服务器资源
ssh root@139.196.155.234 "top -n 1"
ssh root@139.196.155.234 "df -h"
ssh root@139.196.155.234 "free -h"
```

### 6.3 备份策略
- 每日自动备份MySQL数据
- 备份文件保留7天
- 重要版本打Tag备份

## 七、故障排查

### 7.1 常见问题

#### 问题1：SSH连接失败
- 检查服务器防火墙
- 检查SSH密钥权限
- 检查网络连通性

#### 问题2：构建失败
- 检查依赖下载
- 检查构建命令
- 查看详细错误日志

#### 问题3：部署失败
- 检查服务器磁盘空间
- 检查Docker/Podman服务状态
- 检查端口冲突

### 7.2 日志查看
```bash
# 查看流水线日志
在云效控制台查看对应步骤日志

# 查看服务器日志
ssh root@139.196.155.234 "journalctl -u podman"
ssh root@139.196.155.234 "cd /opt/health-tracker && podman-compose logs"
```

## 八、优化建议

### 8.1 性能优化
- 使用缓存加速构建
- 并行执行构建步骤
- 使用镜像仓库缓存

### 8.2 安全优化
- 使用密钥管理服务
- 限制SSH访问IP
- 定期更新依赖包

### 8.3 成本优化
- 使用按量付费服务器
- 优化构建资源使用
- 设置自动启停策略

## 九、后续扩展

### 9.1 多环境部署
- 开发环境
- 测试环境
- 生产环境

### 9.2 自动化测试
- 单元测试
- 集成测试
- 端到端测试

### 9.3 监控告警
- 应用性能监控
- 错误日志监控
- 业务指标监控

---

## 快速开始

1. 按照本文档配置云效流水线
2. 推送代码到main分支触发部署
3. 访问服务验证部署结果

访问地址：
- 后端API: http://139.196.155.234:8080
- PC Web端: http://139.196.155.234
- 小程序H5端: http://139.196.155.234:3101

如有问题，请查看日志或联系维护人员。