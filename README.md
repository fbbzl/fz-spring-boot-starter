<p align="center">
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21"/>
  <img src="https://img.shields.io/badge/Spring_Boot-3.5.13-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot 3.5.13"/>
  <img src="https://img.shields.io/badge/Maven_Central-1.3.61-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" alt="Maven Central"/>
  <img src="https://img.shields.io/badge/License-Apache_2.0-151515?style=for-the-badge&logo=apache&logoColor=white" alt="License"/>
</p>

<h1 align="center">🚀 fz-spring-boot-starter</h1>

<p align="center">
  <b>Supercharge Your Spring Boot Development</b><br>
  <i>A curated suite of enterprise-grade Spring Boot starters — zero boilerplate, full auto</i>
</p>

<p align="center">
  <a href="./README_zh.md">🌐 Chinese</a>
</p>

---

## 🧩 Module Matrix

```
┌──────────────────────────────────────────────────────────────┐
│  📦 fz-spring-boot-starter                                   │
│  ├── ⚙️  core        —— Utilities: exceptions, functions     │
│  ├── 📐 pojo         —— Models: Entity / DTO / BO / Mapper   │
│  ├── 🗃️  dal          —— Data layer: CRUD interface, queries │
│  ├── 🐬 mybatis-plus —— MyBatis-Plus auto CRUD               │
│  ├── 🏛️  jpa          —— JPA / Specification dynamic queries │
│  ├── 🌐 web          —— Web: generic CRUD + unified response │
│  ├── ⚡ webflux     —— WebFlux reactive variant              │
│  ├── 🔐 auth        —— JWT authentication                    │
│  ├── 🔴 redisson     —— Redis rate-limit / anti-duplicate    │
│  ├── 📊 excel        —— EasyExcel import / export            │
│  ├── 📝 audit        —— AOP operation audit logging          │
│  ├── 💬 message      —— i18n internationalization            │
│  └── 🏭 generator    —— Code generator (table → code)        │
└──────────────────────────────────────────────────────────────┘
```

---

## ⚡ 60-Second Setup

```xml
<dependency>
    <groupId>io.github.fbbzl</groupId>
    <artifactId>fz-spring-boot-starter-web</artifactId>
    <version>1.3.76_3.5.15</version>
</dependency>
```

```bash
# Build from source
git clone https://github.com/fbbzl/fz-spring-boot-starter.git
cd fz-spring-boot-starter
mvn clean install -DskipTests
```

---

## 🔥 Core Powers

### 🎯 Generic CRUD —— Extend & Done

```java
@RestController
@RequestMapping("/user")
public class UserController extends BaseCrudController<UserService, UserEntity, UserDTO, UserBO, Long> {
    // ✅ Auto-enabled endpoints:
    // GET  /{id}    POST  /ids   POST /list
    // POST /page    POST /tree   POST /
    // PUT  /        DELETE/{id}  DELETE /ids
}
```

### 📦 Unified Response —— Zero Intrusion

```json
{
  "code": 200,
  "success": true,
  "message": "Success",
  "data": { ... }
}
```

### 🛑 Distributed Guardians

```java
@RateLimit(permits = 10, timeWindowMillis = 1000, byIp = true)
@SubmitOnce(waitMillis = 3000)
public R<String> submit(@RequestBody Q<DTO> req) {
    return R.success("ok");
}
```

### 📋 Audit Logging —— Auto Track

```java
@AuditModule("User Management")
@AuditMethod(saveParam = true, saveResult = true)
public R<BO> create(@RequestBody Q<DTO> req) { ... }
```

### 🏭 Code Generator —— One-Click Output

```yaml
fz:
  generator:
    enable: true
    table-names: sys_user, sys_role
    base-package: com.example.myapp
```

---

## 🛠️ Tech Stack

| Technology | Version |
|------------|---------|
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

