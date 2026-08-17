# SQLAgent 架构

版权所有 © 2026 上海如静知华信息科技有限公司。

Vue 3 管理端和 H5 工作台通过 JWT 调用 Spring Boot API。`SqlPlanningService` 在应用层执行标识符白名单、写意图阻断、敏感字段复核、扫描成本控制和租户条件注入；任务、复核与资源台账由 JPA、MySQL 和 Flyway 管理。

生产落地应在 SQLAgent 与数据库之间增加企业 SSO、细粒度数据权限、查询沙箱、审计日志和资源限额，禁止模型凭据或用户输入绕过安全策略。
