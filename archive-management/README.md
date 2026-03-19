# 电子档案管理系统

## 项目介绍

电子档案管理系统是一个基于Spring Boot和Vue3的现代化档案管理平台，旨在实现档案的采集、挂接、校验和日志管理等功能，提高档案管理的效率和准确性。

## 技术栈

### 后端
- **Java**：JDK 17
- **框架**：Spring Boot 3.2.0
- **ORM**：MyBatis-Plus 3.5.5
- **数据库**：MySQL 8.0.33
- **API文档**：SpringDoc OpenAPI 2.3.0
- **构建工具**：Maven
- **其他**：Lombok、JJWT、Commons IO

### 前端
- **框架**：Vue 3.3.4
- **UI组件**：Element Plus 2.4.2
- **路由**：Vue Router 4.2.5
- **状态管理**：Pinia 2.1.7
- **HTTP客户端**：Axios 1.6.2
- **构建工具**：Vite 5.0.0
- **图表库**：ECharts 6.0.0

## 项目结构

### 后端项目结构
```
archive-management/src/
├── main/java/com/electronic/archive/
│   ├── controller/       # 控制器层
│   ├── service/          # 服务层
│   ├── service/impl/     # 服务实现层
│   ├── mapper/           # 数据访问层
│   ├── entity/           # 实体类
│   ├── dto/              # 数据传输对象
│   ├── vo/               # 视图对象
│   ├── config/           # 配置类
│   ├── exception/        # 异常处理
│   ├── utils/            # 工具类
│   └── constant/         # 常量定义
└── main/resources/       # 配置文件
```

### 前端项目结构
```
archive-management-frontend/src/
├── components/       # 公共组件
├── views/            # 视图页面
├── api/              # API请求
├── utils/            # 工具类
├── router/           # 路由配置
├── store/            # 状态管理
├── assets/           # 静态资源
├── App.vue           # 根组件
└── main.js           # 入口文件
```

## 功能模块

### 1. 档案采集模块
- 自动接口采集
- 手动上传采集
- 批量上传采集
- 外部导入采集

### 2. 挂接管理模块
- 自动挂接
- 手动挂接
- 批量挂接
- 解除挂接
- 挂接重试

### 3. 校验管理模块
- 档案完整性校验
- 档案一致性校验
- 档案合规性校验

### 4. 日志管理模块
- 挂接日志管理
- 采集日志管理
- 系统日志管理

## 快速开始

### 环境要求
- JDK 17+
- MySQL 8.0+
- Maven 3.6+
- Node.js 16+

### 后端配置

1. 克隆项目
```bash
git clone <项目地址>
cd archive-management
```

2. 配置数据库
编辑 `src/main/resources/application.yml` 文件，修改数据库连接信息：
```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/archive_management?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
```

3. 构建项目
```bash
mvn clean compile
```

4. 运行项目
```bash
mvn spring-boot:run
```

### 前端配置

1. 进入前端目录
```bash
cd archive-management-frontend
```

2. 安装依赖
```bash
npm install
```

3. 启动开发服务器
```bash
npm run dev
```

## API文档

项目使用SpringDoc OpenAPI生成API文档，启动项目后可访问：
```
http://localhost:8080/archive/swagger-ui/index.html
```

## 数据库设计

### 主要表结构

#### archive_info（档案信息表）
| 字段名 | 类型 | 描述 |
| --- | --- | --- |
| id | bigint | 档案ID |
| archive_code | varchar(50) | 档案编码 |
| archive_name | varchar(255) | 档案名称 |
| archive_type | int | 档案类型 |
| file_path | varchar(255) | 文件路径 |
| file_size | bigint | 文件大小 |
| metadata | text | 元数据 |
| system_code | varchar(50) | 所属系统编码 |
| status | int | 档案状态 |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |

#### hang_on_log（挂接日志表）
| 字段名 | 类型 | 描述 |
| --- | --- | --- |
| id | bigint | 日志ID |
| archive_id | bigint | 档案ID |
| hang_on_type | int | 挂接类型 |
| status | int | 挂接状态 |
| operator | varchar(50) | 操作人 |
| hang_on_way | varchar(50) | 挂接方式 |
| target_system | varchar(50) | 目标系统 |
| remark | varchar(255) | 备注 |
| error_info | text | 错误信息 |
| create_time | datetime | 创建时间 |

#### interface_config（接口配置表）
| 字段名 | 类型 | 描述 |
| --- | --- | --- |
| id | bigint | 配置ID |
| interface_name | varchar(100) | 接口名称 |
| interface_url | varchar(255) | 接口URL |
| request_method | varchar(10) | 请求方法 |
| request_params | text | 请求参数 |
| headers | text | 请求头 |
| status | int | 状态 |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |

## 开发指南

### 代码规范
- 遵循Spring Boot和MyBatis-Plus的最佳实践
- 使用Lombok简化实体类的编写
- 统一异常处理和响应格式
- 代码注释清晰，关键方法和类必须有注释

### 提交规范
- 提交信息要清晰明了，说明修改的内容
- 使用英文或中文提交信息，保持一致性
- 定期进行代码评审

## 许可证

本项目采用MIT许可证，详见LICENSE文件。

## 联系方式

如有问题或建议，请联系项目负责人：
- 邮箱：contact@example.com
- 电话：138-0000-0000

---

© 2026 电子档案管理系统