package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.mapper.CertificateUserMapper;
import cn.org.alan.exam.model.entity.CertificateUser;
import cn.org.alan.exam.service.ICertificateUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class CertificateUserServiceImpl extends ServiceImpl<CertificateUserMapper, CertificateUser> implements ICertificateUserService {

}
