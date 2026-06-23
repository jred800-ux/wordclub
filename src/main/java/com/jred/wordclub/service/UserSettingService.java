package com.jred.wordclub.service;

import com.jred.wordclub.entity.UserSetting;
import com.jred.wordclub.repository.UserSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserSettingService {

    private final UserSettingRepository userSettingRepository;

    public UserSetting getSettings(Long userId) {
        return userSettingRepository.findByUserId(userId).orElseGet(() -> {
            UserSetting s = new UserSetting();
            s.setUserId(userId);
            return s;
        });
    }

    @Transactional
    public UserSetting saveSettings(Long userId, UserSetting incoming) {
        UserSetting s = userSettingRepository.findByUserId(userId).orElseGet(() -> {
            UserSetting ns = new UserSetting();
            ns.setUserId(userId);
            return ns;
        });

        if (incoming.getNewWordCount() != null) s.setNewWordCount(incoming.getNewWordCount());
        if (incoming.getReviewRatio() != null) s.setReviewRatio(incoming.getReviewRatio());
        if (incoming.getCardOrder() != null) s.setCardOrder(incoming.getCardOrder());
        if (incoming.getLargeFont() != null) s.setLargeFont(incoming.getLargeFont());
        if (incoming.getDarkMode() != null) s.setDarkMode(incoming.getDarkMode());
        if (incoming.getExamDate() != null) s.setExamDate(incoming.getExamDate());
        if (incoming.getSelectedBookId() != null) s.setSelectedBookId(incoming.getSelectedBookId());

        return userSettingRepository.save(s);
    }
}
