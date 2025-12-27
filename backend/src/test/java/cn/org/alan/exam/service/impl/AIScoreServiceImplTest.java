package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.*;
import cn.org.alan.exam.model.entity.*;
import cn.org.alan.exam.model.form.answer.AIScoreForm;
import cn.org.alan.exam.model.vo.answer.AIScoreVO;
import cn.org.alan.exam.utils.AIEvaluateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AIScoreServiceImplTest {

    @InjectMocks
    private AIScoreServiceImpl aiScoreService;

    @Mock private AIEvaluateUtil aiEvaluateUtil;
    @Mock private ExamQuAnswerMapper examQuAnswerMapper;
    @Mock private ExamQuestionMapper examQuestionMapper;
    @Mock private QuestionMapper questionMapper;
    @Mock private OptionMapper optionMapper;

    @Test
    @DisplayName("测试单题AI评分(evaluateAnswer)")
    void testEvaluateAnswer() {
        // 1. 准备数据
        AIScoreForm form = new AIScoreForm();
        form.setExamId(1);
        form.setQuestionId(100);
        form.setUserId(1001);

        // 模拟答题记录
        ExamQuAnswer answer = new ExamQuAnswer();
        answer.setAnswerContent("用户的回答");
        when(examQuAnswerMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(answer);

        // 模拟题目和满分
        when(questionMapper.selectById(100)).thenReturn(new Question());
        ExamQuestion examQuestion = new ExamQuestion();
        examQuestion.setScore(10);
        when(examQuestionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(examQuestion);

        // 模拟参考答案
        Option option = new Option();
        option.setContent("标准答案");
        when(optionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(option);

        // 模拟 AI 工具类返回结果
        AIEvaluateUtil.EvaluateResult aiResult = new AIEvaluateUtil.EvaluateResult(null, null, null);
        aiResult.setScore(8);
        aiResult.setComment("回答不错");
        aiResult.setConfidence(0.95);
        when(aiEvaluateUtil.evaluateAnswer(anyString(), anyString(), anyInt(), any())).thenReturn(aiResult);

        // 2. 执行
        Result<AIScoreVO> result = aiScoreService.evaluateAnswer(form);

        // 3. 验证
        // Assertions.assertEquals(1, result.getCode());
        Assertions.assertEquals(1, result.getCode().intValue());
        // Assertions.assertEquals(8, result.getData().getAiScore());
        Assertions.assertEquals(8, result.getData().getAiScore().intValue());
        // 验证是否更新了数据库
        verify(examQuAnswerMapper).updateById(any(ExamQuAnswer.class));
    }
}