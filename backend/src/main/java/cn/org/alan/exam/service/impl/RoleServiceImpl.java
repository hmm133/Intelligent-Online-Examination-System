package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.mapper.RoleMapper;
import cn.org.alan.exam.model.entity.Role;
import cn.org.alan.exam.service.IRoleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements IRoleService {

}
