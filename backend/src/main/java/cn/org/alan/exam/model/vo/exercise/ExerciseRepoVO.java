package cn.org.alan.exam.model.vo.exercise;

import lombok.Data;


@Data
public class ExerciseRepoVO {
    private Integer id;
    // 题库标题
    private String repoTitle;
    // 总题数
    private Integer totalCount;
    private Integer exerciseCount;
    // 分类相关字段
    private Integer categoryId;
    private String categoryName;
    private Integer parentCategoryId;
    private String parentCategoryName;
}
