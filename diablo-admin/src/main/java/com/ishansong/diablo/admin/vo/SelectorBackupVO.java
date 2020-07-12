package com.ishansong.diablo.admin.vo;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
@AllArgsConstructor
public class SelectorBackupVO {

    @SerializedName("选择器列表")
    private SelectorBackup selectorBackup;

    @SerializedName("规则列表")
    private List<RuleBackup> ruleBackup;

    @Data
    public static class SelectorBackup {

        public SelectorBackup() {
        }

        @SerializedName("选择器id")
        private String id;

        @SerializedName("创建时间")
        private String dateCreated;

        @SerializedName("更新时间")
        private String dateUpdated;

        @SerializedName("选择器名称")
        private String name;

        @SerializedName("插件id")
        private String pluginId;

        @SerializedName("匹配方式")
        private Integer matchMode;

        @SerializedName("流量类型")
        private Integer type;

        @SerializedName("选择器执行顺序")
        private Integer sort;

        @SerializedName("是否开启")
        private Boolean enabled;

        @SerializedName("打印日志")
        private Boolean loged;

        @SerializedName("继续后续选择器")
        private Boolean continued;

        @SerializedName("发布时间")
        private String datePublished;

        @SerializedName("回滚时间")
        private String dateRollbacked;

        @SerializedName("选择器条件列表")
        private List<SelectorConditionBackup> selectorConditionBackups;
    }

    @Data
    public static class SelectorConditionBackup {

        @SerializedName("选择器条件id")
        private String id;

        @SerializedName("创建时间")
        private String dateCreated;

        @SerializedName("更新时间")
        private String dateUpdated;

        @SerializedName("选择器id")
        private String selectorId;

        @SerializedName("参数类型")
        private String paramType;

        @SerializedName("条件类型")
        private String operator;

        @SerializedName("参数名称")
        private String paramName;

        @SerializedName("参数值")
        private String paramValue;

        @SerializedName("发布时间")
        private String datePublished;

    }

    @Data
    public static class RuleBackup {

        @SerializedName("规则id")
        private String id;

        @SerializedName("创建时间")
        private String dateCreated;

        @SerializedName("更新时间")
        private String dateUpdated;

        @SerializedName("选择器id")
        private String selectorId;

        @SerializedName("匹配方式")
        private Integer matchMode;

        @SerializedName("规则名称")
        private String name;

        @SerializedName("是否开启")
        private Boolean enabled;

        @SerializedName("打印日志")
        private Boolean loged;

        @SerializedName("规则顺序")
        private Integer sort;

        @SerializedName("策略配置")
        private String handle;

        @SerializedName("主机配置")
        private String upstreamHandle;

        @SerializedName("发布时间")
        private String datePublished;

        @SerializedName("规则条件列表")
        private List<RuleConditionBackup> ruleConditionBackups;
    }

    @Data
    public static class RuleConditionBackup {

        @SerializedName("规则条件id")
        private String id;

        @SerializedName("创建时间")
        private String dateCreated;

        @SerializedName("更新时间")
        private String dateUpdated;

        @SerializedName("规则id")
        private String ruleId;

        @SerializedName("参数类型")
        private String paramType;

        @SerializedName("条件类型")
        private String operator;

        @SerializedName("参数名称")
        private String paramName;

        @SerializedName("参数值")
        private String paramValue;

        @SerializedName("发布时间")
        private Timestamp datePublished;
    }
}
