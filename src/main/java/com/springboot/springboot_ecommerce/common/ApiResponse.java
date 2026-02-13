package com.springboot.springboot_ecommerce.common;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponse<T> {

    private int status;
    private String message;
    private T data;
}
