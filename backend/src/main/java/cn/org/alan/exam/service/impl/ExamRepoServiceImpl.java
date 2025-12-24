package cn.org.alan.exam.service.impl;

import cn.org.alan.exam.mapper.ExamRepoMapper;
import cn.org.alan.exam.model.entity.ExamRepo;
import cn.org.alan.exam.service.IExamRepoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;


@Service
public class ExamRepoServiceImpl extends ServiceImpl<ExamRepoMapper, ExamRepo> implements IExamRepoService {

}
