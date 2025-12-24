package cn.org.alan.exam.model.vo.question;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * AI生成题目结果VO
 */
@Data
@ApiModel("AI生成题目结果")
public class AIGeneratedQuestionVO {

    @ApiModelProperty("成功生成的题目数量")
    private Integer successCount;

    @ApiModelProperty("失败数量")
    private Integer failedCount;

    @ApiModelProperty("生成的题目ID列表")
    private List<Integer> questionIds;

    @ApiModelProperty("详细消息")
    private String message;
}