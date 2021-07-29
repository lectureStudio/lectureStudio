package org.lecturestudio.web.api.model;

public class UserName {
    /**
     * Encapsulates the display name of a user.
     *
     * @author Alex Andres
     */


    /**
     * The display name of a user.
     */
    private String displayname;


    /**
     * @return The display name of a user.
     */
    public String getDisplayName() {
        return displayname;
    }

    @Override
    public String toString() {
        return "DisplayName [displayname=" + displayname + "]";
    }


}
