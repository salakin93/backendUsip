package edu.usip.pdfdocumentmanager.dto.request;

import edu.usip.pdfdocumentmanager.model.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequest {
    @NotBlank
    private String name;

    @NotBlank
    private String phone;

    @NotNull
    private Role role;
}