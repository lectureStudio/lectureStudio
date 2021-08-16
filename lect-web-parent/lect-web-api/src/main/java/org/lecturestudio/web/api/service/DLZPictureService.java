package org.lecturestudio.web.api.service;

import org.lecturestudio.web.api.model.DLZPicture;
import org.lecturestudio.web.api.model.DLZPushMessage;

import java.net.*;
import java.io.*;
import java.util.UUID;

public class DLZPictureService {

    public static InputStream getPic() throws Exception{
        String domain = "https://chat.etit.tu-darmstadt.de";

        DLZService matrixClient = new DLZService(new URI(domain));
        var test = matrixClient.getMessageClient().getPicture("chat.etit.tu-darmstadt.de", "glezGilQYMlpwiLsdqBnloYz");
        return test;
    }
}
