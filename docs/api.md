# SQLAgent API

版权所有 © 2026 上海如静知华信息科技有限公司。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/auth/login` | 登录并获取 JWT |
| GET | `/api/admin/dashboard` | 查询治理控制台 |
| GET | `/api/admin/work-orders` | 查询任务列表 |
| GET | `/api/shopfloor/dashboard` | 分析师工作台 |
| POST | `/api/shopfloor/work-orders/{id}/reports` | 提交查询反馈 |
| POST | `/api/ai/sql/plan` | 生成 SQL 预览、风险和控制措施 |
| POST | `/api/shopfloor/ai-risk-assessment` | AI 上线风险初筛 |
| POST | `/api/enterprise/sqlagent/execution-policy` | 校验候选 SQL 的权限、租户、成本和结果限制 |

除登录外均需 Bearer Token。`/api/ai/sql/plan` 接受数据源、问题、候选表、预计扫描行数、敏感标记、写意图和结果限制；社区版不连接真实数据库。
