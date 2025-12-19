<template>
  <div class="make-test-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <el-page-header @back="goBack" :content="`批改试卷 - ${studentInfo.userName || ''}`" />
      <div class="header-actions">
        <el-button 
          type="warning" 
          icon="el-icon-magic-stick" 
          :loading="batchAILoading"
          @click="handleBatchAIScore"
        >
          批量AI评分
        </el-button>
        <el-button 
          type="success" 
          icon="el-icon-check" 
          :loading="submitLoading"
          @click="handleSubmitAll"
        >
          提交全部评分
        </el-button>
      </div>
    </div>

    <!-- 学生信息卡片 -->
    <el-card class="student-info-card" shadow="never">
      <div class="info-row">
        <div class="info-item">
          <span class="label">学生姓名：</span>
          <span class="value">{{ studentInfo.userName }}</span>
        </div>
        <div class="info-item">
          <span class="label">提交时间：</span>
          <span class="value">{{ studentInfo.limitTime }}</span>
        </div>
        <div class="info-item">
          <span class="label">当前得分：</span>
          <span class="value score">{{ totalScore }} 分</span>
        </div>
      </div>
    </el-card>

    <!-- 题目列表 -->
    <div v-loading="loading" class="questions-container">
      <div 
        v-for="(question, index) in questions" 
        :key="question.id" 
        class="question-item"
      >
        <el-card class="question-card">
          <!-- 题目头部 -->
          <div class="question-header">
            <div class="question-title">
              <span class="question-number">第 {{ index + 1}} 题</span>
              <el-tag :type="getQuestionTypeTag(question.quType)" size="small">
                {{ getQuestionTypeName(question.quType) }}
              </el-tag>
              <span class="question-score">（{{ question.score }}分）</span>
            </div>
          </div>

          <!-- 题干 -->
          <div class="question-content">
            <div class="content-label">题目：</div>
            <div class="content-text" v-html="question.content" />
            <div v-if="question.image" class="question-image">
              <el-image :src="question.image" :preview-src-list="[question.image]" />
            </div>
          </div>

          <!-- 选择题选项 -->
          <div v-if="isChoiceQuestion(question.quType)" class="question-options">
            <div class="content-label">选项：</div>
            <div 
              v-for="(option, optIndex) in question.options" 
              :key="optIndex"
              class="option-item"
              :class="{
                'correct-option': option.isRight,
                'wrong-option': isStudentWrongOption(question, option)
              }"
            >
              <span class="option-label">{{ String.fromCharCode(65 + optIndex) }}.</span>
              <span class="option-content">{{ option.content }}</span>
              <el-tag v-if="option.isRight" type="success" size="mini">正确答案</el-tag>
            </div>
          </div>

          <!-- 学生答案 -->
          <div class="student-answer">
            <div class="content-label">学生答案：</div>
            <div v-if="isChoiceQuestion(question.quType)" class="answer-content">
              {{ formatStudentAnswer(question) }}
            </div>
            <div v-else class="answer-content">
              <el-input
                :value="question.studentAnswer"
                type="textarea"
                :autosize="{ minRows: 3, maxRows: 8 }"
                readonly
              />
            </div>
          </div>

          <!-- 参考答案（主观题） -->
          <div v-if="!isChoiceQuestion(question.quType)" class="reference-answer">
            <div class="content-label">参考答案：</div>
            <div class="answer-content">
              <el-input
                :value="question.rightAnswer"
                type="textarea"
                :autosize="{ minRows: 2, maxRows: 6 }"
                readonly
              />
            </div>
          </div>

          <!-- AI评分卡片（仅主观题） -->
          <div v-if="!isChoiceQuestion(question.quType)" class="ai-score-section">
            <AIScoreCard
              :ai-score="question.aiScoreData"
              :teacher-score="question.manualScore"
              :adopted="question.aiAdopted"
              :loading="question.aiLoading"
              :error="question.aiError"
              @adopt="(score) => handleAdoptAIScore(question, score)"
              @retry="() => handleGetAIScore(question)"
            />
            <el-button
              v-if="!question.aiScoreData && !question.aiLoading"
              type="primary"
              size="small"
              icon="el-icon-magic-stick"
              @click="handleGetAIScore(question)"
            >
              获取AI评分
            </el-button>
          </div>

          <!-- 评分区域 -->
          <div class="score-input-section">
            <div class="score-input-wrapper">
              <span class="score-label">教师评分：</span>
              <el-input-number
                v-model="question.manualScore"
                :min="0"
                :max="question.score"
                :precision="1"
                :step="0.5"
                size="small"
                @change="handleScoreChange(question)"
              />
              <span class="score-max">/ {{ question.score }}分</span>
            </div>
            <div v-if="question.aiScoreData && question.manualScore !== null" class="score-diff-tip">
              <span v-if="getScoreDiff(question) === 0" class="diff-none">
                <i class="el-icon-success" /> 与AI评分一致
              </span>
              <span v-else-if="Math.abs(getScoreDiff(question)) > question.score * 0.15" class="diff-large">
                <i class="el-icon-warning" /> 差异较大({{ getScoreDiff(question) > 0 ? '+' : '' }}{{ getScoreDiff(question).toFixed(1) }}分)，建议复核
              </span>
              <span v-else class="diff-normal">
                差异：{{ getScoreDiff(question) > 0 ? '+' : '' }}{{ getScoreDiff(question).toFixed(1) }}分
              </span>
            </div>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 底部操作栏 -->
    <div class="footer-actions">
      <el-button @click="goBack">返回</el-button>
      <el-button 
        type="primary" 
        :loading="submitLoading"
        @click="handleSubmitAll"
      >
        提交评分
      </el-button>
    </div>
  </div>
