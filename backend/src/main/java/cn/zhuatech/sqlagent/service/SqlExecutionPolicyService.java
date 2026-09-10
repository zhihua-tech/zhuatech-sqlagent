/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.sqlagent.service;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 对候选 SQL 执行只读、单语句、表白名单、租户、敏感字段、成本和行数策略。 */
@Service
public class SqlExecutionPolicyService {
    private static final Pattern FORBIDDEN = Pattern.compile(
            "(?i)\\b(insert|update|delete|merge|drop|alter|truncate|grant|revoke|call|execute|copy)\\b");
    private static final Pattern TABLE = Pattern.compile(
            "(?i)\\b(?:from|join)\\s+([a-zA-Z][a-zA-Z0-9_.]{0,127})");
    private static final Pattern FINAL_LIMIT = Pattern.compile("(?i)\\blimit\\s+(\\d+)\\s*$");

    public PolicyResult evaluate(PolicyRequest request) {
        String sql = request.sql().trim();
        String noTrailingSemicolon = sql.replaceFirst(";\\s*$", "").trim();
        String normalized = noTrailingSemicolon.replaceAll("\\s+", " ");
        String lower = normalized.toLowerCase(Locale.ROOT);
        List<String> blockers = new ArrayList<>();
        List<String> actions = new ArrayList<>();
        Set<String> referencedTables = referencedTables(normalized);
        Set<String> allowedTables = new LinkedHashSet<>();
        request.allowedTables().forEach(table -> allowedTables.add(table.toLowerCase(Locale.ROOT)));

        if (!(lower.startsWith("select ") || lower.startsWith("with "))) {
            blockers.add("仅允许 SELECT 或 WITH 查询");
        }
        if (FORBIDDEN.matcher(normalized).find()) blockers.add("SQL 包含写入或管理语句关键字");
        if (noTrailingSemicolon.contains(";")) blockers.add("禁止执行多条 SQL");
        if (sql.contains("--") || sql.contains("/*") || sql.contains("*/")) blockers.add("SQL 注释已被禁用");
        referencedTables.stream().filter(table -> !allowedTables.contains(table.toLowerCase(Locale.ROOT)))
                .forEach(table -> blockers.add("表不在允许清单: " + table));

        if (request.tenantFilterRequired() && !hasTenantPredicate(lower, request)) {
            blockers.add("缺少参数化租户过滤条件");
        }
        if (request.sensitiveColumnsRequested() && !request.purposeApproved()) {
            blockers.add("敏感字段查询用途未获批准");
        }

        String queryHash = hash(normalized);
        if (!blockers.isEmpty()) {
            actions.add("拒绝数据库连接并记录查询哈希与策略命中项");
            return result(Decision.DENY, null, queryHash, referencedTables, blockers, actions);
        }

        if (request.estimatedScanRows() > request.maxScanRows() && !request.explainApproved()) {
            actions.add("先执行 EXPLAIN 并由数据平台主管批准扫描范围");
            return result(Decision.REVIEW, normalized, queryHash, referencedTables, blockers, actions);
        }
        if (request.sensitiveColumnsRequested()) {
            actions.add("对敏感字段执行列级脱敏并记录查询用途");
            return result(Decision.REVIEW, enforceLimit(normalized, request.maxResultRows()), queryHash,
                    referencedTables, blockers, actions);
        }

        Matcher limit = FINAL_LIMIT.matcher(normalized);
        if (!limit.find() || Long.parseLong(limit.group(1)) > request.maxResultRows()) {
            String safeSql = enforceLimit(normalized, request.maxResultRows());
            actions.add("将结果集限制为 " + request.maxResultRows() + " 行后执行");
            return result(Decision.REWRITE_REQUIRED, safeSql, queryHash, referencedTables, blockers, actions);
        }

        actions.add("以只读事务、查询超时和审计上下文执行");
        return result(Decision.ALLOW, normalized, queryHash, referencedTables, blockers, actions);
    }

    private boolean hasTenantPredicate(String lowerSql, PolicyRequest request) {
        String column = Pattern.quote(request.tenantColumn().toLowerCase(Locale.ROOT));
        String parameter = Pattern.quote(request.tenantParameterName().toLowerCase(Locale.ROOT));
        return Pattern.compile("\\b" + column + "\\s*=\\s*:" + parameter + "\\b")
                .matcher(lowerSql).find();
    }

    private Set<String> referencedTables(String sql) {
        Set<String> tables = new LinkedHashSet<>();
        Matcher matcher = TABLE.matcher(sql);
        while (matcher.find()) tables.add(matcher.group(1));
        return tables;
    }

    private String enforceLimit(String sql, int maxRows) {
        Matcher matcher = FINAL_LIMIT.matcher(sql);
        return matcher.find() ? matcher.replaceFirst("LIMIT " + maxRows) : sql + " LIMIT " + maxRows;
    }

    private String hash(String sql) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(sql.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }

    private PolicyResult result(Decision decision, String safeSql, String queryHash,
                                Set<String> tables, List<String> blockers, List<String> actions) {
        return new PolicyResult(decision, safeSql, queryHash, List.copyOf(tables),
                List.copyOf(blockers), List.copyOf(actions));
    }

    public record PolicyRequest(
            @NotBlank String queryId,
            @NotBlank @Size(max = 5000) String sql,
            @NotEmpty Set<@NotBlank String> allowedTables,
            boolean tenantFilterRequired,
            @NotBlank String tenantColumn,
            @NotBlank String tenantParameterName,
            @Min(1) @Max(10_000) int maxResultRows,
            @PositiveOrZero long estimatedScanRows,
            @Positive long maxScanRows,
            boolean sensitiveColumnsRequested,
            boolean purposeApproved,
            boolean explainApproved
    ) {}

    public record PolicyResult(Decision decision, String safeSql, String queryHash,
                               List<String> referencedTables, List<String> blockers,
                               List<String> actions) {}

    public enum Decision { ALLOW, REWRITE_REQUIRED, REVIEW, DENY }
}
