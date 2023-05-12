package com.akouma.veyuzwebapp.service.mapper;

import com.akouma.veyuzwebapp.dto.DeviseDto;
import com.akouma.veyuzwebapp.model.Devise;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DeviseMapper {
    List<DeviseDto> deviseDto(List<Devise> devise);
}
