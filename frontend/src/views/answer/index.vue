<template>
  <div class="app-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <i class="el-icon-edit-outline"></i>
        <div>
          <h2 class="page-title">阅卷管理</h2>
          <p class="page-subtitle">Marking Management</p>
        </div>
      </div>
    </div>

    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="formInline" class="search-form">
        <el-form-item label="考试名称">
          <el-input 
            v-model="searchTitle" 
            placeholder="输入考试名称" 
            clearable
            prefix-icon="el-icon-search"
            class="search-input"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="searchExam">
            查询
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card">
      <el-table
        :data="data.records"
        border
        fit
        highlight-current-row
        class="marking-table"
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
        
        <el-table-column prop="examTitle" label="考试名称" align="center" min-width="200" />
        
        <el-table-column prop="classSize" label="总人数" align="center" width="110">
          <template slot-scope="{ row }">
            <div class="total-badge">
              <i class="el-icon-user"></i>
              {{ row.classSize }}人
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="numberOfApplicants" label="实际参考人数" align="center" width="140">
          <template slot-scope="{ row }">
            <div class="attend-badge">
              <i class="el-icon-check"></i>
              {{ row.numberOfApplicants }}人
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="correctedPaper" label="已阅卷人数" align="center" width="140">
          <template slot-scope="{ row }">
            <div class="marked-badge" :class="{ 'complete': row.numberOfApplicants <= row.correctedPaper }">
              <i :class="row.numberOfApplicants <= row.correctedPaper ? 'el-icon-success' : 'el-icon-document'"></i>
              {{ row.correctedPaper }}人
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="阅卷进度" align="center" width="180">
          <template slot-scope="{ row }">
            <div class="progress-container">
              <el-progress 
                :percentage="getProgress(row)" 
                :color="getProgressColor(row)"
                :status="row.numberOfApplicants <= row.correctedPaper ? 'success' : ''"
              />
            </div>
          </template>
        </el-table-column>
        
        <el-table-column fixed="right" label="操作" align="center" width="120">
          <template slot-scope="{ row }">
            <el-button
              type="text"
              size="small"
              class="action-btn"
              :class="row.numberOfApplicants <= row.correctedPaper ? 'complete-btn' : 'view-btn'"
              icon="el-icon-view"
              :disabled="row.numberOfApplicants <= row.correctedPaper"
              @click="screenInfo(row)"
            >
              {{ row.numberOfApplicants <= row.correctedPaper ? '已完成' : '查看详情' }}
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
import { answerExamPging } from '@/api/answer'
export default {
  data() {
    return {
      pageNum: 1,
      pageSize: 10,
      data: {},
      input: '',
      input1: '',
      formInline: {
        user: '',
        region: ''
      },
      dialogTableVisible: false,
      dialogFormVisible: false,
      form: {},
      searchTitle: '',
      formLabelWidth: '120px'
    }
  },
  created() {
    this.getAnswerPage()
  },
  methods: {
    getProgress(row) {
      if (!row.numberOfApplicants || row.numberOfApplicants === 0) return 0
      return Math.round((row.correctedPaper / row.numberOfApplicants) * 100)
    },
    getProgressColor(row) {
      const progress = this.getProgress(row)
      if (progress === 100) return '#1abc9c'
      if (progress >= 60) return '#3498db'
      if (progress >= 30) return '#f39c12'
      return '#e74c3c'
    },
    searchExam() {
      this.getAnswerPage(this.pageNum, this.pageSize, this.searchTitle)
    },
    getAnswerPage(pageNum, pageSize, examName) {
      const params = { pageNum: pageNum, pageSize: pageSize, examName: examName }
      answerExamPging(params).then((res) => {
        this.data = res.data
      })
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.getAnswerPage(this.pageNum, val, this.searchTitle)
    },
    handleCurrentChange(val) {
      this.pageNum = val
      this.getAnswerPage(val, this.pageSize, this.searchTitle)
    },
    screenInfo(row) {
      localStorage.setItem('answer_examId', row.examId)
      this.$router.push({ name: 'answer-show', query: { zhi: row }})
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

.search-input {
  width: 260px;
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

::v-deep .el-button--primary {
  background: linear-gradient(135deg, #1abc9c 0%, #16a085 100%);
  border: none;
  border-radius: 8px;
  padding: 10px 20px;
  font-weight: 500;
  transition: all 0.3s ease;
}

::v-deep .el-button--primary:hover {
  background: linear-gradient(135deg, #48d5b5 0%, #1abc9c 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(26, 188, 156, 0.3);
}

/* 表格卡片 */
.table-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  animation: fadeInUp 0.5s ease 0.2s backwards;
}

.marking-table {
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

/* 统计徽章 */
.total-badge,
.attend-badge,
.marked-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
  padding: 6px 12px;
  border-radius: 6px;
}

.total-badge {
  color: #3498db;
  background: rgba(52, 152, 219, 0.1);
}

.total-badge i {
  font-size: 16px;
}

.attend-badge {
  color: #1abc9c;
  background: rgba(26, 188, 156, 0.1);
}

.attend-badge i {
  font-size: 16px;
}

.marked-badge {
  color: #f39c12;
  background: rgba(243, 156, 18, 0.1);
}

.marked-badge.complete {
  color: #27ae60;
  background: rgba(39, 174, 96, 0.1);
}

.marked-badge i {
  font-size: 16px;
}

/* 进度条 */
.progress-container {
  padding: 0 10px;
}

::v-deep .el-progress__text {
  font-weight: 600;
}

/* 操作按钮 */
.action-btn {
  font-size: 14px;
  font-weight: 500;
  padding: 6px 12px;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.view-btn {
  color: #3498db;
}

.view-btn:hover:not(.is-disabled) {
  background: rgba(52, 152, 219, 0.1);
  transform: translateY(-2px);
}

.complete-btn {
  color: #27ae60;
  cursor: not-allowed;
}

.action-btn.is-disabled {
  color: #c0c4cc;
}

/* 分页 */
.pagination-container {
  padding: 24px 0 8px;
  display: flex;
  justify-content: center;
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

  .search-input {
    width: 100%;
  }
}
</style>