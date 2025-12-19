<template>
  <div class="app-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <i class="el-icon-tickets"></i>
        <div>
          <h2 class="page-title">试题管理</h2>
          <p class="page-subtitle">Question Management</p>
        </div>
      </div>
    </div>

    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="formInline" class="search-form">
        <el-form-item label="试题名称">
          <el-input 
            v-model="searchName" 
            placeholder="输入试题名称" 
            clearable
            prefix-icon="el-icon-search"
            class="search-input"
          />
        </el-form-item>
        
        <el-form-item label="所属题库">
          <repo-select
            v-model="selectedRepoSingleSearch"
            @change="handleRepoChangeSingle"
            class="search-select"
          />
        </el-form-item>
        
        <el-form-item label="题库类型">
          <el-select 
            v-model="selValue" 
            placeholder="请选择"
            clearable
            class="search-select"
          >
            <el-option
              v-for="item in options"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="searchQu">
            查询
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 操作按钮组 -->
      <div class="action-buttons">
        <el-button type="primary" icon="el-icon-plus" @click="screenInfo()">
          新增试题
        </el-button>
        <el-button type="success" icon="el-icon-upload2" @click="fileDialogVisible = true">
          导入试题
        </el-button>
        <el-button type="warning" icon="el-icon-magic-stick" @click="openAIDialog">
          AI生成
        </el-button>
      </div>
    </el-card>

    <!-- 文件上传对话框 -->
    <el-dialog
      width="500px"
      :show-close="false"
      :close-on-click-modal="false"
      title="导入试题"
      :visible.sync="fileDialogVisible"
      class="upload-dialog"
    >
      <div class="upload-content">
        <div class="upload-tip">
          <i class="el-icon-info"></i>
          请先选择题库，然后上传Excel文件
        </div>
        
        <div class="repo-select-wrapper">
          <label>选择题库：</label>
          <repo-select
            v-model="selectedRepoSingle"
            @change="handleRepoChangeSingle"
          />
        </div>

        <el-upload
          class="upload-demo"
          drag
          action="xxxxxx"
          multiple
          :limit="1"
          accept=".xlsx, .xls"
          :auto-upload="false"
          :on-remove="handleRemove"
          :on-change="handleFileChange"
          :file-list="fileList"
        >
          <i class="el-icon-upload"></i>
          <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
          <div slot="tip" class="el-upload__tip">只能上传 xls/xlsx文件，且不超过500kb</div>
        </el-upload>
      </div>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="fileDialogVisible = false">取消</el-button>
        <el-button type="success" plain icon="el-icon-download" @click="startDownload">
          下载模板
        </el-button>
        <el-button type="primary" icon="el-icon-check" @click="importQu">
          确定导入
        </el-button>
      </div>
    </el-dialog>

    <!-- AI生成对话框 -->
    <ai-question-generator
      :visible.sync="aiDialogVisible"
      :default-repo-id="selectedRepoSingleSearch"
      @success="handleAIGenerateSuccess"
      @view-questions="handleViewQuestions"
    />

    <!-- 数据表格 -->
    <el-card class="table-card">
      <el-table
        :data="data.records"
        border
        fit
        highlight-current-row
        class="question-table"
        :header-cell-style="{
          background: 'linear-gradient(135deg, #f8fffe 0%, #e8f5f3 100%)',
          color: '#2c3e50',
          fontWeight: '600',
          fontSize: '14px',
          padding: '16px 0'
        }"
      >
        <el-table-column align="center" type="selection" width="55" />
        
        <el-table-column label="序号" align="center" width="80">
          <template slot-scope="scope">
            <span class="row-number">{{ scope.$index + 1 }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="content" label="题干" align="left" min-width="300">
          <template slot-scope="scope">
            <div class="question-content">{{ scope.row.content }}</div>
          </template>
        </el-table-column>
        
        <el-table-column label="题目类型" align="center" width="120">
          <template slot-scope="scope">
            <el-tag
              :type="getQuTypeTag(scope.row.quType)"
              size="small"
              effect="plain"
              class="type-tag"
            >
              <span v-if="scope.row.quType == 1">单选题</span>
              <span v-else-if="scope.row.quType == 2">多选题</span>
              <span v-else-if="scope.row.quType == 3">判断题</span>
              <span v-else-if="scope.row.quType == 4">简答题</span>
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="repoTitle" label="所属题库" align="center" width="150">
          <template slot-scope="scope">
            <div class="repo-title">{{ scope.row.repoTitle }}</div>
          </template>
        </el-table-column>
        
        <el-table-column prop="createTime" label="创建时间" align="center" width="180">
          <template slot-scope="scope">
            <div class="create-time">
              <i class="el-icon-time"></i>
              {{ scope.row.createTime }}
            </div>
          </template>
        </el-table-column>
        
        <el-table-column align="center" label="操作" width="180" fixed="right">
          <template slot-scope="{ row }">
            <el-button
              type="text"
              size="small"
              class="action-btn edit-btn"
              icon="el-icon-edit"
              @click="updateRow(row)"
            >
              编辑
            </el-button>
            <el-button
              type="text"
              size="small"
              class="action-btn delete-btn"
              icon="el-icon-delete"
              @click="delQu(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          :current-page="data.current"
          :page-sizes="[10, 20, 30, 40]"
          :page-size="data.size"
          layout="total, sizes, prev, pager, next, jumper"
          :total="data.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script>
import { quPaging, quDel, quUpdate, importQue } from '@/api/question'
import RepoSelect from '@/components/RepoSelect'
import AIQuestionGenerator from '@/components/AIQuestionGenerator'

export default {
  components: { 
    RepoSelect,
    'ai-question-generator': AIQuestionGenerator
  },
  data() {
    return {
      options: [
        { value: null, label: '全部类型' },
        { value: 1, label: '单选题' },
        { value: 2, label: '多选题' },
        { value: 3, label: '判断题' },
        { value: 4, label: '简答题' }
      ],
      length: '',
      fileList: [],
      selValue: '',
      searchName: '',
      pageNum: 1,
      pageSize: 10,
      data: {},
      fileDialogVisible: false,
      aiDialogVisible: false,
      selectedRepoSingle: '',
      selectedRepoSingleSearch: '',
      input: '',
      input1: '',
      formInline: {
        user: '',
        region: ''
      },
      cancle() {},
      dialogTableVisible: false,
      dialogFormVisible: false,
      hasFiles: null,
      form: {
        name: '',
        region: '',
        date1: '',
        date2: '',
        delivery: false,
        type: [],
        resource: '',
        desc: ''
      },
      formLabelWidth: '110px'
    }
  },
  created() {
    this.getQuPage()
  },
  methods: {
    getQuTypeTag(type) {
      const tags = {
        1: 'success',
        2: 'warning',
        3: 'info',
        4: ''
      }
      return tags[type] || ''
    },
    
    handleRepoChangeSingle(repo) {
      console.log('单选题库变化:', repo)
    },
    
    openAIDialog() {
      console.log('按钮被点击了！准备打开弹窗...')
      this.aiDialogVisible = true
    },
    
    handleAIGenerateSuccess(result) {
      console.log('AI生成成功:', result)
      this.getQuPage(this.pageNum, this.pageSize, this.searchName, this.selectedRepoSingleSearch, this.selValue)
    },
    
    handleViewQuestions(questionIds) {
      console.log('查看题目:', questionIds)
      this.$message.info('题目已生成，刷新列表即可查看')
    },
    
    updateRow(row) {
      localStorage.setItem('quId', row.id)
      this.$router.push({ name: 'questions-add' })
    },
    
    importQu() {
      if (this.fileList && this.fileList.length > 0 && this.selectedRepoSingle != '') {
        const formData = new FormData()
        formData.append('file', this.fileList[0].raw)
        importQue(this.selectedRepoSingle, formData)
          .then((response) => {
            if (response.code) {
              this.$message.success('导入成功！')
              this.getQuPage(this.pageNum, this.pageSize)
              this.fileDialogVisible = false
              this.selectedRepoSingle = ''
              this.fileList = []
            } else {
              this.$message({
                type: 'error',
                message: response.msg
              })
              this.fileList = []
            }
          })
          .catch((error) => {
            console.error('文件上传失败：', error)
            this.$message.error('文件上传失败！')
            this.fileList = []
          })
      } else {
        this.$message.warning('请选择文件后再上传！')
      }
    },
    
    handleFileChange(file, fileList) {
      this.fileList = fileList
    },
    
    handleRemove(file, fileList) {
      if (fileList.length === 0) {
        this.hasFiles = false
      }
    },
    
    async getQuPage(pageNum, pageSize, content = null, repoId = null, type = null) {
      const params = {
        pageNum: pageNum,
        pageSize: pageSize,
        content: content,
        repoId: repoId,
        type: type
      }
      const res = await quPaging(params)
      this.data = res.data
    },
    
    updateQu() {
      quUpdate(this.form.id, { title: this.form.title })
        .then((res) => {
          if (res.code) {
            this.getQuPage(
              this.pageNum,
              this.pageSize,
              this.searchName,
              this.selectedRepoSingleSearch,
              this.selValue
            )
            this.dialogFormVisible = false
            this.$message({
              type: 'success',
              message: '编辑成功!'
            })
          } else {
            this.$message({
              type: 'info',
              message: res.msg
            })
          }
        })
        .catch(() => {
          this.$message({
            type: 'info',
            message: '取消编辑'
          })
        })
    },
    
    delQu(row) {
      this.$confirm('此操作将永久删除该试题, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        center: true
      })
        .then(() => {
          quDel(row.id).then((res) => {
            if (res.code) {
              this.getQuPage(
                this.pageNum,
                this.pageSize,
                this.searchName,
                this.selectedRepoSingleSearch,
                this.selValue
              )
              this.$message({
                type: 'success',
                message: '删除成功!'
              })
            } else {
              this.$message({
                type: 'info',
                message: res.msg
              })
            }
          })
        })
        .catch(() => {
          this.$message({
            type: 'info',
            message: '已取消删除'
          })
        })
    },
    
    searchQu() {
      this.getQuPage(
        this.pageNum,
        this.pageSize,
        this.searchName,
        this.selectedRepoSingleSearch,
        this.selValue
      )
    },

    screenInfo(row, index, done) {
      this.$router.push({ name: 'questions-add', query: { zhi: row }})
    },

    handleSizeChange(val) {
      this.pageSize = val
      this.getQuPage(this.pageNum, val, this.searchName, this.selectedRepoSingleSearch, this.selValue)
    },
    
    handleCurrentChange(val) {
      this.pageNum = val
      this.getQuPage(val, this.pageSize, this.searchName, this.selectedRepoSingleSearch, this.selValue)
    },
    
    async startDownload() {
      const a = document.createElement('a')
      a.href = './template/ImportQuestionTemplate.xlsx'
      a.download = '导入试题模板.xlsx'
      a.style.display = 'none'
      document.body.appendChild(a)
      a.click()
      a.remove()
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
  animation: fadeInDown 0.5s ease;
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
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

/* 搜索卡片 */
.search-card {
  margin-bottom: 24px;
  border-radius: 16px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  animation: fadeInUp 0.5s ease 0.1s backwards;
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

.search-form {
  margin-bottom: 0;
}

::v-deep .search-form .el-form-item {
  margin-bottom: 16px;
}

::v-deep .search-form .el-form-item__label {
  font-weight: 500;
  color: #5a6c7d;
}

.search-input,
.search-select {
  width: 200px;
}

::v-deep .search-input .el-input__inner {
  border-radius: 8px;
  border-color: #e0e6ed;
  transition: all 0.3s ease;
}

::v-deep .search-input .el-input__inner:focus {
  border-color: #1abc9c;
  box-shadow: 0 0 0 2px rgba(26, 188, 156, 0.1);
}

/* 操作按钮组 */
.action-buttons {
  display: flex;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid #e8f5f3;
  flex-wrap: wrap;
}

::v-deep .action-buttons .el-button {
  border-radius: 8px;
  padding: 10px 20px;
  font-weight: 500;
  transition: all 0.3s ease;
}

::v-deep .el-button--primary {
  background: linear-gradient(135deg, #1abc9c 0%, #16a085 100%);
  border: none;
}

::v-deep .el-button--primary:hover {
  background: linear-gradient(135deg, #48d5b5 0%, #1abc9c 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(26, 188, 156, 0.3);
}

::v-deep .el-button--success {
  background: linear-gradient(135deg, #2ecc71 0%, #27ae60 100%);
  border: none;
}

::v-deep .el-button--success:hover {
  background: linear-gradient(135deg, #54d98c 0%, #2ecc71 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(46, 204, 113, 0.3);
}

::v-deep .el-button--warning {
  background: linear-gradient(135deg, #f39c12 0%, #e67e22 100%);
  border: none;
}

::v-deep .el-button--warning:hover {
  background: linear-gradient(135deg, #f5ab35 0%, #f39c12 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(243, 156, 18, 0.3);
}

/* 上传对话框 */
::v-deep .upload-dialog .el-dialog {
  border-radius: 16px;
}

::v-deep .upload-dialog .el-dialog__header {
  background: linear-gradient(135deg, #f8fffe 0%, #e8f5f3 100%);
  padding: 20px 24px;
  border-radius: 16px 16px 0 0;
}

::v-deep .upload-dialog .el-dialog__title {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

.upload-content {
  padding: 20px 0;
}

.upload-tip {
  background: linear-gradient(135deg, rgba(26, 188, 156, 0.1) 0%, rgba(22, 160, 133, 0.05) 100%);
  border-left: 3px solid #1abc9c;
  padding: 12px 16px;
  margin-bottom: 20px;
  border-radius: 4px;
  color: #5a6c7d;
  display: flex;
  align-items: center;
  gap: 8px;
}

.upload-tip i {
  color: #1abc9c;
  font-size: 18px;
}

.repo-select-wrapper {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.repo-select-wrapper label {
  font-weight: 500;
  color: #5a6c7d;
  white-space: nowrap;
}

::v-deep .el-upload-dragger {
  border-radius: 8px;
  border: 2px dashed #d0e8e3;
  background: #f8fffe;
  transition: all 0.3s ease;
}

::v-deep .el-upload-dragger:hover {
  border-color: #1abc9c;
  background: #e8f5f3;
}

::v-deep .el-upload-dragger .el-icon-upload {
  color: #1abc9c;
  font-size: 64px;
  margin: 20px 0;
}

/* 表格卡片 */
.table-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  animation: fadeInUp 0.5s ease 0.2s backwards;
}

.question-table {
  border-radius: 8px;
  overflow: hidden;
}

.row-number {
  display: inline-block;
  width: 32px;
  height: 32px;
  line-height: 32px;
  background: linear-gradient(135deg, #1abc9c 0%, #16a085 100%);
  color: white;
  border-radius: 50%;
  font-weight: 600;
  font-size: 14px;
}

.question-content {
  color: #2c3e50;
  line-height: 1.6;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.type-tag {
  font-weight: 500;
  border-radius: 6px;
  padding: 6px 12px;
}

.repo-title {
  color: #5a6c7d;
  font-weight: 500;
}

.create-time {
  color: #7f8c8d;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.create-time i {
  color: #1abc9c;
}

/* 操作按钮 */
.action-btn {
  font-size: 14px;
  font-weight: 500;
  padding: 8px 12px;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.edit-btn {
  color: #1abc9c;
}

.edit-btn:hover {
  background: rgba(26, 188, 156, 0.1);
  transform: translateY(-2px);
}

.delete-btn {
  color: #e74c3c;
}

.delete-btn:hover {
  background: rgba(231, 76, 60, 0.1);
  transform: translateY(-2px);
}

/* 分页 */
.pagination-container {
  padding: 24px 0 8px;
  display: flex;
  justify-content: center;
}

::v-deep .el-pagination {
  font-weight: 500;
}

::v-deep .el-pagination.is-background .el-pager li:not(.disabled).active {
  background: linear-gradient(135deg, #1abc9c 0%, #16a085 100%);
  color: white;
}

::v-deep .el-pagination.is-background .el-pager li:hover {
  color: #1abc9c;
}

/* 响应式 */
@media screen and (max-width: 768px) {
  .app-container {
    padding: 15px;
  }

  .search-input,
  .search-select {
    width: 100%;
  }

  .action-buttons {
    flex-direction: column;
  }

  ::v-deep .action-buttons .el-button {
    width: 100%;
  }
}
</style>