package com.company.security.password.infrastructure.adapter.input.rest.mapper;

import com.company.security.password.domain.model.PasswordChangeResult;
import com.company.security.password.infrastructure.adapter.input.rest.dto.response.PasswordOperationResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "default")
public interface PasswordRestMapper {

    default PasswordOperationResponse toResponse(PasswordChangeResult result) {
        return new PasswordOperationResponse(
                result.success(),
                result.message(),
                result.changedAt()
        );
    }
}
