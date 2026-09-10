# 企业 Text-to-SQL 执行策略

Copyright © 2026 上海如静知华信息科技有限公司 · <https://www.zhuatech.cn/>

`POST /api/enterprise/sqlagent/execution-policy` 对模型或规则生成的候选 SQL 进行真正的执行前检查：仅允许只读单语句，禁止注释和写入关键字，解析 `FROM/JOIN` 表并匹配白名单，强制参数化租户条件，同时控制敏感字段用途、预计扫描量和结果行数。

接口返回 `ALLOW / REWRITE_REQUIRED / REVIEW / DENY`。缺少或超出 `LIMIT` 时生成安全 SQL；高扫描量或已批准的敏感查询进入人工复核；写语句、多语句、越权表、缺少租户条件或未经批准的敏感字段会直接拒绝。

每个查询生成 SHA-256 哈希以关联审计、审批和执行记录。该轻量策略层不替代数据库权限，生产环境仍应使用只读账号、连接超时、行列级安全和数据库原生审计。
