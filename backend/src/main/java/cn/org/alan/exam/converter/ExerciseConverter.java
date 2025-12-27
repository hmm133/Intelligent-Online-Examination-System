package cn.org.alan.exam.converter;

import cn.org.alan.exam.model.entity.ExerciseRecord;
import cn.org.alan.exam.model.form.exercise.ExerciseFillAnswerFrom;
import cn.org.alan.exam.model.vo.question.QuestionVO;
import cn.org.alan.exam.model.vo.exercise.AnswerInfoVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.springframework.stereotype.Component;


@Component
@Mapper(componentModel="spring")
public interface ExerciseConverter {
    @Mappings({
            @Mapping(source = "quId",target = "questionId"),
            @Mapping(source = "quType",target = "questionType")
    })
    ExerciseRecord fromToEntity(ExerciseFillAnswerFrom exerciseFillAnswerFrom);

    AnswerInfoVO quVOToAnswerInfoVO(QuestionVO questionVO);
}
