package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.converter.QuestionConverter;
import cn.org.alan.exam.mapper.ExerciseRecordMapper;
import cn.org.alan.exam.mapper.OptionMapper;
import cn.org.alan.exam.mapper.QuestionMapper;
import cn.org.alan.exam.model.entity.Option;
import cn.org.alan.exam.model.entity.Question;
import cn.org.alan.exam.model.form.question.QuestionFrom;
import cn.org.alan.exam.utils.AIQuestionGeneratorUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * 题库管理业务测试
 * 对应: src/main/java/.../QuestionServiceImpl.java
 */
@ExtendWith(MockitoExtension.class)
class QuestionServiceImplTest {

    @InjectMocks
    private QuestionServiceImpl questionService;

    @Mock private QuestionMapper questionMapper;
    @Mock private OptionMapper optionMapper;
    @Mock private QuestionConverter questionConverter;
    @Mock private ExerciseRecordMapper exerciseRecordMapper;
    @Mock private AIQuestionGeneratorUtil aiQuestionGeneratorUtil;

    /**
     * 测试点: 添加单选题
     * 验证是否同时插入了“题目”和“选项”
     */
    @Test
    @DisplayName("测试添加单选题(addSingleQuestion)")
    void testAddSingleQuestion() {
        // 1. 准备数据
        QuestionFrom form = new QuestionFrom();
        form.setQuType(1); // 单选
        // 准备两个选项
        List<Option> options = new ArrayList<>();
        options.add(new Option());
        options.add(new Option());
        form.setOptions(options);

        Question question = new Question();
        question.setId(100);

        // 2. 配置Mock
        when(questionConverter.fromToEntity(form)).thenReturn(question);
        when(questionMapper.insert(question)).thenReturn(1);
        when(optionMapper.insertBatch(anyList())).thenReturn(2);

        // 3. 执行
        questionService.addSingleQuestion(form);

        // 4. 验证
        verify(questionMapper).insert(question); // 验证题目入库
        verify(optionMapper).insertBatch(options); // 验证选项入库
    }

    /**
     * 测试点: 批量删除试题
     * 验证级联删除逻辑（先删记录，再删选项，最后删题目）
     */
    @Test
    @DisplayName("测试批量删除试题(deleteBatchByIds)")
    void testDeleteBatchByIds() {
        String ids = "101,102";
        List<Integer> idList = Arrays.asList(101, 102);

        when(exerciseRecordMapper.delete(any(LambdaUpdateWrapper.class))).thenReturn(1);
        when(optionMapper.deleteBatchIds(idList)).thenReturn(2);
        when(questionMapper.deleteBatchIds(idList)).thenReturn(2);

        // 执行删除
        questionService.deleteBatchByIds(ids);

        // 验证删除顺序和调用
        verify(exerciseRecordMapper).delete(any(LambdaUpdateWrapper.class));
        verify(optionMapper).deleteBatchIds(idList);
        verify(questionMapper).deleteBatchIds(idList);
    }
}