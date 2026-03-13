# 健康管理系统部署步骤

以下步骤在服务器 `139.196.155.234` 上完成，默认使用 root 账号与 SSH 私钥登录。

## 1. SSH 登录
```bash
ssh -i /Users/shuaiqideguigegegegege/.ssh/health_tracker_deploy root@139.196.155.234
```

## 2. 安装基础工具与容器运行时
```bash
dnf -y install git podman podman-docker podman-compose
```

## 3. 获取代码
```bash
mkdir -p /opt
cd /opt
git clone https://github.com/Mrwu-gui/health-tracker.git
cd /opt/health-tracker
```

## 4. 确认容器编排文件与 Dockerfile
- `docker-compose.yml` 在 `/opt/health-tracker`
- 三个服务目录:
  - `/opt/health-tracker/health-tracker-java`
  - `/opt/health-tracker/health-tracker-web`
  - `/opt/health-tracker/health-tracker-miniapp`

## 5. 启动服务
首次启动或更新代码后:
```bash
cd /opt/health-tracker
podman-compose up -d --build
```

如需重建并清理旧容器:
```bash
cd /opt/health-tracker
podman-compose down
podman-compose up -d --build
```

## 6. 验证服务
```bash
curl -i http://127.0.0.1:8080/api/health
curl -I http://127.0.0.1/
curl -I http://127.0.0.1:3101/
```

## 7. 外网访问地址
- PC Web: http://139.196.155.234
- 小程序 H5: http://139.196.155.234:3101
- 后端 API: http://139.196.155.234:8080

## 8. 常用运维命令
查看容器:
```bash
podman ps --format 'table {{.Names}}\t{{.Status}}\t{{.Ports}}'
```

查看日志(示例: 后端):
```bash
podman logs -f health-tracker_backend_1
```

重启单个容器(示例: 后端):
```bash
podman restart health-tracker_backend_1
```
