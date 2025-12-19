<template>
  <div class="app-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <i class="el-icon-user"></i>
        <div>
          <h2 class="page-title">用户管理</h2>
          <p class="page-subtitle">User Management</p>
        </div>
      </div>
    </div>

    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form :inline="true" v-model="searchForm" class="search-form">
        <el-form-item label="真实姓名">
          <el-input 
            v-model="searchForm.searchRealName" 
            placeholder="输入姓名" 
            clearable
            prefix-icon="el-icon-search"
            class="search-input"
          />
        </el-form-item>
        
        <el-form-item label="班级">
          <ClassSelect 
            v-model="searchForm.searchClass" 
            :is-multiple="false" 
            class="search-select"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="searchUser">
            查询
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 操作按钮组 -->
      <div class="action-buttons">
        <el-button type="primary" icon="el-icon-plus" @click="addUserDiologVisible = true">
          新增用户
        </el-button>
        <el-button type="success" icon="el-icon-upload2" @click="fileDialogVisible = true">
          导入用户
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
        class="user-table"
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
        
        <el-table-column prop="userName" label="用户名" align="center" />
        
        <el-table-column prop="realName" label="真实姓名" align="center" />
        
        <el-table-column prop="roleId" label="角色名称" align="center" width="120">
          <template slot-scope="{ row }">
            <el-tag 
              :type="getRoleTag(row.roleId)" 
              size="small" 
              effect="plain"
              class="role-tag"
            >
              <span v-if="row.roleId == 1">学生</span>
              <span v-if="row.roleId == 2">教师</span>
              <span v-if="row.roleId == 3">管理员</span>
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="gradeName" label="班级" align="center" />
        
        <el-table-column prop="createTime" label="注册时间" align="center" width="180">
          <template slot-scope="scope">
            <div class="create-time">
              <i class="el-icon-time"></i>
              {{ scope.row.createTime }}
            </div>
          </template>
        </el-table-column>
        
        <el-table-column align="center" label="操作" width="150" fixed="right">
          <template slot-scope="{ row }">
            <el-button
              v-if="role == 'teacher'"
              type="text"
              size="small"
              class="action-btn remove-btn"
              icon="el-icon-close"
              @click="removeUserClass(row)"
            >
              移除班级
            </el-button>
            <el-button
              v-if="role == 'admin'"
              type="text"
              size="small"
              class="action-btn delete-btn"
              icon="el-icon-delete"
              @click="delUser(row)"
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

    <!-- 新增用户对话框 -->
    <el-dialog 
      title="新增用户" 
      :visible.sync="addUserDiologVisible"
      width="600px"
      class="custom-dialog"
    >
      <el-form :model="addForm" label-width="90px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名">
              <el-input v-model="addForm.userName" autocomplete="off" placeholder="请输入用户名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="真实姓名">
              <el-input v-model="addForm.realName" autocomplete="off" placeholder="请输入真实姓名" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="身份选择" v-if="role == 'admin'">
              <el-select v-model="addForm.roleId" placeholder="请选择身份" style="width: 100%">
                <el-option label="学生" value="1" />
                <el-option label="教师" value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item 
              label="班级选择" 
              v-if="role == 'teacher' || (role == 'admin' && addForm.roleId == '1')"
            >
              <ClassSelect v-model="addForm.gradeId" :is-multiple="false" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="addUserDiologVisible = false">取消</el-button>
        <el-button type="primary" icon="el-icon-check" @click="addUser">确定</el-button>
      </div>
    </el-dialog>

    <!-- 文件上传对话框 -->
    <el-dialog
      width="500px"
      :show-close="false"
      :close-on-click-modal="false"
      title="导入用户"
      :visible.sync="fileDialogVisible"
      class="upload-dialog"
    >
      <div class="upload-content">
        <div class="upload-tip">
          <i class="el-icon-info"></i>
          请选择Excel文件进行批量导入
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
          <div slot="tip" class="el-upload__tip">只能上传 xls/xlsx文件，且不超过500kb，大小为12kb</div>
        </el-upload>
      </div>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="fileDialogVisible = false">取消</el-button>
        <el-button type="success" plain icon="el-icon-download" @click="startDownload">
          下载模板
        </el-button>
        <el-button type="primary" icon="el-icon-check" @click="importUser">
          确定导入
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import ClassSelect from '@/components/ClassSelect'
import { userPaging, classAdd, userDel, userImport } from '@/api/user'
import { userClassRemove } from '@/api/class_'

