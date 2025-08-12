package com.siso.user.application;

import com.siso.user.domain.repository.UserInterestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserInterestService {
    private final UserInterestRepository userInterestRepository;


}
