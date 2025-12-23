// package cn.org.alan.exam.model.vo.answer;

// import lombok.Data;


// @Data
// public class UserAnswerDetailVO {
//     // 试题ID
//     private Integer quId;
//     // 用户ID
//     private Integer userId;
//     // 试卷ID
//     private Integer examId;
//     // 试题标题
//     private String quTitle;
//     // 试题图片
//     private String quImg;
//     private String answer;
//     private String refAnswer;
//     private Integer correctScore;
//     private String aiReason;
//     private Integer totalScore;

//         // ========== 新增字段开始 ==========
    
//     // AI评分
//     private Integer aiScore;
//     // AI置信度
//     private Double aiConfidence;
//     // 题型（用于前端判断是否显示AI评分按钮）
//     private Integer quType;
    
//     // ========== 新增字段结束 ==========

// }


package cn.org.alan.exam.model.vo.answer;

import cn.org.alan.exam.model.entity.Option;
import lombok.Data;
import java.util.List;

/**
 * 用户作答信息 VO
 * 修正字段名以匹配前端 makeTest.vue
 */
@Data
public class UserAnswerDetailVO {
    
    // 【修正】前端用 id，后端原为 quId
    private Integer id; 
    
    // 为了兼容性保留 quId (可选)
    private Integer quId;

    private Integer userId;
    private Integer examId;

    // 【修正】前端用 content，后端原为 quTitle
    private String content; 
    
    // 【修正】前端用 image，后端原为 quImg
    private String image; 
    
    // 【修正】前端用 studentAnswer，后端原为 answer
    private String studentAnswer; 
    
    // 【修正】前端用 rightAnswer，后端原为 refAnswer
    private String rightAnswer; 
    
    // 【修正】前端用 score，后端原为 totalScore
    private Integer score; 
    
    // 用户实际得分
    private Integer userScore; 

    // 【新增】前端必须要有选项列表才能渲染选择题
    private List<Option> options; 

    // 题目解析
    private String analysis;

    // ========== 新增 AI 相关字段 ==========
    
    // AI评分
    private Integer aiScore;
    // AI置信度
    private Double aiConfidence;
    // AI评语
    private String aiReason; // 前端用 aiReason 或 aiComment，注意对应
    // 题型
    private Integer quType;
}