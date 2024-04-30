package com.boot3.myrestapi.common.runner;

import com.boot3.myrestapi.security.userinfo.UserInfo;
import com.boot3.myrestapi.security.userinfo.UserInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class UserInfoInsertRunner implements ApplicationRunner {
    @Autowired
    UserInfoRepository userInfoRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        UserInfo userInfo = new UserInfo();
        userInfo.setName("adminboot");
        userInfo.setPassword(
                passwordEncoder.encode("pwd1"));
        userInfo.setEmail("admin@aa.com");
        userInfo.setRoles("ROLE_ADMIN,ROLE_USER");

        UserInfo userInfo2 = new UserInfo();
        userInfo2.setName("userboot");
        userInfo2.setPassword(
                passwordEncoder.encode("pwd2"));
        userInfo2.setEmail("user@aa.com");
        userInfo2.setRoles("ROLE_USER");

        userInfoRepository.saveAll(Arrays.asList(userInfo, userInfo2));

    }
}
