package com.ishansong.diablo.core.constant;

public interface AdminConstants {

    String PLUGIN_NAME_IS_EXIST = "插件名称已经存在，不能重复添加!";

    String PLUGIN_NAME_NOT_EXIST = "插件名称已经不存在!";

    String SYS_PLUGIN_NOT_DELETE = "系统插件不能删除!";

    String SYS_PLUGIN_ID_NOT_EXIST = "插件id不存在！";
    
    String OPERATE_RESULT_CODE = "code";

    String SELECOTR_NOT_EXIST = "发布失败,无选择器";

    String BACKUP_SELECOTR_NOT_EXIST = "回滚失败,无备份记录";

    String NOTICE_FAILED = "发布成功,后台通知网关变更失败,稍后重新发布";

    String ROLLBACK_NOT_TIME = "请选择回滚时间";

    String ROLLBACK_FAILED = "回滚成功,后台通知网关变更失败,稍后重新发布";

    String SESSION_LOGIN_NAME = "session_login_name";

    String SELECTOR_NAME = "selector_name";
    
    String NOTICE_MESSAGE_SELECTORID = "selectorId";

    String NOTICE_MESSAGE_RULEIDS = "ruleIds";

    String NOTICE_MESSAGE_TIMESTAMP = "timestamp";


}
