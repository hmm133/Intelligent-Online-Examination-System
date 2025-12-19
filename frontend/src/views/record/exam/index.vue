<template>
  <div class="app-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <i class="el-icon-document-copy"></i>
        <div>
          <h2 class="page-title">考试记录</h2>
          <p class="page-subtitle">Exam Records</p>
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

      <!-- 排序开关 -->
      <div class="sort-section">
        <span class="sort-label">
          <i class="el-icon-time"></i>
          完成时间：
        </span>
        <el-switch
          v-model="isASC"
          active-text="升序"
          inactive-text="降序"
          active-color="#1abc9c"
          inactive-color="#3498db"
          @change="toggleSort"
        />
      </div>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card">
      <el-table
        :data="data.records"
        border
        fit
        highlight-current-row
        class="exam-record-table"
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
        
        <el-table-column prop="title" align="center" label="试卷名称" min-width="220" />
        
        <el-table-column prop="passedScore" align="center" label="及格分" width="100">
          <template slot-scope="{ row }">
            <div class="pass-score">
              <i class="el-icon-success"></i>
              {{ row.passedScore }}
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="userScore" align="center" label="用户成绩" width="120">
          <template slot-scope="{ row }">
            <el-tooltip 
              :content="row.userScore >= row.passedScore ? '及格' : '不及格'" 
              placement="top"
            >
              <div 
                class="user-score-badge"
                :class="row.userScore >= row.passedScore ? 'passed' : 'failed'"
              >
                <i :class="row.userScore >= row.passedScore ? 'el-icon-circle-check' : 'el-icon-circle-close'"></i>
                {{ row.userScore }}分
              </div>
            </el-tooltip>
          </template>
        </el-table-column>
        
        <el-table-column prop="examDuration" align="center" label="考试时长（分钟）" width="150">
          <template slot-scope="{ row }">
            <div class="duration-badge">
              <i class="el-icon-time"></i>
              {{ row.examDuration }}分钟
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="userTime" align="center" label="用户用时" width="120">
          <template slot-scope="{ row }">
            <div class="time-used">
              <i class="el-icon-timer"></i>
              {{ Math.ceil(row.userTime / 60) }}分钟
            </div>
          </template>
        </el-table-column>
        
        <el-table-column align="center" label="操作" width="120">
          <template slot-scope="{ row }">
            <el-button
              type="text"
              size="small"
              class="action-btn view-btn"
              icon="el-icon-view"
              @click="screenInfo(row)"
            >
              查看
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
import { recordExamPaging } from '@/api/record'
export default {
  namespaced: true,
  data() {
    return {
      pageNum: 1,
      pageSize: 10,
      data: {},
      input: '',
      searchTitle: '',
      isASC: false,
      formInline: {
        user: '',
        region: ''
      },
      dialogVisible: false,
      form: {
        name: ''
      },
      cancle() {},
      updateRow(row) {
        this.dialogFormVisible = true
        this.form = row
      },
      diaTitle: '新增',
      dialogTableVisible: false,
      dialogFormVisible: false
    }
  },
  computed: {
    tables() {
      const input = this.input
      if (input) {
        return this.tableData.filter((data) => {
          return Object.keys(data).some((key) => {
            return String(data[key]).toLowerCase().indexOf(input) > -1
          })
        })
      }
      return this.tableData
    }
  },
  created() {
    this.getExamRecordPaging()
  },
  methods: {
    searchExam() {
      this.getExamRecordPaging(this.pageNum, this.pageSize, this.searchTitle)
    },
    toggleSort() {
      this.getExamRecordPaging(this.pageNum, this.pageSize, this.searchTitle)
    },
    async getExamRecordPaging(pageNum, pageSize, examName) {
      const params = { pageNum: pageNum, pageSize: pageSize, examName: examName, isASC: this.isASC }
      const res = await recordExamPaging(params)
      this.data = res.data
    },
    screenInfo(row) {
      localStorage.setItem('record_exam_examId', row.id)
      this.$router.push({ name: 'exam-record-detail', query: { zhi: row }})
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.getExamRecordPaging(this.pageNum, val, this.searchTitle)
    },
    handleCurrentChange(val) {
      this.pageNum = val
      this.getExamRecordPaging(val, this.pageSize, this.searchTitle)
    },
    handleClose(done) {
      this.$confirm('确认关闭？')
        .then((_) => {
          done()
        })
        .catch((_) => {})
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

/* 排序区域 */
.sort-section {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-top: 16px;
  border-top: 1px solid #e8f5f3;
}

.sort-label {
  font-size: 14px;
  font-weight: 500;
  color: #5a6c7d;
  display: flex;
  align-items: center;
  gap: 6px;
}

.sort-label i {
  color: #1abc9c;
  font-size: 16px;
}

::v-deep .el-switch__label {
  color: #5a6c7d;
  font-weight: 500;
}

::v-deep .el-switch__label.is-active {
  color: #1abc9c;
}

/* 表格卡片 */
.table-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  animation: fadeInUp 0.5s ease 0.2s backwards;
}

.exam-record-table {
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

.pass-score {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #27ae60;
  font-weight: 500;
}

.pass-score i {
  font-size: 16px;
}

.user-score-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.user-score-badge i {
  font-size: 16px;
}

.user-score-badge.passed {
  color: #27ae60;
  background: #f0f9eb;
}

.user-score-badge.failed {
  color: #e74c3c;
  background: #fef0f0;
}

.duration-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #3498db;
  font-weight: 500;
}

.duration-badge i {
  font-size: 16px;
}

.time-used {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #1abc9c;
  font-weight: 500;
}

.time-used i {
  font-size: 16px;
}

/* 操作按钮 */
.action-btn {
  font-size: 14px;
  font-weight: 500;
  padding: 6px 10px;
  border-radius: 6px;
  transition: all 0.3s ease;
}

.view-btn {
  color: #3498db;
}

.view-btn:hover {
  background: rgba(52, 152, 219, 0.1);
  transform: translateY(-2px);
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

  .sort-section {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>