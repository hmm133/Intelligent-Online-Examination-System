<template>
  <div class="profile-container">
    <div class="profile-header">
      <div class="header-content">
        <i class="el-icon-user-solid"></i>
        <div>
          <h2 class="page-title">个人中心</h2>
          <p class="page-subtitle">Personal Center</p>
        </div>
      </div>
    </div>

    <el-card class="profile-card">
      <div slot="header" class="card-header">
        <div class="header-left">
          <i class="el-icon-postcard"></i>
          <span>个人信息</span>
        </div>
        <div class="header-actions">
          <el-button
            v-if="data.gradeName == null && !isAdmin"
            type="text"
            size="small"
            class="action-link"
            icon="el-icon-plus"
            @click="addClassBt"
          >
            加入班级
          </el-button>
          <el-button
            v-if="data.gradeName != null"
            type="text"
            size="small"
            class="action-link"
            icon="el-icon-close"
            @click="exitGrade"
          >
            退出班级
          </el-button>
          <el-button
            type="text"
            size="small"
            class="action-link"
            icon="el-icon-picture"
            @click="fileDialogVisible = true"
          >
            编辑头像
          </el-button>
        </div>
      </div>

      <div class="card-body">
        <div class="info-section">
          <div class="info-item">
            <div class="info-label">
              <i class="el-icon-user"></i>
              用户名
            </div>
            <div class="info-value">{{ data.userName }}</div>
          </div>
          
          <div class="info-item">
            <div class="info-label">
              <i class="el-icon-postcard"></i>
              真实姓名
            </div>
            <div class="info-value">{{ data.realName }}</div>
          </div>
          
          <div v-if="!isAdmin" class="info-item">
            <div class="info-label">
              <i class="el-icon-school"></i>
              班级
            </div>
            <div class="info-value">
              <el-tag v-if="data.gradeName" type="success" effect="plain">
                {{ data.gradeName }}
              </el-tag>
              <span v-else class="no-class">暂未加入班级</span>
            </div>
          </div>
        </div>

        <div class="avatar-section">
          <div class="avatar-wrapper">
            <img :src="data.avatar" alt="用户头像" class="user-avatar">
            <div class="avatar-badge">
              <i class="el-icon-check"></i>
            </div>
          </div>
          <div class="avatar-tips">
            <i class="el-icon-info"></i>
            点击"编辑头像"更换头像
          </div>
        </div>
      </div>
    </el-card>

    <!-- 上传头像对话框 -->
    <el-dialog
      width="500px"
      :show-close="false"
      :close-on-click-modal="false"
      title="上传头像"
      :visible.sync="fileDialogVisible"
      class="upload-dialog"
    >
      <div class="upload-content">
        <div class="upload-tip">
          <i class="el-icon-info"></i>
          支持 png, jpg, jpeg, bmp 格式，文件大小不超过 2MB
        </div>
        
        <el-upload
          class="avatar-uploader"
          drag
          action="xxxxxx"
          multiple
          :limit="1"
          accept="png, jpg, jpeg, bmp"
          :auto-upload="false"
          :on-remove="handleRemove"
          :on-change="handleFileChange"
          :file-list="fileList"
        >
          <i class="el-icon-upload"></i>
          <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
          <div slot="tip" class="el-upload__tip">
            注意：右上角头像需要重新登录后更新
          </div>
        </el-upload>
      </div>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="fileDialogVisible = false">取消</el-button>
        <el-button type="primary" icon="el-icon-check" @click="importAvatar">
          确定上传
        </el-button>
      </div>
    </el-dialog>

    <!-- 加入班级对话框 -->
    <el-dialog 
      title="加入班级" 
      :visible.sync="addClassDialogVisible"
      width="450px"
      class="custom-dialog"
    >
      <el-form :model="form" label-width="90px">
        <el-form-item label="班级口令">
          <el-input 
            v-model="form.code" 
            autocomplete="off" 
            placeholder="请输入班级口令"
            prefix-icon="el-icon-key"
          />
        </el-form-item>
      </el-form>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="addClassDialogVisible = false">取消</el-button>
        <el-button type="primary" icon="el-icon-check" @click="addClass">
          确定加入
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { exitUserGrade, getInfo, userAddClass, uploadAvatar } from '@/api/user'
import { getRole } from '@/utils/jwtUtils'

