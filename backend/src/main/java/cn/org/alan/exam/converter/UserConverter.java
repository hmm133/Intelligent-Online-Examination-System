package cn.org.alan.exam.converter;


import cn.org.alan.exam.model.entity.User;
import cn.org.alan.exam.model.form.user.UserForm;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Mapper(componentModel = "spring")
public interface UserConverter {

    User fromToEntity(UserForm userForm);

    List<User> listFromToEntity(List<UserForm> list);

}
