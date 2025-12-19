<template>
  <div class="app-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <i class="el-icon-notebook-2"></i>
        <div>
          <h2 class="page-title">练习记录</h2>
          <p class="page-subtitle">Exercise Records</p>
        </div>
      </div>
    </div>

    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="formInline" class="search-form">
        <el-form-item label="题库名称">
          <el-input 
            v-model="searchTitle" 
            placeholder="输入题库名称" 
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
        class="exercise-record-table"
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
        
        <el-table-column prop="title" align="center" label="题库名称" min-width="300">
          <template slot-scope="{ row }">
            <div class="repo-name">
              <i class="el-icon-collection"></i>
              {{ row.title }}
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="createTime" align="center" label="刷题时间" width="200">
          <template slot-scope="{ row }">
            <div class="practice-time">
              <i class="el-icon-time"></i>
              {{ row.createTime }}
            </div>
          </template>
        </el-table-column>
        
        <el-table-column align="center" label="练习状态" width="120">
          <template>
            <el-tag type="success" effect="plain" class="status-tag">
              <i class="el-icon-circle-check"></i>
              已完成
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column align="center" label="操作" width="120">
          <template slot-scope="{ row }">
            <el-button
              type="text"
              size="small"
              class="action-btn view-btn"
              icon="el-icon-view"
              @click="updateRow(row)"
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

    <!-- 查看对话框 -->
    <el-dialog 
      title="查看详情" 
      :visible.sync="dialogFormVisible"
      width="600px"
      class="custom-dialog"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form :model="form">
            <el-form-item label="序号" :label-width="formLabelWidth">
              <el-input v-model="form.xh" :disabled="true" />
            </el-form-item>
          </el-form>
        </el-col>
        <el-col :span="12">
          <el-form :model="form">
            <el-form-item label="班级名称" :label-width="formLabelWidth">
              <el-input v-model="form.sjmc" :disabled="true" />
            </el-form-item>
          </el-form>
        </el-col>
      </el-row>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form :model="form">
            <el-form-item label="班级口令" :label-width="formLabelWidth">
              <el-input v-model="form.ctsl" :disabled="true" />
            </el-form-item>
          </el-form>
        </el-col>
        <el-col :span="12">
          <el-form :model="form">
            <el-form-item label="班级" :label-width="formLabelWidth">
              <el-input v-model="form.cjsj" :disabled="true" />
            </el-form-item>
          </el-form>
        </el-col>
      </el-row>

      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible = false">取消</el-button>
        <el-button type="primary" @click="dialogFormVisible = false">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { recordExercisePaging } from '@/api/record'
export default {
  data() {
    return {
      pageNum: 1,
      pageSize: 10,
      data: {},
      input: '',
      searchTitle: '',
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
        localStorage.setItem('record_exercise_repoId', row.id)
        this.$router.push({ name: 'exercise-record-detail', query: { zhi: row }})
      },
      diaTitle: '新增',
      dialogTableVisible: false,
      dialogFormVisible: false,
      formLabelWidth: '110px'
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
    this.getExerciseRecordPaging()
  },
  methods: {
    searchExam() {
      this.getExerciseRecordPaging(this.pageNum, this.pageSize, this.searchTitle)
    },
    async getExerciseRecordPaging(pageNum, pageSize, repoName) {
      const params = { pageNum: pageNum, pageSize: pageSize, repoName: repoName }
      const res = await recordExercisePaging(params)
      this.data = res.data
    },
    onSubmit() {
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.getExerciseRecordPaging(this.pageNum, this.pageSize, this.searchTitle)
    },
    handleCurrentChange(val) {
      this.pageNum = val
      this.getExerciseRecordPaging(this.pageNum, this.pageSize, this.searchTitle)
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

/* 表格卡片 */
.table-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  animation: fadeInUp 0.5s ease 0.2s backwards;
}

.exercise-record-table {
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

.repo-name {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #2c3e50;
  font-weight: 500;
}

.repo-name i {
  color: #1abc9c;
  font-size: 18px;
}

.practice-time {
  color: #7f8c8d;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 13px;
}

.practice-time i {
  color: #1abc9c;
  font-size: 16px;
}

.status-tag {
  font-weight: 500;
  border-radius: 6px;
  padding: 6px 12px;
}

.status-tag i {
  margin-right: 4px;
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

/* 对话框样式 */
::v-deep .custom-dialog .el-dialog {
  border-radius: 16px;
}

::v-deep .custom-dialog .el-dialog__header {
  background: linear-gradient(135deg, #f8fffe 0%, #e8f5f3 100%);
  padding: 20px 24px;
  border-radius: 16px 16px 0 0;
}

::v-deep .custom-dialog .el-dialog__title {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

::v-deep .custom-dialog .el-form-item__label {
  font-weight: 500;
  color: #5a6c7d;
}

::v-deep .custom-dialog .el-input__inner {
  border-radius: 8px;
}

::v-deep .custom-dialog .el-input.is-disabled .el-input__inner {
  background-color: #f8fffe;
  color: #5a6c7d;
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