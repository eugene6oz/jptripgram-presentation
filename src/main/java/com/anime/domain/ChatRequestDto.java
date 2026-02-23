package com.anime.domain;

import lombok.Data;

@Data
public class ChatRequestDto {

	private int malId; // 현재 상세 애니 id
	private String lang; // ko/ja/en
	private String question; // 사용자 질문
	
	
}

