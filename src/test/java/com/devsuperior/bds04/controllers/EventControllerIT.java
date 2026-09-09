package com.devsuperior.bds04.controllers;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.devsuperior.bds04.dto.EventDTO;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class EventControllerIT {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Test
	public void insertShouldReturn401WhenInvalidToken() throws Exception {

		EventDTO dto = new EventDTO(null, "Expo XP", LocalDate.of(2021, 5, 18), "https://expoxp.com.br", 1L);
		String jsonBody = objectMapper.writeValueAsString(dto);
		
		ResultActions result =
					mockMvc.perform(post("/events")
					.content(jsonBody)
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isUnauthorized());
	}

	@Test
	public void insertShouldInsertResourceWhenClientLoggedAndCorrectData() throws Exception {

		LocalDate nextMonth = LocalDate.now().plusMonths(1L);
		
		EventDTO dto = new EventDTO(null, "Expo XP", nextMonth, "https://expoxp.com.br", 1L);
		String jsonBody = objectMapper.writeValueAsString(dto);
		
		ResultActions result =
					mockMvc.perform(post("/events").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_OPERATOR")))
					.content(jsonBody)
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isCreated());
		result.andExpect(jsonPath("$.id").exists());
		result.andExpect(jsonPath("$.name").value("Expo XP"));
		result.andExpect(jsonPath("$.date").value(nextMonth.toString()));
		result.andExpect(jsonPath("$.url").value("https://expoxp.com.br"));
		result.andExpect(jsonPath("$.cityId").value(1L));
	}

	@Test
	public void insertShouldInsertResourceWhenAdminLoggedAndCorrectData() throws Exception {

		LocalDate nextMonth = LocalDate.now().plusMonths(1L);
		
		EventDTO dto = new EventDTO(null, "Expo XP", nextMonth, "https://expoxp.com.br", 1L);
		String jsonBody = objectMapper.writeValueAsString(dto);
		
		ResultActions result = 
					mockMvc.perform(post("/events").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
					.content(jsonBody)
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON))
					.andDo(print());
		
		result.andExpect(status().isCreated());
		result.andExpect(jsonPath("$.id").exists());
		result.andExpect(jsonPath("$.name").value("Expo XP"));
		result.andExpect(jsonPath("$.date").value(nextMonth.toString()));
		result.andExpect(jsonPath("$.url").value("https://expoxp.com.br"));
		result.andExpect(jsonPath("$.cityId").value(1L));
	}

	@Test
	public void insertShouldReturn422WhenAdminLoggedAndBlankName() throws Exception {


		LocalDate nextMonth = LocalDate.now().plusMonths(1L);
		
		EventDTO dto = new EventDTO(null, "      ", nextMonth, "https://expoxp.com.br", 1L);
		String jsonBody = objectMapper.writeValueAsString(dto);
		
		ResultActions result =
					mockMvc.perform(post("/events").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
					.content(jsonBody)
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isUnprocessableContent());
		result.andExpect(jsonPath("$.errors[0].fieldName").value("name"));
		result.andExpect(jsonPath("$.errors[0].message").value("Campo requerido"));
	}

	@Test
	public void insertShouldReturn422WhenAdminLoggedAndPastDate() throws Exception {

		LocalDate pastMonth = LocalDate.now().minusMonths(1L);
		
		EventDTO dto = new EventDTO(null, "Expo XP", pastMonth, "https://expoxp.com.br", 1L);
		String jsonBody = objectMapper.writeValueAsString(dto);
		
		ResultActions result =
					mockMvc.perform(post("/events").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
					.content(jsonBody)
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isUnprocessableContent());
		result.andExpect(jsonPath("$.errors[0].fieldName").value("date"));
		result.andExpect(jsonPath("$.errors[0].message").value("A data do evento não pode ser passada"));
	}

	@Test
	public void insertShouldReturn422WhenAdminLoggedAndNullCity() throws Exception {

		LocalDate nextMonth = LocalDate.now().plusMonths(1L);
		
		EventDTO dto = new EventDTO(null, "Expo XP", nextMonth, "https://expoxp.com.br", null);
		String jsonBody = objectMapper.writeValueAsString(dto);
		
		ResultActions result =
					mockMvc.perform(post("/events").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
					.content(jsonBody)
					.contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON));
		
		result.andExpect(status().isUnprocessableContent());
		result.andExpect(jsonPath("$.errors[0].fieldName").value("cityId"));
		result.andExpect(jsonPath("$.errors[0].message").value("Campo obrigatório"));
	}

	@Test
	public void findAllShouldReturnPagedResources() throws Exception {
		
		ResultActions result =
				mockMvc.perform(get("/events")
						.contentType(MediaType.APPLICATION_JSON));

		result.andExpect(status().isOk());
		result.andExpect(jsonPath("$.content").exists());
	}	
}
