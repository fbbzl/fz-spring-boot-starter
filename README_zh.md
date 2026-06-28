<p align="center">
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21"/>
  <img src="https://img.shields.io/badge/Spring_Boot-3.5.13-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot 3.5.13"/>
  <img src="https://img.shields.io/badge/Maven_Central-1.3.61-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" alt="Maven Central"/>
  <img src="https://img.shields.io/badge/License-Apache_2.0-151515?style=for-the-badge&logo=apache&logoColor=white" alt="License"/>
</p>

<h1 align="center">🚀 fz-spring-boot-starter</h1>

<p align="center">
  <b>让 Spring Boot 开发飞起来</b><br>
  <i>一套精心整合的企业级 Spring Boot 启动器，零套路、全自动</i>
</p>

<p align="center">
  <a href="./README.md">🌐 English</a>
</p>

---

## 🧩 模块矩阵

```
┌──────────────────────────────────────────────────────────────┐
│  📦 fz-spring-boot-starter                                   │
│  ├── ⚙️  core        —— 工具层：异常、函数式、泛型             │
│  ├── 📐 pojo         —— 模型层：Entity / DTO / BO / Mapper    │
│  ├── 🗃️  dal          —— 数据层：CRUD 接口、范围查询、事务      │
│  ├── 🐬 mybatis-plus —— MyBatis-Plus 自动 CRUD                │
│  ├── 🏛️  jpa          —— JPA / Specification 动态查询          │
│  ├── 🌐 web          —— Web 层：通用 CRUD + 统一响应 + Excel   │
│  ├── ⚡ webflux     —— WebFlux 响应式版                       │
│  ├── 🔐 auth        —— JWT 认证                               │
│  ├── 🔴 redisson     —— Redis 限流 / 防重复提交               │
│  ├── 📊 excel        —— EasyExcel 导入导出                    │
│  ├── 📝 audit        —— AOP 操作审计日志                       │
│  ├── 💬 message      —— i18n 国际化                            │
│  └── 🏭 generator    —— 代码生成器（表→代码）                  │
└──────────────────────────────────────────────────────────────┘
```

---

## ⚡ 一分钟接入

```xml
<dependency>
    <groupId>io.github.fbbzl</groupId>
    <artifactId>fz-spring-boot-starter-web</artifactId>
    <version>1.3.78_3.5.15</version>
</dependency>
```

```bash
# 本地构建
git clone https://github.com/fbbzl/fz-spring-boot-starter.git
cd fz-spring-boot-starter
mvn clean install -DskipTests
```

---

## 🔥 硬核能力

### 🎯 通用 CRUD —— 继承即用

```java
@RestController
@RequestMapping("/user")
public class UserController extends BaseCrudController<UserService, UserEntity, UserDTO, UserBO, Long> {
    // ✅ 以下端点自动生效：
    // GET    /{id}    POST  /ids     POST /list
    // POST   /page    POST /tree     POST /
    // PUT    /        DELETE /{id}   DELETE /ids
}
```

### 📦 统一响应 —— 零侵入

```json
{
  "code": 200,
  "success": true,
  "message": "操作成功",
  "data": { ... }
}
```

### 🛑 分布式守护

```java
@RateLimit(permits = 10, timeWindowMillis = 1000, byIp = true)
@SubmitOnce(waitMillis = 3000)
public R<String> submit(@RequestBody Q<DTO> req) {
    return R.success("ok");
}
```

### 📋 审计日志 —— 自动记录

```java
@AuditModule("用户管理")
@AuditMethod(saveParam = true, saveResult = true)
public R<BO> create(@RequestBody Q<DTO> req) { ... }
```

### 🏭 代码生成 —— 一键产出

```yaml
fz:
  generator:
    enable: true
    table-names: sys_user, sys_role
    base-package: com.example.myapp
```

---

## 🛠️ 全家桶

| 技术 | 版本 |
|------|------|
| <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/java/java-original.svg" width="16"/> Java | 21 |
| <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/spring/spring-original.svg" width="16"/> Spring Boot | 3.5.13 |
| <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/mysql/mysql-original.svg" width="16"/> MyBatis-Plus | 3.5.16 |
| <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/redis/redis-original.svg" width="16"/> Redisson | 4.4.0 |
| <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/gradle/gradle-original.svg" width="16"/> EasyExcel | 4.0.3 |
| MapStruct | 1.7.0 |
| crane4j | 2.10.0 |
| Bean Searcher | 4.8.7 |
| Hutool | 5.8.46 |
| Guava | 33.6.0 |
| Sa-Token | 1.45.0 |
| SpringDoc OpenAPI | 2.8.17 |
| Lombok | 1.18.46 |

---

## 📜 License

[Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0)

<p align="center">
  <sub>Made with ❤️ by <a href="https://github.com/fbbzl">fengbinbin</a></sub>
</p>
