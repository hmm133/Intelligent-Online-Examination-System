<template>
  <div class="ai-score-card">
    <!-- AI评分加载中 -->
    <el-card v-if="loading" class="ai-card loading-card">
      <div class="loading-content">
        <i class="el-icon-loading" />
        <span>AI评分中...</span>
      </div>
    </el-card>

    <!-- AI评分结果 -->
    <el-card v-else-if="aiScore" class="ai-card" :class="confidenceClass">
      <div slot="header" class="card-header">
        <span class="header-title">
          <i class="el-icon-magic-stick" />
          AI评分建议
        </span>
        <el-tag v-if="aiScore.aiConfidence < 0.6" type="warning" size="mini">
          低置信度
        </el-tag>
        <el-tag v-else-if="aiScore.aiConfidence >= 0.85" type="success" size="mini">
          高置信度
        </el-tag>
        <el-tag v-else type="info" size="mini">
          中等置信度
        </el-tag>
      </div>

      <!-- AI分数 -->
      <div class="score-section">
        <div class="score-label">AI建议分数：</div>
        <div class="score-value">
          <span class="score-number">{{ aiScore.aiScore }}</span>
          <span class="score-total">/ {{ aiScore.totalScore }}</span>
        </div>
        <div class="confidence">
          置信度：{{ (aiScore.aiConfidence * 100).toFixed(0) }}%
        </div>
      </div>

      <!-- AI评语 -->
      <div class="comment-section">
        <div class="comment-label">AI评语：</div>
        <div class="comment-content">{{ aiScore.aiComment }}</div>
      </div>

      <!-- 分数差异对比（仅在教师打分后显示） -->
      <div v-if="teacherScore !== null && teacherScore !== undefined" class="diff-section">
        <el-divider />
        <div class="diff-content">
          <span class="diff-label">教师评分：</span>
          <span class="diff-value">{{ teacherScore }}</span>
          <span 
            class="diff-gap" 
            :class="diffClass"
          >
            {{ diffText }}
          </span>
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="action-section">
        <el-button 
          v-if="!adopted" 
          type="primary" 
          size="small" 
          icon="el-icon-check"
          @click="handleAdopt"
        >
          采纳AI分数
        </el-button>
        <el-tag v-else type="success" size="small">
          <i class="el-icon-success" /> 已采纳
        </el-tag>
      </div>
    </el-card>

    <!-- 未评分/错误状态 -->
    <el-card v-else-if="error" class="ai-card error-card">
      <div class="error-content">
        <i class="el-icon-warning" />
        <span>{{ error }}</span>
        <el-button type="text" size="small" @click="$emit('retry')">
          重试
        </el-button>
      </div>
    </el-card>

    <!-- 默认提示 -->
    <el-card v-else class="ai-card empty-card">
      <div class="empty-content">
        <i class="el-icon-info" />
        <span>点击"获取AI评分"按钮获取AI评分建议</span>
      </div>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'AIScoreCard',
  props: {
    // AI评分数据
    aiScore: {
      type: Object,
      default: null
      // 格式: { aiScore, aiComment, aiConfidence, totalScore }
    },
    // 教师实际打分
    teacherScore: {
      type: Number,
      default: null
    },
    // 是否已采纳
    adopted: {
      type: Boolean,
      default: false
    },
    // 加载状态
    loading: {
      type: Boolean,
      default: false
    },
    // 错误信息
    error: {
      type: String,
      default: ''
    }
  },
  computed: {
    // 置信度样式类
    confidenceClass() {
      if (!this.aiScore) return ''
      const confidence = this.aiScore.aiConfidence
      if (confidence < 0.6) return 'low-confidence'
      if (confidence >= 0.85) return 'high-confidence'
      return 'medium-confidence'
    },
    // 分数差异
    scoreDiff() {
      if (this.teacherScore === null || !this.aiScore) return 0
      return this.teacherScore - this.aiScore.aiScore
    },
    // 差异百分比
    diffPercent() {
      if (!this.aiScore || !this.aiScore.totalScore) return 0
      return Math.abs(this.scoreDiff / this.aiScore.totalScore * 100)
    },
    // 差异文本
    diffText() {
      const diff = this.scoreDiff
      if (diff === 0) return '(无差异)'
      const sign = diff > 0 ? '+' : ''
      return `(${sign}${diff.toFixed(1)}分)`
    },
    // 差异样式类
    diffClass() {
      if (this.diffPercent > 15) return 'large-diff'
      if (this.diffPercent > 5) return 'medium-diff'
      return 'small-diff'
    }
  },
  methods: {
    // 采纳AI分数
    handleAdopt() {
      this.$emit('adopt', this.aiScore.aiScore)
    }
  }
}
</script>

<style scoped lang="scss">
.ai-score-card {
  margin: 15px 0;

  .ai-card {
    border-left: 4px solid #409EFF;
    
    &.low-confidence {
      border-left-color: #E6A23C;
      background-color: #fdf6ec;
    }
    
    &.high-confidence {
      border-left-color: #67C23A;
    }
    
    &.medium-confidence {
      border-left-color: #909399;
    }

    &.loading-card,
    &.error-card,
    &.empty-card {
      border-left-color: #909399;
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .header-title {
      font-weight: bold;
      color: #303133;
      
      i {
        margin-right: 5px;
        color: #409EFF;
      }
    }
  }

  .score-section {
    display: flex;
    align-items: center;
    margin-bottom: 15px;
    padding: 10px;
    background-color: #f5f7fa;
    border-radius: 4px;

    .score-label {
      font-weight: bold;
      color: #606266;
      margin-right: 10px;
    }

    .score-value {
      flex: 1;
      
      .score-number {
        font-size: 28px;
        font-weight: bold;
        color: #409EFF;
      }
      
      .score-total {
        font-size: 18px;
        color: #909399;
        margin-left: 5px;
      }
    }

    .confidence {
      font-size: 14px;
      color: #909399;
    }
  }

  .comment-section {
    margin-bottom: 15px;

    .comment-label {
      font-weight: bold;
      color: #606266;
      margin-bottom: 8px;
    }

    .comment-content {
      padding: 10px;
      background-color: #f5f7fa;
      border-radius: 4px;
      line-height: 1.6;
      color: #606266;
      white-space: pre-wrap;
    }
  }

  .diff-section {
    .diff-content {
      display: flex;
      align-items: center;
      padding: 8px;
      background-color: #f5f7fa;
      border-radius: 4px;

      .diff-label {
        font-weight: bold;
        color: #606266;
        margin-right: 10px;
      }

      .diff-value {
        font-size: 18px;
        font-weight: bold;
        color: #303133;
        margin-right: 10px;
      }

      .diff-gap {
        font-size: 14px;
        font-weight: bold;
        
        &.small-diff {
          color: #67C23A;
        }
        
        &.medium-diff {
          color: #E6A23C;
        }
        
        &.large-diff {
          color: #F56C6C;
        }
      }
    }
  }

  .action-section {
    margin-top: 15px;
    text-align: right;
  }

  .loading-content,
  .error-content,
  .empty-content {
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 20px;
    color: #909399;

    i {
      font-size: 20px;
      margin-right: 10px;
    }
  }

  .error-content {
    color: #F56C6C;
    
    i {
      color: #F56C6C;
    }
  }
}
</style>