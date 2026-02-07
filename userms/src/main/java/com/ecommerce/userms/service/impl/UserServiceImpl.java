package com.ecommerce.userms.service.impl;

import com.ecommerce.userms.dto.request.AddressDTO;
import com.ecommerce.userms.dto.request.UserRequest;
import com.ecommerce.userms.dto.response.UserResponse;
import com.ecommerce.userms.model.Address;
import com.ecommerce.userms.model.User;
import com.ecommerce.userms.repository.UserRepository;
import com.ecommerce.userms.service.UserService;
import com.ecommerce.userms.service.adminservice.KeycloakAdminService;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final KeycloakAdminService keycloakAdminService;

    public UserServiceImpl(UserRepository userRepository, KeycloakAdminService keycloakAdminService) {
        this.userRepository = userRepository;
        this.keycloakAdminService = keycloakAdminService;
    }

    // Mapper
    // user-> userresponse
    private UserResponse mapTouserResponse(User user){
        if(user == null){
            return null;
        }
        UserResponse userResponse=new UserResponse();
        userResponse.setId(String.valueOf(user.getId()));
        userResponse.setFirstName(user.getFirstName());
        userResponse.setKeycloakId(user.getKeycloakId());
        userResponse.setUsername(user.getUsername());
        userResponse.setLastName(user.getLastName());
        userResponse.setEmail(user.getEmail());
        userResponse.setPhone(user.getPhone());

        if(user.getAddress() != null){
            AddressDTO addressDTO=new AddressDTO();
            Address address=user.getAddress();
            addressDTO.setCity(address.getCity());
            addressDTO.setCountry(address.getCountry());
            addressDTO.setState(address.getState());
            addressDTO.setStreet(address.getStreet());
            addressDTO.setLandmark(address.getLandmark());
            addressDTO.setZipcode(address.getZipcode());

            userResponse.setAddress(addressDTO);

        }
        return userResponse;

    }
    // userRequest -> user
    private void mapUserRequestToUser(UserRequest request, User user) {

        // ---- USER FIELDS ----
        Optional.ofNullable(request.getFirstName())
                .ifPresent(user::setFirstName);

        Optional.ofNullable(request.getLastName())
                .ifPresent(user::setLastName);

        Optional.ofNullable(request.getEmail())
                .ifPresent(user::setEmail);

        Optional.ofNullable(request.getPhone())
                .ifPresent(user::setPhone);

        // ---- ADDRESS FIELDS ----
        Optional.ofNullable(request.getAddress())
                .ifPresent(addressReq -> {

                    // create address if not exists
                    if (user.getAddress() == null) {
                        user.setAddress(new Address());
                    }

                    Address address = user.getAddress();

                    Optional.ofNullable(addressReq.getStreet())
                            .ifPresent(address::setStreet);

                    Optional.ofNullable(addressReq.getCity())
                            .ifPresent(address::setCity);

                    Optional.ofNullable(addressReq.getState())
                            .ifPresent(address::setState);

                    Optional.ofNullable(addressReq.getCountry())
                            .ifPresent(address::setCountry);

                    Optional.ofNullable(addressReq.getZipcode())
                            .ifPresent(address::setZipcode);

                    Optional.ofNullable(addressReq.getLandmark())
                            .ifPresent(address::setLandmark);
                });
    }



    public List<UserResponse> fetchAllUsers(){
//        List<User> userList= userRepository.findAll();
//        List<UserResponse> userResponseList=new ArrayList<>();
//        for(User user:userList){
//            userResponseList.add(mapTouserResponse(user));
//        }
        return userRepository.findAll().stream()
                .map(this::mapTouserResponse)
                .collect(Collectors.toList());
    }

    public boolean createUser(UserRequest userRequest){

        // Step 1 — Pehle Keycloak me create
        String keycloakId = keycloakAdminService.createUser(
                userRequest.getUsername(),
                userRequest.getEmail(),
                userRequest.getFirstName(),
                userRequest.getLastName(),
                userRequest.getPassword()
        );

        try {
            // Step 2 — Default role assign (agar fail hua to exception)
            keycloakAdminService.assignRealmRoleToUser(
                    userRequest.getUsername(),
                    "USER",
                    keycloakId
            );

            // Step 3 — Ab database me save
            User user = new User();
            mapUserRequestToUser(userRequest, user);
            user.setKeycloakId(keycloakId);
            user.setUsername(userRequest.getUsername());

            userRepository.save(user);

            return true;

        } catch (Exception e) {

            keycloakAdminService.deleteUser(keycloakId);

            throw new RuntimeException("User creation failed, rolled back in Keycloak", e);
        }
    }


    public Optional<UserResponse> getUser(String id){
        return userRepository.findById(id)
                .map(u->mapTouserResponse(u));
    }

    public boolean updateUser(UserRequest updatedUser, String id) {

        return userRepository.findById(id)
                .map(user -> {
                    mapUserRequestToUser(updatedUser,user);
                    userRepository.save(user);
                    return true;
                })
                .orElse(false);
    }



    public boolean deleteUser(String id){

            if (!userRepository.existsById(id)) {
                return false;
            }

            userRepository.deleteById(id);
            return true;
    }

}
