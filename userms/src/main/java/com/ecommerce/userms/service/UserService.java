package com.ecommerce.userms.service;


import com.ecommerce.userms.dto.request.UserRequest;
import com.ecommerce.userms.dto.response.UserResponse;

import java.util.List;
import java.util.Optional;


public interface UserService {

     List<UserResponse> fetchAllUsers();
     boolean createUser(UserRequest user);
     Optional<UserResponse> getUser(String id);
     boolean updateUser(UserRequest updatedUser,String id);
     boolean deleteUser(String id);

}
