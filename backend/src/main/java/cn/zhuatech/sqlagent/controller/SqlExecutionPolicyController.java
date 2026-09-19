/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.sqlagent.controller;

import cn.zhuatech.sqlagent.common.ApiResponse;
import cn.zhuatech.sqlagent.service.SqlExecutionPolicyService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
 */
@RestController
@RequestMapping("/api/enterprise/sqlagent")
public class SqlExecutionPolicyController {
    private final SqlExecutionPolicyService service;

    /**
     * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
     */
    public SqlExecutionPolicyController(SqlExecutionPolicyService service) {
        this.service = service;
    }

    /**
     * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
     */
    @PostMapping("/execution-policy")
    public ApiResponse<SqlExecutionPolicyService.PolicyResult> evaluate(
            @Valid @RequestBody SqlExecutionPolicyService.PolicyRequest request) {
        return ApiResponse.ok("SQL 执行策略评估完成", service.evaluate(request));
    }
}
