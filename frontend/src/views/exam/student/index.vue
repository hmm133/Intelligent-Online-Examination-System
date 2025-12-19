<template>
  <div class="app-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <i class="el-icon-edit-outline"></i>
        <div>
          <h2 class="page-title">我的考试</h2>
          <p class="page-subtitle">My Exams</p>
        </div>
      </div>
    </div>

    <!-- 搜索和排序卡片 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="formInline" class="search-form">
        <el-form-item label="试卷名称">
          <el-input 
            v-model="searchTitle" 
            placeholder="输入试卷名称" 
            clearable
            prefix-icon="el-icon-search"
            class="search-input"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="searchExamStu">
            查询
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 排序开关 -->
      <div class="sort-section">
        <span class="sort-label">
          <i class="el-icon-time"></i>
          创建时间：
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
        class="exam-table"
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
        
        <el-table-column prop="examDuration" label="考试时长（分钟）" align="center" width="140">
          <template slot-scope="{ row }">
            <div class="duration-badge">
              <i class="el-icon-time"></i>
              {{ row.examDuration }}分钟
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="grossScore" label="总分" align="center" width="100">
          <template slot-scope="{ row }">
            <el-tag type="success" effect="plain" class="score-tag">
              {{ row.grossScore }}分
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="passedScore" label="及格分" align="center" width="100">
          <template slot-scope="{ row }">
            <el-tag type="warning" effect="plain" class="score-tag">
              {{ row.passedScore }}分
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="startTime" label="开始时间" align="center" width="180">
          <template slot-scope="scope">
            <div class="time-display">
              <i class="el-icon-time"></i>
              {{ scope.row.startTime }}
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="endTime" label="结束时间" align="center" width="180">
          <template slot-scope="scope">
            <div class="time-display">
              <i class="el-icon-time"></i>
              {{ scope.row.endTime }}
            </div>
          </template>
        </el-table-column>
        
        <el-table-column fixed="right" label="操作" align="center" width="140">
          <template slot-scope="{ row }">
            <el-button
              :type="getExamStatus(row).type"
              :disabled="getExamStatus(row).disabled"
              plain
              size="small"
              class="exam-action-btn"
              @click="screenInfo(row)"
            >
              <i :class="getExamStatus(row).icon"></i>
              {{ getExamStatus(row).text }}
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
import { getGradeExamList } from '@/api/exam'
export default {
  data() {
    return {
      pageNum: 1,
      pageSize: 10,
      data: {},
      searchTitle: '',
      isASC: false,
      formInline: {
        user: '',
        region: ''
      },
      dialogTableVisible: false,
      dialogFormVisible: false,
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
      formLabelWidth: '120px'
    }
  },
  created() {
    this.getExamGradePage()
  },
  methods: {
    async getExamGradePage(pageNum, pageSize, searchTitle = null) {
      const params = { pageNum: pageNum, pageSize: pageSize, title: searchTitle, isASC: this.isASC }
      const res = await getGradeExamList(params)
      this.data = res.data
    },
    toggleSort() {
      this.getExamGradePage(this.pageNum, this.pageSize, this.searchTitle)
    },
    getExamStatus(row) {
      const now = new Date().getTime()
      const endTime = new Date(row.endTime).getTime()
      const startTime = new Date(row.startTime).getTime()

      if (now > endTime) {
        return {
          text: '已结束',
          type: 'info',
          disabled: true,
          icon: 'el-icon-circle-close'
        }
      } else if (now < startTime) {
        return {
          text: '未开始',
          type: 'warning',
          disabled: true,
          icon: 'el-icon-warning'
        }
      } else {
        return {
          text: '开始考试',
          type: 'success',
          disabled: false,
          icon: 'el-icon-edit'
        }
      }
    },
    searchExamStu() {
      this.getExamGradePage(this.pageNum, this.pageSize, this.searchTitle)
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.getExamGradePage(this.pageNum, val, this.searchTitle)
    },
    handleCurrentChange(val) {
      this.pageNum = val
      this.getExamGradePage(val, this.pageSize, this.searchTitle)
    },
    handleClick(row) {
    },
    screenInfo(row) {
      const status = this.getExamStatus(row)
      if (status.disabled) {
        return
      }
      localStorage.setItem('examInfo_examId', row.id)
      this.$router.push({ name: 'prepare-exam', query: { zhi: row }})
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

.exam-table {
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

.duration-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: #1abc9c;
  font-weight: 500;
}

.duration-badge i {
  font-size: 16px;
}

.score-tag {
  font-weight: 500;
  border-radius: 6px;
  padding: 6px 12px;
}

.time-display {
  color: #7f8c8d;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 13px;
}

.time-display i {
  color: #1abc9c;
}

/* 考试操作按钮 */
.exam-action-btn {
  font-weight: 500;
  border-radius: 8px;
  padding: 8px 16px;
  transition: all 0.3s ease;
}

.exam-action-btn i {
  margin-right: 4px;
}

::v-deep .el-button--success.is-plain:not(.is-disabled) {
  color: #1abc9c;
  background: rgba(26, 188, 156, 0.1);
  border-color: #1abc9c;
}

::v-deep .el-button--success.is-plain:not(.is-disabled):hover {
  background: linear-gradient(135deg, #1abc9c 0%, #16a085 100%);
  color: white;
  border-color: #1abc9c;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(26, 188, 156, 0.3);
}

::v-deep .el-button--warning.is-plain.is-disabled {
  color: #f39c12;
  background: rgba(243, 156, 18, 0.05);
  border-color: #f39c12;
  opacity: 0.6;
}

::v-deep .el-button--info.is-plain.is-disabled {
  color: #95a5a6;
  background: rgba(149, 165, 166, 0.05);
  border-color: #95a5a6;
  opacity: 0.6;
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