package com.airbnb.controller;

import com.airbnb.dto.LoginDto;
import com.airbnb.dto.PropertyUserDto;
import com.airbnb.dto.TokenResponse;
import com.airbnb.entity.PropertyUser;
import com.airbnb.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
    //RequestMapping is used for all crud operation
    //@RequestMapping(name="/addUser", method= RequestMethod.POST)
    @PostMapping("/addUser")
    public ResponseEntity<String> addUser(@RequestBody PropertyUserDto propertyUserDto){
        PropertyUser propertyUser = userService.addUser(propertyUserDto);
        if(propertyUser!=null){
            return new ResponseEntity<>("Registration is successful", HttpStatus.CREATED);
        }
        return new ResponseEntity<>("something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDto loginDto){
//        public ResponseEntity<String> login(@RequestBody LoginDto loginDto){
//        boolean status = userService.verifyLogin(loginDto);
//
//        if(status){
//            return new ResponseEntity<>("user signed", HttpStatus.OK);
//        }
//        return new ResponseEntity<>("invalid credentials ", HttpStatus.UNAUTHORIZED);

//        String token = userService.verifyLogin(loginDto);
//
//        if(token!=null){
//
//            return new ResponseEntity<>(token, HttpStatus.OK);
//        }
//        return new ResponseEntity<>("invalid credentials ", HttpStatus.UNAUTHORIZED);
        String token = userService.verifyLogin(loginDto);

        if(token!=null){
            TokenResponse tokenResponse = new TokenResponse();
            tokenResponse.setToken(token);
            return new ResponseEntity<>(tokenResponse, HttpStatus.OK);
        }
        return new ResponseEntity<>("invalid credentials ", HttpStatus.UNAUTHORIZED);
    }
    @GetMapping("/profile")
    public PropertyUser getCurrentUserProfile(@AuthenticationPrincipal PropertyUser user){
        return user;
    }

}
