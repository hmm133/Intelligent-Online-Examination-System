package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.*;
import cn.org.alan.exam.model.entity.*;
import cn.org.alan.exam.model.form.answer.CorrectAnswerFrom;
import cn.org.alan.exam.model.vo.answer.AnswerExamVO;
import cn.org.alan.exam.model.vo.answer.UncorrectedUserVO;
import cn.org.alan.exam.model.vo.answer.UserAnswerDetailVO;
import cn.org.alan.exam.service.IManualScoreService;
import cn.org.alan.exam.utils.ClassTokenGenerator;
import cn.org.alan.exam.utils.SecurityUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;



@Service
public class ManualScoreServiceImpl extends ServiceImpl<ManualScoreMapper, ManualScore> implements IManualScoreService {

    @Resource
    private ExamMapper examMapper;
    @Resource
    private ExamGradeMapper examGradeMapper;
    @Resource
    private UserExamsScoreMapper userExamsScoreMapper;
    @Resource
    private ExamQuAnswerMapper examQuAnswerMapper;
    @Resource
    private ManualScoreMapper manualScoreMapper;
    @Resource
    private CertificateUserMapper certificateUserMapper;

    //新增这三个注入
    @Resource
    private QuestionMapper questionMapper;
    @Resource
    private OptionMapper optionMapper;
    @Resource
    private ExamQuestionMapper examQuestionMapper;

    



    // /**
    //  * 试卷查询信息
    //  *
    //  * @param userId
    //  * @param examId
    //  * @return
    //  */
    // @Override
    // public Result<List<UserAnswerDetailVO>> getDetail(Integer userId, Integer examId) {
    //     List<UserAnswerDetailVO> list = examQuAnswerMapper.selectUserAnswer(userId, examId);
    //     return Result.success("查询成功", list);
    // }

    /**
     * 试卷查询信息 (重写逻辑，手动组装数据)
     */
    @Override
    public Result<List<UserAnswerDetailVO>> getDetail(Integer userId, Integer examId) {
        
        // 1. 查询该用户在本次考试的所有答题记录
        LambdaQueryWrapper<ExamQuAnswer> answerWrapper = new LambdaQueryWrapper<>();
        answerWrapper.eq(ExamQuAnswer::getUserId, userId)
                     .eq(ExamQuAnswer::getExamId, examId)
                     .orderByAsc(ExamQuAnswer::getQuestionId); // 按题目ID排序
        List<ExamQuAnswer> answerList = examQuAnswerMapper.selectList(answerWrapper);

        List<UserAnswerDetailVO> resultList = new ArrayList<>();

        for (ExamQuAnswer answer : answerList) {
            UserAnswerDetailVO vo = new UserAnswerDetailVO();
            
            // --- 基础 ID 映射 ---
            vo.setId(answer.getQuestionId()); // 关键！前端要用的 question.id
            vo.setQuId(answer.getQuestionId());
            vo.setExamId(examId);
            vo.setUserId(userId);
            vo.setQuType(answer.getQuestionType());
            

            // --- 填充答案 ---
            // 简答题取 answerContent，选择题取 answerId
            if (answer.getQuestionType() == 4) {
                vo.setStudentAnswer(answer.getAnswerContent());
            } else {
                vo.setStudentAnswer(answer.getAnswerId());
            }

            // --- 填充 AI 评分信息 ---
            vo.setAiScore(answer.getAiScore());
            vo.setAiReason(answer.getAiReason());
            vo.setAiConfidence(answer.getAiConfidence()); // 如果数据库有这个字段就填

            // --- 查题目表 (获取题干、图片、解析) ---
            Question question = questionMapper.selectById(answer.getQuestionId());
            if (question != null) {
                vo.setContent(question.getContent()); // 关键！前端 question.content
                vo.setImage(question.getImage());
                vo.setAnalysis(question.getAnalysis());
            }

            // --- 查试卷题目关联表 (获取本题满分) ---
            LambdaQueryWrapper<ExamQuestion> eqWrapper = new LambdaQueryWrapper<>();
            eqWrapper.eq(ExamQuestion::getExamId, examId)
                     .eq(ExamQuestion::getQuestionId, answer.getQuestionId());
            ExamQuestion examQuestion = examQuestionMapper.selectOne(eqWrapper);
            if (examQuestion != null) {
                vo.setScore(examQuestion.getScore()); // 关键！前端 question.score
            } else {
                vo.setScore(0);
            }

            // --- 查选项表 (获取选项列表和参考答案) ---
            LambdaQueryWrapper<Option> optionWrapper = new LambdaQueryWrapper<>();
            optionWrapper.eq(Option::getQuId, answer.getQuestionId())
                         .orderByAsc(Option::getSort);
            List<Option> options = optionMapper.selectList(optionWrapper);
            vo.setOptions(options); // 关键！前端渲染选项需要

            // 提取参考答案
            if (question != null && question.getQuType() == 4) {
                // 简答题：通常参考答案存在 Option 表的第一个记录，或者 Analysis 中
                if (!options.isEmpty()) {
                    vo.setRightAnswer(options.get(0).getContent());
                }
            } else {
                 // 客观题：前端通常不显示 refAnswer，而是高亮 options 中的 isRight
                 // 但为了完整性，可以拼接正确选项的 Content
                 // 这里暂留空，因为 makeTest.vue 对于客观题主要看 options.isRight
            }

            // --- 填充人工/系统评分 ---
            // 先尝试查人工评分表
            LambdaQueryWrapper<ManualScore> manualWrapper = new LambdaQueryWrapper<>();
            manualWrapper.eq(ManualScore::getExamQuAnswerId, answer.getId());
            ManualScore manualScore = manualScoreMapper.selectOne(manualWrapper);
            
            if (manualScore != null) {
                vo.setUserScore(manualScore.getScore());
            } else {
                // 如果没有人工评分，且是客观题，如果答对了给满分 (这是为了回显系统自动评分)
                if (answer.getIsRight() != null && answer.getIsRight() == 1) {
                    vo.setUserScore(vo.getScore());
                } else {
                    vo.setUserScore(0);
                }
            }

            resultList.add(vo);
        }

        return Result.success("查询成功", resultList);
    }

