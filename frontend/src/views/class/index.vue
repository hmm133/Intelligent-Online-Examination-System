<template>
  <div class="app-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <i class="el-icon-school"></i>
        <div>
          <h2 class="page-title">班级管理</h2>
          <p class="page-subtitle">Class Management</p>
        </div>
      </div>
    </div>

    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="formInline" class="search-form">
        <el-form-item label="班级">
          <el-input 
            v-model="formInline.searchTitle" 
            placeholder="输入班级名称" 
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
        <el-button 
          v-if="role==3" 
          type="primary" 
          icon="el-icon-plus" 
          @click="dialogTableVisible = true"
        >
          新增班级
        </el-button>
        <el-button 
          v-if="role==2" 
          type="success" 
          icon="el-icon-link" 
          @click="joinClassVisible = true"
        >
          加入班级
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
        class="class-table"
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
        
        <el-table-column prop="gradeName" label="班级名称" align="center" />
        
        <el-table-column prop="gradeCount" label="班级人数" align="center" width="120">
          <template slot-scope="{ row }">
            <div class="count-badge">
              <i class="el-icon-user"></i>
              {{ row.gradeCount }}人
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="code" label="班级口令" align="center" width="150">
          <template slot-scope="{ row }">
            <el-tag type="success" effect="plain" class="code-tag">
              <i class="el-icon-lock"></i>
              {{ row.code }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="userName" label="创建用户" align="center" />
        
        <el-table-column align="center" label="操作" width="180">
          <template slot-scope="{ row }">
            <!-- 管理员按钮 -->
            <el-button
              v-if="role==3"
              type="text"
              size="small"
              class="action-btn edit-btn"
              icon="el-icon-edit"
              @click="updateRow(row)"
            >
              编辑
            </el-button>

            <el-button
              v-if="role==3"
              type="text"
              size="small"
              class="action-btn delete-btn"
              icon="el-icon-delete"
              @click="delClass(row)"
            >
              删除
            </el-button>
            
            <!-- 教师按钮 -->
            <el-button
              v-if="role==2"
              type="text"
              size="small"
              class="action-btn exit-btn"
              icon="el-icon-close"
              @click="exitClass(row)"
            >
              退出班级
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

    <!-- 新增弹窗 -->
    <el-dialog 
      title="新增班级" 
      :visible.sync="dialogTableVisible"
      width="500px"
      class="custom-dialog"
    >
      <el-form :model="addForm">
        <el-form-item label="班级名称" :label-width="formLabelWidth">
          <el-input 
            v-model="addForm.gradeName" 
            autocomplete="off" 
            placeholder="请输入班级名称"
          />
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogTableVisible = false">取消</el-button>
        <el-button type="primary" icon="el-icon-check" @click="addClass">确定</el-button>
      </div>
    </el-dialog>

    <!-- 加入班级弹窗 -->
    <el-dialog 
      title="加入班级" 
      :visible.sync="joinClassVisible"
      width="500px"
      class="custom-dialog"
    >
      <el-form :model="teacharForm">
        <el-form-item label="班级代码" :label-width="formLabelWidth">
          <el-input 
            v-model="teacharForm.classCode" 
            autocomplete="off"
            placeholder="请输入班级口令代码"
          >
            <i slot="prefix" class="el-input__icon el-icon-lock"></i>
          </el-input>
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="joinClassVisible = false">取消</el-button>
        <el-button type="primary" icon="el-icon-check" @click="joinClass">确定</el-button>
      </div>
    </el-dialog>

    <!-- 编辑弹窗 -->
    <el-dialog 
      title="编辑班级" 
      :visible.sync="dialogFormVisible"
      width="500px"
      class="custom-dialog"
    >
      <el-form :model="form">
        <el-form-item label="班级名称" :label-width="formLabelWidth">
          <el-input 
            v-model="form.gradeName" 
            autocomplete="off"
            placeholder="请输入班级名称"
          />
        </el-form-item>
      </el-form>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible = false">取消</el-button>
        <el-button type="primary" icon="el-icon-check" @click="updateClass">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { teacherJoinClass, teacherExitClass, classPaging, classDel, classUpdate, classAdd } from '@/api/class_'
import { getRole } from '@/utils/jwtUtils'
export default {
  data() {
    return {
      teacharForm: {
        classCode: ''
      },
      role: 0,
      pageNum: 1,
      pageSize: 10,
      data: {},
      diaTitle: '新增',
      joinClassVisible: false,
      dialogTableVisible: false,
      dialogFormVisible: false,
      addForm: {
        gradeName: ''
      },
      formInline: {
        searchTitle: ''
      },
      form: {
        gradeName: ''
      },
      formLabelWidth: '110px'
    }
  },

  created() {
    this.getClassPage()
    this.role = getRole()
  },
  methods: {
    joinClass() {
      const params = { code: this.teacharForm.classCode }
      teacherJoinClass(params).then((res) => {
        if (res.code) {
          this.joinClassVisible = false
          this.getClassPage(this.pageNum, this.pageSize, this.formInline.searchTitle)
          this.$message({
            type: 'success',
            message: '加入成功!'
          })
        } else {
          this.$message({
            type: 'info',
            message: res.msg
          })
        }
      })
    },
    exitClass(row) {
      const classId = row['id']
      teacherExitClass(classId).then((res) => {
        if (res.code) {
          this.getClassPage(this.pageNum, this.pageSize, this.formInline.searchTitle)
          this.$message({
            type: 'success',
            message: '退出成功!'
          })
        } else {
          this.$message({
            type: 'info',
            message: res.msg
          })
        }
      })
    },
    async getClassPage(pageNum, pageSize, title = null) {
      const params = { pageNum: pageNum, pageSize: pageSize, gradeName: title }
      const res = await classPaging(params)
      this.data = res.data
    },
    addClass() {
      const data = { gradeName: this.addForm.gradeName }
      classAdd(data).then((res) => {
        if (res.code) {
          this.addForm.gradeName = ''
          this.getClassPage(this.pageNum, this.pageSize, this.formInline.searchTitle)
          this.dialogTableVisible = false
          this.$message({
            type: 'success',
            message: '新增成功!'
          })
        } else {
          this.$message({
            type: 'info',
            message: res.msg
          })
        }
      })
    },
    delClass(row) {
      this.$confirm('此操作将永久删除该班级, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        center: true
      })
        .then(() => {
          classDel(row.id).then((res) => {
            if (res.code) {
              this.getClassPage(this.pageNum, this.pageSize, this.formInline.searchTitle)
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
    updateClass() {
      classUpdate(this.form.id, { gradeName: this.form.gradeName })
        .then((res) => {
          if (res.code) {
            this.getClassPage(this.pageNum, this.pageSize, this.formInline.searchTitle)
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
    updateRow(row) {
      this.dialogFormVisible = true
      this.form = row
    },
    searchExam() {
      this.getClassPage(this.pageNum, this.pageSize, this.formInline.searchTitle)
    },
    handleClick(row) {
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.getClassPage(this.pageNum, val, this.formInline.searchTitle)
    },
    handleCurrentChange(val) {
      this.pageNum = val
      this.getClassPage(val, this.pageSize, this.formInline.searchTitle)
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

::v-deep .el-button--success {
  background: linear-gradient(135deg, #2ecc71 0%, #27ae60 100%);
  border: none;
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

.class-table {
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

.code-tag {
  font-weight: 500;
  border-radius: 6px;
  padding: 6px 12px;
  font-family: 'Courier New', monospace;
}

.code-tag i {
  margin-right: 4px;
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

.exit-btn {
  color: #f39c12;
}

.exit-btn:hover {
  background: rgba(243, 156, 18, 0.1);
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