package cn.org.alan.exam.model.form.answer;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;


@Data
@ApiModel("AI评分请求表单")
public class AIScoreForm {

    @ApiModelProperty(value = "考试ID", required = true)
    @NotNull(message = "考试ID不能为空")
    private Integer examId;

    @ApiModelProperty(value = "用户ID", required = true)
    @NotNull(message = "用户ID不能为空")
    private Integer userId;

    @ApiModelProperty(value = "试题ID", required = true)
    @NotNull(message = "试题ID不能为空")
    private Integer questionId;
}