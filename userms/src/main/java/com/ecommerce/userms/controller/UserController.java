package com.ecommerce.userms.controller;


import com.ecommerce.userms.dto.request.UserRequest;
import com.ecommerce.userms.dto.response.UserResponse;
import com.ecommerce.userms.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
//@Tag(name = "User", description = "User management APIs")
public class UserController {

   //AUTO WIRING OF SERVICES
    private final UserService userService;
    private  static Logger logger= LoggerFactory.getLogger(UserController.class);

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
   public ResponseEntity<List<UserResponse>> getAllUsers(){
       return ResponseEntity.ok(userService.fetchAllUsers());
   }

   @PostMapping
   public ResponseEntity<String> createUser(@RequestBody UserRequest requestUser){
      if(userService.createUser(requestUser)){
          return ResponseEntity.ok("User Added Successfull");
      }
      return new ResponseEntity<>("Problem in Adding", HttpStatus.INTERNAL_SERVER_ERROR);
   }

   @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable String  id){
     logger.trace("Req recieved ");
     logger.debug("debug start");
     logger.info("user id: {}",id);
     logger.warn("i want to find the id ");
     logger.error("id incorrect");

     return userService.getUser(id)
               .map(ResponseEntity::ok)
               .orElseGet(()->ResponseEntity.notFound().build());
   }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateUser(@PathVariable String  id,@RequestBody UserRequest user){
       boolean updated= userService.updateUser(user,id);
       if(updated)
           return ResponseEntity.ok("Updated Successfully !");

       return ResponseEntity.notFound().build();

    }


    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable String  id){
        boolean updated= userService.deleteUser(id);
        if(updated)
            return ResponseEntity.ok("Deleted Successfully !");

        return ResponseEntity.notFound().build();

    }








}
