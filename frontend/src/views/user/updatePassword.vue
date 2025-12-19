<template>
  <div class="password-container">
    <div class="password-header">
      <div class="header-content">
        <i class="el-icon-lock"></i>
        <div>
          <h2 class="page-title">修改密码</h2>
          <p class="page-subtitle">Change Password</p>
        </div>
      </div>
    </div>

    <el-card class="password-card">
      <div slot="header" class="card-header">
        <div class="header-left">
          <i class="el-icon-edit"></i>
          <span>密码设置</span>
        </div>
        <div class="security-badge">
          <i class="el-icon-success"></i>
          安全设置
        </div>
      </div>

      <div class="card-body">
        <div class="password-tips">
          <div class="tip-icon">
            <i class="el-icon-warning"></i>
          </div>
          <div class="tip-content">
            <div class="tip-title">温馨提示</div>
            <ul class="tip-list">
              <li>密码长度至少6位字符</li>
              <li>建议使用字母、数字和符号的组合</li>
              <li>修改密码后需要重新登录</li>
              <li>请妥善保管您的密码</li>
            </ul>
          </div>
        </div>

        <el-form 
          ref="updatePasswordForm" 
          :label-position="labelPosition" 
          :model="updatePasswordForm" 
          label-width="100px"
          class="password-form"
        >
          <el-form-item label="原密码">
            <el-input 
              v-model="updatePasswordForm.originPassword" 
              type="password"
              placeholder="请输入原密码"
              prefix-icon="el-icon-lock"
              show-password
              autocomplete="off"
            />
          </el-form-item>
          
          <el-form-item label="新密码">
            <el-input 
              v-model="updatePasswordForm.newPassword" 
              type="password" 
              placeholder="请输入新密码"
              prefix-icon="el-icon-key"
              show-password
              autocomplete="off"
            />
          </el-form-item>
          
          <el-form-item label="确认密码">
            <el-input 
              v-model="updatePasswordForm.checkedPassword" 
              type="password" 
              placeholder="请再次输入新密码"
              prefix-icon="el-icon-key"
              show-password
              autocomplete="off"
            />
          </el-form-item>
          
          <el-form-item class="form-actions">
            <el-button 
              type="primary" 
              icon="el-icon-check"
              @click="updatePassword"
            >
              确认修改
            </el-button>
            <el-button 
              icon="el-icon-close"
              @click="cancelFun"
            >
              取消
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>

    <!-- 成功提示动画 -->
    <div v-if="showSuccess" class="success-overlay">
      <div class="success-content">
        <div class="success-icon">
          <i class="el-icon-circle-check"></i>
        </div>
        <div class="success-text">密码修改成功！</div>
        <div class="success-subtext">正在跳转到登录页...</div>
      </div>
    </div>
  </div>
</template>

<script>
import { changePassword } from '@/api/user'

export default {
  data() {
    return {
      labelPosition: 'right',
      updatePasswordForm: {
        originPassword: '',
        newPassword: '',
        checkedPassword: ''
      },
      formLabelWidth: '70px',
      showSuccess: false
    }
  },
  methods: {
    cancelFun() {
      this.$router.push({ path: 'index' })
    },
    
    updatePassword() {
      // 表单验证
      if (!this.updatePasswordForm.originPassword) {
        this.$message.warning('请输入原密码')
        return
      }
      if (!this.updatePasswordForm.newPassword) {
        this.$message.warning('请输入新密码')
        return
      }
      if (this.updatePasswordForm.newPassword.length < 6) {
        this.$message.warning('新密码长度不能少于6位')
        return
      }
      if (!this.updatePasswordForm.checkedPassword) {
        this.$message.warning('请确认新密码')
        return
      }
      if (this.updatePasswordForm.newPassword !== this.updatePasswordForm.checkedPassword) {
        this.$message.warning('两次输入的密码不一致')
        return
      }

      const data = { 
        originPassword: this.updatePasswordForm.originPassword,
        newPassword: this.updatePasswordForm.newPassword,
        checkedPassword: this.updatePasswordForm.checkedPassword 
      }
      
      changePassword(data).then((res) => {
        if (res.code) {
          this.showSuccess = true
          setTimeout(() => {
            this.$message({
              type: 'success',
              message: '修改成功',
              duration: 1500
            })
            setTimeout(() => {
              this.$router.push({ path: '/login', query: { zhi: 1 }})
            }, 1500)
          }, 1000)
        } else {
          this.$message({
            type: 'error',
            message: res.msg
          })
        }
      })
    }
  }
}
</script>

