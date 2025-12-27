package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.converter.UserConverter;
import cn.org.alan.exam.mapper.UserMapper;
import cn.org.alan.exam.model.entity.User;
import cn.org.alan.exam.model.form.user.UserForm;
import cn.org.alan.exam.model.vo.user.UserVO;
import cn.org.alan.exam.utils.SecurityUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 用户管理业务测试
 * 对应: src/main/java/.../UserServiceImpl.java
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock private UserMapper userMapper;
    @Mock private UserConverter userConverter;

    /**
     * 测试点: 创建用户
     * 验证是否对密码进行了加密（不等于明文）
     */
    @Test
    @DisplayName("测试创建用户(createUser)")
    void testCreateUser() {
        try (MockedStatic<SecurityUtil> securityUtilMock = Mockito.mockStatic(SecurityUtil.class)) {
            // 模拟管理员操作
            securityUtilMock.when(SecurityUtil::getRoleCode).thenReturn(1);

            UserForm form = new UserForm();
            form.setUserName("testUser");
            form.setRoleId(1); // 学生
            form.setPassword("123456"); // 原始密码（虽然代码里会覆盖它）

            User userEntity = new User();
            when(userConverter.fromToEntity(form)).thenReturn(userEntity);
            when(userMapper.insert(any(User.class))).thenReturn(1);

            // 执行
            userService.createUser(form);

            // 验证
            verify(userMapper).insert(any(User.class));
            // 关键验证：存入数据库前，form里的密码应该已经被加密了，不等于 "123456"
            Assertions.assertNotNull(form.getPassword());
            Assertions.assertNotEquals("123456", form.getPassword(), "密码未加密直接存库！");
        }
    }

    /**
     * 测试点: 获取用户信息
     * 验证返回给前端的数据中，密码字段是否已被清空（安全要求）
     */
    @Test
    @DisplayName("测试获取用户信息(info)")
    void testInfo() {
        try (MockedStatic<SecurityUtil> securityUtilMock = Mockito.mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getUserId).thenReturn(1001);

            UserVO mockUserVO = new UserVO();
            mockUserVO.setUserName("alan");
            mockUserVO.setPassword("encodedPassword"); // 数据库里查出来是有密码的

            when(userMapper.info(1001)).thenReturn(mockUserVO);

            // 执行
            UserVO result = userService.info().getData();

            // 验证
            Assertions.assertEquals("alan", result.getUserName());
            Assertions.assertNull(result.getPassword(), "返回前端前必须清除密码字段");
        }
    }
}