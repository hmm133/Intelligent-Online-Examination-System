<template>
  <div class="app-container">
    <div class="dashboard-grid">
      <!-- 左侧：登录时长图表 -->
      <div class="chart-section">
        <div class="section-card">
          <div class="card-header">
            <div class="header-content">
              <i class="el-icon-time"></i>
              <div>
                <div class="header-title">登录时长统计</div>
                <div class="header-subtitle">Login Duration Statistics</div>
              </div>
            </div>
            <div class="header-badge">近15天</div>
          </div>
          <div ref="charts" class="chart-div" />
        </div>
      </div>

      <!-- 右侧：最新公告 -->
      <div class="notice-section">
        <div class="section-card">
          <div class="card-header">
            <div class="header-content">
              <i class="el-icon-bell"></i>
              <div>
                <div class="header-title">最新公告</div>
                <div class="header-subtitle">Latest Announcements</div>
              </div>
            </div>
            <div class="notice-count">{{ noticePage.records.length }} 条</div>
          </div>
          <div class="notice-content">
            <el-collapse accordion v-if="noticePage.records.length > 0">
              <el-collapse-item
                v-for="(item, index) in noticePage.records"
                :key="index"
                :name="index"
              >
                <template slot="title">
                  <div class="notice-title">
                    <span class="notice-icon">
                      <i class="el-icon-document"></i>
                    </span>
                    <span class="notice-text">{{ item.title }}</span>
                  </div>
                </template>
                <div class="notice-body">
                  <div v-html="item.content" class="notice-html-content" />
                  <div class="notice-footer">
                    <div class="notice-author">
                      <i class="el-icon-user"></i>
                      {{ item.realName }}
                    </div>
                    <div class="notice-time">
                      <i class="el-icon-time"></i>
                      {{ item.createTime }}
                    </div>
                  </div>
                </div>
              </el-collapse-item>
            </el-collapse>
            <div v-else class="empty-notice">
              <i class="el-icon-warning-outline"></i>
              <div class="empty-text">暂无公告</div>
              <div class="empty-subtext">目前没有最新公告，请稍后再查看</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { noticeGetNew } from '@/api/notice'
import { getDaily } from '@/api/stat'
import echarts from 'echarts'

