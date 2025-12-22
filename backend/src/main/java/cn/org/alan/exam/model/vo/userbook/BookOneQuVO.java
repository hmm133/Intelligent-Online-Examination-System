package cn.org.alan.exam.model.vo.userbook;

import cn.org.alan.exam.model.entity.Option;
import lombok.Data;

import java.util.List;

@Data
public class BookOneQuVO {
    private static final long serialVersionUID = 1L;

    /**
     * 图片
     */
    private String image;

    /**
     * 题目内容
     */
    private String content;

    /**
     * 试题类型
     */
    private Integer quType;

    /**
     * 答案内容
     */
    private List<Option> answerList;
}
