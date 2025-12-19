<template>
  <div class="app-container">
    <div class="page-header">
      <div class="header-content">
        <i class="el-icon-edit"></i>
        <div>
          <h2 class="page-title">{{ quId ? '编辑试题' : '添加试题' }}</h2>
          <p class="page-subtitle">{{ quId ? 'Edit Question' : 'Add New Question' }}</p>
        </div>
      </div>
    </div>

    <el-form
      ref="postForm"
      :model="postForm"
      :rules="rules"
      label-position="left"
      label-width="150px"
      class="question-form"
    >
      <el-card class="form-card">
        <div class="card-title">
          <i class="el-icon-document"></i>
          基本信息
        </div>
        
        <el-form-item label="题目类型" prop="quType">
          <el-select
            v-model="postForm.quType"
            :disabled="quTypeDisabled"
            class="form-select"
            placeholder="请选择题目类型"
            @change="handleTypeChange"
          >
            <el-option
              v-for="item in quTypes"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="归属题库" prop="repoId">
          <repo-select v-model="postForm.repoId" :multi="false" class="form-select" />
        </el-form-item>

        <el-form-item label="题目内容" prop="content">
          <el-input
            v-model="postForm.content"
            type="textarea"
            :rows="4"
            resize="vertical"
            placeholder="请输入题目内容..."
            class="form-textarea"
          />
        </el-form-item>

        <el-form-item label="试题图片">
          <file-upload v-model="postForm.image" accept=".jpg,.jepg,.png" />
        </el-form-item>

        <el-form-item label="整题解析" prop="oriPrice">
          <el-input
            v-model="postForm.analysis"
            :precision="1"
            :max="999999"
            type="textarea"
            :rows="12"
            resize="vertical"
            placeholder="请输入题目解析..."
            class="form-textarea"
          />
        </el-form-item>
      </el-card>

      <el-card
        v-if="postForm.quType != 4"
        class="form-card options-card"
      >
        <div class="card-title">
          <i class="el-icon-menu"></i>
          选项配置
        </div>
        
        <el-button
          class="add-option-btn"
          type="primary"
          icon="el-icon-plus"
          size="small"
          plain
          @click="handleAdd"
        >
          添加选项
        </el-button>

        <el-table 
          :data="postForm.options.filter(option => !option.isDeleted)" 
          :border="true" 
          class="options-table"
          :header-cell-style="{
            background: 'linear-gradient(135deg, #f8fffe 0%, #e8f5f3 100%)',
            color: '#2c3e50',
            fontWeight: '600',
            fontSize: '14px'
          }"
        >
          <el-table-column label="是否答案" width="120" align="center">
            <template v-slot="scope">
              <el-checkbox v-model="scope.row.isRight" class="answer-checkbox">答案</el-checkbox>
            </template>
          </el-table-column>

          <el-table-column
            v-if="itemImage"
            label="选项图片"
            width="120px"
            align="center"
          >
            <template v-slot="scope">
              <file-upload v-model="scope.row.image" accept=".jpg,.jepg,.png" />
            </template>
          </el-table-column>

          <el-table-column label="答案内容">
            <template v-slot="scope">
              <el-input 
                v-model="scope.row.content" 
                type="textarea" 
                :rows="2"
                placeholder="请输入选项内容..."
                class="option-input"
              />
            </template>
          </el-table-column>

          <el-table-column label="操作" align="center" width="100px">
            <template v-slot="scope">
              <el-button
                type="danger"
                icon="el-icon-delete"
                circle
                size="small"
                class="delete-btn"
                @click="removeItem(scope.$index)"
              />
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <el-card
        v-if="postForm.quType == 4"
        class="form-card"
      >
        <div class="card-title">
          <i class="el-icon-edit-outline"></i>
          简答题答案
        </div>
        
        <el-table
          :data="postForm.options"
          :border="true"
          class="options-table"
          :header-cell-style="{
            background: 'linear-gradient(135deg, #f8fffe 0%, #e8f5f3 100%)',
            color: '#2c3e50',
            fontWeight: '600',
            fontSize: '14px'
          }"
        >
          <el-table-column label="答案内容">
            <template v-slot="scope">
              <el-input 
                v-model="scope.row.content" 
                type="textarea" 
                :rows="4"
                placeholder="请输入简答题参考答案..."
                class="option-input"
              />
            </template>
          </el-table-column>
        </el-table>
      </el-card>

      <div class="form-actions">
        <el-button type="primary" size="medium" icon="el-icon-check" @click="submitForm">
          保存试题
        </el-button>
        <el-button type="info" size="medium" icon="el-icon-back" @click="onCancel">
          返回列表
        </el-button>
      </div>
    </el-form>
  </div>
</template>

<script>
import { fetchDetail, quAdd, quDetail, quUpdate } from '@/api/question'
import RepoSelect from '@/components/RepoSelect'
import FileUpload from '@/components/FileUpload'