    // @Override
    // @Transactional
    // public Result<String> correct(List<CorrectAnswerFrom> correctAnswerFroms) {
    //     List<ManualScore> list = new ArrayList<>(correctAnswerFroms.size());
    //     AtomicInteger manualTotalScore = new AtomicInteger();
    //     correctAnswerFroms.forEach(correctAnswerFrom -> {

    //         // 获取用户作答信息id
    //         LambdaQueryWrapper<ExamQuAnswer> wrapper = new LambdaQueryWrapper<ExamQuAnswer>()
    //                 .select(ExamQuAnswer::getId)
    //                 .eq(ExamQuAnswer::getExamId, correctAnswerFrom.getExamId())
    //                 .eq(ExamQuAnswer::getUserId, correctAnswerFrom.getUserId())
    //                 .eq(ExamQuAnswer::getQuestionId, correctAnswerFrom.getQuestionId());

    //         ManualScore manualScore = new ManualScore();
    //         manualScore.setExamQuAnswerId(examQuAnswerMapper.selectOne(wrapper).getId());
    //         manualScore.setScore(correctAnswerFrom.getScore());
    //         list.add(manualScore);
    //         manualTotalScore.addAndGet(correctAnswerFrom.getScore());
    //     });
    //     manualScoreMapper.insertList(list);

    //     // 把用户考试记录修改为已批改，并把简答题分数添加进去
    //     CorrectAnswerFrom correctAnswerFrom = correctAnswerFroms.get(0);
    //     LambdaUpdateWrapper<UserExamsScore> userExamsScoreLambdaUpdateWrapper = new LambdaUpdateWrapper<UserExamsScore>()
    //             .eq(UserExamsScore::getExamId, correctAnswerFrom.getExamId())
    //             .eq(UserExamsScore::getUserId, correctAnswerFrom.getUserId())
    //             .set(UserExamsScore::getWhetherMark, 1)
    //             .setSql("user_score = user_score + " + manualTotalScore.get());
    //     userExamsScoreMapper.update(userExamsScoreLambdaUpdateWrapper);

