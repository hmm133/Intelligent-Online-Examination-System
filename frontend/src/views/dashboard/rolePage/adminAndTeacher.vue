<template>
  <div class="app-container">
    <!-- 数据卡片区域 -->
    <div class="stats-container">
      <div class="stats-row">
        <!-- 班级总数卡片 -->
        <div class="stat-card card-1">
          <div class="card-background">
            <div class="circle circle-1"></div>
            <div class="circle circle-2"></div>
          </div>
          <div class="card-content">
            <div class="icon-container">
              <el-image
                style="width: 55px; height: 55px"
                :src="iconUrl.gradeImgUrl"
              />
            </div>
            <div class="stat-info">
              <div class="stat-title">班级总数</div>
              <div class="stat-value">{{ classCount }}</div>
              <div class="stat-label">Classes</div>
            </div>
          </div>
        </div>

        <!-- 试题总数卡片 -->
        <div class="stat-card card-2">
          <div class="card-background">
            <div class="circle circle-1"></div>
            <div class="circle circle-2"></div>
          </div>
          <div class="card-content">
            <div class="icon-container">
              <el-image
                style="width: 55px; height: 55px"
                :src="iconUrl.questionImgUrl"
              />
            </div>
            <div class="stat-info">
              <div class="stat-title">试题总数</div>
              <div class="stat-value">{{ quCount }}</div>
              <div class="stat-label">Questions</div>
            </div>
          </div>
        </div>

        <!-- 试卷总数卡片 -->
        <div class="stat-card card-3">
          <div class="card-background">
            <div class="circle circle-1"></div>
            <div class="circle circle-2"></div>
          </div>
          <div class="card-content">
            <div class="icon-container">
              <el-image
                style="width: 55px; height: 55px"
                :src="iconUrl.examImgUrl"
              />
            </div>
            <div class="stat-info">
              <div class="stat-title">试卷总数</div>
              <div class="stat-value">{{ examCount }}</div>
              <div class="stat-label">Exams</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <div class="loading-spinner"></div>
      <div class="loading-text">数据加载中...</div>
    </div>

    <!-- 错误提示 -->
    <div v-if="error" class="error-message">
      <i class="el-icon-warning"></i>
      {{ errorMessage }}
    </div>

    <!-- 图表区域 -->
    <div v-else class="charts-container">
      <div class="chart-wrapper">
        <div class="chart-header">
          <div class="chart-title">
            <i class="el-icon-pie-chart"></i>
            班级人数分布
          </div>
          <div class="chart-subtitle">Class Student Distribution</div>
        </div>
        <div ref="classChart" class="chart-box" />
      </div>
      
      <div class="chart-wrapper">
        <div class="chart-header">
          <div class="chart-title">
            <i class="el-icon-document"></i>
            班级试卷分布
          </div>
          <div class="chart-subtitle">Class Exam Distribution</div>
        </div>
        <div ref="examChart" class="chart-box" />
      </div>
    </div>
  </div>
</template>

<script>
import echarts from 'echarts'
import { classCount, classExamCount, classAllCounts } from '@/api/stat'
import questionImgUrl from '@/assets/img/questions.png'
import examImgUrl from '@/assets/img/paper.png'
import gradePng from '@/assets/img/grade.png'

