<template>
  <div class="login-container">
    <el-form
      ref="loginForm"
      :model="loginForm"
      :rules="loginRules"
      class="login-form"
      auto-complete="on"
      label-position="left"
    >
      <div class="title-container">
        <h3 class="title">登录</h3>
      </div>

      <el-form-item prop="username">
        <span class="svg-container">
          <svg-icon icon-class="user" />
        </span>
        <el-input
          ref="username"
          v-model="loginForm.username"
          placeholder="用户名"
          name="username"
          type="text"
          tabindex="1"
          auto-complete="on"
        />
      </el-form-item>

      <el-form-item prop="password">
        <span class="svg-container">
          <svg-icon icon-class="password" />
        </span>
        <el-input
          :key="passwordType"
          ref="password"
          v-model="loginForm.password"
          :type="passwordType"
          placeholder="密码"
          name="password"
          tabindex="2"
          auto-complete="on"
        />
        <span class="show-pwd" @click="showPwd">
          <svg-icon :icon-class="passwordType === 'password' ? 'eye' : 'eye-open'" />
        </span>
      </el-form-item>

      <div style="display: flex">
        <el-form-item prop="code">
          <span class="svg-container">
            <svg-icon icon-class="code" />
          </span>
          <el-input
            ref="codeInput"
            v-model="loginForm.code"
            style="width: 300px"
            placeholder="验证码"
            name="code"
            type="text"
            tabindex="3"
            auto-complete="off"
            @keyup.enter.native="handleLogin"
          />
        </el-form-item>
        <img
          ref="captchaImg"
          src="/api/auths/captcha"
          style="margin-left: 20px; height: 47px"
          alt=""
          @click="getVerify"
        >
      </div>
      <div
        v-if="enableRegister"
        style="
          display: flex;
          align-items: center;
          justify-content: flex-end;
          margin-bottom: 20px;
        "
      >
        <router-link style="color: #1abc9c" to="/register"> 立即注册 </router-link>
      </div>
      <el-form-item>
        <el-button
          :loading="loading"
          type="primary"
          style="width: 100%"
          @click.native.prevent="handleLogin"
        >登录</el-button>
      </el-form-item>

    </el-form>
    <!-- 添加备案信息 -->
    <div v-if="icpNumber" class="icp-info">
      <a :href="icpLink" target="_blank">{{ icpNumber }}</a>
    </div>
  </div>
</template>

<script>
import { validUsername } from '@/utils/validate'
import { getTokenInfo } from '@/utils/jwtUtils'
import { verifyCode } from '@/api/user'
import { Message } from 'element-ui'
import { Encrypt } from '@/utils/Secret'
export default {
  name: 'Login',
  data() {
    const validateUsername = (rule, value, callback) => {
      if (!validUsername(value)) {
        callback(new Error('请输入正确的用户名'))
      } else {
        callback()
      }
    }
    const validatePassword = (rule, value, callback) => {
      if (value.length < 6) {
        callback(new Error('密码不能少于6位'))
      } else {
        callback()
      }
    }
    return {
      loginForm: {
        username: '',
        password: '',
        code: ''
      },
      enableRegister: process.env.VUE_APP_ENABLE_REGISTER === 'true',
      icpNumber: process.env.VUE_APP_ICP_NUMBER,
      icpLink: process.env.VUE_APP_ICP_LINK,
      loginRules: {
        username: [{ required: true, trigger: 'blur', validator: validateUsername }],
        password: [{ required: true, trigger: 'blur', validator: validatePassword }],
        code: [{ required: true, trigger: 'blur', message: '请输入验证码' }]
      },
      loading: false,
      passwordType: 'password'
    }
  },
  computed: {
    redirect() {
      return this.$route.query.redirect || '/index'
    }
  },
  created() {
  },
  mounted() {
    this.$nextTick(() => {
      this.$refs.username.focus()
    })
  },
  methods: {
    getVerify() {
      this.$refs.captchaImg.src = `/api/auths/captcha?${Math.random()}`
    },

    showPwd() {
      if (this.passwordType === 'password') {
        this.passwordType = ''
      } else {
        this.passwordType = 'password'
      }
      this.$nextTick(() => {
        this.$refs.password.focus()
      })
    },
    handleLogin() {
      verifyCode(this.loginForm.code).then((res) => {
        if (res.code) {
          this.$refs.loginForm.validate((valid) => {
            if (valid) {
              this.loading = true
              const loginData = {
                username: this.loginForm.username,
                password: Encrypt(this.loginForm.password)
              }
              this.$store
                .dispatch('user/login', loginData)
                .then(() => {
                  this.$store.commit('menu/CLOSE_SIDEBAR')
                  const userInfo = getTokenInfo()
                  this.$store.dispatch('loginUser', { id: userInfo.id })
                  this.$router.push(this.redirect || '/index')

                  this.loading = false
                })
                .catch((error) => {
                  this.getVerify()
                  Message.error(error.msg)
                  this.loading = false
                })
            } else {
              return false
            }
          })
        } else {
          this.loginForm.code = ''
          this.getVerify()
          this.$message({
            type: 'info',
            message: res.msg
          })
        }
      })
    }
  }
}
</script>

<style lang="scss">
/* 修复input 背景不协调 和光标变色 */
$bg: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
$light_gray: #fff;
$cursor: #1abc9c;

@supports (-webkit-mask: none) and (not (cater-color: $cursor)) {
  .login-container .el-input input {
    color: $cursor;
  }
}

