package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.mapper.ExamQuAnswerMapper;
import cn.org.alan.exam.model.entity.ExamQuAnswer;
import cn.org.alan.exam.model.vo.question.QuestionScoreVO;
import cn.org.alan.exam.utils.agent.AIChat;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutoScoringServiceImplTest {

    @InjectMocks
    private AutoScoringServiceImpl autoScoringService;

    @Mock private ExamQuAnswerMapper examQuAnswerMapper;
    @Mock private AIChat aiChat;
    @Mock private PlatformTransactionManager platformTransactionManager;
    @Mock private TransactionStatus transactionStatus;

    @Test
    @DisplayName("测试自动评分逻辑(autoScoringExam)")
    void testAutoScoringExam() throws Exception {
        // 1. 模拟事务管理
        when(platformTransactionManager.getTransaction(any())).thenReturn(transactionStatus);

        // 2. 模拟待评分题目
        List<QuestionScoreVO> questions = new ArrayList<>();
        questions.add(new QuestionScoreVO()); // 添加一个题目
        when(examQuAnswerMapper.getQuestionsForGrading(anyInt(), anyInt())).thenReturn(questions);

        // 3. 模拟 AI 返回的 JSON 字符串 (注意转义字符)
        // 模拟返回格式： ```json ... ```
        String mockAiResponse = "```json\n" +
                "{\n" +
                "  \"评分结果\": [\n" +
                "    {\n" +
                "      \"题目ID\": \"100\",\n" +
                "      \"最终得分\": \"5\",\n" +
                "      \"扣分原因\": \"无\"\n" +
                "    }\n" +
                "  ]\n" +
                "}\n" +
                "```";
        when(aiChat.getChatResponse(anyString())).thenReturn(mockAiResponse);

        // 4. 模拟数据库存在该记录以便更新
        ExamQuAnswer existingRecord = new ExamQuAnswer();
        existingRecord.setId(1);
        when(examQuAnswerMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existingRecord);

        // 5. 执行
        autoScoringService.autoScoringExam(1, 1001);

        // 6. 验证
        // 验证是否提交了事务
        verify(platformTransactionManager).commit(transactionStatus);
        // 验证是否执行了更新操作
        verify(examQuAnswerMapper, atLeastOnce()).updateById(any(ExamQuAnswer.class));
    }
}