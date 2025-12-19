<template>
  <div class="app-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <i class="el-icon-data-analysis"></i>
        <div>
          <h2 class="page-title">成绩详情</h2>
          <p class="page-subtitle">Score Details</p>
        </div>
      </div>
    </div>

    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="formInline" class="search-form">
        <el-form-item label="真实姓名">
          <el-input 
            v-model="realName" 
            placeholder="输入真实姓名" 
            clearable
            prefix-icon="el-icon-search"
            class="search-input"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="onSubmit">
            查询
          </el-button>
          <el-button type="success" icon="el-icon-download" @click="getExportScores">
            导出
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
        class="score-detail-table"
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
        
        <el-table-column prop="title" label="试卷名称" align="center" min-width="200" />
        
        <el-table-column prop="realName" label="真实姓名" align="center" width="120">
          <template slot-scope="{ row }">
            <div class="student-name">
              <i class="el-icon-user"></i>
              {{ row.realName }}
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="userScore" label="用户得分" align="center" width="110">
          <template slot-scope="{ row }">
            <div class="user-score">
              <i class="el-icon-medal-1"></i>
              {{ row.userScore }}分
            </div>
          </template>
        </el-table-column>
        
        <!-- AI评分相关列 -->
        <el-table-column label="AI评分" align="center" width="100">
          <template slot-scope="{ row }">
            <div v-if="row.aiScore !== null && row.aiScore !== undefined" class="ai-score">
              <i class="el-icon-cpu"></i>
              {{ row.aiScore }}
            </div>
            <span v-else class="no-data">-</span>
          </template>
        </el-table-column>
        
        <el-table-column label="评分差异" align="center" width="110">
          <template slot-scope="{ row }">
            <el-tag 
              v-if="row.aiScore !== null && row.aiScore !== undefined"
              :type="getScoreDiffType(row)"
              size="small"
              effect="plain"
              class="diff-tag"
            >
              {{ getScoreDiff(row) }}
            </el-tag>
            <span v-else class="no-data">-</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="count" label="切屏次数" align="center" width="100">
          <template slot-scope="{ row }">
            <el-tag 
              :type="row.count > 0 ? 'warning' : 'success'" 
              size="small"
              class="count-tag"
            >
              {{ row.count }}次
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="userTime" label="用户用时" align="center" width="110">
          <template slot-scope="{ row }">
            <div class="time-badge">
              <i class="el-icon-time"></i>
              {{ row.userTime }}
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="limitTime" label="提交时间" align="center" width="180">
          <template slot-scope="{ row }">
            <div class="submit-time">
              <i class="el-icon-date"></i>
              {{ row.limitTime }}
            </div>
          </template>
        </el-table-column>
        
        <el-table-column fixed="right" label="操作" align="center" width="120">
          <template slot-scope="{ row }">
            <el-button
              type="text"
              size="small"
              class="action-btn view-btn"
              icon="el-icon-view"
              @click="updateRow(row)"
            >
              查看详情
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
import { scorePaging, exportScores } from '@/api/score'

