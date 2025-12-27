package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.common.result.Result;
import cn.org.alan.exam.mapper.RoleMapper;
import cn.org.alan.exam.mapper.UserMapper;
import cn.org.alan.exam.model.entity.User;
import cn.org.alan.exam.model.form.auth.LoginForm;
import cn.org.alan.exam.service.ILogService;
import cn.org.alan.exam.utils.JwtUtil;
import cn.org.alan.exam.utils.SecretUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock private UserMapper userMapper;
    @Mock private StringRedisTemplate stringRedisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;
    @Mock private RoleMapper roleMapper;
    @Mock private JwtUtil jwtUtil;
    @Mock private ObjectMapper objectMapper;
    @Mock private HttpServletRequest request;
    @Mock private HttpSession session;
    @Mock private ILogService logService;

    @Test
    @DisplayName("测试用户登录成功")
    void testLogin() {
        try (MockedStatic<SecretUtils> secretUtilsMock = Mockito.mockStatic(SecretUtils.class)) {
            // 1. 模拟 Redis 操作
            when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
            when(request.getSession()).thenReturn(session);
            when(session.getId()).thenReturn("sessionId");
            // 模拟验证码校验通过 (captchaEnabled=false 或 redis有值)
            // 这里假设配置里开启了验证码，所以要模拟redis返回 "1"
            when(valueOperations.get(anyString())).thenReturn("1");

            // 2. 模拟表单和解密
            LoginForm form = new LoginForm();
            form.setUsername("admin");
            form.setPassword("encryptedPwd");
            secretUtilsMock.when(() -> SecretUtils.desEncrypt("encryptedPwd")).thenReturn("123456");

            // 3. 模拟数据库用户
            User user = new User();
            user.setId(1);
            user.setUserName("admin");
            // 模拟数据库里存的是 BCrypt 加密后的 "123456"
            user.setPassword(new BCryptPasswordEncoder().encode("123456"));
            user.setRoleId(1);
            when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);

            // 4. 模拟角色和 Token 生成
            when(roleMapper.selectCodeById(anyInt())).thenReturn(new ArrayList<>());
            when(jwtUtil.createJwt(any(), any())).thenReturn("mockToken");

            // 5. 执行
            Result<String> result = authService.login(request, form);

            // 6. 验证
            Assertions.assertEquals("登录成功", result.getMsg());
            Assertions.assertEquals("mockToken", result.getData());
            // 验证 Token 是否存入 Redis
            verify(valueOperations).set(eq("token:sessionId"), eq("mockToken"), anyLong(), any(TimeUnit.class));
        }
    }
}