package com.police.kb.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.police.kb.domain.entity.Conversation;
import com.police.kb.mapper.ConversationMapper;
import com.police.kb.service.ConversationService;
import org.springframework.stereotype.Service;

@Service
public class ConversationServiceImpl extends ServiceImpl<ConversationMapper, Conversation> implements ConversationService {
}
