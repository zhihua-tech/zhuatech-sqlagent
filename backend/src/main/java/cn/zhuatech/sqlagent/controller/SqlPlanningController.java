/* Copyright 2026 上海如静知华信息科技有限公司 */
package cn.zhuatech.sqlagent.controller;

import cn.zhuatech.sqlagent.common.ApiResponse;
import cn.zhuatech.sqlagent.service.SqlPlanningService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/sql")
@PreAuthorize("hasAnyRole('DOMAIN_USER','DOMAIN_OPERATOR','ADMIN')")
public class SqlPlanningController {
    private final SqlPlanningService service;
    public SqlPlanningController(SqlPlanningService service) { this.service = service; }
    @PostMapping("/plan")
    public ApiResponse<SqlPlanningService.Result> plan(@Valid @RequestBody SqlPlanningService.Request request) {
        return ApiResponse.ok("查询计划已生成", service.plan(request));
    }
}
