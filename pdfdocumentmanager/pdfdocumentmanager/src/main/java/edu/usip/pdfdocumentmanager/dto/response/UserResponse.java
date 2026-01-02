package edu.usip.pdfdocumentmanager.dto.response;

import edu.usip.pdfdocumentmanager.model.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {
    private String name;
    private String phone;
    private Role role;
}