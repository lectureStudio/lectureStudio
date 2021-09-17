package org.lecturestudio.web.api.service;

import java.net.*;
import java.io.*;

/**
 * @author Daniel Schröter
 * Class which fetches a picture from a given URL
 */
public class DLZPictureService {

    /**
     * Returns a requested picture as an InputStream
     * @param mediaId Server specified ID of the picture
     * @return picture as an InputStream
     * @throws Exception
     */
    public static InputStream getPic(String mediaId) throws Exception{
        String domain = "https://chat.etit.tu-darmstadt.de";

        DLZWebService matrixClient = new DLZWebService(new URI(domain));
        var picture = matrixClient.getMessageClient().getPicture("chat.etit.tu-darmstadt.de", mediaId);
        return picture;
    }
}
