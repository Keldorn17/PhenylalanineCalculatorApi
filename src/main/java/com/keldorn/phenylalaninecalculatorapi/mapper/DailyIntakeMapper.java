package com.keldorn.phenylalaninecalculatorapi.mapper;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.DailyIntake;
import com.keldorn.phenylalaninecalculatorapi.dto.dailyintake.DailyIntakeResponse;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface DailyIntakeMapper {

    DailyIntakeMapper INSTANCE = Mappers.getMapper(DailyIntakeMapper.class);

    DailyIntakeResponse toResponse(DailyIntake dailyIntake);

}
