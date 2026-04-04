package com.wms.backend.vo;

import lombok.Data;

@Data
public class LoginVO {
    private String token;
    private Long id;
    private String username;
    private String realName;
    private String role;
}
