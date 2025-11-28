# 仓库管理系统 - 前端

Vue 3 + TypeScript + Vite 项目

## 🚀 快速启动

### 安装依赖

```bash
npm install
```

**如果遇到权限问题**：
- 使用CMD（命令提示符）而不是PowerShell
- 或者运行：`npm config set cache "%USERPROFILE%\AppData\Roaming\npm-cache"`

### 启动开发服务器

```bash
npm run serve
```

访问：**http://localhost:3000**

## 📋 项目结构

```
frontend/
├── src/
│   ├── api/          # API调用
│   ├── pages/        # 页面组件
│   ├── router/       # 路由配置
│   ├── stores/       # Pinia状态管理
│   ├── types/        # TypeScript类型
│   ├── App.vue       # 根组件
│   └── main.ts       # 入口文件
├── package.json
└── vite.config.ts    # Vite配置（含代理）
```

## ⚙️ 配置说明

- **开发服务器端口**: 3000
- **后端API地址**: http://localhost:8080
- **API代理**: `/api` → `http://localhost:8080`（自动配置）

## 📝 命令说明

- `npm run serve` - 启动开发服务器（端口3000）
- `npm run dev` - 功能相同
- `npm run build` - 构建生产版本
- `npm run preview` - 预览构建后的版本