export default {
  components: { ClassSelect },
  data() {
    return {
      role: '',
      pageNum: 1,
      pageSize: 10,
      data: {},
      fileList: [],
      addUserDiologVisible: false,
      fileDialogVisible: false,
      addForm: {
        userName: '',
        realName: '',
        roleId: '',
        gradeId: ''
      },
      searchForm: {
        searchRealName: '',
        searchClass: ''
      },
      formLabelWidth: '80px'
    }
  },
  created() {
    this.role = localStorage.getItem('roles')
    this.getUserPage()
  },
  methods: {
    getRoleTag(roleId) {
      const tags = {
        1: 'success',
        2: 'warning',
        3: 'danger'
      }
      return tags[roleId] || ''
    },
    
    async getUserPage(pageNum, pageSize, realName = null, gradeId = null) {
      const params = {
        pageNum: pageNum,
        pageSize: pageSize,
        realName: realName,
        gradeId: gradeId
      }
      const res = await userPaging(params)
      this.data = res.data
    },
    
    searchUser() {
      this.getUserPage(
        this.pageNum,
        this.pageSize,
        this.searchForm.searchRealName,
        this.searchForm.searchClass
      )
    },
    
    handleSizeChange(val) {
      this.pageSize = val
      this.getUserPage(this.pageNum, val, this.searchForm.searchRealName, this.searchForm.searchClass)
    },
    
    handleCurrentChange(val) {
      this.pageNum = val
      this.getUserPage(val, this.pageSize, this.searchForm.searchRealName, this.searchForm.searchClass)
    },
    
    addUser() {
      const data = {
        userName: this.addForm.userName,
        realName: this.addForm.realName,
        roleId: this.addForm.roleId,
        gradeId: this.addForm.gradeId
      }
      classAdd(data).then((res) => {
        if (res.code) {
          this.addForm.userName = ''
          this.addForm.realName = ''
          this.addForm.roleId = ''
          this.addForm.gradeId = ''
          this.getUserPage(this.pageNum, this.pageSize)
          this.addUserDiologVisible = false
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
    
    importUser() {
      if (this.fileList.length > 0) {
        const formData = new FormData()
        formData.append('file', this.fileList[0].raw)
        userImport(formData)
          .then((response) => {
            if (response.code) {
              this.getUserPage(this.pageNum, this.pageSize)
              this.$message.success(`${response.msg}`)
              this.fileDialogVisible = false
            } else {
              this.$message.error(`${response.msg}`)
            }
          })
          .catch((error) => {
            console.error('文件上传失败：', error)
            this.$message.error('文件上传失败！')
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
    
    delUser(row) {
      this.$confirm('此操作将永久删除该用户, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        center: true
      })
        .then(() => {
          userDel(row.id).then((res) => {
            if (res.code) {
              this.getUserPage(this.pageNum, this.pageSize)
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
    
    removeUserClass(row) {
      userClassRemove(row.id).then((res) => {
        if (res.code) {
          this.getUserPage(this.pageNum, this.pageSize)
          this.$message({
            type: 'success',
            message: '移除成功!'
          })
        } else {
          this.$message({
            type: 'info',
            message: res.msg
          })
        }
      })
    },
    
    async startDownload() {
      const a = document.createElement('a')
      a.href = './template/ImportUserTemplate.xlsx'
      a.download = '导入用户模板.xlsx'
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

.search-input {
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

/* 表格卡片 */
.table-card {
  border-radius: 16px;
  border: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  animation: fadeInUp 0.5s ease 0.2s backwards;
}

.user-table {
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

.role-tag {
  font-weight: 500;
  border-radius: 6px;
  padding: 6px 12px;
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

.remove-btn {
  color: #f39c12;
}

.remove-btn:hover {
  background: rgba(243, 156, 18, 0.1);
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

::v-deep .custom-dialog .el-input__inner,
::v-deep .custom-dialog .el-select {
  border-radius: 8px;
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