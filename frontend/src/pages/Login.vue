<template>
  <div class="login-container">
    <div class="login-card">
      <h2>仓库管理系统</h2>
      <div class="login-tabs">
        <button :class="{ active: loginType === 'password' && !showForgotPassword }" @click="loginType = 'password'; showForgotPassword = false">
          密码登录
        </button>
        <button :class="{ active: loginType === 'code' && !showForgotPassword }" @click="loginType = 'code'; showForgotPassword = false">
          验证码登录
        </button>
        <button :class="{ active: showForgotPassword }" @click="showForgotPassword = true; loginType = 'password'">
          修改密码
        </button>
      </div>

      <!-- 登录表单 -->
      <form v-if="!showForgotPassword" @submit.prevent="handleLogin">
        <div class="form-group">
          <label>邮箱</label>
          <input type="email" v-model="email" required placeholder="请输入邮箱" />
        </div>

        <div class="form-group" v-if="loginType === 'password'">
          <label>密码</label>
          <input type="password" v-model="password" required placeholder="请输入密码" />
        </div>

        <div class="form-group" v-else>
          <label>验证码</label>
          <div class="code-input-group">
            <input type="text" v-model="code" required placeholder="请输入验证码" maxlength="6" />
            <button type="button" @click="handleSendCode" :disabled="loading || codeSent" class="btn btn-primary">
              {{ codeSent ? '已发送' : '获取验证码' }}
            </button>
          </div>
        </div>

        <div class="error-message" v-if="error">{{ error }}</div>

        <button type="submit" class="btn btn-primary login-btn" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </form>

      <!-- 修改密码表单 -->
      <form v-else @submit.prevent="handleUpdatePassword">
        <div class="form-group">
          <label>邮箱</label>
          <input type="email" v-model="forgotPasswordEmail" required placeholder="请输入邮箱" />
        </div>

        <div class="form-group">
          <label>验证码</label>
          <div class="code-input-group">
            <input type="text" v-model="forgotPasswordCode" required placeholder="请输入验证码" maxlength="6" />
            <button type="button" @click="handleSendForgotPasswordCode" :disabled="loading || forgotPasswordCodeSent" class="btn btn-primary">
              {{ forgotPasswordCodeSent ? '已发送' : '获取验证码' }}
            </button>
          </div>
        </div>

        <div class="form-group">
          <label>新密码</label>
          <input type="password" v-model="newPassword" required placeholder="请输入新密码" minlength="2" maxlength="10" />
        </div>

        <div class="form-group">
          <label>确认新密码</label>
          <input type="password" v-model="confirmPassword" required placeholder="请再次输入新密码" minlength="2" maxlength="10" />
        </div>

        <transition name="fade">
          <div class="error-message" v-if="error">{{ error }}</div>
        </transition>
        <transition name="fade">
          <div class="success-message" v-if="successMessage">{{ successMessage }}</div>
        </transition>

        <button type="submit" class="btn btn-primary login-btn" :disabled="loading">
          {{ loading ? '修改中...' : '修改密码' }}
        </button>
        <button type="button" class="btn login-btn" @click="handleReturnToLogin" style="margin-top: 10px;">
          返回登录
        </button>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { userApi } from '@/api/user'
import { LoginRequest } from '@/types'

const router = useRouter()
const authStore = useAuthStore()

const loginType = ref<'password' | 'code'>('password')
const email = ref('')
const password = ref('')
const code = ref('')
const loading = ref(false)
const error = ref('')
const codeSent = ref(false)

// 修改密码相关
const showForgotPassword = ref(false)
const forgotPasswordEmail = ref('')
const forgotPasswordCode = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const forgotPasswordCodeSent = ref(false)
const successMessage = ref('')

const handleSendCode = async () => {
  if (!email.value) {
    error.value = '请输入邮箱'
    return
  }
  try {
    loading.value = true
    error.value = ''
    await userApi.getCode(email.value)
    codeSent.value = true
    alert('验证码已发送到您的邮箱')
  } catch (err: any) {
    error.value = err.message || '发送验证码失败'
  } finally {
    loading.value = false
  }
}

