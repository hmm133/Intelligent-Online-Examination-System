package cn.org.alan.exam.converter;


import cn.org.alan.exam.model.entity.UserBook;
import cn.org.alan.exam.model.vo.userbook.ReUserExamBookVO;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Mapper(componentModel = "spring")
public interface UserBookConverter {

    List<ReUserExamBookVO> listEntityToVo(List<UserBook> list);

}
