package com.anime.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import com.anime.domain.ChatRequestDto;
import com.anime.domain.ChatResponse;
import com.anime.service.ai.ChatService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {
	
	@Value("${app.chat.enabled:true}")
	private boolean chatEnabled;
    private final ChatService chatService;

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequestDto req) {
       

        if (!chatEnabled) {
            ChatResponse r = new ChatResponse();
            r.setAnswer("챗봇 기능은 현재 비활성화되어 있습니다.");
            r.setRecommendations(java.util.List.of());
            r.setSource("disabled");
            return r;
        }
    	
    	return chatService.ask(req);
    }
}

