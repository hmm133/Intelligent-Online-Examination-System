package cn.org.alan.exam.model.vo.score;

import lombok.Data;



@Data
public class QuestionAnalyseVO {
    // 正确数量
    private Integer rightCount;
    // 总题数
    private Integer totalCount;
    private Double accuracy;

}
