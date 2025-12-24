package cn.org.alan.exam.mapper;

import cn.org.alan.exam.model.entity.Category;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}