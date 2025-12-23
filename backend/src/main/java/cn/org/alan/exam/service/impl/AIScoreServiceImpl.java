package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.exception.ServiceRuntimeException;
import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.ExamQuAnswerMapper;
import cn.org.alan.exam.mapper.ExamQuestionMapper;
import cn.org.alan.exam.mapper.OptionMapper;
import cn.org.alan.exam.mapper.QuestionMapper;
import cn.org.alan.exam.model.entity.ExamQuAnswer;
import cn.org.alan.exam.model.entity.ExamQuestion;
import cn.org.alan.exam.model.entity.Option;
import cn.org.alan.exam.model.entity.Question;
import cn.org.alan.exam.model.form.answer.AIScoreForm;
import cn.org.alan.exam.model.vo.answer.AIScoreVO;
import cn.org.alan.exam.service.IAIScoreService;
import cn.org.alan.exam.utils.AIEvaluateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;


@Service
public class AIScoreServiceImpl implements IAIScoreService {

    private static final Logger log = LoggerFactory.getLogger(AIScoreServiceImpl.class);

    @Resource
    private AIEvaluateUtil aiEvaluateUtil;

    @Resource
    private ExamQuAnswerMapper examQuAnswerMapper;

    @Resource
    private ExamQuestionMapper examQuestionMapper;

    @Resource
    private QuestionMapper questionMapper;

    @Resource
    private OptionMapper optionMapper;

    @Override
    @Transactional
    public Result<AIScoreVO> evaluateAnswer(AIScoreForm form) {
        try {
            log.info("开始AI评分，examId:{}, userId:{}, questionId:{}", 
                     form.getExamId(), form.getUserId(), form.getQuestionId());

            // 1. 查询考生答题记录
            LambdaQueryWrapper<ExamQuAnswer> answerWrapper = new LambdaQueryWrapper<>();
            answerWrapper.eq(ExamQuAnswer::getExamId, form.getExamId())
                        .eq(ExamQuAnswer::getUserId, form.getUserId())
                        .eq(ExamQuAnswer::getQuestionId, form.getQuestionId())
                        .eq(ExamQuAnswer::getQuestionType, 4);  // 简答题
            
            ExamQuAnswer examQuAnswer = examQuAnswerMapper.selectOne(answerWrapper);
            
            if (examQuAnswer == null) {
                return Result.failed("未找到该答题记录");
            }

            // 2. 查询题目信息
            Question question = questionMapper.selectById(form.getQuestionId());
            if (question == null) {
                return Result.failed("题目不存在");
            }

            // 3. 查询参考答案（简答题的参考答案存在option表中）
            LambdaQueryWrapper<Option> optionWrapper = new LambdaQueryWrapper<>();
            optionWrapper.eq(Option::getQuId, form.getQuestionId())
                        .eq(Option::getIsRight, 1);
            Option referenceOption = optionMapper.selectOne(optionWrapper);
            
            if (referenceOption == null) {
                return Result.failed("未找到参考答案");
            }

            // 4. 查询题目分值
            LambdaQueryWrapper<ExamQuestion> examQuestionWrapper = new LambdaQueryWrapper<>();
            examQuestionWrapper.eq(ExamQuestion::getExamId, form.getExamId())
                              .eq(ExamQuestion::getQuestionId, form.getQuestionId());
            ExamQuestion examQuestion = examQuestionMapper.selectOne(examQuestionWrapper);
            
            if (examQuestion == null) {
                return Result.failed("该题目未关联到考试");
            }

            Integer totalScore = examQuestion.getScore();

            // 5. 调用AI评分
            String userAnswer = examQuAnswer.getAnswerContent();
            String referenceAnswer = referenceOption.getContent();
            String questionContent = question.getContent();

            if (userAnswer == null || userAnswer.trim().isEmpty()) {
                return Result.failed("考生未作答，无法进行AI评分");
            }

            AIEvaluateUtil.EvaluateResult evaluateResult = aiEvaluateUtil.evaluateAnswer(
                userAnswer,
                referenceAnswer,
                totalScore,
                questionContent
            );

            // 6. 更新答题记录的AI评分
            examQuAnswer.setAiScore(evaluateResult.getScore());
            examQuAnswer.setAiReason(evaluateResult.getComment());
            examQuAnswer.setAiConfidence(evaluateResult.getConfidence());
            examQuAnswerMapper.updateById(examQuAnswer);

            // 7. 构建返回结果
            AIScoreVO aiScoreVO = new AIScoreVO();
            aiScoreVO.setExamQuAnswerId(examQuAnswer.getId());
            aiScoreVO.setAiScore(evaluateResult.getScore());
            aiScoreVO.setAiComment(evaluateResult.getComment());
            aiScoreVO.setAiConfidence(evaluateResult.getConfidence());
            aiScoreVO.setTotalScore(totalScore);
            aiScoreVO.setUserAnswer(userAnswer);
            aiScoreVO.setReferenceAnswer(referenceAnswer);

            log.info("AI评分完成，得分：{}/{}", evaluateResult.getScore(), totalScore);

            return Result.success("AI评分成功", aiScoreVO);

        } catch (ServiceRuntimeException e) {
            log.error("AI评分失败", e);
            return Result.failed(e.getMessage());
        } catch (Exception e) {
            log.error("AI评分异常", e);
            return Result.failed("AI评分失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Result<String> batchEvaluate(Integer examId) {
        try {
            log.info("开始批量AI评分，examId:{}", examId);

            // 查询该考试所有简答题的答题记录
            LambdaQueryWrapper<ExamQuAnswer> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ExamQuAnswer::getExamId, examId)
                  .eq(ExamQuAnswer::getQuestionType, 4)
                  .isNotNull(ExamQuAnswer::getAnswerContent);

            List<ExamQuAnswer> answers = examQuAnswerMapper.selectList(wrapper);

            if (answers.isEmpty()) {
                return Result.failed("该考试没有需要AI评分的主观题");
            }

            int successCount = 0;
            int failedCount = 0;

            for (ExamQuAnswer answer : answers) {
                try {
                    AIScoreForm form = new AIScoreForm();
                    form.setExamId(answer.getExamId());
                    form.setUserId(answer.getUserId());
                    form.setQuestionId(answer.getQuestionId());

                    Result<AIScoreVO> result = evaluateAnswer(form);
                    if (result.getCode() == 1) {
                        successCount++;
                    } else {
                        failedCount++;
                        log.warn("批量评分失败：userId={}, questionId={}, 原因：{}", 
                                answer.getUserId(), answer.getQuestionId(), result.getMsg());
                    }
                } catch (Exception e) {
                    failedCount++;
                    log.error("批量评分异常：userId={}, questionId={}", 
                            answer.getUserId(), answer.getQuestionId(), e);
                }
            }

            String message = String.format("批量AI评分完成！成功%d题，失败%d题", successCount, failedCount);
            log.info(message);

            return Result.success(message);

        } catch (Exception e) {
            log.error("批量AI评分异常", e);
            return Result.failed("批量AI评分失败：" + e.getMessage());
        }
    }
}