<style scoped>
.password-container {
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8f5f3 100%);
  min-height: calc(100vh - 110px);
  position: relative;
}

/* 页面头部 */
.password-header {
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

/* 密码卡片 */
.password-card {
  max-width: 800px;
  margin: 0 auto;
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

::v-deep .password-card .el-card__header {
  background: linear-gradient(135deg, #f8fffe 0%, #e8f5f3 100%);
  border-bottom: 2px solid #e8f5f3;
  padding: 20px 30px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
}

.header-left i {
  color: #1abc9c;
  font-size: 20px;
}

.security-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 16px;
  background: linear-gradient(135deg, rgba(26, 188, 156, 0.1) 0%, rgba(22, 160, 133, 0.1) 100%);
  color: #1abc9c;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
}

/* 卡片主体 */
.card-body {
  padding: 40px;
}

/* 提示信息 */
.password-tips {
  display: flex;
  gap: 16px;
  padding: 20px;
  background: linear-gradient(135deg, rgba(241, 196, 15, 0.1) 0%, rgba(243, 156, 18, 0.05) 100%);
  border-left: 4px solid #f39c12;
  border-radius: 8px;
  margin-bottom: 32px;
}

.tip-icon {
  font-size: 32px;
  color: #f39c12;
  flex-shrink: 0;
}

.tip-content {
  flex: 1;
}

.tip-title {
  font-size: 16px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 8px;
}

.tip-list {
  margin: 0;
  padding-left: 20px;
  color: #5a6c7d;
  font-size: 14px;
  line-height: 2;
}

.tip-list li {
  list-style: disc;
}

/* 表单样式 */
.password-form {
  max-width: 500px;
  margin: 0 auto;
}

::v-deep .password-form .el-form-item__label {
  font-weight: 500;
  color: #5a6c7d;
}

::v-deep .password-form .el-input__inner {
  border-radius: 8px;
  border-color: #e0e6ed;
  transition: all 0.3s ease;
  height: 44px;
  line-height: 44px;
}

::v-deep .password-form .el-input__inner:focus {
  border-color: #1abc9c;
  box-shadow: 0 0 0 2px rgba(26, 188, 156, 0.1);
}

::v-deep .password-form .el-input__prefix {
  display: flex;
  align-items: center;
  color: #1abc9c;
}

.form-actions {
  margin-top: 40px;
}

::v-deep .form-actions .el-form-item__content {
  display: flex;
  justify-content: center;
  gap: 16px;
}

::v-deep .password-form .el-button {
  padding: 12px 32px;
  font-size: 14px;
  font-weight: 500;
  border-radius: 8px;
  transition: all 0.3s ease;
}

::v-deep .password-form .el-button--primary {
  background: linear-gradient(135deg, #1abc9c 0%, #16a085 100%);
  border: none;
}

::v-deep .password-form .el-button--primary:hover {
  background: linear-gradient(135deg, #48d5b5 0%, #1abc9c 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(26, 188, 156, 0.3);
}

::v-deep .password-form .el-button:not(.el-button--primary) {
  background: #95a5a6;
  border: none;
  color: white;
}

::v-deep .password-form .el-button:not(.el-button--primary):hover {
  background: #7f8c8d;
  transform: translateY(-2px);
}

/* 成功提示覆盖层 */
.success-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.success-content {
  background: white;
  padding: 48px;
  border-radius: 20px;
  text-align: center;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: zoomIn 0.5s ease;
}

@keyframes zoomIn {
  from {
    opacity: 0;
    transform: scale(0.5);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.success-icon {
  font-size: 80px;
  color: #1abc9c;
  margin-bottom: 20px;
  animation: bounce 1s ease infinite;
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.success-text {
  font-size: 24px;
  font-weight: 600;
  color: #2c3e50;
  margin-bottom: 8px;
}

.success-subtext {
  font-size: 14px;
  color: #7f8c8d;
}

/* 响应式 */
@media screen and (max-width: 768px) {
  .password-container {
    padding: 15px;
  }

  .card-body {
    padding: 24px 20px;
  }

  .password-tips {
    flex-direction: column;
    text-align: center;
  }

  .password-form {
    max-width: 100%;
  }

  ::v-deep .form-actions .el-form-item__content {
    flex-direction: column;
  }

  ::v-deep .password-form .el-button {
    width: 100%;
  }
}
</style>