export default {
  name: 'QuDetail',
  components: { FileUpload, RepoSelect },

  data() {
    return {
      quId: '',
      quTypeDisabled: false,
      itemImage: true,

      levels: [
        { value: 1, label: '普通' },
        { value: 2, label: '较难' }
      ],

      quTypes: [
        { value: 1, label: '单选题' },
        { value: 2, label: '多选题' },
        { value: 3, label: '判断题' },
        { value: 4, label: '简答题' }
      ],

      postForm: {
        repoId: '',
        options: []
      },
      rules: {
        content: [{ required: true, message: '题目内容不能为空！' }],
        quType: [{ required: true, message: '题目类型不能为空！' }],
        level: [{ required: true, message: '必须选择难度等级！' }],
        repoId: [{ required: true, message: '请先选择题库！' }]
      }
    }
  },
  created() {
    const id = this.$route.params.id
    if (typeof id != 'undefined') {
      this.quTypeDisabled = true
      this.fetchData(id)
    }
    this.quId = localStorage.getItem('quId')
    if (this.quId) {
      this.getQuDetail()
    }
  },
  beforeDestroy() {
    localStorage.removeItem('quId')
    this.postForm = {}
  },
  methods: {
    async getQuDetail() {
      const res = await quDetail(this.quId)
      if (res.code) {
        res.data.options.forEach(item => {
          if (item.isRight) {
            item.isRight = true
          } else {
            item.isRight = false
          }
        })
        this.postForm = res.data
      }
    },
    handleTypeChange(v) {
      this.postForm.options = []
      if (v === 3) {
        this.postForm.options.push({ isRight: true, content: '正确' })
        this.postForm.options.push({ isRight: false, content: '错误' })
      }

      if (v === 1 || v === 2) {
        this.postForm.options.push({ isRight: false, content: '' })
        this.postForm.options.push({ isRight: false, content: '' })
        this.postForm.options.push({ isRight: false, content: '' })
        this.postForm.options.push({ isRight: false, content: '' })
      }
      if (v === 4) {
        this.postForm.options.push({ isRight: true, content: '' })
      }
    },

    handleAdd() {
      this.postForm.options.push({ isRight: false, content: '' })
    },

    removeItem(index) {
      const actualIndex = this.postForm.options.findIndex((option, idx) => {
        return idx === index && !option.isDeleted
      })
      if (actualIndex !== -1) {
        this.postForm.options[actualIndex].isDeleted = 1
        this.postForm.options.forEach((option, idx) => {
          if (!option.isDeleted) {
            option.sort = idx
          }
        })
      }
    },

    fetchData(id) {
      fetchDetail(id).then((response) => {
        this.postForm = response.data
      })
    },
    submitForm() {
      let rightCount = 0

      this.postForm.options.forEach(function(item) {
        if (item.isRight) {
          rightCount += 1
        }
      })

      if (this.postForm.quType === 1) {
        if (rightCount != 1) {
          this.$message({
            message: '单选题答案只能有一个',
            type: 'warning'
          })
          return
        }
      }

      if (this.postForm.quType === 2) {
        if (rightCount < 2) {
          this.$message({
            message: '多选题至少要有两个正确答案！',
            type: 'warning'
          })
          return
        }
      }

      if (this.postForm.quType === 3) {
        if (rightCount != 1) {
          this.$message({
            message: '判断题只能有一个正确项！',
            type: 'warning'
          })
          return
        }
      }

      this.$refs.postForm.validate((valid) => {
        if (!valid) {
          return
        }
        for (let i = 0; i < this.postForm.options.length; i++) {
          const option = this.postForm.options[i]
          if (option.isRight) {
            option.isRight = 1
          } else {
            option.isRight = 0
          }
        }

        if (this.quId) {
          quUpdate(this.quId, this.postForm).then(res => {
            if (res.code) {
              this.$notify({
                title: '成功',
                message: `${res.msg}`,
                type: 'success',
                duration: 2000
              })
              this.$router.push({ name: 'questions-management' })
            } else {
              this.$notify({
                title: '失败',
                message: `${res.msg}`,
                type: 'error',
                duration: 2000
              })
            }
          })
        } else {
          quAdd(this.postForm).then((response) => {
            this.postForm = response.data
            if (response.code) {
              this.$notify({
                title: '成功',
                message: '试题保存成功！',
                type: 'success',
                duration: 2000
              })
              this.$router.push({ name: 'questions-management' })
            } else {
              this.$notify({
                title: '失败',
                message: `${response.msg}`,
                type: 'error',
                duration: 2000
              })
            }
          })
        }
      })
    },
    onCancel() {
      this.$router.push({ name: 'questions-management' })
    }
  }
}
</script>

<style scoped>
.app-container {
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8f5f3 100%);
  min-height: calc(100vh - 110px);
}

