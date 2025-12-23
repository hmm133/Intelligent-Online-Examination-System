package cn.org.alan.exam.model.vo.answer;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
@ApiModel("AI评分结果")
public class AIScoreVO {

    @ApiModelProperty("答题记录ID")
    private Integer examQuAnswerId;

    @ApiModelProperty("AI建议分数")
    private Integer aiScore;

    @ApiModelProperty("AI评语")
    private String aiComment;

    @ApiModelProperty("AI置信度(0-1)")
    private Double aiConfidence;

    @ApiModelProperty("题目满分")
    private Integer totalScore;

    @ApiModelProperty("学生答案")
    private String userAnswer;

    @ApiModelProperty("参考答案")
    private String referenceAnswer;
}