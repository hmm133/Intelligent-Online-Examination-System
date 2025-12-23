package cn.org.alan.exam.model.dto;

import cn.org.alan.exam.model.enums.MessageType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("消息封装Dto")
public class Message {
    @ApiModelProperty("消息类型")
    private MessageType type;
    @ApiModelProperty("消息具体内容")
    private Object data;
}
