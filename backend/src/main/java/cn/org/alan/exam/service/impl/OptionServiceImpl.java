package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.mapper.OptionMapper;
import cn.org.alan.exam.model.entity.Option;
import cn.org.alan.exam.service.IOptionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;


@Service
public class OptionServiceImpl extends ServiceImpl<OptionMapper, Option> implements IOptionService {

}
