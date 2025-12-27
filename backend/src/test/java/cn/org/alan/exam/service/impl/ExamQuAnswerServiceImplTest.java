package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.ExamQuAnswerMapper;
import cn.org.alan.exam.model.vo.score.QuestionAnalyseVO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExamQuAnswerServiceImplTest {

    @InjectMocks
    private ExamQuAnswerServiceImpl examQuAnswerService;

    @Mock
    private ExamQuAnswerMapper examQuAnswerMapper;

    @Test
    @DisplayName("测试题目分析-正确率计算")
    void testQuestionAnalyse() {
        // 1. 模拟 Mapper 返回数据
        QuestionAnalyseVO vo = new QuestionAnalyseVO();
        vo.setRightCount(20);
        vo.setTotalCount(100);
        // 预期正确率: 20/100 = 0.20
        
        when(examQuAnswerMapper.questionAnalyse(anyInt(), anyInt())).thenReturn(vo);

        // 2. 执行
        Result<QuestionAnalyseVO> result = examQuAnswerService.questionAnalyse(1, 1);

        // 3. 验证
        Assertions.assertEquals(0.20, result.getData().getAccuracy(), 0.001);
    }
}