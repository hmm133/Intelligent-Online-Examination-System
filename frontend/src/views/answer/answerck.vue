<template>
  <div class="app-container">

    <el-form :inline="true" :model="formInline" class="demo-form-inline">
      <el-form-item label="用户姓名">
        <el-input v-model="realName" placeholder="输入姓名" />
      </el-form-item>
      <el-form-item>
        <el-button
          type="primary"
          @click="searchFun"
        >查询</el-button>
        <!-- 新增：批量AI评分按钮 -->
        <el-button
          type="warning"
          icon="el-icon-magic-stick"
          :loading="batchAILoading"
          @click="handleBatchAIScore"
        >批量AI评分</el-button>
      </el-form-item>
    </el-form>
    
    <!-- table -->
    <el-table
      :data="data.records"
      border
      fit
      highlight-current-row
      :header-cell-style="{
        background: '#f2f3f4',
        color: '#555',
        'font-weight': 'bold',
        'line-height': '32px',
      }"
    >
      <el-table-column align="center" type="selection" width="55" />
      <el-table-column fixed label="序号" align="center" width="80">
        <template slot-scope="scope">{{ scope.$index + 1 }}</template>
      </el-table-column>
      <el-table-column prop="userName" label="用户名字" align="center" />
      <el-table-column prop="limitTime" label="提交时间" align="center" />
      
      <!-- 新增：AI评分状态列 -->
      <el-table-column label="AI评分状态" align="center" width="120">
        <template slot-scope="scope">
          <el-tag v-if="scope.row.aiScored" type="success" size="small">
            <i class="el-icon-circle-check" /> 已评分
          </el-tag>
          <el-tag v-else type="info" size="small">
            <i class="el-icon-remove" /> 未评分
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column fixed="right" label="操作" align="center">
        <template slot-scope="scope">
          <el-button
            type="text"
            size="small"
            style="font-size: 14px"
            @click="screenInfo(scope.row)"
          >批改试卷</el-button>
        </template>
      </el-table-column>
    </el-table>

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

  </div>
</template>

<script>
import { answerUserPging } from '@/api/answer'
import { aiEvaluateBatch } from '@/api/answer'  // 新增导入

export default {
  data() {
    return {
      pageNum: 1,
      pageSize: 10,
      data: {},
      examId: '',
      realName: '',
      formInline: {},
      handleSizeChange: '',
      handleCurrentChange: '',
      batchAILoading: false  // 新增：批量AI评分加载状态
    }
  },
  created() {
    this.examId = localStorage.getItem('answer_examId')
    this.getAnswerUserPage(
      this.pageNum,
      this.pageSize,
      this.examId
    )
  },
  methods: {
    searchFun() {
      this.getAnswerUserPage(
        this.pageNum,
        this.pageSize,
        this.examId
      )
    },
    getAnswerUserPage(pageNum, pageSize, examId, realName) {
      const params = { pageNum: pageNum, pageSize: pageSize, examId: examId, 'realName': this.realName }
      answerUserPging(params).then((res) => {
        this.data = res.data
      })
    },
    screenInfo(row) {
      sessionStorage.setItem('answer_info', JSON.stringify(row))
      this.$router.push({ name: 'makeTest' })
    },

    // 新增：批量AI评分方法
    handleBatchAIScore() {
      this.$confirm('将对本场考试的所有主观题进行AI评分，这可能需要一些时间，是否继续？', '批量AI评分', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        this.batchAILoading = true
        
        try {
          const res = await aiEvaluateBatch(this.examId)
          
          if (res.code === 1) {
            this.$message({
              type: 'success',
              message: res.msg || '批量AI评分完成！',
              duration: 3000
            })
            // 刷新列表
            this.getAnswerUserPage(this.pageNum, this.pageSize, this.examId)
          } else {
            this.$message.error(res.msg || '批量AI评分失败')
          }
        } catch (error) {
          console.error('批量AI评分失败:', error)
          this.$message.error('批量AI评分异常，请稍后重试')
        } finally {
          this.batchAILoading = false
        }
      }).catch(() => {
        this.$message.info('已取消批量AI评分')
      })
    }
  }
}
</script>

<style scoped></style>