/* 页面头部 */
.page-header {
  background: white;
  padding: 24px 30px;
  border-radius: 16px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.header-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-content > i {
  font-size: 32px;
  color: #1abc9c;
  background: linear-gradient(135deg, rgba(26, 188, 156, 0.1) 0%, rgba(22, 160, 133, 0.1) 100%);
  padding: 16px;
  border-radius: 12px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin: 0 0 4px 0;
}

.page-subtitle {
  font-size: 13px;
  color: #95a5a6;
  margin: 0;
  text-transform: uppercase;
  letter-spacing: 1px;
}

/* 表单卡片 */
.form-card {
  margin-bottom: 24px;
  border-radius: 16px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  transition: all 0.3s ease;
}

.form-card:hover {
  box-shadow: 0 4px 20px rgba(26, 188, 156, 0.15);
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  padding: 20px 20px 16px;
  border-bottom: 2px solid #e8f5f3;
  display: flex;
  align-items: center;
  gap: 8px;
  background: linear-gradient(135deg, #f8fffe 0%, #e8f5f3 100%);
}

.card-title i {
  color: #1abc9c;
  font-size: 18px;
}

/* 表单样式 */
.question-form {
  animation: fadeInUp 0.5s ease;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

::v-deep .el-form-item {
  margin-bottom: 24px;
  padding: 0 20px;
}

::v-deep .el-form-item__label {
  font-weight: 500;
  color: #5a6c7d;
  font-size: 14px;
}

.form-select {
  width: 400px;
}

.form-textarea {
  width: 100%;
  max-width: 1200px;
}

::v-deep .el-input__inner,
::v-deep .el-textarea__inner {
  border-radius: 8px;
  border-color: #e0e6ed;
  transition: all 0.3s ease;
}

::v-deep .el-input__inner:focus,
::v-deep .el-textarea__inner:focus {
  border-color: #1abc9c;
  box-shadow: 0 0 0 2px rgba(26, 188, 156, 0.1);
}

::v-deep .el-textarea__inner {
  min-height: 120px;
  font-size: 14px;
  line-height: 1.6;
  padding: 12px 15px;
}

/* 选项配置 */
.options-card {
  background: #fafbfc;
}

.add-option-btn {
  margin: 16px 20px 20px;
}

::v-deep .el-button--primary.is-plain {
  background: linear-gradient(135deg, rgba(26, 188, 156, 0.1) 0%, rgba(22, 160, 133, 0.1) 100%);
  border: 1px solid #1abc9c;
  color: #1abc9c;
  transition: all 0.3s ease;
}

::v-deep .el-button--primary.is-plain:hover {
  background: linear-gradient(135deg, #1abc9c 0%, #16a085 100%);
  border-color: #1abc9c;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(26, 188, 156, 0.3);
}

.options-table {
  margin: 0 20px 20px;
  border-radius: 8px;
  overflow: hidden;
}

::v-deep .options-table .el-table__body-wrapper {
  border-radius: 0 0 8px 8px;
}

.answer-checkbox {
  font-weight: 500;
}

::v-deep .el-checkbox__input.is-checked .el-checkbox__inner {
  background-color: #1abc9c;
  border-color: #1abc9c;
}

::v-deep .el-checkbox__input.is-checked + .el-checkbox__label {
  color: #1abc9c;
}

.option-input {
  width: 100%;
}

::v-deep .option-input .el-textarea__inner {
  min-height: 60px;
  border: 1px solid #e0e6ed;
}

.delete-btn {
  transition: all 0.3s ease;
}

.delete-btn:hover {
  transform: scale(1.1);
}

/* 表单操作按钮 */
.form-actions {
  display: flex;
  gap: 12px;
  padding: 24px 0;
  justify-content: center;
}

::v-deep .form-actions .el-button {
  padding: 12px 32px;
  font-size: 14px;
  font-weight: 500;
  border-radius: 8px;
  transition: all 0.3s ease;
}

::v-deep .form-actions .el-button--primary {
  background: linear-gradient(135deg, #1abc9c 0%, #16a085 100%);
  border: none;
  box-shadow: 0 4px 12px rgba(26, 188, 156, 0.3);
}

::v-deep .form-actions .el-button--primary:hover {
  background: linear-gradient(135deg, #48d5b5 0%, #1abc9c 100%);
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(26, 188, 156, 0.4);
}

::v-deep .form-actions .el-button--info {
  background: #95a5a6;
  border: none;
  color: white;
}

::v-deep .form-actions .el-button--info:hover {
  background: #7f8c8d;
  transform: translateY(-2px);
}

/* 响应式 */
@media screen and (max-width: 768px) {
  .app-container {
    padding: 15px;
  }

  .form-select,
  .form-textarea {
    width: 100% !important;
    max-width: 100%;
  }

  .form-actions {
    flex-direction: column;
  }

  ::v-deep .form-actions .el-button {
    width: 100%;
  }
}
</style>