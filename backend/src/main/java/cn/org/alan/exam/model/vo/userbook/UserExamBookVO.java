package cn.org.alan.exam.model.vo.userbook;

import cn.org.alan.exam.model.entity.Option;
import lombok.Data;

import java.util.List;

@Data
public class UserExamBookVO {
    /**
     * 题干
     */
    private String content;
    /**
     * 选项
     */
    private List<Option> answerList;
    /**
     * 正确答案
     */
    private String rightAnswers;
    /**
     * 试题分析
     */
    private String analyse;
    /**
     * 所填答案
     */
    private String reply;

}
