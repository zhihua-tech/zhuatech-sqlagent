/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.sqlagent.controller;

import cn.zhuatech.sqlagent.common.ApiResponse;
import cn.zhuatech.sqlagent.service.SqlQueryReleaseService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
 */
@RestController
@RequestMapping("/api/enterprise/sqlagent")
public class SqlQueryReleaseController {
    private final SqlQueryReleaseService service;
    /**
     * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
     */
    public SqlQueryReleaseController(SqlQueryReleaseService service) { this.service = service; }

    /**
     * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
     */
    @PostMapping("/query-release")
    public ApiResponse<SqlQueryReleaseService.ReleaseResult> release(
            @Valid @RequestBody SqlQueryReleaseService.ReleaseRequest request) {
        return ApiResponse.ok("SQL 查询发布评估完成", service.release(request));
    }
}
