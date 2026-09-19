/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.sqlagent;

import cn.zhuatech.sqlagent.service.SqlQueryReleaseService;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class SqlQueryReleaseServiceTests {
    private final SqlQueryReleaseService service = new SqlQueryReleaseService();

    @Test void approvesGovernedQueryRelease() {
        var result = service.release(request(SqlQueryReleaseService.PolicyDecision.ALLOW,
                "analyst", "data-owner", false, true, true, 1_000, 20_000));
        assertThat(result.decision()).isEqualTo(SqlQueryReleaseService.Decision.APPROVE);
        assertThat(result.releaseFingerprint()).hasSize(64);
        assertThat(result.runtimeControls()).hasSize(3);
    }

    @Test void routesIncompleteSeparationAndLargeScanToReview() {
        var result = service.release(request(SqlQueryReleaseService.PolicyDecision.REVIEW,
                "analyst", "analyst", true, true, true, 1_000, 2_000_000));
        assertThat(result.decision()).isEqualTo(SqlQueryReleaseService.Decision.REVIEW);
        assertThat(result.reviewReasons()).hasSize(4);
    }

    @Test void rejectsUnvalidatedOrUnmaskedQuery() {
        var result = service.release(request(SqlQueryReleaseService.PolicyDecision.DENY,
                "analyst", "owner", false, false, false, 50_000, 100));
        assertThat(result.decision()).isEqualTo(SqlQueryReleaseService.Decision.REJECT);
        assertThat(result.blockers()).contains("SQL 执行策略已拒绝该查询", "查询验证或回归样例未通过",
                "敏感结果脱敏未验证", "预计结果行数超过发布上限");
    }

    private SqlQueryReleaseService.ReleaseRequest request(SqlQueryReleaseService.PolicyDecision policyDecision,
            String requester, String approver, boolean scheduled, boolean validationPassed,
            boolean maskingVerified, long resultRows, long scanRows) {
        return new SqlQueryReleaseService.ReleaseRequest("Q-200",
                "SELECT customer_name FROM orders WHERE tenant_id = :tenantId LIMIT 100", "sales-v3",
                policyDecision, requester, approver, scheduled ? "" : "APP-10", scheduled,
                validationPassed, true, true, true, maskingVerified, 30, 60,
                resultRows, 10_000, scanRows, 1_000_000);
    }
}
