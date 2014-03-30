package org.freeyourmetadata.ner.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;

import org.freeyourmetadata.ner.operations.NEROperation;
import org.freeyourmetadata.ner.services.NERService;
import org.freeyourmetadata.ner.services.NERServiceManager;
import org.json.JSONObject;

import com.google.refine.commands.EngineDependentCommand;
import com.google.refine.model.AbstractOperation;
import com.google.refine.model.Column;
import com.google.refine.model.Project;

/**
 * Command that starts a named-entity recognition operation
 * @author Ruben Verborgh
 */
public class ExtractionCommand extends EngineDependentCommand {
    private final NERServiceManager serviceManager;
    
    /**
     * Creates a new <tt>ExtractionCommand</tt>
     * @param serviceManager The manager whose services will be used for named-entity recognition
     */
    public ExtractionCommand(final NERServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }

    /** {@inheritDoc} */
    @Override
    protected AbstractOperation createOperation(Project project, HttpServletRequest request, JSONObject engineConfig) throws Exception {
        final String columnName = request.getParameter("column");
        final Column column = project.columnModel.getColumnByName(columnName);
        final String[] serviceNames = request.getParameterValues("services[]");
        final TreeMap<String, NERService> services = new TreeMap<String, NERService>();
        final Map<String, Map<String, String>> settings = new HashMap<String, Map<String, String>>();
        
        // Instantiate all needed services
        for (final String serviceName : serviceNames) {
            // Create the service
            final NERService service = serviceManager.getService(serviceName);
            services.put(serviceName, service);

            // Apply the service settings
            final HashMap<String, String> serviceSettings = new HashMap<String, String>();
            settings.put(serviceName, serviceSettings);
            for (final String settingName : service.getExtractionSettings()) {
            	final String settingValue = request.getParameter(serviceName + "-" + settingName);
            	serviceSettings.put(settingName, settingValue == null ? "" : settingValue);
            }
        }
        
        return new NEROperation(column, services, settings, getEngineConfig(request));
    }
}
