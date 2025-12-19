<template>
  <div class="app-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <i class="el-icon-reading"></i>
        <div>
          <h2 class="page-title">在线练习</h2>
          <p class="page-subtitle">Online Exercise</p>
        </div>
      </div>
    </div>

    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form :inline="true" class="search-form">
        <el-form-item label="题库名称">
          <el-input 
            v-model="repoTitle" 
            placeholder="输入题库名称" 
            clearable
            prefix-icon="el-icon-search"
            class="search-input"
          />
        </el-form-item>
        
        <el-form-item label="题库分类">
          <el-select 
            v-model="categoryId" 
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
          <el-button type="primary" icon="el-icon-search" @click="queryRepo()">
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
        class="exercise-table"
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
        
        <el-table-column prop="repoTitle" label="题库标题" align="center" min-width="250">
          <template slot-scope="{ row }">
            <div class="repo-title">
              <i class="el-icon-collection"></i>
              {{ row.repoTitle }}
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="categoryName" label="题库分类" align="center" width="200">
          <template slot-scope="{ row }">
            <div class="category-display">
              <i class="el-icon-folder-opened"></i>
              <span v-if="row.parentCategoryName" class="parent-category">
                {{ row.parentCategoryName }} / 
              </span>
              <span class="category-name">{{ row.categoryName || '未分类' }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="totalCount" label="试题总数" align="center" width="120">
          <template slot-scope="{ row }">
            <div class="question-count" :class="{ 'zero-count': row.totalCount === 0 }">
              <i class="el-icon-document"></i>
              {{ row.totalCount }}题
            </div>
          </template>
        </el-table-column>

        <el-table-column fixed="right" label="操作" align="center" width="140">
          <template slot-scope="{ row }">
            <el-button
              type="success"
              plain
              :disabled="row.totalCount == 0"
              size="small"
              class="start-btn"
              icon="el-icon-video-play"
              @click="screenInfo(row.id, row.repoTitle)"
            >
              {{ row.totalCount == 0 ? '暂无题目' : '开始刷题' }}
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
import { exercisePaging } from '@/api/exercise'
import { getCategoryTree } from '@/api/category'

export default {
  data() {
    return {
      pageNum: 1,
      pageSize: 10,
      data: {},
      repoTitle: '',
      categoryId: '',
      categoryOptions: [],
      dialogTableVisible: false,
      dialogFormVisible: false,
      formLabelWidth: '120px'
    }
  },
  created() {
    this.getExercisePage()
    this.fetchCategories()
  },
  methods: {
    queryRepo() {
      this.getExercisePage(this.pageNum, this.pageSize, this.repoTitle, this.categoryId)
    },
    async getExercisePage(pageNum, pageSize, title = null, categoryId = null) {
      const params = { 
        pageNum: pageNum, 
        pageSize: pageSize, 
        title: title,
        categoryId: categoryId
      }
      const res = await exercisePaging(params)
      this.data = res.data
    },
    async fetchCategories() {
      try {
        const res = await getCategoryTree()
        if (res.code) {
          this.categoryOptions = this.flattenCategoryTree(res.data)
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
    screenInfo(id, repoTitle) {
      this.$router.push({ name: 'start-exercise', query: { repoId: id, repoTitle: repoTitle }})
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.getExercisePage(this.pageNum, val, this.repoTitle, this.categoryId)
    },
    handleCurrentChange(val) {
      this.pageNum = val
      this.getExercisePage(val, this.pageSize, this.repoTitle, this.categoryId)
    },
    handleClick(row) {
      console.log(row)
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

::v-deep .search-input .el-input__inner:focus,
::v-deep .search-select .el-input__inner:focus {
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

.exercise-table {
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

.repo-title {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #2c3e50;
  font-weight: 500;
}

.repo-title i {
  color: #1abc9c;
  font-size: 18px;
}

.category-display {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: #5a6c7d;
}

.category-display i {
  color: #f39c12;
  font-size: 16px;
}

.parent-category {
  color: #95a5a6;
  font-size: 13px;
}

.category-name {
  color: #2c3e50;
  font-weight: 500;
}

.question-count {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  color: #1abc9c;
  font-weight: 600;
  padding: 6px 12px;
  border-radius: 6px;
  background: rgba(26, 188, 156, 0.1);
}

.question-count.zero-count {
  color: #e74c3c;
  background: rgba(231, 76, 60, 0.1);
}

.question-count i {
  font-size: 16px;
}

/* 开始按钮 */
.start-btn {
  font-weight: 500;
  border-radius: 8px;
  padding: 8px 16px;
  transition: all 0.3s ease;
}

::v-deep .el-button--success.is-plain:not(.is-disabled) {
  color: #27ae60;
  background: rgba(39, 174, 96, 0.1);
  border-color: #27ae60;
}

::v-deep .el-button--success.is-plain:not(.is-disabled):hover {
  background: linear-gradient(135deg, #2ecc71 0%, #27ae60 100%);
  color: white;
  border-color: #27ae60;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(46, 204, 113, 0.3);
}

::v-deep .el-button--success.is-plain.is-disabled {
  color: #c0c4cc;
  background: #f5f7fa;
  border-color: #e4e7ed;
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