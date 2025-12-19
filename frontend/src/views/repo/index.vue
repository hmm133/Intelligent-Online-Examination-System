<template>
  <div class="app-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <i class="el-icon-collection"></i>
        <div>
          <h2 class="page-title">题库管理</h2>
          <p class="page-subtitle">Question Bank Management</p>
        </div>
      </div>
    </div>

    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="formInline" class="search-form">
        <el-form-item label="题库名称">
          <el-input 
            v-model="searchTitle" 
            placeholder="请输入查询内容" 
            clearable
            prefix-icon="el-icon-search"
            class="search-input"
          />
        </el-form-item>
        
        <el-form-item label="题库分类">
          <el-select 
            v-model="searchCategory" 
            placeholder="请选择分类" 
            clearable
            class="search-select"
          >
            <el-option
              v-for="item in categoryOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="searchRepo">
            查询
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 操作按钮组 -->
      <div class="action-buttons">
        <el-button type="primary" icon="el-icon-plus" @click="addRepoDialogVisible = true">
          新增题库
        </el-button>
        <el-button type="success" icon="el-icon-s-operation" @click="categoryDialogVisible = true">
          分类管理
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
        class="repo-table"
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
        
        <el-table-column prop="title" label="题库名称" align="center" min-width="200" />
        
        <el-table-column prop="categoryName" label="题库分类" align="center" width="150">
          <template slot-scope="{ row }">
            <el-tag type="info" effect="plain" size="small" class="category-tag">
              <i class="el-icon-folder"></i>
              {{ row.categoryName }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="题目数量" align="center" width="120">
          <template slot-scope="{ row }">
            <div class="question-count" :class="{ 'zero-count': row.questionCount === 0 }">
              <i class="el-icon-document"></i>
              <span>{{ row.questionCount }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="realName" label="创建人" align="center" width="120" />
        
        <el-table-column prop="createTime" label="创建时间" align="center" width="180">
          <template slot-scope="scope">
            <div class="time-display">
              <i class="el-icon-time"></i>
              {{ scope.row.createTime }}
            </div>
          </template>
        </el-table-column>

        <el-table-column label="开启刷题" align="center" width="110">
          <template slot-scope="{ row }">
            <el-tag 
              :type="row.isExercise === 1 ? 'success' : 'danger'" 
              :effect="row.isExercise === 0 ? 'dark' : 'light'"
              class="status-tag"
            >
              {{ row.isExercise === 1 ? '已开启' : '未开启' }}
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column fixed="right" label="操作" align="center" width="240">
          <template slot-scope="{ row }">
            <el-button 
              type="text" 
              size="small" 
              class="action-btn ai-btn"
              icon="el-icon-magic-stick"
              @click="openAIDialog(row.id)"
            >
              AI补题
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
              @click="delRepo(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- AI生成对话框 -->
      <ai-question-generator
        :visible.sync="aiDialogVisible"
        :default-repo-id="currentRepoId"
        @success="handleAIGenerateSuccess"
        @view-questions="handleViewQuestions"
      />

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

    <!-- 新增题库 -->
    <el-dialog 
      title="新增题库" 
      :visible.sync="addRepoDialogVisible" 
      width="600px"
      class="custom-dialog"
    >
      <el-form :model="addRepoForm" label-width="110px">
        <el-form-item label="题库名称">
          <el-input v-model="addRepoForm.title" placeholder="请输入题库名称"></el-input>
        </el-form-item>
        <el-form-item label="题库分类">
          <el-select v-model="addRepoForm.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option
              v-for="item in categoryOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="是否开启刷题">
          <el-switch v-model="addRepoForm.isExercise"></el-switch>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="addRepoDialogVisible = false">取消</el-button>
        <el-button type="primary" icon="el-icon-check" @click="submitAddRepo">确定</el-button>
      </div>
    </el-dialog>

    <!-- 编辑题库 -->
    <el-dialog 
      title="编辑题库" 
      :visible.sync="dialogFormVisible" 
      width="600px"
      class="custom-dialog"
    >
      <el-form :model="form" label-width="110px">
        <el-form-item label="题库名称">
          <el-input v-model="form.title" placeholder="请输入题库名称"></el-input>
        </el-form-item>
        <el-form-item label="题库分类">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option
              v-for="item in categoryOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="是否开启刷题">
          <el-switch v-model="form.isExercise" :active-value="1" :inactive-value="0"></el-switch>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible = false">取消</el-button>
        <el-button type="primary" icon="el-icon-check" @click="submitEditRepo">确定</el-button>
      </div>
    </el-dialog>

    <!-- 分类管理对话框 -->
    <el-dialog 
      title="题库分类管理" 
      :visible.sync="categoryDialogVisible" 
      width="700px"
      class="custom-dialog category-dialog"
    >
      <div class="category-header">
        <el-button type="primary" size="small" icon="el-icon-plus" @click="addCategory">
          添加分类
        </el-button>
      </div>
      <el-table 
        :data="categoryList" 
        border 
        style="width: 100%"
        :header-cell-style="{
          background: '#f8fffe',
          color: '#2c3e50',
          fontWeight: '500'
        }"
      >
        <el-table-column prop="name" label="分类名称" />
        <el-table-column prop="parentName" label="父级分类">
          <template slot-scope="{ row }">
            <span v-if="row.parentName" class="parent-name">{{ row.parentName }}</span>
            <span v-else class="no-parent">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template slot-scope="scope">
            <el-button 
              type="text" 
              size="small" 
              class="category-action-btn edit"
              @click="editCategory(scope.row)"
            >
              编辑
            </el-button>
            <el-button 
              type="text" 
              size="small" 
              class="category-action-btn delete"
              @click="deleteCategory(scope.row.id)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 添加分类对话框 -->
    <el-dialog 
      title="添加分类" 
      :visible.sync="addCategoryDialogVisible" 
      width="500px" 
      append-to-body
      class="custom-dialog"
    >
      <el-form :model="categoryForm" label-width="100px">
        <el-form-item label="分类名称">
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="父级分类">
          <el-select v-model="categoryForm.parentId" placeholder="请选择父级分类" clearable style="width: 100%">
            <el-option
              v-for="item in categoryOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="addCategoryDialogVisible = false">取消</el-button>
        <el-button type="primary" icon="el-icon-check" @click="submitAddCategory">确定</el-button>
      </div>
    </el-dialog>

    <!-- 编辑分类对话框 -->
    <el-dialog 
      title="编辑分类" 
      :visible.sync="editCategoryDialogVisible" 
      width="500px" 
      append-to-body
      class="custom-dialog"
    >
      <el-form :model="categoryForm" label-width="100px">
        <el-form-item label="分类名称">
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="父级分类">
          <el-select v-model="categoryForm.parentId" placeholder="请选择父级分类" clearable style="width: 100%">
            <el-option
              v-for="item in categoryOptions.filter(opt => opt.id !== categoryForm.id)"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="editCategoryDialogVisible = false">取消</el-button>
        <el-button type="primary" icon="el-icon-check" @click="submitEditCategory">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import AIQuestionGenerator from '@/components/AIQuestionGenerator'
import { repoPaging, repoDel, repoUpdate, repoAdd } from '@/api/repo'
import { getCategoryTree, addCategory, updateCategory, deleteCategory } from '@/api/category'

export default {
  components: {
    'ai-question-generator': AIQuestionGenerator
  },
  data() {
    return {
      aiDialogVisible: false,
      currentRepoId: null,
      pageNum: 1,
      pageSize: 10,
      isExercise: true,
      data: {
        records: [],
        total: 0,
        size: 10,
        current: 1
      },
      addTitle: '',
      delVisible: false,
      searchTitle: '',
      searchCategory: '',
      Obj: {},
      formInline: {
        searchTitle: ''
      },
      cancle() { },
      diaTitle: '新增',
      form: {
        title: '',
        isExercise: 0,
        categoryId: ''
      },
      addRepoForm: {
        title: '',
        isExercise: false,
        categoryId: ''
      },
      formLabelWidth: '120px',
      dialogVisible: false,
      addRepoDialogVisible: false,
      dialogTableVisible: false,
      dialogFormVisible: false,
      categoryDialogVisible: false,
      addCategoryDialogVisible: false,
      editCategoryDialogVisible: false,
      categoryForm: {
        id: '',
        name: '',
        parentId: null
      },
      categoryOptions: [],
      categoryList: []
    }
  },
  created() {
    this.getRepoPage()
    this.fetchCategories()
  },
  methods: {
    openAIDialog(repoId) {
      this.currentRepoId = repoId
      this.aiDialogVisible = true
    },
    handleAIGenerateSuccess(result) {
      this.$message.success(`AI成功生成 ${result.successCount} 道题目！`)
      this.getRepoPage(this.pageNum, this.pageSize, this.searchTitle, this.searchCategory)
    },
    handleViewQuestions(questionIds) {
      this.$router.push({
        name: 'questions',
        params: {
          repoId: this.currentRepoId,
          questionIds: questionIds
        }
      })
    },
    async getRepoPage(pageNum = this.pageNum, pageSize = this.pageSize, title = null, categoryId = null) {
      try {
        const params = {
          pageNum: pageNum,
          pageSize: pageSize,
          title: title,
          categoryId: categoryId
        }
        const res = await repoPaging(params)
        if (res.code) {
          this.data = res.data
        } else {
          this.$message.error(res.msg || '获取题库数据失败')
        }
      } catch (error) {
        console.error('获取题库数据失败:', error)
        this.$message.error('获取题库数据失败')
      }
    },
    async fetchCategories() {
      try {
        const res = await getCategoryTree()
        if (res.code) {
          this.categoryOptions = this.flattenCategoryTree(res.data)
          this.categoryList = this.processCategoryList(res.data)
        } else {
          this.$message.error(res.msg || '获取分类数据失败')
        }
      } catch (error) {
        console.error('获取分类失败:', error)
        this.$message.error('获取分类数据失败')
      }
    },
    flattenCategoryTree(tree, result = []) {
      if (!tree || !tree.length) return result
      tree.forEach(node => {
        result.push({
          id: node.id,
          name: node.name
        })
        if (node.children && node.children.length > 0) {
          this.flattenCategoryTree(node.children, result)
        }
      })
      return result
    },
    processCategoryList(tree, parentName = null, result = []) {
      if (!tree || !tree.length) return result
      tree.forEach(node => {
        result.push({
          id: node.id,
          name: node.name,
          parentId: node.parentId,
          parentName: parentName
        })
        if (node.children && node.children.length > 0) {
          this.processCategoryList(node.children, node.name, result)
        }
      })
      return result
    },
    searchRepo() {
      this.getRepoPage(this.pageNum, this.pageSize, this.searchTitle, this.searchCategory)
    },
    updateRow(row) {
      this.dialogFormVisible = true
      this.form = { ...row }
    },
    submitAddRepo() {
      if (!this.addRepoForm.title) {
        this.$message.warning('请输入题库名称')
        return
      }
      const data = {
        'title': this.addRepoForm.title,
        'isExercise': this.addRepoForm.isExercise ? 1 : 0,
        'categoryId': this.addRepoForm.categoryId
      }
      repoAdd(data)
        .then((res) => {
          if (res.code) {
            this.addRepoDialogVisible = false
            this.getRepoPage()
            this.$message({
              type: 'success',
              message: '添加成功'
            })
            this.addRepoForm = {
              title: '',
              isExercise: false,
              categoryId: ''
            }
          } else {
            this.$message({
              type: 'info',
              message: res.msg || '添加失败'
            })
          }
        })
        .catch((error) => {
          console.error('添加题库失败:', error)
          this.$message.error('添加题库失败')
        })
    },
    submitEditRepo() {
      if (!this.form.title) {
        this.$message.warning('请输入题库名称')
        return
      }
      const data = {
        'title': this.form.title,
        'isExercise': this.form.isExercise,
        'categoryId': this.form.categoryId
      }
      repoUpdate(this.form.id, data)
        .then((res) => {
          if (res.code) {
            this.getRepoPage()
            this.dialogFormVisible = false
            this.$message({
              type: 'success',
              message: '编辑成功!'
            })
          } else {
            this.$message({
              type: 'info',
              message: res.msg || '编辑失败'
            })
          }
        })
        .catch((error) => {
          console.error('编辑题库失败:', error)
          this.$message.error('编辑题库失败')
        })
    },
    delRepo(row) {
      this.$confirm('此操作将永久删除该题库, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
        center: true
      })
        .then(() => {
          repoDel(row.id).then((res) => {
            if (res.code) {
              this.getRepoPage()
              this.$message({
                type: 'success',
                message: '删除成功!'
              })
            } else {
              this.$message({
                type: 'info',
                message: res.msg || '删除失败'
              })
            }
          }).catch((error) => {
            console.error('删除题库失败:', error)
            this.$message.error('删除题库失败')
          })
        })
        .catch(() => {
          this.$message({
            type: 'info',
            message: '已取消删除'
          })
        })
    },
    addCategory() {
      this.categoryForm = {
        name: '',
        parentId: null
      }
      this.addCategoryDialogVisible = true
    },
    editCategory(row) {
      this.categoryForm = {
        id: row.id,
        name: row.name,
        parentId: row.parentId
      }
      this.editCategoryDialogVisible = true
    },
    submitAddCategory() {
      if (!this.categoryForm.name) {
        this.$message.warning('请输入分类名称')
        return
      }
      const data = {
        name: this.categoryForm.name,
        parentId: this.categoryForm.parentId || 0
      }
      addCategory(data)
        .then((res) => {
          if (res.code) {
            this.$message.success('添加成功')
            this.addCategoryDialogVisible = false
            this.fetchCategories()
          } else {
            this.$message.error(res.msg || '添加分类失败')
          }
        })
        .catch((error) => {
          console.error('添加分类失败:', error)
          this.$message.error('添加分类失败')
        })
    },
    submitEditCategory() {
      if (!this.categoryForm.name) {
        this.$message.warning('请输入分类名称')
        return
      }
      const data = {
        name: this.categoryForm.name,
        parentId: this.categoryForm.parentId || 0
      }
      updateCategory(this.categoryForm.id, data)
        .then((res) => {
          if (res.code) {
            this.$message.success('修改成功')
            this.editCategoryDialogVisible = false
            this.fetchCategories()
          } else {
            this.$message.error(res.msg || '修改分类失败')
          }
        })
        .catch((error) => {
          console.error('修改分类失败:', error)
          this.$message.error('修改分类失败')
        })
    },
    deleteCategory(id) {
      this.$confirm('确认删除该分类?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteCategory(id)
          .then((res) => {
            if (res.code) {
              this.$message.success('删除成功')
              this.fetchCategories()
            } else {
              this.$message.error(res.msg || '删除分类失败')
            }
          })
          .catch((error) => {
            console.error('删除分类失败:', error)
            this.$message.error('删除分类失败')
          })
      }).catch(() => {
        this.$message({
          type: 'info',
          message: '已取消删除'
        })
      })
    },
    handleClose(done) {
      done()
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.getRepoPage(this.pageNum, val, this.searchTitle, this.searchCategory)
    },
    handleCurrentChange(val) {
      this.pageNum = val
      this.getRepoPage(val, this.pageSize, this.searchTitle, this.searchCategory)
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
  width: 200px;
}

::v-deep .search-input .el-input__inner,
::v-deep .search-select .el-input__inner {
  border-radius: 8px;
  border-color: #e0e6ed;
  transition: all 0.3s ease;
}

::v-deep .search-input .el-input__inner:focus,
::v-deep .search-select .el-input__inner:focus {
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

.repo-table {
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

.category-tag {
  font-weight: 500;
  border-radius: 6px;
  padding: 6px 12px;
}

.category-tag i {
  margin-right: 4px;
}

.question-count {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #1abc9c;
  font-weight: 500;
}

.question-count.zero-count {
  color: #F56C6C;
}

.question-count i {
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

.status-tag {
  font-weight: 500;
  border-radius: 6px;
  padding: 6px 12px;
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

.ai-btn {
  color: #67C23A;
}

.ai-btn:hover {
  background: rgba(103, 194, 58, 0.1);
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

::v-deep .custom-dialog .el-input__inner,
::v-deep .custom-dialog .el-select {
  border-radius: 8px;
}

::v-deep .custom-dialog .el-input__inner:focus {
  border-color: #1abc9c;
  box-shadow: 0 0 0 2px rgba(26, 188, 156, 0.1);
}

/* 分类管理对话框 */
.category-header {
  margin-bottom: 15px;
}

.parent-name {
  color: #1abc9c;
  font-weight: 500;
}

.no-parent {
  color: #95a5a6;
}

.category-action-btn {
  font-size: 13px;
  padding: 4px 8px;
  margin: 0 2px;
}

.category-action-btn.edit {
  color: #1abc9c;
}

.category-action-btn.delete {
  color: #e74c3c;
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

  .action-buttons {
    flex-direction: column;
  }

  ::v-deep .action-buttons .el-button {
    width: 100%;
  }
}
</style>