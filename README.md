# 电子商务网站项目

## 项目简介
基于Spring Boot的电子商务平台。系统包含完整的购物流程，支持用户注册、商品浏览、购物车管理、订单处理和卖家管理等功能。

## 技术栈
- **后端**: Spring Boot 4.0, Spring MVC, Spring Data JPA, Spring Security
- **数据库**: H2内存数据库
- **模板引擎**: Thymeleaf
- **安全**: Spring Security + BCrypt加密
- **构建工具**: Maven
- **Java版本**: JDK 17+

## 功能特性
1. **用户管理**: 注册、登录、注销，支持买家/卖家角色
2. **商品管理**: 商品展示、搜索、卖家商品增删改查
3. **购物车**: 添加、修改、删除商品，实时计算总价
4. **订单系统**: 下单、支付、订单状态跟踪、邮件通知
5. **卖家中心**: 商品管理、订单处理、销售报表、客户管理
6. **权限控制**: 基于角色的访问控制（买家、卖家）

## 快速开始

### 环境要求
- 使用docker部署
- JDK 17或更高版本
- Maven 3.6+
- 无需安装外部数据库

### 运行步骤
1. **克隆项目**
   ```bash
   git clone https://github.com/Mutsum1n/ecommerce
   cd ecommerce
2. **配置环境变量**
    ```bash
    cat > .env << EOF
    DB_PASSWORD=your_db_password
    MAIL_USERNAME=your_e-mail
    MAIL_PASSWORD=your_password
    APP_URL=your_app_url
    TZ=Asia/Shanghai
    # your_password是你的邮箱SMTP服务授权码
    EOF
3. **构建镜像运行容器**
   ```bash
   docker compose -d --build

## 学生信息
- 姓名:赖裕安  
- 学号:202330450821