/* reset element-ui css */
.login-container {
    .el-input {
      display: inline-block;
      height: 47px;
      width: 85%;
      input {
        background: rgba(255, 255, 255, 0.1);
        border: 0px;
        -webkit-appearance: none;
        border-radius: 8px;
        padding: 12px 5px 12px 15px;
        color: $light_gray;
        height: 47px;
        caret-color: $cursor;
        transition: all 0.3s ease;
        &:-webkit-autofill {
          box-shadow: 0 0 0px 1000px rgba(26, 188, 156, 0.1) inset !important;
          -webkit-text-fill-color: $light_gray !important;
        }
        &:focus {
          background: rgba(255, 255, 255, 0.15);
        }
      }
    }

  .el-form-item {
    border: 1px solid rgba(26, 188, 156, 0.3);
    background: rgba(255, 255, 255, 0.05);
    border-radius: 12px;
    color: #fff;
    margin-bottom: 22px;
    transition: all 0.3s ease;
    &:hover {
      border-color: rgba(26, 188, 156, 0.6);
      background: rgba(255, 255, 255, 0.08);
      transform: translateY(-2px);
      box-shadow: 0 8px 16px rgba(26, 188, 156, 0.2);
    }
  }
  
  .el-button--primary {
    background: linear-gradient(135deg, #1abc9c 0%, #16a085 100%);
    border: none;
    border-radius: 12px;
    height: 50px;
    font-size: 16px;
    font-weight: 600;
    letter-spacing: 1px;
    transition: all 0.3s ease;
    box-shadow: 0 4px 15px rgba(26, 188, 156, 0.4);
    &:hover {
      background: linear-gradient(135deg, #48d5b5 0%, #1abc9c 100%);
      transform: translateY(-2px);
      box-shadow: 0 6px 20px rgba(26, 188, 156, 0.6);
    }
    &:active {
      transform: translateY(0);
    }
  }
}
</style>

<style lang="scss" scoped>
$dark_gray: #1abc9c;
$light_gray: #fff;

.login-container {
  min-height: 100vh;
  width: 100%;
  background: linear-gradient(135deg, #0f2027 0%, #203a43 25%, #2c5364 50%, #1abc9c 100%);
  background-size: 400% 400%;
  animation: gradientShift 15s ease infinite;
  overflow: hidden;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;

  &::before {
    content: '';
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(circle, rgba(26, 188, 156, 0.15) 0%, transparent 70%);
    animation: rotate 20s linear infinite;
  }

  @keyframes gradientShift {
    0% {
      background-position: 0% 50%;
    }
    50% {
      background-position: 100% 50%;
    }
    100% {
      background-position: 0% 50%;
    }
  }

  @keyframes rotate {
    from {
      transform: rotate(0deg);
    }
    to {
      transform: rotate(360deg);
    }
  }

  .login-form {
    position: relative;
    width: 480px;
    max-width: 90%;
    padding: 50px 40px;
    margin: 0 auto;
    background: rgba(255, 255, 255, 0.1);
    backdrop-filter: blur(20px);
    border-radius: 24px;
    border: 1px solid rgba(255, 255, 255, 0.2);
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3), 
                0 0 100px rgba(26, 188, 156, 0.2);
    overflow: visible;
    z-index: 1;
    animation: slideUp 0.6s ease;

    @keyframes slideUp {
      from {
        opacity: 0;
        transform: translateY(30px);
      }
      to {
        opacity: 1;
        transform: translateY(0);
      }
    }
  }

  .tips {
    font-size: 14px;
    color: #fff;
    margin-bottom: 10px;

    span {
      &:first-of-type {
        margin-right: 16px;
      }
    }
  }

  .svg-container {
    padding: 6px 5px 6px 15px;
    color: $dark_gray;
    vertical-align: middle;
    width: 30px;
    display: inline-block;
    font-size: 18px;
  }

  .title-container {
    position: relative;
    margin-bottom: 40px;

    .title {
      font-size: 36px;
      color: $light_gray;
      margin: 0 auto;
      text-align: center;
      font-weight: 700;
      letter-spacing: 2px;
      text-shadow: 0 2px 10px rgba(26, 188, 156, 0.3);
      position: relative;
      
      &::after {
        content: '';
        position: absolute;
        bottom: -12px;
        left: 50%;
        transform: translateX(-50%);
        width: 60px;
        height: 4px;
        background: linear-gradient(90deg, #1abc9c 0%, #16a085 100%);
        border-radius: 2px;
        box-shadow: 0 2px 8px rgba(26, 188, 156, 0.5);
      }
    }
  }

  .show-pwd {
    position: absolute;
    right: 15px;
    top: 14px;
    font-size: 18px;
    color: $dark_gray;
    cursor: pointer;
    user-select: none;
    transition: all 0.3s ease;
    &:hover {
      color: #48d5b5;
      transform: scale(1.1);
    }
  }
  
  .but {
    width: 220px;
    height: 39px;
    color: #fff;
    background: linear-gradient(135deg, #1abc9c 0%, #16a085 100%);
    border: none;
    padding: 12px 20px;
    font-size: 14px;
    border-radius: 12px;
    font-weight: 600;
    text-align: center;
    font-family: sans-serif;
    padding-top: 10px;
    box-shadow: 0 4px 15px rgba(26, 188, 156, 0.4);
    transition: all 0.3s ease;
    cursor: pointer;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 20px rgba(26, 188, 156, 0.6);
    }
  }
  
  .icp-info {
    position: fixed;
    bottom: 20px;
    width: 100%;
    text-align: center;
    z-index: 1;
    
    a {
      color: rgba(255, 255, 255, 0.6);
      font-size: 12px;
      text-decoration: none;
      transition: all 0.3s ease;
      
      &:hover {
        color: $light_gray;
        text-decoration: underline;
      }
    }
  }
  
  ::v-deep .el-form-item__error {
    color: #ffb3b3;
    font-size: 12px;
    padding-top: 4px;
  }
}
</style>