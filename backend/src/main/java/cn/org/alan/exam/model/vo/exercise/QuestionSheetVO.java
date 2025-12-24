package cn.org.alan.exam.model.vo.exercise;

import lombok.Data;
import lombok.experimental.Accessors;


@Data
public class QuestionSheetVO {
    // 试题ID
    private Integer quId;
    // 试题类型
    private Integer quType;
    // 题库ID
    private Integer repoId;
    private Integer exercised;
    private Integer isRight;
}
