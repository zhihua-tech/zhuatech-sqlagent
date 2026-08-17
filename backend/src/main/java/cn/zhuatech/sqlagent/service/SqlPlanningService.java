/* Copyright 2026 上海如静知华信息科技有限公司 */
package cn.zhuatech.sqlagent.service;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** 将自然语言查询意图转换为可审计的 SQL 执行计划，社区版不连接外部模型。 */
@Service
public class SqlPlanningService {
    private static final Pattern SAFE_IDENTIFIER = Pattern.compile("[A-Za-z][A-Za-z0-9_]{0,63}");

    public Result plan(Request request) {
        int risk = request.writeIntent() ? 60 : 8;
        List<String> controls = new ArrayList<>(List.of("强制租户条件", "默认只读事务", "结果集脱敏"));
        if (request.containsSensitiveData()) { risk += 22; controls.add("敏感字段须经字段级授权"); }
        if (request.estimatedRows() > 100_000) { risk += 18; controls.add("先执行 EXPLAIN 并限制扫描分区"); }
        else if (request.estimatedRows() > 10_000) { risk += 8; controls.add("结果行数限制为 1,000"); }
        if (request.requestedTables().size() > 4) { risk += 10; controls.add("跨主题查询由数据平台主管复核"); }
        boolean invalidIdentifier = request.requestedTables().stream().anyMatch(t -> !SAFE_IDENTIFIER.matcher(t).matches());
        if (invalidIdentifier) { risk = 100; controls.add("检测到非法表标识，禁止生成 SQL"); }
        risk = Math.min(100, risk);
        String decision = request.writeIntent() || invalidIdentifier ? "BLOCK" :
            request.containsSensitiveData() || risk >= 45 ? "REVIEW" : "PASS";
        String table = request.requestedTables().getFirst();
        String sql = "BLOCK".equals(decision) ? "-- blocked by SQL guard" :
            "SELECT * FROM " + table + " WHERE tenant_id = :tenantId LIMIT " + Math.min(1000, Math.max(50, request.resultLimit()));
        List<String> reasons = new ArrayList<>();
        reasons.add("问题意图：" + request.question());
        reasons.add("预计扫描 " + request.estimatedRows() + " 行，涉及 " + request.requestedTables().size() + " 张表");
        if (request.containsSensitiveData()) reasons.add("查询涉及敏感字段");
        if (request.writeIntent()) reasons.add("社区版禁止 AI 直接生成写操作");
        return new Result(request.datasourceCode(), risk, risk >= 70 ? "HIGH" : risk >= 35 ? "MEDIUM" : "LOW",
            decision, sql, controls, reasons);
    }

    public record Request(@NotBlank String datasourceCode, @NotBlank @Size(max = 300) String question,
                          @NotEmpty @Size(max = 8) List<@NotBlank String> requestedTables,
                          @Min(0) long estimatedRows, boolean containsSensitiveData,
                          boolean writeIntent, @Min(1) @Max(1000) int resultLimit) {}
    public record Result(String datasourceCode, int riskScore, String riskTier, String decision,
                         String sqlPreview, List<String> controls, List<String> reasons) {}
}
