package cn.org.alan.exam.model.form.count;

import lombok.Data;


@Data
public class ClassCountResult {
    // 班级ID
    private Integer gradeId;
    // 班级名称
    private String gradeName;
    private Integer count;
    // 班级数量
    private int gradeCount;
    // 试卷数量
    private  int examCount;
    // 试题数量
    private int questionCount;
}
