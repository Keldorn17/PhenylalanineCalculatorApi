package com.keldorn.phenylalaninecalculatorapi.service;

import com.keldorn.phenylalaninecalculatorapi.domain.entity.FoodType;
import com.keldorn.phenylalaninecalculatorapi.dto.foodtype.FoodTypeRequest;
import com.keldorn.phenylalaninecalculatorapi.dto.foodtype.FoodTypeResponse;
import com.keldorn.phenylalaninecalculatorapi.exception.notfound.FoodTypeNotFoundException;
import com.keldorn.phenylalaninecalculatorapi.factory.TestEntityFactory;
import com.keldorn.phenylalaninecalculatorapi.mapper.FoodTypeMapper;
import com.keldorn.phenylalaninecalculatorapi.repository.FoodTypeRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FoodTypeServiceTests {

    @Mock
    private FoodTypeRepository foodTypeRepository;
    @Spy
    private FoodTypeMapper foodTypeMapper = Mappers.getMapper(FoodTypeMapper.class);

    @InjectMocks
    private FoodTypeService foodTypeService;

    private final Long FOOD_TYPE_ID = 1L;

    @Test
    public void findById_shouldReturnFoodTypeResponse_whenFoodTypeExists() {
        FoodType foodType = TestEntityFactory.foodType();
        foodType.setId(FOOD_TYPE_ID);

        when(foodTypeRepository.findById(FOOD_TYPE_ID)).thenReturn(Optional.of(foodType));

        FoodTypeResponse response = foodTypeService.findById(FOOD_TYPE_ID);

        verify(foodTypeMapper).toResponse(foodType);

        Assertions.assertThat(response.id()).isEqualTo(foodType.getId());
        Assertions.assertThat(response.name()).isEqualTo(foodType.getName());
        Assertions.assertThat(response.multiplier()).isEqualTo(foodType.getMultiplier());
    }

    @Test
    public void findById_shouldThrowException_whenFoodTypeNotFound() {
        when(foodTypeRepository.findById(FOOD_TYPE_ID)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> foodTypeService.findById(FOOD_TYPE_ID))
                .isInstanceOf(FoodTypeNotFoundException.class);
    }

    @Test
    public void findAll_shouldReturnPageOfFoodTypeResponses() {
        FoodType foodType = TestEntityFactory.foodType();
        foodType.setId(FOOD_TYPE_ID);
        List<FoodType> foodTypeList = List.of(foodType);
        Page<FoodType> foodTypePage = new PageImpl<>(foodTypeList);

        when(foodTypeRepository.findAll(any(Pageable.class))).thenReturn(foodTypePage);

        Page<FoodTypeResponse> response = foodTypeService.findAll(0, 20);
        verify(foodTypeMapper).toResponse(foodTypeList.getFirst());

        Assertions.assertThat(response).hasSize(1);
        Assertions.assertThat(response.getContent().getFirst().id()).isEqualTo(foodType.getId());
        Assertions.assertThat(response.getContent().getFirst().name()).isEqualTo(foodType.getName());
        Assertions.assertThat(response.getContent().getFirst().multiplier()).isEqualTo(foodType.getMultiplier());
    }

    @Test
    public void save_shouldReturnSavedFoodTypeResponse() {
        FoodType foodType = TestEntityFactory.foodType();
        FoodTypeRequest request = new FoodTypeRequest(foodType.getName(), foodType.getMultiplier());

        when(foodTypeRepository.save(foodType)).thenReturn(foodType);

        FoodTypeResponse response = foodTypeService.save(request);

        verify(foodTypeMapper).toEntity(request);
        verify(foodTypeRepository).save(foodType);
        verify(foodTypeMapper).toResponse(foodType);

        Assertions.assertThat(response.id()).isEqualTo(foodType.getId());
        Assertions.assertThat(response.name()).isEqualTo(foodType.getName());
        Assertions.assertThat(response.multiplier()).isEqualTo(foodType.getMultiplier());
    }

    @Test
    public void update_shouldReturnUpdatedFoodTypeResponse_whenFoodTypeExists() {
        FoodType foodType = TestEntityFactory.foodType();
        foodType.setId(FOOD_TYPE_ID);
        FoodTypeRequest request = new FoodTypeRequest("Updated Name", 100);

        when(foodTypeRepository.findById(FOOD_TYPE_ID)).thenReturn(Optional.of(foodType));
        when(foodTypeRepository.save(any(FoodType.class))).thenReturn(foodType);

        FoodTypeResponse response = foodTypeService.update(FOOD_TYPE_ID, request);

        ArgumentCaptor<FoodType> captor = ArgumentCaptor.forClass(FoodType.class);
        verify(foodTypeRepository).save(captor.capture());

        FoodType savedEntity = captor.getValue();
        verify(foodTypeMapper).toResponse(savedEntity);

        Assertions.assertThat(savedEntity).isNotNull();
        Assertions.assertThat(savedEntity.getName()).isEqualTo(request.name());
        Assertions.assertThat(savedEntity.getMultiplier()).isEqualTo(request.multiplier());
        Assertions.assertThat(response.id()).isEqualTo(foodType.getId());
        Assertions.assertThat(response.name()).isEqualTo(request.name());
        Assertions.assertThat(response.multiplier()).isEqualTo(request.multiplier());
    }

    @Test
    public void update_shouldThrowExceptionAndSaveNothing_whenFoodTypeNotFound() {
        FoodTypeRequest request = new FoodTypeRequest("Updated Name", 100);

        when(foodTypeRepository.findById(FOOD_TYPE_ID)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> foodTypeService.update(FOOD_TYPE_ID, request))
                .isInstanceOf(FoodTypeNotFoundException.class);

        verify(foodTypeRepository, never()).save(any());
    }

    @Test
    public void delete_shouldDeleteEntity() {
        FoodType foodType = TestEntityFactory.foodType();

        when(foodTypeRepository.findById(FOOD_TYPE_ID)).thenReturn(Optional.of(foodType));

        foodTypeService.deleteById(FOOD_TYPE_ID);

        verify(foodTypeRepository).delete(foodType);
        verify(foodTypeRepository, never()).deleteById(any());
    }

    @Test
    public void delete_shouldThrowExceptionAndSaveNothing_whenFoodTypeNotFound() {
        when(foodTypeRepository.findById(FOOD_TYPE_ID)).thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> foodTypeService.deleteById(FOOD_TYPE_ID))
                        .isInstanceOf(FoodTypeNotFoundException.class);

        verify(foodTypeRepository, never()).delete(any());
        verify(foodTypeRepository, never()).deleteById(any());
    }
}
