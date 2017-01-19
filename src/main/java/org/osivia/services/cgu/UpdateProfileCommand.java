package org.osivia.services.cgu;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.automation.client.Constants;
import org.nuxeo.ecm.automation.client.OperationRequest;
import org.nuxeo.ecm.automation.client.Session;
import org.nuxeo.ecm.automation.client.model.Document;

import fr.toutatice.portail.cms.nuxeo.api.INuxeoCommand;

public class UpdateProfileCommand implements INuxeoCommand {

    protected static final Log logger = LogFactory.getLog(UpdateProfileCommand.class);

    private final String userName;
    private final int levelAgreement;


    public UpdateProfileCommand(String userName, int levelAgreement) {
        super();
        this.userName = userName;
        this.levelAgreement = levelAgreement;

    }


    @Override
    public Object execute(Session automationSession) throws Exception {

        OperationRequest newRequest = automationSession.newRequest("Services.GetToutaticeUserProfile");
        newRequest.set("username", this.userName);
        Document userProfile = (Document) newRequest.execute();

        // Get Full doc
        Document doc = (Document) automationSession.newRequest("Document.Fetch").setHeader(Constants.HEADER_NX_SCHEMAS, "*").set("value", userProfile.getPath()).execute();
        

        Map<String, String> propertiesToUpdate = new HashMap<String, String>();

        propertiesToUpdate.put("userprofile:terms_of_use_agreement", Integer.toString(levelAgreement));

        OperationRequest majFicheProfil = automationSession.newRequest("Document.Update").setInput(doc);


        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> property : propertiesToUpdate.entrySet()) {
            if (StringUtils.isNotBlank(property.getValue())) {
                sb.append(property.getKey());
                sb.append("=");
                sb.append(property.getValue());
                sb.append("\n");
            }
        }

        majFicheProfil.set("properties", sb.toString());


        return majFicheProfil.execute();

    }

    @Override
    public String getId() {
        return "UpdateProfilCommand";
    }
}
