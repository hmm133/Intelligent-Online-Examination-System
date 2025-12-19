<template>
  <div class="login-container">
    <el-form
      ref="registerForm"
      :model="registerForm"
      :rules="loginRules"
      class="login-form"
      auto-complete="on"
      label-position="left"
    >
      <div class="title-container">
        <h3 class="title">注册</h3>
      </div>

      <el-form-item prop="userName">
        <span class="svg-container">
          <svg-icon icon-class="user" />
        </span>
        <el-input
          ref="username"
          v-model="registerForm.userName"
          placeholder="用户名"
          name="username"
          type="text"
          tabindex="1"
          auto-complete="on"
        />
      </el-form-item>
      <el-form-item prop="realName">
        <span class="svg-container">
          <svg-icon icon-class="user" />
        </span>
        <el-input
          ref="realName"
          v-model="registerForm.realName"
          placeholder="真实姓名"
          name="realName"
          type="text"
          tabindex="2"
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
          v-model="registerForm.password"
          :type="passwordType"
          placeholder="密码"
          name="password"
          tabindex="3"
          auto-complete="on"
        />
        <span class="show-pwd" @click="showPwd">
          <svg-icon :icon-class="passwordType === 'password' ? 'eye' : 'eye-open'" />
        </span>
      </el-form-item>

      <el-form-item prop="checkedPassword">
        <span class="svg-container">
          <svg-icon icon-class="password" />
        </span>
        <el-input
          :key="checkedPasswordType"
          ref="checkedPassword"
          v-model="registerForm.checkedPassword"
          :type="checkedPasswordType"
          placeholder="确认密码"
          name="checkedPassword"
          tabindex="4"
          auto-complete="on"
        />
        <span class="show-pwd" @click="showPwd2">
          <svg-icon
            :icon-class="checkedPasswordType === 'password' ? 'eye' : 'eye-open'"
          />
        </span>
      </el-form-item>

      <div style="display: flex">
        <el-form-item prop="code">
          <span class="svg-container">
            <svg-icon icon-class="code" />
          </span>
          <el-input
            ref="code"
            v-model="registerForm.code"
            style="width: 300px"
            placeholder="验证码"
            name="code"
            type="text"
            tabindex="5"
            auto-complete="off"
            @keyup.enter.native="registerFn"
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
        style="
          display: flex;
          align-items: center;
          justify-content: flex-end;
          margin-bottom: 20px;
        "
      >
        <router-link style="color: #1abc9c" to="/login"> 登录 </router-link>
      </div>

      <el-button
        :loading="loading"
        type="primary"
        style="width: 100%; margin-bottom: 30px"
        @click="registerFn"
      >注册</el-button>
    </el-form>
    <!-- 添加备案信息 -->
    <div v-if="icpNumber" class="icp-info">
      <a :href="icpLink" target="_blank">{{ icpNumber }}</a>
    </div>
  </div>
</template>

<script>
import { validUsername } from '@/utils/validate'
import { verifyCode, register } from '@/api/user'
import { Message } from 'element-ui'
import { Encrypt } from '@/utils/Secret'
export default {
  name: 'Register',
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
    const validateRealName = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请输入真实姓名'))
      } else {
        callback()
      }
    }
    const validateCheckedPassword = (rule, value, callback) => {
      if (value  != this.registerForm.password) {
        callback(new Error('两次输入密码不一致'))
      } else {
        callback()
      }
    }
    const validateCode = (rule, value, callback) => {
      if (!value) {
        callback(new Error('请输入验证码'))
      } else {
        callback()
      }
    }
    return {
      icpNumber: process.env.VUE_APP_ICP_NUMBER,
      icpLink: process.env.VUE_APP_ICP_LINK,
      registerForm: {
        userName: '',
        password: '',
        realName: '',
        checkedPassword: '',
        code: ''
      },
      loginRules: {
        userName: [{ required: true, trigger: 'blur', validator: validateUsername }],
        password: [{ required: true, trigger: 'blur', validator: validatePassword }],
        realName: [{ required: true, trigger: 'blur', validator: validateRealName }],
        checkedPassword: [{ required: true, trigger: 'blur', validator: validateCheckedPassword }],
        code: [{ required: true, trigger: 'blur', validator: validateCode }]
      },
      loading: false,
      passwordType: 'password',
      checkedPasswordType: 'password',
      redirect: undefined
    }
  },
  watch: {
    $route: {
      handler: function(route) {
        this.redirect = route.query && route.query.redirect
      },
      immediate: true
    }
  },
  created() {
  },
  methods: {
    registerFn() {
      this.$refs.registerForm.validate(valid => {
        if (valid) {
          verifyCode(this.registerForm.code).then((res) => {
            if (res.code) {
              const registerData = {
                userName: this.registerForm.userName,
                realName: this.registerForm.realName,
                password: Encrypt(this.registerForm.password),
                checkedPassword: Encrypt(this.registerForm.checkedPassword)
              }
              register(registerData).then((res2) => {
                if (res2.code) {
                  Message({
                    message: res2.msg,
                    type: 'success',
                    duration: 5 * 1000
                  })
                  this.$router.push({ path: '/login' })
                } else {
                  this.getVerify()
                  Message({
                    message: res2.msg,
                    type: 'error',
                    duration: 5 * 1000
                  })
                }
              }).catch(() => {
                this.getVerify()
                Message({
                  message: '注册失败,请重试',
                  type: 'error',
                  duration: 5 * 1000
                })
              })
            } else {
              this.getVerify()
              Message({
                message: res.msg || '验证码验证失败',
                type: 'error',
                duration: 5 * 1000
              })
            }
          }).catch(() => {
            this.getVerify()
            Message({
              message: '验证码验证失败',
              type: 'error',
              duration: 5 * 1000
            })
          })
        } else {
          Message({
            message: '请填写完整的注册信息',
            type: 'warning',
            duration: 5 * 1000
          })
        }
      })
    },
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
    showPwd2() {
      if (this.checkedPasswordType === 'password') {
        this.checkedPasswordType = ''
      } else {
        this.checkedPasswordType = 'password'
      }
      this.$nextTick(() => {
        this.$refs.checkedPassword.focus()
      })
    }
  }
}
</script>

<style lang="scss">
$bg: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
$light_gray: #fff;
$cursor: #1abc9c;

@supports (-webkit-mask: none) and (not (cater-color: $cursor)) {
  .login-container .el-input input {
    color: $cursor;
  }
}

$dark_gray: #1abc9c;

.login-container {
  min-height: 100vh;
  width: 100%;
  background: linear-gradient(135deg, #0f2027 0%, #203a43 25%, #2c5364 50%, #1abc9c 100%);
  background-size: 400% 400%;
  animation: gradientShift 15s ease infinite;
  overflow: hidden;
  
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
    margin-bottom: 18px;
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
    animation: slideUp 0.6s ease;
    top: 50%;
    transform: translateY(-50%);

    @keyframes slideUp {
      from {
        opacity: 0;
        transform: translateY(calc(-50% + 30px));
      }
      to {
        opacity: 1;
        transform: translateY(-50%);
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
    margin-bottom: 35px;

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
</style>