# 云效流水线部署（Java / Web / Uniapp 分开）

下面的方式是用云效流水线触发脚本，分别部署 **后端 / PC 端 / 小程序 H5**。
部署目标服务器：`/opt/health-tracker`（已是容器化部署，服务器使用 podman-compose）。

## 1. 云效变量准备
在云效项目的「流水线变量」配置以下变量：

- `SSH_HOST` = 139.196.155.234
- `SSH_USER` = root
- `SSH_PRIVATE_KEY` = 你的私钥内容（多行）
- `APP_DIR` = /opt/health-tracker

> `SSH_PRIVATE_KEY` 请直接粘贴私钥内容（包含 BEGIN/END），云效会以安全变量保存。

## 2. 流水线步骤（Shell）

### 通用准备步骤（每个 Job 前执行）
```bash
mkdir -p ~/.ssh
echo "$SSH_PRIVATE_KEY" > ~/.ssh/id_rsa
chmod 600 ~/.ssh/id_rsa
export SSH_HOST SSH_USER APP_DIR
```

### Job 1：部署后端
```bash
bash ./scripts/deploy_backend.sh
```

### Job 2：部署 PC 端
```bash
bash ./scripts/deploy_web.sh
```

### Job 3：部署小程序 H5
```bash
bash ./scripts/deploy_miniapp.sh
```

## 3. 说明
1. 这些脚本会在服务器上执行：
   - `git pull`
   - `podman-compose up -d --build <service>`
2. 因为三端分开部署，所以可以单独触发某个 Job。
3. 若你希望「只部署某个分支」，可以在云效流水线里限制触发分支。

## 4. 服务检查
```bash
curl -I http://139.196.155.234
curl -I http://139.196.155.234:3101
curl -I http://139.196.155.234:8080/api/health
```
