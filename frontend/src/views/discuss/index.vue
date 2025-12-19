<template>
  <div class="app-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <i class="el-icon-chat-dot-round"></i>
        <div>
          <h2 class="page-title">讨论管理</h2>
          <p class="page-subtitle">Discussion Management</p>
        </div>
      </div>
    </div>

    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form :inline="true" :mode="searchForm" class="search-form">
        <el-form-item label="讨论名称">
          <el-input 
            v-model="searchForm.searchTitle" 
            placeholder="输入讨论名称" 
            clearable
            prefix-icon="el-icon-search"
            class="search-input"
          />
        </el-form-item>
        
        <el-form-item label="班级" v-if="currentRole === 'teacher'">
          <ClassSelect 
            v-model="searchForm.gradeId" 
            :is-multiple="false"
            class="search-select"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="searchDiscussion">
            查询
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 操作按钮 -->
      <div class="action-buttons" v-if="currentRole === 'teacher' || currentRole === 'admin'">
        <el-button type="primary" icon="el-icon-plus" @click="visible = true">
          新建话题
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
        class="discussion-table"
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
        
        <el-table-column prop="title" label="标题" align="center" min-width="250">
          <template slot-scope="{ row }">
            <div class="title-cell">
              <i class="el-icon-chat-line-round"></i>
              {{ row.title }}
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="sender" label="创建人" align="center" width="150">
          <template slot-scope="{ row }">
            <div class="creator-badge">
              <i class="el-icon-user"></i>
              {{ row.sender }}
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="createTime" label="创建时间" align="center" width="180">
          <template slot-scope="scope">
            <div class="time-display">
              <i class="el-icon-time"></i>
              {{ scope.row.createTime }}
            </div>
          </template>
        </el-table-column>
        
        <el-table-column fixed="right" label="操作" align="center" width="180">
          <template slot-scope="{ row }">
            <el-button
              type="text"
              size="small"
              class="action-btn view-btn"
              icon="el-icon-view"
              @click="showRow(row)"
            >
              查看
            </el-button>
            <el-button
              type="text"
              size="small"
              class="action-btn delete-btn"
              icon="el-icon-delete"
              @click="handleDel(row.id)"
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

    <!-- 发布讨论对话框 -->
    <el-dialog 
      title="发布讨论" 
      :visible.sync="visible" 
      width="600px"
      class="custom-dialog"
    >
      <el-form v-model="discussionForm" label-position="top" label-width="100px">
        <el-form-item label="讨论标题">
          <el-input
            v-model="discussionForm.title"
            placeholder="请输入标题"
            autocomplete="off"
          />
        </el-form-item>
        
        <el-form-item label="讨论内容">
          <el-input
            type="textarea"
            :rows="6"
            placeholder="请输入内容"
            clearable
            resize="none"
            v-model="discussionForm.content"
          />
        </el-form-item>
        
        <el-form-item label="班级">
          <ClassSelect v-model="discussionForm.gradeId" :is-multiple="false" />
        </el-form-item>
      </el-form>

      <span slot="footer" class="dialog-footer">
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" icon="el-icon-check" @click="handleConfirm">确定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { discussionpageOwner, discussionpageStudent, discussionAdd, discussionDel } from "@/api/discussion"
import ClassSelect from "@/components/ClassSelect"

export default {
  components: {
    ClassSelect,
  },
  data() {
    return {
      visible: false,
      currentRole: null,
      pageNum: 1,
      pageSize: 10,
      data: {},
      searchForm: {
        searchTitle: "",
        gradeId: null,
      },
      discussionForm: {
        title: null,
        content: null,
        gradeId: null
      },
    }
  },
  created() {
    this.currentRole = localStorage.getItem('roles')
    this.getDiscussionPage()
  },
  methods: {
    handleDel(id) {
      this.$confirm('此操作将永久删除该讨论, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.delDiscussion(id)
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消删除'
        })
      })
    },
    delDiscussion(id) {
      discussionDel(id).then(res => {
        if (res.code) {
          this.$message({
            type: 'success',
            message: res.msg
          })
          this.getDiscussionPage(this.pageNum, this.pageSize, this.title, this.gradeId)
        } else {
          this.$message({
            type: 'info',
            message: res.msg
          })
        }
      })
    },
    handleConfirm() {
      discussionAdd(this.discussionForm).then(res => {
        if (res.code) {
          this.discussionForm.title = null
          this.discussionForm.content = null
          this.discussionForm.gradeId = null
          this.visible = false
          this.getDiscussionPage()
          this.$message({
            type: 'success',
            message: res.msg
          })
        } else {
          this.$message({
            type: 'info',
            message: res.msg
          })
        }
      })
    },
    showRow(row) {
      this.$router.push({ name: 'discussion-detail', query: { discussionId: row.id }})
    },
    searchDiscussion() {
      this.getDiscussionPage(
        this.pageNum,
        this.pageSize,
        this.searchForm.searchTitle,
        this.searchForm.gradeId
      )
    },
    async getDiscussionPage(pageNum, pageSize, title = null, gradeId = null) {
      const params = {
        currentPage: pageNum,
        size: pageSize,
        title: title,
        gradeId: gradeId,
      }
      if (this.currentRole === 'teacher') {
        const res = await discussionpageOwner(params)
        this.data = res.data
      } else if (this.currentRole === 'student') {
        delete params.gradeId
        const res = await discussionpageStudent(params)
        this.data = res.data
      }
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.getDiscussionPage(this.pageNum, val, this.searchForm.searchTitle, this.searchForm.gradeId)
    },
    handleCurrentChange(val) {
      this.pageNum = val
      this.getDiscussionPage(val, this.pageSize, this.searchForm.searchTitle, this.searchForm.gradeId)
    },
  },
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

/* 操作按钮组 */
.action-buttons {
  padding-top: 16px;
  border-top: 1px solid #e8f5f3;
}

::v-deep .action-buttons .el-button,
::v-deep .el-button--primary {
  background: linear-gradient(135deg, #1abc9c 0%, #16a085 100%);
  border: none;
  border-radius: 8px;
  padding: 10px 20px;
  font-weight: 500;
  transition: all 0.3s ease;
}

::v-deep .action-buttons .el-button:hover,
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

.discussion-table {
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

.title-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #2c3e50;
  font-weight: 500;
}

.title-cell i {
  color: #1abc9c;
  font-size: 18px;
}

.creator-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #1abc9c;
  font-weight: 500;
}

.creator-badge i {
  font-size: 16px;
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
  color: #1abc9c;
}

.view-btn:hover {
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

::v-deep .custom-dialog .el-input__inner,
::v-deep .custom-dialog .el-textarea__inner {
  border-radius: 8px;
}

::v-deep .custom-dialog .el-input__inner:focus,
::v-deep .custom-dialog .el-textarea__inner:focus {
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

  .search-input,
  .search-select {
    width: 100%;
  }
}
</style>