export default {
  name: 'AdminDashboard',
  data() {
    return {
      iconUrl: {
        gradeImgUrl: gradePng,
        questionImgUrl: questionImgUrl,
        examImgUrl: examImgUrl
      },
      chartData: [],
      chartDataTitle: [],
      chartData2: [],
      chartDataTitle2: [],
      classCount: 0,
      quCount: 0,
      examCount: 0,
      loading: true,
      error: false,
      errorMessage: '',
      classChartInstance: null,
      examChartInstance: null
    }
  },

  async created() {
    try {
      await this.fetchAllData()
    } catch (error) {
      this.handleError(error)
    }
  },

  mounted() {
    this.$nextTick(() => {
      this.initCharts()
      window.addEventListener('resize', this.handleResize)
    })
  },

  beforeDestroy() {
    if (this.classChartInstance) {
      this.classChartInstance.dispose()
    }
    if (this.examChartInstance) {
      this.examChartInstance.dispose()
    }
    window.removeEventListener('resize', this.handleResize)
  },

  methods: {
    async fetchAllData() {
      this.loading = true
      try {
        const res0 = await classAllCounts()
        if (res0.data) {
          this.classCount = res0.data.classCount
          this.quCount = res0.data.questionCount
          this.examCount = res0.data.examCount
        } else {
          this.classCount = 0
          this.quCount = 0
          this.examCount = 0
        }

        const res1 = await classCount()
        if (res1.data) {
          this.processChartData(res1.data)
        } else {
          this.chartData = [{ name: '暂无数据', value: 1 }]
          this.chartDataTitle = ['暂无数据']
          if (this.classChartInstance) {
            this.updateClassChart()
          }
        }

        const res2 = await classExamCount()
        if (res2.data) {
          this.processChartData2(res2.data)
        } else {
          this.chartData2 = [{ name: '暂无数据', value: 1 }]
          this.chartDataTitle2 = ['暂无数据']
          if (this.examChartInstance) {
            this.updateExamChart()
          }
        }

        this.loading = false
      } catch (error) {
        this.handleError(error)
      }
    },

    handleError(error) {
      this.loading = false
      this.error = true
      this.errorMessage = `数据加载失败: ${error.message || '未知错误'}`
      console.error('数据加载失败:', error)
    },

    handleResize() {
      if (this.classChartInstance) {
        this.classChartInstance.resize()
      }
      if (this.examChartInstance) {
        this.examChartInstance.resize()
      }
    },

    initCharts() {
      this.$nextTick(() => {
        this.classChartInstance = echarts.init(this.$refs.classChart)
        this.examChartInstance = echarts.init(this.$refs.examChart)
        this.updateClassChart()
        this.updateExamChart()
      })
    },

    processChartData(data) {
      if (data.length === 0) {
        this.chartData = [{ name: '暂无数据', value: 1 }]
        this.chartDataTitle = ['暂无数据']
      } else {
        this.chartData = data.map((item) => ({
          name: item.gradeName,
          value: item.totalStudent
        }))
        this.chartDataTitle = this.chartData.map((item) => item.name)
      }

      if (this.classChartInstance) {
        this.updateClassChart()
      }
    },

    processChartData2(data) {
      if (data.length === 0) {
        this.chartData2 = [{ name: '暂无数据', value: 1 }]
        this.chartDataTitle2 = ['暂无数据']
      } else {
        this.chartData2 = data.map((item) => ({
          name: item.gradeName,
          value: item.total
        }))
        this.chartDataTitle2 = this.chartData2.map((item) => item.name)
      }

      if (this.examChartInstance) {
        this.updateExamChart()
      }
    },

    updateClassChart() {
      if (!this.classChartInstance) return

      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{a}<br/>{b}: {c} ({d}%)',
          backgroundColor: 'rgba(255, 255, 255, 0.95)',
          borderColor: '#1abc9c',
          borderWidth: 1,
          textStyle: {
            color: '#333'
          }
        },
        legend: {
          bottom: 20,
          left: 'center',
          itemWidth: 14,
          itemHeight: 14,
          textStyle: {
            color: '#666',
            fontSize: 13
          },
          data: this.chartDataTitle
        },
        color: ['#1abc9c', '#16a085', '#48d5b5', '#0fb9b1', '#2ecc71', '#27ae60'],
        series: {
          name: '班级人数',
          type: 'pie',
          radius: ['45%', '70%'],
          center: ['50%', '45%'],
          avoidLabelOverlap: true,
          itemStyle: {
            borderRadius: 8,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: true,
            position: 'outside',
            formatter: '{b}\n{d}%',
            fontSize: 12,
            color: '#666'
          },
          labelLine: {
            show: true,
            length: 15,
            length2: 10,
            smooth: true
          },
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(26, 188, 156, 0.5)'
            },
            label: {
              show: true,
              fontSize: 14,
              fontWeight: 'bold'
            }
          },
          data: this.chartData
        }
      }

      this.classChartInstance.setOption(option)
    },

    updateExamChart() {
      if (!this.examChartInstance) return

      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{a}<br/>{b}: {c} ({d}%)',
          backgroundColor: 'rgba(255, 255, 255, 0.95)',
          borderColor: '#1abc9c',
          borderWidth: 1,
          textStyle: {
            color: '#333'
          }
        },
        legend: {
          bottom: 20,
          left: 'center',
          itemWidth: 14,
          itemHeight: 14,
          textStyle: {
            color: '#666',
            fontSize: 13
          },
          data: this.chartDataTitle2
        },
        color: ['#3498db', '#9b59b6', '#e74c3c', '#f39c12', '#1abc9c', '#16a085'],
        series: {
          name: '试卷数量',
          type: 'pie',
          radius: ['45%', '70%'],
          center: ['50%', '45%'],
          avoidLabelOverlap: true,
          itemStyle: {
            borderRadius: 8,
            borderColor: '#fff',
            borderWidth: 2
          },
          label: {
            show: true,
            position: 'outside',
            formatter: '{b}\n{d}%',
            fontSize: 12,
            color: '#666'
          },
          labelLine: {
            show: true,
            length: 15,
            length2: 10,
            smooth: true
          },
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(26, 188, 156, 0.5)'
            },
            label: {
              show: true,
              fontSize: 14,
              fontWeight: 'bold'
            }
          },
          data: this.chartData2
        }
      }

      this.examChartInstance.setOption(option)
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