</template>

<script>
import { answerDetail, correct, aiEvaluateSingle, aiEvaluateBatch } from '@/api/answer'
import AIScoreCard from '@/components/AIScoreCard'

export default {
  name: 'MakeTest',
  components: {
    AIScoreCard
  },
  data() {
    return {
      loading: false,
      submitLoading: false,
      batchAILoading: false,
      studentInfo: {},
      questions: [],
      examId: null,
      userId: null
    }
  },
  computed: {
    // 计算总分
    totalScore() {
      return this.questions.reduce((sum, q) => {
        return sum + (q.manualScore || 0)
      }, 0).toFixed(1)
    }
  },
  created() {
    this.initData()
  },
  methods: {
    // 初始化数据
    initData() {
      const answerInfo = sessionStorage.getItem('answer_info')
      if (answerInfo) {
        this.studentInfo = JSON.parse(answerInfo)
        this.examId = this.studentInfo.examId
        this.userId = this.studentInfo.userId
        this.loadAnswerDetail()
      } else {
        this.$message.error('未找到答题信息')
        this.goBack()
      }
    },


    // 加载答题详情
    async loadAnswerDetail() {
      this.loading = true
      try {
        const params = {
          examId: this.examId,
          userId: this.userId
        }
        const res = await answerDetail(params)
        
        if (res.code === 1) {
          // 处理题目数据
          this.questions = res.data.map(q => {
            
            // 1. 构建 AI 评分对象 (如果有数据的话)
            let aiData = null
            // 只有当 aiScore 不为 null 时，才认为有 AI 评分记录
            if (q.aiScore !== null && q.aiScore !== undefined) {
              aiData = {
                aiScore: q.aiScore,
                // 注意：后端 VO 里叫 aiReason，前端组件可能需要 aiComment，这里做个映射
                aiComment: q.aiReason || q.aiComment, 
                aiConfidence: q.aiConfidence
              }
            }

            // 2. 返回处理后的对象
            return {
              ...q,
              // 如果有人工分显示人工分，否则为0
              manualScore: q.userScore || 0,
              
              // 【核心修改】这里不再写死为 null，而是使用刚才构建的 aiData
              aiScoreData: aiData,
              
              // 如果 AI 有分，且和人工分(userScore)一致，则认为已采纳
              aiAdopted: aiData && (q.userScore === aiData.aiScore),
              
              aiLoading: false,
              aiError: ''
            }
          })
        } else {
          this.$message.error(res.msg || '加载答题详情失败')
        }
      } catch (error) {
        console.error('加载答题详情失败:', error)
        this.$message.error('加载答题详情失败')
      } finally {
        this.loading = false
      }
    },

    // 获取单题AI评分
    async handleGetAIScore(question) {
      question.aiLoading = true
      question.aiError = ''
      
      try {
        const res = await aiEvaluateSingle({
          examId: this.examId,
          userId: this.userId,
          questionId: question.id
        })
        
        if (res.code === 1) {
          question.aiScoreData = res.data
          this.$message.success('AI评分完成')
        } else {
          question.aiError = res.msg || 'AI评分失败'
          this.$message.error(question.aiError)
        }
      } catch (error) {
        console.error('AI评分失败:', error)
        question.aiError = 'AI评分异常，请稍后重试'
        this.$message.error(question.aiError)
      } finally {
        question.aiLoading = false
      }
    },

    // 批量AI评分
    async handleBatchAIScore() {
      this.$confirm('将对所有主观题进行AI评分，是否继续？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        this.batchAILoading = true
        
        try {
          const res = await aiEvaluateBatch(this.examId)
          
          if (res.code === 1) {
            this.$message.success(res.msg || '批量AI评分完成')
            // 重新加载答题详情以获取AI评分结果
            await this.loadAnswerDetail()
          } else {
            this.$message.error(res.msg || '批量AI评分失败')
          }
        } catch (error) {
          console.error('批量AI评分失败:', error)
          this.$message.error('批量AI评分异常')
        } finally {
          this.batchAILoading = false
        }
      }).catch(() => {})
    },

    // 采纳AI分数
    handleAdoptAIScore(question, score) {
      question.manualScore = score
      question.aiAdopted = true
      this.$message.success('已采纳AI评分')
    },

    // 分数变化
    handleScoreChange(question) {
      // 检查是否与AI评分一致
      if (question.aiScoreData) {
        question.aiAdopted = (question.manualScore === question.aiScoreData.aiScore)
      }
    },

    // 获取分数差异
    getScoreDiff(question) {
      if (!question.aiScoreData) return 0
      return question.manualScore - question.aiScoreData.aiScore
    },

    // 提交全部评分
    async handleSubmitAll() {
      // 检查是否所有题目都已评分
      const unscored = this.questions.filter(q => q.manualScore === null || q.manualScore === undefined)
      if (unscored.length > 0) {
        this.$message.warning('请完成所有题目的评分')
        return
      }

      this.$confirm('确定提交评分吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        this.submitLoading = true
        
        try {
          // 构造评分数据
          const scoreData = this.questions.map(q => ({
            questionId: q.id,
            score: q.manualScore,
            examId: this.examId,
            userId: this.userId
          }))

          const res = await correct(scoreData)
          
          if (res.code === 1) {
            this.$message.success('评分提交成功')
            this.goBack()
          } else {
            this.$message.error(res.msg || '评分提交失败')
          }
        } catch (error) {
          console.error('评分提交失败:', error)
          this.$message.error('评分提交异常')
        } finally {
          this.submitLoading = false
        }
      }).catch(() => {})
    },

    // 返回
    goBack() {
      this.$router.go(-1)
    },

    // 判断是否为选择题
    isChoiceQuestion(quType) {
      return [1, 2, 3].includes(quType)
    },

    // 获取题型标签类型
    getQuestionTypeTag(quType) {
      const types = {
        1: 'success',
        2: 'warning',
        3: 'danger',
        4: 'info',
        5: 'info'
      }
      return types[quType] || 'info'
    },

    // 获取题型名称
    getQuestionTypeName(quType) {
      const names = {
        1: '单选题',
        2: '多选题',
        3: '判断题',
        4: '简答题',
        5: '填空题'
      }
      return names[quType] || '未知题型'
    },

    // 格式化学生答案（选择题）
    formatStudentAnswer(question) {
      if (!question.studentAnswer) return '未作答'
      if (!question.options) return question.studentAnswer // 防御性代码
      
      // 1. 将答案字符串分割为数组 (处理多选 "1,2,3")
      const answerIds = question.studentAnswer.split(',')
      
      // 2. 遍历答案ID，找到它在 options 列表中的索引
      const result = []
      answerIds.forEach(id => {
        // 注意：数据类型可能不一致，转为字符串比较
        const index = question.options.findIndex(opt => String(opt.id) === String(id))
        if (index !== -1) {
          // 找到索引，转为字母 (0->A, 1->B...)
          result.push(String.fromCharCode(65 + index))
        } else {
          // 找不到（理论上不应发生），显示原始值方便调试
          result.push('?')
        }
      })
      
      return result.join(', ')
    },
    
    // 同时修改判断错题的逻辑，也需要匹配 ID
    isStudentWrongOption(question, option) {
      if (!question.studentAnswer) return false
      const answerIds = question.studentAnswer.split(',')
      // 判断当前选项的 ID 是否在用户的答案 ID 列表中
      const isSelected = answerIds.some(id => String(id) === String(option.id))
      return isSelected && !option.isRight
    },


  }
}
</script>