    //     // 根据该考试是否有证书来给用户颁发对应证书
    //     // 判断该考试是否有证书
    //     LambdaQueryWrapper<Exam> examWrapper = new LambdaQueryWrapper<Exam>()
    //             .select(Exam::getId, Exam::getCertificateId, Exam::getPassedScore)
    //             .eq(Exam::getId, correctAnswerFrom.getExamId());
    //     Exam exam = examMapper.selectOne(examWrapper);
    //     // 不必对exam做非空验证，这里一定不为null
    //     if (exam.getCertificateId() != null && exam.getCertificateId() > 0) {
    //         // 有证书 获取用户得分
    //         LambdaQueryWrapper<UserExamsScore> examsScoreWrapper = new LambdaQueryWrapper<UserExamsScore>()
    //                 .select(UserExamsScore::getId, UserExamsScore::getUserScore)
    //                 .eq(UserExamsScore::getExamId, correctAnswerFrom.getExamId())
    //                 .eq(UserExamsScore::getUserId, correctAnswerFrom.getUserId());
    //         UserExamsScore userExamsScore = userExamsScoreMapper.selectOne(examsScoreWrapper);
    //         // 不必对userExamsScore做非空验证，这里一定不为null
    //         if (userExamsScore.getUserScore() >= exam.getPassedScore()) {
    //             // 分数合格，判罚证书
    //             CertificateUser certificateUser = new CertificateUser();
    //             certificateUser.setUserId(correctAnswerFrom.getUserId());
    //             certificateUser.setExamId(correctAnswerFrom.getExamId());
    //             certificateUser.setCode(ClassTokenGenerator.generateClassToken(18));
    //             certificateUser.setCertificateId(exam.getCertificateId());
    //             certificateUserMapper.insert(certificateUser);
    //         }

    //     }
    //     return Result.success("批改成功");
    // }

    /**
     * 批改试卷（修复版：防止分数重复累加）
     */
    @Override
    @Transactional
    public Result<String> correct(List<CorrectAnswerFrom> correctAnswerFroms) {
        if (correctAnswerFroms == null || correctAnswerFroms.isEmpty()) {
            return Result.success("无修改内容");
        }

        // 1. 获取基本信息 (假设同一批次提交的都是同一个人的同一场考试)
        Integer examId = correctAnswerFroms.get(0).getExamId();
        Integer userId = correctAnswerFroms.get(0).getUserId();

        // 2. 保存/更新人工评分记录 (t_manual_score)
        for (CorrectAnswerFrom item : correctAnswerFroms) {
            // 查出对应的答题记录ID
            LambdaQueryWrapper<ExamQuAnswer> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ExamQuAnswer::getExamId, examId)
                   .eq(ExamQuAnswer::getUserId, userId)
                   .eq(ExamQuAnswer::getQuestionId, item.getQuestionId());
            ExamQuAnswer answer = examQuAnswerMapper.selectOne(wrapper);

            if (answer != null) {
                // 先删除旧的人工评分（防止重复插入）
                answer.setScore(item.getScore());
                examQuAnswerMapper.updateById(answer);
                
                LambdaQueryWrapper<ManualScore> deleteWrapper = new LambdaQueryWrapper<>();
                deleteWrapper.eq(ManualScore::getExamQuAnswerId, answer.getId());
                manualScoreMapper.delete(deleteWrapper);

                // 插入新的人工评分
                ManualScore manualScore = new ManualScore();
                manualScore.setExamQuAnswerId(answer.getId());
                manualScore.setUserId(SecurityUtil.getUserId()); // 批改人ID
                manualScore.setScore(item.getScore());
                manualScoreMapper.insert(manualScore);
            }
        }

        // 3. 【核心修复】重新计算整张试卷的总分
        // 逻辑：总分 = 所有题目得分之和 (优先取人工评分，没有人工评分则取系统自动评分)

        // 3.1 查出该用户的所有答题记录
        LambdaQueryWrapper<ExamQuAnswer> allAnswersWrapper = new LambdaQueryWrapper<>();
        allAnswersWrapper.eq(ExamQuAnswer::getExamId, examId)
                         .eq(ExamQuAnswer::getUserId, userId);
        List<ExamQuAnswer> allAnswers = examQuAnswerMapper.selectList(allAnswersWrapper);

        int finalTotalScore = 0;

        for (ExamQuAnswer ans : allAnswers) {
            // 尝试查找人工评分
            LambdaQueryWrapper<ManualScore> msWrapper = new LambdaQueryWrapper<>();
            msWrapper.eq(ManualScore::getExamQuAnswerId, ans.getId());
            ManualScore ms = manualScoreMapper.selectOne(msWrapper);

            if (ms != null) {
                // 场景A: 有人工评分 -> 也就是主观题，或者老师强制修改过分数的客观题
                finalTotalScore += ms.getScore();
            } else {
                // 场景B: 没有人工评分 -> 系统自动评分类 (客观题)
                // 只有答对 (isRight=1) 才加分
                if (ans.getIsRight() != null && ans.getIsRight() == 1) {
                    // 查询该题在试卷中的分值 (t_exam_question)
                    LambdaQueryWrapper<ExamQuestion> eqWrapper = new LambdaQueryWrapper<>();
                    eqWrapper.eq(ExamQuestion::getExamId, examId)
                             .eq(ExamQuestion::getQuestionId, ans.getQuestionId());
                    ExamQuestion eq = examQuestionMapper.selectOne(eqWrapper);
                    
                    if (eq != null) {
                        finalTotalScore += eq.getScore();
                    }
                }
            }
        }

