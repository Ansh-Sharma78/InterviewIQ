package com.interviewiq.ai.usage;

import com.interviewiq.auth.entity.User;
import com.interviewiq.auth.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AiUsageLogService {
    private final AiUsageLogRepository repository;
    private final UserRepository userRepository;

    public AiUsageLogService(AiUsageLogRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Long userId, String feature, String provider, String model, int promptTokens, int completionTokens, boolean success, String errorCode) {
        User user = userRepository.getReferenceById(userId);
        repository.save(new AiUsageLog(user, feature, provider, model, promptTokens, completionTokens, success, errorCode));
    }
}

