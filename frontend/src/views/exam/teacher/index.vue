<template>
  <div class="app-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <i class="el-icon-document"></i>
        <div>
          <h2 class="page-title">考试管理</h2>
          <p class="page-subtitle">Exam Management</p>
        </div>
      </div>
    </div>

    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="formInline" class="search-form">
        <el-form-item label="考试名称">
          <el-input 
            v-model="input" 
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

      <!-- 操作按钮组 -->
      <div class="action-buttons">
        <el-button type="primary" icon="el-icon-plus" @click="screenInfo()">
          新增考试
        </el-button>
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
        
        <el-table-column label="序号" align="center" width="80px">
          <template slot-scope="scope">
            <span class="row-number">{{ scope.$index + 1 }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="title" label="试卷名称" align="center" />
        
        <el-table-column prop="examDuration" label="考试时间" align="center" width="100">
          <template slot-scope="{ row }">
            <div class="duration-badge">
              <i class="el-icon-time"></i>
              {{ row.examDuration }}分钟
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="maxCount" label="最多切屏次数" align="center" width="120" />
        
        <el-table-column prop="grossScore" label="总分" align="center" width="80">
          <template slot-scope="{ row }">
            <el-tag type="success" effect="plain" class="score-tag">
              {{ row.grossScore }}分
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="passedScore" label="及格分" align="center" width="90">
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
        
        <el-table-column fixed="right" label="操作" align="center" width="200">
          <template slot-scope="{ row }">
            <el-button
              type="text"
              size="small"
              class="action-btn view-btn"
              icon="el-icon-view"
              @click="showExam(row)"
            >
              查看详情
            </el-button>
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
              @click="delExam(row)"
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

    <!-- 显示详情对话框 -->
    <el-dialog 
      title="考试详情" 
      :visible.sync="showExamDialogVisible"
      width="700px"
      class="custom-dialog"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form :model="showExamData">
            <el-form-item label="考试标题" :label-width="formLabelWidth">
              <el-input v-model="showExamData.title" :disabled="true" />
            </el-form-item>
          </el-form>
        </el-col>
        <el-col :span="12">
          <el-form :model="form">
            <el-form-item label="考试时长" :label-width="formLabelWidth">
              <el-input v-model="showExamData.examDuration" :disabled="true" />
            </el-form-item>
          </el-form>
        </el-col>
      </el-row>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form :model="form">
            <el-form-item label="最大切屏次数" :label-width="formLabelWidth">
              <el-input v-model="showExamData.maxCount" :disabled="true" />
            </el-form-item>
          </el-form>
        </el-col>
        <el-col :span="12">
          <el-form :model="form">
            <el-form-item label="及格分" :label-width="formLabelWidth">
              <el-input v-model="showExamData.passedScore" :disabled="true" />
            </el-form-item>
          </el-form>
        </el-col>
      </el-row>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form :model="form">
            <el-form-item label="单选题数量" :label-width="formLabelWidth">
              <el-input v-model="showExamData.radioCount" :disabled="true" />
            </el-form-item>
          </el-form>
        </el-col>
        <el-col :span="12">
          <el-form :model="form">
            <el-form-item label="单选题分数" :label-width="formLabelWidth">
              <el-input v-model="showExamData.radioScore" :disabled="true" />
            </el-form-item>
          </el-form>
        </el-col>
      </el-row>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form :model="form">
            <el-form-item label="多选题数量" :label-width="formLabelWidth">
              <el-input v-model="showExamData.multiCount" :disabled="true" />
            </el-form-item>
          </el-form>
        </el-col>
        <el-col :span="12">
          <el-form :model="form">
            <el-form-item label="多选题分数" :label-width="formLabelWidth">
              <el-input v-model="showExamData.multiScore" :disabled="true" />
            </el-form-item>
          </el-form>
        </el-col>
      </el-row>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form :model="form">
            <el-form-item label="判断题数量" :label-width="formLabelWidth">
              <el-input v-model="showExamData.judgeCount" :disabled="true" />
            </el-form-item>
          </el-form>
        </el-col>
        <el-col :span="12">
          <el-form :model="form">
            <el-form-item label="判断题分数" :label-width="formLabelWidth">
              <el-input v-model="showExamData.judgeScore" :disabled="true" />
            </el-form-item>
          </el-form>
        </el-col>
      </el-row>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form :model="form">
            <el-form-item label="简答题数量" :label-width="formLabelWidth">
              <el-input v-model="showExamData.saqCount" :disabled="true" />
            </el-form-item>
          </el-form>
        </el-col>
        <el-col :span="12">
          <el-form :model="form">
            <el-form-item label="简答题分数" :label-width="formLabelWidth">
              <el-input v-model="showExamData.saqScore" :disabled="true" />
            </el-form-item>
          </el-form>
        </el-col>
      </el-row>

      <div slot="footer" class="dialog-footer">
        <el-button @click="showExamDialogVisible = false">关闭详情</el-button>
      </div>
    </el-dialog>

    <!-- 编辑对话框 -->
    <el-dialog 
      title="编辑考试" 
      :visible.sync="dialogFormVisible"
      width="700px"
      class="custom-dialog"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form :model="form">
            <el-form-item label="考试标题" :label-width="formLabelWidth">
              <el-input v-model="form.title" placeholder="请输入考试标题" />
            </el-form-item>
          </el-form>
        </el-col>
        <el-col :span="12">
          <el-form :model="form">
            <el-form-item label="考试时长" :label-width="formLabelWidth">
              <el-input v-model="form.examDuration" placeholder="请输入考试时长（分钟）" />
            </el-form-item>
          </el-form>
        </el-col>
      </el-row>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form :model="form">
            <el-form-item label="最大切屏次数" :label-width="formLabelWidth">
              <el-input v-model="form.maxCount" placeholder="请输入最大切屏次数" />
            </el-form-item>
          </el-form>
        </el-col>
        <el-col :span="12">
          <el-form :model="form">
            <el-form-item label="及格分" :label-width="formLabelWidth">
              <el-input v-model="form.passedScore" placeholder="请输入及格分" />
            </el-form-item>
          </el-form>
        </el-col>
      </el-row>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form :model="form">
            <el-form-item label="单选题分数" :label-width="formLabelWidth">
              <el-input v-model="form.radioScore" placeholder="请输入单选题分数" />
            </el-form-item>
          </el-form>
        </el-col>
        <el-col :span="12">
          <el-form :model="form">
            <el-form-item label="多选题分数" :label-width="formLabelWidth">
              <el-input v-model="form.multiScore" placeholder="请输入多选题分数" />
            </el-form-item>
          </el-form>
        </el-col>
      </el-row>
      
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form :model="form">
            <el-form-item label="判断题分数" :label-width="formLabelWidth">
              <el-input v-model="form.judgeScore" placeholder="请输入判断题分数" />
            </el-form-item>
          </el-form>
        </el-col>
        <el-col :span="12">
          <el-form :model="form">
            <el-form-item label="简答题分数" :label-width="formLabelWidth">
              <el-input v-model="form.saqScore" placeholder="请输入简答题分数" />
            </el-form-item>
          </el-form>
        </el-col>
      </el-row>

      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible = false">取消</el-button>
        <el-button type="primary" icon="el-icon-check" @click="updateExam()">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { examPaging, examUpdate, examDel } from '@/api/exam'
export default {
  data() {
    return {
      pageNum: 1,
      pageSize: 10,
      data: {},
      input: '',
      formInline: {
        user: '',
        region: '',
        date1: '',
        date2: ''
      },
      value1: [new Date(2000, 10, 10, 10, 10), new Date(2000, 10, 11, 10, 10)],
      dialogVisible: false,
      showExamDialogVisible: false,
      dialogTableVisible: false,
      dialogFormVisible: false,
      showExamData: {},
      form: {
        name: ''
      },
      cancle() {},
      updateRow(row) {
        this.dialogFormVisible = true
        this.form = row
      },
      diaTitle: '新增',
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
    this.getExamPage()
  },
  methods: {
    delExam(row) {
      this.$confirm('此操作将永久删除该考试, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        center: true
      })
        .then(() => {
          examDel(row.id).then((res) => {
            if (res.code) {
              this.getExamPage(this.pageNum, this.pageSize)
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
    showExam(row) {
      localStorage.setItem("exam-details-examId", row.id)
      this.$router.push({name: 'exam-details'})
    },
    updateExam() {
      const data = {
        examDuration: this.form.examDuration,
        judgeScore: this.form.judgeScore,
        maxCount: this.form.maxCount,
        multiScore: this.form.multiScore,
        passedScore: this.form.passedScore,
        radioScore: this.form.radioScore,
        saqScore: this.form.saqScore,
        title: this.form.title
      }
      examUpdate(this.form.id, data).then((res) => {
        if (res.code) {
          this.getExamPage(this.pageNum, this.pageSize)
          this.dialogFormVisible = false
          this.$message({
            type: 'success',
            message: '修改成功'
          })
        } else {
          this.$message({
            type: 'info',
            message: res.msg
          })
        }
      })
    },
    async getExamPage(pageNum, pageSize, title = null) {
      const params = { pageNum: pageNum, pageSize: pageSize, title: title }
      const res = await examPaging(params)
      this.data = res.data
    },
    searchExam() {
      this.getExamPage(this.pageNum, this.pageSize, this.input)
    },
    screenInfo(row) {
      this.$router.push({ name: 'exam-add', query: { zhi: row }})
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.getExamPage(this.pageNum, val, this.input)
    },
    handleCurrentChange(val) {
      this.pageNum = val
      this.getExamPage(val, this.pageSize, this.input)
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

/* 操作按钮 */
.action-btn {
  font-size: 14px;
  font-weight: 500;
  padding: 6px 10px;
  border-radius: 6px;
  transition: all 0.3s ease;
  margin: 0 2px;
}

.view-btn {
  color: #3498db;
}

.view-btn:hover {
  background: rgba(52, 152, 219, 0.1);
  transform: translateY(-2px);
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

::v-deep .custom-dialog .el-input__inner:focus {
  border-color: #1abc9c;
  box-shadow: 0 0 0 2px rgba(26, 188, 156, 0.1);
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

  .action-buttons {
    flex-direction: column;
  }

  ::v-deep .action-buttons .el-button {
    width: 100%;
  }
}
</style>