# 健康管理系统部署配置清单

本文记录当前部署使用的关键配置、端口、账户、服务地址与重要路径，方便后续运维与排查。

## 服务器
- 公网 IP: 139.196.155.234
- 私网 IP: 172.24.51.229
- SSH 用户: root
- SSH 登录方式: 私钥
- 私钥路径(本机): /Users/shuaiqideguigegegegege/.ssh/health_tracker_deploy

## 代码与部署目录
- 服务器部署目录: /opt/health-tracker
- 容器编排文件: /opt/health-tracker/docker-compose.yml

## 服务与端口
- 后端 API: http://139.196.155.234:8080
- PC Web: http://139.196.155.234
- 小程序 H5: http://139.196.155.234:3101

## 数据库与缓存
- MySQL 版本: 8.3
- MySQL 数据库名: health_tracker
- MySQL 账号: root
- MySQL 密码: root
- 数据目录(宿主机): /opt/health-tracker/docker/mysql
- 初始化 SQL: /opt/health-tracker/health-tracker-java/src/main/resources/db

- Redis 版本: 7
- Redis 服务地址(容器): redis

## 容器与镜像
- 运行时: Podman
- 编排: podman-compose
- 镜像源: docker.m.daocloud.io
- 容器:
  - health-tracker_mysql_1
  - health-tracker_redis_1
  - health-tracker_backend_1
  - health-tracker_web_1
  - health-tracker_miniapp_1

## 后端环境变量
- SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/health_tracker?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
- SPRING_DATASOURCE_USERNAME=root
- SPRING_DATASOURCE_PASSWORD=root
- SPRING_DATA_REDIS_HOST=redis

## 微信与短信
- 微信小程序 AppID: `<YOUR_WECHAT_APPID>`
- 微信小程序 AppSecret: `<YOUR_WECHAT_APPSECRET>`
- 手机号登录: 阿里云短信（需在服务器或环境变量中配置 AccessKey/Secret）

## 说明
- 当前未绑定域名，直接使用公网 IP 访问。
- 所有配置均在 docker-compose 中集中管理。
