/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.sqlagent.service;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

/**
 * 将通过语法门禁的查询进一步纳入审批、血缘、脱敏和运行时限额。
 *
 * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
 */
@Service
public class SqlQueryReleaseService {
    /**
     * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
     */
    public ReleaseResult release(ReleaseRequest request) {
        List<String> blockers = new ArrayList<>();
        List<String> reviewReasons = new ArrayList<>();
        List<String> controls = new ArrayList<>();

        if (request.policyDecision() == PolicyDecision.DENY) blockers.add("SQL 执行策略已拒绝该查询");
        if (!request.validationPassed()) blockers.add("查询验证或回归样例未通过");
        if (!request.lineageComplete()) blockers.add("结果字段与来源表血缘不完整");
        if (!request.parametersBound()) blockers.add("查询仍包含未绑定参数");
        if (request.timeoutSeconds() > request.maxTimeoutSeconds()) blockers.add("查询超时设置超过平台上限");
        if (request.estimatedResultRows() > request.maxResultRows()) blockers.add("预计结果行数超过发布上限");
        if (request.sensitiveResult() && !request.maskingVerified()) blockers.add("敏感结果脱敏未验证");

        if (request.policyDecision() == PolicyDecision.REVIEW
                || request.policyDecision() == PolicyDecision.REWRITE_REQUIRED) {
            reviewReasons.add("上游执行策略要求复核或改写");
        }
        if (request.requester().equalsIgnoreCase(request.approver())) {
            reviewReasons.add("申请人与审批人未实现职责分离");
        }
        if (request.scheduled() && (request.approvalTicket() == null || request.approvalTicket().isBlank())) {
            reviewReasons.add("定时查询缺少有效审批单号");
        }
        if (request.estimatedScanRows() > request.maxScanRows()) {
            reviewReasons.add("预计扫描行数超过常规阈值");
        }

        String fingerprint = fingerprint(request.queryId() + "|" + request.normalizedSql()
                + "|" + request.schemaVersion());
        controls.add("使用只读事务、" + request.timeoutSeconds() + " 秒超时和 "
                + request.maxResultRows() + " 行结果上限");
        controls.add("记录查询指纹、申请人、审批人、用途和数据血缘");
        if (request.sensitiveResult()) controls.add("应用列级脱敏并禁止原始结果进入模型上下文");

        if (!blockers.isEmpty()) return result(Decision.REJECT, fingerprint, blockers, reviewReasons, controls);
        if (!reviewReasons.isEmpty()) return result(Decision.REVIEW, fingerprint, blockers, reviewReasons, controls);
        return result(Decision.APPROVE, fingerprint, blockers, reviewReasons, controls);
    }

    /**
     * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
     */
    private ReleaseResult result(Decision decision, String fingerprint, List<String> blockers,
                                 List<String> reviewReasons, List<String> controls) {
        return new ReleaseResult(decision, fingerprint, List.copyOf(blockers),
                List.copyOf(reviewReasons), List.copyOf(controls));
    }

    /**
     * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
     */
    private String fingerprint(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }

    /**
     * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
     */
    public record ReleaseRequest(@NotBlank String queryId, @NotBlank String normalizedSql,
            @NotBlank String schemaVersion, @NotNull PolicyDecision policyDecision,
            @NotBlank String requester, @NotBlank String approver, String approvalTicket,
            boolean scheduled, boolean validationPassed, boolean lineageComplete,
            boolean parametersBound, boolean sensitiveResult, boolean maskingVerified,
            @Min(1) @Max(3600) int timeoutSeconds, @Min(1) @Max(3600) int maxTimeoutSeconds,
            @PositiveOrZero long estimatedResultRows, @Positive long maxResultRows,
            @PositiveOrZero long estimatedScanRows, @Positive long maxScanRows) {}

    /**
     * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
     */
    public record ReleaseResult(Decision decision, String releaseFingerprint,
            List<String> blockers, List<String> reviewReasons, List<String> runtimeControls) {}
    /**
     * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
     */
    public enum PolicyDecision { ALLOW, REWRITE_REQUIRED, REVIEW, DENY }
    /**
     * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
     */
    public enum Decision { APPROVE, REVIEW, REJECT }
}
