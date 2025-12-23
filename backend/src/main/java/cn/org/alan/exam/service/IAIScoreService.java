package cn.org.alan.exam.service;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.model.form.answer.AIScoreForm;
import cn.org.alan.exam.model.vo.answer.AIScoreVO;


public interface IAIScoreService {

    /**
     * AI评分
     *
     * @param form 评分请求参数
     * @return AI评分结果
     */
    Result<AIScoreVO> evaluateAnswer(AIScoreForm form);

    /**
     * 批量AI评分（为某场考试的所有主观题进行AI评分）
     *
     * @param examId 考试ID
     * @return 评分结果统计
     */
    Result<String> batchEvaluate(Integer examId);
}