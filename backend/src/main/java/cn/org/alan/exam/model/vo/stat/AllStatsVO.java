package cn.org.alan.exam.model.vo.stat;

import lombok.Data;


@Data
public class AllStatsVO {
    // 班级数量
    private Integer classCount;
    // 试卷数量
    private Integer examCount;
    // 试题数量
    private Integer questionCount;


}
