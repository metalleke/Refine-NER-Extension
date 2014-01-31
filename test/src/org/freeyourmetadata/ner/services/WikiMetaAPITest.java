package org.freeyourmetadata.ner.services;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

import org.json.JSONException;
import org.json.JSONTokener;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class WikiMetaAPITest extends APITest {

    @Override
    @BeforeTest
    public void init() {
        logger = LoggerFactory.getLogger(this.getClass());
    }
    
    WikiMetaAPI api;

    @BeforeMethod
    public void SetUp(){
    	api = new WikiMetaAPI();
    	api.setExtractionSettingDefault("Language", "EN");
    }

    @AfterMethod
    public void TearDown(){
        api = null;
    }

	@Test
	public void parseExtractionResponseEntityValid() throws URISyntaxException {
		InputStream stream = WikiMetaAPITest.class.getResourceAsStream("/wikimeta.json");
		JSONTokener tokener = new JSONTokener(new InputStreamReader(stream));
		NamedEntity[] result = null;
		try {
			result = api.parseExtractionResponseEntity(tokener);
		} catch (JSONException e) {
			Assert.fail();
		}
		Assert.assertNotNull(result);
		Assert.assertEquals(result.length, 2);
		Assert.assertEquals(result[0].getDisambiguations().length, 1);
		Assert.assertEquals(result[0].getDisambiguations()[0].getLabel(), "Antibes");		
		Assert.assertEquals(result[0].getDisambiguations()[0].getScore(), new Double("826.85"));
		Assert.assertEquals(result[0].getDisambiguations()[0].getUri(), new URI("http://wikimeta.com/wapi/display.pl?query=Antibes&search=FR"));
		Assert.assertEquals(result[1].getDisambiguations().length, 1);
		Assert.assertEquals(result[1].getDisambiguations()[0].getLabel(), "Test");
		Assert.assertEquals(result[1].getDisambiguations()[0].getScore(), new Double("826.85"));
		Assert.assertEquals(result[1].getDisambiguations()[0].getUri(), new URI("http://wikimeta.com/wapi/display.pl?query=Test&search=EN"));



	}
	
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void parseExtractionResponseEntityOverLimit() throws JSONException, IllegalArgumentException {
		InputStream stream = WikiMetaAPITest.class.getResourceAsStream("/wikimeta_over_limit.json");
		JSONTokener tokener = new JSONTokener(new InputStreamReader(stream));
		api.parseExtractionResponseEntity(tokener);
		Assert.fail();
	}
	
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void parseExtractionResponseEntityUnknownUser() throws JSONException, IllegalArgumentException {
		InputStream stream = WikiMetaAPITest.class.getResourceAsStream("/wikimeta_unknown_user.json");
		JSONTokener tokener = new JSONTokener(new InputStreamReader(stream));
		api.parseExtractionResponseEntity(tokener);
		Assert.fail();
	}
}
