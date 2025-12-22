package cn.org.alan.exam.model.vo.stat;

import lombok.Data;


@Data
public class GradeExamVO {
    private Integer id;
    // 班级名称
    private String gradeName;
    private Integer total;
}
