package com.pku.service;

import com.pku.pojo.dto.UserDTO;
import com.pku.pojo.entity.User;
import org.springframework.http.HttpStatus;

import java.math.BigInteger;

public interface UserService {
    public Long registerUser(UserDTO userDTO);
    public Long loginUser(UserDTO userDTO);
    public User getUserById(Long userId);
    public boolean isAdmin(Long userId);
}