<style scoped lang="scss">
.make-test-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    padding: 15px;
    background: white;
    border-radius: 4px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);

    .header-actions {
      display: flex;
      gap: 10px;
    }
  }

  .student-info-card {
    margin-bottom: 20px;

    .info-row {
      display: flex;
      gap: 30px;

      .info-item {
        .label {
          color: #909399;
          margin-right: 8px;
        }

        .value {
          color: #303133;
          font-weight: 500;

          &.score {
            color: #409EFF;
            font-size: 18px;
            font-weight: bold;
          }
        }
      }
    }
  }

  .questions-container {
    margin-bottom: 80px;

    .question-item {
      margin-bottom: 20px;

      .question-card {
        .question-header {
          margin-bottom: 15px;

          .question-title {
            display: flex;
            align-items: center;
            gap: 10px;

            .question-number {
              font-size: 18px;
              font-weight: bold;
              color: #303133;
            }

            .question-score {
              color: #909399;
            }
          }
        }

        .question-content,
        .student-answer,
        .reference-answer {
          margin-bottom: 15px;

          .content-label {
            font-weight: bold;
            color: #606266;
            margin-bottom: 8px;
          }

          .content-text {
            padding: 10px;
            background-color: #f5f7fa;
            border-radius: 4px;
            line-height: 1.6;
          }

          .answer-content {
            padding: 10px;
            background-color: #f5f7fa;
            border-radius: 4px;
          }
        }

        .question-image {
          margin-top: 10px;
        }

        .question-options {
          margin-bottom: 15px;

          .content-label {
            font-weight: bold;
            color: #606266;
            margin-bottom: 8px;
          }

          .option-item {
            padding: 10px;
            margin: 8px 0;
            border: 1px solid #DCDFE6;
            border-radius: 4px;
            background-color: white;

            &.correct-option {
              background-color: #f0f9ff;
              border-color: #67C23A;
            }

            &.wrong-option {
              background-color: #fef0f0;
              border-color: #F56C6C;
            }

            .option-label {
              font-weight: bold;
              margin-right: 8px;
              color: #409EFF;
            }
          }
        }

        .ai-score-section {
          margin: 15px 0;
        }

        .score-input-section {
          margin-top: 20px;
          padding: 15px;
          background-color: #f5f7fa;
          border-radius: 4px;

          .score-input-wrapper {
            display: flex;
            align-items: center;
            gap: 10px;

            .score-label {
              font-weight: bold;
              color: #606266;
            }

            .score-max {
              color: #909399;
            }
          }

          .score-diff-tip {
            margin-top: 10px;
            font-size: 14px;

            .diff-none {
              color: #67C23A;
            }

            .diff-normal {
              color: #606266;
            }

            .diff-large {
              color: #F56C6C;
              font-weight: bold;
            }
          }
        }
      }
    }
  }

  .footer-actions {
    position: fixed;
    bottom: 0;
    left: 0;
    right: 0;
    padding: 15px 20px;
    background: white;
    border-top: 1px solid #DCDFE6;
    box-shadow: 0 -2px 4px rgba(0, 0, 0, 0.1);
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    z-index: 100;
  }
}
</style>