        // 4. 更新用户总分和状态 (直接 set userScore，而不是 setSql user_score + ...)
        LambdaUpdateWrapper<UserExamsScore> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserExamsScore::getExamId, examId)
                     .eq(UserExamsScore::getUserId, userId)
                     .set(UserExamsScore::getUserScore, finalTotalScore) // 直接覆盖新总分
                     .set(UserExamsScore::getWhetherMark, 1); // 标记已阅卷

        userExamsScoreMapper.update(null, updateWrapper);

        // 5. 证书颁发逻辑 (保持原有业务)
        checkAndIssueCertificate(examId, userId, finalTotalScore);

        return Result.success("批改成功");
    }

    /**
     * 辅助方法：检查并颁发证书
     */
    private void checkAndIssueCertificate(Integer examId, Integer userId, Integer totalScore) {
        LambdaQueryWrapper<Exam> examWrapper = new LambdaQueryWrapper<Exam>()
                .select(Exam::getId, Exam::getCertificateId, Exam::getPassedScore)
                .eq(Exam::getId, examId);
        Exam exam = examMapper.selectOne(examWrapper);

        if (exam != null && exam.getCertificateId() != null && exam.getCertificateId() > 0) {
            // 检查是否已经发过证书，防止重复发
            LambdaQueryWrapper<CertificateUser> checkCertWrapper = new LambdaQueryWrapper<>();
            checkCertWrapper.eq(CertificateUser::getUserId, userId)
                            .eq(CertificateUser::getExamId, examId);
            Long count = certificateUserMapper.selectCount(checkCertWrapper);

            if (count == 0 && totalScore >= exam.getPassedScore()) {
                CertificateUser certificateUser = new CertificateUser();
                certificateUser.setUserId(userId);
                certificateUser.setExamId(examId);
                certificateUser.setCode(ClassTokenGenerator.generateClassToken(18));
                certificateUser.setCertificateId(exam.getCertificateId());
                certificateUserMapper.insert(certificateUser);
            }
        }
    }


    @Override
    public Result<IPage<AnswerExamVO>> examPage(Integer pageNum, Integer pageSize, String examName) {

        Page<AnswerExamVO> page = new Page<>(pageNum, pageSize);
        // 获取自己创建的考试
        List<AnswerExamVO> list = examMapper.selectMarkedList(page, SecurityUtil.getUserId(), SecurityUtil.getRole(), examName).getRecords();

        // 获取相关信息
        list.forEach(answerExamVO -> {
            // 需要参加考试人数
            answerExamVO.setClassSize(examGradeMapper.selectClassSize(answerExamVO.getExamId()));
            // 实际参加考试人数
            LambdaQueryWrapper<UserExamsScore> numberWrapper = new LambdaQueryWrapper<UserExamsScore>()
                    .eq(UserExamsScore::getExamId, answerExamVO.getExamId());
            answerExamVO.setNumberOfApplicants(userExamsScoreMapper.selectCount(numberWrapper).intValue());
            // 已阅人数
            LambdaQueryWrapper<UserExamsScore> correctedWrapper = new LambdaQueryWrapper<UserExamsScore>()
                    .eq(UserExamsScore::getWhetherMark, 1)
                    .eq(UserExamsScore::getExamId, answerExamVO.getExamId());
            answerExamVO.setCorrectedPaper(userExamsScoreMapper.selectCount(correctedWrapper).intValue());
        });
        return Result.success(null, page);

    }

    @Override

    public Result<IPage<UncorrectedUserVO>> stuExamPage(Integer pageNum, Integer pageSize, Integer examId, String realName) {
        IPage<UncorrectedUserVO> page = new Page<>(pageNum, pageSize);
        page = userExamsScoreMapper.uncorrectedUser(page, examId, realName);
        return Result.success(null, page);
    }
}
