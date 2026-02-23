package com.anime.domain;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

	private String answer;
	private java.util.List<String> recommendations;
	private String source;
	
	public ChatResponse(String answer, List<String> recommendations) {
        this.answer = answer;
        this.recommendations = recommendations;
    }
}
