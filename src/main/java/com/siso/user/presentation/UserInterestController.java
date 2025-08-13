package com.siso.user.presentation;

import com.siso.common.response.SisoResponse;
import com.siso.user.application.UserInterestService;
import com.siso.user.domain.model.UserInterest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user_interests")
@RequiredArgsConstructor
public class UserInterestController {
    private final UserInterestService userInterestService;
    
}
