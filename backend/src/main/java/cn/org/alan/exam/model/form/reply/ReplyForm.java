package cn.org.alan.exam.model.form.reply;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


@Data
public class ReplyForm {
    @NotNull(message = "讨论id都不能为空")
    private Integer discussionId;

    private Integer parentId;

    @NotBlank(message = "回复内容不能为空")
    private String content;
}
