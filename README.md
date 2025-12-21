# 项目效果

## 登录页面

![image-20251221035242437](https://img.fz688.dpdns.org/2025-12-21-1766260362615.png)

## 文章列表

![image-20251221035455094](https://img.fz688.dpdns.org/2025-12-21-1766260495292.png)

## 发表文章


![image-20251221035631286](https://img.fz688.dpdns.org/2025-12-21-1766260591418.png)

## 用户管理

![image-20251221035957543](https://img.fz688.dpdns.org/2025-12-21-1766260797672.png)

## 栏目管理

![image-20251221040234767](https://img.fz688.dpdns.org/2025-12-21-1766260954921.png)

## 数据统计

![image-20251221040935469](https://img.fz688.dpdns.org/2025-12-21-1766261375610.png)



# WorkingBlog - 个人博客系统

## 项目简介

WorkingBlog 是一个基于 Spring Boot + Vue.js 的全栈博客系统，提供完整的博客文章管理、用户管理等功能

## 技术栈

### 后端技术
- **Spring Boot 2** - 主框架
- **MyBatis** - 持久层框架
- **Spring Security** - 安全框架
- **MySQL 8.0** - 数据库

### 前端技术
- **Vue.js** - 前端框架 
- **Element UI** - UI 组件库 
- **Vue Router** - 路由管理
- **Axios** - HTTP 客户端 
- **Mavon Editor** - Markdown 编辑器 
- **ECharts** - 数据可视化 

## 主要功能

### 文章管理
- 文章发布（支持 Markdown 格式） 
- 文章分类管理
- 文章标签管理
- 草稿箱功能
- 文章浏览量统计 

### 用户系统
- 用户注册与登录
- 角色权限管理 
- 用户头像和个人信息管理

### 数据统计
- 访问量统计（PV）
- 数据可视化展示 

## 项目结构

```
WorkingBlog/
├── src/                          # 后端代码
│   └── main/
│       ├── java/com/iblog/      # Java 源代码
│       │   ├── bean/            # 实体类
│       │   ├── config/          # 配置类
│       │   ├── controller/      # 控制器
│       │   ├── mapper/          # MyBatis Mapper
│       │   ├── service/         # 服务层
│       │   └── utils/           # 工具类
│       └── resources/           # 资源文件
│ 			├── com/iblog/mapper # Mapper XML
│           ├── application.yml  # 应用配置
│           └── vueblog.sql      # 数据库脚本
├── vueblog/                     # 前端代码
│   ├── src/                     # Vue 源代码
│   ├── config/                  # 配置文件
│   └── package.json             # 依赖管理
└── pom.xml                      # Maven 配置
```

## 环境要求

- **JDK**: 17 或更高版本
- **Maven**: 3.0+
- **MySQL**: 8.0+
- **Node.js**: 4.0+ 
- **npm**: 3.0+

## 快速开始

### 1. 数据库配置

创建数据库并导入 SQL 文件：
```bash
mysql -u root -p < src/main/resources/vueblog.sql
```

### 2. 后端配置

修改 `src/main/resources/application.yml` 中的数据库连接

### 3. 启动后端服务

```bash
# 进入项目目录  
cd WorkingBlog  
  
# 编译打包  
mvn clean package  
  
# 运行应用  
java -jar target/blogserver-0.0.1-SNAPSHOT.jar
```

后端服务将在 **8081** 端口启动

### 4. 启动前端服务

```bash
# 进入前端目录  
cd vueblog
# 安装依赖  
npm install
# 启动开发服务器  
npm run dev
```

## 配置说明

### 数据库连接池配置
项目使用 Druid 连接池，可在配置文件中调整连接池参数

默认：

- 初始连接数：5
- 最小空闲连接：5
- 最大活跃连接：20
- 最大等待时间：600000ms

### 日志配置
日志级别和输出格式可在配置文件中自定义

默认：

- 根日志级别：INFO
- 应用日志级别：DEBUG
- 日志文件：`logs/app.log`

## 注意事项

1. 首次运行请确保数据库已正确创建
2. 修改配置文件中的数据库密码
3. 确保 MySQL 时区设置正确 
4. 主要依赖版本见 `pom.xml` 

## 贡献

欢迎提交 Issue 和 Pull Request 来改进项目。

