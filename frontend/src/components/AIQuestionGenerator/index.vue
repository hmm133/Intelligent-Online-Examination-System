<template>
  <!-- <el-dialog
    title="AI智能生成题目"
    :visible.sync="dialogVisible"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
  > -->
    <el-dialog
    title="AI智能生成题目"
    :visible.sync="dialogVisible"
    width="600px"
    :close-on-click-modal="false"
    @close="handleClose"
    append-to-body     
    >
    <el-form
      ref="aiForm"
      :model="form"
      :rules="rules"
      label-width="120px"
      v-loading="generating"
      element-loading-text="AI正在生成题目中..."
    >
      <el-form-item label="选择题库" prop="repoId">
        <repo-select
          v-model="form.repoId"
          :multi="false"
          placeholder="请选择题库"
          style="width: 100%"
          @change="handleRepoChange"
        />
      </el-form-item>

      <el-form-item label="知识点" prop="knowledgePoint">
        <el-input
          v-model="form.knowledgePoint"
          placeholder="请输入知识点，如：Java多线程、数据结构树、微积分"
          :maxlength="100"
          show-word-limit
        />
        <div style="color: #909399; font-size: 12px; margin-top: 5px;">
          💡 提示：知识点描述越具体，生成的题目质量越高
        </div>
      </el-form-item>

      <el-form-item label="题目类型" prop="quType">
        <el-select v-model="form.quType" placeholder="请选择题型" style="width: 100%">
          <el-option label="单选题" :value="1" />
          <el-option label="多选题" :value="2" />
          <el-option label="判断题" :value="3" />
          <el-option label="简答题" :value="4" />
        </el-select>
      </el-form-item>

      <el-form-item label="难度等级" prop="difficulty">
        <el-radio-group v-model="form.difficulty">
          <el-radio :label="1">简单</el-radio>
          <el-radio :label="2">中等</el-radio>
          <el-radio :label="3">困难</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="生成数量" prop="count">
        <el-input-number
          v-model="form.count"
          :min="1"
          :max="200"
          :step="1"
          style="width: 100%"
        />
        <div style="color: #909399; font-size: 12px; margin-top: 5px;">
          单次最多生成200题，建议5-20题
        </div>
      </el-form-item>
    </el-form>

    <!-- 生成结果展示 -->
    <el-alert
      v-if="result"
      :title="resultTitle"
      :type="resultType"
      :closable="false"
      style="margin-top: 20px"
    >
      <div v-if="result.successCount > 0">
        ✅ 成功生成 <strong>{{ result.successCount }}</strong> 道题目
      </div>
      <div v-if="result.failedCount > 0" style="color: #E6A23C; margin-top: 5px;">
        ⚠️ 失败 {{ result.failedCount }} 道题目
      </div>
      <div v-if="result.questionIds && result.questionIds.length > 0" style="margin-top: 10px;">
        <el-button type="text" size="small" @click="viewGeneratedQuestions">
          查看生成的题目 →
        </el-button>
      </div>
    </el-alert>

    <div slot="footer" class="dialog-footer">
      <el-button @click="handleClose">取消</el-button>
      <el-button
        type="primary"
        :loading="generating"
        :disabled="generating"
        @click="handleGenerate"
      >
        {{ generating ? '生成中...' : '开始生成' }}
      </el-button>
    </div>
  </el-dialog>
</template>

<script>
import { aiGenerateQuestions } from '@/api/question'
import RepoSelect from '@/components/RepoSelect'

export default {
  name: 'AIQuestionGenerator',
  components: {
    RepoSelect
  },
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    defaultRepoId: {
      type: [String, Number],
      default: null
    }
  },
  data() {
    return {
      dialogVisible: false,
      generating: false,
      form: {
        repoId: null,
        knowledgePoint: '',
        quType: 1,
        difficulty: 2,
        count: 5
      },
      rules: {
        repoId: [
          { required: true, message: '请选择题库', trigger: 'change' }
        ],
        knowledgePoint: [
          { required: true, message: '请输入知识点', trigger: 'blur' },
          { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
        ],
        quType: [
          { required: true, message: '请选择题型', trigger: 'change' }
        ],
        difficulty: [
          { required: true, message: '请选择难度', trigger: 'change' }
        ],
        count: [
          { required: true, message: '请输入生成数量', trigger: 'blur' },
          { type: 'number', min: 1, max: 200, message: '数量范围 1-200', trigger: 'blur' }
        ]
      },
      result: null,
      resultTitle: '',
      resultType: 'success'
    }
  },
  watch: {
    visible: {
      handler(val) {
        this.dialogVisible = val
        if (val) {
          this.initForm()
        }
      },
      immediate: true
    },
    dialogVisible(val) {
      this.$emit('update:visible', val)
    },
    defaultRepoId: {
      handler(val) {
        if (val) {
          this.form.repoId = val
        }
      },
      immediate: true
    }
  },
  methods: {
    initForm() {
      // 重置表单
      this.$nextTick(() => {
        if (this.$refs.aiForm) {
          this.$refs.aiForm.clearValidate()
        }
      })
      
      // 如果有默认题库，使用默认值，否则重置
      if (this.defaultRepoId) {
        this.form.repoId = this.defaultRepoId
      }
      
      this.result = null
    },
    
    handleRepoChange(repo) {
      console.log('选择的题库:', repo)
    },
    
    handleGenerate() {
      this.$refs.aiForm.validate(async (valid) => {
        if (!valid) {
          return false
        }

        this.generating = true
        this.result = null

        try {
          const params = {
            repoId: this.form.repoId,
            count: this.form.count,
            quType: this.form.quType,
            knowledgePoint: this.form.knowledgePoint,
            difficulty: this.form.difficulty
          }

          const res = await aiGenerateQuestions(params)
          
          if (res.code === 1) {
            this.result = res.data
            
            if (res.data.successCount > 0) {
              this.resultType = 'success'
              this.resultTitle = '生成成功！'
              
              this.$message.success(
                `AI成功生成 ${res.data.successCount} 道题目！`
              )
              
              // 通知父组件刷新列表
              this.$emit('success', res.data)
            } else {
              this.resultType = 'error'
              this.resultTitle = '生成失败'
              this.$message.error('题目生成失败，请重试')
            }
          } else {
            this.resultType = 'error'
            this.resultTitle = '生成失败'
            this.$message.error(res.msg || '生成失败')
          }
        } catch (error) {
          console.error('AI生成题目失败:', error)
          this.resultType = 'error'
          this.resultTitle = '生成失败'
          this.$message.error('生成失败，请检查网络后重试')
        } finally {
          this.generating = false
        }
      })
    },
    
    viewGeneratedQuestions() {
      // 关闭对话框并通知父组件跳转
      this.$emit('view-questions', this.result.questionIds)
      this.handleClose()
    },
    
    handleClose() {
      if (!this.generating) {
        this.dialogVisible = false
        this.result = null
        
        // 重置表单（保留题库选择）
        const repoId = this.form.repoId
        this.$refs.aiForm.resetFields()
        this.form.repoId = repoId
      }
    }
  }
}
</script>

<style scoped>
.dialog-footer {
  text-align: right;
}

::v-deep .el-dialog__body {
  padding-top: 10px;
}

::v-deep .el-form-item__label {
  font-weight: 500;
}

::v-deep .el-alert__content {
  line-height: 1.8;
}
</style>