/* 统计卡片容器 */
.stats-container {
  margin-bottom: 30px;
  animation: fadeInDown 0.6s ease;
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

.stats-row {
  display: flex;
  justify-content: space-between;
  gap: 24px;
  flex-wrap: wrap;
}

/* 统计卡片 */
.stat-card {
  flex: 1;
  min-width: 280px;
  height: 140px;
  border-radius: 20px;
  overflow: hidden;
  position: relative;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  box-shadow: 0 4px 20px rgba(26, 188, 156, 0.1);
}

.stat-card:hover {
  transform: translateY(-8px);
  box-shadow: 0 12px 40px rgba(26, 188, 156, 0.25);
}

.card-background {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  overflow: hidden;
}

.circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.1;
}

.circle-1 {
  width: 120px;
  height: 120px;
  top: -40px;
  right: -40px;
}

.circle-2 {
  width: 80px;
  height: 80px;
  bottom: -20px;
  left: -20px;
}

.card-1 {
  background: linear-gradient(135deg, #1abc9c 0%, #16a085 100%);
}

.card-1 .circle {
  background: #fff;
}

.card-2 {
  background: linear-gradient(135deg, #3498db 0%, #2980b9 100%);
}

.card-2 .circle {
  background: #fff;
}

.card-3 {
  background: linear-gradient(135deg, #9b59b6 0%, #8e44ad 100%);
}

.card-3 .circle {
  background: #fff;
}

.card-content {
  position: relative;
  display: flex;
  align-items: center;
  padding: 25px 30px;
  height: 100%;
  gap: 20px;
}

.icon-container {
  background: rgba(255, 255, 255, 0.2);
  padding: 12px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.stat-card:hover .icon-container {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.1);
}

.stat-info {
  flex: 1;
  color: white;
}

.stat-title {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 8px;
  opacity: 0.95;
  letter-spacing: 0.5px;
}

.stat-value {
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 4px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.stat-label {
  font-size: 12px;
  opacity: 0.8;
  text-transform: uppercase;
  letter-spacing: 1px;
}

/* 图表容器 */
.charts-container {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
  animation: fadeInUp 0.6s ease 0.2s backwards;
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

.chart-wrapper {
  flex: 1;
  min-width: 400px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  transition: all 0.3s ease;
}

.chart-wrapper:hover {
  box-shadow: 0 8px 30px rgba(26, 188, 156, 0.15);
  transform: translateY(-4px);
}

.chart-header {
  padding: 24px 30px 16px;
  background: linear-gradient(135deg, #f8fffe 0%, #e8f5f3 100%);
  border-bottom: 1px solid rgba(26, 188, 156, 0.1);
}

.chart-title {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.chart-title i {
  color: #1abc9c;
  font-size: 20px;
}

.chart-subtitle {
  font-size: 12px;
  color: #95a5a6;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.chart-box {
  height: 400px;
  padding: 20px;
}

/* 加载状态 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 400px;
  background: white;
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 4px solid #e8f5f3;
  border-top: 4px solid #1abc9c;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

.loading-text {
  margin-top: 20px;
  font-size: 16px;
  color: #7f8c8d;
  font-weight: 500;
}

/* 错误提示 */
.error-message {
  background: linear-gradient(135deg, #fff5f5 0%, #ffe5e5 100%);
  color: #e74c3c;
  padding: 20px 30px;
  border-radius: 12px;
  text-align: center;
  font-size: 16px;
  border: 1px solid rgba(231, 76, 60, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}

.error-message i {
  font-size: 20px;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 响应式布局 */
@media screen and (max-width: 1200px) {
  .chart-wrapper {
    min-width: 100%;
  }
}

@media screen and (max-width: 768px) {
  .app-container {
    padding: 15px;
  }

  .stats-row {
    flex-direction: column;
    gap: 16px;
  }

  .stat-card {
    min-width: 100%;
  }

  .charts-container {
    flex-direction: column;
  }

  .chart-wrapper {
    min-width: 100%;
  }

  .chart-box {
    height: 350px;
  }
}
</style>