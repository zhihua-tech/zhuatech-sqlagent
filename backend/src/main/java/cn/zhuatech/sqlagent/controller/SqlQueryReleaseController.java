/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.sqlagent.controller;

import cn.zhuatech.sqlagent.common.ApiResponse;
import cn.zhuatech.sqlagent.service.SqlQueryReleaseService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/enterprise/sqlagent")
public class SqlQueryReleaseController {
    private final SqlQueryReleaseService service;
    public SqlQueryReleaseController(SqlQueryReleaseService service) { this.service = service; }

    @PostMapping("/query-release")
    public ApiResponse<SqlQueryReleaseService.ReleaseResult> release(
            @Valid @RequestBody SqlQueryReleaseService.ReleaseRequest request) {
        return ApiResponse.ok("SQL 查询发布评估完成", service.release(request));
    }
}
