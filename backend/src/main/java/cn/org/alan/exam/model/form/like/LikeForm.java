package cn.org.alan.exam.model.form.like;

import lombok.Data;

import javax.validation.constraints.NotNull;


@Data
public class LikeForm {
    @NotNull(message = "讨论id不能为空")
    private Integer discussionId;

    @NotNull(message = "回复id不能为空")
    private Integer replyId;
}
