/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.sqlagent;

import cn.zhuatech.sqlagent.service.SqlExecutionPolicyService;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
 */
class SqlExecutionPolicyServiceTests {
    private final SqlExecutionPolicyService service = new SqlExecutionPolicyService();

    /**
     * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
     */
    @Test
    void allowsScopedReadOnlyQuery() {
        var result = service.evaluate(request(
                "SELECT id, amount FROM orders WHERE tenant_id = :tenantId LIMIT 100",
                Set.of("orders"), 1000, false, true, false));
        assertThat(result.decision()).isEqualTo(SqlExecutionPolicyService.Decision.ALLOW);
        assertThat(result.queryHash()).hasSize(64);
    }

    /**
     * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
     */
    @Test
    void rewritesMissingOrExcessiveLimit() {
        var result = service.evaluate(request(
                "SELECT id FROM orders WHERE tenant_id = :tenantId",
                Set.of("orders"), 1000, false, true, false));
        assertThat(result.decision()).isEqualTo(SqlExecutionPolicyService.Decision.REWRITE_REQUIRED);
        assertThat(result.safeSql()).endsWith("LIMIT 500");
    }

    /**
     * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
     */
    @Test
    void reviewsExpensiveOrSensitiveQuery() {
        var result = service.evaluate(request(
                "SELECT customer_name FROM orders WHERE tenant_id = :tenantId LIMIT 100",
                Set.of("orders"), 2_000_000, true, true, false));
        assertThat(result.decision()).isEqualTo(SqlExecutionPolicyService.Decision.REVIEW);
        assertThat(result.actions()).contains("先执行 EXPLAIN 并由数据平台主管批准扫描范围");
    }

    /**
     * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
     */
    @Test
    void deniesWriteAndMultipleStatements() {
        var result = service.evaluate(request(
                "UPDATE orders SET amount = 0; SELECT * FROM orders WHERE tenant_id = :tenantId",
                Set.of("orders"), 10, false, true, true));
        assertThat(result.decision()).isEqualTo(SqlExecutionPolicyService.Decision.DENY);
        assertThat(result.blockers()).contains("SQL 包含写入或管理语句关键字", "禁止执行多条 SQL");
    }

    /**
     * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
     */
    @Test
    void deniesCrossDomainTableAndMissingTenantFilter() {
        var result = service.evaluate(request(
                "SELECT * FROM payroll LIMIT 100",
                Set.of("orders"), 10, true, false, true));
        assertThat(result.decision()).isEqualTo(SqlExecutionPolicyService.Decision.DENY);
        assertThat(result.blockers()).hasSize(3);
    }

    /**
     * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
     */
    private SqlExecutionPolicyService.PolicyRequest request(
            String sql, Set<String> allowedTables, long estimatedRows, boolean sensitive,
            boolean purposeApproved, boolean explainApproved) {
        return new SqlExecutionPolicyService.PolicyRequest("Q-100", sql, allowedTables,
                true, "tenant_id", "tenantId", 500, estimatedRows, 1_000_000,
                sensitive, purposeApproved, explainApproved);
    }
}