export default {
  data() {
    return {
      pageNum: 1,
      pageSize: 10,
      gradeId: '',
      examId: '',
      realName: '',
      examTitle: '',
      gradeName: '',
      data: {},
      formInline: {
        user: '',
        region: ''
      },
      input: '',
      input1: '',
      form: {
        name: ''
      },
      diaTitle: '',
      dialogTableVisible: false,
      dialogFormVisible: false,
      formLabelWidth: '120px'
    }
  },
  computed: {
    tables() {
      const input = this.input
      const input1 = this.input1
      if (input) {
        return this.tableData.filter((data) => {
          return Object.keys(data).some((key) => {
            return String(data[key]).toLowerCase().indexOf(input) > -1
          })
        })
      }
      if (input1) {
        return this.tableData.filter((data) => {
          return Object.keys(data).some((key) => {
            return String(data[key]).toLowerCase().indexOf(input1) > -1
          })
        })
      }
      return this.tableData
    }
  },
  created() {
    this.examId = localStorage.getItem('examId')
    this.gradeId = localStorage.getItem('gradeId')
    this.examTitle = localStorage.getItem('examTitle')
    this.gradeName = localStorage.getItem('gradeName')
    this.getScorePage()
  },
  beforeDestroy() {
    localStorage.removeItem('examId')
    localStorage.removeItem('gradeId')
  },
  methods: {
    updateRow(row) {
      row.type = 1
      console.log(row)
      localStorage.setItem('record_exam_examId', row.examId)
      this.$router.push({ name: 'exam-record-detail', query: { data: row }})
    },
    
    getScoreDiff(row) {
      if (row.aiScore === null || row.aiScore === undefined) return '-'
      const diff = row.userScore - row.aiScore
      if (diff === 0) return '0'
      return (diff > 0 ? '+' : '') + diff.toFixed(1)
    },
    
    getScoreDiffType(row) {
      if (row.aiScore === null || row.aiScore === undefined) return 'info'
      const diff = Math.abs(row.userScore - row.aiScore)
      const percent = (diff / row.userScore) * 100
      
      if (percent === 0) return 'success'
      if (percent > 15) return 'danger'
      if (percent > 5) return 'warning'
      return 'info'
    },
    
    async getScorePage() {
      const params = {
        pageNum: this.pageNum,
        pageSize: this.pageSize,
        examId: this.examId,
        gradeId: this.gradeId,
        realName: this.realName
      }
      const res = await scorePaging(params)
      this.data = res.data
    },
    
    getExportScores() {
      exportScores(this.examId, this.gradeId).then(res => {
        console.log(res)
        var debug = res
        if (debug) {
          var elink = document.createElement('a')
          var filename = 'downloaded-file.xlsx'
          if (this.gradeName) {
            filename = this.gradeName + '-' + this.examTitle + '.xlsx'
          }
          elink.download = filename
          elink.style.display = 'none'
          elink.href = URL.createObjectURL(res)
          document.body.appendChild(elink)
          elink.click()
          document.body.removeChild(elink)
        } else {
          this.$message.error('导出异常请联系管理员')
        }
      }).catch(err => {
        console.log(err)
      })
    },

    onSubmit() {
      this.getScorePage()
    },
    
    handleSizeChange(val) {
      this.pageSize = val
      this.getScorePage(this.pageNum, val)
    },
    
    handleCurrentChange(val) {
      this.pageNum = val
      this.getScorePage(val, this.pageSize)
    },

    handleClick(row) {
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
  width: 220px;
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

::v-deep .el-button--success {
  background: linear-gradient(135deg, #2ecc71 0%, #27ae60 100%);
  border: none;
  border-radius: 8px;
  padding: 10px 20px;
  font-weight: 500;
  transition: all 0.3s ease;
}

::v-deep .el-button--success:hover {
  background: linear-gradient(135deg, #54d98c 0%, #2ecc71 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(46, 204, 113, 0.3);
}

/* 表格卡片 */
.table-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  animation: fadeInUp 0.5s ease 0.2s backwards;
}

.score-detail-table {
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

.student-name {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #2c3e50;
  font-weight: 500;
}

.student-name i {
  color: #1abc9c;
  font-size: 16px;
}

.user-score {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #e74c3c;
  font-weight: 600;
  padding: 6px 12px;
  border-radius: 6px;
  background: rgba(231, 76, 60, 0.1);
}

.user-score i {
  font-size: 16px;
}

.ai-score {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #3498db;
  font-weight: 500;
}

.ai-score i {
  font-size: 16px;
}

.no-data {
  color: #909399;
}

.diff-tag {
  font-weight: 600;
  border-radius: 6px;
  padding: 6px 12px;
}

.count-tag {
  font-weight: 500;
  border-radius: 6px;
  padding: 6px 12px;
}

.time-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #1abc9c;
  font-weight: 500;
}

.time-badge i {
  font-size: 16px;
}

.submit-time {
  color: #7f8c8d;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 13px;
}

.submit-time i {
  color: #1abc9c;
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
}
</style>