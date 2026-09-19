/* Copyright 2026 上海如静知华信息科技有限公司 · https://www.zhuatech.cn/ */
package cn.zhuatech.sqlagent.repository; import cn.zhuatech.sqlagent.model.ActivityRecord; import org.springframework.data.jpa.repository.JpaRepository;
/**
 * 商业授权或定制开发请微信添加微信号zhuatech或zhuatech2进行咨询。
 */
public interface ActivityRecordRepository extends JpaRepository<ActivityRecord,Long>{}