export default {
  data() {
    return {
      load: '',
      activeNames: '',
      pageNum: 1,
      pageSize: 10,
      noticePage: { records: [] },
      dateArray: [],
      formattedData: [],
      option: {
        tooltip: {
          trigger: 'axis',
          backgroundColor: 'rgba(255, 255, 255, 0.95)',
          borderColor: '#1abc9c',
          borderWidth: 1,
          textStyle: {
            color: '#333'
          },
          axisPointer: {
            type: 'shadow',
            shadowStyle: {
              color: 'rgba(26, 188, 156, 0.1)'
            }
          }
        },
        grid: {
          left: '3%',
          right: '4%',
          bottom: '3%',
          top: '3%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: [],
          axisLine: {
            lineStyle: {
              color: '#e0e6ed'
            }
          },
          axisLabel: {
            color: '#7f8c8d',
            fontSize: 11,
            rotate: 45
          }
        },
        yAxis: {
          type: 'value',
          name: '分钟',
          nameTextStyle: {
            color: '#7f8c8d',
            fontSize: 12
          },
          axisLine: {
            show: false
          },
          axisTick: {
            show: false
          },
          axisLabel: {
            color: '#7f8c8d',
            fontSize: 11
          },
          splitLine: {
            lineStyle: {
              color: '#e8f5f3',
              type: 'dashed'
            }
          }
        },
        series: [
          {
            name: '登录时长(分钟)',
            type: 'bar',
            data: [],
            barWidth: '50%',
            itemStyle: {
              borderRadius: [8, 8, 0, 0],
              color: {
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [
                  { offset: 0, color: '#1abc9c' },
                  { offset: 1, color: '#16a085' }
                ]
              }
            },
            emphasis: {
              itemStyle: {
                color: {
                  type: 'linear',
                  x: 0,
                  y: 0,
                  x2: 0,
                  y2: 1,
                  colorStops: [
                    { offset: 0, color: '#48d5b5' },
                    { offset: 1, color: '#1abc9c' }
                  ]
                }
              }
            }
          }
        ]
      },
      defaultProps: {
        children: 'children',
        label: 'label'
      },
      myChart: null
    }
  },
  created() {
    this.getDailyFun()
    this.getNotice(this.pageNum, this.pageSize)
  },
  mounted() {
    this.initCharts()
    window.addEventListener('resize', this.resizeChart)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.resizeChart)
    if (this.myChart) {
      this.myChart.dispose()
    }
  },
  methods: {
    getDailyFun() {
      getDaily().then((res) => {
        if (res.code === 1) {
          const currentDate = new Date()
          for (let i = 0; i <= 14; i++) {
            const date = new Date(
              currentDate.getTime() - i * 24 * 60 * 60 * 1000
            )
            this.dateArray.push(date.toISOString().split('T')[0])
          }
          this.dateArray.reverse()

          const dataMap = res.data.reduce((acc, item) => {
            acc[item.loginDate] = item.totalSeconds
            return acc
          }, {})

          this.formattedData = this.dateArray.map((date) => {
            const secondsOnDate = dataMap[date] || 0
            return secondsOnDate / 60
          })

          this.option.xAxis.data = this.dateArray
          this.option.series[0].data = this.formattedData
          this.$nextTick(() => {
            this.initCharts()
          })
        }
      })
    },
    async getNotice(pageNum, pageSize) {
      const params = { pageNum: pageNum, pageSize: pageSize }
      const res = await noticeGetNew(params)
      if (res && res.data) {
        this.noticePage = res.data
      } else {
        this.noticePage = { records: [] }
      }
    },
    initCharts() {
      this.myChart = echarts.init(this.$refs.charts)
      this.myChart.setOption(this.option)
    },
    resizeChart() {
      if (this.myChart) {
        this.myChart.resize()
      }
    },
    handleNodeClick(data) {
      // handle node click
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

.dashboard-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  animation: fadeIn 0.6s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.section-card {
  background: white;
  border-radius: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  height: 100%;
  transition: all 0.3s ease;
}

.section-card:hover {
  box-shadow: 0 8px 30px rgba(26, 188, 156, 0.15);
  transform: translateY(-4px);
}

/* 卡片头部 */
.card-header {
  padding: 24px 30px;
  background: linear-gradient(135deg, #f8fffe 0%, #e8f5f3 100%);
  border-bottom: 1px solid rgba(26, 188, 156, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-content > i {
  font-size: 24px;
  color: #1abc9c;
  background: rgba(26, 188, 156, 0.1);
  padding: 10px;
  border-radius: 12px;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 2px;
}

.header-subtitle {
  font-size: 12px;
  color: #95a5a6;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.header-badge {
  background: linear-gradient(135deg, #1abc9c 0%, #16a085 100%);
  color: white;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
  letter-spacing: 0.5px;
}

.notice-count {
  background: linear-gradient(135deg, #3498db 0%, #2980b9 100%);
  color: white;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}

/* 图表区域 */
.chart-div {
  height: 450px;
  padding: 20px;
}

/* 公告区域 */
.notice-content {
  max-height: 500px;
  overflow-y: auto;
  padding: 10px;
}

.notice-content::-webkit-scrollbar {
  width: 6px;
}

.notice-content::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.notice-content::-webkit-scrollbar-thumb {
  background: #1abc9c;
  border-radius: 10px;
}

.notice-content::-webkit-scrollbar-thumb:hover {
  background: #16a085;
}

/* Element UI 折叠面板样式覆盖 */
::v-deep .el-collapse {
  border: none;
}

::v-deep .el-collapse-item {
  margin-bottom: 12px;
  border-radius: 12px;
  overflow: hidden;
  background: #fafafa;
  transition: all 0.3s ease;
}

::v-deep .el-collapse-item:hover {
  background: #f0f9f7;
}

::v-deep .el-collapse-item__header {
  height: auto;
  line-height: 1.5;
  padding: 16px 20px;
  background: transparent;
  border: none;
  font-size: 15px;
  color: #2c3e50;
  transition: all 0.3s ease;
}

::v-deep .el-collapse-item__header:hover {
  color: #1abc9c;
}

::v-deep .el-collapse-item__arrow {
  color: #1abc9c;
  font-weight: bold;
}

::v-deep .el-collapse-item__wrap {
  border: none;
  background: white;
}

::v-deep .el-collapse-item__content {
  padding: 0 20px 20px;
  color: #5a6c7d;
  line-height: 1.8;
}

.notice-title {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
}

.notice-icon {
  background: linear-gradient(135deg, #1abc9c 0%, #16a085 100%);
  color: white;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.notice-text {
  flex: 1;
  font-weight: 500;
}

.notice-body {
  background: #f8fffe;
  padding: 16px;
  border-radius: 8px;
  margin-top: 12px;
}

.notice-html-content {
  margin-bottom: 16px;
  font-size: 14px;
  line-height: 1.8;
  color: #5a6c7d;
}

.notice-footer {
  display: flex;
  justify-content: space-between;
  padding-top: 12px;
  border-top: 1px solid #e8f5f3;
  font-size: 13px;
  color: #7f8c8d;
}

.notice-author,
.notice-time {
  display: flex;
  align-items: center;
  gap: 6px;
}

.notice-author i,
.notice-time i {
  color: #1abc9c;
}

/* 空状态 */
.empty-notice {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #95a5a6;
}

.empty-notice i {
  font-size: 64px;
  color: #d5e8e4;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 18px;
  font-weight: 500;
  color: #7f8c8d;
  margin-bottom: 8px;
}

.empty-subtext {
  font-size: 14px;
  color: #95a5a6;
}

/* 响应式布局 */
@media screen and (max-width: 1200px) {
  .dashboard-grid {
    grid-template-columns: 1fr;
  }

  .chart-div {
    height: 400px;
  }
}

@media screen and (max-width: 768px) {
  .app-container {
    padding: 15px;
  }

  .card-header {
    padding: 20px;
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .header-badge,
  .notice-count {
    align-self: flex-end;
  }

  .chart-div {
    height: 350px;
    padding: 15px;
  }

  .notice-footer {
    flex-direction: column;
    gap: 8px;
  }
}
</style>