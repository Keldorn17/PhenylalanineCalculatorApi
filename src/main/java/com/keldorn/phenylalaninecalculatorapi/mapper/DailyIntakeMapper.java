package com.keldorn.phenylalaninecalculatorapi.mapper;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.DailyIntake;
import com.keldorn.phenylalaninecalculatorapi.dto.dailyintake.DailyIntakeResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DailyIntakeMapper {

    DailyIntakeResponse toResponse(DailyIntake dailyIntake);
}
