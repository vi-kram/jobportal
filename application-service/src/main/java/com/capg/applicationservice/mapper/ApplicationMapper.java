package com.capg.applicationservice.mapper;

import com.capg.applicationservice.dto.response.ApplicationResponse;
import com.capg.applicationservice.entity.Application;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ApplicationMapper {

    ApplicationResponse toResponse(Application application);
}
