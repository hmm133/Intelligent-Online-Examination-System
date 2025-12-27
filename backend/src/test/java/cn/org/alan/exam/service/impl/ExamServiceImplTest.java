package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.converter.ExamConverter;
import cn.org.alan.exam.mapper.*;
import cn.org.alan.exam.model.entity.Exam;
import cn.org.alan.exam.model.entity.ExamQuAnswer;
import cn.org.alan.exam.model.entity.UserExamsScore;
import cn.org.alan.exam.model.form.exam.ExamAddForm;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * 考试核心业务测试
 * 对应: src/main/java/.../ExamServiceImpl.java
 */
@ExtendWith(MockitoExtension.class)
class ExamServiceImplTest {

    @InjectMocks
    private ExamServiceImpl examService;

    // 模拟所有依赖的 Mapper
    @Mock private ExamMapper examMapper;
    @Mock private ExamConverter examConverter;
    @Mock private ExamGradeMapper examGradeMapper;
    @Mock private ExamRepoMapper examRepoMapper;
    @Mock private UserExamsScoreMapper userExamsScoreMapper;
    @Mock private ExamQuAnswerMapper examQuAnswerMapper;
    @Mock private ExamQuestionMapper examQuestionMapper;

    /**
     * 测试点 1: 验证试卷总分计算逻辑是否正确
     * 场景: 单选10分 + 多选20分 + 判断10分 + 简答20分 = 60分
     */
    @Test
    @DisplayName("测试试卷总分计算(getGrossScore)")
    void testGetGrossScore() {
        // 1. 准备数据
        Exam exam = new Exam();
        exam.setRadioCount(5);  exam.setRadioScore(2);  // 10分
        exam.setMultiCount(5);  exam.setMultiScore(4);  // 20分
        exam.setJudgeCount(5);  exam.setJudgeScore(2);  // 10分
        exam.setSaqCount(1);    exam.setSaqScore(20);   // 20分
        
        // 2. 执行方法
        Integer result = examService.getGrossScore(exam);

        // 3. 验证结果
        // 明确将 result 转为 int，匹配 assertEquals(int, int, String)
        Assertions.assertEquals(60, result.intValue(), "试卷总分计算错误，预期60分");
    }

    /**
     * 测试点 2: 验证创建考试流程
     */
    @Test
    @DisplayName("测试创建考试流程(createExam)")
    void testCreateExam() {
        // 1. 准备表单数据
        ExamAddForm form = new ExamAddForm();
        form.setGradeIds("1,2"); // 关联班级
        form.setRepoId(1);       // 关联题库
        form.setAddQuype("1");   // 随机抽题模式
        
        // 模拟转换后的实体
        Exam examEntity = new Exam();
        examEntity.setId(1);

        // 2. 配置 Mock 行为
        when(examConverter.formToEntity(form)).thenReturn(examEntity);
        when(examMapper.insert(any(Exam.class))).thenReturn(1);
        when(examGradeMapper.addExamGrade(anyInt(), anyList())).thenReturn(1);
        when(examRepoMapper.insert(any())).thenReturn(1);

        // 3. 执行
        examService.createExam(form);

        // 4. 验证是否调用了数据库插入
        verify(examMapper).insert(any(Exam.class)); // 验证插了考试表
        verify(examGradeMapper).addExamGrade(eq(1), anyList()); // 验证关联了班级
    }

    /**
     * 测试点 3: 测试交卷时的自动评分逻辑 (客观题)
     */
    @Test
    @DisplayName("测试交卷自动评分(handExam)")
    void testHandExam() {
        // 模拟静态工具类 SecurityUtil 获取当前用户ID
        try (MockedStatic<SecurityUtil> securityUtilMock = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getUserId).thenReturn(1001);

            // 1. 模拟考试规则
            Exam exam = new Exam();
            exam.setId(1);
            exam.setExamDuration(60);
            exam.setRadioScore(2); // 单选题每题2分
            when(examMapper.selectById(1)).thenReturn(exam);

            // 2. 模拟正在进行的考试记录
            UserExamsScore userScore = new UserExamsScore();
            userScore.setState(0); // 状态0表示正在考试
            userScore.setCreateTime(LocalDateTime.now().minusMinutes(30)); 
            when(userExamsScoreMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(userScore);

            // 3. 模拟学生答题情况 (答对1道单选题)
            ExamQuAnswer answer = new ExamQuAnswer();
            answer.setIsRight(1);      // 答对了
            answer.setQuestionType(1); // 单选题
            List<ExamQuAnswer> answers = new ArrayList<>();
            answers.add(answer);
            when(examQuAnswerMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(answers);

            // 4. 执行交卷
            examService.handExam(1);

            // 5. 验证是否调用了更新分数的方法，且分数是否包含了那2分
            // 这里主要验证 update 被调用，具体的数值校验依赖于 update 的参数捕获，为简化演示验证调用次数
            verify(userExamsScoreMapper, atLeastOnce()).update(any(), any(LambdaUpdateWrapper.class));
        }
    }
}