/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.sqlagent;
import cn.zhuatech.sqlagent.service.SqlPlanningService; import org.junit.jupiter.api.Test; import java.util.List; import static org.assertj.core.api.Assertions.assertThat;
/**
 * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
 */
class SqlPlanningServiceTests { private final SqlPlanningService service=new SqlPlanningService();
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 @Test void blocksWriteIntent(){var result=service.plan(new SqlPlanningService.Request("DW","删除重复订单",List.of("sales_order"),3000,false,true,100));assertThat(result.decision()).isEqualTo("BLOCK");assertThat(result.sqlPreview()).contains("blocked");}
 /**
  * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
  */
 @Test void permitsSmallReadQuery(){var result=service.plan(new SqlPlanningService.Request("DW","查询本月销售额",List.of("sales_summary"),1200,false,false,200));assertThat(result.decision()).isEqualTo("PASS");assertThat(result.sqlPreview()).contains("tenant_id");}}
