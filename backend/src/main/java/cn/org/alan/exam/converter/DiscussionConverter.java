package cn.org.alan.exam.converter;

import cn.org.alan.exam.model.entity.Discussion;
import cn.org.alan.exam.model.form.discussion.DiscussionForm;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;


@Component
@Mapper(componentModel="spring")
public interface DiscussionConverter {

    Discussion formToEntity(DiscussionForm discussion);
}
