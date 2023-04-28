package com.akouma.veyuzwebapp.service.mapper;

import com.akouma.veyuzwebapp.dto.ClientDto;
import com.akouma.veyuzwebapp.model.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper
public interface ClientMapper {
    @Mappings({
            @Mapping(target = "id", expression = "java(encrypt(client.getId()))"),
            // other mappings
    })
    ClientDto mapToDTO(Client client);

    String encrypt(Long id);
}