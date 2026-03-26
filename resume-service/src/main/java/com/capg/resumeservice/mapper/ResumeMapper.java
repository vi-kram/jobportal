package com.capg.resumeservice.mapper;

import com.capg.resumeservice.dto.response.ResumeResponse;
import com.capg.resumeservice.entity.Resume;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ResumeMapper {

    ResumeResponse toResponse(Resume resume);
}
