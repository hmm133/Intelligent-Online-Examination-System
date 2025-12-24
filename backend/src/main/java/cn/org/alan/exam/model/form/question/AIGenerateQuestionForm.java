package cn.org.alan.exam.model.form.question;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.*;

/**
 * AI生成试题请求表单
 *
 */
@Data
@ApiModel("AI生成试题请求表单")
public class AIGenerateQuestionForm {

    @ApiModelProperty(value = "题库ID", required = true)
    @NotNull(message = "题库ID不能为空")
    private Integer repoId;

    @ApiModelProperty(value = "生成数量", required = true)
    @NotNull(message = "生成数量不能为空")
    @Min(value = 1, message = "生成数量至少为1")
    @Max(value = 50, message = "单次生成数量不能超过50")
    private Integer count;

    @ApiModelProperty(value = "题型：1单选 2多选 3判断 4简答", required = true)
    @NotNull(message = "题型不能为空")
    @Min(value = 1, message = "题型只能是：1单选2多选3判断4简答")
    @Max(value = 4, message = "题型只能是：1单选2多选3判断4简答")
    private Integer quType;

    @ApiModelProperty(value = "知识点描述", required = true, example = "Java多线程基础知识")
    @NotBlank(message = "知识点描述不能为空")
    @Size(max = 200, message = "知识点描述不能超过200字符")
    private String knowledgePoint;

    @ApiModelProperty(value = "难度等级：1简单 2中等 3困难", example = "2")
    @Min(value = 1, message = "难度等级：1简单 2中等 3困难")
    @Max(value = 3, message = "难度等级：1简单 2中等 3困难")
    private Integer difficulty = 2; // 默认中等难度

    @ApiModelProperty(value = "额外要求", example = "题目要贴近实际应用场景")
    @Size(max = 500, message = "额外要求不能超过500字符")
    private String additionalRequirements;
}