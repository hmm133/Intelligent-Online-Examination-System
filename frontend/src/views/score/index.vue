<template>
  <div class="app-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <i class="el-icon-medal"></i>
        <div>
          <h2 class="page-title">成绩管理</h2>
          <p class="page-subtitle">Score Management</p>
        </div>
      </div>
    </div>

    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="formInline" class="search-form">
        <el-form-item label="试卷名称">
          <el-input 
            v-model="examTitle" 
            placeholder="输入试卷名称" 
            clearable
            prefix-icon="el-icon-search"
            class="search-input"
          />
        </el-form-item>
        
        <el-form-item label="所属班级">
          <ClassSelect v-model="gradeId" class="search-select" />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="onSubmit">
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
        class="score-table"
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
        
        <el-table-column prop="gradeName" label="班级" align="center" width="150">
          <template slot-scope="{ row }">
            <el-tag type="info" effect="plain" size="small" class="grade-tag">
              <i class="el-icon-school"></i>
              {{ row.gradeName }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="maxScore" label="最高分" align="center" width="110">
          <template slot-scope="{ row }">
            <div class="score-badge max-score">
              <i class="el-icon-top"></i>
              {{ row.maxScore || '-' }}
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="minScore" label="最低分" align="center" width="110">
          <template slot-scope="{ row }">
            <div class="score-badge min-score">
              <i class="el-icon-bottom"></i>
              {{ row.minScore || '-' }}
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="avgScore" label="平均分" align="center" width="110">
          <template slot-scope="{ row }">
            <div class="score-badge avg-score">
              <i class="el-icon-data-line"></i>
              {{ row.avgScore || '-' }}
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="attendNum" label="参考人数" align="center" width="120">
          <template slot-scope="{ row }">
            <div class="count-badge">
              <i class="el-icon-user"></i>
              {{ row.attendNum }}人
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
              :disabled="row.maxScore == null"
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
import ClassSelect from '@/components/ClassSelect'
import { getExamScore } from '@/api/score'
export default {
  components: { ClassSelect },
  data() {
    return {
      pageNum: 1,
      pageSize: 10,
      gradeId: '',
      examTitle: '',
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
      cancle() {},
      updateRow(row) {
        localStorage.setItem('examId', row.examId)
        localStorage.setItem('gradeId', row.gradeId)
        localStorage.setItem('examTitle', row.examTitle)
        localStorage.setItem('gradeName', row.gradeName)
        this.$router.push({ name: 'user-score' })
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
    this.getScorePage()
  },
  methods: {
    async getScorePage() {
      const params = {
        pageNum: this.pageNum,
        pageSize: this.pageSize,
        gradeId: this.gradeId,
        examTitle: this.examTitle
      }
      const res = await getExamScore(params)
      this.data = res.data
    },
    onSubmit() {
      this.getScorePage()
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.getScorePage()
    },
    handleCurrentChange(val) {
      this.pageNum = val
      this.getScorePage()
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

.search-input,
.search-select {
  width: 220px;
}

::v-deep .search-input .el-input__inner,
::v-deep .search-select .el-input__inner {
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

.score-table {
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

.grade-tag {
  font-weight: 500;
  border-radius: 6px;
  padding: 6px 12px;
}

.grade-tag i {
  margin-right: 4px;
}

/* 分数徽章 */
.score-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  padding: 6px 12px;
  border-radius: 6px;
}

.score-badge i {
  font-size: 16px;
}

.max-score {
  color: #e74c3c;
  background: rgba(231, 76, 60, 0.1);
}

.min-score {
  color: #3498db;
  background: rgba(52, 152, 219, 0.1);
}

.avg-score {
  color: #1abc9c;
  background: rgba(26, 188, 156, 0.1);
}

.count-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #1abc9c;
  font-weight: 500;
}

.count-badge i {
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

.view-btn:hover:not(.is-disabled) {
  background: rgba(52, 152, 219, 0.1);
  transform: translateY(-2px);
}

.view-btn.is-disabled {
  color: #c0c4cc;
  cursor: not-allowed;
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

  .search-input,
  .search-select {
    width: 100%;
  }
}
</style>