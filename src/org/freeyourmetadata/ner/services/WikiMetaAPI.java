package org.freeyourmetadata.ner.services;

import static org.freeyourmetadata.util.UriUtil.createUri;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.freeyourmetadata.util.ParameterList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * WikiMeta service connector
 * 
 * Parameters:
 * <ul>
 * 	<li>span=[0-n]: Span value give the amount of word context used for disambiguation.</li>
 * 	<li>lng=[FR|EN|ES]: Force the language model used (FRench, ENglish or SPanish). This is useful when a short text sequence is not sufficient to activate the detection function (default EN).
 * 	<li>semtag=[0|1]: 0 parameter activate Named Entity Recognition (NER) only, 1 activate NER and semantic labeling. This is useful if you only need Named Entities labels (3 to 6 time faster).
 * 	<li>textmining=[1]: This option activate the text mining feature. This introduce verbs, adjective and words frequence count. 0 is default option.
 * </ul>
 * 
 * @author Kevin Van Raepenbusch
 */
public class WikiMetaAPI extends NERServiceBase {
    private final static URI SERVICEBASEURL = createUri("http://www.wikimeta.com/wapi/service");
    private final static URI DOCUMENTATIONURI = createUri("http://www.wikimeta.com/api.html");
    private final static String[] SERVICESETTINGS = { "API key" };
    private final static String[] EXTRACTIONSETTINGS = { "Language", "Span", "Treshold" };
    
    /**
     * Creates a new WikiMeta service connector
     */
	public WikiMetaAPI() {
        super(SERVICEBASEURL, DOCUMENTATIONURI, SERVICESETTINGS, EXTRACTIONSETTINGS);
        setExtractionSettingDefault("Treshold", "10");
        setExtractionSettingDefault("Span", "100");
        setExtractionSettingDefault("Language", "EN");
	}
	
	/** {@inheritDoc} */
    public boolean isConfigured() {
        return getServiceSetting("API key").length() > 0;
        		
    }
    
    /** {@inheritDoc} */
    protected HttpEntity createExtractionRequestBody(final String text, final Map<String, String> extractionSettings)
    throws UnsupportedEncodingException {
        final ParameterList parameters = new ParameterList();
        parameters.add("api", getServiceSetting("API key"));
        parameters.add("contenu", text);
        parameters.add("treshold", getExtractionSettingDefault("Treshold"));
        parameters.add("span", getExtractionSettingDefault("Span"));
        parameters.add("lng", getExtractionSettingDefault("Language"));
        parameters.add("semtag", "1");
        return parameters.toEntity();
    }
    
    /** {@inheritDoc} */
    @Override
    protected NamedEntity[] parseExtractionResponseEntity(final JSONTokener tokener) throws JSONException, IllegalArgumentException {
        // Check response status
    	// Somehow when call is invalid, over limit etc, json that is returned is also invalid
    	final JSONObject response;
    	try {
            response = (JSONObject)tokener.nextValue();
    	} catch (JSONException e) {
    		throw new IllegalArgumentException("The WikiMetaAPI request did not succeed.");
    	}
        final JSONArray document = response.getJSONArray("document");
        
        // Find all entities
        final JSONArray entities = document.getJSONObject(2).getJSONArray("Named Entities");
        final ArrayList<NamedEntity> results = new ArrayList<NamedEntity>();
        for (int i = 0; i < entities.length(); i++) {
            final JSONObject entity = entities.getJSONObject(i);
            final String entityText = entity.getString("EN");
            final URI uri = createUri(entity.getString("URI"));
            final Double score = entity.getString("confidenceScore").equals("") ? 0d : Double.parseDouble(entity.getString("confidenceScore"));

            // Put it in an array, ya never know
            final ArrayList<Disambiguation> disambiguations = new ArrayList<Disambiguation>();
            Disambiguation disambiguation = score.equals(0d) ? new Disambiguation(entityText, uri) : new Disambiguation(entityText, uri, score);
            disambiguations.add(disambiguation);
            results.add(new NamedEntity(entityText, disambiguations));
        }
        
        return results.toArray(new NamedEntity[results.size()]);
    }
}