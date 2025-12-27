package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.mapper.*;
import cn.org.alan.exam.model.entity.*;
import cn.org.alan.exam.model.form.answer.CorrectAnswerFrom;
import cn.org.alan.exam.model.vo.answer.UserAnswerDetailVO;
import cn.org.alan.exam.utils.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 人工阅卷业务测试
 * 对应: src/main/java/.../ManualScoreServiceImpl.java
 */
@ExtendWith(MockitoExtension.class)
class ManualScoreServiceImplTest {

    @InjectMocks
    private ManualScoreServiceImpl manualScoreService;

    @Mock private ExamQuAnswerMapper examQuAnswerMapper;
    @Mock private QuestionMapper questionMapper;
    @Mock private ExamQuestionMapper examQuestionMapper;
    @Mock private OptionMapper optionMapper;
    @Mock private ManualScoreMapper manualScoreMapper;
    @Mock private UserExamsScoreMapper userExamsScoreMapper;

    /**
     * 测试点: 获取阅卷详情
     * 验证是否能正确回显学生的答案和 AI 的评分
     */
    @Test
    @DisplayName("测试获取阅卷详情(getDetail)")
    void testGetDetail() {
        // 1. 模拟数据库里的答题记录
        ExamQuAnswer answer = new ExamQuAnswer();
        answer.setQuestionId(100);
        answer.setQuestionType(4); // 简答题
        answer.setAnswerContent("这是学生的答案");
        answer.setAiScore(8);      // AI给了8分
        answer.setAiReason("回答得很好");

        when(examQuAnswerMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.singletonList(answer));

        // 2. 模拟题目信息
        Question question = new Question();
        question.setContent("这是题目内容");
        when(questionMapper.selectById(100)).thenReturn(question);

        // 3. 模拟题目满分
        ExamQuestion examQuestion = new ExamQuestion();
        examQuestion.setScore(10);
        when(examQuestionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(examQuestion);

        // 4. 执行
        List<UserAnswerDetailVO> result = manualScoreService.getDetail(1, 1).getData();

        // 5. 验证数据是否正确映射
        UserAnswerDetailVO vo = result.get(0);
        Assertions.assertEquals("这是学生的答案", vo.getStudentAnswer());
        // Assertions.assertEquals(8, vo.getAiScore(), "AI分数回显错误");
        // 明确将 Integer 转为 int
        Assertions.assertEquals(8, vo.getAiScore().intValue(), "AI分数回显错误");
        Assertions.assertEquals("这是题目内容", vo.getContent());
    }

    /**
     * 测试点: 教师打分提交
     * 验证是否保存了分数并更新了总成绩
     */
    @Test
    @DisplayName("测试教师人工评分提交(correct)")
    void testCorrect() {
        try (MockedStatic<SecurityUtil> securityUtilMock = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getUserId).thenReturn(2001); // 老师ID

            // 1. 准备提交的数据
            CorrectAnswerFrom form = new CorrectAnswerFrom();
            form.setExamId(1);
            form.setUserId(1001);
            form.setQuestionId(100);
            form.setScore(9); // 老师改了9分

            // 2. 模拟数据库查询
            ExamQuAnswer answer = new ExamQuAnswer();
            answer.setId(500);
            when(examQuAnswerMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(answer);

            // 模拟重算总分时的查询
            when(examQuAnswerMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.singletonList(answer));
            
            ManualScore ms = new ManualScore();
            ms.setScore(9);
            when(manualScoreMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(ms);

            // 3. 执行
            manualScoreService.correct(Collections.singletonList(form));

            // 4. 验证
            verify(manualScoreMapper).insert(any(ManualScore.class)); // 验证保存了评分
            verify(userExamsScoreMapper).update(any(), any(LambdaUpdateWrapper.class)); // 验证更新了总分
        }
    }
}