const handleLogin = async () => {
  error.value = ''
  loading.value = true

  try {
    const loginData: LoginRequest = {
      email: email.value,
      ...(loginType.value === 'password' ? { password: password.value } : { code: parseInt(code.value) }),
    }

    let response
    if (loginType.value === 'password') {
      response = await userApi.loginByPassword(loginData)
    } else {
      response = await userApi.loginByCode(loginData)
    }

    if (response.data) {
      authStore.login(response.data)
      router.push('/')
    }
  } catch (err: any) {
    error.value = err.message || '登录失败，请检查网络连接或跨域配置'
    console.error('登录错误:', err)
  } finally {
    loading.value = false
  }
}

const handleSendForgotPasswordCode = async () => {
  if (!forgotPasswordEmail.value) {
    error.value = '请输入邮箱'
    return
  }
  try {
    loading.value = true
    error.value = ''
    successMessage.value = ''
    await userApi.getCode(forgotPasswordEmail.value)
    forgotPasswordCodeSent.value = true
    successMessage.value = '验证码已发送到您的邮箱'
  } catch (err: any) {
    error.value = err.message || '发送验证码失败'
  } finally {
    loading.value = false
  }
}

const handleUpdatePassword = async () => {
  error.value = ''
  successMessage.value = ''
  
  // 验证新密码和确认密码是否一致
  if (newPassword.value !== confirmPassword.value) {
    error.value = '两次输入的密码不一致'
    return
  }
  
  // 验证密码长度
  if (newPassword.value.length < 2 || newPassword.value.length > 10) {
    error.value = '密码长度必须在2-10个字符之间'
    return
  }
  
  loading.value = true

  try {
    const updateData: LoginRequest = {
      email: forgotPasswordEmail.value,
      code: parseInt(forgotPasswordCode.value),
      password: newPassword.value,
    }

    await userApi.updatePassword(updateData)
    successMessage.value = '密码修改成功，请使用新密码登录'
    
    // 3秒后自动返回登录页面
    setTimeout(() => {
      showForgotPassword.value = false
      loginType.value = 'password' // 确保返回时显示密码登录
      resetForgotPasswordForm()
      // 自动填充邮箱到登录表单
      email.value = forgotPasswordEmail.value
    }, 3000)
  } catch (err: any) {
    error.value = err.message || '修改密码失败'
    console.error('修改密码错误:', err)
  } finally {
    loading.value = false
  }
}

const handleReturnToLogin = () => {
  showForgotPassword.value = false
  loginType.value = 'password' // 确保返回时显示密码登录
  resetForgotPasswordForm()
}

const resetForgotPasswordForm = () => {
  forgotPasswordEmail.value = ''
  forgotPasswordCode.value = ''
  newPassword.value = ''
  confirmPassword.value = ''
  forgotPasswordCodeSent.value = false
  error.value = ''
  successMessage.value = ''
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  background: white;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
}

.login-card h2 {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

.login-tabs {
  display: flex;
  margin-bottom: 30px;
  border-bottom: 2px solid #f0f0f0;
}

.login-tabs button {
  flex: 1;
  padding: 12px;
  border: none;
  background: none;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  border-bottom: 2px solid transparent;
  margin-bottom: -2px;
  transition: all 0.2s ease;
  position: relative;
}

.login-tabs button::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 50%;
  width: 0;
  height: 2px;
  background: #1890ff;
  transform: translateX(-50%);
  transition: width 0.3s ease;
}

.login-tabs button.active {
  color: #1890ff;
  border-bottom-color: #1890ff;
}

.login-tabs button.active::after {
  width: 100%;
}

.login-tabs button:active {
  background-color: rgba(24, 144, 255, 0.05);
  opacity: 0.9;
}

.code-input-group {
  display: flex;
  gap: 10px;
}

.code-input-group input {
  flex: 1;
}

.login-btn {
  width: 100%;
  margin-top: 20px;
}

.success-message {
  color: #52c41a;
  font-size: 14px;
  margin-bottom: 15px;
  padding: 10px;
  background-color: #f6ffed;
  border: 1px solid #b7eb8f;
  border-radius: 4px;
}

/* 淡入淡出动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

/* 登录卡片动画 */
.login-card {
  animation: slideIn 0.5s ease;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(30px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}
</style>