export default {
  data() {
    return {
      fileDialogVisible: false,
      fileList: [],
      data: {},
      form: {
        code: ''
      },
      isAdmin: false,
      addClassDialogVisible: false
    }
  },
  created() {
    const role = getRole()
    if (role === 3 || role === 2) {
      this.isAdmin = true
    }
    this.getInfoFun()
  },
  methods: {
    exitGrade() {
      this.$confirm('退出班级, 是否继续?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          exitUserGrade()
            .then((res) => {
              if (res.code) {
                this.getInfoFun()
                this.$message({
                  type: 'success',
                  message: '退出成功!'
                })
              } else {
                this.$message({
                  type: 'error',
                  message: res.msg
                })
              }
            })
            .catch(() => {
              this.$message({
                type: 'info',
                message: '已取消退出'
              })
            })
        })
        .catch(() => {
          this.$message({
            type: 'info',
            message: '已取消退出'
          })
        })
    },
    
    async getInfoFun() {
      const res = await getInfo()
      if (res.code) {
        this.data = res.data
      } else {
        this.$message.error('获取个人信息失败')
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
    
    importAvatar() {
      if (this.fileList.length > 0) {
        const formData = new FormData()
        formData.append('file', this.fileList[0].raw)
        uploadAvatar(formData)
          .then((res) => {
            if (res.code) {
              this.getInfoFun()
              this.$message.success('文件上传成功！')
              this.fileDialogVisible = false
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
    
    addClassBt() {
      this.addClassDialogVisible = true
    },
    
    addClass() {
      const params = { code: this.form.code }
      userAddClass(params).then((res) => {
        if (res.code) {
          this.addClassDialogVisible = false
          this.getInfoFun()
          this.$message({
            type: 'success',
            message: '加入成功'
          })
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
.profile-container {
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8f5f3 100%);
  min-height: calc(100vh - 110px);
}

/* 页面头部 */
.profile-header {
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

/* 个人信息卡片 */
.profile-card {
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

::v-deep .profile-card .el-card__header {
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

.header-actions {
  display: flex;
  gap: 8px;
}

.action-link {
  color: #1abc9c;
  font-weight: 500;
  transition: all 0.3s ease;
}

.action-link:hover {
  color: #16a085;
  transform: translateY(-2px);
}

/* 卡片主体 */
.card-body {
  display: flex;
  justify-content: space-between;
  padding: 40px;
  gap: 60px;
}

.info-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 16px 0;
  border-bottom: 1px solid #e8f5f3;
}

.info-label {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 120px;
  font-size: 14px;
  color: #7f8c8d;
  font-weight: 500;
}

.info-label i {
  color: #1abc9c;
  font-size: 16px;
}

.info-value {
  font-size: 16px;
  color: #2c3e50;
  font-weight: 500;
}

.no-class {
  color: #95a5a6;
  font-style: italic;
}

/* 头像区域 */
.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.avatar-wrapper {
  position: relative;
}

.user-avatar {
  width: 150px;
  height: 150px;
  border-radius: 50%;
  border: 4px solid #1abc9c;
  object-fit: cover;
  box-shadow: 0 4px 20px rgba(26, 188, 156, 0.3);
  transition: all 0.3s ease;
}

.user-avatar:hover {
  transform: scale(1.05);
  box-shadow: 0 6px 30px rgba(26, 188, 156, 0.4);
}

.avatar-badge {
  position: absolute;
  bottom: 5px;
  right: 5px;
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #1abc9c 0%, #16a085 100%);
  border-radius: 50%;
  border: 3px solid white;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.avatar-badge i {
  color: white;
  font-size: 16px;
  font-weight: bold;
}

.avatar-tips {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #7f8c8d;
  font-size: 13px;
  padding: 8px 16px;
  background: rgba(26, 188, 156, 0.05);
  border-radius: 20px;
  border: 1px solid rgba(26, 188, 156, 0.1);
}

.avatar-tips i {
  color: #1abc9c;
}

/* 对话框样式 */
::v-deep .custom-dialog .el-dialog,
::v-deep .upload-dialog .el-dialog {
  border-radius: 16px;
}

::v-deep .custom-dialog .el-dialog__header,
::v-deep .upload-dialog .el-dialog__header {
  background: linear-gradient(135deg, #f8fffe 0%, #e8f5f3 100%);
  padding: 20px 24px;
  border-radius: 16px 16px 0 0;
}

::v-deep .custom-dialog .el-dialog__title,
::v-deep .upload-dialog .el-dialog__title {
  font-size: 18px;
  font-weight: 600;
  color: #2c3e50;
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

::v-deep .avatar-uploader .el-upload-dragger {
  border-radius: 8px;
  border: 2px dashed #d0e8e3;
  background: #f8fffe;
  transition: all 0.3s ease;
}

::v-deep .avatar-uploader .el-upload-dragger:hover {
  border-color: #1abc9c;
  background: #e8f5f3;
}

::v-deep .avatar-uploader .el-icon-upload {
  color: #1abc9c;
  font-size: 64px;
  margin: 20px 0;
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

/* 响应式 */
@media screen and (max-width: 768px) {
  .profile-container {
    padding: 15px;
  }

  .card-body {
    flex-direction: column;
    padding: 24px;
    gap: 32px;
  }

  .avatar-section {
    order: -1;
  }

  .header-actions {
    flex-direction: column;
    align-items: flex-end;
  }
}
</style>