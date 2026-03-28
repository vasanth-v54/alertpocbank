package com.notification.consumer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification.consumer.entity.TemplateMaster;
import com.notification.consumer.repository.TemplateMasterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TemplateService {

	private final TemplateMasterRepository repository;
/*
	public TemplateService(TemplateMasterRepository repository) {
		super();
		this.repository = repository;
	}
*/

	public Map<String, TemplateMaster> findTemplates(String config, String payload) {

		List<TemplateMaster> templates = repository.findByIsActiveTrue();

		Map<String, TemplateMaster> resultMap = new HashMap<>();

		for (TemplateMaster template : templates) {

			try {

				if (isEqual(template.getTemplateIdentifiersRef(), config)) {

					// 🔥 KEY = message_type column
					String messageType = template.getMessageType();

					resultMap.put(messageType, template);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return resultMap;
	}

	public static boolean isEqual(String json1, String json2) {
		try {
			ObjectMapper mapper = new ObjectMapper();

			JsonNode node1 = mapper.readTree(json1);
			JsonNode node2 = mapper.readTree(json2);

			return node1.equals(node2);

		} catch (Exception e) {
			throw new RuntimeException("Invalid JSON", e);
		}
	}
}
