package eu.epitech;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.builder.api.DefaultApi10a;
import com.github.scribejava.core.builder.api.DefaultApi20;
import com.github.scribejava.core.model.*;
import com.github.scribejava.core.oauth.OAuth10aService;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.github.scribejava.core.oauth.OAuthService;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickListener;
import eu.epitech.API.ApiInfo;

import java.io.IOException;

@SuppressWarnings("serial")
public class GetTestComponent extends Panel {

	private final VerticalLayout layout = new VerticalLayout();
	private final Token accessToken;
	private final ApiInfo service;
	private TextArea responseArea;

	public GetTestComponent(ApiInfo service, Token accessToken) {
		setSizeFull();

		setContent(layout);
		layout.setSizeFull();

		this.service = service;
		this.accessToken = accessToken;

		layout.setMargin(true);
		layout.setSpacing(true);
		final TextField field = new TextField("Request:", service.exampleGetRequest);
		field.setWidth("100%");
		layout.addComponent(field);

		Button bu = new Button("GET");
		layout.addComponent(bu);
		bu.addClickListener((ClickListener) event -> sendGet(field.getValue()));

		responseArea = new TextArea("Response:");
		responseArea.setSizeFull();
		layout.addComponent(responseArea);
		layout.setExpandRatio(responseArea, 1);
	}

	private void sendGet(String get) {
		final OAuthService service = createOAuthService();
		final OAuthRequest request = new OAuthRequest(Verb.GET, get, service);
		if (service instanceof OAuth20Service) {
			((OAuth20Service) service).signRequest((OAuth2AccessToken) accessToken, request);
		} else {
			((OAuth10aService) service).signRequest((OAuth1AccessToken) accessToken, request);
		}
		Response resp = request.send();
		try {
			responseArea.setValue(resp.getBody());
		} catch (IOException e) {
			responseArea.setValue(e.getClass() + ": " + e.getMessage());
		}
	}

	private OAuthService createOAuthService() {
		final ServiceBuilder sb =  new ServiceBuilder()
				.apiKey(service.apiKey)
				.apiSecret(service.apiSecret)
				.callback("http://www.google.fi");
		if (service.scribeApi instanceof DefaultApi10a) {
			return sb.build((DefaultApi10a) service.scribeApi);
		} else {
			return sb.build((DefaultApi20) service.scribeApi);
		}
	}
}

