<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <img :src="hnuLogo" alt="湖南大学校徽与校名" class="university-logo" />
        <span class="system-label">智慧校园 · 教学考勤</span>
        <h1>人脸识别考勤系统</h1>
        <p>Hunan University Attendance System</p>
      </div>
      <el-form :model="form" :rules="rules" ref="formRef" @submit.prevent="handleLogin">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleLogin" class="login-btn">
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-footer">账号由管理员统一创建和维护</div>
      <el-collapse class="server-settings">
        <el-collapse-item title="服务器设置（首次使用）" name="server">
          <el-input v-model="serverUrl" placeholder="例如：http://192.168.1.10:8080/api" />
          <p>手机与电脑同一 Wi-Fi 时填写电脑局域网地址；正式使用请填写云端 HTTPS 地址。</p>
          <el-button plain @click="saveServer">保存服务器地址</el-button>
        </el-collapse-item>
      </el-collapse>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { getConfiguredApiBaseUrl, setApiBaseUrl } from '@/utils/request'
import hnuLogo from '@/assets/brand/hnu-logo.png'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)
const serverUrl = ref(getConfiguredApiBaseUrl() === '/api' ? '' : getConfiguredApiBaseUrl())

const applyServerUrl = (showSuccess = true) => {
  const value = serverUrl.value.trim()
  if (value && !/^https?:\/\//i.test(value)) {
    ElMessage.warning('服务器地址必须以 http:// 或 https:// 开头')
    return false
  }
  setApiBaseUrl(value)
  if (showSuccess) {
    ElMessage.success(value ? '服务器地址已保存' : '已恢复网页默认地址')
  }
  return true
}

const saveServer = () => applyServerUrl(true)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  // 手机上的用户经常输入地址后直接点登录；登录前自动应用当前地址。
  if (!applyServerUrl(false)) return
  await formRef.value.validate()
  loading.value = true

  try {
    await userStore.loginAction(form.username, form.password)
    ElMessage.success('登录成功')
    const redirect = typeof router.currentRoute.value.query.redirect === 'string'
      ? router.currentRoute.value.query.redirect
      : '/'
    await router.replace(redirect)
  } catch (error) {
    if (!error.messageShown) ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  width: 100%;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f6f1ed;
  background-image: linear-gradient(115deg, rgba(111,26,31,.97), rgba(177,47,53,.91) 44%, rgba(246,241,237,.88) 44.2%), radial-gradient(circle at 84% 16%, rgba(198,161,91,.16), transparent 31%);
}

.login-box {
  width: 400px;
  padding: 40px;
  background: #fff;
  border: 1px solid #eadedb;
  border-radius: 18px;
  box-shadow: 0 28px 70px rgba(83,25,29,.18);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.university-logo { width: 280px; max-width: 100%; height: 82px; object-fit: contain; margin-bottom: 12px; }
.system-label { display:inline-block; color:var(--brand); background:var(--brand-soft); border:1px solid #ecd0d2; border-radius:999px; padding:4px 11px; font-size:12px; letter-spacing:.7px; margin-bottom:12px; }

.login-header h1 {
  font-size: 28px;
  color: var(--text);
  margin-bottom: 8px;
}

.login-header p {
  font-size: 14px;
  color: #98a2b3;
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
}

.login-footer {
  text-align: center;
  margin-top: 20px;
  font-size: 14px;
}

.login-footer a {
  color: var(--brand);
  margin-left: 8px;
}
.server-settings { margin-top: 20px; border-top: 1px solid #eef1f5; }
.server-settings p { color:#98a2b3; font-size:12px; line-height:1.6; margin:10px 0; }
@media(max-width:600px){
  .login-container{align-items:stretch;background:#fff;padding:calc(28px + env(safe-area-inset-top)) 22px 24px;overflow-y:auto;border-top:6px solid var(--brand)}
  .login-box{width:100%;padding:32px 0;border:0;box-shadow:none;margin:auto}
  .login-header{text-align:left;margin-bottom:30px}.university-logo{width:235px;height:68px;object-position:left center}.login-header h1{font-size:26px}.login-header p{margin-top:8px}
  .login-btn{height:48px}.login-footer{margin-top:8px}
}
</style>
